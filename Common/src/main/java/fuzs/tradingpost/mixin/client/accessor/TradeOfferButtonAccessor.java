package fuzs.tradingpost.mixin.client.accessor;

import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net.minecraft.client.gui.screens.inventory.MerchantScreen$TradeOfferButton")
public interface TradeOfferButtonAccessor {

    @Accessor("index")
    int tradingpost$getIndex();

    @Invoker("renderToolTip")
    void tradingpost$callRenderToolTip(PoseStack poseStack, int mouseX, int mouseY);
}
