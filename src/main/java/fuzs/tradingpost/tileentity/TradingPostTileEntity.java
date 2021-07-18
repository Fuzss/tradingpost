package fuzs.tradingpost.tileentity;

import fuzs.tradingpost.element.TradingPostElement;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.INameable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

public class TradingPostTileEntity extends TileEntity implements INameable, ITickableTileEntity {

    private ITextComponent name;

    @SuppressWarnings("ConstantConditions")
    public TradingPostTileEntity() {

        super(TradingPostElement.TRADING_POST_TILE_ENTITY);
    }

    @Override
    public CompoundNBT save(CompoundNBT p_189515_1_) {

        super.save(p_189515_1_);
        if (this.hasCustomName()) {

            p_189515_1_.putString("CustomName", ITextComponent.Serializer.toJson(this.name));
        }

        return p_189515_1_;
    }

    @Override
    public void load(BlockState p_230337_1_, CompoundNBT p_230337_2_) {

        super.load(p_230337_1_, p_230337_2_);
        if (p_230337_2_.contains("CustomName", 8)) {

            this.name = ITextComponent.Serializer.fromJson(p_230337_2_.getString("CustomName"));
        }
    }

    @Override
    public void tick() {

    }

    @Override
    public ITextComponent getName() {

        return (ITextComponent)(this.name != null ? this.name : new TranslationTextComponent("container.enchant"));
    }

    public void setCustomName(@Nullable ITextComponent p_200229_1_) {

        this.name = p_200229_1_;
    }

    @Nullable
    @Override
    public ITextComponent getCustomName() {

        return this.name;
    }

}
