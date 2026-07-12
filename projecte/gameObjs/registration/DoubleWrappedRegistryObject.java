package moze_intel.projecte.gameObjs.registration;

import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class DoubleWrappedRegistryObject implements INamedEntry {
   private final @NotNull RegistryObject primaryRO;
   private final @NotNull RegistryObject secondaryRO;

   public DoubleWrappedRegistryObject(@NotNull RegistryObject primaryRO, @NotNull RegistryObject secondaryRO) {
      this.primaryRO = primaryRO;
      this.secondaryRO = secondaryRO;
   }

   public @NotNull Object getPrimary() {
      return this.primaryRO.get();
   }

   public @NotNull Object getSecondary() {
      return this.secondaryRO.get();
   }

   public String getInternalRegistryName() {
      return this.primaryRO.getId().m_135815_();
   }
}
