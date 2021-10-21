package fuzs.tradingpost.block;

import fuzs.tradingpost.TradingPost;
import fuzs.tradingpost.element.TradingPostElement;
import fuzs.tradingpost.entity.merchant.MerchantCollection;
import fuzs.tradingpost.inventory.container.TradingPostContainer;
import fuzs.tradingpost.mixin.accessor.VillagerEntityAccessor;
import fuzs.tradingpost.tileentity.TradingPostTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

@SuppressWarnings("deprecation")
public class TradingPostBlock extends Block implements IWaterLoggable {

    public static final ITextComponent CONTAINER_TITLE = new TranslationTextComponent("container.trading_post");
    public static final ITextComponent NO_MERCHANT_FOUND = new TranslationTextComponent("trading_post.no_trader_found");

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape LEG1 = Block.box(0.0, 0.0, 0.0, 4.0, 8.0, 4.0);
    private static final VoxelShape LEG2 = Block.box(16.0, 0.0, 0.0, 12.0, 8.0, 4.0);
    private static final VoxelShape LEG3 = Block.box(0.0, 0.0, 16.0, 4.0, 8.0, 12.0);
    private static final VoxelShape LEG4 = Block.box(16.0, 0.0, 16.0, 12.0, 8.0, 12.0);
    private static final VoxelShape TOP = Block.box(0.0, 8.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape SHAPE = VoxelShapes.or(TOP, LEG1, LEG2, LEG3, LEG4);

    public TradingPostBlock(Properties blockProperties) {

        super(blockProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.FALSE));
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {

        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader blockReader, BlockPos pos, ISelectionContext context) {
        
        return SHAPE;
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {

        return BlockRenderType.MODEL;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState oldState, IWorld level, BlockPos newPos, BlockPos oldPos) {

        if (state.getValue(WATERLOGGED)) {

            level.getLiquidTicks().scheduleTick(newPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(state, direction, oldState, level, newPos, oldPos);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {

        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState().setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState state) {

        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {

        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {

        return new TradingPostTileEntity();
    }

    @Override
    public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {

        if (level.isClientSide) {

            return ActionResultType.SUCCESS;
        } else {

            Vector3d blockCenterPos = Vector3d.atCenterOf(pos);
            TradingPostElement element = (TradingPostElement) TradingPost.TRADING_POST;
            List<Entity> nearbyTraders = level.getEntitiesOfClass(Entity.class, new AxisAlignedBB(blockCenterPos.add(-element.horizontalRange, -element.verticalRange, -element.horizontalRange), blockCenterPos.add(element.horizontalRange, element.verticalRange, element.horizontalRange)), this::canTrade);
            if (!nearbyTraders.isEmpty()) {

                IWorldPosCallable access = IWorldPosCallable.create(level, pos);
                MerchantCollection merchants = new MerchantCollection(access, level);
                nearbyTraders.forEach(merchant -> {

                    if (merchant instanceof VillagerEntity) {

                        ((VillagerEntityAccessor) merchant).callUpdateSpecialPrices(player);
                    }

                    merchants.addMerchant(merchant.getId(), (IMerchant) merchant);
                });

                merchants.setTradingPlayer(player);
                merchants.buildOffers(merchants.getIdToOfferCountMap());
                ITextComponent title = this.getContainerTitle(level, pos);
                this.openTradingScreen(player, merchants, title, access);
            } else {

                player.displayClientMessage(NO_MERCHANT_FOUND, false);
            }

            return ActionResultType.CONSUME;
        }
    }

    private boolean canTrade(Entity entity) {

        if (((TradingPostElement) TradingPost.TRADING_POST).traderBlacklist.contains(entity.getType()) || entity.getType().is(TradingPostElement.BLACKLISTED_TRADERS_TAG)) {

            return false;
        }

        if (!entity.isAlive() || !(entity instanceof IMerchant) || ((IMerchant) entity).getTradingPlayer() != null || ((IMerchant) entity).getOffers().isEmpty()) {

            return false;
        }

        return !(entity instanceof LivingEntity) || (!((LivingEntity) entity).isSleeping() && !((LivingEntity) entity).isBaby());
    }

    private ITextComponent getContainerTitle(World level, BlockPos pos) {

        TileEntity tileentity = level.getBlockEntity(pos);
        return tileentity instanceof TradingPostTileEntity ? ((INameable) tileentity).getDisplayName() : TradingPostBlock.CONTAINER_TITLE;
    }

    private void openTradingScreen(PlayerEntity player, MerchantCollection merchants, ITextComponent title, IWorldPosCallable worldPosCallable) {

        player.openMenu(new SimpleNamedContainerProvider((containerMenuId, playerInventory, playerEntity) -> new TradingPostContainer(containerMenuId, playerInventory, merchants, worldPosCallable), title))
                .ifPresent(containerId -> merchants.sendMerchantData(containerId, player));
    }

    @Override
    public void setPlacedBy(World level, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {

        if (stack.hasCustomHoverName()) {

            TileEntity tileentity = level.getBlockEntity(pos);
            if (tileentity instanceof TradingPostTileEntity) {

                ((TradingPostTileEntity) tileentity).setCustomName(stack.getHoverName());
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {

        builder.add(WATERLOGGED);
    }

    @Override
    public boolean isPathfindable(BlockState state, IBlockReader blockReader, BlockPos pos, PathType pathType) {

        return false;
    }

}
