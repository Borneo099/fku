package moze_intel.projecte.gameObjs.items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import moze_intel.projecte.capability.ItemCapabilityWrapper;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Range;

public class ItemPE extends Item {
   private final List supportedCapabilities = new ArrayList();

   public ItemPE(Item.Properties props) {
      super(props);
   }

   protected void addItemCapability(Supplier capabilitySupplier) {
      this.supportedCapabilities.add(capabilitySupplier);
   }

   protected void addItemCapability(String modid, Supplier capabilitySupplier) {
      if (ModList.get().isLoaded(modid)) {
         this.supportedCapabilities.add((Supplier)capabilitySupplier.get());
      }

   }

   public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
      if (oldStack.m_41720_() != newStack.m_41720_()) {
         return true;
      } else if (oldStack.m_41782_() && newStack.m_41782_()) {
         CompoundTag newTag = newStack.m_41784_();
         CompoundTag oldTag = oldStack.m_41784_();
         boolean diffActive = oldTag.m_128441_("Active") && newTag.m_128441_("Active") && !oldTag.m_128423_("Active").equals(newTag.m_128423_("Active"));
         boolean diffMode = oldTag.m_128441_("Mode") && newTag.m_128441_("Mode") && !oldTag.m_128423_("Mode").equals(newTag.m_128423_("Mode"));
         return diffActive || diffMode;
      } else {
         return false;
      }
   }

   public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
      return (ICapabilityProvider)(this.supportedCapabilities.isEmpty() ? super.initCapabilities(stack, nbt) : new ItemCapabilityWrapper(stack, this.supportedCapabilities));
   }

   public static @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long getEmc(ItemStack stack) {
      return stack.m_41782_() ? stack.m_41783_().m_128454_("StoredEMC") : 0L;
   }

   public static void setEmc(ItemStack stack, @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long amount) {
      setEmc(stack.m_41784_(), amount);
   }

   public static void setEmc(CompoundTag nbt, @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long amount) {
      nbt.m_128356_("StoredEMC", amount);
   }

   public static void addEmcToStack(ItemStack stack, @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long amount) {
      if (amount > 0L) {
         setEmc(stack, getEmc(stack) + amount);
      }

   }

   public static void removeEmc(ItemStack stack, @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long amount) {
      if (amount > 0L) {
         setEmc(stack, Math.max(getEmc(stack) - amount, 0L));
      }

   }

   public static boolean consumeFuel(Player player, ItemStack stack, long amount, boolean shouldRemove) {
      if (amount <= 0L) {
         return true;
      } else {
         long current = getEmc(stack);
         if (current < amount) {
            long consume = EMCHelper.consumePlayerFuel(player, amount - current);
            if (consume == -1L) {
               return false;
            }

            addEmcToStack(stack, consume);
         }

         if (shouldRemove) {
            removeEmc(stack, amount);
         }

         return true;
      }
   }
}
