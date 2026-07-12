package moze_intel.projecte.gameObjs.registration.impl;

import moze_intel.projecte.gameObjs.registration.WrappedRegistryObject;
import moze_intel.projecte.utils.text.IHasTranslationKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.RegistryObject;

public class EntityTypeRegistryObject extends WrappedRegistryObject implements IHasTranslationKey {
   public EntityTypeRegistryObject(RegistryObject registryObject) {
      super(registryObject);
   }

   public String getTranslationKey() {
      return ((EntityType)this.get()).m_20675_();
   }
}
