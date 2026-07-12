package moze_intel.projecte.gameObjs.items.armor;

import java.util.List;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.handlers.InternalTimers;
import moze_intel.projecte.utils.ClientKeyHelper;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.PEKeybind;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GemHelmet extends GemArmorBase {
   public GemHelmet(Item.Properties props) {
      super(Type.HELMET, props);
   }

   public static void toggleNightVision(ItemStack helm, Player player) {
      CompoundTag helmetTag = helm.m_41784_();
      boolean value;
      if (helmetTag.m_128425_("NightVision", 1)) {
         value = !helmetTag.m_128471_("NightVision");
         helmetTag.m_128379_("NightVision", value);
      } else {
         helmetTag.m_128379_("NightVision", true);
         value = true;
      }

      if (value) {
         player.m_213846_(PELang.NIGHT_VISION.translate(new Object[]{ChatFormatting.GREEN, PELang.GEM_ENABLED}));
      } else {
         player.m_213846_(PELang.NIGHT_VISION.translate(new Object[]{ChatFormatting.RED, PELang.GEM_DISABLED}));
      }

   }

   public void m_7373_(@NotNull ItemStack stack, @Nullable Level level, @NotNull List tooltips, @NotNull TooltipFlag flags) {
      super.m_7373_(stack, level, tooltips, flags);
      tooltips.add(PELang.GEM_LORE_HELM.translate(new Object[0]));
      tooltips.add(PELang.NIGHT_VISION_PROMPT.translate(new Object[]{ClientKeyHelper.getKeyName(PEKeybind.HELMET_TOGGLE)}));
      if (ItemHelper.checkItemNBT(stack, "NightVision")) {
         tooltips.add(PELang.NIGHT_VISION.translate(new Object[]{ChatFormatting.GREEN, PELang.GEM_ENABLED}));
      } else {
         tooltips.add(PELang.NIGHT_VISION.translate(new Object[]{ChatFormatting.RED, PELang.GEM_DISABLED}));
      }

   }

   public void onArmorTick(ItemStack stack, Level level, Player player) {
      if (!level.f_46443_) {
         player.getCapability(InternalTimers.CAPABILITY).ifPresent((handler) -> {
            handler.activateHeal();
            if (player.m_21223_() < player.m_21233_() && handler.canHeal()) {
               player.m_5634_(2.0F);
            }

         });
         if (ItemHelper.checkItemNBT(stack, "NightVision")) {
            player.m_7292_(new MobEffectInstance(MobEffects.f_19611_, 220, 0, true, false));
         } else {
            player.m_21195_(MobEffects.f_19611_);
         }
      }

   }

   public void doZap(Player player) {
      if (ProjectEConfig.server.difficulty.offensiveAbilities.get()) {
         BlockHitResult strikeResult = PlayerHelper.getBlockLookingAt(player, 120.0);
         if (strikeResult.m_6662_() != net.minecraft.world.phys.HitResult.Type.MISS) {
            BlockPos strikePos = strikeResult.m_82425_();
            Level level = player.m_9236_();
            LightningBolt lightning = (LightningBolt)EntityType.f_20465_.m_20615_(level);
            if (lightning != null) {
               lightning.m_20219_(Vec3.m_82512_(strikePos));
               lightning.m_20879_((ServerPlayer)player);
               level.m_7967_(lightning);
            }
         }
      }

   }
}
