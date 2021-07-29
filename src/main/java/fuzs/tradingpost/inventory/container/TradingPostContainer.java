package fuzs.tradingpost.inventory.container;

import fuzs.tradingpost.entity.merchant.MerchantCollection;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.MerchantContainer;

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

    public void removeMerchant(int merchantId) {

        this.traders.disableMerchant(merchantId);
    }

}
