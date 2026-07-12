package moze_intel.projecte.gameObjs;

import moze_intel.projecte.PECore;
import moze_intel.projecte.utils.LazyTagLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.ForgeRegistries;

public class PETags {
   private PETags() {
   }

   public static void init() {
      PETags.Items.init();
      PETags.Blocks.init();
      PETags.Entities.init();
      PETags.BlockEntities.init();
   }

   public static class Items {
      public static final TagKey ALCHEMICAL_BAGS = tag("alchemical_bags");
      public static final TagKey COLLECTOR_FUEL = tag("collector_fuel");
      public static final LazyTagLookup COLLECTOR_FUEL_LOOKUP;
      public static final TagKey NBT_WHITELIST;
      public static final TagKey COVALENCE_DUST;
      public static final TagKey CURIOS_BELT;
      public static final TagKey CURIOS_KLEIN_STAR;
      public static final TagKey CURIOS_NECKLACE;
      public static final TagKey CURIOS_RING;
      public static final TagKey TOOLS_HAMMERS;
      public static final TagKey TOOLS_KATARS;
      public static final TagKey TOOLS_MORNING_STARS;
      public static final TagKey ARMORS_HELMETS_DARK_MATTER;
      public static final TagKey ARMORS_CHESTPLATES_DARK_MATTER;
      public static final TagKey ARMORS_LEGGINGS_DARK_MATTER;
      public static final TagKey ARMORS_BOOTS_DARK_MATTER;
      public static final TagKey TOOLS_SWORDS_DARK_MATTER;
      public static final TagKey TOOLS_AXES_DARK_MATTER;
      public static final TagKey TOOLS_PICKAXES_DARK_MATTER;
      public static final TagKey TOOLS_SHOVELS_DARK_MATTER;
      public static final TagKey TOOLS_HOES_DARK_MATTER;
      public static final TagKey TOOLS_HAMMERS_DARK_MATTER;
      public static final TagKey ARMORS_HELMETS_RED_MATTER;
      public static final TagKey ARMORS_CHESTPLATES_RED_MATTER;
      public static final TagKey ARMORS_LEGGINGS_RED_MATTER;
      public static final TagKey ARMORS_BOOTS_RED_MATTER;
      public static final TagKey TOOLS_SWORDS_RED_MATTER;
      public static final TagKey TOOLS_AXES_RED_MATTER;
      public static final TagKey TOOLS_PICKAXES_RED_MATTER;
      public static final TagKey TOOLS_SHOVELS_RED_MATTER;
      public static final TagKey TOOLS_HOES_RED_MATTER;
      public static final TagKey TOOLS_HAMMERS_RED_MATTER;
      public static final TagKey TOOLS_KATARS_RED_MATTER;
      public static final TagKey TOOLS_MORNING_STARS_RED_MATTER;
      public static final LazyTagLookup ORES_LOOKUP;
      public static final LazyTagLookup RAW_ORES_LOOKUP;

      private static void init() {
      }

      private Items() {
      }

      private static TagKey tag(String name) {
         return ItemTags.create(PECore.rl(name));
      }

      private static TagKey curiosTag(String name) {
         return ItemTags.create(new ResourceLocation("curios", name));
      }

      private static TagKey forgeTag(String name) {
         return ItemTags.create(new ResourceLocation("forge", name));
      }

      static {
         COLLECTOR_FUEL_LOOKUP = LazyTagLookup.create(ForgeRegistries.ITEMS, COLLECTOR_FUEL);
         NBT_WHITELIST = tag("nbt_whitelist");
         COVALENCE_DUST = tag("covalence_dust");
         CURIOS_BELT = curiosTag("belt");
         CURIOS_KLEIN_STAR = curiosTag("klein_star");
         CURIOS_NECKLACE = curiosTag("necklace");
         CURIOS_RING = curiosTag("ring");
         TOOLS_HAMMERS = forgeTag("tools/hammers");
         TOOLS_KATARS = forgeTag("tools/katars");
         TOOLS_MORNING_STARS = forgeTag("tools/morning_stars");
         ARMORS_HELMETS_DARK_MATTER = forgeTag("armors/armors/dark_matter");
         ARMORS_CHESTPLATES_DARK_MATTER = forgeTag("armors/chestplates/dark_matter");
         ARMORS_LEGGINGS_DARK_MATTER = forgeTag("armors/leggings/dark_matter");
         ARMORS_BOOTS_DARK_MATTER = forgeTag("armors/boots/dark_matter");
         TOOLS_SWORDS_DARK_MATTER = forgeTag("tools/swords/dark_matter");
         TOOLS_AXES_DARK_MATTER = forgeTag("tools/axes/dark_matter");
         TOOLS_PICKAXES_DARK_MATTER = forgeTag("tools/pickaxes/dark_matter");
         TOOLS_SHOVELS_DARK_MATTER = forgeTag("tools/shovels/dark_matter");
         TOOLS_HOES_DARK_MATTER = forgeTag("tools/hoes/dark_matter");
         TOOLS_HAMMERS_DARK_MATTER = forgeTag("tools/hammers/dark_matter");
         ARMORS_HELMETS_RED_MATTER = forgeTag("armors/armors/red_matter");
         ARMORS_CHESTPLATES_RED_MATTER = forgeTag("armors/chestplates/red_matter");
         ARMORS_LEGGINGS_RED_MATTER = forgeTag("armors/leggings/red_matter");
         ARMORS_BOOTS_RED_MATTER = forgeTag("armors/boots/red_matter");
         TOOLS_SWORDS_RED_MATTER = forgeTag("tools/swords/red_matter");
         TOOLS_AXES_RED_MATTER = forgeTag("tools/axes/red_matter");
         TOOLS_PICKAXES_RED_MATTER = forgeTag("tools/pickaxes/red_matter");
         TOOLS_SHOVELS_RED_MATTER = forgeTag("tools/shovels/red_matter");
         TOOLS_HOES_RED_MATTER = forgeTag("tools/hoes/red_matter");
         TOOLS_HAMMERS_RED_MATTER = forgeTag("tools/hammers/red_matter");
         TOOLS_KATARS_RED_MATTER = forgeTag("tools/katars/red_matter");
         TOOLS_MORNING_STARS_RED_MATTER = forgeTag("tools/morning_stars/red_matter");
         ORES_LOOKUP = LazyTagLookup.create(ForgeRegistries.ITEMS, net.minecraftforge.common.Tags.Items.ORES);
         RAW_ORES_LOOKUP = LazyTagLookup.create(ForgeRegistries.ITEMS, net.minecraftforge.common.Tags.Items.RAW_MATERIALS);
      }
   }

   public static class Blocks {
      public static final TagKey BLACKLIST_HARVEST = tag("blacklist/harvest");
      public static final TagKey BLACKLIST_TIME_WATCH = tag("blacklist/time_watch");
      public static final TagKey FARMING_OVERRIDE = tag("farming_override");
      public static final TagKey NEEDS_DARK_MATTER_TOOL = tag("needs_dark_matter_tool");
      public static final TagKey NEEDS_RED_MATTER_TOOL = tag("needs_red_matter_tool");
      public static final TagKey MINEABLE_WITH_PE_KATAR = tag("mineable/katar");
      public static final TagKey MINEABLE_WITH_PE_HAMMER = tag("mineable/hammer");
      public static final TagKey MINEABLE_WITH_PE_MORNING_STAR = tag("mineable/morning_star");
      public static final TagKey MINEABLE_WITH_PE_SHEARS = tag("mineable/shears");
      public static final TagKey MINEABLE_WITH_PE_SWORD = tag("mineable/sword");
      public static final TagKey MINEABLE_WITH_HAMMER = forgeTag("mineable/hammer");
      public static final TagKey MINEABLE_WITH_KATAR = forgeTag("mineable/katar");
      public static final TagKey MINEABLE_WITH_MORNING_STAR = forgeTag("mineable/morning_star");

      private static void init() {
      }

      private Blocks() {
      }

      private static TagKey tag(String name) {
         return BlockTags.create(PECore.rl(name));
      }

      private static TagKey forgeTag(String name) {
         return BlockTags.create(new ResourceLocation("forge", name));
      }
   }

   public static class Entities {
      public static final TagKey BLACKLIST_SWRG = tag("blacklist/swrg");
      public static final TagKey BLACKLIST_INTERDICTION = tag("blacklist/interdiction");
      public static final TagKey RANDOMIZER_PEACEFUL = tag("randomizer/peaceful");
      public static final TagKey RANDOMIZER_HOSTILE = tag("randomizer/hostile");

      private static void init() {
      }

      private Entities() {
      }

      private static TagKey tag(String name) {
         return TagKey.m_203882_(Registries.f_256939_, PECore.rl(name));
      }
   }

   public static class BlockEntities {
      public static final TagKey BLACKLIST_TIME_WATCH = tag("blacklist/time_watch");
      public static final LazyTagLookup BLACKLIST_TIME_WATCH_LOOKUP;

      private static void init() {
      }

      private BlockEntities() {
      }

      private static TagKey tag(String name) {
         return TagKey.m_203882_(Registries.f_256922_, PECore.rl(name));
      }

      static {
         BLACKLIST_TIME_WATCH_LOOKUP = LazyTagLookup.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BLACKLIST_TIME_WATCH);
      }
   }
}
