package fuzs.tradingpost.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import fuzs.tradingpost.world.level.block.entity.TradingPostBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * mostly copied from Quark's matrix enchanting table by Vazkii
 * https://github.com/Vazkii/Quark/blob/master/src/main/java/vazkii/quark/addons/oddities/client/render/MatrixEnchantingTableTileEntityRenderer.java
 */
public class TradingPostRenderer implements BlockEntityRenderer<TradingPostBlockEntity> {

    public TradingPostRenderer(BlockEntityRendererProvider.Context pContext) {

    }

    @Override
    public void render(TradingPostBlockEntity tileEntityIn, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        float ageInTicks = tileEntityIn.time + partialTicks;
        float nextRotation = tileEntityIn.rot - tileEntityIn.oRot;
        while (nextRotation >= Math.PI) {
            nextRotation -= (Math.PI * 2F);
        }
        while (nextRotation < -Math.PI) {
            nextRotation += (Math.PI * 2F);
        }
        float bookRotation = tileEntityIn.oRot + nextRotation * partialTicks;
        float bookOpen = Mth.lerp(partialTicks, tileEntityIn.oOpen, tileEntityIn.open);
        this.renderItem(new ItemStack(Items.EMERALD), ageInTicks, bookOpen, bookRotation, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
    }

    private void renderItem(ItemStack stack, float ageInTicks, float bookOpen, float bookRotation, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5F, 1.03125F, 0.5F);
        matrixStackIn.scale(0.8F, 0.8F, 0.8F);
        bookRotation *= -180.0F / (float) Math.PI;
        bookRotation -= 90.0F;
        bookRotation *= bookOpen;
        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(bookRotation));
        matrixStackIn.translate(0.0F, bookOpen, Math.sin(bookOpen * Math.PI));
        matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(-90.0F * (bookOpen - 1.0F)));
        float hoveringHeight = (float) Math.sin(ageInTicks * 0.06F) * bookOpen * 0.2F;
        matrixStackIn.translate(0.0F, hoveringHeight, 0.0F);
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        itemRenderer.renderStatic(stack, ItemTransforms.TransformType.FIXED, combinedLightIn, combinedOverlayIn, matrixStackIn, bufferIn, 0);
        matrixStackIn.popPose();
    }

}
