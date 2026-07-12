package moze_intel.projecte.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.function.Supplier;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class NovaRenderer extends EntityRenderer {
   private final BlockRenderDispatcher blockRenderer;
   private final Supplier stateSupplier;

   public NovaRenderer(EntityRendererProvider.Context context, Supplier stateSupplier) {
      super(context);
      this.blockRenderer = context.m_234597_();
      this.stateSupplier = stateSupplier;
      this.f_114477_ = 0.5F;
   }

   public void render(@NotNull PrimedTnt entity, float entityYaw, float partialTick, @NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int light) {
      matrix.m_85836_();
      matrix.m_85837_(0.0, 0.5, 0.0);
      int fuse = entity.m_32100_();
      if ((float)fuse - partialTick + 1.0F < 10.0F) {
         float f = 1.0F - ((float)fuse - partialTick + 1.0F) / 10.0F;
         f = Mth.m_14036_(f, 0.0F, 1.0F);
         f *= f;
         f *= f;
         float f1 = 1.0F + f * 0.3F;
         matrix.m_85841_(f1, f1, f1);
      }

      matrix.m_252781_(Axis.f_252436_.m_252977_(-90.0F));
      matrix.m_85837_(-0.5, -0.5, 0.5);
      matrix.m_252781_(Axis.f_252436_.m_252977_(90.0F));
      TntMinecartRenderer.m_234661_(this.blockRenderer, (BlockState)this.stateSupplier.get(), matrix, renderer, light, fuse / 5 % 2 == 0);
      matrix.m_85849_();
      super.m_7392_(entity, entityYaw, partialTick, matrix, renderer, light);
   }

   public @NotNull ResourceLocation getTextureLocation(@NotNull PrimedTnt entity) {
      return TextureAtlas.f_118259_;
   }
}
