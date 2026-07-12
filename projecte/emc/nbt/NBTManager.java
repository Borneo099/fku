package moze_intel.projecte.emc.nbt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.nbt.INBTProcessor;
import moze_intel.projecte.config.NBTProcessorConfig;
import moze_intel.projecte.emc.EMCMappingHandler;
import moze_intel.projecte.gameObjs.PETags;
import moze_intel.projecte.utils.AnnotationHelper;
import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public class NBTManager {
   private static final List processors = new ArrayList();

   public static void loadProcessors() {
      if (processors.isEmpty()) {
         processors.addAll(AnnotationHelper.getNBTProcessors());
         NBTProcessorConfig.setup(processors);
      }

   }

   public static @NotNull ItemInfo getPersistentInfo(@NotNull ItemInfo info) {
      if (info.hasNBT() && !info.is(PETags.Items.NBT_WHITELIST) && !EMCMappingHandler.hasEmcValue(info)) {
         List persistentNBT = new ArrayList();
         Iterator var2 = processors.iterator();

         while(var2.hasNext()) {
            INBTProcessor processor = (INBTProcessor)var2.next();
            if (NBTProcessorConfig.isEnabled(processor) && processor.hasPersistentNBT() && NBTProcessorConfig.hasPersistent(processor)) {
               CompoundTag nbt = processor.getPersistentNBT(info);
               if (nbt != null) {
                  persistentNBT.add(nbt);
               }
            }
         }

         return ItemInfo.fromItem(info.getItem(), ItemHelper.recombineNBT(persistentNBT));
      } else {
         return info;
      }
   }

   public static @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long getEmcValue(@NotNull ItemInfo info) {
      long emcValue = EMCMappingHandler.getStoredEmcValue(info);
      if (!info.hasNBT()) {
         return emcValue;
      } else {
         if (emcValue == 0L) {
            emcValue = EMCMappingHandler.getStoredEmcValue(ItemInfo.fromItem(info.getItem()));
            if (emcValue == 0L) {
               return 0L;
            }
         }

         Iterator var3 = processors.iterator();

         while(var3.hasNext()) {
            INBTProcessor processor = (INBTProcessor)var3.next();
            if (NBTProcessorConfig.isEnabled(processor)) {
               try {
                  emcValue = processor.recalculateEMC(info, emcValue);
               } catch (ArithmeticException var6) {
                  return emcValue;
               }

               if (emcValue <= 0L) {
                  return 0L;
               }
            }
         }

         return emcValue;
      }
   }
}
