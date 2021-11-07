package fuzs.tradingpost.client;

import fuzs.tradingpost.TradingPost;
import fuzs.tradingpost.client.gui.screens.inventory.TradingPostScreen;
import fuzs.tradingpost.client.renderer.blockentity.TradingPostRenderer;
import fuzs.tradingpost.registry.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.searchtree.MutableSearchTree;
import net.minecraft.client.searchtree.ReloadableSearchTree;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.stream.Stream;

@Mod.EventBusSubscriber(modid = TradingPost.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class TradingPostClient {
    public static final ResourceLocation MAGNIFYING_GLASS_LOCATION = new ResourceLocation(TradingPost.MOD_ID, "item/magnifying_glass");
    public static final SearchRegistry.Key<MerchantOffer> OFFER_SEARCH_TREE = new SearchRegistry.Key<>();

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent evt) {
        MenuScreens.register(ModRegistry.TRADING_POST_MENU_TYPE.get(), TradingPostScreen::new);
        BlockEntityRenderers.register(ModRegistry.TRADING_POST_BLOCK_ENTITY_TYPE.get(), TradingPostRenderer::new);
        createSearchTree();
    }

    @SubscribeEvent
    public static void onTextureStitch(final TextureStitchEvent.Pre evt) {
        if (evt.getMap().location().equals(InventoryMenu.BLOCK_ATLAS)) {
            evt.addSprite(MAGNIFYING_GLASS_LOCATION);
        }
    }

    private static void createSearchTree() {
        MutableSearchTree<MerchantOffer> offerSearchTree = new ReloadableSearchTree<>(offer -> Stream.of(offer.getBaseCostA(), offer.getCostB(), offer.getResult())
                .filter(itemStack -> !itemStack.isEmpty())
                .flatMap(itemStack -> itemStack.getTooltipLines(null, TooltipFlag.Default.NORMAL).stream())
                .map(tooltipLine -> ChatFormatting.stripFormatting(tooltipLine.getString()).trim())
                .filter(tooltipString -> !tooltipString.isEmpty()),
                offer -> Stream.of(offer.getBaseCostA(), offer.getCostB(), offer.getResult())
                        .filter(itemStack -> !itemStack.isEmpty())
                        .map(ItemStack::getItem)
                        .map(ForgeRegistries.ITEMS::getKey)
        );
        Minecraft.getInstance().getSearchTreeManager().register(OFFER_SEARCH_TREE, offerSearchTree);
    }
}
