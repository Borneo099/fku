package moze_intel.projecte.emc.nbt.processor;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.nbt.INBTProcessor;
import moze_intel.projecte.api.nbt.NBTProcessor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@NBTProcessor(
   priority = Integer.MAX_VALUE
)
public class DamageProcessor implements INBTProcessor {
   public String getName() {
      return "DamageProcessor";
   }

   public String getDescription() {
      return "Reduces the EMC value the more damaged an item is.";
   }

   public long recalculateEMC(@NotNull ItemInfo info, long currentEMC) throws ArithmeticException {
      Item item = info.getItem();
      if (item.m_41465_()) {
         ItemStack fakeStack = info.createStack();
         int maxDamage = item.getMaxDamage(fakeStack);
         int damage = item.getDamage(fakeStack);
         if (damage > maxDamage) {
            throw new ArithmeticException();
         }

         currentEMC = Math.multiplyExact(currentEMC, Math.addExact(maxDamage - damage, 1)) / (long)maxDamage;
      }

      return currentEMC;
   }
}
