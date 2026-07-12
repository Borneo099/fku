package moze_intel.projecte.emc.nbt.processor;

import java.util.Iterator;
import java.util.Map;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.nbt.INBTProcessor;
import moze_intel.projecte.api.nbt.NBTProcessor;
import moze_intel.projecte.utils.ItemInfoHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NBTProcessor
public class EnchantmentProcessor implements INBTProcessor {
   private static final long ENCH_EMC_BONUS = 5000L;

   public String getName() {
      return "EnchantmentProcessor";
   }

   public String getDescription() {
      return "Increases the EMC value to take into account any enchantments on an item.";
   }

   public boolean isAvailable() {
      return false;
   }

   public boolean hasPersistentNBT() {
      return true;
   }

   public boolean usePersistentNBT() {
      return false;
   }

   public long recalculateEMC(@NotNull ItemInfo info, long currentEMC) throws ArithmeticException {
      Map enchants = ItemInfoHelper.getEnchantments(info);
      Iterator var5 = enchants.entrySet().iterator();

      while(var5.hasNext()) {
         Map.Entry entry = (Map.Entry)var5.next();
         int rarityWeight = ((Enchantment)entry.getKey()).m_44699_().m_44716_();
         if (rarityWeight > 0) {
            currentEMC = Math.addExact(currentEMC, Math.multiplyExact(5000L / (long)rarityWeight, (Integer)entry.getValue()));
         }
      }

      return currentEMC;
   }

   public @Nullable CompoundTag getPersistentNBT(@NotNull ItemInfo info) {
      CompoundTag tag = info.getNBT();
      if (tag == null) {
         return null;
      } else {
         String location = ItemInfoHelper.getEnchantTagLocation(info);
         if (!tag.m_128425_(location, 9)) {
            return null;
         } else {
            CompoundTag toReturn = new CompoundTag();
            ListTag enchantments = tag.m_128437_(location, 10);
            toReturn.m_128365_(location, enchantments);
            return toReturn;
         }
      }
   }
}
