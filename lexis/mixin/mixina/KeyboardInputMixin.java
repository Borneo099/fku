package lexis.mixin.mixina;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Movement.GUIMoveHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({KeyboardInput.class})
public class KeyboardInputMixin extends Input {
   private Minecraft mc = Minecraft.m_91087_();

   @Inject(
      method = {"tick"},
      at = {@At("RETURN")}
   )
   private void onTick(boolean slowDown, float f, CallbackInfo ci) {
      Iterator var4 = HackManager.getInstance().getHacks().iterator();

      while(var4.hasNext()) {
         Hack hack = (Hack)var4.next();
         if (hack instanceof GUIMoveHack && hack.isEnabled() && this.mc.f_91080_ != null) {
            long window = this.mc.m_91268_().m_85439_();
            this.f_108568_ = GLFW.glfwGetKey(window, 87) == 1;
            this.f_108569_ = GLFW.glfwGetKey(window, 83) == 1;
            this.f_108570_ = GLFW.glfwGetKey(window, 65) == 1;
            this.f_108571_ = GLFW.glfwGetKey(window, 68) == 1;
            this.f_108572_ = GLFW.glfwGetKey(window, 32) == 1;
            this.f_108573_ = GLFW.glfwGetKey(window, 340) == 1;
            this.f_108567_ = (this.f_108568_ ? 1.0F : 0.0F) - (this.f_108569_ ? 1.0F : 0.0F);
            this.f_108566_ = (this.f_108570_ ? 1.0F : 0.0F) - (this.f_108571_ ? 1.0F : 0.0F);
            break;
         }
      }

   }
}
