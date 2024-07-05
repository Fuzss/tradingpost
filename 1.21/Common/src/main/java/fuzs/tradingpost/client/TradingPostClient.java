package fuzs.tradingpost.client;

import fuzs.puzzleslib.api.client.core.v1.ClientAbstractions;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.BlockEntityRenderersContext;
import fuzs.puzzleslib.api.client.core.v1.context.MenuScreensContext;
import fuzs.tradingpost.client.gui.screens.inventory.TradingPostScreen;
import fuzs.tradingpost.client.renderer.blockentity.TradingPostRenderer;
import fuzs.tradingpost.init.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.searchtree.FullTextSearchTree;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.stream.Stream;

public class TradingPostClient implements ClientModConstructor {
    public static final SearchRegistry.Key<MerchantOffer> OFFER_SEARCH_TREE = new SearchRegistry.Key<>();

    @Override
    public void onClientSetup() {
        ClientAbstractions.INSTANCE.getSearchRegistry().register(OFFER_SEARCH_TREE, base -> new FullTextSearchTree<>(offer ->
                Stream.of(offer.getBaseCostA(), offer.getCostB(), offer.getResult())
                        .filter(itemStack -> !itemStack.isEmpty())
                        .flatMap(itemStack -> itemStack.getTooltipLines(null, TooltipFlag.Default.NORMAL).stream())
                        .map(tooltipLine -> ChatFormatting.stripFormatting(tooltipLine.getString()).trim())
                        .filter(tooltipString -> !tooltipString.isEmpty()),
                offer -> Stream.of(offer.getBaseCostA(), offer.getCostB(), offer.getResult())
                        .filter(itemStack -> !itemStack.isEmpty())
                        .map(ItemStack::getItem)
                        .map(BuiltInRegistries.ITEM::getKey),
                base
        ));
    }

    @Override
    public void onRegisterMenuScreens(MenuScreensContext context) {
        context.registerMenuScreen(ModRegistry.TRADING_POST_MENU_TYPE.value(), TradingPostScreen::new);
    }

    @Override
    public void onRegisterBlockEntityRenderers(BlockEntityRenderersContext context) {
        context.registerBlockEntityRenderer(ModRegistry.TRADING_POST_BLOCK_ENTITY_TYPE.value(), TradingPostRenderer::new);
    }
}
