package moze_intel.projecte.gameObjs.items.rings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import moze_intel.projecte.api.block_entity.IDMPedestal;
import moze_intel.projecte.api.capabilities.item.IItemCharge;
import moze_intel.projecte.api.capabilities.item.IPedestalItem;
import moze_intel.projecte.capability.ChargeItemCapabilityWrapper;
import moze_intel.projecte.capability.PedestalItemCapabilityWrapper;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.PETags;
import moze_intel.projecte.gameObjs.items.IBarHelper;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.text.ILangEntry;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.IPlantable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TimeWatch extends PEToggleItem implements IPedestalItem, IItemCharge, IBarHelper {
   public TimeWatch(Item.Properties props) {
      super(props);
      this.addItemCapability(PedestalItemCapabilityWrapper::new);
      this.addItemCapability(ChargeItemCapabilityWrapper::new);
   }

   public @NotNull InteractionResultHolder m_7203_(Level level, Player player, @NotNull InteractionHand hand) {
      ItemStack stack = player.m_21120_(hand);
      if (!level.f_46443_) {
         if (!ProjectEConfig.server.items.enableTimeWatch.get()) {
            player.m_213846_(PELang.TIME_WATCH_DISABLED.translate(new Object[0]));
            return InteractionResultHolder.m_19100_(stack);
         }

         byte current = this.getTimeBoost(stack);
         this.setTimeBoost(stack, (byte)(current == 2 ? 0 : current + 1));
         player.m_213846_(PELang.TIME_WATCH_MODE_SWITCH.translate(new Object[]{this.getTimeName(stack)}));
      }

      return InteractionResultHolder.m_19090_(stack);
   }

   public void m_6883_(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int invSlot, boolean isHeld) {
      super.m_6883_(stack, level, entity, invSlot, isHeld);
      if (entity instanceof Player player) {
         if (invSlot < Inventory.m_36059_() && ProjectEConfig.server.items.enableTimeWatch.get()) {
            byte timeControl = this.getTimeBoost(stack);
            if (!level.f_46443_ && level.m_46469_().m_46207_(GameRules.f_46140_)) {
               ServerLevel serverWorld = (ServerLevel)level;
               if (timeControl == 1) {
                  serverWorld.m_8615_(Math.min(level.m_46468_() + (long)(this.getCharge(stack) + 1) * 4L, Long.MAX_VALUE));
               } else if (timeControl == 2) {
                  long charge = (long)(this.getCharge(stack) + 1);
                  if (level.m_46468_() - charge * 4L < 0L) {
                     serverWorld.m_8615_(0L);
                  } else {
                     serverWorld.m_8615_(level.m_46468_() - charge * 4L);
                  }
               }
            }

            if (!level.f_46443_ && ItemHelper.checkItemNBT(stack, "Active")) {
               long reqEmc = EMCHelper.removeFractionalEMC(stack, this.getEmcPerTick(this.getCharge(stack)));
               if (!consumeFuel(player, stack, reqEmc, true)) {
                  return;
               }

               int charge = this.getCharge(stack);
               byte bonusTicks;
               float mobSlowdown;
               if (charge == 0) {
                  bonusTicks = 8;
                  mobSlowdown = 0.25F;
               } else if (charge == 1) {
                  bonusTicks = 12;
                  mobSlowdown = 0.16F;
               } else {
                  bonusTicks = 16;
                  mobSlowdown = 0.12F;
               }

               AABB bBox = player.m_20191_().m_82400_(8.0);
               this.speedUpBlockEntities(level, bonusTicks, bBox);
               this.speedUpRandomTicks(level, bonusTicks, bBox);
               this.slowMobs(level, bBox, (double)mobSlowdown);
               return;
            }

            return;
         }
      }

   }

   private void slowMobs(Level level, AABB bBox, double mobSlowdown) {
      if (bBox != null) {
         Iterator var5 = level.m_45976_(Mob.class, bBox).iterator();

         while(var5.hasNext()) {
            Mob ent = (Mob)var5.next();
            ent.m_20256_(ent.m_20184_().m_82542_(mobSlowdown, 1.0, mobSlowdown));
         }

      }
   }

   private void speedUpBlockEntities(Level level, int bonusTicks, AABB bBox) {
      if (bBox != null && bonusTicks != 0) {
         Iterator var4 = WorldHelper.getBlockEntitiesWithinAABB(level, bBox).iterator();

         while(true) {
            BlockEntity blockEntity;
            BlockPos pos;
            LevelChunk chunk;
            LevelChunk.RebindableTickingBlockEntityWrapper tickingWrapper;
            LevelChunk.BoundTickingBlockEntity tickingBE;
            do {
               while(true) {
                  do {
                     do {
                        do {
                           do {
                              do {
                                 if (!var4.hasNext()) {
                                    return;
                                 }

                                 blockEntity = (BlockEntity)var4.next();
                              } while(blockEntity.m_58901_());
                           } while(PETags.BlockEntities.BLACKLIST_TIME_WATCH_LOOKUP.contains(blockEntity.m_58903_()));

                           pos = blockEntity.m_58899_();
                        } while(!level.m_183438_(ChunkPos.m_151388_(pos)));

                        chunk = level.m_46745_(pos);
                        tickingWrapper = (LevelChunk.RebindableTickingBlockEntityWrapper)chunk.f_156362_.get(pos);
                     } while(tickingWrapper == null);
                  } while(tickingWrapper.m_142220_());

                  TickingBlockEntity var10 = tickingWrapper.f_156444_;
                  if (var10 instanceof LevelChunk.BoundTickingBlockEntity) {
                     tickingBE = (LevelChunk.BoundTickingBlockEntity)var10;
                     break;
                  }

                  for(int i = 0; i < bonusTicks; ++i) {
                     tickingWrapper.m_142224_();
                  }
               }
            } while(!chunk.m_156410_(pos));

            ProfilerFiller profiler = level.m_46473_();
            Objects.requireNonNull(tickingWrapper);
            profiler.m_6521_(tickingWrapper::m_142280_);
            BlockState state = chunk.m_8055_(pos);
            if (blockEntity.m_58903_().m_155262_(state)) {
               for(int i = 0; i < bonusTicks; ++i) {
                  tickingBE.f_156429_.m_155252_(level, pos, state, blockEntity);
               }
            }

            profiler.m_7238_();
         }
      }
   }

   private void speedUpRandomTicks(Level level, int bonusTicks, AABB bBox) {
      if (bBox != null && bonusTicks != 0 && level instanceof ServerLevel serverLevel) {
         Iterator var5 = WorldHelper.getPositionsFromBox(bBox).iterator();

         while(true) {
            BlockPos pos;
            BlockState state;
            Block block;
            do {
               do {
                  do {
                     do {
                        do {
                           do {
                              if (!var5.hasNext()) {
                                 return;
                              }

                              pos = (BlockPos)var5.next();
                           } while(!WorldHelper.isBlockLoaded(serverLevel, pos));

                           state = serverLevel.m_8055_(pos);
                           block = state.m_60734_();
                        } while(!state.m_60823_());
                     } while(state.m_204336_(PETags.Blocks.BLACKLIST_TIME_WATCH));
                  } while(block instanceof LiquidBlock);
               } while(block instanceof BonemealableBlock);
            } while(block instanceof IPlantable);

            pos = pos.m_7949_();

            for(int i = 0; i < bonusTicks; ++i) {
               state.m_222972_(serverLevel, pos, serverLevel.f_46441_);
            }
         }
      }
   }

   private ILangEntry getTimeName(ItemStack stack) {
      byte mode = this.getTimeBoost(stack);
      PELang var10000;
      switch (mode) {
         case 0:
            var10000 = PELang.TIME_WATCH_OFF;
            break;
         case 1:
            var10000 = PELang.TIME_WATCH_FAST_FORWARD;
            break;
         case 2:
            var10000 = PELang.TIME_WATCH_REWIND;
            break;
         default:
            var10000 = PELang.INVALID_MODE;
      }

      return var10000;
   }

   private byte getTimeBoost(ItemStack stack) {
      return stack.m_41782_() ? stack.m_41784_().m_128445_("TimeMode") : 0;
   }

   private void setTimeBoost(ItemStack stack, byte time) {
      stack.m_41784_().m_128344_("TimeMode", (byte)Mth.m_14045_(time, 0, 2));
   }

   public double getEmcPerTick(int charge) {
      return (double)(charge + 2) / 2.0;
   }

   public void m_7373_(@NotNull ItemStack stack, @Nullable Level level, @NotNull List tooltips, @NotNull TooltipFlag flags) {
      super.m_7373_(stack, level, tooltips, flags);
      tooltips.add(PELang.TOOLTIP_TIME_WATCH_1.translate(new Object[0]));
      tooltips.add(PELang.TOOLTIP_TIME_WATCH_2.translate(new Object[0]));
      if (stack.m_41782_()) {
         tooltips.add(PELang.TIME_WATCH_MODE.translate(new Object[]{this.getTimeName(stack)}));
      }

   }

   public boolean updateInPedestal(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockEntity pedestal) {
      if (!level.f_46443_ && ProjectEConfig.server.items.enableTimeWatch.get()) {
         AABB bBox = ((IDMPedestal)pedestal).getEffectBounds();
         if (ProjectEConfig.server.effects.timePedBonus.get() > 0) {
            this.speedUpBlockEntities(level, ProjectEConfig.server.effects.timePedBonus.get(), bBox);
            this.speedUpRandomTicks(level, ProjectEConfig.server.effects.timePedBonus.get(), bBox);
         }

         if (ProjectEConfig.server.effects.timePedMobSlowness.get() < 1.0) {
            this.slowMobs(level, bBox, ProjectEConfig.server.effects.timePedMobSlowness.get());
         }
      }

      return false;
   }

   public @NotNull List getPedestalDescription() {
      List list = new ArrayList();
      if (ProjectEConfig.server.effects.timePedBonus.get() > 0) {
         list.add(PELang.PEDESTAL_TIME_WATCH_1.translateColored(ChatFormatting.BLUE, new Object[]{ProjectEConfig.server.effects.timePedBonus.get()}));
      }

      if (ProjectEConfig.server.effects.timePedMobSlowness.get() < 1.0) {
         list.add(PELang.PEDESTAL_TIME_WATCH_2.translateColored(ChatFormatting.BLUE, new Object[]{String.format("%.3f", ProjectEConfig.server.effects.timePedMobSlowness.get())}));
      }

      return list;
   }

   public int getNumCharges(@NotNull ItemStack stack) {
      return 2;
   }

   public boolean m_142522_(@NotNull ItemStack stack) {
      return true;
   }

   public float getWidthForBar(ItemStack stack) {
      return 1.0F - this.getChargePercent(stack);
   }

   public int m_142158_(@NotNull ItemStack stack) {
      return this.getScaledBarWidth(stack);
   }

   public int m_142159_(@NotNull ItemStack stack) {
      return this.getColorForBar(stack);
   }
}
