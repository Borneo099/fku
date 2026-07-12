package moze_intel.projecte.gameObjs.entity;

import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.registries.PEEntityTypes;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

public class EntityWaterProjectile extends NoGravityThrowableProjectile {
   public EntityWaterProjectile(EntityType type, Level level) {
      super(type, level);
   }

   public EntityWaterProjectile(Player entity, Level level) {
      super((EntityType)PEEntityTypes.WATER_PROJECTILE.get(), entity, level);
   }

   protected void m_8097_() {
   }

   public void m_8119_() {
      super.m_8119_();
      if (!this.m_9236_().f_46443_ && this.m_6084_()) {
         Entity thrower = this.m_19749_();
         if (thrower instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)thrower;
            BlockPos.m_121990_(this.m_20183_().m_7918_(-3, -3, -3), this.m_20183_().m_7918_(3, 3, 3)).forEach((pos) -> {
               BlockState state = this.m_9236_().m_8055_(pos);
               FluidState fluidState = state.m_60819_();
               if (fluidState.m_205070_(FluidTags.f_13132_)) {
                  pos = pos.m_7949_();
                  if (state.m_60734_() instanceof LiquidBlock) {
                     Block block = fluidState.m_76170_() ? Blocks.f_50080_ : Blocks.f_50652_;
                     BlockEvent.FluidPlaceBlockEvent event = new BlockEvent.FluidPlaceBlockEvent(this.m_9236_(), pos, pos, block.m_49966_());
                     if (!MinecraftForge.EVENT_BUS.post(event)) {
                        PlayerHelper.checkedPlaceBlock(player, pos, event.getNewState());
                     }
                  } else {
                     WorldHelper.drainFluid(this.m_9236_(), pos, state, Fluids.f_76195_);
                  }

                  this.m_5496_(SoundEvents.f_11909_, 0.5F, 2.6F + (this.m_9236_().f_46441_.m_188501_() - this.m_9236_().f_46441_.m_188501_()) * 0.8F);
               }

            });
         }

         if (this.m_20069_()) {
            this.m_146870_();
         }

         if (this.m_20186_() > 128.0) {
            LevelData worldInfo = this.m_9236_().m_6106_();
            worldInfo.m_5565_(true);
            this.m_146870_();
         }
      }

   }

   protected void m_6532_(@NotNull HitResult result) {
      super.m_6532_(result);
      this.m_146870_();
   }

   protected void m_8060_(@NotNull BlockHitResult result) {
      super.m_8060_(result);
      if (!this.m_9236_().f_46443_) {
         Entity var3 = this.m_19749_();
         if (var3 instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)var3;
            WorldHelper.placeFluid(player, this.m_9236_(), result.m_82425_(), result.m_82434_(), Fluids.f_76193_, !ProjectEConfig.server.items.opEvertide.get());
         }
      }

   }

   protected void m_5790_(@NotNull EntityHitResult result) {
      super.m_5790_(result);
      if (!this.m_9236_().f_46443_) {
         Entity ent = this.m_19749_();
         if (ent instanceof Player) {
            Player player = (Player)ent;
            ent = result.m_82443_();
            if (ent.m_6060_()) {
               ent.m_20095_();
            }

            ent.m_5997_(this.m_20184_().m_7096_() * 2.0, this.m_20184_().m_7098_() * 2.0, this.m_20184_().m_7094_() * 2.0);
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
