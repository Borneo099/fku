package moze_intel.projecte.gameObjs.items.tools;

import com.google.common.collect.Multimap;
import java.util.List;
import java.util.Random;
import moze_intel.projecte.api.capabilities.item.IExtraFunction;
import moze_intel.projecte.capability.ExtraFunctionItemCapabilityWrapper;
import moze_intel.projecte.capability.ModeChangerItemCapabilityWrapper;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.EnumMatterType;
import moze_intel.projecte.gameObjs.PETags;
import moze_intel.projecte.gameObjs.items.IItemMode;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.ToolHelper;
import moze_intel.projecte.utils.text.ILangEntry;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PEKatar extends PETool implements IItemMode, IExtraFunction {
   private final ToolHelper.ChargeAttributeCache attributeCache = new ToolHelper.ChargeAttributeCache();
   private final ILangEntry[] modeDesc;

   public PEKatar(EnumMatterType matterType, int numCharges, Item.Properties props) {
      super(matterType, PETags.Blocks.MINEABLE_WITH_PE_KATAR, 19.0F, -2.4F, numCharges, props);
      this.modeDesc = new ILangEntry[]{PELang.MODE_KATAR_1, PELang.MODE_KATAR_2};
      this.addItemCapability(ModeChangerItemCapabilityWrapper::new);
      this.addItemCapability(ExtraFunctionItemCapabilityWrapper::new);
   }

   public ILangEntry[] getModeLangEntries() {
      return this.modeDesc;
   }

   public void m_7373_(@NotNull ItemStack stack, @Nullable Level level, @NotNull List tooltips, @NotNull TooltipFlag flags) {
      super.m_7373_(stack, level, tooltips, flags);
      tooltips.add(this.getToolTip(stack));
   }

   public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
      return ToolActions.DEFAULT_AXE_ACTIONS.contains(toolAction) || ToolActions.DEFAULT_SHEARS_ACTIONS.contains(toolAction) || ToolActions.DEFAULT_SWORD_ACTIONS.contains(toolAction) || ToolActions.DEFAULT_HOE_ACTIONS.contains(toolAction) || ToolHelper.DEFAULT_PE_KATAR_ACTIONS.contains(toolAction);
   }

   public @NotNull AABB getSweepHitBox(@NotNull ItemStack stack, @NotNull Player player, @NotNull Entity target) {
      int charge = this.getCharge(stack);
      return target.m_20191_().m_82377_((double)charge, (double)charge / 4.0, (double)charge);
   }

   protected float getShortCutDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
      float destroySpeed = super.getShortCutDestroySpeed(stack, state);
      return destroySpeed == 1.0F && state.m_204336_(BlockTags.f_278398_) ? 1.5F : destroySpeed;
   }

   public @NotNull InteractionResult m_6225_(UseOnContext context) {
      Player player = context.m_43723_();
      if (player == null) {
         return InteractionResult.PASS;
      } else {
         Level level = context.m_43725_();
         BlockPos pos = context.m_8083_();
         BlockState state = level.m_8055_(pos);
         return ToolHelper.performActions(ToolHelper.stripLogsAOE(context, state, 0L), () -> {
            return ToolHelper.scrapeAOE(context, state, 0L);
         }, () -> {
            return ToolHelper.waxOffAOE(context, state, 0L);
         }, () -> {
            return ToolHelper.tillAOE(context, state, 0L);
         }, () -> {
            return state.m_204336_(BlockTags.f_13106_) ? ToolHelper.clearTagAOE(level, player, context.m_43724_(), context.m_43722_(), 0L, BlockTags.f_13106_) : InteractionResult.PASS;
         }, () -> {
            return state.m_204336_(BlockTags.f_13035_) ? ToolHelper.clearTagAOE(level, player, context.m_43724_(), context.m_43722_(), 0L, BlockTags.f_13035_) : InteractionResult.PASS;
         });
      }
   }

   public boolean m_7579_(@NotNull ItemStack stack, @NotNull LivingEntity damaged, @NotNull LivingEntity damager) {
      ToolHelper.attackWithCharge(stack, damaged, damager, 1.0F);
      return true;
   }

   public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
      return ToolHelper.shearBlock(stack, pos, player).m_19077_();
   }

   public @NotNull InteractionResultHolder m_7203_(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      return ItemHelper.actionResultFromType(ToolHelper.shearEntityAOE(player, hand, 0L), player.m_21120_(hand));
   }

   public boolean doExtraFunction(@NotNull ItemStack stack, @NotNull Player player, InteractionHand hand) {
      if (player.m_36403_(0.0F) == 1.0F) {
         ToolHelper.attackAOE(stack, player, this.getMode(stack) == 1, ProjectEConfig.server.difficulty.katarDeathAura.get(), 0L, hand);
         PlayerHelper.resetCooldown(player);
         return true;
      } else {
         return false;
      }
   }

   public @NotNull UseAnim m_6164_(@NotNull ItemStack stack) {
      return UseAnim.BLOCK;
   }

   public int m_8105_(@NotNull ItemStack stack) {
      return 72000;
   }

   public @NotNull Multimap getAttributeModifiers(@NotNull EquipmentSlot slot, ItemStack stack) {
      return this.attributeCache.addChargeAttributeModifier(super.getAttributeModifiers(slot, stack), slot, stack);
   }

   public @NotNull InteractionResult m_6880_(@NotNull ItemStack stack, @NotNull Player player, @NotNull LivingEntity entity, @NotNull InteractionHand hand) {
      if (entity instanceof IForgeShearable target) {
         BlockPos pos = entity.m_20183_();
         if (target.isShearable(stack, entity.m_9236_(), pos)) {
            if (!entity.m_9236_().f_46443_) {
               List drops = target.onSheared(player, stack, entity.m_9236_(), pos, stack.getEnchantmentLevel(Enchantments.f_44987_));
               Random rand = new Random();
               drops.forEach((d) -> {
                  ItemEntity ent = entity.m_5552_(d, 1.0F);
                  if (ent != null) {
                     ent.m_20256_(ent.m_20184_().m_82520_((double)((rand.nextFloat() - rand.nextFloat()) * 0.1F), (double)(rand.nextFloat() * 0.05F), (double)((rand.nextFloat() - rand.nextFloat()) * 0.1F)));
                  }

               });
            }

            return InteractionResult.SUCCESS;
         }
      }

      return InteractionResult.PASS;
   }
}
