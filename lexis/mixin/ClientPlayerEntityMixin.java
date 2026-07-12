package lexis.mixin;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Movement.NoSlowdownHack;
import lexis.Hack.Hackutil.HackManager;
import lexis.Hack.events.StopUsingItemEvent;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LocalPlayer.class})
public class ClientPlayerEntityMixin {
   @Unique
   private boolean hideNextItemUse = false;

   @Inject(
      method = {"stopUsingItem"},
      at = {@At("HEAD")}
   )
   private void onStopUsingItem(CallbackInfo ci) {
      StopUsingItemEvent.fire();
   }

   @Inject(
      at = {@At("HEAD")},
      method = {"startUsingItem"}
   )
   private void onStartUsingItem(CallbackInfo ci) {
   }

   @Inject(
      method = {"aiStep"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z",
   ordinal = 0
)}
   )
   private void onAiStepItemUse(CallbackInfo ci) {
      Iterator var2 = HackManager.getInstance().getHacks().iterator();

      while(var2.hasNext()) {
         Hack hack = (Hack)var2.next();
         if (hack instanceof NoSlowdownHack && hack.isEnabled()) {
            this.hideNextItemUse = true;
            break;
         }
      }

   }

   @Inject(
      method = {"isUsingItem"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onIsUsingItem(CallbackInfoReturnable cir) {
      if (this.hideNextItemUse) {
         cir.setReturnValue(false);
         this.hideNextItemUse = false;
      }

   }

   @Inject(
      method = {"aiStep"},
      at = {@At(
   value = "FIELD",
   target = "Lnet/minecraft/client/player/LocalPlayer;autoJumpTime:I",
   ordinal = 0
)}
   )
   private void afterAiStep(CallbackInfo ci) {
      this.hideNextItemUse = false;
   }
}
