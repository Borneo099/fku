package moze_intel.projecte.gameObjs.registries;

import moze_intel.projecte.gameObjs.block_entities.AlchBlockEntityChest;
import moze_intel.projecte.gameObjs.block_entities.CollectorMK1BlockEntity;
import moze_intel.projecte.gameObjs.block_entities.CollectorMK2BlockEntity;
import moze_intel.projecte.gameObjs.block_entities.CollectorMK3BlockEntity;
import moze_intel.projecte.gameObjs.block_entities.CondenserBlockEntity;
import moze_intel.projecte.gameObjs.block_entities.CondenserMK2BlockEntity;
import moze_intel.projecte.gameObjs.block_entities.DMFurnaceBlockEntity;
import moze_intel.projecte.gameObjs.block_entities.RMFurnaceBlockEntity;
import moze_intel.projecte.gameObjs.block_entities.RelayMK1BlockEntity;
import moze_intel.projecte.gameObjs.block_entities.RelayMK2BlockEntity;
import moze_intel.projecte.gameObjs.block_entities.RelayMK3BlockEntity;
import moze_intel.projecte.gameObjs.container.AlchBagContainer;
import moze_intel.projecte.gameObjs.container.AlchChestContainer;
import moze_intel.projecte.gameObjs.container.CollectorMK1Container;
import moze_intel.projecte.gameObjs.container.CollectorMK2Container;
import moze_intel.projecte.gameObjs.container.CollectorMK3Container;
import moze_intel.projecte.gameObjs.container.CondenserContainer;
import moze_intel.projecte.gameObjs.container.CondenserMK2Container;
import moze_intel.projecte.gameObjs.container.DMFurnaceContainer;
import moze_intel.projecte.gameObjs.container.EternalDensityContainer;
import moze_intel.projecte.gameObjs.container.MercurialEyeContainer;
import moze_intel.projecte.gameObjs.container.RMFurnaceContainer;
import moze_intel.projecte.gameObjs.container.RelayMK1Container;
import moze_intel.projecte.gameObjs.container.RelayMK2Container;
import moze_intel.projecte.gameObjs.container.RelayMK3Container;
import moze_intel.projecte.gameObjs.container.TransmutationContainer;
import moze_intel.projecte.gameObjs.registration.INamedEntry;
import moze_intel.projecte.gameObjs.registration.impl.ContainerTypeDeferredRegister;
import moze_intel.projecte.gameObjs.registration.impl.ContainerTypeRegistryObject;

public class PEContainerTypes {
   public static final ContainerTypeDeferredRegister CONTAINER_TYPES = new ContainerTypeDeferredRegister("projecte");
   public static final ContainerTypeRegistryObject RM_FURNACE_CONTAINER;
   public static final ContainerTypeRegistryObject DM_FURNACE_CONTAINER;
   public static final ContainerTypeRegistryObject CONDENSER_CONTAINER;
   public static final ContainerTypeRegistryObject CONDENSER_MK2_CONTAINER;
   public static final ContainerTypeRegistryObject ALCH_CHEST_CONTAINER;
   public static final ContainerTypeRegistryObject ALCH_BAG_CONTAINER;
   public static final ContainerTypeRegistryObject ETERNAL_DENSITY_CONTAINER;
   public static final ContainerTypeRegistryObject TRANSMUTATION_CONTAINER;
   public static final ContainerTypeRegistryObject RELAY_MK1_CONTAINER;
   public static final ContainerTypeRegistryObject RELAY_MK2_CONTAINER;
   public static final ContainerTypeRegistryObject RELAY_MK3_CONTAINER;
   public static final ContainerTypeRegistryObject COLLECTOR_MK1_CONTAINER;
   public static final ContainerTypeRegistryObject COLLECTOR_MK2_CONTAINER;
   public static final ContainerTypeRegistryObject COLLECTOR_MK3_CONTAINER;
   public static final ContainerTypeRegistryObject MERCURIAL_EYE_CONTAINER;

   static {
      RM_FURNACE_CONTAINER = CONTAINER_TYPES.register(PEBlocks.RED_MATTER_FURNACE, RMFurnaceBlockEntity.class, RMFurnaceContainer::new);
      DM_FURNACE_CONTAINER = CONTAINER_TYPES.register(PEBlocks.DARK_MATTER_FURNACE, DMFurnaceBlockEntity.class, DMFurnaceContainer::new);
      CONDENSER_CONTAINER = CONTAINER_TYPES.register(PEBlocks.CONDENSER, CondenserBlockEntity.class, CondenserContainer::new);
      CONDENSER_MK2_CONTAINER = CONTAINER_TYPES.register(PEBlocks.CONDENSER_MK2, CondenserMK2BlockEntity.class, CondenserMK2Container::new);
      ALCH_CHEST_CONTAINER = CONTAINER_TYPES.register(PEBlocks.ALCHEMICAL_CHEST, AlchBlockEntityChest.class, AlchChestContainer::new);
      ALCH_BAG_CONTAINER = CONTAINER_TYPES.register("alchemical_bag", AlchBagContainer::fromNetwork);
      ETERNAL_DENSITY_CONTAINER = CONTAINER_TYPES.register((INamedEntry)PEItems.GEM_OF_ETERNAL_DENSITY, EternalDensityContainer::fromNetwork);
      TRANSMUTATION_CONTAINER = CONTAINER_TYPES.register((INamedEntry)PEBlocks.TRANSMUTATION_TABLE, TransmutationContainer::fromNetwork);
      RELAY_MK1_CONTAINER = CONTAINER_TYPES.register(PEBlocks.RELAY, RelayMK1BlockEntity.class, RelayMK1Container::new);
      RELAY_MK2_CONTAINER = CONTAINER_TYPES.register(PEBlocks.RELAY_MK2, RelayMK2BlockEntity.class, RelayMK2Container::new);
      RELAY_MK3_CONTAINER = CONTAINER_TYPES.register(PEBlocks.RELAY_MK3, RelayMK3BlockEntity.class, RelayMK3Container::new);
      COLLECTOR_MK1_CONTAINER = CONTAINER_TYPES.register(PEBlocks.COLLECTOR, CollectorMK1BlockEntity.class, CollectorMK1Container::new);
      COLLECTOR_MK2_CONTAINER = CONTAINER_TYPES.register(PEBlocks.COLLECTOR_MK2, CollectorMK2BlockEntity.class, CollectorMK2Container::new);
      COLLECTOR_MK3_CONTAINER = CONTAINER_TYPES.register(PEBlocks.COLLECTOR_MK3, CollectorMK3BlockEntity.class, CollectorMK3Container::new);
      MERCURIAL_EYE_CONTAINER = CONTAINER_TYPES.register((INamedEntry)PEItems.MERCURIAL_EYE, MercurialEyeContainer::fromNetwork);
   }
}
