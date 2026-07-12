package moze_intel.projecte.gameObjs.items.rings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import moze_intel.projecte.api.block_entity.IDMPedestal;
import moze_intel.projecte.api.capabilities.item.IPedestalItem;
import moze_intel.projecte.capability.PedestalItemCapabilityWrapper;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.MathUtils;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;
import org.jetbrains.annotations.NotNull;

public class HarvestGoddess extends PEToggleItem implements IPedestalItem {
   public HarvestGoddess(Item.Properties props) {
      super(props);
      this.addItemCapability(PedestalItemCapabilityWrapper::new);
   }

   public void m_6883_(@NotNull ItemStack stack, Level level, @NotNull Entity entity, int slot, boolean held) {
      if (!level.f_46443_ && slot < Inventory.m_36059_() && entity instanceof Player player) {
         super.m_6883_(stack, level, entity, slot, held);
         CompoundTag nbt = stack.m_41784_();
         if (nbt.m_128471_("Active")) {
            long storedEmc = getEmc(stack);
            if (storedEmc == 0L && !consumeFuel(player, stack, 64L, true)) {
               nbt.m_128379_("Active", false);
            } else {
               WorldHelper.growNearbyRandomly(true, level, player.m_20183_(), player);
               removeEmc(stack, EMCHelper.removeFractionalEMC(stack, 0.3199999928474426));
            }
         } else {
            WorldHelper.growNearbyRandomly(false, level, player.m_20183_(), player);
         }

      }
   }

   public @NotNull InteractionResult m_6225_(UseOnContext ctx) {
      Level level = ctx.m_43725_();
      Player player = ctx.m_43723_();
      BlockPos pos = ctx.m_8083_();
      Direction side = ctx.m_43719_();
      if (!level.f_46443_ && player != null && player.m_36204_(pos, side, ctx.m_43722_())) {
         if (player.m_36341_()) {
            for(int i = 0; i < player.m_150109_().f_35974_.size(); ++i) {
               ItemStack stack = (ItemStack)player.m_150109_().f_35974_.get(i);
               if (!stack.m_41619_() && stack.m_41613_() >= 4 && stack.m_41720_() == Items.f_42499_) {
                  if (this.useBoneMeal(level, pos, side)) {
                     player.m_150109_().m_7407_(i, 4);
                     player.f_36095_.m_38946_();
                     return InteractionResult.CONSUME;
                  }
                  break;
               }
            }
         } else if (this.plantSeeds(level, player, pos)) {
            return InteractionResult.CONSUME;
         }

         return InteractionResult.FAIL;
      } else {
         return InteractionResult.FAIL;
      }
   }

   private boolean useBoneMeal(Level level, BlockPos pos, Direction side) {
      if (!(level instanceof ServerLevel serverLevel)) {
         return false;
      } else {
         boolean result = false;
         Iterator var6 = BlockPos.m_121940_(pos.m_7918_(-15, 0, -15), pos.m_7918_(15, 0, 15)).iterator();

         while(true) {
            while(var6.hasNext()) {
               BlockPos currentPos = (BlockPos)var6.next();
               currentPos = currentPos.m_7949_();
               BlockState state = serverLevel.m_8055_(currentPos);
               Block var10 = state.m_60734_();
               if (var10 instanceof BonemealableBlock growable) {
                  if (growable.m_7370_(serverLevel, currentPos, state, false) && growable.m_214167_(serverLevel, serverLevel.f_46441_, currentPos, state)) {
                     growable.m_214148_(serverLevel, serverLevel.f_46441_, currentPos, state);
                     level.m_46796_(1505, currentPos, 0);
                     result = true;
                     continue;
                  }
               }

               if (WorldHelper.growWaterPlant(serverLevel, currentPos, state, side)) {
                  level.m_46796_(1505, currentPos, 0);
                  result = true;
               }
            }

            return result;
         }
      }
   }

   private boolean plantSeeds(Level level, Player player, BlockPos pos) {
      List seeds = this.getAllSeeds(player.m_150109_().f_35974_);
      if (seeds.isEmpty()) {
         return false;
      } else {
         boolean result = false;
         Iterator var6 = BlockPos.m_121940_(pos.m_7918_(-8, 0, -8), pos.m_7918_(8, 0, 8)).iterator();

         while(true) {
            while(true) {
               BlockPos currentPos;
               do {
                  if (!var6.hasNext()) {
                     return result;
                  }

                  currentPos = (BlockPos)var6.next();
               } while(level.m_46859_(currentPos));

               BlockState state = level.m_8055_(currentPos);
               currentPos = currentPos.m_7949_();

               for(int i = 0; i < seeds.size(); ++i) {
                  StackWithSlot s = (StackWithSlot)seeds.get(i);
                  if (state.canSustainPlant(level, currentPos, Direction.UP, s.plantable) && level.m_46859_(currentPos.m_7494_())) {
                     level.m_46597_(currentPos.m_7494_(), s.plantable.getPlant(level, currentPos.m_7494_()));
                     player.m_150109_().m_7407_(s.slot, 1);
                     player.f_36095_.m_38946_();
                     --s.count;
                     if (s.count == 0) {
                        seeds.remove(i);
                        if (seeds.isEmpty()) {
                           return true;
                        }
                     }

                     if (!result) {
                        result = true;
                     }
                     break;
                  }
               }
            }
         }
      }
   }

   private List getAllSeeds(NonNullList inv) {
      List result = new ArrayList();

      for(int i = 0; i < inv.size(); ++i) {
         ItemStack stack = (ItemStack)inv.get(i);
         if (!stack.m_41619_()) {
            Item item = stack.m_41720_();
            if (item instanceof IPlantable) {
               result.add(new StackWithSlot(stack, i, (IPlantable)item));
            } else {
               Block block = Block.m_49814_(item);
               if (block instanceof IPlantable) {
                  result.add(new StackWithSlot(stack, i, (IPlantable)block));
               }
            }
         }
      }

      return result;
   }

   public boolean updateInPedestal(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockEntity pedestal) {
      if (!level.f_46443_ && ProjectEConfig.server.cooldown.pedestal.harvest.get() != -1) {
         if (((IDMPedestal)pedestal).getActivityCooldown() == 0) {
            WorldHelper.growNearbyRandomly(true, level, pos, (Player)null);
            ((IDMPedestal)pedestal).setActivityCooldown(ProjectEConfig.server.cooldown.pedestal.harvest.get());
         } else {
            ((IDMPedestal)pedestal).decrementActivityCooldown();
         }
      }

      return false;
   }

   public @NotNull List getPedestalDescription() {
      List list = new ArrayList();
      if (ProjectEConfig.server.cooldown.pedestal.harvest.get() != -1) {
         list.add(PELang.PEDESTAL_HARVEST_GODDESS_1.translateColored(ChatFormatting.BLUE, new Object[0]));
         list.add(PELang.PEDESTAL_HARVEST_GODDESS_2.translateColored(ChatFormatting.BLUE, new Object[0]));
         list.add(PELang.PEDESTAL_HARVEST_GODDESS_3.translateColored(ChatFormatting.BLUE, new Object[]{MathUtils.tickToSecFormatted(ProjectEConfig.server.cooldown.pedestal.harvest.get())}));
      }

      return list;
   }

   private static class StackWithSlot {
      public final IPlantable plantable;
      public final int slot;
      public int count;

      public StackWithSlot(ItemStack stack, int slot, IPlantable plantable) {
         this.slot = slot;
         this.count = stack.m_41613_();
         this.plantable = plantable;
      }
   }
}
