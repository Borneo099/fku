package moze_intel.projecte.gameObjs.container;

import moze_intel.projecte.gameObjs.container.inventory.EternalDensityInventory;
import moze_intel.projecte.gameObjs.container.slots.SlotGhost;
import moze_intel.projecte.gameObjs.container.slots.SlotPredicates;
import moze_intel.projecte.gameObjs.registries.PEContainerTypes;
import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

public class EternalDensityContainer extends PEHandContainer {
   public final EternalDensityInventory inventory;

   public static EternalDensityContainer fromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf data) {
      return new EternalDensityContainer(windowId, playerInv, (InteractionHand)data.m_130066_(InteractionHand.class), data.readByte(), (EternalDensityInventory)null);
   }

   public EternalDensityContainer(int windowId, Inventory playerInv, InteractionHand hand, int selected, EternalDensityInventory gemInv) {
      super(PEContainerTypes.ETERNAL_DENSITY_CONTAINER, windowId, playerInv, hand, selected);
      this.inventory = gemInv == null ? new EternalDensityInventory(this.stack) : gemInv;

      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 3; ++j) {
            this.m_38897_(new SlotGhost(this.inventory, j + i * 3, 62 + j * 18, 26 + i * 18, SlotPredicates.HAS_EMC));
         }
      }

      this.addPlayerInventory(8, 93);
   }

   public @NotNull ItemStack m_7648_(@NotNull Player player, int slotIndex) {
      if (slotIndex > 8) {
         Slot slot = this.tryGetSlot(slotIndex);
         if (slot != null) {
            ItemHandlerHelper.insertItem(this.inventory, ItemHelper.getNormalizedStack(slot.m_7993_()), false);
         }
      }

      return ItemStack.f_41583_;
   }

   public void clickPostValidate(int slotIndex, int button, @NotNull ClickType flag, @NotNull Player player) {
      Slot slot = this.tryGetSlot(slotIndex);
      if (slot instanceof SlotGhost && !slot.m_7993_().m_41619_()) {
         slot.m_5852_(ItemStack.f_41583_);
      } else {
         super.clickPostValidate(slotIndex, button, flag, player);
      }

   }

   public boolean m_5622_(@NotNull Slot slot) {
      return false;
   }
}
