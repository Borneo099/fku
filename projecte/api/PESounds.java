package moze_intel.projecte.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class PESounds {
   public static RegistryObject WIND = get("windmagic");
   public static RegistryObject WATER = get("watermagic");
   public static RegistryObject POWER = get("power");
   public static RegistryObject HEAL = get("heal");
   public static RegistryObject DESTRUCT = get("destruct");
   public static RegistryObject CHARGE = get("charge");
   public static RegistryObject UNCHARGE = get("uncharge");
   public static RegistryObject TRANSMUTE = get("transmute");

   private PESounds() {
   }

   private static RegistryObject get(String name) {
      return RegistryObject.create(new ResourceLocation("projecte", name), ForgeRegistries.SOUND_EVENTS);
   }
}
