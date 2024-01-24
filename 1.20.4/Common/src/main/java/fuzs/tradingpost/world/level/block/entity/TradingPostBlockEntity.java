package fuzs.tradingpost.world.level.block.entity;

import fuzs.puzzleslib.api.block.v1.entity.TickingBlockEntity;
import fuzs.tradingpost.init.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TradingPostBlockEntity extends BlockEntity implements Nameable, TickingBlockEntity {
    public static final Component CONTAINER_COMPONENT = Component.translatable("container.trading_post");

    private Component name;
    public int time;
    public float open;
    public float oOpen;
    public float rot;
    public float oRot;
    private float tRot;

    public TradingPostBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModRegistry.TRADING_POST_BLOCK_ENTITY_TYPE.value(), blockPos, blockState);
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
        if (this.getLevel() == null || !this.getLevel().isClientSide) return;
        this.oOpen = this.open;
        this.oRot = this.rot;
        Player playerentity = this.getLevel().getNearestPlayer((double) this.getBlockPos().getX() + 0.5, (double) this.getBlockPos().getY() + 0.5, (double) this.getBlockPos().getZ() + 0.5, 3.0, false);
        if (playerentity != null) {
            double d0 = playerentity.getX() - ((double) this.getBlockPos().getX() + 0.5);
            double d1 = playerentity.getZ() - ((double) this.getBlockPos().getZ() + 0.5);
            this.tRot = (float) Mth.atan2(d1, d0);
            this.open += 0.1F;
        } else {
            this.tRot += 0.02F;
            this.open -= 0.1F;
        }
        while(this.rot >= (float) Math.PI) {
            this.rot -= ((float) Math.PI * 2.0F);
        }
        while(this.rot < -(float) Math.PI) {
            this.rot += ((float) Math.PI * 2.0F);
        }
        while(this.tRot >= (float) Math.PI) {
            this.tRot -= ((float) Math.PI * 2.0F);
        }
        while(this.tRot < -(float) Math.PI) {
            this.tRot += ((float) Math.PI * 2.0F);
        }
        float f2;
        f2 = this.tRot - this.rot;
        while (f2 >= (float) Math.PI) {
            f2 -= ((float) Math.PI * 2.0F);
        }
        while(f2 < -(float) Math.PI) {
            f2 += ((float) Math.PI * 2.0F);
        }
        this.rot += f2 * 0.4F;
        this.open = Mth.clamp(this.open, 0.0F, 1.0F);
        ++this.time;
    }

    @Override
    public Component getName() {
        return this.name != null ? this.name : CONTAINER_COMPONENT;
    }

    public void setCustomName(@Nullable Component textComponent) {
        this.name = textComponent;
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return this.name;
    }
}
