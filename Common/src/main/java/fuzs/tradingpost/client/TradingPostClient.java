package fuzs.tradingpost.client;

import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.BlockEntityRenderersContext;
import fuzs.puzzleslib.api.client.core.v1.context.BuildCreativeModeTabContentsContext;
import fuzs.puzzleslib.api.client.core.v1.context.SearchRegistryContext;
import fuzs.puzzleslib.api.core.v1.context.ModLifecycleContext;
import fuzs.tradingpost.client.gui.screens.inventory.TradingPostScreen;
import fuzs.tradingpost.client.renderer.blockentity.TradingPostRenderer;
import fuzs.tradingpost.init.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.searchtree.FullTextSearchTree;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.stream.Stream;

public class TradingPostClient implements ClientModConstructor {

    @Override
    public void onClientSetup(ModLifecycleContext context) {
        MenuScreens.register(ModRegistry.TRADING_POST_MENU_TYPE.get(), TradingPostScreen::new);
    }

    @Override
    public void onRegisterBlockEntityRenderers(BlockEntityRenderersContext context) {
        context.registerBlockEntityRenderer(ModRegistry.TRADING_POST_BLOCK_ENTITY_TYPE.get(), TradingPostRenderer::new);
    }

    @Override
    public void onRegisterSearchTrees(SearchRegistryContext context) {
        context.registerSearchTree(TradingPostScreen.OFFER_SEARCH_TREE, base -> new FullTextSearchTree<>(offer ->
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
    public void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsContext context) {
        context.registerBuildListener(CreativeModeTabs.FUNCTIONAL_BLOCKS, (featureFlagSet, output, bl) -> {
            output.accept(ModRegistry.TRADING_POST_ITEM.get());
        });
    }
}
