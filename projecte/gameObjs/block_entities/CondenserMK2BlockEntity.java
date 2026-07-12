package moze_intel.projecte.gameObjs.block_entities;

import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import moze_intel.projecte.gameObjs.container.CondenserMK2Container;
import moze_intel.projecte.gameObjs.container.slots.SlotPredicates;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.text.TextComponentUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;

public class CondenserMK2BlockEntity extends CondenserBlockEntity {
   public CondenserMK2BlockEntity(BlockPos pos, BlockState state) {
      super(PEBlockEntityTypes.CONDENSER_MK2, pos, state);
   }

   protected @NotNull IItemHandler createAutomationInventory() {
      IItemHandlerModifiable automationInput = new WrappedItemHandler(this.getInput(), WrappedItemHandler.WriteMode.IN) {
         public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return SlotPredicates.HAS_EMC.test(stack) && !CondenserMK2BlockEntity.this.isStackEqualToLock(stack) ? super.insertItem(slot, stack, simulate) : stack;
         }
      };
      IItemHandlerModifiable automationOutput = new WrappedItemHandler(this.getOutput(), WrappedItemHandler.WriteMode.OUT);
      return new CombinedInvWrapper(new IItemHandlerModifiable[]{automationInput, automationOutput});
   }

   protected ItemStackHandler createInput() {
      return new EmcBlockEntity.StackHandler(42);
   }

   protected ItemStackHandler createOutput() {
      return new EmcBlockEntity.StackHandler(42);
   }

   protected void condense() {
      while(this.hasSpace() && this.getStoredEmc() >= this.requiredEmc) {
         this.pushStack();
         this.forceExtractEmc(this.requiredEmc, IEmcStorage.EmcAction.EXECUTE);
      }

      if (this.hasSpace()) {
         for(int i = 0; i < this.getInput().getSlots(); ++i) {
            ItemStack stack = this.getInput().getStackInSlot(i);
            if (!stack.m_41619_()) {
               this.forceInsertEmc(EMCHelper.getEmcSellValue(stack) * (long)stack.m_41613_(), IEmcStorage.EmcAction.EXECUTE);
               this.getInput().setStackInSlot(i, ItemStack.f_41583_);
               break;
            }
         }
      }

   }

   public void m_142466_(@NotNull CompoundTag nbt) {
      super.m_142466_(nbt);
      this.getOutput().deserializeNBT(nbt.m_128469_("Output"));
   }

   protected void m_183515_(@NotNull CompoundTag tag) {
      super.m_183515_(tag);
      tag.m_128365_("Output", this.getOutput().serializeNBT());
   }

   public AbstractContainerMenu m_7208_(int windowId, @NotNull Inventory playerInv, @NotNull Player player) {
      return new CondenserMK2Container(windowId, playerInv, this);
   }

   public @NotNull Component m_5446_() {
      return TextComponentUtil.build(PEBlocks.CONDENSER_MK2);
   }
}
