package lexis.Hack.Utils.Blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import lexis.Hack.Utils.Colors.SettingColor;
import lexis.Hack.Utils.Render.RenderUtils;
import lexis.mixin.accessor.ClientPlayerInteractionManagerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BlockBreakProgressRenderer {
   private static final Minecraft mc = Minecraft.m_91087_();

   public static void renderProgress(PoseStack poseStack, SettingColor color) {
      if (mc.f_91074_ != null) {
         MultiPlayerGameMode gameMode = mc.f_91072_;
         if (gameMode != null) {
            BlockPos breakingPos = ((ClientPlayerInteractionManagerAccessor)gameMode).getDestroyBlockPos();
            float progress = ((ClientPlayerInteractionManagerAccessor)gameMode).getDestroyProgress();
            if (breakingPos != null && !(progress <= 0.0F) && !(progress >= 1.0F)) {
               float alpha = Math.min(1.0F, progress * 1.5F);
               int packedColor = (int)(alpha * 255.0F) << 24 | color.getPacked() & 16777215;
               AABB box = (new AABB(breakingPos)).m_82400_(-0.02);
               Vec3 cameraPos = RenderUtils.getCameraPos();
               poseStack.m_85836_();
               poseStack.m_85837_(-cameraPos.f_82479_, -cameraPos.f_82480_, -cameraPos.f_82481_);
               RenderUtils.drawSolidBoxes(poseStack, List.of(box), packedColor, false);
               poseStack.m_85849_();
            }
         }
      }
   }

   public static float getCurrentProgress() {
      MultiPlayerGameMode gameMode = mc.f_91072_;
      return gameMode == null ? 0.0F : ((ClientPlayerInteractionManagerAccessor)gameMode).getDestroyProgress();
   }

   public static BlockPos getCurrentBreakingBlock() {
      MultiPlayerGameMode gameMode = mc.f_91072_;
      return gameMode == null ? null : ((ClientPlayerInteractionManagerAccessor)gameMode).getDestroyBlockPos();
   }

   public static boolean isBreakingBlock() {
      float prog = getCurrentProgress();
      return prog > 0.0F && prog < 1.0F;
   }
}
