package moze_intel.projecte.gameObjs.block_entities;

import moze_intel.projecte.capability.managing.ICapabilityResolver;
import moze_intel.projecte.gameObjs.registration.impl.BlockEntityTypeRegistryObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public abstract class CapabilityEmcBlockEntity extends EmcBlockEntity {
   protected @Nullable ICapabilityResolver itemHandlerResolver;

   public CapabilityEmcBlockEntity(BlockEntityTypeRegistryObject type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }

   public CapabilityEmcBlockEntity(BlockEntityTypeRegistryObject type, BlockPos pos, BlockState state, @Range(
   from = 1L,
   to = Long.MAX_VALUE
) long maxAmount) {
      super(type, pos, state, maxAmount);
   }

   public void invalidateCaps() {
      super.invalidateCaps();
      if (this.itemHandlerResolver != null) {
         this.itemHandlerResolver.invalidateAll();
      }

   }

   public @NotNull LazyOptional getCapability(@NotNull Capability cap, Direction side) {
      return cap == ForgeCapabilities.ITEM_HANDLER && this.itemHandlerResolver != null ? this.itemHandlerResolver.getCapabilityUnchecked(cap, side) : super.getCapability(cap, side);
   }
}
