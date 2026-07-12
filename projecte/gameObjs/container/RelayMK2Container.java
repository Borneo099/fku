package moze_intel.projecte.gameObjs.container;

import moze_intel.projecte.gameObjs.block_entities.RelayMK2BlockEntity;
import moze_intel.projecte.gameObjs.container.slots.SlotPredicates;
import moze_intel.projecte.gameObjs.container.slots.ValidatedSlot;
import moze_intel.projecte.gameObjs.registration.impl.BlockRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.gameObjs.registries.PEContainerTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.items.IItemHandler;

public class RelayMK2Container extends RelayMK1Container {
   public RelayMK2Container(int windowId, Inventory playerInv, RelayMK2BlockEntity relay) {
      super(PEContainerTypes.RELAY_MK2_CONTAINER, windowId, playerInv, relay);
   }

   void initSlots() {
      IItemHandler input = this.relay.getInput();
      IItemHandler output = this.relay.getOutput();
      this.m_38897_(new ValidatedSlot(output, 0, 144, 44, SlotPredicates.EMC_HOLDER));
      this.m_38897_(new ValidatedSlot(input, 0, 84, 44, SlotPredicates.RELAY_INV));
      int counter = 1;

      for(int i = 2; i >= 0; --i) {
         for(int j = 3; j >= 0; --j) {
            this.m_38897_(new ValidatedSlot(input, counter++, 26 + i * 18, 18 + j * 18, SlotPredicates.RELAY_INV));
         }
      }

      this.addPlayerInventory(16, 101);
   }

   protected BlockRegistryObject getValidBlock() {
      return PEBlocks.RELAY_MK2;
   }
}
