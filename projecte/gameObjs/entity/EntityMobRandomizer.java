package moze_intel.projecte.gameObjs.entity;

import moze_intel.projecte.gameObjs.registries.PEEntityTypes;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.EntityRandomizerHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Rabbit.Variant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

public class EntityMobRandomizer extends NoGravityThrowableProjectile {
   public EntityMobRandomizer(EntityType type, Level level) {
      super(type, level);
   }

   public EntityMobRandomizer(Player entity, Level level) {
      super((EntityType)PEEntityTypes.MOB_RANDOMIZER.get(), entity, level);
   }

   protected void m_8097_() {
   }

   public void m_8119_() {
      super.m_8119_();
      if (!this.m_9236_().f_46443_ && this.m_6084_() && this.m_20069_()) {
         this.m_146870_();
      }

   }

   protected void m_6532_(@NotNull HitResult result) {
      if (this.m_9236_().f_46443_) {
         for(int i = 0; i < 4; ++i) {
            this.m_9236_().m_7106_(ParticleTypes.f_123760_, this.m_20185_(), this.m_20186_() + this.f_19796_.m_188500_() * 2.0, this.m_20189_(), this.f_19796_.m_188583_(), 0.0, this.f_19796_.m_188583_());
         }
      }

      if (!this.m_20069_()) {
         super.m_6532_(result);
      }

      this.m_146870_();
   }

   protected void m_5790_(@NotNull EntityHitResult result) {
      super.m_5790_(result);
      if (!this.m_9236_().f_46443_) {
         Entity var4 = result.m_82443_();
         if (var4 instanceof Mob) {
            Mob ent = (Mob)var4;
            var4 = this.m_19749_();
            if (var4 instanceof Player) {
               Player player = (Player)var4;
               ServerLevel level = (ServerLevel)this.m_9236_();
               Mob randomized = EntityRandomizerHelper.getRandomEntity(level, ent);
               if (randomized != null && EMCHelper.consumePlayerFuel(player, 384L) != -1L) {
                  Rabbit.RabbitGroupData data;
                  label20: {
                     randomized.m_7678_(ent.m_20185_(), ent.m_20186_(), ent.m_20189_(), ent.m_146908_(), ent.m_146909_());
                     if (randomized instanceof Rabbit) {
                        Rabbit rabbit = (Rabbit)randomized;
                        if (rabbit.m_28554_() == Variant.EVIL) {
                           data = new Rabbit.RabbitGroupData(Variant.EVIL);
                           break label20;
                        }
                     }

                     data = null;
                  }

                  ForgeEventFactory.onFinalizeSpawn(randomized, level, level.m_6436_(randomized.m_20183_()), MobSpawnType.CONVERSION, data, (CompoundTag)null);
                  level.m_8860_(randomized);
                  if (randomized.isAddedToWorld()) {
                     randomized.m_21373_();
                     ent.m_146870_();
                  }
               }
            }
         }
      }

   }

   public @NotNull Packet m_5654_() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   public boolean m_6128_() {
      return true;
   }
}
