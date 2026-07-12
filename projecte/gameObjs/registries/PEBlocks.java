package moze_intel.projecte.gameObjs.registries;

import java.util.function.Function;
import java.util.function.ToIntFunction;
import moze_intel.projecte.gameObjs.EnumCollectorTier;
import moze_intel.projecte.gameObjs.EnumFuelType;
import moze_intel.projecte.gameObjs.EnumMatterType;
import moze_intel.projecte.gameObjs.EnumRelayTier;
import moze_intel.projecte.gameObjs.blocks.AlchemicalChest;
import moze_intel.projecte.gameObjs.blocks.Collector;
import moze_intel.projecte.gameObjs.blocks.Condenser;
import moze_intel.projecte.gameObjs.blocks.CondenserMK2;
import moze_intel.projecte.gameObjs.blocks.InterdictionTorchEntityBlock;
import moze_intel.projecte.gameObjs.blocks.MatterBlock;
import moze_intel.projecte.gameObjs.blocks.MatterFurnace;
import moze_intel.projecte.gameObjs.blocks.Pedestal;
import moze_intel.projecte.gameObjs.blocks.ProjectETNT;
import moze_intel.projecte.gameObjs.blocks.Relay;
import moze_intel.projecte.gameObjs.blocks.TransmutationStone;
import moze_intel.projecte.gameObjs.entity.EntityNovaCataclysmPrimed;
import moze_intel.projecte.gameObjs.entity.EntityNovaCatalystPrimed;
import moze_intel.projecte.gameObjs.items.blocks.CollectorItem;
import moze_intel.projecte.gameObjs.items.blocks.ItemFuelBlock;
import moze_intel.projecte.gameObjs.items.blocks.RelayItem;
import moze_intel.projecte.gameObjs.registration.impl.BlockDeferredRegister;
import moze_intel.projecte.gameObjs.registration.impl.BlockRegistryObject;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class PEBlocks {
   public static final BlockDeferredRegister BLOCKS = new BlockDeferredRegister("projecte");
   public static final BlockRegistryObject ALCHEMICAL_CHEST;
   public static final BlockRegistryObject ALCHEMICAL_COAL;
   public static final BlockRegistryObject MOBIUS_FUEL;
   public static final BlockRegistryObject AETERNALIS_FUEL;
   public static final BlockRegistryObject COLLECTOR;
   public static final BlockRegistryObject COLLECTOR_MK2;
   public static final BlockRegistryObject COLLECTOR_MK3;
   public static final BlockRegistryObject CONDENSER;
   public static final BlockRegistryObject CONDENSER_MK2;
   public static final BlockRegistryObject DARK_MATTER_PEDESTAL;
   public static final BlockRegistryObject DARK_MATTER_FURNACE;
   public static final BlockRegistryObject RED_MATTER_FURNACE;
   public static final BlockRegistryObject DARK_MATTER;
   public static final BlockRegistryObject RED_MATTER;
   public static final BlockRegistryObject.WallOrFloorBlockRegistryObject INTERDICTION_TORCH;
   public static final BlockRegistryObject NOVA_CATALYST;
   public static final BlockRegistryObject NOVA_CATACLYSM;
   public static final BlockRegistryObject TRANSMUTATION_TABLE;
   public static final BlockRegistryObject RELAY;
   public static final BlockRegistryObject RELAY_MK2;
   public static final BlockRegistryObject RELAY_MK3;

   private static BlockRegistryObject registerFuelBlock(String name, EnumFuelType fuelType, MapColor mapColor) {
      return BLOCKS.registerDefaultProperties(name, () -> {
         return new Block(Properties.m_284310_().m_284180_(mapColor).m_280658_(NoteBlockInstrument.BASEDRUM).m_60999_().m_60913_(0.5F, 1.5F));
      }, (block, properties) -> {
         return new ItemFuelBlock(block, properties, fuelType);
      });
   }

   private static BlockRegistryObject registerCollector(String name, EnumCollectorTier collectorTier, ToIntFunction lightLevel) {
      return BLOCKS.registerDefaultProperties(name, () -> {
         return new Collector(collectorTier, Properties.m_284310_().m_284180_(MapColor.f_283761_).m_280658_(NoteBlockInstrument.PLING).m_60918_(SoundType.f_56744_).m_60999_().m_60913_(0.3F, 0.9F).m_60953_(lightLevel));
      }, CollectorItem::new);
   }

   private static BlockRegistryObject registerCondenser(String name, Function condenserFunction, Function itemCreator) {
      return BLOCKS.register(name, () -> {
         return (Condenser)condenserFunction.apply(Properties.m_284310_().m_284180_(MapColor.f_283947_).m_280658_(NoteBlockInstrument.BASEDRUM).m_60999_().m_60913_(10.0F, 3600000.0F));
      }, itemCreator);
   }

   private static BlockRegistryObject registerRelay(String name, EnumRelayTier relayTier, ToIntFunction lightLevel) {
      return BLOCKS.registerDefaultProperties(name, () -> {
         return new Relay(relayTier, Properties.m_284310_().m_284180_(MapColor.f_283927_).m_280658_(NoteBlockInstrument.BASEDRUM).m_60999_().m_60913_(10.0F, 30.0F).m_60953_(lightLevel));
      }, RelayItem::new);
   }

   private static BlockRegistryObject registerExplosive(String name, ProjectETNT.TNTEntityCreator tntEntityCreator) {
      return BLOCKS.register(name, () -> {
         return new ProjectETNT(Properties.m_284310_().m_284180_(MapColor.f_283816_).m_60978_(0.0F).m_60966_().m_60918_(SoundType.f_56740_).m_278183_().m_60924_((state, getter, pos) -> {
            return false;
         }), tntEntityCreator);
      });
   }

   private static BlockRegistryObject registerFurnace(String name, EnumMatterType matterType, float hardness, float resistance) {
      return BLOCKS.register(name, () -> {
         return new MatterFurnace(Properties.m_284310_().m_60999_().m_60913_(hardness, resistance).m_284180_(matterType.getMapColor()).m_280658_(NoteBlockInstrument.BASEDRUM).m_60953_((state) -> {
            return 14;
         }), matterType);
      }, (block) -> {
         return new BlockItem(block, (new Item.Properties()).m_41486_());
      });
   }

   private static BlockRegistryObject registerMatterBlock(String name, EnumMatterType matterType, float hardness, float resistance) {
      return BLOCKS.register(name, () -> {
         return new MatterBlock(Properties.m_284310_().m_60999_().m_60913_(hardness, resistance).m_284180_(matterType.getMapColor()).m_280658_(NoteBlockInstrument.BASEDRUM).m_60953_((state) -> {
            return 14;
         }), matterType);
      }, (block) -> {
         return new BlockItem(block, (new Item.Properties()).m_41486_());
      });
   }

   static {
      ALCHEMICAL_CHEST = BLOCKS.register("alchemical_chest", () -> {
         return new AlchemicalChest(Properties.m_284310_().m_284180_(MapColor.f_283947_).m_280658_(NoteBlockInstrument.BASEDRUM).m_60999_().m_60913_(10.0F, 3600000.0F));
      });
      ALCHEMICAL_COAL = registerFuelBlock("alchemical_coal_block", EnumFuelType.ALCHEMICAL_COAL, MapColor.f_283913_);
      MOBIUS_FUEL = registerFuelBlock("mobius_fuel_block", EnumFuelType.MOBIUS_FUEL, MapColor.f_283913_);
      AETERNALIS_FUEL = registerFuelBlock("aeternalis_fuel_block", EnumFuelType.AETERNALIS_FUEL, MapColor.f_283779_);
      COLLECTOR = registerCollector("collector_mk1", EnumCollectorTier.MK1, (state) -> {
         return 7;
      });
      COLLECTOR_MK2 = registerCollector("collector_mk2", EnumCollectorTier.MK2, (state) -> {
         return 11;
      });
      COLLECTOR_MK3 = registerCollector("collector_mk3", EnumCollectorTier.MK3, (state) -> {
         return 15;
      });
      CONDENSER = registerCondenser("condenser_mk1", Condenser::new, (block) -> {
         return new BlockItem(block, new Item.Properties());
      });
      CONDENSER_MK2 = registerCondenser("condenser_mk2", CondenserMK2::new, (block) -> {
         return new BlockItem(block, (new Item.Properties()).m_41486_());
      });
      DARK_MATTER_PEDESTAL = BLOCKS.register("dm_pedestal", () -> {
         return new Pedestal(Properties.m_284310_().m_284180_(MapColor.f_283927_).m_280658_(NoteBlockInstrument.BASEDRUM).m_60999_().m_60913_(1000000.0F, 3000000.0F).m_60953_((state) -> {
            return 12;
         }));
      }, (block) -> {
         return new BlockItem(block, (new Item.Properties()).m_41486_());
      });
      DARK_MATTER_FURNACE = registerFurnace("dm_furnace", EnumMatterType.DARK_MATTER, 1000000.0F, 3000000.0F);
      RED_MATTER_FURNACE = registerFurnace("rm_furnace", EnumMatterType.RED_MATTER, 2000000.0F, 6000000.0F);
      DARK_MATTER = registerMatterBlock("dark_matter_block", EnumMatterType.DARK_MATTER, 1000000.0F, 3000000.0F);
      RED_MATTER = registerMatterBlock("red_matter_block", EnumMatterType.RED_MATTER, 2000000.0F, 6000000.0F);
      INTERDICTION_TORCH = BLOCKS.registerWallOrFloorItem("interdiction_torch", InterdictionTorchEntityBlock.InterdictionTorch::new, InterdictionTorchEntityBlock.InterdictionTorchWall::new, Properties.m_284310_().m_278166_(PushReaction.DESTROY).m_60910_().m_60966_().m_60978_(0.0F).m_60953_((state) -> {
         return 14;
      }).m_60977_());
      NOVA_CATALYST = registerExplosive("nova_catalyst", EntityNovaCatalystPrimed::new);
      NOVA_CATACLYSM = registerExplosive("nova_cataclysm", EntityNovaCataclysmPrimed::new);
      TRANSMUTATION_TABLE = BLOCKS.register("transmutation_table", () -> {
         return new TransmutationStone(Properties.m_284310_().m_284180_(MapColor.f_283947_).m_280658_(NoteBlockInstrument.BASEDRUM).m_60999_().m_60913_(10.0F, 30.0F));
      });
      RELAY = registerRelay("relay_mk1", EnumRelayTier.MK1, (state) -> {
         return 7;
      });
      RELAY_MK2 = registerRelay("relay_mk2", EnumRelayTier.MK2, (state) -> {
         return 11;
      });
      RELAY_MK3 = registerRelay("relay_mk3", EnumRelayTier.MK3, (state) -> {
         return 15;
      });
   }
}
