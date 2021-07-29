package fuzs.tradingpost.client.element;

import fuzs.puzzleslib.element.extension.ElementExtension;
import fuzs.puzzleslib.element.side.IClientElement;
import fuzs.tradingpost.client.renderer.tileentity.TradingPostTileEntityRenderer;
import fuzs.tradingpost.element.TradingPostElement;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.inventory.MerchantScreen;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class TradingPostExtension extends ElementExtension<TradingPostElement> implements IClientElement {

    public TradingPostExtension(TradingPostElement parent) {

        super(parent);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void loadClient() {

        ScreenManager.register(TradingPostElement.TRADING_POST_CONTAINER, MerchantScreen::new);
        ClientRegistry.bindTileEntityRenderer(TradingPostElement.TRADING_POST_TILE_ENTITY, TradingPostTileEntityRenderer::new);
    }

}
