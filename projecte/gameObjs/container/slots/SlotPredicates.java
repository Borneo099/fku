package moze_intel.projecte.gameObjs.container.slots;

import java.util.function.Predicate;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.emc.FuelMapper;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class SlotPredicates {
   public static final Predicate ALWAYS_FALSE = (input) -> {
      return false;
   };
   public static final Predicate HAS_EMC = (input) -> {
      return !input.m_41619_() && EMCHelper.doesItemHaveEmc(input);
   };
   public static final Predicate COLLECTOR_LOCK = (input) -> {
      return !input.m_41619_() && FuelMapper.isStackFuel(input);
   };
   public static final Predicate COLLECTOR_INV = (input) -> {
      return !input.m_41619_() && input.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).isPresent() || FuelMapper.isStackFuel(input) && !FuelMapper.isStackMaxFuel(input);
   };
   public static final Predicate EMC_HOLDER = (input) -> {
      return !input.m_41619_() && input.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).isPresent();
   };
   public static final Predicate RELAY_INV = (input) -> {
      return EMC_HOLDER.test(input) || HAS_EMC.test(input);
   };
   public static final Predicate FURNACE_FUEL = (input) -> {
      return EMC_HOLDER.test(input) || !input.m_41619_() && AbstractFurnaceBlockEntity.m_58399_(input);
   };
   public static final Predicate MERCURIAL_TARGET = (input) -> {
      if (input.m_41619_()) {
         return false;
      } else {
         BlockState state = ItemHelper.stackToState(input, (BlockPlaceContext)null);
         return state != null && !state.m_155947_() && EMCHelper.doesItemHaveEmc(input);
      }
   };

   private SlotPredicates() {
   }
}
