package fuzs.tradingpost.world.level.block.entity;

import fuzs.tradingpost.init.ModRegistry;
import fuzs.tradingpost.world.level.block.TradingPostBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TradingPostBlockEntity extends BlockEntity implements Nameable {
    private Component name;
    public int time;
    public float open;
    public float oOpen;
    public float rot;
    public float oRot;
    private float tRot;

    public TradingPostBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModRegistry.TRADING_POST_BLOCK_ENTITY_TYPE.get(), pWorldPosition, pBlockState);
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

    public static void tickEmeraldAnimation(Level pLevel, BlockPos pPos, BlockState pState, TradingPostBlockEntity pBlockEntity) {
        if (pLevel == null || !pLevel.isClientSide) return;
        pBlockEntity.oOpen = pBlockEntity.open;
        pBlockEntity.oRot = pBlockEntity.rot;
        Player playerentity = pLevel.getNearestPlayer((double) pPos.getX() + 0.5, (double) pPos.getY() + 0.5, (double) pPos.getZ() + 0.5, 3.0, false);
        if (playerentity != null) {
            double d0 = playerentity.getX() - ((double) pPos.getX() + 0.5);
            double d1 = playerentity.getZ() - ((double) pPos.getZ() + 0.5);
            pBlockEntity.tRot = (float) Mth.atan2(d1, d0);
            pBlockEntity.open += 0.1F;
        } else {
            pBlockEntity.tRot += 0.02F;
            pBlockEntity.open -= 0.1F;
        }
        while(pBlockEntity.rot >= (float) Math.PI) {
            pBlockEntity.rot -= ((float) Math.PI * 2.0F);
        }
        while(pBlockEntity.rot < -(float) Math.PI) {
            pBlockEntity.rot += ((float) Math.PI * 2.0F);
        }
        while(pBlockEntity.tRot >= (float) Math.PI) {
            pBlockEntity.tRot -= ((float) Math.PI * 2.0F);
        }
        while(pBlockEntity.tRot < -(float) Math.PI) {
            pBlockEntity.tRot += ((float) Math.PI * 2.0F);
        }
        float f2;
        f2 = pBlockEntity.tRot - pBlockEntity.rot;
        while (f2 >= (float) Math.PI) {
            f2 -= ((float) Math.PI * 2.0F);
        }
        while(f2 < -(float) Math.PI) {
            f2 += ((float) Math.PI * 2.0F);
        }
        pBlockEntity.rot += f2 * 0.4F;
        pBlockEntity.open = Mth.clamp(pBlockEntity.open, 0.0F, 1.0F);
        ++pBlockEntity.time;
    }

    @Override
    public Component getName() {
        return this.name != null ? this.name : TradingPostBlock.CONTAINER_TITLE;
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
