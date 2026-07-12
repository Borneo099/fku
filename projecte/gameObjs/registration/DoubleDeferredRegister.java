package moze_intel.projecte.gameObjs.registration;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class DoubleDeferredRegister {
   protected final @NotNull DeferredRegister primaryRegister;
   protected final @NotNull DeferredRegister secondaryRegister;

   public DoubleDeferredRegister(IForgeRegistry primaryRegistry, IForgeRegistry secondaryRegistry, String modid) {
      this.primaryRegister = DeferredRegister.create(primaryRegistry, modid);
      this.secondaryRegister = DeferredRegister.create(secondaryRegistry, modid);
   }

   public DoubleWrappedRegistryObject register(String name, Supplier primarySupplier, Supplier secondarySupplier, BiFunction objectWrapper) {
      return (DoubleWrappedRegistryObject)objectWrapper.apply(this.primaryRegister.register(name, primarySupplier), this.secondaryRegister.register(name, secondarySupplier));
   }

   public DoubleWrappedRegistryObject register(String name, Supplier primarySupplier, Function secondarySupplier, BiFunction objectWrapper) {
      RegistryObject primaryObject = this.primaryRegister.register(name, primarySupplier);
      return (DoubleWrappedRegistryObject)objectWrapper.apply(primaryObject, this.secondaryRegister.register(name, () -> {
         return secondarySupplier.apply(primaryObject.get());
      }));
   }

   public void register(IEventBus bus) {
      this.primaryRegister.register(bus);
      this.secondaryRegister.register(bus);
   }
}
