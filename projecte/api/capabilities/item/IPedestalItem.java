package moze_intel.projecte.api.capabilities.item;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public interface IPedestalItem {
   boolean updateInPedestal(@NotNull ItemStack var1, @NotNull Level var2, @NotNull BlockPos var3, @NotNull BlockEntity var4);

   @NotNull List getPedestalDescription();
}
