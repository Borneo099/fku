package moze_intel.projecte.gameObjs.blocks;

import moze_intel.projecte.gameObjs.EnumMatterType;
import moze_intel.projecte.gameObjs.block_entities.DMFurnaceBlockEntity;
import moze_intel.projecte.gameObjs.registration.impl.BlockEntityTypeRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MatterFurnace extends AbstractFurnaceBlock implements IMatterBlock, PEEntityBlock {
   private final EnumMatterType matterType;

   public MatterFurnace(BlockBehaviour.Properties props, EnumMatterType type) {
      super(props);
      this.matterType = type;
   }

   public @Nullable BlockEntityTypeRegistryObject getType() {
      return this.matterType == EnumMatterType.RED_MATTER ? PEBlockEntityTypes.RED_MATTER_FURNACE : PEBlockEntityTypes.DARK_MATTER_FURNACE;
   }

   protected void m_7137_(Level level, @NotNull BlockPos pos, @NotNull Player player) {
      if (!level.f_46443_) {
         DMFurnaceBlockEntity furnace = (DMFurnaceBlockEntity)WorldHelper.getBlockEntity(DMFurnaceBlockEntity.class, level, pos, true);
         if (furnace != null) {
            NetworkHooks.openScreen((ServerPlayer)player, furnace, pos);
         }
      }

   }

   /** @deprecated */
   @Deprecated
   public void m_6810_(BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
      if (state.m_60734_() != newState.m_60734_()) {
         BlockEntity furnace = WorldHelper.getBlockEntity(level, pos);
         if (furnace != null) {
            furnace.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent((inv) -> {
               WorldHelper.dropInventory(inv, level, pos);
            });
         }

         super.m_6810_(state, level, pos, newState, isMoving);
      }

   }

   public int m_6782_(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
      BlockEntity blockEntity = WorldHelper.getBlockEntity(level, pos);
      return blockEntity != null ? (Integer)blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).map(ItemHandlerHelper::calcRedstoneFromInventory).orElse(0) : 0;
   }

   public EnumMatterType getMatterType() {
      return this.matterType;
   }
}
