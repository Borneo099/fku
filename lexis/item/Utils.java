package lexis.item;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.item.ItemStack;

public class Utils {
   public static ItemStack fixGhostItem(ItemStack stack) {
      if (stack.m_41619_()) {
         return stack;
      } else {
         if (!stack.m_41782_()) {
            stack.m_41751_(new CompoundTag());
         }

         CompoundTag tag = stack.m_41783_();
         if (tag.m_128425_("Enchantments", 9)) {
            ListTag enchantments = tag.m_128437_("Enchantments", 10);
            if (enchantments.isEmpty()) {
               tag.m_128473_("Enchantments");
            }
         }

         if (stack.m_41613_() <= 0) {
            stack.m_41764_(1);
         }

         return stack;
      }
   }

   public static ItemStack setItemDurability(ItemStack stack, int value) {
      if (stack.m_41619_()) {
         return stack;
      } else {
         ItemStack result = stack.m_41777_();
         result.m_41721_(value);
         return result;
      }
   }

   public static ItemStack fixItemCount(ItemStack stack, int count) {
      if (stack.m_41619_()) {
         return stack;
      } else {
         int newCount = Math.max(1, Math.min(127, count));
         if (newCount == 0) {
            return ItemStack.f_41583_;
         } else {
            stack.m_41764_(newCount);
            return stack;
         }
      }
   }

   public static boolean addItem(ItemStack stack) {
      Minecraft mc = Minecraft.m_91087_();
      if (mc.f_91074_ != null && mc.m_91403_() != null) {
         ItemStack fixedStack = fixGhostItem(stack.m_41777_());
         fixedStack.m_41764_(1);
         int emptySlot = -1;

         int packetSlot;
         for(packetSlot = 0; packetSlot < 36; ++packetSlot) {
            if (mc.f_91074_.m_150109_().m_8020_(packetSlot).m_41619_()) {
               emptySlot = packetSlot;
               break;
            }
         }

         if (emptySlot != -1) {
            packetSlot = emptySlot < 9 ? emptySlot + 36 : emptySlot;
            mc.m_91403_().m_104955_(new ServerboundSetCreativeModeSlotPacket(packetSlot, fixedStack));
         }

         return false;
      } else {
         return false;
      }
   }

   public static void safeRemoveItem(int slot, ItemStack stack) {
      Minecraft mc = Minecraft.m_91087_();
      if (mc.f_91074_ != null && mc.m_91403_() != null) {
         mc.m_91403_().m_104955_(new ServerboundSetCreativeModeSlotPacket(slot, ItemStack.f_41583_));
         if (slot < 36) {
            mc.f_91074_.m_150109_().f_35974_.set(slot, ItemStack.f_41583_);
         } else if (slot == 45) {
            mc.f_91074_.m_150109_().f_35976_.set(0, ItemStack.f_41583_);
         }

      }
   }
}
