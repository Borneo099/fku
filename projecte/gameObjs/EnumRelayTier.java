package moze_intel.projecte.gameObjs;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public enum EnumRelayTier implements StringRepresentable {
   MK1("relay_mk1", 64L, 100000L),
   MK2("relay_mk2", 192L, 1000000L),
   MK3("relay_mk3", 640L, 10000000L);

   private final String name;
   private final long chargeRate;
   private final long storage;

   private EnumRelayTier(@Range(
   from = 1L,
   to = Long.MAX_VALUE
) String name, long chargeRate, long storage) {
      this.name = name;
      this.chargeRate = chargeRate;
      this.storage = storage;
   }

   public @NotNull String m_7912_() {
      return this.name;
   }

   public @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long getChargeRate() {
      return this.chargeRate;
   }

   public @Range(
   from = 1L,
   to = Long.MAX_VALUE
) long getStorage() {
      return this.storage;
   }

   public String toString() {
      return this.m_7912_();
   }

   // $FF: synthetic method
   private static EnumRelayTier[] $values() {
      return new EnumRelayTier[]{MK1, MK2, MK3};
   }
}
