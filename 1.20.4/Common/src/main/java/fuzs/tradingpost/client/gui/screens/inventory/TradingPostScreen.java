package fuzs.tradingpost.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.tradingpost.TradingPost;
import fuzs.tradingpost.mixin.client.accessor.ButtonAccessor;
import fuzs.tradingpost.mixin.client.accessor.MerchantScreenAccessor;
import fuzs.tradingpost.mixin.client.accessor.TradeOfferButtonAccessor;
import fuzs.tradingpost.network.client.C2SClearSlotsMessage;
import fuzs.tradingpost.world.inventory.TradingPostMenu;
import fuzs.tradingpost.world.item.trading.TradingPostOffers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class TradingPostScreen extends MerchantScreen {
    public static final ResourceLocation MAGNIFYING_GLASS_LOCATION = new ResourceLocation(TradingPost.MOD_ID, "item/magnifying_glass");
    public static final SearchRegistry.Key<MerchantOffer> OFFER_SEARCH_TREE = new SearchRegistry.Key<>();
    private static final ResourceLocation VILLAGER_LOCATION = new ResourceLocation("textures/gui/container/villager2.png");
    private static final ResourceLocation CREATIVE_INVENTORY_LOCATION = new ResourceLocation("textures/gui/container/creative_inventory/tab_item_search.png");
    private static final Component DEPRECATED_TOOLTIP = Component.translatable("merchant.deprecated");
    private static final Component MERCHANT_GONE = Component.translatable("trading_post.trader_gone");

    private Button[] tradeOfferButtons = new Button[7];
    private EditBox searchBox;
    private boolean ignoreTextInput;

    public TradingPostScreen(MerchantMenu container, Inventory playerInventory, Component title) {

        super(container, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.tradeOfferButtons = this.getTradeOfferButtons(this.renderables);
        for (Button tradeOfferButton : this.tradeOfferButtons) {

            ((ButtonAccessor) tradeOfferButton).tradingpost$setOnPress(button -> {

                MerchantScreenAccessor accessor = (MerchantScreenAccessor) this;
                final int shopItem = ((TradeOfferButtonAccessor) button).tradingpost$getIndex() + accessor.tradingpost$getScrollOff();
                MerchantOffers offers = this.getMenu().getOffers();
                accessor.tradingpost$setShopItem(shopItem);
                this.getMenu().setSelectionHint(shopItem);
                this.getMenu().getTraders().setActiveOffer(offers.get(shopItem));
                this.getMenu().tryMoveItems(shopItem);
                // get real index when sending to server
                this.minecraft.getConnection().send(new ServerboundSelectTradePacket(offers instanceof TradingPostOffers ? ((TradingPostOffers) offers).getOrigShopItem(shopItem) : shopItem));
            });
        }

        this.searchBox = new EditBox(this.font, this.leftPos + 13, this.topPos + 6, 80, 9, Component.translatable("itemGroup.search")) {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                // left click clears text
                if (this.isVisible() && button == InputConstants.MOUSE_BUTTON_RIGHT) {
                    this.setValue("");
                    TradingPostScreen.this.refreshSearchResults();
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }
        };
        this.searchBox.setMaxLength(50);
        this.searchBox.setBordered(false);
        this.searchBox.setTextColor(16777215);
        this.addWidget(this.searchBox);
    }

    private Button[] getTradeOfferButtons(List<Renderable> buttons) {
        Button[] tradeOfferButtons = buttons.stream().filter(button -> button instanceof TradeOfferButtonAccessor).map(button -> (Button) button).toArray(Button[]::new);
        if (tradeOfferButtons.length != 7) {
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

            this.refreshSearchResults();
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Component title = this.getMenu().getTraders().getDisplayName();
        if (title != null) {
            int traderLevel = this.menu.getTraderLevel();
            if (traderLevel > 0 && traderLevel <= 5 && this.menu.showProgressBar()) {
                title = title.copy().append(" - ").append(Component.translatable("merchant.level." + traderLevel));
            }
        } else {
            title = this.title;
        }
        guiGraphics.drawString(this.font, title, (49 + this.imageWidth / 2 - this.font.width(title) / 2), 6, 0x404040, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTime) {

        MerchantOffers merchantoffers = this.getMenu().getOffers();
        this.setButtonsActive(merchantoffers);

        final int scrollOff = ((MerchantScreenAccessor) this).tradingpost$getScrollOff();
        final Slot hoveredSlot = this.hoveredSlot;
        // set offers to empty to prevent MerchantScreen::render code from running, also disabled buttons drawing tooltips by changing scrollOff, as well as tooltip for hovered slot by removing hovered slot
        this.lock(true, merchantoffers.size(), null);
        super.render(guiGraphics, mouseX, mouseY, partialTime);
        // reset everything so we can do this ourselves
        this.lock(false, scrollOff, hoveredSlot);

        if (!merchantoffers.isEmpty()) {

            // normally rendered as part of background, but skipped as offers are empty when it's called
            final int shopItem = ((MerchantScreenAccessor) this).tradingpost$getShopItem();
            if (shopItem >= 0 && shopItem < merchantoffers.size()) {

                MerchantOffer merchantoffer = merchantoffers.get(shopItem);
                if (merchantoffer.isOutOfStock()) {
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    guiGraphics.blit(VILLAGER_LOCATION, this.leftPos + 83 + 99, this.topPos + 35, 0, 311.0F, 0.0F, 28, 21, 512, 256);
                }
            }

            final int width = (this.width - this.imageWidth) / 2;
            final int height = (this.height - this.imageHeight) / 2;
            int posX = width + 5;
            int posY = height + 16 + 2;
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, VILLAGER_LOCATION);
            ((MerchantScreenAccessor) this).tradingpost$callRenderScroller(guiGraphics, width, height, merchantoffers);

            for (int i = 0, merchantoffersSize = merchantoffers.size(); i < merchantoffersSize; i++) {

                if (merchantoffers.size() <= 7 || (i >= scrollOff && i < 7 + scrollOff)) {

                    MerchantOffer merchantoffer = merchantoffers.get(i);
                    // move this call here to render below red overlay
                    ((MerchantScreenAccessor) this).tradingpost$callRenderButtonArrows(guiGraphics, merchantoffer, width, posY + 1);
                    if (!this.getMenu().getTraders().checkOffer(merchantoffer)) {

                        guiGraphics.fill(posX, posY, posX + 88, posY + 20, 822018048);
                    }

                    ItemStack baseCostA = merchantoffer.getBaseCostA();
                    ItemStack costA = merchantoffer.getCostA();
                    ItemStack costB = merchantoffer.getCostB();
                    ItemStack result = merchantoffer.getResult();
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);

                    if (!this.getMenu().getTraders().checkOffer(merchantoffer)) {

                        RenderSystem.depthFunc(516);
                        guiGraphics.fill(posX, posY, posX + 88, posY + 20, 822083583);
                        RenderSystem.depthFunc(515);
                    }

                    this.renderAndDecorateCostA(guiGraphics, posX, posY, baseCostA, costA);

                    if (!costB.isEmpty()) {
                        guiGraphics.renderFakeItem(costB, posX + 35, posY + 1);
                        guiGraphics.renderItemDecorations(this.font, costB, posX + 35, posY + 1);
                    }

                    guiGraphics.renderFakeItem(result, posX + 68, posY + 1);
                    guiGraphics.renderItemDecorations(this.font, result, posX + 68, posY + 1);
                    guiGraphics.pose().popPose();
                    posY += 20;
                }
            }

            final MerchantOffer activeOffer = merchantoffers.get(shopItem);
            if (this.getMenu().showProgressBar()) {

                ((MerchantScreenAccessor) this).tradingpost$callRenderProgressBar(guiGraphics, width, height, activeOffer);
            }

            if (activeOffer.isOutOfStock() && this.isHovering(186, 35, 22, 21, mouseX, mouseY) && this.getMenu().canRestock()) {

                guiGraphics.renderTooltip(this.font, DEPRECATED_TOOLTIP, mouseX, mouseY);
            }

            posY = height + 16 + 2;
            for (int i = 0, merchantoffersSize = merchantoffers.size(); i < merchantoffersSize; i++) {

                if (merchantoffers.size() <= 7 || (i >= scrollOff && i < 7 + scrollOff)) {

                    MerchantOffer merchantoffer = merchantoffers.get(i);
                    if (!this.getMenu().getTraders().checkOffer(merchantoffer)) {

                        if (this.isHovering(posX, posY, 88, 19, mouseX + this.leftPos, mouseY + this.topPos)) {

                            guiGraphics.renderTooltip(this.font, MERCHANT_GONE, mouseX, mouseY);
                        }
                    }

                    posY += 20;
                }
            }
            RenderSystem.enableDepthTest();
        }

        // move this out of if block above since search may update this
        Button[] offerButtons = this.tradeOfferButtons;
        for (int i = 0, offerButtonsLength = offerButtons.length; i < offerButtonsLength; i++) {

            Button button = offerButtons[i];
            if (button.active && button.isHoveredOrFocused()) {

                ((TradeOfferButtonAccessor) button).tradingpost$callRenderToolTip(guiGraphics, mouseX, mouseY);
            }

            button.visible = i < this.getMenu().getOffers().size();
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderAndDecorateCostA(GuiGraphics guiGraphics, int posX, int posY, ItemStack baseCostA, ItemStack costA) {
        guiGraphics.renderFakeItem(costA, posX + 5, posY + 1);
        if (baseCostA.getCount() == costA.getCount()) {
            guiGraphics.renderItemDecorations(this.font, costA, posX + 5, posY + 1);
        } else {
            guiGraphics.renderItemDecorations(this.font, baseCostA, posX + 5, posY + 1, baseCostA.getCount() == 1 ? "1" : null);
            guiGraphics.renderItemDecorations(this.font, costA, posX + 5 + 14, posY + 1, costA.getCount() == 1 ? "1" : null);
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0.0F, 0.0F, 300.0F);
            guiGraphics.blit(VILLAGER_LOCATION, posX + 5 + 7, posY + 1 + 12, 0, 0.0F, 176.0F, 9, 2, 512, 256);
            guiGraphics.pose().popPose();
        }
    }

    private void setButtonsActive(MerchantOffers merchantoffers) {

        if (!merchantoffers.isEmpty()) {

            for (int i = 0, merchantoffersSize = merchantoffers.size(); i < merchantoffersSize; i++) {

                int scrollOff = ((MerchantScreenAccessor) this).tradingpost$getScrollOff();
                if (merchantoffers.size() <= 7 || (i >= scrollOff && i < 7 + scrollOff)) {

                    MerchantOffer offer = merchantoffers.get(i);
                    this.tradeOfferButtons[i - scrollOff].active = this.getMenu().getTraders().checkOffer(offer);
                }
            }
        }
    }

    private void lock(boolean lockOffers, int newScrollOff, Slot newHoveredSlot) {

        this.getMenu().lockOffers(lockOffers);
        ((MerchantScreenAccessor) this).tradingpost$setScrollOff(newScrollOff);
        this.hoveredSlot = newHoveredSlot;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTicks, mouseX, mouseY);
        this.renderSearchBox(guiGraphics, partialTicks, mouseX, mouseY);
        TextureAtlasSprite atlasSprite = this.minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(MAGNIFYING_GLASS_LOCATION);
        guiGraphics.blit(this.leftPos, this.topPos + 4, 0, 16, 16, atlasSprite);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, VILLAGER_LOCATION);
    }

    private void renderSearchBox(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(CREATIVE_INVENTORY_LOCATION, i + 11, j + 4, 0, 80.0F, 4.0F, 90, 12, 256, 256);
        this.searchBox.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseKey) {
        if (this.searchBox.mouseClicked(mouseX, mouseY, mouseKey)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, mouseKey);
    }

    @Override
    protected void containerTick() {
        this.searchBox.tick();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifierKeys) {
        this.ignoreTextInput = false;
        final String lastSearch = this.searchBox.getValue().trim();
        if (this.searchBox.keyPressed(keyCode, scanCode, modifierKeys)) {

            if (!Objects.equals(this.searchBox.getValue().trim(), lastSearch)) {

                this.refreshSearchResults();
            }

            return true;
        } else if (this.searchBox.isFocused() && this.searchBox.isVisible() && keyCode != 256) {
            return true;
        } else if (this.minecraft.options.keyChat.matches(keyCode, scanCode) && !this.searchBox.isFocused()) {
            this.ignoreTextInput = true;
            this.searchBox.setFocused(true);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifierKeys);
    }

    @Override
    public boolean charTyped(char typedChar, int modifierKeys) {

        final String lastSearch = this.searchBox.getValue().trim();
        if (!this.ignoreTextInput && this.searchBox.charTyped(typedChar, modifierKeys)) {

            if (!Objects.equals(this.searchBox.getValue().trim(), lastSearch)) {

                this.refreshSearchResults();
            }

            return true;
        }

        return super.charTyped(typedChar, modifierKeys);
    }

    public void refreshSearchResults() {

        if (!(this.getMenu().getOffers() instanceof TradingPostOffers offers)) {
            return;
        }
        String query = this.searchBox.getValue().trim();
        if (query.isEmpty()) {
            offers.clearFilter();
        } else {
            SearchTree<MerchantOffer> isearchtree = this.minecraft.getSearchTree(OFFER_SEARCH_TREE);
            offers.setFilter(isearchtree.search(query.toLowerCase(Locale.ROOT)));
        }
        ((MerchantScreenAccessor) this).tradingpost$setScrollOff(0);
        ((MerchantScreenAccessor) this).tradingpost$setShopItem(0);
        this.getMenu().setSelectionHint(-1);
        this.getMenu().getTraders().setActiveOffer(null);
        this.getMenu().clearPaymentSlots();
        TradingPost.NETWORK.sendToServer(new C2SClearSlotsMessage());
    }

    @Override
    public TradingPostMenu getMenu() {

        return (TradingPostMenu) super.getMenu();
    }

}
