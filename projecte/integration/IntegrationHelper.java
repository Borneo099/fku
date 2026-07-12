package moze_intel.projecte.integration;

import java.util.function.Supplier;
import moze_intel.projecte.integration.curios.CurioItemCapability;
import moze_intel.projecte.integration.top.TOPIntegration;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;

public class IntegrationHelper {
   public static final String CURIO_MODID = "curios";
   public static final String TOP_MODID = "theoneprobe";
   public static final Supplier CURIO_CAP_SUPPLIER = () -> {
      return CurioItemCapability::new;
   };

   public static void sendIMCMessages(InterModEnqueueEvent event) {
      ModList modList = ModList.get();
      if (modList.isLoaded("theoneprobe")) {
         TOPIntegration.sendIMC(event);
      }

   }
}
