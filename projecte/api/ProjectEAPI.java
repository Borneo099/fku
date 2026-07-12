package moze_intel.projecte.api;

import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.api.proxy.ITransmutationProxy;

public final class ProjectEAPI {
   public static final String PROJECTE_MODID = "projecte";

   private ProjectEAPI() {
   }

   /** @deprecated */
   @Deprecated(
      forRemoval = true,
      since = "MC 1.20.1"
   )
   public static IEMCProxy getEMCProxy() {
      return IEMCProxy.INSTANCE;
   }

   /** @deprecated */
   @Deprecated(
      forRemoval = true,
      since = "MC 1.20.1"
   )
   public static ITransmutationProxy getTransmutationProxy() {
      return ITransmutationProxy.INSTANCE;
   }
}
