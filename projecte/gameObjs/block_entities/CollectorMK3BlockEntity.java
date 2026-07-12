package moze_intel.projecte.gameObjs.block_entities;

import moze_intel.projecte.gameObjs.EnumCollectorTier;
import moze_intel.projecte.gameObjs.container.CollectorMK3Container;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.utils.text.TextComponentUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class CollectorMK3BlockEntity extends CollectorMK1BlockEntity {
   public CollectorMK3BlockEntity(BlockPos pos, BlockState state) {
      super(PEBlockEntityTypes.COLLECTOR_MK3, pos, state, EnumCollectorTier.MK3);
   }

   protected int getInvSize() {
      return 16;
   }

   public @NotNull AbstractContainerMenu m_7208_(int windowId, @NotNull Inventory playerInventory, @NotNull Player playerIn) {
      return new CollectorMK3Container(windowId, playerInventory, this);
   }

   public @NotNull Component m_5446_() {
      return TextComponentUtil.build(PEBlocks.COLLECTOR_MK3);
   }
}
