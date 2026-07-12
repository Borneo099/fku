package moze_intel.projecte.gameObjs.container;

import moze_intel.projecte.gameObjs.container.slots.HotBarSlot;
import moze_intel.projecte.gameObjs.registration.impl.ContainerTypeRegistryObject;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PEHandContainer extends PEContainer {
   public final InteractionHand hand;
   private final int selected;
   protected final ItemStack stack;

   protected PEHandContainer(ContainerTypeRegistryObject typeRO, int windowId, Inventory playerInv, InteractionHand hand, int selected) {
      super(typeRO, windowId, playerInv);
      this.hand = hand;
      this.selected = selected;
      if (this.hand == null) {
         this.stack = ItemStack.f_41583_;
      } else {
         this.stack = this.hand == InteractionHand.OFF_HAND ? this.playerInv.f_35978_.m_21206_() : this.playerInv.m_8020_(selected);
      }

   }

   protected HotBarSlot createHotBarSlot(@NotNull Inventory inv, int index, int x, int y) {
      return this.hand == InteractionHand.MAIN_HAND && index == this.selected ? new HotBarSlot(inv, index, x, y) {
         public boolean m_8010_(@NotNull Player player) {
            return false;
         }
      } : super.createHotBarSlot(inv, index, x, y);
   }

   public boolean m_6875_(@NotNull Player player) {
      return this.hand == null || !this.stack.m_41619_() && player.m_21120_(this.hand).m_150930_(this.stack.m_41720_());
   }

   public void m_150399_(int slotId, int dragType, @NotNull ClickType clickType, @NotNull Player player) {
      if (clickType == ClickType.SWAP) {
         if (this.hand == InteractionHand.OFF_HAND && dragType == 40) {
            return;
         }

         if (this.hand == InteractionHand.MAIN_HAND && dragType >= 0 && dragType < Inventory.m_36059_() && !((HotBarSlot)this.hotBarSlots.get(dragType)).m_8010_(player)) {
            return;
         }
      }

      this.clickPostValidate(slotId, dragType, clickType, player);
   }

   public void clickPostValidate(int slotId, int dragType, @NotNull ClickType clickType, @NotNull Player player) {
      super.m_150399_(slotId, dragType, clickType, player);
   }
}
