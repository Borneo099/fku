package moze_intel.projecte.emc.nbt.processor;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.nbt.INBTProcessor;
import moze_intel.projecte.api.nbt.NBTProcessor;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity.Decorations;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NBTProcessor
public class DecoratedPotProcessor implements INBTProcessor {
   public String getName() {
      return "DecoratedPotProcessor";
   }

   public String getDescription() {
      return "Takes the different sherds into account for each decorated pot.";
   }

   public boolean hasPersistentNBT() {
      return true;
   }

   public long recalculateEMC(@NotNull ItemInfo info, long currentEMC) throws ArithmeticException {
      if (info.getItem() == Items.f_271478_) {
         CompoundTag tag = info.getNBT();
         if (tag != null && tag.m_128425_("BlockEntityTag", 10)) {
            CompoundTag beTag = tag.m_128469_("BlockEntityTag");
            DecoratedPotBlockEntity.Decorations decorations = Decorations.m_284207_(beTag);
            if (!decorations.equals(Decorations.f_283770_)) {
               long decorationEmc = decorations.m_284195_().mapToLong(EMCHelper::getEmcValue).reduce(0L, Math::addExact);
               return Math.addExact(currentEMC - EMCHelper.getEmcValue((ItemLike)Items.f_271478_), decorationEmc);
            }
         }
      }

      return currentEMC;
   }

   public @Nullable CompoundTag getPersistentNBT(@NotNull ItemInfo info) {
      if (info.getItem() == Items.f_271478_) {
         CompoundTag tag = info.getNBT();
         if (tag != null && tag.m_128425_("BlockEntityTag", 10)) {
            CompoundTag beTag = tag.m_128469_("BlockEntityTag");
            if (beTag.m_128425_("sherds", 9)) {
               CompoundTag toReturnIntermediary = new CompoundTag();
               toReturnIntermediary.m_128365_("sherds", beTag.m_128437_("sherds", 8));
               CompoundTag toReturn = new CompoundTag();
               toReturn.m_128365_("BlockEntityTag", toReturnIntermediary);
               return toReturn;
            }
         }
      }

      return null;
   }
}
