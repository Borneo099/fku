package lexis.mixin.mixinb;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.FreeCamHack;
import lexis.Hack.Hackutil.HackManager;
import lexis.Hack.Utils.BaritoneBridge;
import lexis.mixin.accessor.LocalPlayerInputAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LocalPlayer.class})
public class LocalPlayerMixin {
   @Shadow
   public Input f_108618_;
   @Unique
   private Input savedInput;

   @Unique
   private FreeCamHack getFreeCam() {
      Iterator var1 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var1.hasNext()) {
            return null;
         }

         hack = (Hack)var1.next();
      } while(!(hack instanceof FreeCamHack) || !hack.isEnabled());

      return (FreeCamHack)hack;
   }

   @Inject(
      method = {"aiStep"},
      at = {@At("HEAD")}
   )
   private void freecamAiStepHead(CallbackInfo ci) {
      FreeCamHack freeCam = this.getFreeCam();
      if (freeCam != null && freeCam.isActive()) {
         if (!freeCam.isKeepBaritone() || !BaritoneBridge.isActive()) {
            LocalPlayerInputAccessor a = (LocalPlayerInputAccessor)this;
            this.savedInput = a.getInput();
            if (freeCam.isControlPlayerMovement()) {
               long window = Minecraft.m_91087_().m_91268_().m_85439_();
               Input arrowInput = new Input();
               arrowInput.f_108568_ = GLFW.glfwGetKey(window, 265) == 1;
               arrowInput.f_108569_ = GLFW.glfwGetKey(window, 264) == 1;
               arrowInput.f_108570_ = GLFW.glfwGetKey(window, 263) == 1;
               arrowInput.f_108571_ = GLFW.glfwGetKey(window, 262) == 1;
               arrowInput.f_108567_ = (float)((arrowInput.f_108568_ ? 1 : 0) - (arrowInput.f_108569_ ? 1 : 0));
               arrowInput.f_108566_ = (float)((arrowInput.f_108570_ ? 1 : 0) - (arrowInput.f_108571_ ? 1 : 0));
               a.setInput(arrowInput);
            } else {
               a.setInput(new Input());
            }

         }
      }
   }

   @Inject(
      method = {"aiStep"},
      at = {@At("RETURN")}
   )
   private void freecamAiStepReturn(CallbackInfo ci) {
      if (this.savedInput != null) {
         ((LocalPlayerInputAccessor)this).setInput(this.savedInput);
         this.savedInput = null;
      }

   }
}
