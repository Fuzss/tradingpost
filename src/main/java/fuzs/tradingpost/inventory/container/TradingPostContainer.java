package fuzs.tradingpost.inventory.container;

import fuzs.tradingpost.entity.merchant.LocalMerchant;
import fuzs.tradingpost.entity.merchant.MerchantCollection;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.MerchantContainer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TradingPostContainer extends MerchantContainer {

    private final MerchantCollection traders;

    public TradingPostContainer(int containerId, PlayerInventory playerInventory) {

        super(containerId, playerInventory);
        this.traders = new MerchantCollection(playerInventory.player);
    }

    public TradingPostContainer(int containerId, PlayerInventory playerInventory, MerchantCollection merchantCollection) {

        super(containerId, playerInventory, merchantCollection);
        this.traders = merchantCollection;
    }

    @Override
    public void setSelectionHint(int offerId) {

        super.setSelectionHint(offerId);
        this.traders.setActiveOffer(offerId);
    }

    @OnlyIn(Dist.CLIENT)
    public void setShowProgressBar(boolean showProgressBar) {

        throw new UnsupportedOperationException("Set showProgressBar to merchants directly");
    }

    @OnlyIn(Dist.CLIENT)
    public void setXp(int xpValue) {

        throw new UnsupportedOperationException("Set xp to merchants directly");
    }

    @OnlyIn(Dist.CLIENT)
    public void setMerchantLevel(int merchantLevel) {

        throw new UnsupportedOperationException("Set level to merchants directly");
    }

    @OnlyIn(Dist.CLIENT)
    public void setCanRestock(boolean canRestock) {

        throw new UnsupportedOperationException("Set canRestock to merchants directly");
    }

    @OnlyIn(Dist.CLIENT)
    public void setOffers(MerchantOffers offers) {

        throw new UnsupportedOperationException("Set offers to merchants directly");
    }

    public void addMerchant(PlayerEntity playerEntity, int merchantId, ITextComponent merchantTitle, MerchantOffers offers, int villagerLevel, int villagerXp, boolean showProgress, boolean canRestock) {

        LocalMerchant merchant = new LocalMerchant(playerEntity, merchantTitle, offers, villagerLevel, villagerXp, showProgress, canRestock);
        this.traders.addMerchant(merchantId, merchant);
    }

    public void removeMerchant(int merchantId) {

        this.traders.disableMerchant(merchantId);
    }

}
