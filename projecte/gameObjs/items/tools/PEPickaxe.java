package moze_intel.projecte.gameObjs.items.tools;

import java.util.List;
import java.util.function.Consumer;
import moze_intel.projecte.api.capabilities.item.IItemCharge;
import moze_intel.projecte.capability.ChargeItemCapabilityWrapper;
import moze_intel.projecte.capability.ItemCapability;
import moze_intel.projecte.capability.ItemCapabilityWrapper;
import moze_intel.projecte.capability.ModeChangerItemCapabilityWrapper;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.EnumMatterType;
import moze_intel.projecte.gameObjs.items.IBarHelper;
import moze_intel.projecte.gameObjs.items.IItemMode;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.ToolHelper;
import moze_intel.projecte.utils.text.ILangEntry;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PEPickaxe extends PickaxeItem implements IItemCharge, IItemMode, IBarHelper {
   private final EnumMatterType matterType;
   private final ILangEntry[] modeDesc;
   private final int numCharges;

   public PEPickaxe(EnumMatterType matterType, int numCharges, Item.Properties props) {
      super(matterType, 4, -2.8F, props);
      this.modeDesc = new ILangEntry[]{PELang.MODE_PICK_1, PELang.MODE_PICK_2, PELang.MODE_PICK_3, PELang.MODE_PICK_4};
      this.matterType = matterType;
      this.numCharges = numCharges;
   }

   public boolean m_8120_(@NotNull ItemStack stack) {
      return false;
   }

   public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
      return false;
   }

   public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
      return false;
   }

   public int damageItem(ItemStack stack, int amount, LivingEntity entity, Consumer onBroken) {
      return 0;
   }

   public boolean m_142522_(@NotNull ItemStack stack) {
      return true;
   }

   public float getWidthForBar(ItemStack stack) {
      return 1.0F - this.getChargePercent(stack);
   }

   public int m_142158_(@NotNull ItemStack stack) {
      return this.getScaledBarWidth(stack);
   }

   public int m_142159_(@NotNull ItemStack stack) {
      return this.getColorForBar(stack);
   }

   public float m_8102_(@NotNull ItemStack stack, @NotNull BlockState state) {
      return ToolHelper.canMatterMine(this.matterType, state.m_60734_()) ? 1200000.0F : ToolHelper.getDestroySpeed(super.m_8102_(stack, state), this.matterType, this.getCharge(stack));
   }

   public int getNumCharges(@NotNull ItemStack stack) {
      return this.numCharges;
   }

   public ILangEntry[] getModeLangEntries() {
      return this.modeDesc;
   }

   public void m_7373_(@NotNull ItemStack stack, @Nullable Level level, @NotNull List tooltips, @NotNull TooltipFlag flags) {
      super.m_7373_(stack, level, tooltips, flags);
      tooltips.add(this.getToolTip(stack));
   }

   public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
      return new ItemCapabilityWrapper(stack, new ItemCapability[]{new ChargeItemCapabilityWrapper(), new ModeChangerItemCapabilityWrapper()});
   }

   public @NotNull InteractionResultHolder m_7203_(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      ItemStack stack = player.m_21120_(hand);
      return ProjectEConfig.server.items.pickaxeAoeVeinMining.get() ? ItemHelper.actionResultFromType(ToolHelper.mineOreVeinsInAOE(player, hand), stack) : InteractionResultHolder.m_19098_(stack);
   }

   public @NotNull InteractionResult m_6225_(UseOnContext context) {
      Player player = context.m_43723_();
      if (player != null && !ProjectEConfig.server.items.pickaxeAoeVeinMining.get()) {
         BlockPos pos = context.m_8083_();
         return ItemHelper.isOre(context.m_43725_().m_8055_(pos)) ? ToolHelper.tryVeinMine(player, context.m_43722_(), pos, context.m_43719_()) : InteractionResult.PASS;
      } else {
         return InteractionResult.PASS;
      }
   }

   public boolean m_6813_(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity living) {
      ToolHelper.digBasedOnMode(stack, level, pos, living, (x$0, x$1, x$2) -> {
         return Item.m_41435_(x$0, x$1, x$2);
      });
      return true;
   }
}
