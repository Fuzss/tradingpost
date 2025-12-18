package fuzs.tradingpost.world.level.block.entity;

import fuzs.puzzleslib.api.block.v1.entity.TickingBlockEntity;
import fuzs.tradingpost.init.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class TradingPostBlockEntity extends BlockEntity implements Nameable, TickingBlockEntity {
    public static final Component CONTAINER_COMPONENT = Component.translatable("container.trading_post");
    public static final String TAG_CUSTOM_NAME = "CustomName";

    private final ItemStationAnimationController animationController;
    @Nullable
    private Component name;

    public TradingPostBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModRegistry.TRADING_POST_BLOCK_ENTITY_TYPE.value(), blockPos, blockState);
        this.animationController = new ItemStationAnimationController(blockPos);
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        this.name = parseCustomNameSafe(valueInput, TAG_CUSTOM_NAME);
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        valueOutput.storeNullable(TAG_CUSTOM_NAME, ComponentSerialization.CODEC, this.name);
    }

    @Override
    public void clientTick() {
        this.animationController.tick(this.getLevel());
    }

    public ItemStationAnimationController getAnimationController() {
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
    protected void applyImplicitComponents(DataComponentGetter dataComponentGetter) {
        super.applyImplicitComponents(dataComponentGetter);
        this.name = dataComponentGetter.get(DataComponents.CUSTOM_NAME);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(DataComponents.CUSTOM_NAME, this.name);
    }

    @Override
    public void removeComponentsFromTag(ValueOutput valueOutput) {
        valueOutput.discard(TAG_CUSTOM_NAME);
    }
}
