package moze_intel.projecte.gameObjs.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProjectETNT extends TntBlock {
   private final TNTEntityCreator tntEntityCreator;

   public ProjectETNT(BlockBehaviour.Properties properties, TNTEntityCreator tntEntityCreator) {
      super(properties);
      this.tntEntityCreator = tntEntityCreator;
   }

   public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
      return 100;
   }

   public void onCaughtFire(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @Nullable Direction side, @Nullable LivingEntity igniter) {
      if (!level.f_46443_) {
         this.createAndAddEntity(level, pos, igniter);
         level.m_142346_(igniter, GameEvent.f_157776_, pos);
      }

   }

   public void createAndAddEntity(@NotNull Level level, @NotNull BlockPos pos, @Nullable LivingEntity igniter) {
      PrimedTnt tnt = this.tntEntityCreator.create(level, (double)((float)pos.m_123341_() + 0.5F), (double)pos.m_123342_(), (double)((float)pos.m_123343_() + 0.5F), igniter);
      level.m_7967_(tnt);
      level.m_6263_((Player)null, tnt.m_20185_(), tnt.m_20186_(), tnt.m_20189_(), SoundEvents.f_12512_, SoundSource.BLOCKS, 1.0F, 1.0F);
   }

   public DispenseItemBehavior createDispenseItemBehavior() {
      return new DefaultDispenseItemBehavior() {
         protected @NotNull ItemStack m_7498_(@NotNull BlockSource source, @NotNull ItemStack stack) {
            BlockPos blockpos = source.m_7961_().m_121945_((Direction)source.m_6414_().m_61143_(DispenserBlock.f_52659_));
            ProjectETNT.this.createAndAddEntity(source.m_7727_(), blockpos, (LivingEntity)null);
            source.m_7727_().m_142346_((Entity)null, GameEvent.f_157810_, blockpos);
            stack.m_41774_(1);
            return stack;
         }
      };
   }

   public void m_7592_(Level level, @NotNull BlockPos pos, @NotNull Explosion explosion) {
      if (!level.f_46443_) {
         PrimedTnt tnt = this.tntEntityCreator.create(level, (double)((float)pos.m_123341_() + 0.5F), (double)pos.m_123342_(), (double)((float)pos.m_123343_() + 0.5F), explosion.m_252906_());
         int fuse = tnt.m_32100_();
         tnt.m_32085_((short)(level.f_46441_.m_188503_(fuse / 4) + fuse / 8));
         level.m_7967_(tnt);
      }

   }

   @FunctionalInterface
   public interface TNTEntityCreator {
      PrimedTnt create(Level var1, double var2, double var4, double var6, @Nullable LivingEntity var8);
   }
}
