package fuzs.tradingpost.client.element;

import fuzs.puzzleslib.element.extension.ElementExtension;
import fuzs.puzzleslib.element.side.IClientElement;
import fuzs.tradingpost.client.renderer.tileentity.WorkbenchTileEntityRenderer;
import fuzs.tradingpost.element.TradingPostElement;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.inventory.CraftingScreen;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class TradingPostExtension extends ElementExtension<TradingPostElement> implements IClientElement {

    public TradingPostExtension(TradingPostElement parent) {

        super(parent);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void loadClient() {

        ScreenManager.register(TradingPostElement.CRAFTING_CONTAINER, CraftingScreen::new);
        ClientRegistry.bindTileEntityRenderer(TradingPostElement.WORKBENCH_TILE_ENTITY, WorkbenchTileEntityRenderer::new);
    }

}
