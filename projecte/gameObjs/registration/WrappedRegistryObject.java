package moze_intel.projecte.gameObjs.registration;

import java.util.function.Supplier;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class WrappedRegistryObject implements Supplier, INamedEntry {
   protected @NotNull RegistryObject registryObject;

   protected WrappedRegistryObject(@NotNull RegistryObject registryObject) {
      this.registryObject = registryObject;
   }

   public @NotNull Object get() {
      return this.registryObject.get();
   }

   public String getInternalRegistryName() {
      return this.registryObject.getId().m_135815_();
   }
}
