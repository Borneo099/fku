package moze_intel.projecte.utils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.function.Predicate;
import moze_intel.projecte.integration.curios.CuriosIntegration;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.to_client.CooldownResetPKT;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

public final class PlayerHelper {
   public static final ObjectiveCriteria SCOREBOARD_EMC = new ReadOnlyScoreCriteria("projecte:emc_score");

   public static boolean checkedPlaceBlock(ServerPlayer player, BlockPos pos, BlockState state) {
      if (!hasEditPermission(player, pos)) {
         return false;
      } else {
         Level level = player.m_9236_();
         BlockSnapshot before = BlockSnapshot.create(level.m_46472_(), level, pos);
         level.m_46597_(pos, state);
         BlockEvent.EntityPlaceEvent evt = new BlockEvent.EntityPlaceEvent(before, Blocks.f_50016_.m_49966_(), player);
         MinecraftForge.EVENT_BUS.post(evt);
         if (evt.isCanceled()) {
            level.restoringBlockSnapshots = true;
            before.restore(true, false);
            level.restoringBlockSnapshots = false;
            return false;
         } else {
            return true;
         }
      }
   }

   public static boolean checkedReplaceBlock(ServerPlayer player, BlockPos pos, BlockState state) {
      return hasBreakPermission(player, pos) && checkedPlaceBlock(player, pos, state);
   }

   public static ItemStack findFirstItem(Player player, Item consumeFrom) {
      return (ItemStack)player.m_150109_().f_35974_.stream().filter((s) -> {
         return !s.m_41619_() && s.m_41720_() == consumeFrom;
      }).findFirst().orElse(ItemStack.f_41583_);
   }

   public static boolean checkArmorHotbarCurios(Player player, Predicate checker) {
      return player.m_150109_().f_35975_.stream().anyMatch(checker) || checkHotbarCurios(player, checker);
   }

   public static boolean checkHotbarCurios(Player player, Predicate checker) {
      for(int i = 0; i < Inventory.m_36059_(); ++i) {
         if (checker.test(player.m_150109_().m_8020_(i))) {
            return true;
         }
      }

      if (checker.test(player.m_21206_())) {
         return true;
      } else {
         IItemHandler curios = getCurios(player);
         if (curios != null) {
            for(int i = 0; i < curios.getSlots(); ++i) {
               if (checker.test(curios.getStackInSlot(i))) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public static @Nullable IItemHandler getCurios(Player player) {
      return ModList.get().isLoaded("curios") ? CuriosIntegration.getAll(player) : null;
   }

   public static BlockHitResult getBlockLookingAt(Player player, double maxDistance) {
      Pair vecs = getLookVec(player, maxDistance);
      ClipContext ctx = new ClipContext((Vec3)vecs.getLeft(), (Vec3)vecs.getRight(), Block.OUTLINE, Fluid.NONE, player);
      return player.m_9236_().m_45547_(ctx);
   }

   public static Pair getLookVec(Player player, double maxDistance) {
      Vec3 look = player.m_20252_(1.0F);
      Vec3 playerPos = new Vec3(player.m_20185_(), player.m_20186_() + (double)player.m_20192_(), player.m_20189_());
      Vec3 src = playerPos.m_82520_(0.0, (double)player.m_20192_(), 0.0);
      Vec3 dest = src.m_82520_(look.f_82479_ * maxDistance, look.f_82480_ * maxDistance, look.f_82481_ * maxDistance);
      return ImmutablePair.of(src, dest);
   }

   public static boolean hasBreakPermission(ServerPlayer player, BlockPos pos) {
      return hasEditPermission(player, pos) && ForgeHooks.onBlockBreakEvent(player.m_9236_(), player.f_8941_.m_9290_(), player, pos) != -1;
   }

   public static boolean hasEditPermission(ServerPlayer player, BlockPos pos) {
      return ServerLifecycleHooks.getCurrentServer().m_7762_((ServerLevel)player.m_9236_(), pos, player) ? false : Arrays.stream(Direction.values()).allMatch((e) -> {
         return player.m_36204_(pos, e, ItemStack.f_41583_);
      });
   }

   public static void resetCooldown(Player player) {
      player.m_36334_();
      PacketHandler.sendTo(new CooldownResetPKT(), (ServerPlayer)player);
   }

   public static void swingItem(Player player, InteractionHand hand) {
      Level var3 = player.m_9236_();
      if (var3 instanceof ServerLevel level) {
         level.m_7726_().m_8394_(player, new ClientboundAnimatePacket(player, hand == InteractionHand.MAIN_HAND ? 0 : 3));
      }

   }

   public static void updateClientServerFlight(ServerPlayer player, boolean allowFlying) {
      updateClientServerFlight(player, allowFlying, allowFlying && player.m_150110_().f_35935_);
   }

   public static void updateClientServerFlight(ServerPlayer player, boolean allowFlying, boolean isFlying) {
      player.m_150110_().f_35936_ = allowFlying;
      player.m_150110_().f_35935_ = isFlying;
      player.m_6885_();
   }

   public static void updateScore(ServerPlayer player, ObjectiveCriteria objective, BigInteger value) {
      updateScore(player, objective, value.compareTo(Constants.MAX_INTEGER) > 0 ? Integer.MAX_VALUE : value.intValueExact());
   }

   public static void updateScore(ServerPlayer player, ObjectiveCriteria objective, int value) {
      player.m_36329_().m_83427_(objective, player.m_6302_(), (score) -> {
         score.m_83402_(value);
      });
   }
}
