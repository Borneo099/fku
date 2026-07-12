package lexis.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import lexis.Hack.Utils.HeadOnlyLookUtils.HeadOnlyLook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({PlayerRenderer.class})
public abstract class PlayerRendererHeadLookMixin {
   @Unique
   private static float lexis$renderedBody = 0.0F;
   @Unique
   private static boolean lexis$bodyInit = false;
   @Unique
   private static long lexis$lastTimeMs = 0L;
   @Unique
   private static final float BODY_FOLLOW_SPEED = 4.0F;
   @Unique
   private static float lexis$backupBodyRot;
   @Unique
   private static float lexis$backupBodyRotO;
   @Unique
   private static float lexis$backupHeadRot;
   @Unique
   private static float lexis$backupHeadRotO;
   @Unique
   private static float lexis$backupXRot;
   @Unique
   private static float lexis$backupXRotO;
   @Unique
   private static boolean lexis$modified = false;

   @Inject(
      method = {"render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"},
      at = {@At("HEAD")}
   )
   private void lexis$beforeRender(AbstractClientPlayer player, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
      lexis$modified = false;
      if (player == Minecraft.m_91087_().f_91074_) {
         if (!HeadOnlyLook.isLooking()) {
            lexis$bodyInit = false;
         } else {
            float targetYaw = HeadOnlyLook.getServerYaw();
            float targetPitch = Mth.m_14036_(HeadOnlyLook.getServerPitch(), -90.0F, 90.0F);
            if (!lexis$bodyInit) {
               lexis$renderedBody = player.f_20883_;
               lexis$bodyInit = true;
               lexis$lastTimeMs = System.currentTimeMillis();
            }

            lexis$backupBodyRot = player.f_20883_;
            lexis$backupBodyRotO = player.f_20884_;
            lexis$backupHeadRot = player.f_20885_;
            lexis$backupHeadRotO = player.f_20886_;
            lexis$backupXRot = player.m_146909_();
            lexis$backupXRotO = player.f_19860_;
            player.f_20883_ = targetYaw;
            player.f_20884_ = targetYaw;
            player.f_20885_ = targetYaw;
            player.f_20886_ = targetYaw;
            player.m_146926_(targetPitch);
            player.f_19860_ = targetPitch;
            lexis$modified = true;
         }
      }
   }

   @Inject(
      method = {"render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"},
      at = {@At("RETURN")}
   )
   private void lexis$afterRender(AbstractClientPlayer player, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
      if (lexis$modified) {
         player.f_20883_ = lexis$backupBodyRot;
         player.f_20884_ = lexis$backupBodyRotO;
         player.f_20885_ = lexis$backupHeadRot;
         player.f_20886_ = lexis$backupHeadRotO;
         player.m_146926_(lexis$backupXRot);
         player.f_19860_ = lexis$backupXRotO;
         lexis$modified = false;
      }
   }
}
