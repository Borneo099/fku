package moze_intel.projecte.capability;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public abstract class ItemCapability {
   private ItemCapabilityWrapper wrapper;

   public void setWrapper(ItemCapabilityWrapper wrapper) {
      if (this.wrapper == null) {
         this.wrapper = wrapper;
      }

   }

   public abstract Capability getCapability();

   public abstract LazyOptional getLazyCapability();

   protected ItemStack getStack() {
      return this.wrapper.getItemStack();
   }

   protected Object getItem() {
      return this.getStack().m_41720_();
   }
}
