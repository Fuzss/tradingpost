package fuzs.tradingpost.mixin.client.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.gui.screen.inventory.MerchantScreen$TradeButton")
public interface TradeButtonAccessor {

    @Accessor
    int getIndex();

}
