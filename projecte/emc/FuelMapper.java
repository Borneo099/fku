package moze_intel.projecte.emc;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.PETags;
import moze_intel.projecte.network.packets.to_client.SyncFuelMapperPKT;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class FuelMapper {
   private static List FUEL_MAP = Collections.emptyList();

   public static void loadMap() {
      FUEL_MAP = PETags.Items.COLLECTOR_FUEL_LOOKUP.tag().stream().filter(EMCHelper::doesItemHaveEmc).sorted(Comparator.comparing(EMCHelper::getEmcValue)).toList();
   }

   public static void setFuelMap(List map) {
      FUEL_MAP = List.copyOf(map);
   }

   public static SyncFuelMapperPKT getSyncPacket() {
      return new SyncFuelMapperPKT(FUEL_MAP);
   }

   public static boolean isStackFuel(ItemStack stack) {
      return FUEL_MAP.contains(stack.m_41720_());
   }

   public static boolean isStackMaxFuel(ItemStack stack) {
      return FUEL_MAP.indexOf(stack.m_41720_()) == FUEL_MAP.size() - 1;
   }

   public static ItemStack getFuelUpgrade(ItemStack stack) {
      int index = FUEL_MAP.indexOf(stack.m_41720_());
      if (index == -1) {
         PECore.LOGGER.warn("Tried to upgrade invalid fuel: {}", stack);
         return ItemStack.f_41583_;
      } else {
         int nextIndex = index == FUEL_MAP.size() - 1 ? 0 : index + 1;
         return new ItemStack((ItemLike)FUEL_MAP.get(nextIndex));
      }
   }

   public static List getFuelMap() {
      return FUEL_MAP;
   }
}
