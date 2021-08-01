package fuzs.tradingpost.client.gui.screen.inventory;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.tradingpost.TradingPost;
import fuzs.tradingpost.inventory.container.TradingPostContainer;
import fuzs.tradingpost.mixin.client.accessor.MerchantScreenAccessor;
import fuzs.tradingpost.mixin.client.accessor.ScreenAccessor;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.inventory.MerchantScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.MerchantContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class TradingPostScreen extends MerchantScreen {

    private static final ResourceLocation VILLAGER_LOCATION = new ResourceLocation("textures/gui/container/villager2.png");
    private static final ITextComponent DEPRECATED_TOOLTIP = new TranslationTextComponent("merchant.deprecated");
    private static final ITextComponent MERCHANT_GONE = new TranslationTextComponent("trading_post.trader_gone");

    private Button[] tradeOfferButtons = new Button[7];

    public TradingPostScreen(MerchantContainer container, PlayerInventory playerInventory, ITextComponent title) {

        super(container, playerInventory, title);
    }

    @Override
    protected void init() {

        super.init();
        this.tradeOfferButtons = this.getTradeOfferButtons(this.buttons, this.tradeOfferButtons.length);
    }

    private Button[] getTradeOfferButtons(List<Widget> buttons, int amount) {

        Button[] tradeOfferButtons = new Button[amount];
        int j = 0, length = tradeOfferButtons.length;
        for (int i = 0, size = buttons.size(); i < size && j < length; i++) {

            Widget widget = buttons.get(i);
            if (widget instanceof Button) {

                tradeOfferButtons[j++] = (Button) widget;
            }
        }

        if (j != length) {

            TradingPost.LOGGER.warn("Unable to find enough tradeOfferButtons");
        }

        return tradeOfferButtons;
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {

        ((ScreenAccessor) this).setTitle(this.getMenu().getTraders().getDisplayName());
        super.renderLabels(matrixStack, mouseX, mouseY);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTime) {

        MerchantOffers merchantoffers = this.getMenu().getOffers();
        this.setButtonsActive(merchantoffers);

        final int scrollOff = ((MerchantScreenAccessor) this).getScrollOff();
        final Slot hoveredSlot = this.hoveredSlot;
        // set offers to empty to prevent MerchantScreen::render code from running, also disabled buttons drawing tooltips by changing scrollOff, as well as tooltip for hovered slot by removing hovered slot
        this.lock(true, merchantoffers.size(), null);
        super.render(matrixStack, mouseX, mouseY, partialTime);
        // reset everything so we can do this ourselves
        this.lock(false, scrollOff, hoveredSlot);

        if (!merchantoffers.isEmpty()) {

            // normally rendered as part of background, but skipped as offers are empty when it's called
            final int shopItem = ((MerchantScreenAccessor) this).getShopItem();
            if (shopItem >= 0 && shopItem < merchantoffers.size()) {

                MerchantOffer merchantoffer = merchantoffers.get(shopItem);
                if (merchantoffer.isOutOfStock()) {

                    this.minecraft.getTextureManager().bind(VILLAGER_LOCATION);
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    blit(matrixStack, this.leftPos + 83 + 99, this.topPos + 35, this.getBlitOffset(), 311.0F, 0.0F, 28, 21, 256, 512);
                }
            }

            final int width = (this.width - this.imageWidth) / 2;
            final int height = (this.height - this.imageHeight) / 2;
            int posX = width + 5;
            int posY = height + 16 + 2;
            RenderSystem.pushMatrix();
            RenderSystem.enableRescaleNormal();
            this.minecraft.getTextureManager().bind(VILLAGER_LOCATION);
            ((MerchantScreenAccessor) this).callRenderScroller(matrixStack, width, height, merchantoffers);

            for (int i = 0, merchantoffersSize = merchantoffers.size(); i < merchantoffersSize; i++) {

                if (merchantoffers.size() <= 7 || (i >= scrollOff && i < 7 + scrollOff)) {

                    MerchantOffer merchantoffer = merchantoffers.get(i);
                    ((MerchantScreenAccessor) this).callRenderButtonArrows(matrixStack, merchantoffer, width, posY + 1);
                    if (!this.getMenu().getTraders().checkOffer(merchantoffer)) {

                        AbstractGui.fill(matrixStack, posX, posY, posX + 88, posY + 20, 822018048);
                    }

                    ItemStack itemstack = merchantoffer.getBaseCostA();
                    ItemStack itemstack1 = merchantoffer.getCostA();
                    ItemStack itemstack2 = merchantoffer.getCostB();
                    ItemStack itemstack3 = merchantoffer.getResult();
                    this.itemRenderer.blitOffset = 100.0F;
                    this.itemRenderer.renderAndDecorateFakeItem(itemstack1, posX + 5, posY + 1);

                    if (!itemstack2.isEmpty()) {

                        this.itemRenderer.renderAndDecorateFakeItem(itemstack2, posX + 35, posY + 1);
                    }

                    this.itemRenderer.renderAndDecorateFakeItem(itemstack3, posX + 68, posY + 1);

                    if (!this.getMenu().getTraders().checkOffer(merchantoffer)) {

                        RenderSystem.depthFunc(516);
                        AbstractGui.fill(matrixStack, posX, posY, posX + 88, posY + 20, 822083583);
                        RenderSystem.depthFunc(515);
                    }

                    if (itemstack.getCount() == itemstack1.getCount()) {

                        this.itemRenderer.renderGuiItemDecorations(this.font, itemstack1, posX + 5, posY + 1);
                    } else {

                        this.itemRenderer.renderGuiItemDecorations(this.font, itemstack, posX + 5, posY + 1, itemstack.getCount() == 1 ? "1" : null);
                        this.itemRenderer.renderGuiItemDecorations(this.font, itemstack1, posX + 5 + 14, posY + 1, itemstack1.getCount() == 1 ? "1" : null);
                        this.minecraft.getTextureManager().bind(VILLAGER_LOCATION);
                        this.setBlitOffset(this.getBlitOffset() + 300);
                        blit(matrixStack, posX + 5 + 7, posY + 1 + 12, this.getBlitOffset(), 0.0F, 176.0F, 9, 2, 256, 512);
                        this.setBlitOffset(this.getBlitOffset() - 300);
                    }

                    if (!itemstack2.isEmpty()) {

                        this.itemRenderer.renderGuiItemDecorations(this.font, itemstack2, posX + 35, posY + 1);
                    }

                    this.itemRenderer.renderGuiItemDecorations(this.font, itemstack3, posX + 68, posY + 1);
                    this.itemRenderer.blitOffset = 0.0F;
                    posY += 20;
                }
            }

            final MerchantOffer activeOffer = merchantoffers.get(shopItem);
            if (this.getMenu().showProgressBar()) {

                ((MerchantScreenAccessor) this).callRenderProgressBar(matrixStack, width, height, activeOffer);
            }

            if (activeOffer.isOutOfStock() && this.isHovering(186, 35, 22, 21, mouseX, mouseY) && this.getMenu().canRestock()) {
                
                this.renderTooltip(matrixStack, DEPRECATED_TOOLTIP, mouseX, mouseY);
            }

            Button[] offerButtons = this.tradeOfferButtons;
            for (int i = 0, offerButtonsLength = offerButtons.length; i < offerButtonsLength; i++) {

                Button button = offerButtons[i];
                if (button.active && button.isHovered()) {

                    button.renderToolTip(matrixStack, mouseX, mouseY);
                }

                button.visible = i < this.getMenu().getOffers().size();
            }

            posY = height + 16 + 2;
            for (int i = 0, merchantoffersSize = merchantoffers.size(); i < merchantoffersSize; i++) {

                if (merchantoffers.size() <= 7 || (i >= scrollOff && i < 7 + scrollOff)) {

                    MerchantOffer merchantoffer = merchantoffers.get(i);
                    if (!this.getMenu().getTraders().checkOffer(merchantoffer)) {

                        if (this.isHovering(posX, posY, 88, 19, mouseX + this.leftPos, mouseY + this.topPos)) {

                            this.renderTooltip(matrixStack, MERCHANT_GONE, mouseX, mouseY);
                        }
                    }

                    posY += 20;
                }
            }

            RenderSystem.popMatrix();
            RenderSystem.enableDepthTest();
        }

        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    private void setButtonsActive(MerchantOffers merchantoffers) {

        if (!merchantoffers.isEmpty()) {

            for (int i = 0, merchantoffersSize = merchantoffers.size(); i < merchantoffersSize; i++) {

                int scrollOff = ((MerchantScreenAccessor) this).getScrollOff();
                if (merchantoffers.size() <= 7 || (i >= scrollOff && i < 7 + scrollOff)) {

                    MerchantOffer offer = merchantoffers.get(i);
                    this.tradeOfferButtons[i - scrollOff].active = this.getMenu().getTraders().checkOffer(offer);
                }
            }
        }
    }

    private void lock(boolean lockOffers, int newScrollOff, Slot newHoveredSlot) {
        
        this.getMenu().lockOffers(lockOffers);
        ((MerchantScreenAccessor) this).setScrollOff(newScrollOff);
        this.hoveredSlot = newHoveredSlot;
    }

    @Override
    public TradingPostContainer getMenu() {
        
        return (TradingPostContainer) super.getMenu();
    }

}
