package lexis.mixin.mixiny;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Window.class})
public class WindowTitleMixin {
   @Shadow
   private long f_85349_;
   private static final String BASE_TITLE = "Minecraft 1.20.1 *Forge | You username: ";

   @Inject(
      method = {"setTitle"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onSetTitle(String title, CallbackInfo ci) {
      Minecraft mc = Minecraft.m_91087_();
      String name = mc.m_91094_() != null ? mc.m_91094_().m_92546_() : "???";
      String customTitle = "Minecraft 1.20.1 *Forge | You username: " + name;
      if (!customTitle.equals(title)) {
         GLFW.glfwSetWindowTitle(this.f_85349_, customTitle);
         ci.cancel();
      }

   }
}
