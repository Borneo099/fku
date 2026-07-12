package moze_intel.projecte.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class EntitySpriteRenderer extends EntityRenderer {
   private final ResourceLocation texture;

   public EntitySpriteRenderer(EntityRendererProvider.Context context, ResourceLocation texture) {
      super(context);
      this.texture = texture;
   }

   public @NotNull ResourceLocation m_5478_(@NotNull Entity entity) {
      return this.texture;
   }

   public void m_7392_(@NotNull Entity entity, float entityYaw, float partialTick, @NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int light) {
      matrix.m_85836_();
      matrix.m_252781_(this.f_114476_.m_253208_());
      matrix.m_85841_(0.5F, 0.5F, 0.5F);
      VertexConsumer builder = renderer.m_6299_((RenderType)PERenderType.SPRITE_RENDERER.apply(this.m_5478_(entity)));
      Matrix4f matrix4f = matrix.m_85850_().m_252922_();
      builder.m_252986_(matrix4f, -1.0F, -1.0F, 0.0F).m_7421_(1.0F, 1.0F).m_5752_();
      builder.m_252986_(matrix4f, -1.0F, 1.0F, 0.0F).m_7421_(1.0F, 0.0F).m_5752_();
      builder.m_252986_(matrix4f, 1.0F, 1.0F, 0.0F).m_7421_(0.0F, 0.0F).m_5752_();
      builder.m_252986_(matrix4f, 1.0F, -1.0F, 0.0F).m_7421_(0.0F, 1.0F).m_5752_();
      matrix.m_85849_();
   }
}
