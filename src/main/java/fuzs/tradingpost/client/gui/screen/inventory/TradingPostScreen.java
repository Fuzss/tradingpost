package fuzs.tradingpost.client.gui.screen.inventory;

import com.mojang.blaze3d.matrix.MatrixStack;
import fuzs.tradingpost.inventory.container.TradingPostContainer;
import fuzs.tradingpost.mixin.accessor.ScreenAccessor;
import net.minecraft.client.gui.screen.inventory.MerchantScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.MerchantContainer;
import net.minecraft.util.text.ITextComponent;

public class TradingPostScreen extends MerchantScreen {

    public TradingPostScreen(MerchantContainer container, PlayerInventory playerInventory, ITextComponent title) {

        super(container, playerInventory, title);
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {

        ((ScreenAccessor) this).setTitle(((TradingPostContainer) this.menu).getContainerTitle());
        super.renderLabels(matrixStack, mouseX, mouseY);
    }

}
