package moze_intel.projecte.gameObjs.container;

import moze_intel.projecte.gameObjs.container.slots.SlotGhost;
import moze_intel.projecte.gameObjs.container.slots.SlotPredicates;
import moze_intel.projecte.gameObjs.container.slots.ValidatedSlot;
import moze_intel.projecte.gameObjs.registries.PEContainerTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class MercurialEyeContainer extends PEHandContainer {
   private final SlotGhost mercurialTarget;

   public static MercurialEyeContainer fromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
      return new MercurialEyeContainer(windowId, playerInv, (InteractionHand)buf.m_130066_(InteractionHand.class), buf.readByte());
   }

   public MercurialEyeContainer(int windowId, Inventory playerInv, InteractionHand hand, int selected) {
      super(PEContainerTypes.MERCURIAL_EYE_CONTAINER, windowId, playerInv, hand, selected);
      IItemHandler handler = (IItemHandler)this.stack.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(NullPointerException::new);
      this.m_38897_(new ValidatedSlot(handler, 0, 50, 26, SlotPredicates.EMC_HOLDER));
      this.m_38897_(this.mercurialTarget = new SlotGhost(handler, 1, 104, 26, SlotPredicates.MERCURIAL_TARGET));
      this.addPlayerInventory(6, 56);
   }

   public void clickPostValidate(int slotId, int button, @NotNull ClickType flag, @NotNull Player player) {
      Slot slot = this.tryGetSlot(slotId);
      if (slot instanceof SlotGhost && !slot.m_7993_().m_41619_()) {
         slot.m_5852_(ItemStack.f_41583_);
      } else {
         super.clickPostValidate(slotId, button, flag, player);
      }

   }

   public @NotNull ItemStack m_7648_(@NotNull Player player, int slotID) {
      if (slotID > 1 && !this.mercurialTarget.m_6657_()) {
         Slot currentSlot = (Slot)this.f_38839_.get(slotID);
         if (currentSlot == null || !currentSlot.m_6657_()) {
            return ItemStack.f_41583_;
         }

         ItemStack slotStack = currentSlot.m_7993_();
         if (!slotStack.m_41619_() && this.mercurialTarget.isValid(slotStack)) {
            this.mercurialTarget.m_5852_(slotStack);
            return ItemStack.f_41583_;
         }
      }

      return super.m_7648_(player, slotID);
   }
}
