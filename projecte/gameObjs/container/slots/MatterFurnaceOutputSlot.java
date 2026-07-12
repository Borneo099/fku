package moze_intel.projecte.gameObjs.container.slots;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class MatterFurnaceOutputSlot extends InventoryContainerSlot {
   private final Player player;
   private int removeCount;

   public MatterFurnaceOutputSlot(Player player, IItemHandler itemHandler, int index, int xPosition, int yPosition) {
      super(itemHandler, index, xPosition, yPosition);
      this.player = player;
   }

   public boolean m_5857_(@NotNull ItemStack stack) {
      return false;
   }

   public @NotNull ItemStack m_6201_(int amount) {
      if (this.m_6657_()) {
         this.removeCount += Math.min(amount, this.m_7993_().m_41613_());
      }

      return super.m_6201_(amount);
   }

   public void m_142406_(@NotNull Player player, @NotNull ItemStack stack) {
      this.m_5845_(stack);
      super.m_142406_(player, stack);
   }

   protected void m_7169_(@NotNull ItemStack stack, int pAmount) {
      this.removeCount += pAmount;
      this.m_5845_(stack);
   }

   protected void m_5845_(ItemStack stack) {
      stack.m_41678_(this.player.m_9236_(), this.player, this.removeCount);
      this.removeCount = 0;
      ForgeEventFactory.firePlayerSmeltedEvent(this.player, stack);
   }
}
