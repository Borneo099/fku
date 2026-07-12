package moze_intel.projecte.gameObjs.registries;

import java.util.Objects;
import java.util.function.Consumer;
import moze_intel.projecte.gameObjs.items.rings.Arcana;
import moze_intel.projecte.gameObjs.registration.impl.CreativeTabDeferredRegister;
import moze_intel.projecte.gameObjs.registration.impl.CreativeTabRegistryObject;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;

public class PECreativeTabs {
   public static final CreativeTabDeferredRegister CREATIVE_TABS = new CreativeTabDeferredRegister("projecte", PECreativeTabs::addToExistingTabs);
   public static final CreativeTabRegistryObject PROJECTE;

   private static void addArmor(Consumer output) {
      output.accept(PEItems.DARK_MATTER_HELMET);
      output.accept(PEItems.DARK_MATTER_CHESTPLATE);
      output.accept(PEItems.DARK_MATTER_LEGGINGS);
      output.accept(PEItems.DARK_MATTER_BOOTS);
      output.accept(PEItems.RED_MATTER_HELMET);
      output.accept(PEItems.RED_MATTER_CHESTPLATE);
      output.accept(PEItems.RED_MATTER_LEGGINGS);
      output.accept(PEItems.RED_MATTER_BOOTS);
      output.accept(PEItems.GEM_HELMET);
      output.accept(PEItems.GEM_CHESTPLATE);
      output.accept(PEItems.GEM_LEGGINGS);
      output.accept(PEItems.GEM_BOOTS);
   }

   private static void addToExistingTabs(BuildCreativeModeTabContentsEvent event) {
      ResourceKey tabKey = event.getTabKey();
      if (tabKey == CreativeModeTabs.f_256788_) {
         addToExistingTab(event, PEBlocks.ALCHEMICAL_COAL, PEBlocks.MOBIUS_FUEL, PEBlocks.AETERNALIS_FUEL, PEBlocks.DARK_MATTER, PEBlocks.RED_MATTER);
      } else if (tabKey == CreativeModeTabs.f_256791_) {
         addToExistingTab(event, PEBlocks.INTERDICTION_TORCH, PEBlocks.TRANSMUTATION_TABLE, PEBlocks.ALCHEMICAL_CHEST, PEBlocks.CONDENSER, PEBlocks.CONDENSER_MK2, PEBlocks.COLLECTOR, PEBlocks.COLLECTOR_MK2, PEBlocks.COLLECTOR_MK3, PEBlocks.RELAY, PEBlocks.RELAY_MK2, PEBlocks.RELAY_MK3, PEBlocks.DARK_MATTER_PEDESTAL, PEBlocks.DARK_MATTER_FURNACE, PEBlocks.RED_MATTER_FURNACE);
      } else if (tabKey == CreativeModeTabs.f_257028_) {
         addToExistingTab(event, PEBlocks.ALCHEMICAL_CHEST, PEBlocks.CONDENSER, PEBlocks.CONDENSER_MK2, PEBlocks.COLLECTOR, PEBlocks.COLLECTOR_MK2, PEBlocks.COLLECTOR_MK3, PEBlocks.RELAY, PEBlocks.RELAY_MK2, PEBlocks.RELAY_MK3, PEBlocks.DARK_MATTER_PEDESTAL, PEBlocks.DARK_MATTER_FURNACE, PEBlocks.RED_MATTER_FURNACE, PEBlocks.NOVA_CATALYST, PEBlocks.NOVA_CATACLYSM);
      } else if (tabKey == CreativeModeTabs.f_256869_) {
         addToExistingTab(event, PEItems.DARK_MATTER_PICKAXE, PEItems.DARK_MATTER_AXE, PEItems.DARK_MATTER_SHOVEL, PEItems.DARK_MATTER_HOE, PEItems.DARK_MATTER_SHEARS, PEItems.DARK_MATTER_HAMMER, PEItems.RED_MATTER_PICKAXE, PEItems.RED_MATTER_AXE, PEItems.RED_MATTER_SHOVEL, PEItems.RED_MATTER_HOE, PEItems.RED_MATTER_SHEARS, PEItems.RED_MATTER_HAMMER, PEItems.RED_MATTER_MORNING_STAR);
         addToExistingTab(event, PEItems.PHILOSOPHERS_STONE, PEItems.REPAIR_TALISMAN, PEItems.TOME_OF_KNOWLEDGE, PEItems.TRANSMUTATION_TABLET, PEItems.DESTRUCTION_CATALYST, PEItems.BLACK_HOLE_BAND, PEItems.HARVEST_GODDESS_BAND, PEItems.IGNITION_RING, PEItems.ZERO_RING, PEItems.SWIFTWOLF_RENDING_GALE, PEItems.WATCH_OF_FLOWING_TIME, PEItems.EVERTIDE_AMULET, PEItems.VOLCANITE_AMULET, PEItems.GEM_OF_ETERNAL_DENSITY, PEItems.MERCURIAL_EYE, PEItems.VOID_RING);

         for(byte i = 0; i < ((Arcana)PEItems.ARCANA_RING.m_5456_()).getModeCount(); ++i) {
            ItemStack stack = new ItemStack(PEItems.ARCANA_RING);
            stack.m_41784_().m_128344_("Mode", i);
            event.m_246342_(stack);
         }

         addToExistingTab(event, PEItems.BODY_STONE, PEItems.SOUL_STONE, PEItems.MIND_STONE, PEItems.LIFE_STONE, PEItems.LOW_DIVINING_ROD, PEItems.MEDIUM_DIVINING_ROD, PEItems.HIGH_DIVINING_ROD);
         addToExistingTab(event, PEItems.KLEIN_STAR_EIN, PEItems.KLEIN_STAR_ZWEI, PEItems.KLEIN_STAR_DREI, PEItems.KLEIN_STAR_VIER, PEItems.KLEIN_STAR_SPHERE, PEItems.KLEIN_STAR_OMEGA);
         addToExistingTab(event, PEItems.WHITE_ALCHEMICAL_BAG, PEItems.ORANGE_ALCHEMICAL_BAG, PEItems.MAGENTA_ALCHEMICAL_BAG, PEItems.LIGHT_BLUE_ALCHEMICAL_BAG, PEItems.YELLOW_ALCHEMICAL_BAG, PEItems.LIME_ALCHEMICAL_BAG, PEItems.PINK_ALCHEMICAL_BAG, PEItems.GRAY_ALCHEMICAL_BAG, PEItems.LIGHT_GRAY_ALCHEMICAL_BAG, PEItems.CYAN_ALCHEMICAL_BAG, PEItems.PURPLE_ALCHEMICAL_BAG, PEItems.BLUE_ALCHEMICAL_BAG, PEItems.BROWN_ALCHEMICAL_BAG, PEItems.GREEN_ALCHEMICAL_BAG, PEItems.RED_ALCHEMICAL_BAG, PEItems.BLACK_ALCHEMICAL_BAG);
      } else if (tabKey == CreativeModeTabs.f_256797_) {
         addToExistingTab(event, PEItems.DARK_MATTER_SWORD, PEItems.RED_MATTER_SWORD, PEItems.RED_MATTER_KATAR);
         Objects.requireNonNull(event);
         addArmor(event::m_246326_);
         addToExistingTab(event, PEItems.ARCHANGEL_SMITE, PEItems.HYPERKINETIC_LENS, PEItems.CATALYTIC_LENS);
      } else if (tabKey == CreativeModeTabs.f_256968_) {
         addToExistingTab(event, PEItems.LOW_COVALENCE_DUST, PEItems.MEDIUM_COVALENCE_DUST, PEItems.HIGH_COVALENCE_DUST, PEItems.ALCHEMICAL_COAL, PEItems.MOBIUS_FUEL, PEItems.AETERNALIS_FUEL, PEItems.DARK_MATTER, PEItems.RED_MATTER, PEItems.IRON_BAND);
      }

   }

   private static void addToExistingTab(BuildCreativeModeTabContentsEvent event, ItemLike... items) {
      ItemLike[] var2 = items;
      int var3 = items.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         ItemLike item = var2[var4];
         event.m_246326_(item);
      }

   }

   static {
      PROJECTE = CREATIVE_TABS.registerMain(PELang.PROJECTE, PEItems.PHILOSOPHERS_STONE, (builder) -> {
         return builder.m_257501_((displayParameters, output) -> {
            output.m_246326_(PEItems.PHILOSOPHERS_STONE);
            output.m_246326_(PEItems.REPAIR_TALISMAN);
            output.m_246326_(PEItems.TOME_OF_KNOWLEDGE);
            output.m_246326_(PEItems.TRANSMUTATION_TABLET);
            output.m_246326_(PEBlocks.TRANSMUTATION_TABLE);
            output.m_246326_(PEItems.LOW_COVALENCE_DUST);
            output.m_246326_(PEItems.MEDIUM_COVALENCE_DUST);
            output.m_246326_(PEItems.HIGH_COVALENCE_DUST);
            output.m_246326_(PEItems.ALCHEMICAL_COAL);
            output.m_246326_(PEItems.MOBIUS_FUEL);
            output.m_246326_(PEItems.AETERNALIS_FUEL);
            output.m_246326_(PEItems.DARK_MATTER);
            output.m_246326_(PEItems.RED_MATTER);
            output.m_246326_(PEBlocks.ALCHEMICAL_COAL);
            output.m_246326_(PEBlocks.MOBIUS_FUEL);
            output.m_246326_(PEBlocks.AETERNALIS_FUEL);
            output.m_246326_(PEBlocks.DARK_MATTER);
            output.m_246326_(PEBlocks.RED_MATTER);
            output.m_246326_(PEItems.KLEIN_STAR_EIN);
            output.m_246326_(PEItems.KLEIN_STAR_ZWEI);
            output.m_246326_(PEItems.KLEIN_STAR_DREI);
            output.m_246326_(PEItems.KLEIN_STAR_VIER);
            output.m_246326_(PEItems.KLEIN_STAR_SPHERE);
            output.m_246326_(PEItems.KLEIN_STAR_OMEGA);
            output.m_246326_(PEItems.DARK_MATTER_PICKAXE);
            output.m_246326_(PEItems.DARK_MATTER_AXE);
            output.m_246326_(PEItems.DARK_MATTER_SHOVEL);
            output.m_246326_(PEItems.DARK_MATTER_HOE);
            output.m_246326_(PEItems.DARK_MATTER_SHEARS);
            output.m_246326_(PEItems.DARK_MATTER_HAMMER);
            output.m_246326_(PEItems.DARK_MATTER_SWORD);
            output.m_246326_(PEItems.RED_MATTER_PICKAXE);
            output.m_246326_(PEItems.RED_MATTER_AXE);
            output.m_246326_(PEItems.RED_MATTER_SHOVEL);
            output.m_246326_(PEItems.RED_MATTER_HOE);
            output.m_246326_(PEItems.RED_MATTER_SHEARS);
            output.m_246326_(PEItems.RED_MATTER_HAMMER);
            output.m_246326_(PEItems.RED_MATTER_MORNING_STAR);
            output.m_246326_(PEItems.RED_MATTER_SWORD);
            output.m_246326_(PEItems.RED_MATTER_KATAR);
            Objects.requireNonNull(output);
            addArmor(output::m_246326_);
            output.m_246326_(PEItems.DESTRUCTION_CATALYST);
            output.m_246326_(PEItems.HYPERKINETIC_LENS);
            output.m_246326_(PEItems.CATALYTIC_LENS);
            output.m_246326_(PEItems.IRON_BAND);
            output.m_246326_(PEItems.BLACK_HOLE_BAND);
            output.m_246326_(PEItems.ARCHANGEL_SMITE);
            output.m_246326_(PEItems.HARVEST_GODDESS_BAND);
            output.m_246326_(PEItems.IGNITION_RING);
            output.m_246326_(PEItems.ZERO_RING);
            output.m_246326_(PEItems.SWIFTWOLF_RENDING_GALE);
            output.m_246326_(PEItems.WATCH_OF_FLOWING_TIME);
            output.m_246326_(PEItems.EVERTIDE_AMULET);
            output.m_246326_(PEItems.VOLCANITE_AMULET);
            output.m_246326_(PEItems.GEM_OF_ETERNAL_DENSITY);
            output.m_246326_(PEItems.MERCURIAL_EYE);
            output.m_246326_(PEItems.VOID_RING);

            for(byte i = 0; i < ((Arcana)PEItems.ARCANA_RING.m_5456_()).getModeCount(); ++i) {
               ItemStack stack = new ItemStack(PEItems.ARCANA_RING);
               stack.m_41784_().m_128344_("Mode", i);
               output.m_246342_(stack);
            }

            output.m_246326_(PEItems.BODY_STONE);
            output.m_246326_(PEItems.SOUL_STONE);
            output.m_246326_(PEItems.MIND_STONE);
            output.m_246326_(PEItems.LIFE_STONE);
            output.m_246326_(PEItems.LOW_DIVINING_ROD);
            output.m_246326_(PEItems.MEDIUM_DIVINING_ROD);
            output.m_246326_(PEItems.HIGH_DIVINING_ROD);
            output.m_246326_(PEItems.WHITE_ALCHEMICAL_BAG);
            output.m_246326_(PEItems.ORANGE_ALCHEMICAL_BAG);
            output.m_246326_(PEItems.MAGENTA_ALCHEMICAL_BAG);
            output.m_246326_(PEItems.LIGHT_BLUE_ALCHEMICAL_BAG);
            output.m_246326_(PEItems.YELLOW_ALCHEMICAL_BAG);
            output.m_246326_(PEItems.LIME_ALCHEMICAL_BAG);
            output.m_246326_(PEItems.PINK_ALCHEMICAL_BAG);
            output.m_246326_(PEItems.GRAY_ALCHEMICAL_BAG);
            output.m_246326_(PEItems.LIGHT_GRAY_ALCHEMICAL_BAG);
            output.m_246326_(PEItems.CYAN_ALCHEMICAL_BAG);
            output.m_246326_(PEItems.PURPLE_ALCHEMICAL_BAG);
            output.m_246326_(PEItems.BLUE_ALCHEMICAL_BAG);
            output.m_246326_(PEItems.BROWN_ALCHEMICAL_BAG);
            output.m_246326_(PEItems.GREEN_ALCHEMICAL_BAG);
            output.m_246326_(PEItems.RED_ALCHEMICAL_BAG);
            output.m_246326_(PEItems.BLACK_ALCHEMICAL_BAG);
            output.m_246326_(PEBlocks.ALCHEMICAL_CHEST);
            output.m_246326_(PEBlocks.CONDENSER);
            output.m_246326_(PEBlocks.CONDENSER_MK2);
            output.m_246326_(PEBlocks.COLLECTOR);
            output.m_246326_(PEBlocks.COLLECTOR_MK2);
            output.m_246326_(PEBlocks.COLLECTOR_MK3);
            output.m_246326_(PEBlocks.RELAY);
            output.m_246326_(PEBlocks.RELAY_MK2);
            output.m_246326_(PEBlocks.RELAY_MK3);
            output.m_246326_(PEBlocks.DARK_MATTER_PEDESTAL);
            output.m_246326_(PEBlocks.DARK_MATTER_FURNACE);
            output.m_246326_(PEBlocks.RED_MATTER_FURNACE);
            output.m_246326_(PEBlocks.INTERDICTION_TORCH);
            output.m_246326_(PEBlocks.NOVA_CATALYST);
            output.m_246326_(PEBlocks.NOVA_CATACLYSM);
         });
      });
   }
}
