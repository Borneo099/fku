package moze_intel.projecte.gameObjs.registries;

import moze_intel.projecte.gameObjs.EnumFuelType;
import moze_intel.projecte.gameObjs.EnumMatterType;
import moze_intel.projecte.gameObjs.items.AlchemicalBag;
import moze_intel.projecte.gameObjs.items.AlchemicalFuel;
import moze_intel.projecte.gameObjs.items.CataliticLens;
import moze_intel.projecte.gameObjs.items.DestructionCatalyst;
import moze_intel.projecte.gameObjs.items.DiviningRod;
import moze_intel.projecte.gameObjs.items.EvertideAmulet;
import moze_intel.projecte.gameObjs.items.GemEternalDensity;
import moze_intel.projecte.gameObjs.items.HyperkineticLens;
import moze_intel.projecte.gameObjs.items.KleinStar;
import moze_intel.projecte.gameObjs.items.MercurialEye;
import moze_intel.projecte.gameObjs.items.PhilosophersStone;
import moze_intel.projecte.gameObjs.items.RepairTalisman;
import moze_intel.projecte.gameObjs.items.Tome;
import moze_intel.projecte.gameObjs.items.TransmutationTablet;
import moze_intel.projecte.gameObjs.items.VolcaniteAmulet;
import moze_intel.projecte.gameObjs.items.armor.DMArmor;
import moze_intel.projecte.gameObjs.items.armor.GemChest;
import moze_intel.projecte.gameObjs.items.armor.GemFeet;
import moze_intel.projecte.gameObjs.items.armor.GemHelmet;
import moze_intel.projecte.gameObjs.items.armor.GemLegs;
import moze_intel.projecte.gameObjs.items.armor.RMArmor;
import moze_intel.projecte.gameObjs.items.rings.Arcana;
import moze_intel.projecte.gameObjs.items.rings.ArchangelSmite;
import moze_intel.projecte.gameObjs.items.rings.BlackHoleBand;
import moze_intel.projecte.gameObjs.items.rings.BodyStone;
import moze_intel.projecte.gameObjs.items.rings.HarvestGoddess;
import moze_intel.projecte.gameObjs.items.rings.Ignition;
import moze_intel.projecte.gameObjs.items.rings.LifeStone;
import moze_intel.projecte.gameObjs.items.rings.MindStone;
import moze_intel.projecte.gameObjs.items.rings.SWRG;
import moze_intel.projecte.gameObjs.items.rings.SoulStone;
import moze_intel.projecte.gameObjs.items.rings.TimeWatch;
import moze_intel.projecte.gameObjs.items.rings.VoidRing;
import moze_intel.projecte.gameObjs.items.rings.Zero;
import moze_intel.projecte.gameObjs.items.tools.PEAxe;
import moze_intel.projecte.gameObjs.items.tools.PEHammer;
import moze_intel.projecte.gameObjs.items.tools.PEHoe;
import moze_intel.projecte.gameObjs.items.tools.PEKatar;
import moze_intel.projecte.gameObjs.items.tools.PEMorningStar;
import moze_intel.projecte.gameObjs.items.tools.PEPickaxe;
import moze_intel.projecte.gameObjs.items.tools.PEShears;
import moze_intel.projecte.gameObjs.items.tools.PEShovel;
import moze_intel.projecte.gameObjs.items.tools.PESword;
import moze_intel.projecte.gameObjs.items.tools.RedMatterSword;
import moze_intel.projecte.gameObjs.registration.impl.ItemDeferredRegister;
import moze_intel.projecte.gameObjs.registration.impl.ItemRegistryObject;
import moze_intel.projecte.utils.text.ILangEntry;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ArmorItem.Type;

public class PEItems {
   public static final ItemDeferredRegister ITEMS = new ItemDeferredRegister("projecte");
   public static final ItemRegistryObject PHILOSOPHERS_STONE;
   public static final ItemRegistryObject REPAIR_TALISMAN;
   public static final ItemRegistryObject LOW_COVALENCE_DUST;
   public static final ItemRegistryObject MEDIUM_COVALENCE_DUST;
   public static final ItemRegistryObject HIGH_COVALENCE_DUST;
   public static final ItemRegistryObject WHITE_ALCHEMICAL_BAG;
   public static final ItemRegistryObject ORANGE_ALCHEMICAL_BAG;
   public static final ItemRegistryObject MAGENTA_ALCHEMICAL_BAG;
   public static final ItemRegistryObject LIGHT_BLUE_ALCHEMICAL_BAG;
   public static final ItemRegistryObject YELLOW_ALCHEMICAL_BAG;
   public static final ItemRegistryObject LIME_ALCHEMICAL_BAG;
   public static final ItemRegistryObject PINK_ALCHEMICAL_BAG;
   public static final ItemRegistryObject GRAY_ALCHEMICAL_BAG;
   public static final ItemRegistryObject LIGHT_GRAY_ALCHEMICAL_BAG;
   public static final ItemRegistryObject CYAN_ALCHEMICAL_BAG;
   public static final ItemRegistryObject PURPLE_ALCHEMICAL_BAG;
   public static final ItemRegistryObject BLUE_ALCHEMICAL_BAG;
   public static final ItemRegistryObject BROWN_ALCHEMICAL_BAG;
   public static final ItemRegistryObject GREEN_ALCHEMICAL_BAG;
   public static final ItemRegistryObject RED_ALCHEMICAL_BAG;
   public static final ItemRegistryObject BLACK_ALCHEMICAL_BAG;
   public static final ItemRegistryObject KLEIN_STAR_EIN;
   public static final ItemRegistryObject KLEIN_STAR_ZWEI;
   public static final ItemRegistryObject KLEIN_STAR_DREI;
   public static final ItemRegistryObject KLEIN_STAR_VIER;
   public static final ItemRegistryObject KLEIN_STAR_SPHERE;
   public static final ItemRegistryObject KLEIN_STAR_OMEGA;
   public static final ItemRegistryObject ALCHEMICAL_COAL;
   public static final ItemRegistryObject MOBIUS_FUEL;
   public static final ItemRegistryObject AETERNALIS_FUEL;
   public static final ItemRegistryObject DARK_MATTER;
   public static final ItemRegistryObject RED_MATTER;
   public static final ItemRegistryObject DARK_MATTER_PICKAXE;
   public static final ItemRegistryObject DARK_MATTER_AXE;
   public static final ItemRegistryObject DARK_MATTER_SHOVEL;
   public static final ItemRegistryObject DARK_MATTER_SWORD;
   public static final ItemRegistryObject DARK_MATTER_HOE;
   public static final ItemRegistryObject DARK_MATTER_SHEARS;
   public static final ItemRegistryObject DARK_MATTER_HAMMER;
   public static final ItemRegistryObject RED_MATTER_PICKAXE;
   public static final ItemRegistryObject RED_MATTER_AXE;
   public static final ItemRegistryObject RED_MATTER_SHOVEL;
   public static final ItemRegistryObject RED_MATTER_SWORD;
   public static final ItemRegistryObject RED_MATTER_HOE;
   public static final ItemRegistryObject RED_MATTER_SHEARS;
   public static final ItemRegistryObject RED_MATTER_HAMMER;
   public static final ItemRegistryObject RED_MATTER_KATAR;
   public static final ItemRegistryObject RED_MATTER_MORNING_STAR;
   public static final ItemRegistryObject DARK_MATTER_HELMET;
   public static final ItemRegistryObject DARK_MATTER_CHESTPLATE;
   public static final ItemRegistryObject DARK_MATTER_LEGGINGS;
   public static final ItemRegistryObject DARK_MATTER_BOOTS;
   public static final ItemRegistryObject RED_MATTER_HELMET;
   public static final ItemRegistryObject RED_MATTER_CHESTPLATE;
   public static final ItemRegistryObject RED_MATTER_LEGGINGS;
   public static final ItemRegistryObject RED_MATTER_BOOTS;
   public static final ItemRegistryObject GEM_HELMET;
   public static final ItemRegistryObject GEM_CHESTPLATE;
   public static final ItemRegistryObject GEM_LEGGINGS;
   public static final ItemRegistryObject GEM_BOOTS;
   public static final ItemRegistryObject IRON_BAND;
   public static final ItemRegistryObject BLACK_HOLE_BAND;
   public static final ItemRegistryObject ARCHANGEL_SMITE;
   public static final ItemRegistryObject HARVEST_GODDESS_BAND;
   public static final ItemRegistryObject IGNITION_RING;
   public static final ItemRegistryObject ZERO_RING;
   public static final ItemRegistryObject SWIFTWOLF_RENDING_GALE;
   public static final ItemRegistryObject WATCH_OF_FLOWING_TIME;
   public static final ItemRegistryObject EVERTIDE_AMULET;
   public static final ItemRegistryObject VOLCANITE_AMULET;
   public static final ItemRegistryObject GEM_OF_ETERNAL_DENSITY;
   public static final ItemRegistryObject MERCURIAL_EYE;
   public static final ItemRegistryObject VOID_RING;
   public static final ItemRegistryObject ARCANA_RING;
   public static final ItemRegistryObject BODY_STONE;
   public static final ItemRegistryObject SOUL_STONE;
   public static final ItemRegistryObject MIND_STONE;
   public static final ItemRegistryObject LIFE_STONE;
   public static final ItemRegistryObject LOW_DIVINING_ROD;
   public static final ItemRegistryObject MEDIUM_DIVINING_ROD;
   public static final ItemRegistryObject HIGH_DIVINING_ROD;
   public static final ItemRegistryObject DESTRUCTION_CATALYST;
   public static final ItemRegistryObject HYPERKINETIC_LENS;
   public static final ItemRegistryObject CATALYTIC_LENS;
   public static final ItemRegistryObject TOME_OF_KNOWLEDGE;
   public static final ItemRegistryObject TRANSMUTATION_TABLET;

   private static ItemRegistryObject registerBag(DyeColor color) {
      return ITEMS.registerNoStack(color.m_41065_() + "_alchemical_bag", (properties) -> {
         return new AlchemicalBag(properties, color);
      });
   }

   private static ItemRegistryObject registerKleinStar(KleinStar.EnumKleinTier tier) {
      return ITEMS.registerNoStack("klein_star_" + tier.name, (properties) -> {
         if (tier == KleinStar.EnumKleinTier.OMEGA) {
            properties = properties.m_41497_(Rarity.EPIC);
         }

         return new KleinStar(properties, tier);
      });
   }

   private static ItemRegistryObject registerAlchemicalFuel(EnumFuelType fuelType) {
      return ITEMS.register(fuelType.m_7912_(), (properties) -> {
         if (fuelType == EnumFuelType.AETERNALIS_FUEL) {
            properties = properties.m_41497_(Rarity.RARE);
         }

         return new AlchemicalFuel(properties, fuelType);
      });
   }

   public static AlchemicalBag getBag(DyeColor color) {
      AlchemicalBag var10000;
      switch (color) {
         case WHITE:
            var10000 = (AlchemicalBag)WHITE_ALCHEMICAL_BAG.get();
            break;
         case ORANGE:
            var10000 = (AlchemicalBag)ORANGE_ALCHEMICAL_BAG.get();
            break;
         case MAGENTA:
            var10000 = (AlchemicalBag)MAGENTA_ALCHEMICAL_BAG.get();
            break;
         case LIGHT_BLUE:
            var10000 = (AlchemicalBag)LIGHT_BLUE_ALCHEMICAL_BAG.get();
            break;
         case YELLOW:
            var10000 = (AlchemicalBag)YELLOW_ALCHEMICAL_BAG.get();
            break;
         case LIME:
            var10000 = (AlchemicalBag)LIME_ALCHEMICAL_BAG.get();
            break;
         case PINK:
            var10000 = (AlchemicalBag)PINK_ALCHEMICAL_BAG.get();
            break;
         case GRAY:
            var10000 = (AlchemicalBag)GRAY_ALCHEMICAL_BAG.get();
            break;
         case LIGHT_GRAY:
            var10000 = (AlchemicalBag)LIGHT_GRAY_ALCHEMICAL_BAG.get();
            break;
         case CYAN:
            var10000 = (AlchemicalBag)CYAN_ALCHEMICAL_BAG.get();
            break;
         case PURPLE:
            var10000 = (AlchemicalBag)PURPLE_ALCHEMICAL_BAG.get();
            break;
         case BLUE:
            var10000 = (AlchemicalBag)BLUE_ALCHEMICAL_BAG.get();
            break;
         case BROWN:
            var10000 = (AlchemicalBag)BROWN_ALCHEMICAL_BAG.get();
            break;
         case GREEN:
            var10000 = (AlchemicalBag)GREEN_ALCHEMICAL_BAG.get();
            break;
         case RED:
            var10000 = (AlchemicalBag)RED_ALCHEMICAL_BAG.get();
            break;
         case BLACK:
            var10000 = (AlchemicalBag)BLACK_ALCHEMICAL_BAG.get();
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public static KleinStar getStar(KleinStar.EnumKleinTier tier) {
      KleinStar var10000;
      switch (tier) {
         case EIN:
            var10000 = (KleinStar)KLEIN_STAR_EIN.get();
            break;
         case ZWEI:
            var10000 = (KleinStar)KLEIN_STAR_ZWEI.get();
            break;
         case DREI:
            var10000 = (KleinStar)KLEIN_STAR_DREI.get();
            break;
         case VIER:
            var10000 = (KleinStar)KLEIN_STAR_VIER.get();
            break;
         case SPHERE:
            var10000 = (KleinStar)KLEIN_STAR_SPHERE.get();
            break;
         case OMEGA:
            var10000 = (KleinStar)KLEIN_STAR_OMEGA.get();
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   static {
      PHILOSOPHERS_STONE = ITEMS.registerNoStack("philosophers_stone", PhilosophersStone::new);
      REPAIR_TALISMAN = ITEMS.registerNoStack("repair_talisman", RepairTalisman::new);
      LOW_COVALENCE_DUST = ITEMS.register("low_covalence_dust");
      MEDIUM_COVALENCE_DUST = ITEMS.register("medium_covalence_dust");
      HIGH_COVALENCE_DUST = ITEMS.register("high_covalence_dust");
      WHITE_ALCHEMICAL_BAG = registerBag(DyeColor.WHITE);
      ORANGE_ALCHEMICAL_BAG = registerBag(DyeColor.ORANGE);
      MAGENTA_ALCHEMICAL_BAG = registerBag(DyeColor.MAGENTA);
      LIGHT_BLUE_ALCHEMICAL_BAG = registerBag(DyeColor.LIGHT_BLUE);
      YELLOW_ALCHEMICAL_BAG = registerBag(DyeColor.YELLOW);
      LIME_ALCHEMICAL_BAG = registerBag(DyeColor.LIME);
      PINK_ALCHEMICAL_BAG = registerBag(DyeColor.PINK);
      GRAY_ALCHEMICAL_BAG = registerBag(DyeColor.GRAY);
      LIGHT_GRAY_ALCHEMICAL_BAG = registerBag(DyeColor.LIGHT_GRAY);
      CYAN_ALCHEMICAL_BAG = registerBag(DyeColor.CYAN);
      PURPLE_ALCHEMICAL_BAG = registerBag(DyeColor.PURPLE);
      BLUE_ALCHEMICAL_BAG = registerBag(DyeColor.BLUE);
      BROWN_ALCHEMICAL_BAG = registerBag(DyeColor.BROWN);
      GREEN_ALCHEMICAL_BAG = registerBag(DyeColor.GREEN);
      RED_ALCHEMICAL_BAG = registerBag(DyeColor.RED);
      BLACK_ALCHEMICAL_BAG = registerBag(DyeColor.BLACK);
      KLEIN_STAR_EIN = registerKleinStar(KleinStar.EnumKleinTier.EIN);
      KLEIN_STAR_ZWEI = registerKleinStar(KleinStar.EnumKleinTier.ZWEI);
      KLEIN_STAR_DREI = registerKleinStar(KleinStar.EnumKleinTier.DREI);
      KLEIN_STAR_VIER = registerKleinStar(KleinStar.EnumKleinTier.VIER);
      KLEIN_STAR_SPHERE = registerKleinStar(KleinStar.EnumKleinTier.SPHERE);
      KLEIN_STAR_OMEGA = registerKleinStar(KleinStar.EnumKleinTier.OMEGA);
      ALCHEMICAL_COAL = registerAlchemicalFuel(EnumFuelType.ALCHEMICAL_COAL);
      MOBIUS_FUEL = registerAlchemicalFuel(EnumFuelType.MOBIUS_FUEL);
      AETERNALIS_FUEL = registerAlchemicalFuel(EnumFuelType.AETERNALIS_FUEL);
      DARK_MATTER = ITEMS.registerFireImmune("dark_matter");
      RED_MATTER = ITEMS.registerFireImmune("red_matter");
      DARK_MATTER_PICKAXE = ITEMS.registerNoStackFireImmune("dm_pick", (properties) -> {
         return new PEPickaxe(EnumMatterType.DARK_MATTER, 2, properties);
      });
      DARK_MATTER_AXE = ITEMS.registerNoStackFireImmune("dm_axe", (properties) -> {
         return new PEAxe(EnumMatterType.DARK_MATTER, 2, properties);
      });
      DARK_MATTER_SHOVEL = ITEMS.registerNoStackFireImmune("dm_shovel", (properties) -> {
         return new PEShovel(EnumMatterType.DARK_MATTER, 2, properties);
      });
      DARK_MATTER_SWORD = ITEMS.registerNoStackFireImmune("dm_sword", (properties) -> {
         return new PESword(EnumMatterType.DARK_MATTER, 2, 9, properties);
      });
      DARK_MATTER_HOE = ITEMS.registerNoStackFireImmune("dm_hoe", (properties) -> {
         return new PEHoe(EnumMatterType.DARK_MATTER, 2, properties);
      });
      DARK_MATTER_SHEARS = ITEMS.registerNoStackFireImmune("dm_shears", (properties) -> {
         return new PEShears(EnumMatterType.DARK_MATTER, 2, properties);
      });
      DARK_MATTER_HAMMER = ITEMS.registerNoStackFireImmune("dm_hammer", (properties) -> {
         return new PEHammer(EnumMatterType.DARK_MATTER, 2, properties);
      });
      RED_MATTER_PICKAXE = ITEMS.registerNoStackFireImmune("rm_pick", (properties) -> {
         return new PEPickaxe(EnumMatterType.RED_MATTER, 3, properties);
      });
      RED_MATTER_AXE = ITEMS.registerNoStackFireImmune("rm_axe", (properties) -> {
         return new PEAxe(EnumMatterType.RED_MATTER, 3, properties);
      });
      RED_MATTER_SHOVEL = ITEMS.registerNoStackFireImmune("rm_shovel", (properties) -> {
         return new PEShovel(EnumMatterType.RED_MATTER, 3, properties);
      });
      RED_MATTER_SWORD = ITEMS.registerNoStackFireImmune("rm_sword", RedMatterSword::new);
      RED_MATTER_HOE = ITEMS.registerNoStackFireImmune("rm_hoe", (properties) -> {
         return new PEHoe(EnumMatterType.RED_MATTER, 3, properties);
      });
      RED_MATTER_SHEARS = ITEMS.registerNoStackFireImmune("rm_shears", (properties) -> {
         return new PEShears(EnumMatterType.RED_MATTER, 3, properties);
      });
      RED_MATTER_HAMMER = ITEMS.registerNoStackFireImmune("rm_hammer", (properties) -> {
         return new PEHammer(EnumMatterType.RED_MATTER, 3, properties);
      });
      RED_MATTER_KATAR = ITEMS.registerNoStackFireImmune("rm_katar", (properties) -> {
         return new PEKatar(EnumMatterType.RED_MATTER, 4, properties);
      });
      RED_MATTER_MORNING_STAR = ITEMS.registerNoStackFireImmune("rm_morning_star", (properties) -> {
         return new PEMorningStar(EnumMatterType.RED_MATTER, 4, properties);
      });
      DARK_MATTER_HELMET = ITEMS.registerNoStackFireImmune("dm_helmet", (properties) -> {
         return new DMArmor(Type.HELMET, properties);
      });
      DARK_MATTER_CHESTPLATE = ITEMS.registerNoStackFireImmune("dm_chestplate", (properties) -> {
         return new DMArmor(Type.CHESTPLATE, properties);
      });
      DARK_MATTER_LEGGINGS = ITEMS.registerNoStackFireImmune("dm_leggings", (properties) -> {
         return new DMArmor(Type.LEGGINGS, properties);
      });
      DARK_MATTER_BOOTS = ITEMS.registerNoStackFireImmune("dm_boots", (properties) -> {
         return new DMArmor(Type.BOOTS, properties);
      });
      RED_MATTER_HELMET = ITEMS.registerNoStackFireImmune("rm_helmet", (properties) -> {
         return new RMArmor(Type.HELMET, properties);
      });
      RED_MATTER_CHESTPLATE = ITEMS.registerNoStackFireImmune("rm_chestplate", (properties) -> {
         return new RMArmor(Type.CHESTPLATE, properties);
      });
      RED_MATTER_LEGGINGS = ITEMS.registerNoStackFireImmune("rm_leggings", (properties) -> {
         return new RMArmor(Type.LEGGINGS, properties);
      });
      RED_MATTER_BOOTS = ITEMS.registerNoStackFireImmune("rm_boots", (properties) -> {
         return new RMArmor(Type.BOOTS, properties);
      });
      GEM_HELMET = ITEMS.registerNoStackFireImmune("gem_helmet", GemHelmet::new);
      GEM_CHESTPLATE = ITEMS.registerNoStackFireImmune("gem_chestplate", GemChest::new);
      GEM_LEGGINGS = ITEMS.registerNoStackFireImmune("gem_leggings", GemLegs::new);
      GEM_BOOTS = ITEMS.registerNoStackFireImmune("gem_boots", GemFeet::new);
      IRON_BAND = ITEMS.register("iron_band");
      BLACK_HOLE_BAND = ITEMS.registerNoStackFireImmune("black_hole_band", BlackHoleBand::new);
      ARCHANGEL_SMITE = ITEMS.registerNoStackFireImmune("archangel_smite", ArchangelSmite::new);
      HARVEST_GODDESS_BAND = ITEMS.registerNoStackFireImmune("harvest_goddess_band", HarvestGoddess::new);
      IGNITION_RING = ITEMS.registerNoStackFireImmune("ignition_ring", Ignition::new);
      ZERO_RING = ITEMS.registerNoStackFireImmune("zero_ring", Zero::new);
      SWIFTWOLF_RENDING_GALE = ITEMS.registerNoStackFireImmune("swiftwolf_rending_gale", SWRG::new);
      WATCH_OF_FLOWING_TIME = ITEMS.registerNoStackFireImmune("watch_of_flowing_time", TimeWatch::new);
      EVERTIDE_AMULET = ITEMS.registerNoStackFireImmune("evertide_amulet", EvertideAmulet::new);
      VOLCANITE_AMULET = ITEMS.registerNoStackFireImmune("volcanite_amulet", VolcaniteAmulet::new);
      GEM_OF_ETERNAL_DENSITY = ITEMS.registerNoStackFireImmune("gem_of_eternal_density", GemEternalDensity::new);
      MERCURIAL_EYE = ITEMS.registerNoStackFireImmune("mercurial_eye", MercurialEye::new);
      VOID_RING = ITEMS.registerNoStackFireImmune("void_ring", VoidRing::new);
      ARCANA_RING = ITEMS.registerNoStackFireImmune("arcana_ring", (properties) -> {
         return new Arcana(properties.m_41497_(Rarity.RARE));
      });
      BODY_STONE = ITEMS.registerNoStackFireImmune("body_stone", BodyStone::new);
      SOUL_STONE = ITEMS.registerNoStackFireImmune("soul_stone", SoulStone::new);
      MIND_STONE = ITEMS.registerNoStackFireImmune("mind_stone", MindStone::new);
      LIFE_STONE = ITEMS.registerNoStackFireImmune("life_stone", LifeStone::new);
      LOW_DIVINING_ROD = ITEMS.registerNoStack("divining_rod_1", (properties) -> {
         return new DiviningRod(properties, new ILangEntry[]{PELang.DIVINING_RANGE_3});
      });
      MEDIUM_DIVINING_ROD = ITEMS.registerNoStack("divining_rod_2", (properties) -> {
         return new DiviningRod(properties, new ILangEntry[]{PELang.DIVINING_RANGE_3, PELang.DIVINING_RANGE_16});
      });
      HIGH_DIVINING_ROD = ITEMS.registerNoStack("divining_rod_3", (properties) -> {
         return new DiviningRod(properties, new ILangEntry[]{PELang.DIVINING_RANGE_3, PELang.DIVINING_RANGE_16, PELang.DIVINING_RANGE_64});
      });
      DESTRUCTION_CATALYST = ITEMS.registerNoStack("destruction_catalyst", DestructionCatalyst::new);
      HYPERKINETIC_LENS = ITEMS.registerNoStackFireImmune("hyperkinetic_lens", HyperkineticLens::new);
      CATALYTIC_LENS = ITEMS.registerNoStackFireImmune("catalytic_lens", CataliticLens::new);
      TOME_OF_KNOWLEDGE = ITEMS.registerNoStack("tome", (properties) -> {
         return new Tome(properties.m_41497_(Rarity.EPIC));
      });
      TRANSMUTATION_TABLET = ITEMS.registerNoStackFireImmune("transmutation_tablet", TransmutationTablet::new);
   }
}
