package fuzs.tradingpost.world.level.block;

import com.mojang.serialization.MapCodec;
import fuzs.puzzleslib.api.block.v1.entity.TickingEntityBlock;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
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

import java.util.List;
import java.util.OptionalInt;

public class TradingPostBlock extends BaseEntityBlock implements SimpleWaterloggedBlock, TickingEntityBlock<TradingPostBlockEntity> {
    public static final Component MISSING_MERCHANT_COMPONENT = Component.translatable("trading_post.no_trader_found");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final MapCodec<TradingPostBlock> CODEC = simpleCodec(TradingPostBlock::new);
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
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
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
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos neighborBlockPos, BlockState neighborBlockState, RandomSource randomSource) {
        if (blockState.getValue(WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
        }

        return super.updateShape(blockState,
                levelReader,
                scheduledTickAccess,
                blockPos,
                direction,
                neighborBlockPos,
                neighborBlockState,
                randomSource);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState().setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState blockState) {
        return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
    }

    @Override
    public BlockEntityType<? extends TradingPostBlockEntity> getBlockEntityType() {
        return ModRegistry.TRADING_POST_BLOCK_ENTITY_TYPE.value();
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            Vec3 atCenter = Vec3.atCenterOf(pos);
            final int horizontalRange = TradingPost.CONFIG.get(ServerConfig.class).horizontalRange;
            final int verticalRange = TradingPost.CONFIG.get(ServerConfig.class).verticalRange;
            List<Entity> traders = level.getEntitiesOfClass(Entity.class,
                    new AABB(atCenter.add(-horizontalRange, -verticalRange, -horizontalRange),
                            atCenter.add(horizontalRange, verticalRange, horizontalRange)),
                    TradingPostBlock::isAllowedToTrade);
            if (!traders.isEmpty()) {
                ContainerLevelAccess access = ContainerLevelAccess.create(level, pos);
                MerchantCollection merchants = new MerchantCollection(access);
                for (Entity merchant : traders) {
                    if (merchant instanceof Villager) {
                        ((VillagerAccessor) merchant).tradingpost$callUpdateSpecialPrices(player);
                    }
                    merchants.addMerchant(merchant.getId(), (Merchant) merchant);
                }
                merchants.setTradingPlayer(player);
                merchants.buildOffers(merchants.getIdToOfferCountMap());
                Component title;
                if (level.getBlockEntity(pos) instanceof TradingPostBlockEntity blockEntity) {
                    title = blockEntity.getDisplayName();
                } else {
                    title = TradingPostBlockEntity.CONTAINER_COMPONENT;
                }
                OptionalInt result = player.openMenu(new SimpleMenuProvider((int containerId, Inventory inventory, Player containerPlayer) -> {
                    return new TradingPostMenu(containerId, inventory, merchants, access);
                }, title));
                result.ifPresent((int containerId) -> {
                    merchants.sendMerchantData((ServerPlayer) player, containerId);
                });
            } else {
                player.displayClientMessage(MISSING_MERCHANT_COMPONENT, false);
            }

            return InteractionResult.CONSUME;
        }
    }

    public static boolean isAllowedToTrade(Entity entity) {
        if (entity.getType().is(ModRegistry.EXCLUDE_FROM_TRADING_POST_ENTITY_TYPE_TAG)) {
            return false;
        }

        if (!entity.isAlive() || !(entity instanceof Merchant merchant) || merchant.getTradingPlayer() != null ||
                merchant.getOffers().isEmpty()) {
            return false;
        }

        return !(entity instanceof LivingEntity livingEntity) || !livingEntity.isSleeping() && !livingEntity.isBaby();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Override
    public boolean isPathfindable(BlockState state, PathComputationType pathType) {
        return false;
    }
}
