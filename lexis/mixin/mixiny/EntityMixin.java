package lexis.mixin.mixiny;

import java.util.List;
import lexis.Hack.Utils.pathfinding.Faker;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({DebugScreenOverlay.class})
public abstract class EntityMixin {
   @Inject(
      method = {"getSystemInformation"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void lexis$fakeCpu(CallbackInfoReturnable cir) {
      if (Faker.isEnabled()) {
         List list = (List)cir.getReturnValue();

         for(int i = 0; i < list.size(); ++i) {
            String line = (String)list.get(i);
            if (line != null && (line.contains("CPU:") || line.contains("Intel") || line.contains("AMD") || line.contains("Ryzen"))) {
               list.set(i, "CPU: 32x Intel(R) Core(TM) i9-14900K @ 3.20GHz");
            }
         }

         cir.setReturnValue(list);
      }
   }
}
