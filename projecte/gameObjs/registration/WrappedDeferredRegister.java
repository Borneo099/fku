package moze_intel.projecte.gameObjs.registration;

import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

public class WrappedDeferredRegister {
   protected final @NotNull DeferredRegister internal;

   protected WrappedDeferredRegister(@NotNull DeferredRegister internal) {
      this.internal = internal;
   }

   protected WrappedDeferredRegister(IForgeRegistry registry, String modid) {
      this(DeferredRegister.create(registry, modid));
   }

   protected WrappedDeferredRegister(ResourceKey registryName, String modid) {
      this(DeferredRegister.create(registryName, modid));
   }

   protected WrappedRegistryObject register(String name, Supplier sup, Function objectWrapper) {
      return (WrappedRegistryObject)objectWrapper.apply(this.internal.register(name, sup));
   }

   public void register(IEventBus bus) {
      this.internal.register(bus);
   }
}
