package fuzs.tradingpost.mixin.client.accessor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MerchantScreen.class)
public interface MerchantScreenAccessor {

    @Accessor
    int getShopItem();

    @Accessor
    void setShopItem(int shopItem);

    @Accessor
    int getScrollOff();

    @Accessor
    void setScrollOff(int scrollOff);

    @Invoker
    void callRenderScroller(PoseStack matrixStack, int width, int height, MerchantOffers offers);

    @Invoker
    void callRenderButtonArrows(PoseStack matrixStack, MerchantOffer offer, int width, int height);

    @Invoker
    void callRenderProgressBar(PoseStack matrixStack, int width, int height, MerchantOffer activeOffer);
}
