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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

/**
 * mostly copied from Quark's matrix enchanting table by Vazkii
 * https://github.com/Vazkii/Quark/blob/master/src/main/java/vazkii/quark/addons/oddities/client/render/MatrixEnchantingTableTileEntityRenderer.java
 */
public class TradingPostTileEntityRenderer extends TileEntityRenderer<TradingPostTileEntity> {

    public TradingPostTileEntityRenderer(TileEntityRendererDispatcher tileEntityRendererDispatcher) {

        super(tileEntityRendererDispatcher);
    }

    @Override
    public void render(TradingPostTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {

        float ageInTicks = tileEntityIn.time + partialTicks;
        float nextRotation = tileEntityIn.rot - tileEntityIn.oRot;
        while (nextRotation >= Math.PI) {

            nextRotation -= (Math.PI * 2F);
        }
        while (nextRotation < -Math.PI) {

            nextRotation += (Math.PI * 2F);
        }

        float bookRotation = tileEntityIn.oRot + nextRotation * partialTicks;
        float bookOpen = MathHelper.lerp(partialTicks, tileEntityIn.oOpen, tileEntityIn.open);
        this.renderItem(new ItemStack(Items.EMERALD), ageInTicks, bookOpen, bookRotation, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
    }

    private void renderItem(ItemStack stack, float ageInTicks, float bookOpen, float bookRotation, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {

        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5F, 1.03125F, 0.5F);
        matrixStackIn.scale(0.8F, 0.8F, 0.8F);

        bookRotation *= -180.0F / (float) Math.PI;
        bookRotation -= 90.0F;
        bookRotation *= bookOpen;

        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(bookRotation));
        matrixStackIn.translate(0.0F, bookOpen, Math.sin(bookOpen * Math.PI));
        matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(-90.0F * (bookOpen - 1.0F)));

        float yFloatiness = (float) Math.sin(ageInTicks * 0.06F) * bookOpen * 0.2F;
        matrixStackIn.translate(0.0F, yFloatiness, 0.0F);

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        itemRenderer.renderStatic(stack, ItemCameraTransforms.TransformType.FIXED, combinedLightIn, combinedOverlayIn, matrixStackIn, bufferIn);
        matrixStackIn.popPose();
    }

}
