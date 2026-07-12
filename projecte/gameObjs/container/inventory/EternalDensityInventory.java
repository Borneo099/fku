package moze_intel.projecte.gameObjs.container.inventory;

import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.to_server.UpdateGemModePKT;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class EternalDensityInventory implements IItemHandlerModifiable {
   private final ItemStackHandler inventory = new ItemStackHandler(9);
   private boolean isInWhitelist;
   public final ItemStack invItem;

   public EternalDensityInventory(ItemStack stack) {
      this.invItem = stack;
      if (stack.m_41782_()) {
         this.readFromNBT(stack.m_41784_());
      }

   }

   public int getSlots() {
      return this.inventory.getSlots();
   }

   public @NotNull ItemStack getStackInSlot(int slot) {
      return this.inventory.getStackInSlot(slot);
   }

   public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
      ItemStack ret = this.inventory.insertItem(slot, stack, simulate);
      this.writeBack();
      return ret;
   }

   public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
      ItemStack ret = this.inventory.extractItem(slot, amount, simulate);
      this.writeBack();
      return ret;
   }

   public int getSlotLimit(int slot) {
      return 1;
   }

   public boolean isItemValid(int slot, @NotNull ItemStack stack) {
      return this.inventory.isItemValid(slot, stack);
   }

   public void setStackInSlot(int slot, @NotNull ItemStack stack) {
      this.inventory.setStackInSlot(slot, stack);
      this.writeBack();
   }

   private void writeBack() {
      for(int i = 0; i < this.inventory.getSlots(); ++i) {
         if (this.inventory.getStackInSlot(i).m_41619_()) {
            this.inventory.setStackInSlot(i, ItemStack.f_41583_);
         }
      }

      this.writeToNBT(this.invItem.m_41784_());
   }

   public void readFromNBT(CompoundTag nbt) {
      this.isInWhitelist = nbt.m_128471_("Whitelist");
      this.inventory.deserializeNBT(nbt.m_128469_("Items"));
   }

   public void writeToNBT(CompoundTag nbt) {
      nbt.m_128379_("Whitelist", this.isInWhitelist);
      nbt.m_128365_("Items", this.inventory.serializeNBT());
   }

   public void changeMode() {
      this.isInWhitelist = !this.isInWhitelist;
      this.writeBack();
      PacketHandler.sendToServer(new UpdateGemModePKT(this.isInWhitelist));
   }

   public boolean isWhitelistMode() {
      return this.isInWhitelist;
   }
}
