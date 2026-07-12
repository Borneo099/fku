package moze_intel.projecte.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags.Blocks;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ItemHelper {
   public static InteractionResultHolder actionResultFromType(InteractionResult type, ItemStack stack) {
      InteractionResultHolder var10000;
      switch (type) {
         case SUCCESS:
            var10000 = InteractionResultHolder.m_19090_(stack);
            break;
         case CONSUME:
            var10000 = InteractionResultHolder.m_19096_(stack);
            break;
         case FAIL:
            var10000 = InteractionResultHolder.m_19100_(stack);
            break;
         default:
            var10000 = InteractionResultHolder.m_19098_(stack);
      }

      return var10000;
   }

   public static boolean checkItemNBT(ItemStack stack, String key) {
      return stack.m_41782_() && stack.m_41784_().m_128471_(key);
   }

   public static boolean compactInventory(IItemHandlerModifiable inventory) {
      List temp = new ArrayList();

      ItemStack s;
      for(int i = 0; i < inventory.getSlots(); ++i) {
         s = inventory.getStackInSlot(i);
         if (!s.m_41619_()) {
            temp.add(s);
            inventory.setStackInSlot(i, ItemStack.f_41583_);
         }
      }

      Iterator var4 = temp.iterator();

      while(var4.hasNext()) {
         s = (ItemStack)var4.next();
         ItemHandlerHelper.insertItemStacked(inventory, s, false);
      }

      return temp.isEmpty();
   }

   public static void compactItemListNoStacksize(List list) {
      for(int i = 0; i < list.size(); ++i) {
         ItemStack s = (ItemStack)list.get(i);
         if (!s.m_41619_()) {
            for(int j = i + 1; j < list.size(); ++j) {
               ItemStack s1 = (ItemStack)list.get(j);
               if (ItemHandlerHelper.canItemStacksStack(s, s1)) {
                  s.m_41769_(s1.m_41613_());
                  list.set(j, ItemStack.f_41583_);
               }
            }
         }
      }

      list.removeIf(ItemStack::m_41619_);
      list.sort(Comparators.ITEMSTACK_ASCENDING);
   }

   public static @Nullable CompoundTag copyNBTSkipKey(@NotNull CompoundTag nbt, @NotNull String keyToSkip) {
      CompoundTag copiedNBT = new CompoundTag();
      Iterator var3 = nbt.m_128431_().iterator();

      while(var3.hasNext()) {
         String key = (String)var3.next();
         if (!keyToSkip.equals(key)) {
            Tag innerNBT = nbt.m_128423_(key);
            if (innerNBT != null) {
               copiedNBT.m_128365_(key, innerNBT.m_6426_());
            }
         }
      }

      if (copiedNBT.m_128456_()) {
         return null;
      } else {
         return copiedNBT;
      }
   }

   public static ItemStack getNormalizedStack(ItemStack stack) {
      return stack.m_255036_(1);
   }

   public static IItemHandlerModifiable immutableCopy(IItemHandler toCopy) {
      final List list = new ArrayList(toCopy.getSlots());

      for(int i = 0; i < toCopy.getSlots(); ++i) {
         list.add(toCopy.getStackInSlot(i));
      }

      return new IItemHandlerModifiable() {
         public void setStackInSlot(int slot, @NotNull ItemStack stack) {
         }

         public int getSlots() {
            return list.size();
         }

         public @NotNull ItemStack getStackInSlot(int slot) {
            return (ItemStack)list.get(slot);
         }

         public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
         }

         public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.f_41583_;
         }

         public int getSlotLimit(int slot) {
            return this.getStackInSlot(slot).m_41741_();
         }

         public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return true;
         }
      };
   }

   public static boolean isOre(BlockState state) {
      return state.m_204336_(Blocks.ORES);
   }

   public static boolean isRepairableDamagedItem(ItemStack stack) {
      return stack.m_41763_() && stack.isRepairable() && stack.m_41773_() > 0;
   }

   public static int simulateFit(NonNullList inv, ItemStack stack) {
      int remainder = stack.m_41613_();
      Iterator var3 = inv.iterator();

      while(var3.hasNext()) {
         ItemStack invStack = (ItemStack)var3.next();
         if (invStack.m_41619_()) {
            return 0;
         }

         if (ItemHandlerHelper.canItemStacksStack(stack, invStack)) {
            int amountSlotNeeds = invStack.m_41741_() - invStack.m_41613_();
            if (amountSlotNeeds > 0) {
               if (remainder <= amountSlotNeeds) {
                  return 0;
               }

               remainder -= amountSlotNeeds;
            }
         }
      }

      return remainder;
   }

   public static @Nullable CompoundTag recombineNBT(List pieces) {
      if (pieces.isEmpty()) {
         return null;
      } else {
         CompoundTag combinedNBT = (CompoundTag)pieces.get(0);

         for(int i = 1; i < pieces.size(); ++i) {
            combinedNBT = combinedNBT.m_128391_((CompoundTag)pieces.get(i));
         }

         return combinedNBT;
      }
   }

   public static ItemStack size(ItemStack stack, int size) {
      return size > 0 && !stack.m_41619_() ? stack.m_255036_(size) : ItemStack.f_41583_;
   }

   public static BlockState stackToState(ItemStack stack, @Nullable BlockPlaceContext context) {
      Item var3 = stack.m_41720_();
      if (var3 instanceof BlockItem blockItem) {
         return context == null ? blockItem.m_40614_().m_49966_() : blockItem.m_40614_().m_5573_(context);
      } else {
         return null;
      }
   }
}
