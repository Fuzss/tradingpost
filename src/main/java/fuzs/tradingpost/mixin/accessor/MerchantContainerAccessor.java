package fuzs.tradingpost.mixin.accessor;

import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.inventory.MerchantInventory;
import net.minecraft.inventory.container.MerchantContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MerchantContainer.class)
public interface MerchantContainerAccessor {

    @Accessor
    void setTrader(IMerchant trader);

    @Accessor
    void setTradeContainer(MerchantInventory tradeContainer);

}
