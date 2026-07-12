package moze_intel.projecte.gameObjs.blocks;

import moze_intel.projecte.gameObjs.EnumRelayTier;
import moze_intel.projecte.gameObjs.block_entities.RelayMK1BlockEntity;
import moze_intel.projecte.gameObjs.registration.impl.BlockEntityTypeRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.utils.MathUtils;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Relay extends BlockDirection implements PEEntityBlock {
   private final EnumRelayTier tier;

   public Relay(EnumRelayTier tier, BlockBehaviour.Properties props) {
      super(props);
      this.tier = tier;
   }

   public EnumRelayTier getTier() {
      return this.tier;
   }

   /** @deprecated */
   @Deprecated
   public @NotNull InteractionResult m_6227_(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult rtr) {
      if (level.f_46443_) {
         return InteractionResult.SUCCESS;
      } else {
         RelayMK1BlockEntity relay = (RelayMK1BlockEntity)WorldHelper.getBlockEntity(RelayMK1BlockEntity.class, level, pos, true);
         if (relay != null) {
            NetworkHooks.openScreen((ServerPlayer)player, relay, pos);
         }

         return InteractionResult.CONSUME;
      }
   }

   public @Nullable BlockEntityTypeRegistryObject getType() {
      BlockEntityTypeRegistryObject var10000;
      switch (this.tier) {
         case MK1:
            var10000 = PEBlockEntityTypes.RELAY;
            break;
         case MK2:
            var10000 = PEBlockEntityTypes.RELAY_MK2;
            break;
         case MK3:
            var10000 = PEBlockEntityTypes.RELAY_MK3;
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
      RelayMK1BlockEntity relay = (RelayMK1BlockEntity)WorldHelper.getBlockEntity(RelayMK1BlockEntity.class, level, pos, true);
      return relay == null ? 0 : MathUtils.scaleToRedstone(relay.getStoredEmc(), relay.getMaximumEmc());
   }
}
