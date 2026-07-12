package moze_intel.projecte.config;

import moze_intel.projecte.config.value.CachedBooleanValue;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.Type;

public class CommonConfig extends BasePEConfig {
   private final ForgeConfigSpec configSpec;
   public final CachedBooleanValue debugLogging;
   public final CachedBooleanValue craftableTome;
   public final CachedBooleanValue fullKleinStars;

   CommonConfig() {
      ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
      builder.comment("Note: The majority of config options are in the server config file. If you do not see the server config file, try opening up a single player world. ProjectE uses one \"server\" config file for all worlds, for convenience in going from one world to another, but makes it be a \"server\" config file so that forge will automatically sync it when we connect to a multiplayer server.").push("common");
      this.debugLogging = CachedBooleanValue.wrap(this, builder.comment("Enable more verbose debug logging").define("debugLogging", false));
      this.craftableTome = CachedBooleanValue.wrap(this, builder.comment("The Tome of Knowledge can be crafted.").define("craftableTome", false));
      this.fullKleinStars = CachedBooleanValue.wrap(this, builder.comment("Require full omega klein stars in the tome of knowledge and gem armor recipes. This is the same behavior that EE2 had.").define("fullKleinStars", false));
      builder.pop();
      this.configSpec = builder.build();
   }

   public String getFileName() {
      return "common";
   }

   public ForgeConfigSpec getConfigSpec() {
      return this.configSpec;
   }

   public ModConfig.Type getConfigType() {
      return Type.COMMON;
   }
}
