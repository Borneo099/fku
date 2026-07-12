package moze_intel.projecte.gameObjs.items;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongComparators;
import it.unimi.dsi.fastutil.longs.LongList;
import java.util.Iterator;
import java.util.List;
import moze_intel.projecte.capability.ModeChangerItemCapabilityWrapper;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.text.ILangEntry;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.NonNullLazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DiviningRod extends ItemPE implements IItemMode {
   private final ILangEntry[] modes;
   private final int maxModes;

   public DiviningRod(Item.Properties props, ILangEntry... modeDesc) {
      super(props);
      this.modes = modeDesc;
      this.maxModes = this.modes.length;
      this.addItemCapability(ModeChangerItemCapabilityWrapper::new);
   }

   public @NotNull InteractionResult m_6225_(UseOnContext ctx) {
      Player player = ctx.m_43723_();
      if (player == null) {
         return InteractionResult.FAIL;
      } else {
         Level level = ctx.m_43725_();
         if (level.f_46443_) {
            return InteractionResult.SUCCESS;
         } else {
            LongList emcValues = new LongArrayList();
            long totalEmc = 0L;
            int numBlocks = 0;
            int depth = this.getDepthFromMode(ctx.m_43722_());
            NonNullLazy furnaceRecipes = NonNullLazy.of(() -> {
               return level.m_7465_().m_44013_(RecipeType.f_44108_);
            });
            Iterator var10 = WorldHelper.getPositionsFromBox(WorldHelper.getDeepBox(ctx.m_8083_(), ctx.m_43719_(), depth)).iterator();

            while(true) {
               List drops;
               do {
                  BlockPos digPos;
                  do {
                     if (!var10.hasNext()) {
                        if (numBlocks == 0) {
                           return InteractionResult.FAIL;
                        }

                        player.m_213846_(PELang.DIVINING_AVG_EMC.translate(new Object[]{numBlocks, totalEmc / (long)numBlocks}));
                        if (this == PEItems.MEDIUM_DIVINING_ROD.get() || this == PEItems.HIGH_DIVINING_ROD.get()) {
                           long[] maxValues = new long[3];

                           int num;
                           for(num = 0; num < 3; ++num) {
                              maxValues[num] = 1L;
                           }

                           emcValues.sort(LongComparators.OPPOSITE_COMPARATOR);
                           num = Math.min(emcValues.size(), 3);

                           for(int i = 0; i < num; ++i) {
                              maxValues[i] = emcValues.getLong(i);
                           }

                           player.m_213846_(PELang.DIVINING_MAX_EMC.translate(new Object[]{maxValues[0]}));
                           if (this == PEItems.HIGH_DIVINING_ROD.get()) {
                              player.m_213846_(PELang.DIVINING_SECOND_MAX.translate(new Object[]{maxValues[1]}));
                              player.m_213846_(PELang.DIVINING_THIRD_MAX.translate(new Object[]{maxValues[2]}));
                           }
                        }

                        return InteractionResult.CONSUME;
                     }

                     digPos = (BlockPos)var10.next();
                  } while(level.m_46859_(digPos));

                  BlockState state = level.m_8055_(digPos);
                  drops = Block.m_49874_(state, (ServerLevel)level, digPos, WorldHelper.getBlockEntity(level, digPos), player, ctx.m_43722_());
               } while(drops.isEmpty());

               ItemStack blockStack = (ItemStack)drops.get(0);
               long blockEmc = EMCHelper.getEmcValue(blockStack);
               if (blockEmc == 0L) {
                  Iterator var17 = ((List)furnaceRecipes.get()).iterator();

                  while(var17.hasNext()) {
                     SmeltingRecipe furnaceRecipe = (SmeltingRecipe)var17.next();
                     if (((Ingredient)furnaceRecipe.m_7527_().get(0)).test(blockStack)) {
                        long currentValue = EMCHelper.getEmcValue(furnaceRecipe.m_8043_(level.m_9598_()));
                        if (currentValue != 0L) {
                           if (!emcValues.contains(currentValue)) {
                              emcValues.add(currentValue);
                           }

                           totalEmc += currentValue;
                           break;
                        }
                     }
                  }
               } else {
                  if (!emcValues.contains(blockEmc)) {
                     emcValues.add(blockEmc);
                  }

                  totalEmc += blockEmc;
               }

               ++numBlocks;
            }
         }
      }
   }

   private int getDepthFromMode(ItemStack stack) {
      byte mode = this.getMode(stack);
      if (mode >= 0 && mode < this.maxModes) {
         if (mode == 0) {
            return 3;
         } else {
            return mode == 1 ? 16 : 64;
         }
      } else {
         return 0;
      }
   }

   public ILangEntry[] getModeLangEntries() {
      return this.modes;
   }

   public void m_7373_(@NotNull ItemStack stack, @Nullable Level level, @NotNull List tooltips, @NotNull TooltipFlag flags) {
      super.m_7373_(stack, level, tooltips, flags);
      tooltips.add(this.getToolTip(stack));
   }
}
