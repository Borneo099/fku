package lexis.Hack.Utils;

import java.util.function.Predicate;
import java.util.stream.IntStream;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class InventoryUtils {
   private static final Minecraft mc = Minecraft.m_91087_();

   private InventoryUtils() {
   }

   public static int indexOf(Item item) {
      return indexOf((Predicate)((stack) -> {
         return stack.m_150930_(item);
      }), 36, false);
   }

   public static int indexOf(Item item, int maxInvSlot) {
      return indexOf((stack) -> {
         return stack.m_150930_(item);
      }, maxInvSlot, false);
   }

   public static int indexOf(Item item, int maxInvSlot, boolean includeOffhand) {
      return indexOf((stack) -> {
         return stack.m_150930_(item);
      }, maxInvSlot, includeOffhand);
   }

   public static int indexOf(Predicate predicate) {
      return indexOf((Predicate)predicate, 36, false);
   }

   public static int indexOf(Predicate predicate, int maxInvSlot) {
      return indexOf(predicate, maxInvSlot, false);
   }

   public static int indexOf(Predicate predicate, int maxInvSlot, boolean includeOffhand) {
      return getMatchingSlots(predicate, maxInvSlot, includeOffhand).findFirst().orElse(-1);
   }

   public static int count(Item item) {
      return count((Predicate)((stack) -> {
         return stack.m_150930_(item);
      }), 36, false);
   }

   public static int count(Item item, int maxInvSlot) {
      return count((stack) -> {
         return stack.m_150930_(item);
      }, maxInvSlot, false);
   }

   public static int count(Item item, int maxInvSlot, boolean includeOffhand) {
      return count((stack) -> {
         return stack.m_150930_(item);
      }, maxInvSlot, includeOffhand);
   }

   public static boolean hasItem(Item item) {
      return indexOf(item) != -1;
   }

   public static int count(Predicate predicate) {
      return count((Predicate)predicate, 36, false);
   }

   public static int count(Predicate predicate, int maxInvSlot) {
      return count(predicate, maxInvSlot, false);
   }

   public static int count(Predicate predicate, int maxInvSlot, boolean includeOffhand) {
      Inventory inv = mc.f_91074_.m_150109_();
      return getMatchingSlots(predicate, maxInvSlot, includeOffhand).map((slot) -> {
         return inv.m_8020_(slot).m_41613_();
      }).sum();
   }

   private static IntStream getMatchingSlots(Predicate predicate, int maxInvSlot, boolean includeOffhand) {
      Inventory inv = mc.f_91074_.m_150109_();
      IntStream stream = IntStream.range(0, maxInvSlot);
      if (includeOffhand) {
         stream = IntStream.concat(stream, IntStream.of(40));
      }

      return stream.filter((i) -> {
         return predicate.test(inv.m_8020_(i));
      });
   }

   public static boolean selectItem(Item item) {
      return selectItem((Predicate)((stack) -> {
         return stack.m_150930_(item);
      }), 36, false);
   }

   public static boolean selectItem(Item item, int maxInvSlot) {
      return selectItem((stack) -> {
         return stack.m_150930_(item);
      }, maxInvSlot, false);
   }

   public static boolean selectItem(Item item, int maxInvSlot, boolean takeFromOffhand) {
      return selectItem((stack) -> {
         return stack.m_150930_(item);
      }, maxInvSlot, takeFromOffhand);
   }

   public static boolean selectItem(Predicate predicate) {
      return selectItem((Predicate)predicate, 36, false);
   }

   public static boolean selectItem(Predicate predicate, int maxInvSlot) {
      return selectItem(predicate, maxInvSlot, false);
   }

   public static boolean selectItem(Predicate predicate, int maxInvSlot, boolean takeFromOffhand) {
      return selectItem(indexOf(predicate, maxInvSlot, takeFromOffhand));
   }

   public static boolean selectItem(int slot) {
      if (slot < 0) {
         return false;
      } else {
         Inventory inv = mc.f_91074_.m_150109_();
         if (slot < 9) {
            inv.f_35977_ = slot;
         } else if (inv.f_35977_ >= 0 && inv.f_35977_ < 9) {
            quickMove(slotToNetworkSlot(slot), inv.f_35977_);
         } else {
            swap(slotToNetworkSlot(slot), inv.f_35977_);
         }

         return true;
      }
   }

   public static int findItem(Item item) {
      for(int i = 0; i < 36; ++i) {
         if (mc.f_91074_.m_150109_().m_8020_(i).m_41720_() == item) {
            return i;
         }
      }

      return -1;
   }

   public static void switchToSlot(int slot) {
      if (slot >= 0 && slot < 9) {
         mc.f_91074_.m_150109_().f_35977_ = slot;
      } else if (slot >= 9 && slot < 36) {
         int targetSlot = mc.f_91074_.m_150109_().f_35977_;
         mc.f_91072_.m_171799_(mc.f_91074_.f_36096_.f_38840_, slot, targetSlot, ClickType.SWAP, mc.f_91074_);
      }

   }

   public static int slotToNetworkSlot(int slot) {
      if (slot >= 0 && slot < 9) {
         return slot + 36;
      } else if (slot >= 36 && slot < 40) {
         return 44 - slot;
      } else {
         return slot == 40 ? 45 : slot;
      }
   }

   public static void quickMove(int fromSlot, int toSlot) {
      if (mc.f_91072_ != null) {
         mc.f_91072_.m_171799_(mc.f_91074_.f_36096_.f_38840_, fromSlot, 0, ClickType.QUICK_MOVE, mc.f_91074_);
      }
   }

   public static void swap(int fromSlot, int toSlot) {
      if (mc.f_91072_ != null) {
         mc.f_91072_.m_171799_(mc.f_91074_.f_36096_.f_38840_, fromSlot, toSlot, ClickType.SWAP, mc.f_91074_);
      }
   }

   public static void setCreativeStack(int slot, ItemStack stack) {
      if (slot >= 0) {
         mc.f_91074_.m_150109_().m_6836_(slot, stack);
         mc.m_91403_().m_104955_(new ServerboundSetCreativeModeSlotPacket(slotToNetworkSlot(slot), stack));
      }
   }
}
