package moze_intel.projecte.gameObjs.registration.impl;

import java.util.function.Supplier;
import moze_intel.projecte.gameObjs.registration.WrappedDeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class IRecipeSerializerDeferredRegister extends WrappedDeferredRegister {
   public IRecipeSerializerDeferredRegister(String modid) {
      super(ForgeRegistries.RECIPE_SERIALIZERS, modid);
   }

   public IRecipeSerializerRegistryObject register(String name, Supplier sup) {
      return (IRecipeSerializerRegistryObject)this.register(name, sup, IRecipeSerializerRegistryObject::new);
   }
}
