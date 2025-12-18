package fuzs.tradingpost.mixin.client.accessor;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MerchantScreen.class)
public interface MerchantScreenAccessor {
    @Accessor("shopItem")
    int tradingpost$getShopItem();

    @Accessor("shopItem")
    void tradingpost$setShopItem(int shopItem);

    @Accessor("scrollOff")
    int tradingpost$getScrollOff();

    @Accessor("scrollOff")
    void tradingpost$setScrollOff(int scrollOff);

    @Invoker("renderScroller")
    void tradingpost$callRenderScroller(GuiGraphics guiGraphics, int width, int height, int mouseX, int mouseY, MerchantOffers offers);

    @Invoker("renderButtonArrows")
    void tradingpost$callRenderButtonArrows(GuiGraphics guiGraphics, MerchantOffer offer, int width, int height);

    @Invoker("renderProgressBar")
    void tradingpost$callRenderProgressBar(GuiGraphics guiGraphics, int width, int height, MerchantOffer activeOffer);
}
