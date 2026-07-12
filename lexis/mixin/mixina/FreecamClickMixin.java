package lexis.mixin.mixina;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.FreeCamHack;
import lexis.Hack.Hackutil.HackManager;
import lexis.Hack.Utils.BaritoneBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({MouseHandler.class})
public class FreecamClickMixin {
   @Unique
   private static long lastClickTime = 0L;
   @Unique
   private static BlockPos lastClickPos = null;

   @Inject(
      method = {"onPress"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void freecamBaritoneClick(long window, int button, int action, int mods, CallbackInfo ci) {
      if (button == 0) {
         if (action == 1) {
            if (BaritoneBridge.isAvailable()) {
               FreeCamHack freeCam = null;
               Iterator var8 = HackManager.getInstance().getHacks().iterator();

               while(var8.hasNext()) {
                  Hack hack = (Hack)var8.next();
                  if (hack instanceof FreeCamHack) {
                     FreeCamHack fc = (FreeCamHack)hack;
                     if (fc.isActive() && fc.isBaritoneGoto()) {
                        freeCam = fc;
                        break;
                     }
                  }
               }

               if (freeCam != null) {
                  Minecraft mc = Minecraft.m_91087_();
                  if (mc.f_91074_ != null && mc.f_91073_ != null) {
                     if (mc.f_91080_ == null) {
                        HitResult hit = mc.f_91077_;
                        if (hit instanceof BlockHitResult) {
                           BlockHitResult bhr = (BlockHitResult)hit;
                           if (hit.m_6662_() == Type.BLOCK) {
                              BlockPos pos = bhr.m_82425_();
                              String mode = freeCam.getClickMode();
                              if ("双击".equals(mode)) {
                                 long now = System.currentTimeMillis();
                                 if (lastClickPos == null || !lastClickPos.equals(pos) || now - lastClickTime >= 400L) {
                                    lastClickTime = now;
                                    lastClickPos = pos;
                                    ci.cancel();
                                    return;
                                 }

                                 lastClickTime = 0L;
                                 lastClickPos = null;
                              }

                              BaritoneBridge.gotoCoordSilent(pos.m_123341_(), pos.m_123342_() + 1, pos.m_123343_());
                              ci.cancel();
                              return;
                           }
                        }

                     }
                  }
               }
            }
         }
      }
   }
}
