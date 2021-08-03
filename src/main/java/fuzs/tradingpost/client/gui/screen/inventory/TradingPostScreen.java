package fuzs.tradingpost.client.gui.screen.inventory;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.tradingpost.TradingPost;
import fuzs.tradingpost.client.element.TradingPostExtension;
import fuzs.tradingpost.inventory.container.TradingPostContainer;
import fuzs.tradingpost.item.TradingPostOffers;
import fuzs.tradingpost.mixin.client.accessor.ButtonAccessor;
import fuzs.tradingpost.mixin.client.accessor.MerchantScreenAccessor;
import fuzs.tradingpost.mixin.client.accessor.ScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.inventory.MerchantScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.ISearchTree;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.MerchantContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.network.play.client.CSelectTradePacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class TradingPostScreen extends MerchantScreen {

    private static final ResourceLocation VILLAGER_LOCATION = new ResourceLocation("textures/gui/container/villager2.png");
    private static final ResourceLocation CREATIVE_INVENTORY_LOCATION = new ResourceLocation("textures/gui/container/creative_inventory/tab_item_search.png");
    private static final ITextComponent DEPRECATED_TOOLTIP = new TranslationTextComponent("merchant.deprecated");
    private static final ITextComponent MERCHANT_GONE = new TranslationTextComponent("trading_post.trader_gone");

    private Button[] tradeOfferButtons = new Button[7];
    private TextFieldWidget searchBox;
    private boolean ignoreTextInput;

    public TradingPostScreen(MerchantContainer container, PlayerInventory playerInventory, ITextComponent title) {

        super(container, playerInventory, title);
    }

    @Override
    protected void init() {

        super.init();
        this.tradeOfferButtons = this.getTradeOfferButtons(this.buttons, this.tradeOfferButtons.length);
        for (int i = 0, length = this.tradeOfferButtons.length; i < length; i++) {

            final int index = i;
            ((ButtonAccessor) this.tradeOfferButtons[i]).setOnPress(button -> {

                MerchantScreenAccessor accessor = (MerchantScreenAccessor) this;
                final int shopItem = index + accessor.getScrollOff();
                accessor.setShopItem(shopItem);
                this.getMenu().setSelectionHint(shopItem);
                this.getMenu().tryMoveItems(shopItem);
                // get real index when sending to server
                MerchantOffers offers = this.getMenu().getOffers();
                this.minecraft.getConnection().send(new CSelectTradePacket(offers instanceof TradingPostOffers ? ((TradingPostOffers) offers).getOrigShopItem(shopItem) : shopItem));
            });
        }

        this.searchBox = new TextFieldWidget(this.font, this.leftPos + 6, this.topPos + 6, 80, 9, new TranslationTextComponent("itemGroup.search"));
        this.searchBox.setMaxLength(50);
        this.searchBox.setBordered(false);
        this.searchBox.setTextColor(16777215);
        this.children.add(this.searchBox);
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
    public void resize(Minecraft mc, int newWidth, int newHeight) {

        final String lastSearch = this.searchBox.getValue();
        super.resize(mc, newWidth, newHeight);
        this.searchBox.setValue(lastSearch);
        if (!this.searchBox.getValue().isEmpty()) {

            this.refreshSearchResults(true);
        }
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {

        ((ScreenAccessor) this).setTitle(this.getMenu().getTraders().getDisplayName());
        // we render our search box there instead, don't want to copy base method just do this
        ITextComponent tradesLabel = MerchantScreenAccessor.getTradesLabel();
        MerchantScreenAccessor.setTradesLabel(StringTextComponent.EMPTY);
        super.renderLabels(matrixStack, mouseX, mouseY);
        MerchantScreenAccessor.setTradesLabel(tradesLabel);
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

        // move this out of if block above since search may update this
        Button[] offerButtons = this.tradeOfferButtons;
        for (int i = 0, offerButtonsLength = offerButtons.length; i < offerButtonsLength; i++) {

            Button button = offerButtons[i];
            if (button.active && button.isHovered()) {

                button.renderToolTip(matrixStack, mouseX, mouseY);
            }

            button.visible = i < this.getMenu().getOffers().size();
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
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {

        super.renderBg(matrixStack, partialTicks, mouseX, mouseY);
        this.minecraft.getTextureManager().bind(CREATIVE_INVENTORY_LOCATION);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        blit(matrixStack, i + 4, j + 4, this.getBlitOffset(), 80.0F, 4.0F, 90, 12, 256, 256);
        this.searchBox.render(matrixStack, mouseX, mouseY, partialTicks);
        this.minecraft.getTextureManager().bind(VILLAGER_LOCATION);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseKey) {

        if (this.searchBox.mouseClicked(mouseX, mouseY, mouseKey)) {

            return true;
        }

        return super.mouseClicked(mouseX, mouseY, mouseKey);
    }

    @Override
    public void tick() {

        super.tick();
        this.searchBox.tick();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifierKeys) {

        this.ignoreTextInput = false;
        final String lastSearch = this.searchBox.getValue();
        if (this.searchBox.keyPressed(keyCode, scanCode, modifierKeys)) {

            if (!Objects.equals(this.searchBox.getValue(), lastSearch)) {

                this.refreshSearchResults(true);
            }

            return true;
        } else if (this.searchBox.isFocused() && this.searchBox.isVisible() && keyCode != 256) {

            return true;
        } else if (this.minecraft.options.keyChat.matches(keyCode, scanCode) && !this.searchBox.isFocused()) {

            this.ignoreTextInput = true;
            this.searchBox.setFocus(true);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifierKeys);
    }

    @Override
    public boolean charTyped(char typedChar, int modifierKeys) {

        final String lastSearch = this.searchBox.getValue();
        if (!this.ignoreTextInput && this.searchBox.charTyped(typedChar, modifierKeys)) {

            if (!Objects.equals(this.searchBox.getValue(), lastSearch)) {

                this.refreshSearchResults(true);
            }

            return true;
        }

        return super.charTyped(typedChar, modifierKeys);
    }

    public void refreshSearchResults(boolean setSelectionHint) {

        if (!(this.getMenu().getOffers() instanceof TradingPostOffers)) {

            return;
        }

        TradingPostOffers offers = (TradingPostOffers) this.getMenu().getOffers();
        MerchantScreenAccessor accessor = (MerchantScreenAccessor) this;
        int origShopItem = offers.getOrigShopItem(accessor.getShopItem());
        String search = this.searchBox.getValue();
        if (search.isEmpty()) {

            offers.clearFilter();
        } else {

            ISearchTree<MerchantOffer> isearchtree = this.minecraft.getSearchTree(TradingPostExtension.OFFER_SEARCH_TREE);
            offers.setFilter(isearchtree.search(search.toLowerCase(Locale.ROOT)));
        }

        accessor.setScrollOff(0);
        if (origShopItem == 0) {

            return;
        }

        int newShopItem = offers.getFilteredShopItem(origShopItem);
        if (newShopItem == -1) {

            newShopItem = 0;
            this.minecraft.getConnection().send(new CSelectTradePacket(0));
        }

        if (newShopItem != origShopItem) {

            accessor.setShopItem(newShopItem);
            this.getMenu().setSelectionHint(newShopItem);
        }
    }

    @Override
    public TradingPostContainer getMenu() {
        
        return (TradingPostContainer) super.getMenu();
    }

}
