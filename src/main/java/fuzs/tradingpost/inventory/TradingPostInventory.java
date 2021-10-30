package fuzs.tradingpost.inventory;

import fuzs.tradingpost.entity.merchant.MerchantCollection;
import net.minecraft.inventory.MerchantInventory;

public class TradingPostInventory extends MerchantInventory {

    private final MerchantCollection merchant;

    public TradingPostInventory(MerchantCollection merchant) {
        super(merchant);
        this.merchant = merchant;
        this.setSelectionHint(-1);
    }

    @Override
    public void updateSellItem() {
        super.updateSellItem();
        if (this.getActiveOffer() != null) {
            this.merchant.setActiveOffer(this.getActiveOffer());
        }
    }

}
