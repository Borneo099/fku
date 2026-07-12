package moze_intel.projecte.gameObjs.items.tools;

import java.util.function.Consumer;
import moze_intel.projecte.api.capabilities.item.IItemCharge;
import moze_intel.projecte.capability.ChargeItemCapabilityWrapper;
import moze_intel.projecte.capability.ItemCapability;
import moze_intel.projecte.capability.ItemCapabilityWrapper;
import moze_intel.projecte.gameObjs.EnumMatterType;
import moze_intel.projecte.gameObjs.items.IBarHelper;
import moze_intel.projecte.utils.ToolHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;

public class PEHoe extends HoeItem implements IItemCharge, IBarHelper {
   private final EnumMatterType matterType;
   private final int numCharges;

   public PEHoe(EnumMatterType matterType, int numCharges, Item.Properties props) {
      super(matterType, (int)(-matterType.m_6631_()), (float)matterType.getMatterTier(), props);
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
      return ToolHelper.getDestroySpeed(super.m_8102_(stack, state), this.matterType, this.getCharge(stack));
   }

   public int getNumCharges(@NotNull ItemStack stack) {
      return this.numCharges;
   }

   public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
      return new ItemCapabilityWrapper(stack, new ItemCapability[]{new ChargeItemCapabilityWrapper()});
   }

   public @NotNull InteractionResult m_6225_(@NotNull UseOnContext context) {
      return ToolHelper.tillAOE(context, context.m_43725_().m_8055_(context.m_8083_()), 0L);
   }
}
