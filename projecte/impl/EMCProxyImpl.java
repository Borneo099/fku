package moze_intel.projecte.impl;

import java.util.Objects;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.emc.nbt.NBTManager;
import moze_intel.projecte.utils.EMCHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public class EMCProxyImpl implements IEMCProxy {
   public @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long getValue(@NotNull ItemInfo info) {
      return EMCHelper.getEmcValue((ItemInfo)Objects.requireNonNull(info));
   }

   public @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long getSellValue(@NotNull ItemInfo info) {
      return EMCHelper.getEmcSellValue((ItemInfo)Objects.requireNonNull(info));
   }

   public @NotNull ItemInfo getPersistentInfo(@NotNull ItemInfo info) {
      return NBTManager.getPersistentInfo(info);
   }
}
