package fuzs.tradingpost.client;

import fuzs.puzzleslib.client.core.ClientModConstructor;
import fuzs.tradingpost.client.gui.screens.inventory.TradingPostScreen;
import fuzs.tradingpost.client.renderer.blockentity.TradingPostRenderer;
import fuzs.tradingpost.init.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.searchtree.FullTextSearchTree;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.core.Registry;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.stream.Stream;

public class TradingPostClient implements ClientModConstructor {
    public static final SearchRegistry.Key<MerchantOffer> OFFER_SEARCH_TREE = new SearchRegistry.Key<>();

    @Override
    public void onRegisterBlockEntityRenderers(BlockEntityRenderersContext context) {
        context.registerBlockEntityRenderer(ModRegistry.TRADING_POST_BLOCK_ENTITY_TYPE.get(), TradingPostRenderer::new);
    }

    @Override
    public void onRegisterMenuScreens(MenuScreensContext context) {
        context.registerMenuScreen(ModRegistry.TRADING_POST_MENU_TYPE.get(), TradingPostScreen::new);
    }

    @Override
    public void onRegisterAtlasSprites(AtlasSpritesContext context) {
        context.registerAtlasSprite(InventoryMenu.BLOCK_ATLAS, TradingPostScreen.MAGNIFYING_GLASS_LOCATION);
    }

    @Override
    public void onRegisterSearchTrees(SearchRegistryContext context) {
        context.registerSearchTree(OFFER_SEARCH_TREE, base -> new FullTextSearchTree<>(offer ->
                Stream.of(offer.getBaseCostA(), offer.getCostB(), offer.getResult())
                        .filter(itemStack -> !itemStack.isEmpty())
                        .flatMap(itemStack -> itemStack.getTooltipLines(null, TooltipFlag.Default.NORMAL).stream())
                        .map(tooltipLine -> ChatFormatting.stripFormatting(tooltipLine.getString()).trim())
                        .filter(tooltipString -> !tooltipString.isEmpty()),
                offer -> Stream.of(offer.getBaseCostA(), offer.getCostB(), offer.getResult())
                        .filter(itemStack -> !itemStack.isEmpty())
                        .map(ItemStack::getItem)
                        .map(Registry.ITEM::getKey),
                base
        ));
    }
}
