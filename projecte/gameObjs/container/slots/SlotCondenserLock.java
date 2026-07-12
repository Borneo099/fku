package moze_intel.projecte.gameObjs.container.slots;

import java.util.function.Supplier;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SlotCondenserLock extends Slot {
   private static final Container emptyInventory = new SimpleContainer(0);
   private final Supplier lockInfo;

   public SlotCondenserLock(Supplier lockInfo, int index, int xPosition, int yPosition) {
      super(emptyInventory, index, xPosition, yPosition);
      this.lockInfo = lockInfo;
   }

   public boolean m_5857_(@NotNull ItemStack stack) {
      if (!stack.m_41619_() && SlotPredicates.HAS_EMC.test(stack)) {
         this.m_5852_(ItemHelper.getNormalizedStack(stack));
      }

      return false;
   }

   public boolean m_8010_(@NotNull Player player) {
      return false;
   }

   public int m_6641_() {
      return 1;
   }

   public @NotNull ItemStack m_7993_() {
      ItemInfo lockInfo = (ItemInfo)this.lockInfo.get();
      return lockInfo == null ? ItemStack.f_41583_ : lockInfo.createStack();
   }

   public void m_5852_(@NotNull ItemStack stack) {
   }

   public void m_40234_(@NotNull ItemStack oldStack, @NotNull ItemStack newStack) {
   }

   public @NotNull ItemStack m_6201_(int amount) {
      return this.m_7993_();
   }
}
