package moze_intel.projecte.emc.nbt.processor;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.nbt.INBTProcessor;
import moze_intel.projecte.api.nbt.NBTProcessor;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NBTProcessor
public class ArmorTrimProcessor implements INBTProcessor {
   public String getName() {
      return "ArmorTrimProcessor";
   }

   public String getDescription() {
      return "Calculates EMC value of trimmed armor.";
   }

   public boolean hasPersistentNBT() {
      return true;
   }

   public long recalculateEMC(@NotNull ItemInfo info, long currentEMC) throws ArithmeticException {
      if (info.is(ItemTags.f_265942_)) {
         CompoundTag tag = info.getNBT();
         if (tag != null && tag.m_128425_("Trim", 10)) {
            CompoundTag compoundtag = tag.m_128469_("Trim");
            Object registryAccess;
            if (FMLEnvironment.dist.isClient()) {
               registryAccess = Minecraft.m_91087_().f_91073_ == null ? null : Minecraft.m_91087_().f_91073_.m_9598_();
            } else {
               registryAccess = ServerLifecycleHooks.getCurrentServer() == null ? null : ServerLifecycleHooks.getCurrentServer().m_206579_();
            }

            if (registryAccess == null) {
               return 0L;
            }

            ArmorTrim armortrim = (ArmorTrim)ArmorTrim.f_265985_.parse(RegistryOps.m_255058_(NbtOps.f_128958_, (HolderLookup.Provider)registryAccess), compoundtag).result().orElse((Object)null);
            if (armortrim != null) {
               Item material = (Item)((TrimMaterial)armortrim.m_266210_().m_203334_()).f_265970_().m_203334_();
               Item template = (Item)((TrimPattern)armortrim.m_266429_().m_203334_()).f_265847_().m_203334_();
               return Math.addExact(Math.addExact(currentEMC, EMCHelper.getEmcValue((ItemLike)material)), EMCHelper.getEmcValue((ItemLike)template));
            }
         }
      }

      return currentEMC;
   }

   public @Nullable CompoundTag getPersistentNBT(@NotNull ItemInfo info) {
      if (info.is(ItemTags.f_265942_)) {
         CompoundTag tag = info.getNBT();
         if (tag != null && tag.m_128425_("Trim", 10)) {
            CompoundTag toReturn = new CompoundTag();
            toReturn.m_128365_("Trim", tag.m_128469_("Trim"));
            return toReturn;
         }
      }

      return null;
   }
}
