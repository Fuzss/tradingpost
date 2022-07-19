package fuzs.tradingpost.world.level.block;

import fuzs.tradingpost.TradingPost;
import fuzs.tradingpost.config.ServerConfig;
import fuzs.tradingpost.init.ModRegistry;
import fuzs.tradingpost.mixin.accessor.VillagerAccessor;
import fuzs.tradingpost.world.entity.npc.MerchantCollection;
import fuzs.tradingpost.world.inventory.TradingPostMenu;
import fuzs.tradingpost.world.level.block.entity.TradingPostBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Nameable;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;

@SuppressWarnings("deprecation")
public class TradingPostBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final Component CONTAINER_TITLE = Component.translatable("container.trading_post");
    public static final Component NO_MERCHANT_FOUND = Component.translatable("trading_post.no_trader_found");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape LEG1 = Block.box(0.0, 0.0, 0.0, 4.0, 8.0, 4.0);
    private static final VoxelShape LEG2 = Block.box(12.0, 0.0, 0.0, 16.0, 8.0, 4.0);
    private static final VoxelShape LEG3 = Block.box(0.0, 0.0, 12.0, 4.0, 8.0, 16.0);
    private static final VoxelShape LEG4 = Block.box(12.0, 0.0, 12.0, 16.0, 8.0, 16.0);
    private static final VoxelShape TOP = Block.box(0.0, 8.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape SHAPE = Shapes.or(TOP, LEG1, LEG2, LEG3, LEG4);

    public TradingPostBlock(Properties blockProperties) {
        super(blockProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.FALSE));
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter blockReader, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState oldState, LevelAccessor level, BlockPos newPos, BlockPos oldPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(newPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, oldState, level, newPos, oldPos);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState().setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new TradingPostBlockEntity(pPos, pState);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return pLevel.isClientSide ? createTickerHelper(pBlockEntityType, ModRegistry.TRADING_POST_BLOCK_ENTITY_TYPE.get(), TradingPostBlockEntity::tickEmeraldAnimation) : null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            Vec3 blockCenterPos = Vec3.atCenterOf(pos);
            final int horizontalRange = TradingPost.CONFIG.get(ServerConfig.class).horizontalRange;
            final int verticalRange = TradingPost.CONFIG.get(ServerConfig.class).verticalRange;
            List<Entity> nearbyTraders = level.getEntitiesOfClass(Entity.class, new AABB(blockCenterPos.add(-horizontalRange, -verticalRange, -horizontalRange), blockCenterPos.add(horizontalRange, verticalRange, horizontalRange)), this::canTrade);
            if (!nearbyTraders.isEmpty()) {
                ContainerLevelAccess access = ContainerLevelAccess.create(level, pos);
                MerchantCollection merchants = new MerchantCollection(access);
                for (Entity merchant : nearbyTraders) {
                    if (merchant instanceof Villager) {
                        ((VillagerAccessor) merchant).callUpdateSpecialPrices(player);
                    }
                    merchants.addMerchant(merchant.getId(), (Merchant) merchant);
                }
                merchants.setTradingPlayer(player);
                merchants.buildOffers(merchants.getIdToOfferCountMap());
                Component title = this.getContainerTitle(level, pos);
                this.openTradingScreen(player, merchants, title, access);
            } else {
                player.displayClientMessage(NO_MERCHANT_FOUND, false);
            }
            return InteractionResult.CONSUME;
        }
    }

    private boolean canTrade(Entity entity) {
        if (TradingPost.CONFIG.get(ServerConfig.class).traderBlacklist.contains(entity.getType()) || entity.getType().is(ModRegistry.BLACKLISTED_TRADERS_TAG)) {
            return false;
        }
        if (!entity.isAlive() || !(entity instanceof Merchant) || ((Merchant) entity).getTradingPlayer() != null || ((Merchant) entity).getOffers().isEmpty()) {
            return false;
        }
        return !(entity instanceof LivingEntity) || (!((LivingEntity) entity).isSleeping() && !((LivingEntity) entity).isBaby());
    }

    private Component getContainerTitle(Level level, BlockPos pos) {
        BlockEntity tileentity = level.getBlockEntity(pos);
        return tileentity instanceof TradingPostBlockEntity ? ((Nameable) tileentity).getDisplayName() : TradingPostBlock.CONTAINER_TITLE;
    }

    private void openTradingScreen(Player player, MerchantCollection merchants, Component title, ContainerLevelAccess worldPosCallable) {
        player.openMenu(new SimpleMenuProvider((containerMenuId, playerInventory, playerEntity) -> new TradingPostMenu(containerMenuId, playerInventory, merchants, worldPosCallable), title))
                .ifPresent(containerId -> merchants.sendMerchantData(containerId, player));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        if (stack.hasCustomHoverName()) {
            BlockEntity tileentity = level.getBlockEntity(pos);
            if (tileentity instanceof TradingPostBlockEntity) {
                ((TradingPostBlockEntity) tileentity).setCustomName(stack.getHoverName());
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter blockReader, BlockPos pos, PathComputationType pathType) {
        return false;
    }
}
