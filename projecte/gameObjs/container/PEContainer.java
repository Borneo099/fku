package moze_intel.projecte.gameObjs.container;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import moze_intel.projecte.gameObjs.container.slots.HotBarSlot;
import moze_intel.projecte.gameObjs.container.slots.IInsertableSlot;
import moze_intel.projecte.gameObjs.container.slots.InventoryContainerSlot;
import moze_intel.projecte.gameObjs.container.slots.MainInventorySlot;
import moze_intel.projecte.gameObjs.registration.impl.BlockRegistryObject;
import moze_intel.projecte.gameObjs.registration.impl.ContainerTypeRegistryObject;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.IPEPacket;
import moze_intel.projecte.network.packets.to_client.UpdateWindowIntPKT;
import moze_intel.projecte.network.packets.to_client.UpdateWindowLongPKT;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PEContainer extends AbstractContainerMenu {
   protected final List inventoryContainerSlots = new ArrayList();
   protected final List mainInventorySlots = new ArrayList();
   protected final List hotBarSlots = new ArrayList();
   private final List intFields = new ArrayList();
   protected final List longFields = new ArrayList();
   protected final Inventory playerInv;

   protected PEContainer(ContainerTypeRegistryObject typeRO, int id, Inventory playerInv) {
      super((MenuType)typeRO.get(), id);
      this.playerInv = playerInv;
   }

   protected void addPlayerInventory(int xStart, int yStart) {
      int slotSize = 18;
      int rows = 3;

      int i;
      for(i = 0; i < rows; ++i) {
         for(int j = 0; j < 9; ++j) {
            this.m_38897_(this.createMainInventorySlot(this.playerInv, j + i * 9 + 9, xStart + j * slotSize, yStart + i * slotSize));
         }
      }

      yStart = yStart + slotSize * rows + 4;

      for(i = 0; i < Inventory.m_36059_(); ++i) {
         this.m_38897_(this.createHotBarSlot(this.playerInv, i, xStart + i * slotSize, yStart));
      }

   }

   protected MainInventorySlot createMainInventorySlot(@NotNull Inventory inv, int index, int x, int y) {
      return new MainInventorySlot(inv, index, x, y);
   }

   protected HotBarSlot createHotBarSlot(@NotNull Inventory inv, int index, int x, int y) {
      return new HotBarSlot(inv, index, x, y);
   }

   protected @NotNull Slot m_38897_(@NotNull Slot slot) {
      super.m_38897_(slot);
      if (slot instanceof InventoryContainerSlot containerSlot) {
         this.inventoryContainerSlots.add(containerSlot);
      } else if (slot instanceof MainInventorySlot inventorySlot) {
         this.mainInventorySlots.add(inventorySlot);
      } else if (slot instanceof HotBarSlot hotBarSlot) {
         this.hotBarSlots.add(hotBarSlot);
      }

      return slot;
   }

   public @Nullable Slot tryGetSlot(int slotId) {
      return slotId >= 0 && slotId < this.f_38839_.size() ? this.m_38853_(slotId) : null;
   }

   public @NotNull ItemStack m_7648_(@NotNull Player player, int slotID) {
      Slot currentSlot = (Slot)this.f_38839_.get(slotID);
      if (currentSlot != null && currentSlot.m_6657_()) {
         ItemStack slotStack = currentSlot.m_7993_();
         ItemStack stackToInsert;
         if (currentSlot instanceof InventoryContainerSlot) {
            stackToInsert = insertItem(this.hotBarSlots, slotStack, true);
            stackToInsert = insertItem(this.mainInventorySlots, stackToInsert, true);
            stackToInsert = insertItem(this.hotBarSlots, stackToInsert, false);
            stackToInsert = insertItem(this.mainInventorySlots, stackToInsert, false);
         } else {
            stackToInsert = insertItem(this.inventoryContainerSlots, slotStack, true);
            if (slotStack.m_41613_() == stackToInsert.m_41613_()) {
               stackToInsert = insertItem(this.inventoryContainerSlots, stackToInsert, false);
               if (slotStack.m_41613_() == stackToInsert.m_41613_()) {
                  if (currentSlot instanceof MainInventorySlot) {
                     stackToInsert = insertItem(this.hotBarSlots, stackToInsert, true);
                     stackToInsert = insertItem(this.hotBarSlots, stackToInsert, false);
                  } else if (currentSlot instanceof HotBarSlot) {
                     stackToInsert = insertItem(this.mainInventorySlots, stackToInsert, true);
                     stackToInsert = insertItem(this.mainInventorySlots, stackToInsert, false);
                  }
               }
            }
         }

         return stackToInsert.m_41613_() == slotStack.m_41613_() ? ItemStack.f_41583_ : this.transferSuccess(currentSlot, player, slotStack, stackToInsert);
      } else {
         return ItemStack.f_41583_;
      }
   }

   protected @NotNull ItemStack transferSuccess(@NotNull Slot currentSlot, @NotNull Player player, @NotNull ItemStack slotStack, @NotNull ItemStack stackToInsert) {
      int difference = slotStack.m_41613_() - stackToInsert.m_41613_();
      ItemStack newStack = currentSlot.m_6201_(difference);
      currentSlot.m_142406_(player, newStack);
      return newStack;
   }

   public static @NotNull ItemStack insertItem(List slots, @NotNull ItemStack stack, boolean ignoreEmpty) {
      if (stack.m_41619_()) {
         return stack;
      } else {
         Iterator var3 = slots.iterator();

         while(var3.hasNext()) {
            Slot slot = (Slot)var3.next();
            if (ignoreEmpty == slot.m_6657_()) {
               stack = ((IInsertableSlot)slot).insertItem(stack, false);
               if (stack.m_41619_()) {
                  break;
               }
            }
         }

         return stack;
      }
   }

   protected static boolean stillValid(Player player, BlockEntity blockEntity, BlockRegistryObject blockRO) {
      BlockPos pos = blockEntity.m_58899_();
      return player.m_9236_().m_8055_(pos).m_60734_() == blockRO.getBlock() && player.m_20275_((double)pos.m_123341_() + 0.5, (double)pos.m_123342_() + 0.5, (double)pos.m_123343_() + 0.5) <= 64.0;
   }

   public final void updateProgressBarLong(int idx, long data) {
      ((BoxedLong)this.longFields.get(idx)).set(data);
   }

   public final void updateProgressBarInt(int idx, int data) {
      ((DataSlot)this.intFields.get(idx)).m_6422_(data);
   }

   protected @NotNull DataSlot m_38895_(@NotNull DataSlot referenceHolder) {
      this.intFields.add(referenceHolder);
      return referenceHolder;
   }

   protected void broadcastPE(boolean all) {
      int i;
      for(i = 0; i < this.longFields.size(); ++i) {
         BoxedLong boxedLong = (BoxedLong)this.longFields.get(i);
         if (boxedLong.isDirty() || all) {
            this.syncDataChange(new UpdateWindowLongPKT((short)this.f_38840_, (short)i, boxedLong.get()));
         }
      }

      for(i = 0; i < this.intFields.size(); ++i) {
         DataSlot referenceHolder = (DataSlot)this.intFields.get(i);
         if (referenceHolder.m_39409_() || all) {
            this.syncDataChange(new UpdateWindowIntPKT((short)this.f_38840_, (short)i, referenceHolder.m_6501_()));
         }
      }

   }

   public void m_38946_() {
      super.m_38946_();
      this.broadcastPE(false);
   }

   public void m_150429_() {
      super.m_150429_();
      this.broadcastPE(true);
   }

   protected void syncDataChange(IPEPacket packet) {
      Player var3 = this.playerInv.f_35978_;
      if (var3 instanceof ServerPlayer player) {
         PacketHandler.sendTo(packet, player);
      }

   }

   public static class BoxedLong {
      private long inner;
      private boolean dirty = false;

      public long get() {
         return this.inner;
      }

      public void set(long v) {
         if (v != this.inner) {
            this.inner = v;
            this.dirty = true;
         }

      }

      public boolean isDirty() {
         boolean ret = this.dirty;
         this.dirty = false;
         return ret;
      }
   }
}
