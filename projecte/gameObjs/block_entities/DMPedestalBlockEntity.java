package moze_intel.projecte.gameObjs.block_entities;

import java.util.Optional;
import moze_intel.projecte.api.block_entity.IDMPedestal;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.item.IPedestalItem;
import moze_intel.projecte.capability.managing.BasicCapabilityResolver;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.gameObjs.registries.PESoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

public class DMPedestalBlockEntity extends CapabilityEmcBlockEntity implements IDMPedestal {
   private static final int RANGE = 4;
   private final EmcBlockEntity.StackHandler inventory = new EmcBlockEntity.StackHandler(1) {
      public void onContentsChanged(int slot) {
         super.onContentsChanged(slot);
         if (DMPedestalBlockEntity.this.f_58857_ != null && !DMPedestalBlockEntity.this.f_58857_.f_46443_) {
            BlockState state = DMPedestalBlockEntity.this.m_58900_();
            DMPedestalBlockEntity.this.f_58857_.m_7260_(DMPedestalBlockEntity.this.f_58858_, state, state, 8);
         }

      }
   };
   private boolean isActive = false;
   private int particleCooldown = 10;
   private int activityCooldown = 0;
   public boolean previousRedstoneState = false;

   public DMPedestalBlockEntity(BlockPos pos, BlockState state) {
      super(PEBlockEntityTypes.DARK_MATTER_PEDESTAL, pos, state, 1000L);
      this.itemHandlerResolver = BasicCapabilityResolver.getBasicItemHandlerResolver((IItemHandler)this.inventory);
   }

   public static void tickClient(Level level, BlockPos pos, BlockState state, DMPedestalBlockEntity pedestal) {
      if (pedestal.getActive()) {
         ItemStack stack = pedestal.inventory.getStackInSlot(0);
         if (stack.m_41619_()) {
            pedestal.setActive(false);
         } else {
            Optional capability = stack.getCapability(PECapabilities.PEDESTAL_ITEM_CAPABILITY).resolve();
            if (capability.isPresent()) {
               ((IPedestalItem)capability.get()).updateInPedestal(stack, level, pos, pedestal);
               if (pedestal.particleCooldown <= 0) {
                  pedestal.spawnParticleTypes();
                  pedestal.particleCooldown = 10;
               } else {
                  --pedestal.particleCooldown;
               }
            } else {
               pedestal.setActive(false);
            }
         }
      }

   }

   public static void tickServer(Level level, BlockPos pos, BlockState state, DMPedestalBlockEntity pedestal) {
      if (pedestal.getActive()) {
         ItemStack stack = pedestal.inventory.getStackInSlot(0);
         if (stack.m_41619_()) {
            pedestal.setActive(false);
         } else {
            Optional capability = stack.getCapability(PECapabilities.PEDESTAL_ITEM_CAPABILITY).resolve();
            if (capability.isPresent()) {
               if (((IPedestalItem)capability.get()).updateInPedestal(stack, level, pos, pedestal)) {
                  pedestal.inventory.onContentsChanged(0);
               }
            } else {
               pedestal.setActive(false);
            }
         }
      }

      pedestal.updateComparators();
   }

   private void spawnParticleTypes() {
      int x = this.f_58858_.m_123341_();
      int y = this.f_58858_.m_123342_();
      int z = this.f_58858_.m_123343_();
      this.f_58857_.m_7106_(ParticleTypes.f_123744_, (double)x + 0.2, (double)y + 0.3, (double)z + 0.2, 0.0, 0.0, 0.0);
      this.f_58857_.m_7106_(ParticleTypes.f_123744_, (double)x + 0.2, (double)y + 0.3, (double)z + 0.5, 0.0, 0.0, 0.0);
      this.f_58857_.m_7106_(ParticleTypes.f_123744_, (double)x + 0.2, (double)y + 0.3, (double)z + 0.8, 0.0, 0.0, 0.0);
      this.f_58857_.m_7106_(ParticleTypes.f_123744_, (double)x + 0.5, (double)y + 0.3, (double)z + 0.2, 0.0, 0.0, 0.0);
      this.f_58857_.m_7106_(ParticleTypes.f_123744_, (double)x + 0.5, (double)y + 0.3, (double)z + 0.8, 0.0, 0.0, 0.0);
      this.f_58857_.m_7106_(ParticleTypes.f_123744_, (double)x + 0.8, (double)y + 0.3, (double)z + 0.2, 0.0, 0.0, 0.0);
      this.f_58857_.m_7106_(ParticleTypes.f_123744_, (double)x + 0.8, (double)y + 0.3, (double)z + 0.5, 0.0, 0.0, 0.0);
      this.f_58857_.m_7106_(ParticleTypes.f_123744_, (double)x + 0.8, (double)y + 0.3, (double)z + 0.8, 0.0, 0.0, 0.0);
      RandomSource rand = this.f_58857_.f_46441_;

      for(int i = 0; i < 3; ++i) {
         int j = rand.m_188503_(2) * 2 - 1;
         int k = rand.m_188503_(2) * 2 - 1;
         double d0 = (double)this.f_58858_.m_123341_() + 0.5 + 0.25 * (double)j;
         double d1 = (double)((float)this.f_58858_.m_123342_() + rand.m_188501_());
         double d2 = (double)this.f_58858_.m_123343_() + 0.5 + 0.25 * (double)k;
         double d3 = (double)(rand.m_188501_() * (float)j);
         double d4 = ((double)rand.m_188501_() - 0.5) * 0.125;
         double d5 = (double)(rand.m_188501_() * (float)k);
         this.f_58857_.m_7106_(ParticleTypes.f_123760_, d0, d1, d2, d3, d4, d5);
      }

   }

   public int getActivityCooldown() {
      return this.activityCooldown;
   }

   public void setActivityCooldown(int cooldown) {
      if (this.activityCooldown != cooldown) {
         this.activityCooldown = cooldown;
         this.markDirty(false);
      }

   }

   public void decrementActivityCooldown() {
      --this.activityCooldown;
      this.markDirty(false);
   }

   public AABB getEffectBounds() {
      return new AABB(this.f_58858_.m_7918_(-4, -4, -4), this.f_58858_.m_7918_(4, 4, 4));
   }

   public void m_142466_(@NotNull CompoundTag nbt) {
      super.m_142466_(nbt);
      this.inventory.deserializeNBT(nbt);
      this.setActive(nbt.m_128471_("isActive"));
      this.activityCooldown = nbt.m_128451_("activityCooldown");
      this.previousRedstoneState = nbt.m_128471_("powered");
   }

   protected void m_183515_(@NotNull CompoundTag tag) {
      super.m_183515_(tag);
      tag.m_128391_(this.inventory.serializeNBT());
      tag.m_128379_("isActive", this.getActive());
      tag.m_128405_("activityCooldown", this.activityCooldown);
      tag.m_128379_("powered", this.previousRedstoneState);
   }

   public boolean getActive() {
      return this.isActive;
   }

   public void setActive(boolean newState) {
      if (newState != this.getActive() && this.f_58857_ != null) {
         int i;
         if (newState) {
            this.f_58857_.m_5594_((Player)null, this.f_58858_, (SoundEvent)PESoundEvents.CHARGE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);

            for(i = 0; i < this.f_58857_.f_46441_.m_188503_(35) + 10; ++i) {
               this.f_58857_.m_7106_(ParticleTypes.f_123771_, (double)this.f_58858_.m_123341_() + 0.5 + this.f_58857_.f_46441_.m_188583_() * 0.12999999523162842, (double)(this.f_58858_.m_123342_() + 1) + this.f_58857_.f_46441_.m_188583_() * 0.12999999523162842, (double)this.f_58858_.m_123343_() + 0.5 + this.f_58857_.f_46441_.m_188583_() * 0.12999999523162842, 0.0, 0.0, 0.0);
            }
         } else {
            this.f_58857_.m_5594_((Player)null, this.f_58858_, (SoundEvent)PESoundEvents.UNCHARGE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);

            for(i = 0; i < this.f_58857_.f_46441_.m_188503_(35) + 10; ++i) {
               this.f_58857_.m_7106_(ParticleTypes.f_123762_, (double)this.f_58858_.m_123341_() + 0.5 + this.f_58857_.f_46441_.m_188583_() * 0.12999999523162842, (double)(this.f_58858_.m_123342_() + 1) + this.f_58857_.f_46441_.m_188583_() * 0.12999999523162842, (double)this.f_58858_.m_123343_() + 0.5 + this.f_58857_.f_46441_.m_188583_() * 0.12999999523162842, 0.0, 0.0, 0.0);
            }
         }
      }

      this.isActive = newState;
      this.m_6596_();
   }

   public IItemHandlerModifiable getInventory() {
      return this.inventory;
   }
}
