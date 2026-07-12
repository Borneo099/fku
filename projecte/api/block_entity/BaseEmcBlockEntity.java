package moze_intel.projecte.api.block_entity;

import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public class BaseEmcBlockEntity extends BlockEntity implements IEmcStorage {
   private LazyOptional emcStorageCapability;
   private long maximumEMC;
   private long currentEMC;

   protected BaseEmcBlockEntity(BlockEntityType type, BlockPos pos, BlockState state) {
      super(type, pos, state);
      this.setMaximumEMC(Long.MAX_VALUE);
   }

   public final void setMaximumEMC(@Range(
   from = 1L,
   to = Long.MAX_VALUE
) long max) {
      this.maximumEMC = max;
      if (this.getStoredEmc() > this.getMaximumEmc()) {
         this.currentEMC = this.getMaximumEmc();
         this.storedEmcChanged();
      }

   }

   public @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long getStoredEmc() {
      return this.currentEMC;
   }

   public @Range(
   from = 1L,
   to = Long.MAX_VALUE
) long getMaximumEmc() {
      return this.maximumEMC;
   }

   protected @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long getEmcInsertLimit() {
      return this.getNeededEmc();
   }

   protected @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long getEmcExtractLimit() {
      return this.getStoredEmc();
   }

   protected boolean canAcceptEmc() {
      return true;
   }

   protected boolean canProvideEmc() {
      return true;
   }

   public long extractEmc(long toExtract, IEmcStorage.EmcAction action) {
      if (toExtract < 0L) {
         return this.insertEmc(-toExtract, action);
      } else {
         return this.canProvideEmc() ? this.forceExtractEmc(Math.min(this.getEmcExtractLimit(), toExtract), action) : 0L;
      }
   }

   public long insertEmc(long toAccept, IEmcStorage.EmcAction action) {
      if (toAccept < 0L) {
         return this.extractEmc(-toAccept, action);
      } else {
         return this.canAcceptEmc() ? this.forceInsertEmc(Math.min(this.getEmcInsertLimit(), toAccept), action) : 0L;
      }
   }

   protected long forceExtractEmc(long toExtract, IEmcStorage.EmcAction action) {
      if (toExtract < 0L) {
         return this.forceInsertEmc(-toExtract, action);
      } else {
         long toRemove = Math.min(this.getStoredEmc(), toExtract);
         if (action.execute()) {
            this.currentEMC -= toRemove;
            this.storedEmcChanged();
         }

         return toRemove;
      }
   }

   protected long forceInsertEmc(long toAccept, IEmcStorage.EmcAction action) {
      if (toAccept < 0L) {
         return this.forceExtractEmc(-toAccept, action);
      } else {
         long toAdd = Math.min(this.getNeededEmc(), toAccept);
         if (action.execute()) {
            this.currentEMC += toAdd;
            this.storedEmcChanged();
         }

         return toAdd;
      }
   }

   protected void storedEmcChanged() {
      this.m_6596_();
   }

   protected void m_183515_(@NotNull CompoundTag tag) {
      super.m_183515_(tag);
      if (this.getStoredEmc() > this.getMaximumEmc()) {
         this.currentEMC = this.getMaximumEmc();
      }

      tag.m_128356_("EMC", this.getStoredEmc());
   }

   public void m_142466_(@NotNull CompoundTag tag) {
      super.m_142466_(tag);
      long set = tag.m_128454_("EMC");
      if (set > this.getMaximumEmc()) {
         set = this.getMaximumEmc();
      }

      this.currentEMC = set;
   }

   public @NotNull LazyOptional getCapability(@NotNull Capability cap, @Nullable Direction side) {
      if (cap != PECapabilities.EMC_STORAGE_CAPABILITY) {
         return super.getCapability(cap, side);
      } else {
         if (this.emcStorageCapability == null || !this.emcStorageCapability.isPresent()) {
            this.emcStorageCapability = LazyOptional.of(() -> {
               return this;
            });
         }

         return this.emcStorageCapability.cast();
      }
   }

   public void invalidateCaps() {
      super.invalidateCaps();
      if (this.emcStorageCapability != null && this.emcStorageCapability.isPresent()) {
         this.emcStorageCapability.invalidate();
         this.emcStorageCapability = null;
      }

   }
}
