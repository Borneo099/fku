package moze_intel.projecte.gameObjs.registries;

import moze_intel.projecte.gameObjs.block_entities.AlchBlockEntityChest;
import moze_intel.projecte.gameObjs.block_entities.CollectorMK1BlockEntity;
import moze_intel.projecte.gameObjs.block_entities.CollectorMK2BlockEntity;
import moze_intel.projecte.gameObjs.block_entities.CollectorMK3BlockEntity;
import moze_intel.projecte.gameObjs.block_entities.CondenserBlockEntity;
import moze_intel.projecte.gameObjs.block_entities.CondenserMK2BlockEntity;
import moze_intel.projecte.gameObjs.block_entities.DMFurnaceBlockEntity;
import moze_intel.projecte.gameObjs.block_entities.DMPedestalBlockEntity;
import moze_intel.projecte.gameObjs.block_entities.EmcChestBlockEntity;
import moze_intel.projecte.gameObjs.block_entities.InterdictionTorchBlockEntity;
import moze_intel.projecte.gameObjs.block_entities.RMFurnaceBlockEntity;
import moze_intel.projecte.gameObjs.block_entities.RelayMK1BlockEntity;
import moze_intel.projecte.gameObjs.block_entities.RelayMK2BlockEntity;
import moze_intel.projecte.gameObjs.block_entities.RelayMK3BlockEntity;
import moze_intel.projecte.gameObjs.registration.impl.BlockEntityTypeDeferredRegister;
import moze_intel.projecte.gameObjs.registration.impl.BlockEntityTypeRegistryObject;

public class PEBlockEntityTypes {
   public static final BlockEntityTypeDeferredRegister BLOCK_ENTITY_TYPES = new BlockEntityTypeDeferredRegister("projecte");
   public static final BlockEntityTypeRegistryObject ALCHEMICAL_CHEST;
   public static final BlockEntityTypeRegistryObject COLLECTOR;
   public static final BlockEntityTypeRegistryObject COLLECTOR_MK2;
   public static final BlockEntityTypeRegistryObject COLLECTOR_MK3;
   public static final BlockEntityTypeRegistryObject CONDENSER;
   public static final BlockEntityTypeRegistryObject CONDENSER_MK2;
   public static final BlockEntityTypeRegistryObject RELAY;
   public static final BlockEntityTypeRegistryObject RELAY_MK2;
   public static final BlockEntityTypeRegistryObject RELAY_MK3;
   public static final BlockEntityTypeRegistryObject DARK_MATTER_FURNACE;
   public static final BlockEntityTypeRegistryObject RED_MATTER_FURNACE;
   public static final BlockEntityTypeRegistryObject INTERDICTION_TORCH;
   public static final BlockEntityTypeRegistryObject DARK_MATTER_PEDESTAL;

   static {
      ALCHEMICAL_CHEST = BLOCK_ENTITY_TYPES.builder(PEBlocks.ALCHEMICAL_CHEST, AlchBlockEntityChest::new).clientTicker(AlchBlockEntityChest::tickClient).serverTicker(AlchBlockEntityChest::tickServer).build();
      COLLECTOR = BLOCK_ENTITY_TYPES.builder(PEBlocks.COLLECTOR, CollectorMK1BlockEntity::new).serverTicker(CollectorMK1BlockEntity::tickServer).build();
      COLLECTOR_MK2 = BLOCK_ENTITY_TYPES.builder(PEBlocks.COLLECTOR_MK2, CollectorMK2BlockEntity::new).serverTicker(CollectorMK1BlockEntity::tickServer).build();
      COLLECTOR_MK3 = BLOCK_ENTITY_TYPES.builder(PEBlocks.COLLECTOR_MK3, CollectorMK3BlockEntity::new).serverTicker(CollectorMK1BlockEntity::tickServer).build();
      CONDENSER = BLOCK_ENTITY_TYPES.builder(PEBlocks.CONDENSER, CondenserBlockEntity::new).clientTicker(EmcChestBlockEntity::lidAnimateTick).serverTicker(CondenserBlockEntity::tickServer).build();
      CONDENSER_MK2 = BLOCK_ENTITY_TYPES.builder(PEBlocks.CONDENSER_MK2, CondenserMK2BlockEntity::new).clientTicker(EmcChestBlockEntity::lidAnimateTick).serverTicker(CondenserBlockEntity::tickServer).build();
      RELAY = BLOCK_ENTITY_TYPES.builder(PEBlocks.RELAY, RelayMK1BlockEntity::new).serverTicker(RelayMK1BlockEntity::tickServer).build();
      RELAY_MK2 = BLOCK_ENTITY_TYPES.builder(PEBlocks.RELAY_MK2, RelayMK2BlockEntity::new).serverTicker(RelayMK1BlockEntity::tickServer).build();
      RELAY_MK3 = BLOCK_ENTITY_TYPES.builder(PEBlocks.RELAY_MK3, RelayMK3BlockEntity::new).serverTicker(RelayMK1BlockEntity::tickServer).build();
      DARK_MATTER_FURNACE = BLOCK_ENTITY_TYPES.builder(PEBlocks.DARK_MATTER_FURNACE, DMFurnaceBlockEntity::new).serverTicker(DMFurnaceBlockEntity::tickServer).build();
      RED_MATTER_FURNACE = BLOCK_ENTITY_TYPES.builder(PEBlocks.RED_MATTER_FURNACE, RMFurnaceBlockEntity::new).serverTicker(DMFurnaceBlockEntity::tickServer).build();
      INTERDICTION_TORCH = BLOCK_ENTITY_TYPES.builder(PEBlocks.INTERDICTION_TORCH, InterdictionTorchBlockEntity::new).commonTicker(InterdictionTorchBlockEntity::tick).build();
      DARK_MATTER_PEDESTAL = BLOCK_ENTITY_TYPES.builder(PEBlocks.DARK_MATTER_PEDESTAL, DMPedestalBlockEntity::new).clientTicker(DMPedestalBlockEntity::tickClient).serverTicker(DMPedestalBlockEntity::tickServer).build();
   }
}
