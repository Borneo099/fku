package lexis.mixin.mixina;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.PortalGuiHack;
import lexis.Hack.Hackutil.HackManager;
import lexis.Hack.Hackutil.BlocksUtilsMixinJava.BlockUtils;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LocalPlayer.class})
public class PortalGuiMixin {
   @Inject(
      method = {"handleNetherPortalClient"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"
)},
      cancellable = true
   )
   private void onPortalSetScreen(CallbackInfo ci) {
      Iterator var2 = HackManager.getInstance().getHacks().iterator();

      while(var2.hasNext()) {
         Hack hack = (Hack)var2.next();
         if (hack instanceof PortalGuiHack && hack.isEnabled()) {
            ci.cancel();
            break;
         }
      }

   }

   @Inject(
      method = {"closeContainer"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onCloseContainer(CallbackInfo ci) {
      LocalPlayer player = (LocalPlayer)this;
      if (BlockUtils.isPlayerInPortal(player)) {
         Iterator var3 = HackManager.getInstance().getHacks().iterator();

         while(var3.hasNext()) {
            Hack hack = (Hack)var3.next();
            if (hack instanceof PortalGuiHack && hack.isEnabled()) {
               ci.cancel();
               break;
            }
         }

      }
   }

   @Inject(
      method = {"clientSideCloseContainer"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onClientSideCloseContainer(CallbackInfo ci) {
      LocalPlayer player = (LocalPlayer)this;
      if (BlockUtils.isPlayerInPortal(player)) {
         Iterator var3 = HackManager.getInstance().getHacks().iterator();

         while(var3.hasNext()) {
            Hack hack = (Hack)var3.next();
            if (hack instanceof PortalGuiHack && hack.isEnabled()) {
               ci.cancel();
               break;
            }
         }

      }
   }
}
