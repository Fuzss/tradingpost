package fuzs.tradingpost.mixin.client.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.gui.screens.inventory.MerchantScreen$TradeOfferButton")
public interface TradeOfferButtonAccessor {

    @Accessor
    int getIndex();
}
