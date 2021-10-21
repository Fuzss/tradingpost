package fuzs.tradingpost.mixin.client.accessor;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.inventory.MerchantScreen;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.util.text.ITextComponent;
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
    void callRenderScroller(MatrixStack matrixStack, int width, int height, MerchantOffers offers);

    @Invoker
    void callRenderButtonArrows(MatrixStack matrixStack, MerchantOffer offer, int width, int height);

    @Invoker
    void callRenderProgressBar(MatrixStack matrixStack, int width, int height, MerchantOffer activeOffer);

}
