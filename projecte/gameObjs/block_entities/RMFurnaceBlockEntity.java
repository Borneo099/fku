package moze_intel.projecte.gameObjs.block_entities;

import moze_intel.projecte.gameObjs.container.RMFurnaceContainer;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class RMFurnaceBlockEntity extends DMFurnaceBlockEntity {
   public RMFurnaceBlockEntity(BlockPos pos, BlockState state) {
      super(PEBlockEntityTypes.RED_MATTER_FURNACE, pos, state, 3, 4);
   }

   protected int getInvSize() {
      return 13;
   }

   protected float getOreDoubleChance() {
      return 1.0F;
   }

   public int getCookProgressScaled(int value) {
      return (this.furnaceCookTime + (this.isBurning() && this.canSmelt() ? 1 : 0)) * value / this.ticksBeforeSmelt;
   }

   public @NotNull AbstractContainerMenu m_7208_(int windowId, @NotNull Inventory inv, @NotNull Player player) {
      return new RMFurnaceContainer(windowId, inv, this);
   }

   public @NotNull Component m_5446_() {
      return PELang.GUI_RED_MATTER_FURNACE.translate(new Object[0]);
   }
}
