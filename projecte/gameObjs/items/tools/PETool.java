package moze_intel.projecte.gameObjs.items.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import moze_intel.projecte.api.capabilities.item.IItemCharge;
import moze_intel.projecte.capability.ChargeItemCapabilityWrapper;
import moze_intel.projecte.capability.ItemCapabilityWrapper;
import moze_intel.projecte.gameObjs.EnumMatterType;
import moze_intel.projecte.gameObjs.items.IBarHelper;
import moze_intel.projecte.utils.ToolHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;

public abstract class PETool extends DiggerItem implements IItemCharge, IBarHelper {
   private final List supportedCapabilities = new ArrayList();
   protected final EnumMatterType matterType;
   private final int numCharges;

   public PETool(EnumMatterType matterType, TagKey blocks, float damage, float attackSpeed, int numCharges, Item.Properties props) {
      super(damage, attackSpeed, matterType, blocks, props);
      this.matterType = matterType;
      this.numCharges = numCharges;
      this.addItemCapability(ChargeItemCapabilityWrapper::new);
   }

   protected void addItemCapability(Supplier capabilitySupplier) {
      this.supportedCapabilities.add(capabilitySupplier);
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
      return ToolHelper.getDestroySpeed(this.getShortCutDestroySpeed(stack, state), this.matterType, this.getCharge(stack));
   }

   public int getNumCharges(@NotNull ItemStack stack) {
      return this.numCharges;
   }

   public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
      return (ICapabilityProvider)(this.supportedCapabilities.isEmpty() ? super.initCapabilities(stack, nbt) : new ItemCapabilityWrapper(stack, this.supportedCapabilities));
   }

   protected float getShortCutDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
      return super.m_8102_(stack, state);
   }
}
