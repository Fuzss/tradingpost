package fuzs.tradingpost.mixin.client.accessor;

import net.minecraft.client.gui.screen.inventory.MerchantScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MerchantScreen.class)
public interface MerchantScreenAccessor {

    @Accessor
    int getScrollOff();

}
