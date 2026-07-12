package moze_intel.projecte.gameObjs.container.slots.transmutation;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Optional;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import moze_intel.projecte.api.capabilities.item.IItemEmcHolder;
import moze_intel.projecte.gameObjs.container.inventory.TransmutationInventory;
import moze_intel.projecte.gameObjs.container.slots.InventoryContainerSlot;
import moze_intel.projecte.gameObjs.container.slots.SlotPredicates;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.MathUtils;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SlotInput extends InventoryContainerSlot {
   private final TransmutationInventory inv;

   public SlotInput(TransmutationInventory inv, int index, int x, int y) {
      super(inv, index, x, y);
      this.inv = inv;
   }

   public boolean m_5857_(@NotNull ItemStack stack) {
      return SlotPredicates.RELAY_INV.test(stack);
   }

   public @NotNull ItemStack m_6201_(int amount) {
      ItemStack stack = super.m_6201_(amount);
      if (!stack.m_41619_() && this.inv.isServer()) {
         this.inv.syncChangedSlots(Collections.singletonList(this.getSlotIndex()), IKnowledgeProvider.TargetUpdateType.IF_NEEDED);
      }

      return stack;
   }

   public void initialize(@NotNull ItemStack stack) {
      super.initialize(stack);
   }

   public void m_5852_(@NotNull ItemStack stack) {
      super.m_5852_(stack);
      if (this.inv.isServer()) {
         if (stack.m_41619_()) {
            this.inv.syncChangedSlots(Collections.singletonList(this.getSlotIndex()), IKnowledgeProvider.TargetUpdateType.ALL);
         } else {
            if (EMCHelper.doesItemHaveEmc(stack)) {
               this.inv.handleKnowledge(stack);
            }

            Optional capability = stack.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).resolve();
            if (capability.isPresent()) {
               IItemEmcHolder emcHolder = (IItemEmcHolder)capability.get();
               long shrunkenAvailableEMC = MathUtils.clampToLong(this.inv.provider.getEmc());
               long actualInserted = emcHolder.insertEmc(stack, shrunkenAvailableEMC, IEmcStorage.EmcAction.EXECUTE);
               if (actualInserted > 0L) {
                  this.inv.syncChangedSlots(Collections.singletonList(this.getSlotIndex()), IKnowledgeProvider.TargetUpdateType.NONE);
                  this.inv.removeEmc(BigInteger.valueOf(actualInserted));
               } else if (emcHolder.getStoredEmc(stack) > 0L) {
                  this.inv.syncChangedSlots(Collections.singletonList(this.getSlotIndex()), IKnowledgeProvider.TargetUpdateType.ALL);
               } else {
                  this.inv.syncChangedSlots(Collections.singletonList(this.getSlotIndex()), IKnowledgeProvider.TargetUpdateType.NONE);
               }
            } else {
               this.inv.syncChangedSlots(Collections.singletonList(this.getSlotIndex()), IKnowledgeProvider.TargetUpdateType.NONE);
            }
         }
      }

   }

   public int m_6641_() {
      return 1;
   }
}
