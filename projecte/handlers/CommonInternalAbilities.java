package moze_intel.projecte.handlers;

import moze_intel.projecte.PECore;
import moze_intel.projecte.capability.managing.BasicCapabilityResolver;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.utils.PlayerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.jetbrains.annotations.NotNull;

public class CommonInternalAbilities {
   public static final Capability CAPABILITY = CapabilityManager.get(new CapabilityToken() {
   });
   public static final ResourceLocation NAME = PECore.rl("common_internal_abilities");
   private static final AttributeModifier WATER_SPEED_BOOST;
   private static final AttributeModifier LAVA_SPEED_BOOST;
   private final Player player;

   public CommonInternalAbilities(Player player) {
      this.player = player;
   }

   public void tick() {
      boolean applyWaterSpeed = false;
      boolean applyLavaSpeed = false;
      WalkOnType waterWalkOnType = this.canWalkOnWater();
      WalkOnType lavaWalkOnType = this.canWalkOnLava();
      if (waterWalkOnType.canWalk() || lavaWalkOnType.canWalk()) {
         int x = (int)Math.floor(this.player.m_20185_());
         int y = (int)(this.player.m_20186_() - this.player.m_6049_());
         int z = (int)Math.floor(this.player.m_20189_());
         BlockPos pos = new BlockPos(x, y, z);
         FluidState below = this.player.m_9236_().m_6425_(pos.m_7495_());
         boolean water = waterWalkOnType.canWalk() && below.m_205070_(FluidTags.f_13131_);
         boolean lava = lavaWalkOnType.canWalk() && below.m_205070_(FluidTags.f_13132_);
         if ((water || lava) && this.player.m_9236_().m_46859_(pos)) {
            if (!this.player.m_6144_()) {
               this.player.m_20256_(this.player.m_20184_().m_82542_(1.0, 0.0, 1.0));
               this.player.f_19789_ = 0.0F;
               this.player.m_6853_(true);
            }

            applyWaterSpeed = water && waterWalkOnType == CommonInternalAbilities.WalkOnType.ABLE_WITH_SPEED;
            applyLavaSpeed = lava && lavaWalkOnType == CommonInternalAbilities.WalkOnType.ABLE_WITH_SPEED;
         } else if (!this.player.m_9236_().f_46443_ && waterWalkOnType.canWalk() && this.player.m_20069_()) {
            this.player.m_20301_(this.player.m_6062_());
         }
      }

      if (!this.player.m_9236_().f_46443_) {
         AttributeInstance attribute = this.player.m_21051_(Attributes.f_22279_);
         if (attribute != null) {
            this.updateSpeed(attribute, applyWaterSpeed, WATER_SPEED_BOOST);
            this.updateSpeed(attribute, applyLavaSpeed, LAVA_SPEED_BOOST);
         }
      }

   }

   private void updateSpeed(AttributeInstance attribute, boolean apply, AttributeModifier speedModifier) {
      if (apply) {
         if (!attribute.m_22109_(speedModifier)) {
            attribute.m_22118_(speedModifier);
         }
      } else if (attribute.m_22109_(speedModifier)) {
         attribute.m_22130_(speedModifier);
      }

   }

   private WalkOnType canWalkOnWater() {
      if (PlayerHelper.checkHotbarCurios(this.player, (stack) -> {
         return !stack.m_41619_() && stack.m_41720_() == PEItems.EVERTIDE_AMULET.get();
      })) {
         return CommonInternalAbilities.WalkOnType.ABLE_WITH_SPEED;
      } else {
         ItemStack helmet = this.player.m_6844_(EquipmentSlot.HEAD);
         return !helmet.m_41619_() && helmet.m_41720_() == PEItems.GEM_HELMET.get() ? CommonInternalAbilities.WalkOnType.ABLE : CommonInternalAbilities.WalkOnType.UNABLE;
      }
   }

   private WalkOnType canWalkOnLava() {
      if (PlayerHelper.checkHotbarCurios(this.player, (stack) -> {
         return !stack.m_41619_() && stack.m_41720_() == PEItems.VOLCANITE_AMULET.get();
      })) {
         return CommonInternalAbilities.WalkOnType.ABLE_WITH_SPEED;
      } else {
         ItemStack chestplate = this.player.m_6844_(EquipmentSlot.CHEST);
         return !chestplate.m_41619_() && chestplate.m_41720_() == PEItems.GEM_CHESTPLATE.get() ? CommonInternalAbilities.WalkOnType.ABLE : CommonInternalAbilities.WalkOnType.UNABLE;
      }
   }

   static {
      WATER_SPEED_BOOST = new AttributeModifier("Walk on water speed boost", 0.15, Operation.ADDITION);
      LAVA_SPEED_BOOST = new AttributeModifier("Walk on lava speed boost", 0.15, Operation.ADDITION);
   }

   private static enum WalkOnType {
      ABLE,
      ABLE_WITH_SPEED,
      UNABLE;

      public boolean canWalk() {
         return this != UNABLE;
      }

      // $FF: synthetic method
      private static WalkOnType[] $values() {
         return new WalkOnType[]{ABLE, ABLE_WITH_SPEED, UNABLE};
      }
   }

   public static class Provider extends BasicCapabilityResolver {
      public Provider(Player player) {
         super(() -> {
            return new CommonInternalAbilities(player);
         });
      }

      public @NotNull Capability getMatchingCapability() {
         return CommonInternalAbilities.CAPABILITY;
      }
   }
}
