package fuzs.tradingpost.mixin.accessor;

import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MerchantMenu.class)
public interface MerchantMenuAccessor {

    @Mutable
    @Accessor
    void setTrader(Merchant trader);

    @Mutable
    @Accessor
    void setTradeContainer(MerchantContainer tradeContainer);
}
