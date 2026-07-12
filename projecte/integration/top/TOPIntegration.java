package moze_intel.projecte.integration.top;

import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;

public class TOPIntegration {
   public static void sendIMC(InterModEnqueueEvent event) {
      InterModComms.sendTo("theoneprobe", "getTheOneProbe", PEProbeInfoProvider::new);
   }
}
