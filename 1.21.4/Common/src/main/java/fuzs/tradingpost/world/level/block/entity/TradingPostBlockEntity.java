package fuzs.tradingpost.world.level.block.entity;

import fuzs.puzzleslib.api.block.v1.entity.TickingBlockEntity;
import fuzs.tradingpost.init.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TradingPostBlockEntity extends BlockEntity implements Nameable, TickingBlockEntity {
    public static final Component CONTAINER_COMPONENT = Component.translatable("container.trading_post");
    public static final String TAG_CUSTOM_NAME = "CustomName";

    private final TradingPostAnimationController animationController;
    @Nullable
    private Component name;

    public TradingPostBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModRegistry.TRADING_POST_BLOCK_ENTITY_TYPE.value(), blockPos, blockState);
        this.animationController = new TradingPostAnimationController(blockPos);
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider registries) {
        super.saveAdditional(compoundTag, registries);
        if (this.hasCustomName()) {
            compoundTag.putString(TAG_CUSTOM_NAME, Component.Serializer.toJson(this.name, registries));
        }
    }

    @Override
    public void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider registries) {
        super.loadAdditional(compoundTag, registries);
        if (compoundTag.contains(TAG_CUSTOM_NAME, Tag.TAG_STRING)) {
            this.name = parseCustomNameSafe(compoundTag.getString(TAG_CUSTOM_NAME), registries);
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

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        this.name = componentInput.get(DataComponents.CUSTOM_NAME);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(DataComponents.CUSTOM_NAME, this.name);
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        tag.remove(TAG_CUSTOM_NAME);
    }
}
