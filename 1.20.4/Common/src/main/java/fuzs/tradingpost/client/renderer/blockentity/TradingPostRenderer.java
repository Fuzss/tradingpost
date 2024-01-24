package fuzs.tradingpost.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fuzs.tradingpost.world.level.block.entity.TradingPostBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Mostly copied from Quark's <a href="https://github.com/VazkiiMods/Quark/blob/master/src/main/java/vazkii/quark/addons/oddities/client/render/be/MatrixEnchantingTableRenderer.java">MatrixEnchantingTableRenderer.java</a> by Vazkii, thanks!
 */
public class TradingPostRenderer implements BlockEntityRenderer<TradingPostBlockEntity> {
    private final ItemRenderer itemRenderer;

    public TradingPostRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(TradingPostBlockEntity blockEntity, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        float ageInTicks = blockEntity.time + partialTicks;
        float nextRotation = blockEntity.rot - blockEntity.oRot;
        while (nextRotation >= Math.PI) {
            nextRotation -= (Math.PI * 2F);
        }
        while (nextRotation < -Math.PI) {
            nextRotation += (Math.PI * 2F);
        }
        float bookRotation = blockEntity.oRot + nextRotation * partialTicks;
        float bookOpen = Mth.lerp(partialTicks, blockEntity.oOpen, blockEntity.open);
        this.renderItem(new ItemStack(Items.EMERALD), ageInTicks, bookOpen, bookRotation, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, blockEntity.getLevel());
    }

    private void renderItem(ItemStack stack, float ageInTicks, float bookOpen, float bookRotation, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, Level level) {
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5F, 1.03125F, 0.5F);
        matrixStackIn.scale(0.8F, 0.8F, 0.8F);
        bookRotation *= -180.0F / (float) Math.PI;
        bookRotation -= 90.0F;
        bookRotation *= bookOpen;
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(bookRotation));
        matrixStackIn.translate(0.0F, bookOpen, Math.sin(bookOpen * Math.PI));
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(-90.0F * (bookOpen - 1.0F)));
        float hoveringHeight = (float) Math.sin(ageInTicks * 0.06F) * bookOpen * 0.2F;
        matrixStackIn.translate(0.0F, hoveringHeight, 0.0F);
        this.itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, combinedLightIn, combinedOverlayIn, matrixStackIn, bufferIn, level, 0);
        matrixStackIn.popPose();
    }
}
