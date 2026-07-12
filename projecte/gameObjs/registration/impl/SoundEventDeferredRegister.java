package moze_intel.projecte.gameObjs.registration.impl;

import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.registration.WrappedDeferredRegister;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class SoundEventDeferredRegister extends WrappedDeferredRegister {
   public SoundEventDeferredRegister(String modid) {
      super(ForgeRegistries.SOUND_EVENTS, modid);
   }

   public SoundEventRegistryObject register(String name) {
      return (SoundEventRegistryObject)this.register(name, () -> {
         return SoundEvent.m_262824_(PECore.rl(name));
      }, SoundEventRegistryObject::new);
   }
}
