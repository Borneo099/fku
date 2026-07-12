package lexis.mixin.mixinb;

import lexis.Gui.GuiAnimator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Minecraft.class})
public class GuiOpenMixin {
   @Inject(
      method = {"setScreen"},
      at = {@At("HEAD")}
   )
   private void onSetScreen(Screen screen, CallbackInfo ci) {
      if (screen instanceof AbstractContainerScreen) {
         GuiAnimator.onGuiOpen();
      }

   }
}
