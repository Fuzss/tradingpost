package fuzs.tradingpost.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.puzzleslib.client.core.ClientCoreServices;
import fuzs.tradingpost.TradingPost;
import fuzs.tradingpost.client.TradingPostClient;
import fuzs.tradingpost.mixin.client.accessor.ButtonAccessor;
import fuzs.tradingpost.mixin.client.accessor.MerchantScreenAccessor;
import fuzs.tradingpost.mixin.client.accessor.TradeOfferButtonAccessor;
import fuzs.tradingpost.network.client.message.C2SClearSlotsMessage;
import fuzs.tradingpost.world.inventory.TradingPostMenu;
import fuzs.tradingpost.world.item.trading.TradingPostOffers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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
        this.tradeOfferButtons = this.getTradeOfferButtons(ClientCoreServices.FACTORIES.screens().getRenderableButtons(this));
        for (Button tradeOfferButton : this.tradeOfferButtons) {

            ((ButtonAccessor) tradeOfferButton).setOnPress(button -> {

                MerchantScreenAccessor accessor = (MerchantScreenAccessor) this;
                final int shopItem = ((TradeOfferButtonAccessor) button).getIndex() + accessor.getScrollOff();
                MerchantOffers offers = this.getMenu().getOffers();
                accessor.setShopItem(shopItem);
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
                if (this.isVisible() && button == 1) {
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

    private Button[] getTradeOfferButtons(List<Widget> buttons) {
        Button[] tradeOfferButtons = buttons.stream()
                .filter(button -> button instanceof TradeOfferButtonAccessor)
                .map(button -> (Button) button)
                .toArray(Button[]::new);
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
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {

        Component title = this.getMenu().getTraders().getDisplayName();
        if (title != null) {
            int traderLevel = this.menu.getTraderLevel();
            if (traderLevel > 0 && traderLevel <= 5 && this.menu.showProgressBar()) {
                title = title.copy().append(" - ").append(Component.translatable("merchant.level." + traderLevel));
            }
        } else {
            title = this.title;
        }
        this.font.draw(matrixStack, title, (float)(49 + this.imageWidth / 2 - this.font.width(title) / 2), 6.0F, 4210752);
        this.font.draw(matrixStack, this.playerInventoryTitle, (float)this.inventoryLabelX, (float)this.inventoryLabelY, 4210752);
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTime) {

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
                    RenderSystem.setShader(GameRenderer::getPositionTexShader);
                    RenderSystem.setShaderTexture(0, VILLAGER_LOCATION);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    blit(matrixStack, this.leftPos + 83 + 99, this.topPos + 35, this.getBlitOffset(), 311.0F, 0.0F, 28, 21, 512, 256);
                }
            }

            final int width = (this.width - this.imageWidth) / 2;
            final int height = (this.height - this.imageHeight) / 2;
            int posX = width + 5;
            int posY = height + 16 + 2;
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, VILLAGER_LOCATION);
            ((MerchantScreenAccessor) this).callRenderScroller(matrixStack, width, height, merchantoffers);

            for (int i = 0, merchantoffersSize = merchantoffers.size(); i < merchantoffersSize; i++) {

                if (merchantoffers.size() <= 7 || (i >= scrollOff && i < 7 + scrollOff)) {

                    MerchantOffer merchantoffer = merchantoffers.get(i);
                    ((MerchantScreenAccessor) this).callRenderButtonArrows(matrixStack, merchantoffer, width, posY + 1);
                    if (!this.getMenu().getTraders().checkOffer(merchantoffer)) {

                        GuiComponent.fill(matrixStack, posX, posY, posX + 88, posY + 20, 822018048);
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
                        GuiComponent.fill(matrixStack, posX, posY, posX + 88, posY + 20, 822083583);
                        RenderSystem.depthFunc(515);
                    }

                    if (itemstack.getCount() == itemstack1.getCount()) {

                        this.itemRenderer.renderGuiItemDecorations(this.font, itemstack1, posX + 5, posY + 1);
                    } else {

                        this.itemRenderer.renderGuiItemDecorations(this.font, itemstack, posX + 5, posY + 1, itemstack.getCount() == 1 ? "1" : null);
                        this.itemRenderer.renderGuiItemDecorations(this.font, itemstack1, posX + 5 + 14, posY + 1, itemstack1.getCount() == 1 ? "1" : null);
                        RenderSystem.setShader(GameRenderer::getPositionTexShader);
                        RenderSystem.setShaderTexture(0, VILLAGER_LOCATION);
                        this.setBlitOffset(this.getBlitOffset() + 300);
                        blit(matrixStack, posX + 5 + 7, posY + 1 + 12, this.getBlitOffset(), 0.0F, 176.0F, 9, 2, 512, 256);
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
            RenderSystem.enableDepthTest();
        }

        // move this out of if block above since search may update this
        Button[] offerButtons = this.tradeOfferButtons;
        for (int i = 0, offerButtonsLength = offerButtons.length; i < offerButtonsLength; i++) {

            Button button = offerButtons[i];
            if (button.active && button.isHoveredOrFocused()) {

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
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(matrixStack, partialTicks, mouseX, mouseY);
        this.renderSearchBox(matrixStack, partialTicks, mouseX, mouseY);
        TextureAtlasSprite textureatlassprite = this.minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(MAGNIFYING_GLASS_LOCATION);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, textureatlassprite.atlas().location());
        blit(matrixStack, this.leftPos, this.topPos + 4, this.getBlitOffset(), 16, 16, textureatlassprite);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, VILLAGER_LOCATION);
    }

    private void renderSearchBox(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, CREATIVE_INVENTORY_LOCATION);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        blit(matrixStack, i + 11, j + 4, this.getBlitOffset(), 80.0F, 4.0F, 90, 12, 256, 256);
        this.searchBox.render(matrixStack, mouseX, mouseY, partialTicks);
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
        final String lastSearch = this.searchBox.getValue();
        if (this.searchBox.keyPressed(keyCode, scanCode, modifierKeys)) {

            if (!Objects.equals(this.searchBox.getValue(), lastSearch)) {

                this.refreshSearchResults();
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
        String query = this.searchBox.getValue();
        if (query.isEmpty()) {
            offers.clearFilter();
        } else {
            SearchTree<MerchantOffer> isearchtree = this.minecraft.getSearchTree(TradingPostClient.OFFER_SEARCH_TREE);
            offers.setFilter(isearchtree.search(query.toLowerCase(Locale.ROOT)));
        }
        ((MerchantScreenAccessor) this).setScrollOff(0);
        ((MerchantScreenAccessor) this).setShopItem(0);
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
