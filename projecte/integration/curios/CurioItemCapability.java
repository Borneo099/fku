package moze_intel.projecte.integration.curios;

import moze_intel.projecte.capability.BasicItemCapability;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

public class CurioItemCapability extends BasicItemCapability implements ICurio {
   public Capability getCapability() {
      return CuriosCapability.ITEM;
   }

   public ItemStack getStack() {
      return super.getStack();
   }

   public void curioTick(SlotContext context) {
      if (!context.cosmetic()) {
         this.getStack().m_41666_(context.entity().m_9236_(), context.entity(), context.index(), false);
      }

   }
}
