package moze_intel.projecte.gameObjs.registration.impl;

import moze_intel.projecte.gameObjs.registration.WrappedDeferredRegister;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

public class EntityTypeDeferredRegister extends WrappedDeferredRegister {
   public EntityTypeDeferredRegister(String modid) {
      super(ForgeRegistries.ENTITY_TYPES, modid);
   }

   public EntityTypeRegistryObject register(String name, EntityType.Builder builder) {
      return (EntityTypeRegistryObject)this.register(name, () -> {
         return builder.m_20712_(name);
      }, EntityTypeRegistryObject::new);
   }
}
