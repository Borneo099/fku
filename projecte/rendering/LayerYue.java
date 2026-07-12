package moze_intel.projecte.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.UUID;
import moze_intel.projecte.PECore;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class LayerYue extends RenderLayer {
   private static final UUID SIN_UUID = UUID.fromString("5f86012c-ca4b-451a-989c-8fab167af647");
   private static final UUID CLAR_UUID = UUID.fromString("e5c59746-9cf7-4940-a849-d09e1f1efc13");
   private static final ResourceLocation HEART_LOC = PECore.rl("textures/models/heartcircle.png");
   private static final ResourceLocation YUE_LOC = PECore.rl("textures/models/yuecircle.png");
   private final PlayerRenderer render;

   public LayerYue(PlayerRenderer renderer) {
      super(renderer);
      this.render = renderer;
   }

   public void render(@NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int light, @NotNull AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
      if (!player.m_20145_()) {
         if (!FMLEnvironment.production || SIN_UUID.equals(player.m_20148_()) || CLAR_UUID.equals(player.m_20148_())) {
            matrix.m_85836_();
            ((PlayerModel)this.render.m_7200_()).f_103378_.m_104299_(matrix);
            double yShift = -0.498;
            if (player.m_6047_()) {
               matrix.m_252781_(Axis.f_252529_.m_252977_(-28.64789F));
               yShift = -0.44;
            }

            matrix.m_252781_(Axis.f_252403_.m_252977_(180.0F));
            matrix.m_85841_(3.0F, 3.0F, 3.0F);
            matrix.m_85837_(-0.5, yShift, -0.5);
            VertexConsumer builder = renderer.m_6299_((RenderType)PERenderType.YEU_RENDERER.apply(CLAR_UUID.equals(player.m_20148_()) ? HEART_LOC : YUE_LOC));
            Matrix4f matrix4f = matrix.m_85850_().m_252922_();
            builder.m_252986_(matrix4f, 0.0F, 0.0F, 0.0F).m_6122_(0, 255, 0, 255).m_7421_(0.0F, 0.0F).m_5752_();
            builder.m_252986_(matrix4f, 0.0F, 0.0F, 1.0F).m_6122_(0, 255, 0, 255).m_7421_(0.0F, 1.0F).m_5752_();
            builder.m_252986_(matrix4f, 1.0F, 0.0F, 1.0F).m_6122_(0, 255, 0, 255).m_7421_(1.0F, 1.0F).m_5752_();
            builder.m_252986_(matrix4f, 1.0F, 0.0F, 0.0F).m_6122_(0, 255, 0, 255).m_7421_(1.0F, 0.0F).m_5752_();
            matrix.m_85849_();
         }

      }
   }
}
