package fuzs.tradingpost.client;

import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.BlockEntityRenderersContext;
import fuzs.puzzleslib.api.client.core.v1.context.MenuScreensContext;
import fuzs.puzzleslib.api.client.searchtree.v1.SearchRegistryHelper;
import fuzs.puzzleslib.api.client.searchtree.v1.SearchTreeType;
import fuzs.tradingpost.TradingPost;
import fuzs.tradingpost.client.gui.screens.inventory.TradingPostScreen;
import fuzs.tradingpost.client.renderer.blockentity.TradingPostRenderer;
import fuzs.tradingpost.init.ModRegistry;
import net.minecraft.client.searchtree.FullTextSearchTree;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class TradingPostClient implements ClientModConstructor {
    public static final SearchTreeType<MerchantOffer> MERCHANT_OFFERS_SEARCH_TREE = new SearchTreeType<>(
            TradingPost.id("merchant_offers"));

    @Override
    public void onClientSetup() {
        SearchRegistryHelper.register(MERCHANT_OFFERS_SEARCH_TREE, (List<MerchantOffer> values) -> {
            return new FullTextSearchTree<>((MerchantOffer offer) -> Stream.of(offer.getBaseCostA(),
                    offer.getCostB(),
                    offer.getResult()
            ).filter(Predicate.not(ItemStack::isEmpty)).flatMap(SearchRegistryHelper::getTooltipLines),
                    (MerchantOffer offer) -> Stream.of(offer.getBaseCostA(), offer.getCostB(), offer.getResult())
                            .filter(Predicate.not(ItemStack::isEmpty))
                            .map(ItemStack::getItem)
                            .map(BuiltInRegistries.ITEM::getKey),
                    values
            );
        });
    }

    @Override
    public void onRegisterMenuScreens(MenuScreensContext context) {
        context.registerMenuScreen(ModRegistry.TRADING_POST_MENU_TYPE.value(), TradingPostScreen::new);
    }

    @Override
    public void onRegisterBlockEntityRenderers(BlockEntityRenderersContext context) {
        context.registerBlockEntityRenderer(ModRegistry.TRADING_POST_BLOCK_ENTITY_TYPE.value(),
                TradingPostRenderer::new
        );
    }
}
