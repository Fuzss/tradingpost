package fuzs.tradingpost.world.level.block.entity;

import fuzs.puzzleslib.api.block.v1.entity.TickingBlockEntity;
import fuzs.tradingpost.init.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TradingPostBlockEntity extends BlockEntity implements Nameable, TickingBlockEntity {
    public static final Component CONTAINER_COMPONENT = Component.translatable("container.trading_post");

    private final TradingPostAnimationController animationController;
    @Nullable
    private Component name;

    public TradingPostBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModRegistry.TRADING_POST_BLOCK_ENTITY_TYPE.value(), blockPos, blockState);
        this.animationController = new TradingPostAnimationController(blockPos);
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        if (this.hasCustomName()) {
            compoundTag.putString("CustomName", Component.Serializer.toJson(this.name));
        }
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        if (compoundTag.contains("CustomName", 8)) {
            this.name = Component.Serializer.fromJson(compoundTag.getString("CustomName"));
        }
    }

    @Override
    public void clientTick() {
        this.animationController.tick(this.getLevel());
    }

    public TradingPostAnimationController getAnimationController() {
        return this.animationController;
    }

    @Override
    public Component getName() {
        return this.name != null ? this.name : CONTAINER_COMPONENT;
    }

    public void setCustomName(@Nullable Component component) {
        this.name = component;
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return this.name;
    }
}
