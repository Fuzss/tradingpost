package fuzs.tradingpost.world.inventory;

import fuzs.tradingpost.world.entity.npc.MerchantCollection;
import net.minecraft.world.inventory.MerchantContainer;

public class TradingPostContainer extends MerchantContainer {
    private final MerchantCollection merchant;

    public TradingPostContainer(MerchantCollection merchant) {
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
