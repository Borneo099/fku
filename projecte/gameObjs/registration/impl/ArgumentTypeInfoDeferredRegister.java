package moze_intel.projecte.gameObjs.registration.impl;

import java.util.function.Function;
import java.util.function.Supplier;
import moze_intel.projecte.gameObjs.registration.WrappedDeferredRegister;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;

public class ArgumentTypeInfoDeferredRegister extends WrappedDeferredRegister {
   public ArgumentTypeInfoDeferredRegister(String modid) {
      super(Registries.f_256982_, modid);
   }

   public ArgumentTypeInfoRegistryObject registerContextFree(String name, Class argumentClass, Supplier constructor) {
      return this.register(name, argumentClass, () -> {
         return SingletonArgumentInfo.m_235451_(constructor);
      });
   }

   public ArgumentTypeInfoRegistryObject registerContextAware(String name, Class argumentClass, Function constructor) {
      return this.register(name, argumentClass, () -> {
         return SingletonArgumentInfo.m_235449_(constructor);
      });
   }

   public ArgumentTypeInfoRegistryObject register(String name, Class argumentClass, Supplier sup) {
      return (ArgumentTypeInfoRegistryObject)this.register(name, () -> {
         return ArgumentTypeInfos.registerByClass(argumentClass, (ArgumentTypeInfo)sup.get());
      }, ArgumentTypeInfoRegistryObject::new);
   }
}
