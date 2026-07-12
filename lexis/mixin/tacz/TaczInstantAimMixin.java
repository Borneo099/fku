package lexis.mixin.tacz;

import java.lang.reflect.Field;
import lexis.Hack.Hacks.TaCZ.InstantAimHack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   targets = {"com.tacz.guns.client.gameplay.LocalPlayerAim"},
   remap = false
)
public class TaczInstantAimMixin {
   private static Field dataField;
   private static Field clientIsAimingField;
   private static Field clientAimingProgressField;
   private static Field oldAimingProgressField;

   @Inject(
      method = {"tickAimingProgress"},
      at = {@At("TAIL")},
      remap = false
   )
   private void onTickAimingProgress(CallbackInfo ci) {
      if (InstantAimHack.instantAimActive) {
         try {
            Object data = dataField.get(this);
            if (data != null && (Boolean)clientIsAimingField.get(data)) {
               clientAimingProgressField.setFloat(data, 1.0F);
               oldAimingProgressField.setFloat(data, 1.0F);
            }
         } catch (Exception var3) {
         }

      }
   }

   static {
      try {
         Class aimClass = Class.forName("com.tacz.guns.client.gameplay.LocalPlayerAim");
         dataField = aimClass.getDeclaredField("data");
         dataField.setAccessible(true);
         Class dataHolderClass = Class.forName("com.tacz.guns.client.gameplay.LocalPlayerDataHolder");
         clientIsAimingField = dataHolderClass.getDeclaredField("clientIsAiming");
         clientIsAimingField.setAccessible(true);
         clientAimingProgressField = dataHolderClass.getDeclaredField("clientAimingProgress");
         clientAimingProgressField.setAccessible(true);
         oldAimingProgressField = dataHolderClass.getDeclaredField("oldAimingProgress");
         oldAimingProgressField.setAccessible(true);
      } catch (Exception var2) {
      }

   }
}
