package fuzs.tradingpost.client.element;

import fuzs.puzzleslib.element.extension.ElementExtension;
import fuzs.puzzleslib.element.side.IClientElement;
import fuzs.tradingpost.TradingPost;
import fuzs.tradingpost.client.gui.screen.inventory.TradingPostScreen;
import fuzs.tradingpost.client.renderer.tileentity.TradingPostTileEntityRenderer;
import fuzs.tradingpost.element.TradingPostElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.util.SearchTree;
import net.minecraft.client.util.SearchTreeManager;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.stream.Stream;

public class TradingPostExtension extends ElementExtension<TradingPostElement> implements IClientElement {

    public static final ResourceLocation MAGNIFYING_GLASS_LOCATION = new ResourceLocation(TradingPost.MODID, "item/magnifying_glass");
    public static final SearchTreeManager.Key<MerchantOffer> OFFER_SEARCH_TREE = new SearchTreeManager.Key<>();

    public TradingPostExtension(TradingPostElement parent) {

        super(parent);
    }

    @Override
    public void constructClient() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onTextureStitch);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void setupClient2() {
        ScreenManager.register(TradingPostElement.TRADING_POST_CONTAINER, TradingPostScreen::new);
        ClientRegistry.bindTileEntityRenderer(TradingPostElement.TRADING_POST_TILE_ENTITY, TradingPostTileEntityRenderer::new);
        this.createSearchTree();
    }

    private void onTextureStitch(final TextureStitchEvent.Pre evt) {
        if (evt.getMap().location().equals(PlayerContainer.BLOCK_ATLAS)) {
            evt.addSprite(MAGNIFYING_GLASS_LOCATION);
        }
    }

    private void createSearchTree() {

        SearchTree<MerchantOffer> offerSearchTree = new SearchTree<>(
                offer -> Stream.of(offer.getBaseCostA(), offer.getCostB(), offer.getResult())
                .filter(itemStack -> !itemStack.isEmpty())
                .flatMap(itemStack -> itemStack.getTooltipLines(null, ITooltipFlag.TooltipFlags.NORMAL).stream())
                .map(tooltipLine -> TextFormatting.stripFormatting(tooltipLine.getString()).trim())
                .filter(tooltipString -> !tooltipString.isEmpty()),
                offer -> Stream.of(offer.getBaseCostA(), offer.getCostB(), offer.getResult())
                .filter(itemStack -> !itemStack.isEmpty())
                .map(ItemStack::getItem)
                .map(ForgeRegistries.ITEMS::getKey)
        );

        Minecraft.getInstance().getSearchTreeManager().register(OFFER_SEARCH_TREE, offerSearchTree);
    }

}
