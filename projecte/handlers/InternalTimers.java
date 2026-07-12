package moze_intel.projecte.handlers;

import moze_intel.projecte.PECore;
import moze_intel.projecte.capability.managing.BasicCapabilityResolver;
import moze_intel.projecte.config.ProjectEConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.jetbrains.annotations.NotNull;

public class InternalTimers {
   public static final Capability CAPABILITY = CapabilityManager.get(new CapabilityToken() {
   });
   public static final ResourceLocation NAME = PECore.rl("internal_timers");
   private final Timer repair = new Timer();
   private final Timer heal = new Timer();
   private final Timer feed = new Timer();

   public void tick() {
      this.repair.tick();
      this.heal.tick();
      this.feed.tick();
   }

   public void activateRepair() {
      this.repair.shouldUpdate = ProjectEConfig.server.cooldown.player.repair.get() != -1;
   }

   public void activateHeal() {
      this.heal.shouldUpdate = ProjectEConfig.server.cooldown.player.heal.get() != -1;
   }

   public void activateFeed() {
      this.feed.shouldUpdate = ProjectEConfig.server.cooldown.player.feed.get() != -1;
   }

   public boolean canRepair() {
      if (this.repair.tickCount == 0) {
         this.repair.tickCount = ProjectEConfig.server.cooldown.player.repair.get();
         this.repair.shouldUpdate = false;
         return true;
      } else {
         return false;
      }
   }

   public boolean canHeal() {
      if (this.heal.tickCount == 0) {
         this.heal.tickCount = ProjectEConfig.server.cooldown.player.heal.get();
         this.heal.shouldUpdate = false;
         return true;
      } else {
         return false;
      }
   }

   public boolean canFeed() {
      if (this.feed.tickCount == 0) {
         this.feed.tickCount = ProjectEConfig.server.cooldown.player.feed.get();
         this.feed.shouldUpdate = false;
         return true;
      } else {
         return false;
      }
   }

   private static class Timer {
      private int tickCount = 0;
      private boolean shouldUpdate = false;

      private void tick() {
         if (this.shouldUpdate) {
            if (this.tickCount > 0) {
               --this.tickCount;
            }

            this.shouldUpdate = false;
         }

      }
   }

   public static class Provider extends BasicCapabilityResolver {
      public Provider() {
         super(InternalTimers::new);
      }

      public @NotNull Capability getMatchingCapability() {
         return InternalTimers.CAPABILITY;
      }
   }
}
