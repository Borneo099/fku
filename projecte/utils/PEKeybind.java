package moze_intel.projecte.utils;

import java.util.Locale;
import moze_intel.projecte.PECore;
import moze_intel.projecte.utils.text.IHasTranslationKey;
import net.minecraft.Util;

public enum PEKeybind implements IHasTranslationKey {
   HELMET_TOGGLE,
   BOOTS_TOGGLE,
   CHARGE,
   EXTRA_FUNCTION,
   FIRE_PROJECTILE,
   MODE;

   private final String translationKey;

   private PEKeybind() {
      this.translationKey = Util.m_137492_("key", PECore.rl(this.name().toLowerCase(Locale.ROOT)));
   }

   public String getTranslationKey() {
      return this.translationKey;
   }

   // $FF: synthetic method
   private static PEKeybind[] $values() {
      return new PEKeybind[]{HELMET_TOGGLE, BOOTS_TOGGLE, CHARGE, EXTRA_FUNCTION, FIRE_PROJECTILE, MODE};
   }
}
