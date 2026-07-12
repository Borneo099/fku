package moze_intel.projecte.gameObjs.block_entities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import moze_intel.projecte.api.block_entity.BaseEmcBlockEntity;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import moze_intel.projecte.gameObjs.registration.impl.BlockEntityTypeRegistryObject;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public abstract class EmcBlockEntity extends BaseEmcBlockEntity {
   private boolean updateComparators;

   public EmcBlockEntity(BlockEntityTypeRegistryObject type, BlockPos pos, BlockState state) {
      this(type, pos, state, Long.MAX_VALUE);
   }

   public EmcBlockEntity(BlockEntityTypeRegistryObject type, BlockPos pos, BlockState state, @Range(
   from = 1L,
   to = Long.MAX_VALUE
) long maxAmount) {
      super((BlockEntityType)type.get(), pos, state);
      this.setMaximumEMC(maxAmount);
   }

   protected void updateComparators() {
      if (this.updateComparators) {
         BlockState state = this.m_58900_();
         if (!state.m_60795_()) {
            this.f_58857_.m_46717_(this.f_58858_, state.m_60734_());
         }

         this.updateComparators = false;
      }

   }

   protected boolean emcAffectsComparators() {
      return false;
   }

   protected void storedEmcChanged() {
      this.markDirty(this.emcAffectsComparators());
   }

   public void m_6596_() {
      this.markDirty(true);
   }

   public void markDirty(boolean recheckComparators) {
      if (this.f_58857_ != null) {
         if (this.f_58857_.m_46805_(this.f_58858_)) {
            this.f_58857_.m_46745_(this.f_58858_).m_8092_(true);
         }

         if (recheckComparators && !this.f_58857_.f_46443_) {
            this.updateComparators = true;
         }
      }

   }

   public final @NotNull CompoundTag m_5995_() {
      return this.m_187482_();
   }

   public final ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.m_195640_(this);
   }

   protected @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long sendToAllAcceptors(long emc) {
      if (this.f_58857_ != null && this.canProvideEmc()) {
         emc = Math.min(this.getEmcExtractLimit(), emc);
         long sentEmc = 0L;
         List targets = new ArrayList();
         Direction[] var6 = Direction.values();
         int var7 = var6.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            Direction dir = var6[var8];
            BlockPos neighboringPos = this.f_58858_.m_121945_(dir);
            if (this.f_58857_.m_46749_(neighboringPos)) {
               BlockEntity neighboringBE = WorldHelper.getBlockEntity(this.f_58857_, neighboringPos);
               if (neighboringBE != null) {
                  neighboringBE.getCapability(PECapabilities.EMC_STORAGE_CAPABILITY, dir.m_122424_()).ifPresent((theirEmcStorage) -> {
                     if ((!this.isRelay() || !theirEmcStorage.isRelay()) && theirEmcStorage.insertEmc(1L, IEmcStorage.EmcAction.SIMULATE) > 0L) {
                        targets.add(theirEmcStorage);
                     }

                  });
               }
            }
         }

         if (!targets.isEmpty()) {
            long emcPer = emc / (long)targets.size();

            long acceptedEmc;
            for(Iterator var16 = targets.iterator(); var16.hasNext(); sentEmc += acceptedEmc) {
               IEmcStorage target = (IEmcStorage)var16.next();
               long emcCanProvide = this.extractEmc(emcPer, IEmcStorage.EmcAction.SIMULATE);
               acceptedEmc = target.insertEmc(emcCanProvide, IEmcStorage.EmcAction.EXECUTE);
               this.extractEmc(acceptedEmc, IEmcStorage.EmcAction.EXECUTE);
            }
         }

         return sentEmc;
      } else {
         return 0L;
      }
   }

   protected class CompactableStackHandler extends StackHandler {
      private boolean needsCompacting = true;
      private boolean empty;

      protected CompactableStackHandler(int size) {
         super(size);
      }

      protected void onContentsChanged(int slot) {
         super.onContentsChanged(slot);
         this.needsCompacting = true;
      }

      public void compact() {
         if (this.needsCompacting) {
            if (EmcBlockEntity.this.f_58857_ != null && !EmcBlockEntity.this.f_58857_.f_46443_) {
               this.empty = ItemHelper.compactInventory(this);
            }

            this.needsCompacting = false;
         }

      }

      protected void onLoad() {
         super.onLoad();
         this.empty = IntStream.range(0, this.getSlots()).allMatch((slot) -> {
            return this.getStackInSlot(slot).m_41619_();
         });
      }

      public boolean isEmpty() {
         return this.empty;
      }
   }

   protected class StackHandler extends ItemStackHandler {
      protected StackHandler(int size) {
         super(size);
      }

      protected void onContentsChanged(int slot) {
         super.onContentsChanged(slot);
         EmcBlockEntity.this.m_6596_();
      }
   }
}
