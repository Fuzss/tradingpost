package fuzs.tradingpost.tileentity;

import fuzs.tradingpost.block.TradingPostBlock;
import fuzs.tradingpost.element.TradingPostElement;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.INameable;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;

public class TradingPostTileEntity extends TileEntity implements INameable, ITickableTileEntity {

    private ITextComponent name;
    public int time;
    public float open;
    public float oOpen;
    public float rot;
    public float oRot;
    private float tRot;

    @SuppressWarnings("ConstantConditions")
    public TradingPostTileEntity() {

        super(TradingPostElement.TRADING_POST_TILE_ENTITY);
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {

        super.save(compound);
        if (this.hasCustomName()) {

            compound.putString("CustomName", ITextComponent.Serializer.toJson(this.name));
        }

        return compound;
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {

        super.load(state, compound);
        if (compound.contains("CustomName", 8)) {

            this.name = ITextComponent.Serializer.fromJson(compound.getString("CustomName"));
        }
    }

    @Override
    public void tick() {

        if (this.level == null || !this.level.isClientSide) {
            
            return;
        }
        
        this.oOpen = this.open;
        this.oRot = this.rot;
        PlayerEntity playerentity = this.level.getNearestPlayer((double) this.worldPosition.getX() + 0.5, (double) this.worldPosition.getY() + 0.5, (double) this.worldPosition.getZ() + 0.5, 3.0, false);
        if (playerentity != null) {
            
            double d0 = playerentity.getX() - ((double) this.worldPosition.getX() + 0.5);
            double d1 = playerentity.getZ() - ((double) this.worldPosition.getZ() + 0.5);
            this.tRot = (float) MathHelper.atan2(d1, d0);
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
        this.open = MathHelper.clamp(this.open, 0.0F, 1.0F);
        ++this.time;
    }

    @Override
    public ITextComponent getName() {

        return this.name != null ? this.name : TradingPostBlock.CONTAINER_TITLE;
    }

    public void setCustomName(@Nullable ITextComponent textComponent) {

        this.name = textComponent;
    }

    @Nullable
    @Override
    public ITextComponent getCustomName() {

        return this.name;
    }

}
