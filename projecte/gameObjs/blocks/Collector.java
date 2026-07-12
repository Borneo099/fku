package moze_intel.projecte.gameObjs.blocks;

import java.util.Optional;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.item.IItemEmcHolder;
import moze_intel.projecte.gameObjs.EnumCollectorTier;
import moze_intel.projecte.gameObjs.block_entities.CollectorMK1BlockEntity;
import moze_intel.projecte.gameObjs.registration.impl.BlockEntityTypeRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.utils.MathUtils;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Collector extends BlockDirection implements PEEntityBlock {
   private final EnumCollectorTier tier;

   public Collector(EnumCollectorTier tier, BlockBehaviour.Properties props) {
      super(props);
      this.tier = tier;
   }

   public EnumCollectorTier getTier() {
      return this.tier;
   }

   /** @deprecated */
   @Deprecated
   public @NotNull InteractionResult m_6227_(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
      if (level.f_46443_) {
         return InteractionResult.SUCCESS;
      } else {
         CollectorMK1BlockEntity collector = (CollectorMK1BlockEntity)WorldHelper.getBlockEntity(CollectorMK1BlockEntity.class, level, pos, true);
         if (collector != null) {
            NetworkHooks.openScreen((ServerPlayer)player, collector, pos);
         }

         return InteractionResult.CONSUME;
      }
   }

   public @Nullable BlockEntityTypeRegistryObject getType() {
      BlockEntityTypeRegistryObject var10000;
      switch (this.tier) {
         case MK1:
            var10000 = PEBlockEntityTypes.COLLECTOR;
            break;
         case MK2:
            var10000 = PEBlockEntityTypes.COLLECTOR_MK2;
            break;
         case MK3:
            var10000 = PEBlockEntityTypes.COLLECTOR_MK3;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   /** @deprecated */
   @Deprecated
   public boolean m_8133_(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, int id, int param) {
      super.m_8133_(state, level, pos, id, param);
      return this.triggerBlockEntityEvent(state, level, pos, id, param);
   }

   /** @deprecated */
   @Deprecated
   public boolean m_7278_(@NotNull BlockState state) {
      return true;
   }

   /** @deprecated */
   @Deprecated
   public int m_6782_(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
      CollectorMK1BlockEntity collector = (CollectorMK1BlockEntity)WorldHelper.getBlockEntity(CollectorMK1BlockEntity.class, level, pos, true);
      if (collector == null) {
         return super.m_6782_(state, level, pos);
      } else {
         Optional cap = collector.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP).resolve();
         if (cap.isEmpty()) {
            return super.m_6782_(state, level, pos);
         } else {
            ItemStack charging = ((IItemHandler)cap.get()).getStackInSlot(0);
            if (!charging.m_41619_()) {
               Optional holderCapability = charging.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).resolve();
               if (holderCapability.isPresent()) {
                  IItemEmcHolder emcHolder = (IItemEmcHolder)holderCapability.get();
                  return MathUtils.scaleToRedstone(emcHolder.getStoredEmc(charging), emcHolder.getMaximumEmc(charging));
               } else {
                  return MathUtils.scaleToRedstone(collector.getStoredEmc(), collector.getEmcToNextGoal());
               }
            } else {
               return MathUtils.scaleToRedstone(collector.getStoredEmc(), collector.getMaximumEmc());
            }
         }
      }
   }

   /** @deprecated */
   @Deprecated
   public void m_6810_(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
      if (state.m_60734_() != newState.m_60734_()) {
         CollectorMK1BlockEntity ent = (CollectorMK1BlockEntity)WorldHelper.getBlockEntity(CollectorMK1BlockEntity.class, level, pos);
         if (ent != null) {
            ent.clearLocked();
         }

         super.m_6810_(state, level, pos, newState, isMoving);
      }

   }
}
