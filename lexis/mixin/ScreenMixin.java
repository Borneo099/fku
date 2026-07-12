package lexis.mixin;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.NoBackgroundHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Screen.class})
public class ScreenMixin {
   @Inject(
      method = {"renderBackground"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderBackground(GuiGraphics p_283688_, CallbackInfo ci) {
      NoBackgroundHack noBg = this.getNoBackgroundHack();
      if (noBg != null && noBg.shouldCancelBackground((Screen)this)) {
         ci.cancel();
      }

   }

   private NoBackgroundHack getNoBackgroundHack() {
      Iterator var1 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var1.hasNext()) {
            return null;
         }

         hack = (Hack)var1.next();
      } while(!(hack instanceof NoBackgroundHack));

      return (NoBackgroundHack)hack;
   }
}
