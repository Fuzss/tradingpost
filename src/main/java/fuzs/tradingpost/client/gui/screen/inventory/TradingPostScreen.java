package fuzs.tradingpost.client.gui.screen.inventory;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.tradingpost.inventory.container.TradingPostContainer;
import fuzs.tradingpost.mixin.client.accessor.MerchantScreenAccessor;
import fuzs.tradingpost.mixin.client.accessor.ScreenAccessor;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.inventory.MerchantScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.MerchantContainer;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class TradingPostScreen extends MerchantScreen {

    private static final ITextComponent MERCHANT_GONE = new TranslationTextComponent("trading_post.trader_gone");

    public TradingPostScreen(MerchantContainer container, PlayerInventory playerInventory, ITextComponent title) {

        super(container, playerInventory, title);
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {

        ((ScreenAccessor) this).setTitle(((TradingPostContainer) this.menu).getTraders().getDisplayName());
        super.renderLabels(matrixStack, mouseX, mouseY);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTime) {

        super.render(matrixStack, mouseX, mouseY, partialTime);
        MerchantOffers merchantoffers = this.menu.getOffers();
        if (!merchantoffers.isEmpty()) {

            int width = (this.width - this.imageWidth) / 2;
            int height = (this.height - this.imageHeight) / 2;
            int posX = width + 5;
            int posY = height + 16 + 2;
            RenderSystem.pushMatrix();
            RenderSystem.enableRescaleNormal();
            for (int i = 0, merchantoffersSize = merchantoffers.size(); i < merchantoffersSize; i++) {

                int scrollOff = ((MerchantScreenAccessor) this).getScrollOff();
                if (merchantoffers.size() <= 7 || (i >= scrollOff && i < 7 + scrollOff)) {

                    MerchantOffer offer = merchantoffers.get(i);
                    if (!((TradingPostContainer) this.menu).getTraders().checkOffer(offer)) {

                        AbstractGui.fill(matrixStack, posX, posY, posX + 88, posY + 20, 822018048);
                        RenderSystem.depthFunc(516);
                        AbstractGui.fill(matrixStack, posX, posY, posX + 88, posY + 20, 822083583);
                        RenderSystem.depthFunc(515);

                        if (this.isHovering(posX, posY, 89, 20, mouseX - 100, mouseY)) {

                            this.renderTooltip(matrixStack, MERCHANT_GONE, mouseX, mouseY);
                        }
                    }

                    posY += 20;
                }
            }

            RenderSystem.popMatrix();
            RenderSystem.enableDepthTest();
        }
    }

//    @Override
//    public boolean mouseClicked(double mouseX, double mouseY, int buttonId) {
//
//        MerchantOffers merchantoffers = this.menu.getOffers();
//        if (!merchantoffers.isEmpty()) {
//
//            int width = (this.width - this.imageWidth) / 2;
//            int height = (this.height - this.imageHeight) / 2;
//            int posX = width + 5;
//            int posY = height + 16 + 2;
//            for (int i = 0, merchantoffersSize = merchantoffers.size(); i < merchantoffersSize; i++) {
//
//                int scrollOff = ((MerchantScreenAccessor) this).getScrollOff();
//                if (merchantoffers.size() <= 7 || (i >= scrollOff && i < 7 + scrollOff)) {
//
//                    MerchantOffer offer = merchantoffers.get(i);
//                    if (!((TradingPostContainer) this.menu).getTraders().checkOffer(offer)) {
//
//                        AbstractGui.fill(matrixStack, posX, posY, posX + 88, posY + 20, 822018048);
//                        RenderSystem.depthFunc(516);
//                        AbstractGui.fill(matrixStack, posX, posY, posX + 88, posY + 20, 822083583);
//                        RenderSystem.depthFunc(515);
//
//                        if (this.isHovering(posX, posY, 89, 20, mouseX - 100, mouseY)) {
//
//                            this.renderTooltip(matrixStack, MERCHANT_GONE, mouseX, mouseY);
//                        }
//                    }
//
//                    posY += 20;
//                }
//            }
//        }
//    }

}
