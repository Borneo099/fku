package moze_intel.projecte.gameObjs.registration.impl;

import moze_intel.projecte.gameObjs.registration.WrappedRegistryObject;
import moze_intel.projecte.utils.text.ILangEntry;
import net.minecraft.Util;
import net.minecraftforge.registries.RegistryObject;

public class SoundEventRegistryObject extends WrappedRegistryObject implements ILangEntry {
   private final String translationKey;

   public SoundEventRegistryObject(RegistryObject registryObject) {
      super(registryObject);
      this.translationKey = Util.m_137492_("sound_event", this.registryObject.getId());
   }

   public String getTranslationKey() {
      return this.translationKey;
   }
}
