package moze_intel.projecte.gameObjs.items;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import moze_intel.projecte.api.capabilities.item.IExtraFunction;
import moze_intel.projecte.api.capabilities.item.IProjectileShooter;
import moze_intel.projecte.capability.ExtraFunctionItemCapabilityWrapper;
import moze_intel.projecte.capability.ProjectileShooterItemCapabilityWrapper;
import moze_intel.projecte.gameObjs.container.PhilosStoneContainer;
import moze_intel.projecte.gameObjs.entity.EntityMobRandomizer;
import moze_intel.projecte.gameObjs.registries.PESoundEvents;
import moze_intel.projecte.utils.ClientKeyHelper;
import moze_intel.projecte.utils.PEKeybind;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.WorldTransmutations;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PhilosophersStone extends ItemMode implements IProjectileShooter, IExtraFunction {
   public PhilosophersStone(Item.Properties props) {
      super(props, 4, PELang.MODE_PHILOSOPHER_1, PELang.MODE_PHILOSOPHER_2, PELang.MODE_PHILOSOPHER_3);
      this.addItemCapability(ExtraFunctionItemCapabilityWrapper::new);
      this.addItemCapability(ProjectileShooterItemCapabilityWrapper::new);
   }

   public boolean hasCraftingRemainingItem(ItemStack stack) {
      return true;
   }

   public ItemStack getCraftingRemainingItem(ItemStack stack) {
      return stack.m_41777_();
   }

   public BlockHitResult getHitBlock(Player player) {
      return m_41435_(player.m_9236_(), player, player.m_36341_() ? Fluid.SOURCE_ONLY : Fluid.NONE);
   }

   public @NotNull InteractionResult m_6225_(UseOnContext ctx) {
      Player player = ctx.m_43723_();
      if (player == null) {
         return InteractionResult.FAIL;
      } else {
         BlockPos pos = ctx.m_8083_();
         Direction sideHit = ctx.m_43719_();
         Level level = ctx.m_43725_();
         ItemStack stack = ctx.m_43722_();
         if (level.f_46443_) {
            return InteractionResult.SUCCESS;
         } else {
            BlockHitResult rtr = this.getHitBlock(player);
            if (rtr.m_6662_() == Type.BLOCK && !rtr.m_82425_().equals(pos)) {
               pos = rtr.m_82425_();
               sideHit = rtr.m_82434_();
            }

            Map toChange = getChanges(level, pos, player, sideHit, this.getMode(stack), this.getCharge(stack));
            if (!toChange.isEmpty()) {
               Iterator var9 = toChange.entrySet().iterator();

               while(var9.hasNext()) {
                  Map.Entry entry = (Map.Entry)var9.next();
                  BlockPos currentPos = (BlockPos)entry.getKey();
                  PlayerHelper.checkedReplaceBlock((ServerPlayer)player, currentPos, (BlockState)entry.getValue());
                  if (level.f_46441_.m_188503_(8) == 0) {
                     ((ServerLevel)level).m_8767_(ParticleTypes.f_123755_, (double)currentPos.m_123341_(), (double)(currentPos.m_123342_() + 1), (double)currentPos.m_123343_(), 2, 0.0, 0.0, 0.0, 0.0);
                  }
               }

               level.m_6263_((Player)null, player.m_20185_(), player.m_20186_(), player.m_20189_(), (SoundEvent)PESoundEvents.TRANSMUTE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }

            return InteractionResult.SUCCESS;
         }
      }
   }

   public boolean shootProjectile(@NotNull Player player, @NotNull ItemStack stack, InteractionHand hand) {
      Level level = player.m_9236_();
      level.m_6263_((Player)null, player.m_20185_(), player.m_20186_(), player.m_20189_(), (SoundEvent)PESoundEvents.TRANSMUTE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
      EntityMobRandomizer ent = new EntityMobRandomizer(player, level);
      ent.m_37251_(player, player.m_146909_(), player.m_146908_(), 0.0F, 1.5F, 1.0F);
      level.m_7967_(ent);
      return true;
   }

   public boolean doExtraFunction(@NotNull ItemStack stack, @NotNull Player player, InteractionHand hand) {
      if (!player.m_9236_().f_46443_) {
         NetworkHooks.openScreen((ServerPlayer)player, new ContainerProvider(stack));
      }

      return true;
   }

   public void m_7373_(@NotNull ItemStack stack, @Nullable Level level, @NotNull List tooltips, @NotNull TooltipFlag flags) {
      super.m_7373_(stack, level, tooltips, flags);
      tooltips.add(PELang.TOOLTIP_PHILOSTONE.translate(new Object[]{ClientKeyHelper.getKeyName(PEKeybind.EXTRA_FUNCTION)}));
   }

   public static Map getChanges(Level level, BlockPos pos, Player player, Direction sideHit, int mode, int charge) {
      BlockState targeted = level.m_8055_(pos);
      boolean isSneaking = player.m_36341_();
      BlockState result = WorldTransmutations.getWorldTransmutation(targeted, isSneaking);
      if (result == null) {
         return Collections.emptyMap();
      } else {
         Stream stream = null;
         switch (mode) {
            case 0:
               stream = BlockPos.m_121990_(pos.m_7918_(-charge, -charge, -charge), pos.m_7918_(charge, charge, charge));
               break;
            case 1:
               if (sideHit != Direction.UP && sideHit != Direction.DOWN) {
                  if (sideHit != Direction.EAST && sideHit != Direction.WEST) {
                     if (sideHit == Direction.SOUTH || sideHit == Direction.NORTH) {
                        stream = BlockPos.m_121990_(pos.m_7918_(-charge, -charge, 0), pos.m_7918_(charge, charge, 0));
                     }
                  } else {
                     stream = BlockPos.m_121990_(pos.m_7918_(0, -charge, -charge), pos.m_7918_(0, charge, charge));
                  }
               } else {
                  stream = BlockPos.m_121990_(pos.m_7918_(-charge, 0, -charge), pos.m_7918_(charge, 0, charge));
               }
               break;
            case 2:
               Direction playerFacing = player.m_6350_();
               if (playerFacing.m_122434_() == Axis.Z) {
                  stream = BlockPos.m_121990_(pos.m_7918_(0, 0, -charge), pos.m_7918_(0, 0, charge));
               } else if (playerFacing.m_122434_() == Axis.X) {
                  stream = BlockPos.m_121990_(pos.m_7918_(-charge, 0, 0), pos.m_7918_(charge, 0, 0));
               }
         }

         if (stream == null) {
            return Collections.emptyMap();
         } else {
            Map conversions = new Object2ObjectArrayMap();
            conversions.put(targeted, result);
            Map changes = new HashMap();
            Block targetBlock = targeted.m_60734_();
            stream.forEach((currentPos) -> {
               BlockState state = level.m_8055_(currentPos);
               if (state.m_60713_(targetBlock)) {
                  BlockState actualResult;
                  if (conversions.containsKey(state)) {
                     actualResult = (BlockState)conversions.get(state);
                  } else {
                     conversions.put(state, actualResult = WorldTransmutations.getWorldTransmutation(state, isSneaking));
                  }

                  if (actualResult != null) {
                     changes.put(currentPos.m_7949_(), actualResult);
                  }
               }

            });
            return changes;
         }
      }
   }

   private static record ContainerProvider(ItemStack stack) implements MenuProvider {
      private ContainerProvider(ItemStack stack) {
         this.stack = stack;
      }

      public @NotNull AbstractContainerMenu m_7208_(int windowId, @NotNull Inventory playerInventory, @NotNull Player player) {
         return new PhilosStoneContainer(windowId, playerInventory, ContainerLevelAccess.m_39289_(player.m_9236_(), player.m_20183_()));
      }

      public @NotNull Component m_5446_() {
         return this.stack.m_41786_();
      }

      public ItemStack stack() {
         return this.stack;
      }
   }
}
