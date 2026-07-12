package moze_intel.projecte.gameObjs.block_entities;

import moze_intel.projecte.gameObjs.container.EmcChestBlockEntityContainer;
import moze_intel.projecte.gameObjs.registration.impl.BlockEntityTypeRegistryObject;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public abstract class EmcChestBlockEntity extends CapabilityEmcBlockEntity implements LidBlockEntity, MenuProvider {
   private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
      protected void m_142292_(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
         level.m_6263_((Player)null, (double)pos.m_123341_() + 0.5, (double)pos.m_123342_() + 0.5, (double)pos.m_123343_() + 0.5, SoundEvents.f_11749_, SoundSource.BLOCKS, 0.5F, level.f_46441_.m_188501_() * 0.1F + 0.9F);
      }

      protected void m_142289_(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
         level.m_6263_((Player)null, (double)pos.m_123341_() + 0.5, (double)pos.m_123342_() + 0.5, (double)pos.m_123343_() + 0.5, SoundEvents.f_11747_, SoundSource.BLOCKS, 0.5F, level.f_46441_.m_188501_() * 0.1F + 0.9F);
      }

      protected void m_142148_(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, int oldCount, int openCount) {
         level.m_7696_(pos, state.m_60734_(), 1, openCount);
      }

      protected boolean m_142718_(Player player) {
         AbstractContainerMenu var3 = player.f_36096_;
         boolean var10000;
         if (var3 instanceof EmcChestBlockEntityContainer container) {
            if (container.blockEntityMatches(EmcChestBlockEntity.this)) {
               var10000 = true;
               return var10000;
            }
         }

         var10000 = false;
         return var10000;
      }
   };
   private final ChestLidController chestLidController = new ChestLidController();

   protected EmcChestBlockEntity(BlockEntityTypeRegistryObject type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }

   protected EmcChestBlockEntity(BlockEntityTypeRegistryObject type, BlockPos pos, BlockState state, @Range(
   from = 1L,
   to = Long.MAX_VALUE
) long maxAmount) {
      super(type, pos, state, maxAmount);
   }

   public static void lidAnimateTick(Level level, BlockPos pos, BlockState state, EmcChestBlockEntity chest) {
      chest.chestLidController.m_155374_();
   }

   public boolean m_7531_(int id, int type) {
      if (id == 1) {
         this.chestLidController.m_155377_(type > 0);
         return true;
      } else {
         return super.m_7531_(id, type);
      }
   }

   public void startOpen(Player player) {
      if (!this.m_58901_() && !player.m_5833_() && this.f_58857_ != null) {
         this.openersCounter.m_155452_(player, this.f_58857_, this.m_58899_(), this.m_58900_());
      }

   }

   public void stopOpen(Player player) {
      if (!this.m_58901_() && !player.m_5833_() && this.f_58857_ != null) {
         this.openersCounter.m_155468_(player, this.f_58857_, this.m_58899_(), this.m_58900_());
      }

   }

   public void recheckOpen() {
      if (!this.m_58901_() && this.f_58857_ != null) {
         this.openersCounter.m_155476_(this.f_58857_, this.m_58899_(), this.m_58900_());
      }

   }

   public float m_6683_(float partialTicks) {
      return this.chestLidController.m_155375_(partialTicks);
   }
}
