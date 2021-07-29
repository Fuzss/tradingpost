package fuzs.tradingpost.client.renderer.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import fuzs.tradingpost.tileentity.TradingPostTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.vector.Vector3f;

public class TradingPostTileEntityRenderer extends TileEntityRenderer<TradingPostTileEntity> {

    private final ItemStack emeraldStack = new ItemStack(Items.EMERALD);

    public TradingPostTileEntityRenderer(TileEntityRendererDispatcher tileEntityRendererDispatcher) {

        super(tileEntityRendererDispatcher);
    }

    @Override
    public void render(TradingPostTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {

        float time = tileEntityIn.time + partialTicks;
        float f1 = tileEntityIn.rot - tileEntityIn.oRot;
        while (f1 >= Math.PI)
            f1 -= (Math.PI * 2F);
        while (f1 < -Math.PI)
            f1 += (Math.PI * 2F);

        float rot = tileEntityIn.oRot + f1 * partialTicks;
        float bookOpen = tileEntityIn.oOpen + (tileEntityIn.open - tileEntityIn.oOpen) * partialTicks;
        renderItem(this.emeraldStack, time, bookOpen, rot, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
    }

    private void renderItem(ItemStack item, float time, float bookOpen, float rot, MatrixStack matrix, IRenderTypeBuffer buffer, int light, int overlay) {
        matrix.pushPose();
        matrix.translate(0.5F, 1.05F, 0.5F);
        matrix.scale(0.8F, 0.8F, 0.8F);

        rot *= -180F / (float) Math.PI;
        rot -= 90F;
        rot *= bookOpen;

        matrix.mulPose(Vector3f.YP.rotationDegrees(rot));
        matrix.translate(0, bookOpen, Math.sin(bookOpen * Math.PI));
        matrix.mulPose(Vector3f.XP.rotationDegrees(-90F * (bookOpen - 1F)));

        float trans = (float) Math.sin(time * 0.06) * bookOpen * 0.2F;
        matrix.translate(0F, trans, 0F);

        ItemRenderer render = Minecraft.getInstance().getItemRenderer();
        render.renderStatic(item, ItemCameraTransforms.TransformType.FIXED, light, overlay, matrix, buffer);
        matrix.popPose();
    }

}
