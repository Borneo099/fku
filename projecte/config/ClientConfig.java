package moze_intel.projecte.config;

import moze_intel.projecte.config.value.CachedBooleanValue;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.Type;

public class ClientConfig extends BasePEConfig {
   private final ForgeConfigSpec configSpec;
   public final CachedBooleanValue tagToolTips;
   public final CachedBooleanValue emcToolTips;
   public final CachedBooleanValue shiftEmcToolTips;
   public final CachedBooleanValue shiftLearnedToolTips;
   public final CachedBooleanValue statToolTips;
   public final CachedBooleanValue pedestalToolTips;
   public final CachedBooleanValue pulsatingOverlay;

   ClientConfig() {
      ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
      builder.push("client");
      this.tagToolTips = CachedBooleanValue.wrap(this, builder.comment("Show item tags in tooltips (useful for custom EMC registration)").define("tagToolTips", false));
      this.emcToolTips = CachedBooleanValue.wrap(this, builder.comment("Show the EMC value as a tooltip on items and blocks").define("emcToolTips", true));
      this.shiftEmcToolTips = CachedBooleanValue.wrap(this, builder.comment("Requires holding shift to display the EMC value as a tooltip on items and blocks. Note: this does nothing if emcToolTips is disabled.").define("shiftEmcToolTips", false));
      this.shiftLearnedToolTips = CachedBooleanValue.wrap(this, builder.comment("Requires holding shift to display the learned/unlearned text as a tooltip on items and blocks. Note: this does nothing if emcToolTips is disabled.").define("shiftLearnedToolTips", true));
      this.statToolTips = CachedBooleanValue.wrap(this, builder.comment("Show stats as tooltips for various ProjectE blocks").define("statToolTips", true));
      this.pedestalToolTips = CachedBooleanValue.wrap(this, builder.comment("Show DM pedestal functions in item tooltips").define("pedestalToolTips", true));
      this.pulsatingOverlay = CachedBooleanValue.wrap(this, builder.comment("The Philosopher's Stone overlay softly pulsates").define("pulsatingOverlay", false));
      builder.pop();
      this.configSpec = builder.build();
   }

   public String getFileName() {
      return "client";
   }

   public ForgeConfigSpec getConfigSpec() {
      return this.configSpec;
   }

   public ModConfig.Type getConfigType() {
      return Type.CLIENT;
   }
}
