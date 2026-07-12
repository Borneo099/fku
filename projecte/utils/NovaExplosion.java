package moze_intel.projecte.utils;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class NovaExplosion extends Explosion {
   private final Level level;
   private final Explosion.BlockInteraction mode;
   private final double x;
   private final double y;
   private final double z;
   private final float size;

   public NovaExplosion(Level level, @Nullable Entity entity, double x, double y, double z, float radius, boolean causesFire, Explosion.BlockInteraction mode) {
      super(level, entity, (DamageSource)null, (ExplosionDamageCalculator)null, x, y, z, radius, causesFire, mode);
      this.level = level;
      this.mode = mode;
      this.size = radius;
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public void m_46075_(boolean spawnParticles) {
      if (this.level.f_46443_) {
         this.level.m_7785_(this.x, this.y, this.z, SoundEvents.f_11913_, SoundSource.BLOCKS, 4.0F, (1.0F + (this.level.f_46441_.m_188501_() - this.level.f_46441_.m_188501_()) * 0.2F) * 0.7F, false);
      }

      boolean hasExplosionMode = this.mode != BlockInteraction.KEEP;
      if (spawnParticles) {
         if (hasExplosionMode && this.size >= 2.0F) {
            this.level.m_7106_(ParticleTypes.f_123812_, this.x, this.y, this.z, 1.0, 0.0, 0.0);
         } else {
            this.level.m_7106_(ParticleTypes.f_123813_, this.x, this.y, this.z, 1.0, 0.0, 0.0);
         }
      }

      if (hasExplosionMode) {
         NonNullList allDrops = NonNullList.m_122779_();
         List toBlow = this.m_46081_();
         ObjectArrayList var10000;
         if (toBlow instanceof ObjectArrayList) {
            ObjectArrayList to = (ObjectArrayList)toBlow;
            var10000 = to;
         } else {
            var10000 = new ObjectArrayList(toBlow);
         }

         ObjectArrayList affectedBlockPositions = var10000;
         Util.m_214673_(affectedBlockPositions, this.level.f_46441_);
         ObjectListIterator var25 = affectedBlockPositions.iterator();

         while(var25.hasNext()) {
            BlockPos pos = (BlockPos)var25.next();
            BlockState state = this.level.m_8055_(pos);
            if (!state.m_60795_()) {
               if (spawnParticles) {
                  double adjustedX = (double)((float)pos.m_123341_() + this.level.f_46441_.m_188501_());
                  double adjustedY = (double)((float)pos.m_123342_() + this.level.f_46441_.m_188501_());
                  double adjustedZ = (double)((float)pos.m_123343_() + this.level.f_46441_.m_188501_());
                  double diffX = adjustedX - this.x;
                  double diffY = adjustedY - this.y;
                  double diffZ = adjustedZ - this.z;
                  double diff = Math.sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ);
                  diffX /= diff;
                  diffY /= diff;
                  diffZ /= diff;
                  double d7 = 0.5 / (diff / (double)this.size + 0.1);
                  d7 *= (double)(this.level.f_46441_.m_188501_() * this.level.f_46441_.m_188501_() + 0.3F);
                  diffX *= d7;
                  diffY *= d7;
                  diffZ *= d7;
                  this.level.m_7106_(ParticleTypes.f_123759_, (adjustedX + this.x) / 2.0, (adjustedY + this.y) / 2.0, (adjustedZ + this.z) / 2.0, diffX, diffY, diffZ);
                  this.level.m_7106_(ParticleTypes.f_123762_, adjustedX, adjustedY, adjustedZ, diffX, diffY, diffZ);
               }

               pos = pos.m_7949_();
               this.level.m_46473_().m_6180_("explosion_blocks");
               Level var10 = this.level;
               if (var10 instanceof ServerLevel) {
                  ServerLevel serverLevel = (ServerLevel)var10;
                  if (state.canDropFromExplosion(this.level, pos, this)) {
                     BlockEntity blockEntity = state.m_155947_() ? WorldHelper.getBlockEntity(serverLevel, pos) : null;
                     LootParams.Builder builder = (new LootParams.Builder(serverLevel)).m_287286_(LootContextParams.f_81460_, Vec3.m_82512_(pos)).m_287286_(LootContextParams.f_81463_, ItemStack.f_41583_).m_287289_(LootContextParams.f_81462_, blockEntity).m_287289_(LootContextParams.f_81455_, this.getExploder());
                     if (this.mode == BlockInteraction.DESTROY_WITH_DECAY) {
                        builder.m_287286_(LootContextParams.f_81464_, this.size);
                     }

                     allDrops.addAll(state.m_287290_(builder));
                  }
               }

               state.onBlockExploded(this.level, pos, this);
               this.level.m_46473_().m_7238_();
            }
         }

         LivingEntity placer = this.m_252906_();
         if (placer == null) {
            WorldHelper.createLootDrop(allDrops, this.level, this.x, this.y, this.z);
         } else {
            WorldHelper.createLootDrop(allDrops, this.level, placer.m_20183_());
         }
      }

   }
}
