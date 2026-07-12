package moze_intel.projecte.gameObjs.items.tools;

import com.google.common.collect.Multimap;
import java.util.List;
import moze_intel.projecte.capability.ModeChangerItemCapabilityWrapper;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.EnumMatterType;
import moze_intel.projecte.gameObjs.PETags;
import moze_intel.projecte.gameObjs.items.IItemMode;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.ToolHelper;
import moze_intel.projecte.utils.text.ILangEntry;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.Tags.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PEMorningStar extends PETool implements IItemMode {
   private final ToolHelper.ChargeAttributeCache attributeCache = new ToolHelper.ChargeAttributeCache();
   private final ILangEntry[] modeDesc;

   public PEMorningStar(EnumMatterType matterType, int numCharges, Item.Properties props) {
      super(matterType, PETags.Blocks.MINEABLE_WITH_PE_MORNING_STAR, 16.0F, -3.0F, numCharges, props);
      this.modeDesc = new ILangEntry[]{PELang.MODE_MORNING_STAR_1, PELang.MODE_MORNING_STAR_2, PELang.MODE_MORNING_STAR_3, PELang.MODE_MORNING_STAR_4};
      this.addItemCapability(ModeChangerItemCapabilityWrapper::new);
   }

   public ILangEntry[] getModeLangEntries() {
      return this.modeDesc;
   }

   public void m_7373_(@NotNull ItemStack stack, @Nullable Level level, @NotNull List tooltips, @NotNull TooltipFlag flags) {
      super.m_7373_(stack, level, tooltips, flags);
      tooltips.add(this.getToolTip(stack));
   }

   public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
      return ToolActions.DEFAULT_PICKAXE_ACTIONS.contains(toolAction) || ToolActions.DEFAULT_SHOVEL_ACTIONS.contains(toolAction) || ToolHelper.DEFAULT_PE_HAMMER_ACTIONS.contains(toolAction) || ToolHelper.DEFAULT_PE_MORNING_STAR_ACTIONS.contains(toolAction);
   }

   public boolean m_7579_(@NotNull ItemStack stack, @NotNull LivingEntity damaged, @NotNull LivingEntity damager) {
      ToolHelper.attackWithCharge(stack, damaged, damager, 1.0F);
      return true;
   }

   public boolean m_6813_(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity living) {
      ToolHelper.digBasedOnMode(stack, level, pos, living, (x$0, x$1, x$2) -> {
         return Item.m_41435_(x$0, x$1, x$2);
      });
      return true;
   }

   public @NotNull InteractionResult m_6225_(UseOnContext context) {
      Player player = context.m_43723_();
      if (player == null) {
         return InteractionResult.PASS;
      } else {
         InteractionHand hand = context.m_43724_();
         Level level = context.m_43725_();
         BlockPos pos = context.m_8083_();
         Direction sideHit = context.m_43719_();
         ItemStack stack = context.m_43722_();
         BlockState state = level.m_8055_(pos);
         return ToolHelper.performActions(ToolHelper.flattenAOE(context, state, 0L), () -> {
            return ToolHelper.dowseCampfire(context, state);
         }, () -> {
            if (!state.m_204336_(Blocks.GRAVEL) && state.m_60734_() != net.minecraft.world.level.block.Blocks.f_50129_) {
               return InteractionResult.PASS;
            } else {
               return ProjectEConfig.server.items.pickaxeAoeVeinMining.get() ? ToolHelper.digAOE(level, player, hand, stack, pos, sideHit, false, 0L) : ToolHelper.tryVeinMine(player, stack, pos, sideHit);
            }
         }, () -> {
            return ItemHelper.isOre(state) && !ProjectEConfig.server.items.pickaxeAoeVeinMining.get() ? ToolHelper.tryVeinMine(player, stack, pos, sideHit) : InteractionResult.PASS;
         }, () -> {
            return ToolHelper.digAOE(level, player, hand, stack, pos, sideHit, !(state.m_60734_() instanceof GrassBlock) && !state.m_204336_(BlockTags.f_13029_) && !state.m_204336_(BlockTags.f_144274_), 0L);
         });
      }
   }

   public @NotNull InteractionResultHolder m_7203_(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      ItemStack stack = player.m_21120_(hand);
      return ProjectEConfig.server.items.pickaxeAoeVeinMining.get() ? ItemHelper.actionResultFromType(ToolHelper.mineOreVeinsInAOE(player, hand), stack) : InteractionResultHolder.m_19098_(stack);
   }

   public float m_8102_(@NotNull ItemStack stack, @NotNull BlockState state) {
      return ToolHelper.canMatterMine(this.matterType, state.m_60734_()) ? 1200000.0F : super.m_8102_(stack, state) + 48.0F;
   }

   public @NotNull Multimap getAttributeModifiers(@NotNull EquipmentSlot slot, ItemStack stack) {
      return this.attributeCache.addChargeAttributeModifier(super.getAttributeModifiers(slot, stack), slot, stack);
   }
}
