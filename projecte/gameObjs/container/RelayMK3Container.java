package moze_intel.projecte.gameObjs.container;

import moze_intel.projecte.gameObjs.block_entities.RelayMK3BlockEntity;
import moze_intel.projecte.gameObjs.container.slots.SlotPredicates;
import moze_intel.projecte.gameObjs.container.slots.ValidatedSlot;
import moze_intel.projecte.gameObjs.registration.impl.BlockRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.gameObjs.registries.PEContainerTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.items.IItemHandler;

public class RelayMK3Container extends RelayMK1Container {
   public RelayMK3Container(int windowId, Inventory playerInv, RelayMK3BlockEntity relay) {
      super(PEContainerTypes.RELAY_MK3_CONTAINER, windowId, playerInv, relay);
   }

   void initSlots() {
      IItemHandler input = this.relay.getInput();
      IItemHandler output = this.relay.getOutput();
      this.m_38897_(new ValidatedSlot(output, 0, 164, 58, SlotPredicates.EMC_HOLDER));
      this.m_38897_(new ValidatedSlot(input, 0, 104, 58, SlotPredicates.RELAY_INV));
      int counter = 1;

      for(int i = 3; i >= 0; --i) {
         for(int j = 4; j >= 0; --j) {
            this.m_38897_(new ValidatedSlot(input, counter++, 28 + i * 18, 18 + j * 18, SlotPredicates.RELAY_INV));
         }
      }

      this.addPlayerInventory(26, 113);
   }

   protected BlockRegistryObject getValidBlock() {
      return PEBlocks.RELAY_MK3;
   }
}
