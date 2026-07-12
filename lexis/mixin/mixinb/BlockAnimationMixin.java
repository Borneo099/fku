package lexis.mixin.mixinb;

import com.mojang.blaze3d.vertex.PoseStack;
import lexis.Hack.Hacks.Render.BlockAnimationHack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemInHandRenderer.class})
public class BlockAnimationMixin {
   @Inject(
      method = {"renderArmWithItem"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderArmWithItem(AbstractClientPlayer player, float p_109373_, float p_109374_, InteractionHand hand, float p_109376_, ItemStack itemStack, float p_109378_, PoseStack poseStack, MultiBufferSource bufferSource, int p_109381_, CallbackInfo ci) {
      BlockAnimationHack hack = BlockAnimationHack.INSTANCE;
      if (hack != null && hack.isEnabled()) {
         if (!player.m_150108_()) {
            if (itemStack.m_41720_() instanceof SwordItem) {
               boolean useDown = Minecraft.m_91087_().f_91066_.f_92095_.m_90857_();
               if (useDown) {
                  boolean flag = hand == InteractionHand.MAIN_HAND;
                  HumanoidArm humanoidarm = flag ? player.m_5737_() : player.m_5737_().m_20828_();
                  boolean rightArm = humanoidarm == HumanoidArm.RIGHT;
                  poseStack.m_85836_();
                  switch (hack.getMode()) {
                     case "希格玛":
                        BlockAnimationHack.animationSigma(poseStack, p_109378_, p_109376_);
                        break;
                     case "转圈":
                        BlockAnimationHack.animationSpin(poseStack, p_109378_, p_109376_);
                        break;
                     default:
                        BlockAnimationHack.animation1_7(poseStack, p_109378_, p_109376_);
                  }

                  ItemInHandRenderer self = (ItemInHandRenderer)this;
                  self.m_269530_(player, itemStack, rightArm ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, !rightArm, poseStack, bufferSource, p_109381_);
                  poseStack.m_85849_();
                  ci.cancel();
               }
            }
         }
      }
   }
}
