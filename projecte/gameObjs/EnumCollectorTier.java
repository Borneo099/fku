package moze_intel.projecte.gameObjs;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public enum EnumCollectorTier implements StringRepresentable {
   MK1("collector_mk1", 4L, 10000L),
   MK2("collector_mk2", 12L, 30000L),
   MK3("collector_mk3", 40L, 60000L);

   private final String name;
   private final long genRate;
   private final long storage;

   private EnumCollectorTier(@Range(
   from = 1L,
   to = Long.MAX_VALUE
) String name, long genRate, long storage) {
      this.name = name;
      this.genRate = genRate;
      this.storage = storage;
   }

   public @NotNull String m_7912_() {
      return this.name;
   }

   public @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long getGenRate() {
      return this.genRate;
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
   private static EnumCollectorTier[] $values() {
      return new EnumCollectorTier[]{MK1, MK2, MK3};
   }
}
