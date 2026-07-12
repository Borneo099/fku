package moze_intel.projecte.gameObjs.items;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public interface IStepAssister {
   boolean canAssistStep(ItemStack var1, ServerPlayer var2);
}
