package moze_intel.projecte.gameObjs.container;

import java.util.Objects;
import java.util.function.Predicate;
import moze_intel.projecte.gameObjs.block_entities.CondenserBlockEntity;
import moze_intel.projecte.gameObjs.block_entities.CondenserMK2BlockEntity;
import moze_intel.projecte.gameObjs.container.slots.SlotCondenserLock;
import moze_intel.projecte.gameObjs.container.slots.SlotPredicates;
import moze_intel.projecte.gameObjs.container.slots.ValidatedSlot;
import moze_intel.projecte.gameObjs.registration.impl.BlockRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.gameObjs.registries.PEContainerTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.items.IItemHandler;

public class CondenserMK2Container extends CondenserContainer {
   public CondenserMK2Container(int windowId, Inventory playerInv, CondenserMK2BlockEntity condenser) {
      super(PEContainerTypes.CONDENSER_MK2_CONTAINER, windowId, playerInv, condenser);
   }

   protected void initSlots() {
      CondenserBlockEntity var10003 = (CondenserBlockEntity)this.blockEntity;
      Objects.requireNonNull(var10003);
      this.m_38897_(new SlotCondenserLock(var10003::getLockInfo, 0, 12, 6));
      IItemHandler input = ((CondenserBlockEntity)this.blockEntity).getInput();
      Predicate validator = (s) -> {
         return SlotPredicates.HAS_EMC.test(s) && !((CondenserBlockEntity)this.blockEntity).isStackEqualToLock(s);
      };

      int i;
      for(int i = 0; i < 7; ++i) {
         for(i = 0; i < 6; ++i) {
            this.m_38897_(new ValidatedSlot(input, i + i * 6, 12 + i * 18, 26 + i * 18, validator));
         }
      }

      IItemHandler output = ((CondenserBlockEntity)this.blockEntity).getOutput();

      for(i = 0; i < 7; ++i) {
         for(int j = 0; j < 6; ++j) {
            this.m_38897_(new ValidatedSlot(output, j + i * 6, 138 + j * 18, 26 + i * 18, SlotPredicates.ALWAYS_FALSE));
         }
      }

      this.addPlayerInventory(48, 154);
   }

   protected BlockRegistryObject getValidBlock() {
      return PEBlocks.CONDENSER_MK2;
   }
}
