package moze_intel.projecte.gameObjs.entity;

import moze_intel.projecte.gameObjs.registries.PEEntityTypes;
import moze_intel.projecte.utils.Constants;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

public class EntityLensProjectile extends NoGravityThrowableProjectile {
   private int charge;

   public EntityLensProjectile(EntityType type, Level level) {
      super(type, level);
   }

   public EntityLensProjectile(Player entity, int charge, Level level) {
      super((EntityType)PEEntityTypes.LENS_PROJECTILE.get(), entity, level);
      this.charge = charge;
   }

   protected void m_8097_() {
   }

   public void m_8119_() {
      super.m_8119_();
      if (!this.m_9236_().f_46443_ && this.m_6084_() && this.m_20069_()) {
         this.m_5496_(SoundEvents.f_11909_, 0.7F, 1.6F + (this.f_19796_.m_188501_() - this.f_19796_.m_188501_()) * 0.4F);
         ((ServerLevel)this.m_9236_()).m_8767_(ParticleTypes.f_123755_, this.m_20185_(), this.m_20186_(), this.m_20189_(), 2, 0.0, 0.0, 0.0, 0.0);
         this.m_146870_();
      }

   }

   protected void m_6532_(@NotNull HitResult result) {
      if (!this.m_9236_().f_46443_) {
         WorldHelper.createNovaExplosion(this.m_9236_(), this.m_19749_(), this.m_20185_(), this.m_20186_(), this.m_20189_(), Constants.EXPLOSIVE_LENS_RADIUS[this.charge]);
      }

      this.m_146852_(GameEvent.f_157777_, this.m_19749_());
      this.m_146870_();
   }

   public void m_7380_(@NotNull CompoundTag nbt) {
      super.m_7380_(nbt);
      nbt.m_128405_("Charge", this.charge);
   }

   public void m_7378_(@NotNull CompoundTag nbt) {
      super.m_7378_(nbt);
      this.charge = nbt.m_128451_("Charge");
   }

   public @NotNull Packet m_5654_() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   public boolean m_6128_() {
      return true;
   }
}
