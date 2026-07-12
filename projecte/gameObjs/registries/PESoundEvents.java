package moze_intel.projecte.gameObjs.registries;

import moze_intel.projecte.gameObjs.registration.impl.SoundEventDeferredRegister;
import moze_intel.projecte.gameObjs.registration.impl.SoundEventRegistryObject;

public class PESoundEvents {
   public static final SoundEventDeferredRegister SOUND_EVENTS = new SoundEventDeferredRegister("projecte");
   public static final SoundEventRegistryObject WIND_MAGIC;
   public static final SoundEventRegistryObject WATER_MAGIC;
   public static final SoundEventRegistryObject POWER;
   public static final SoundEventRegistryObject HEAL;
   public static final SoundEventRegistryObject DESTRUCT;
   public static final SoundEventRegistryObject CHARGE;
   public static final SoundEventRegistryObject UNCHARGE;
   public static final SoundEventRegistryObject TRANSMUTE;

   static {
      WIND_MAGIC = SOUND_EVENTS.register("windmagic");
      WATER_MAGIC = SOUND_EVENTS.register("watermagic");
      POWER = SOUND_EVENTS.register("power");
      HEAL = SOUND_EVENTS.register("heal");
      DESTRUCT = SOUND_EVENTS.register("destruct");
      CHARGE = SOUND_EVENTS.register("charge");
      UNCHARGE = SOUND_EVENTS.register("uncharge");
      TRANSMUTE = SOUND_EVENTS.register("transmute");
   }
}
