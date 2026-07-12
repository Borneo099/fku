package moze_intel.projecte.gameObjs.items.tools;

import java.util.function.Consumer;
import moze_intel.projecte.api.capabilities.item.IItemCharge;
import moze_intel.projecte.capability.ChargeItemCapabilityWrapper;
import moze_intel.projecte.capability.ItemCapability;
import moze_intel.projecte.capability.ItemCapabilityWrapper;
import moze_intel.projecte.gameObjs.EnumMatterType;
import moze_intel.projecte.gameObjs.PETags;
import moze_intel.projecte.gameObjs.items.IBarHelper;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.ToolHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;

public class PEShears extends ShearsItem implements IItemCharge, IBarHelper {
   private final EnumMatterType matterType;
   private final int numCharges;

   public PEShears(EnumMatterType matterType, int numCharges, Item.Properties props) {
      super(props);
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
      float speed = super.m_8102_(stack, state);
      if (speed == 1.0F && state.m_204336_(PETags.Blocks.MINEABLE_WITH_PE_SHEARS)) {
         speed = this.matterType.m_6624_();
      }

      return ToolHelper.getDestroySpeed(speed, this.matterType, this.getCharge(stack));
   }

   public int getNumCharges(@NotNull ItemStack stack) {
      return this.numCharges;
   }

   public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
      return new ItemCapabilityWrapper(stack, new ItemCapability[]{new ChargeItemCapabilityWrapper()});
   }

   public boolean isCorrectToolForDrops(@NotNull ItemStack stack, BlockState state) {
      return state.m_204336_(PETags.Blocks.MINEABLE_WITH_PE_SHEARS) && TierSortingRegistry.isCorrectTierForDrops(this.matterType, state);
   }

   public @NotNull InteractionResultHolder m_7203_(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      return ItemHelper.actionResultFromType(ToolHelper.shearEntityAOE(player, hand, 0L), player.m_21120_(hand));
   }

   public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
      return ToolHelper.shearBlock(stack, pos, player).m_19077_();
   }

   public @NotNull InteractionResult m_6225_(UseOnContext context) {
      Player player = context.m_43723_();
      if (player != null) {
         Level level = context.m_43725_();
         BlockState state = level.m_8055_(context.m_8083_());
         if (state.m_204336_(BlockTags.f_13035_)) {
            ToolHelper.clearTagAOE(level, player, context.m_43724_(), context.m_43722_(), 0L, BlockTags.f_13035_);
         }
      }

      return InteractionResult.PASS;
   }
}
