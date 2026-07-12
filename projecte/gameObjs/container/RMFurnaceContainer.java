package moze_intel.projecte.gameObjs.container;

import java.util.function.Predicate;
import moze_intel.projecte.gameObjs.block_entities.RMFurnaceBlockEntity;
import moze_intel.projecte.gameObjs.container.slots.MatterFurnaceOutputSlot;
import moze_intel.projecte.gameObjs.container.slots.SlotPredicates;
import moze_intel.projecte.gameObjs.container.slots.ValidatedSlot;
import moze_intel.projecte.gameObjs.registration.impl.BlockRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.gameObjs.registries.PEContainerTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.items.IItemHandler;

public class RMFurnaceContainer extends DMFurnaceContainer {
   public RMFurnaceContainer(int windowId, Inventory playerInv, RMFurnaceBlockEntity furnace) {
      super(PEContainerTypes.RM_FURNACE_CONTAINER, windowId, playerInv, furnace);
   }

   void initSlots() {
      IItemHandler fuel = this.furnace.getFuel();
      IItemHandler input = this.furnace.getInput();
      IItemHandler output = this.furnace.getOutput();
      this.m_38897_(new ValidatedSlot(fuel, 0, 65, 53, SlotPredicates.FURNACE_FUEL));
      Predicate inputPredicate = (stack) -> {
         return !this.furnace.getSmeltingResult(stack).m_41619_();
      };
      this.m_38897_(new ValidatedSlot(input, 0, 65, 17, inputPredicate));
      int counter = 1;

      int i;
      int j;
      for(i = 2; i >= 0; --i) {
         for(j = 3; j >= 0; --j) {
            this.m_38897_(new ValidatedSlot(input, counter++, 11 + i * 18, 8 + j * 18, inputPredicate));
         }
      }

      counter = output.getSlots() - 1;
      this.m_38897_(new MatterFurnaceOutputSlot(this.playerInv.f_35978_, output, counter--, 125, 35));

      for(i = 0; i < 3; ++i) {
         for(j = 0; j < 4; ++j) {
            this.m_38897_(new MatterFurnaceOutputSlot(this.playerInv.f_35978_, output, counter--, 147 + i * 18, 8 + j * 18));
         }
      }

      this.addPlayerInventory(24, 84);
   }

   protected BlockRegistryObject getValidBlock() {
      return PEBlocks.RED_MATTER_FURNACE;
   }
}
