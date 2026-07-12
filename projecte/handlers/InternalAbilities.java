package moze_intel.projecte.handlers;

import java.util.UUID;
import moze_intel.projecte.PECore;
import moze_intel.projecte.capability.managing.BasicCapabilityResolver;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.items.IFlightProvider;
import moze_intel.projecte.gameObjs.items.IStepAssister;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.utils.PlayerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.jetbrains.annotations.NotNull;

public final class InternalAbilities {
   private static final UUID STEP_ASSIST_MODIFIER_UUID = UUID.fromString("4726C09D-FD86-46D0-92DD-49ED952A12D2");
   private static final AttributeModifier STEP_ASSIST;
   public static final Capability CAPABILITY;
   public static final ResourceLocation NAME;
   private final ServerPlayer player;
   private boolean swrgOverride = false;
   private boolean gemArmorReady = false;
   private boolean hadFlightItem = false;
   private boolean wasFlyingGamemode = false;
   private boolean isFlyingGamemode = false;
   private boolean wasFlying = false;
   private int projectileCooldown = 0;
   private int gemChestCooldown = 0;

   public InternalAbilities(ServerPlayer player) {
      this.player = player;
   }

   public void resetProjectileCooldown() {
      this.projectileCooldown = ProjectEConfig.server.cooldown.player.projectile.get();
   }

   public int getProjectileCooldown() {
      return this.projectileCooldown;
   }

   public void resetGemCooldown() {
      this.gemChestCooldown = ProjectEConfig.server.cooldown.player.gemChest.get();
   }

   public int getGemCooldown() {
      return this.gemChestCooldown;
   }

   public void setGemState(boolean state) {
      this.gemArmorReady = state;
   }

   public boolean getGemState() {
      return this.gemArmorReady;
   }

   public void tick() {
      if (this.projectileCooldown > 0) {
         --this.projectileCooldown;
      }

      if (this.gemChestCooldown > 0) {
         --this.gemChestCooldown;
      }

      if (!this.shouldPlayerFly()) {
         if (this.hadFlightItem) {
            if (this.player.m_150110_().f_35936_) {
               PlayerHelper.updateClientServerFlight(this.player, false);
            }

            this.hadFlightItem = false;
         }

         this.wasFlyingGamemode = false;
         this.wasFlying = false;
      } else {
         if (!this.hadFlightItem) {
            if (!this.player.m_150110_().f_35936_) {
               PlayerHelper.updateClientServerFlight(this.player, true);
            }

            this.hadFlightItem = true;
         } else if (this.wasFlyingGamemode && !this.isFlyingGamemode) {
            PlayerHelper.updateClientServerFlight(this.player, true, this.wasFlying);
         }

         this.wasFlyingGamemode = this.isFlyingGamemode;
         this.wasFlying = this.player.m_150110_().f_35935_;
      }

      AttributeInstance attributeInstance = this.player.m_21051_((Attribute)ForgeMod.STEP_HEIGHT_ADDITION.get());
      if (attributeInstance != null) {
         AttributeModifier existing = attributeInstance.m_22111_(STEP_ASSIST_MODIFIER_UUID);
         if (this.shouldPlayerStep()) {
            if (existing == null) {
               attributeInstance.m_22118_(STEP_ASSIST);
            }
         } else if (existing != null) {
            attributeInstance.m_22130_(existing);
         }
      }

   }

   public void onDimensionChange() {
      PlayerHelper.updateClientServerFlight(this.player, this.player.m_150110_().f_35936_);
   }

   private boolean shouldPlayerFly() {
      if (!this.hasSwrg()) {
         this.disableSwrgFlightOverride();
      }

      this.isFlyingGamemode = this.player.m_7500_() || this.player.m_5833_();
      return !this.isFlyingGamemode && !this.swrgOverride ? PlayerHelper.checkArmorHotbarCurios(this.player, (stack) -> {
         boolean var10000;
         if (!stack.m_41619_()) {
            Item patt4692$temp = stack.m_41720_();
            if (patt4692$temp instanceof IFlightProvider) {
               IFlightProvider provider = (IFlightProvider)patt4692$temp;
               if (provider.canProvideFlight(stack, this.player)) {
                  var10000 = true;
                  return var10000;
               }
            }
         }

         var10000 = false;
         return var10000;
      }) : true;
   }

   private boolean shouldPlayerStep() {
      return PlayerHelper.checkArmorHotbarCurios(this.player, (stack) -> {
         boolean var10000;
         if (!stack.m_41619_()) {
            Item patt4914$temp = stack.m_41720_();
            if (patt4914$temp instanceof IStepAssister) {
               IStepAssister assister = (IStepAssister)patt4914$temp;
               if (assister.canAssistStep(stack, this.player)) {
                  var10000 = true;
                  return var10000;
               }
            }
         }

         var10000 = false;
         return var10000;
      });
   }

   private boolean hasSwrg() {
      return PlayerHelper.checkHotbarCurios(this.player, (stack) -> {
         return !stack.m_41619_() && stack.m_41720_() == PEItems.SWIFTWOLF_RENDING_GALE.get();
      });
   }

   public void enableSwrgFlightOverride() {
      this.swrgOverride = true;
   }

   public void disableSwrgFlightOverride() {
      this.swrgOverride = false;
   }

   static {
      STEP_ASSIST = new AttributeModifier(STEP_ASSIST_MODIFIER_UUID, "Step Assist", 0.4, Operation.ADDITION);
      CAPABILITY = CapabilityManager.get(new CapabilityToken() {
      });
      NAME = PECore.rl("internal_abilities");
   }

   public static class Provider extends BasicCapabilityResolver {
      public Provider(ServerPlayer player) {
         super(() -> {
            return new InternalAbilities(player);
         });
      }

      public @NotNull Capability getMatchingCapability() {
         return InternalAbilities.CAPABILITY;
      }
   }
}
