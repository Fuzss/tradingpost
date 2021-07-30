package fuzs.tradingpost.entity.merchant;

import net.minecraft.entity.NPCMerchant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.MerchantOffers;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LocalMerchant extends NPCMerchant {

    private ITextComponent merchantTitle;
    private final int merchantLevel;
    private final boolean showProgressBar;
    private final boolean canRestock;
    
    public LocalMerchant(PlayerEntity playerEntity, ITextComponent merchantTitle, MerchantOffers offers, int villagerLevel, int villagerXp, boolean showProgress, boolean canRestock) {
        
        super(playerEntity);
        this.merchantTitle = merchantTitle;
        this.overrideOffers(offers);
        this.merchantLevel = villagerLevel;
        this.overrideXp(villagerXp);
        this.showProgressBar = showProgress;
        this.canRestock = canRestock;
    }

    public int getMerchantLevel() {

        return this.merchantLevel;
    }

    @Override
    public boolean canRestock() {

        return this.canRestock;
    }

    @Override
    public boolean showProgressBar() {

        return this.showProgressBar;
    }

    public ITextComponent getDisplayName() {

        return this.merchantTitle;
    }

}
