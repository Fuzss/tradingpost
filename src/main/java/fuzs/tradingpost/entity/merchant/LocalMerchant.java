package fuzs.tradingpost.entity.merchant;

import net.minecraft.entity.NPCMerchant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.MerchantOffers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class LocalMerchant extends NPCMerchant {
    
    private int merchantLevel;
    private boolean showProgressBar;
    private boolean canRestock;
    
    public LocalMerchant(PlayerEntity playerEntity) {
        
        super(playerEntity);
    }

    public void setOffers(@Nullable MerchantOffers merchantOffers) {

        this.overrideOffers(merchantOffers);
    }
    
    public void setXp(int xpValue) {
        
        this.overrideXp(xpValue);
    }

    public int getMerchantLevel() {

        return this.merchantLevel;
    }

    public void setMerchantLevel(int merchantLevel) {

        this.merchantLevel = merchantLevel;
    }

    public void setCanRestock(boolean canRestock) {

        this.canRestock = canRestock;
    }

    @Override
    public boolean canRestock() {

        return this.canRestock;
    }

    @Override
    public boolean showProgressBar() {

        return this.showProgressBar;
    }

    public void setShowProgressBar(boolean showProgressBar) {

        this.showProgressBar = showProgressBar;
    }

}
