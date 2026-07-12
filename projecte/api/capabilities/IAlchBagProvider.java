package moze_intel.projecte.api.capabilities;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public interface IAlchBagProvider extends INBTSerializable {
   @NotNull IItemHandler getBag(@NotNull DyeColor var1);

   void sync(DyeColor var1, @NotNull ServerPlayer var2);
}
