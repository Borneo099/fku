package moze_intel.projecte.utils;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import moze_intel.projecte.api.capabilities.item.IItemEmcHolder;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.emc.FuelMapper;
import moze_intel.projecte.emc.nbt.NBTManager;
import moze_intel.projecte.gameObjs.items.KleinStar;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public final class EMCHelper {
   public static long consumePlayerFuel(Player player, @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long minFuel) {
      if (!player.m_7500_() && minFuel != 0L) {
         IItemHandler curios = PlayerHelper.getCurios(player);
         long actualExtracted;
         if (curios != null) {
            for(int i = 0; i < curios.getSlots(); ++i) {
               actualExtracted = tryExtract(curios.getStackInSlot(i), minFuel);
               if (actualExtracted > 0L) {
                  player.f_36096_.m_38946_();
                  return actualExtracted;
               }
            }
         }

         ItemStack offhand = player.m_21206_();
         if (!offhand.m_41619_()) {
            actualExtracted = tryExtract(offhand, minFuel);
            if (actualExtracted > 0L) {
               player.f_36096_.m_38946_();
               return actualExtracted;
            }
         }

         Optional itemHandlerCap = player.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
         if (itemHandlerCap.isPresent()) {
            IItemHandler inv = (IItemHandler)itemHandlerCap.get();
            Map map = new LinkedHashMap();
            boolean metRequirement = false;
            long emcConsumed = 0L;

            for(int i = 0; i < inv.getSlots(); ++i) {
               ItemStack stack = inv.getStackInSlot(i);
               if (!stack.m_41619_()) {
                  long actualExtracted = tryExtract(stack, minFuel);
                  if (actualExtracted > 0L) {
                     player.f_36096_.m_38946_();
                     return actualExtracted;
                  }

                  if (!metRequirement && FuelMapper.isStackFuel(stack)) {
                     long emc = getEmcValue(stack);
                     int toRemove = (int)Math.ceil((double)(minFuel - emcConsumed) / (double)emc);
                     if (stack.m_41613_() >= toRemove) {
                        map.put(i, toRemove);
                        emcConsumed += emc * (long)toRemove;
                        metRequirement = true;
                     } else {
                        map.put(i, stack.m_41613_());
                        emcConsumed += emc * (long)stack.m_41613_();
                        if (emcConsumed >= minFuel) {
                           metRequirement = true;
                        }
                     }
                  }
               }
            }

            if (metRequirement) {
               Iterator var20 = map.entrySet().iterator();

               while(var20.hasNext()) {
                  Map.Entry entry = (Map.Entry)var20.next();
                  inv.extractItem((Integer)entry.getKey(), (Integer)entry.getValue(), false);
               }

               player.f_36096_.m_38946_();
               return emcConsumed;
            }
         }

         return -1L;
      } else {
         return minFuel;
      }
   }

   private static long tryExtract(@NotNull ItemStack stack, long minFuel) {
      if (stack.m_41619_()) {
         return 0L;
      } else {
         Optional holderCapability = stack.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).resolve();
         if (holderCapability.isPresent()) {
            IItemEmcHolder emcHolder = (IItemEmcHolder)holderCapability.get();
            long simulatedExtraction = emcHolder.extractEmc(stack, minFuel, IEmcStorage.EmcAction.SIMULATE);
            if (simulatedExtraction == minFuel) {
               return emcHolder.extractEmc(stack, simulatedExtraction, IEmcStorage.EmcAction.EXECUTE);
            }
         }

         return 0L;
      }
   }

   public static boolean doesItemHaveEmc(ItemInfo info) {
      return getEmcValue(info) > 0L;
   }

   public static boolean doesItemHaveEmc(ItemStack stack) {
      return getEmcValue(stack) > 0L;
   }

   public static boolean doesItemHaveEmc(ItemLike item) {
      return getEmcValue(item) > 0L;
   }

   public static @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long getEmcValue(ItemLike item) {
      return item == null ? 0L : getEmcValue(ItemInfo.fromItem(item));
   }

   public static @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long getEmcValue(ItemStack stack) {
      return stack.m_41619_() ? 0L : getEmcValue(ItemInfo.fromStack(stack));
   }

   public static @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long getEmcValue(ItemInfo info) {
      return NBTManager.getEmcValue(info);
   }

   public static @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long getEmcSellValue(ItemStack stack) {
      return stack.m_41619_() ? 0L : getEmcSellValue(ItemInfo.fromStack(stack));
   }

   public static @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long getEmcSellValue(ItemInfo info) {
      return getEmcSellValue(getEmcValue(info));
   }

   public static @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long getEmcSellValue(@Range(
   from = 0L,
   to = Long.MAX_VALUE
) long originalValue) {
      if (originalValue == 0L) {
         return 0L;
      } else {
         long emc = (long)Math.floor((double)originalValue * ProjectEConfig.server.difficulty.covalenceLoss.get());
         if (emc < 1L) {
            if (ProjectEConfig.server.difficulty.covalenceLossRounding.get()) {
               emc = 1L;
            } else {
               emc = 0L;
            }
         }

         return emc;
      }
   }

   public static Component getEmcTextComponent(long emc, int stackSize) {
      if (ProjectEConfig.server.difficulty.covalenceLoss.get() == 1.0) {
         String value;
         PELang prefix;
         if (stackSize > 1) {
            prefix = PELang.EMC_STACK_TOOLTIP;
            value = Constants.EMC_FORMATTER.format(BigInteger.valueOf(emc).multiply(BigInteger.valueOf((long)stackSize)));
         } else {
            prefix = PELang.EMC_TOOLTIP;
            value = Constants.EMC_FORMATTER.format(emc);
         }

         return prefix.translateColored(ChatFormatting.YELLOW, ChatFormatting.WHITE, value);
      } else {
         long emcSellValue = getEmcSellValue(emc);
         PELang prefix;
         String value;
         String sell;
         if (stackSize > 1) {
            prefix = PELang.EMC_STACK_TOOLTIP_WITH_SELL;
            BigInteger bigIntStack = BigInteger.valueOf((long)stackSize);
            value = Constants.EMC_FORMATTER.format(BigInteger.valueOf(emc).multiply(bigIntStack));
            sell = Constants.EMC_FORMATTER.format(BigInteger.valueOf(emcSellValue).multiply(bigIntStack));
         } else {
            prefix = PELang.EMC_TOOLTIP_WITH_SELL;
            value = Constants.EMC_FORMATTER.format(emc);
            sell = Constants.EMC_FORMATTER.format(emcSellValue);
         }

         return prefix.translateColored(ChatFormatting.YELLOW, ChatFormatting.WHITE, value, ChatFormatting.BLUE, sell);
      }
   }

   public static @Range(
   from = 1L,
   to = Long.MAX_VALUE
) long getKleinStarMaxEmc(ItemStack stack) {
      Item var2 = stack.m_41720_();
      if (var2 instanceof KleinStar star) {
         return Constants.MAX_KLEIN_EMC[star.tier.ordinal()];
      } else {
         return Constants.MAX_KLEIN_EMC[0];
      }
   }

   public static @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long getEMCPerDurability(ItemStack stack) {
      if (stack.m_41619_()) {
         return 0L;
      } else if (stack.m_41763_()) {
         ItemStack stackCopy = stack.m_41777_();
         stackCopy.m_41721_(0);
         long emc = (long)Math.ceil((double)getEmcValue(stackCopy) / (double)stack.m_41776_());
         return Math.max(emc, 1L);
      } else {
         return 1L;
      }
   }

   public static long removeFractionalEMC(ItemStack stack, double amount) {
      CompoundTag nbt = stack.m_41784_();
      double unprocessedEMC = nbt.m_128459_("UnprocessedEMC");
      unprocessedEMC += amount;
      long toRemove = (long)unprocessedEMC;
      unprocessedEMC -= (double)toRemove;
      nbt.m_128347_("UnprocessedEMC", unprocessedEMC);
      return toRemove;
   }
}
