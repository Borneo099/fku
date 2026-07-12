package lexis.mixin.mixinb;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.OldWeaponHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemInHandRenderer.class})
public class HeldItemRendererMixin {
   @Inject(
      method = {"renderArmWithItem"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderArmWithItem(AbstractClientPlayer p_109372_, float p_109373_, float p_109374_, InteractionHand p_109375_, float p_109376_, ItemStack p_109377_, float p_109378_, PoseStack p_109379_, MultiBufferSource p_109380_, int p_109381_, CallbackInfo ci) {
      Iterator var12 = HackManager.getInstance().getHacks().iterator();

      while(var12.hasNext()) {
         Hack hack = (Hack)var12.next();
         if (hack instanceof OldWeaponHack && hack.isEnabled()) {
            p_109379_.m_85836_();
            p_109379_.m_85837_(0.5, 0.2, -0.5);
            break;
         }
      }

   }

   @Inject(
      method = {"renderArmWithItem"},
      at = {@At("RETURN")}
   )
   private void onRenderArmWithItemReturn(AbstractClientPlayer p_109372_, float p_109373_, float p_109374_, InteractionHand p_109375_, float p_109376_, ItemStack p_109377_, float p_109378_, PoseStack p_109379_, MultiBufferSource p_109380_, int p_109381_, CallbackInfo ci) {
      Iterator var12 = HackManager.getInstance().getHacks().iterator();

      while(var12.hasNext()) {
         Hack hack = (Hack)var12.next();
         if (hack instanceof OldWeaponHack && hack.isEnabled()) {
            p_109379_.m_85849_();
            break;
         }
      }

   }
}
