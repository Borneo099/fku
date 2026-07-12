package moze_intel.projecte.gameObjs;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum EnumFuelType implements StringRepresentable {
   ALCHEMICAL_COAL("alchemical_coal", 6400),
   MOBIUS_FUEL("mobius_fuel", ALCHEMICAL_COAL.getBurnTime() * 4),
   AETERNALIS_FUEL("aeternalis_fuel", MOBIUS_FUEL.getBurnTime() * 4);

   private final String name;
   private final int burnTime;

   private EnumFuelType(String name, int burnTime) {
      this.name = name;
      this.burnTime = burnTime;
   }

   public @NotNull String m_7912_() {
      return this.name;
   }

   public int getBurnTime() {
      return this.burnTime;
   }

   public String toString() {
      return this.m_7912_();
   }

   // $FF: synthetic method
   private static EnumFuelType[] $values() {
      return new EnumFuelType[]{ALCHEMICAL_COAL, MOBIUS_FUEL, AETERNALIS_FUEL};
   }
}
