package moze_intel.projecte.gameObjs.items.tools;

import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import moze_intel.projecte.api.capabilities.item.IExtraFunction;
import moze_intel.projecte.api.capabilities.item.IItemCharge;
import moze_intel.projecte.capability.ChargeItemCapabilityWrapper;
import moze_intel.projecte.capability.ExtraFunctionItemCapabilityWrapper;
import moze_intel.projecte.capability.ItemCapabilityWrapper;
import moze_intel.projecte.gameObjs.EnumMatterType;
import moze_intel.projecte.gameObjs.PETags;
import moze_intel.projecte.gameObjs.items.IBarHelper;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.ToolHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;

public class PESword extends SwordItem implements IExtraFunction, IItemCharge, IBarHelper {
   private final List supportedCapabilities = new ArrayList();
   private final ToolHelper.ChargeAttributeCache attributeCache = new ToolHelper.ChargeAttributeCache();
   private final EnumMatterType matterType;
   private final int numCharges;

   public PESword(EnumMatterType matterType, int numCharges, int damage, Item.Properties props) {
      super(matterType, damage, -2.4F, props);
      this.matterType = matterType;
      this.numCharges = numCharges;
      this.addItemCapability(ChargeItemCapabilityWrapper::new);
      this.addItemCapability(ExtraFunctionItemCapabilityWrapper::new);
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
      float speed = super.m_8102_(stack, state);
      if (speed == 1.0F && state.m_204336_(PETags.Blocks.MINEABLE_WITH_PE_SWORD)) {
         speed = this.matterType.m_6624_();
      }

      return ToolHelper.getDestroySpeed(speed, this.matterType, this.getCharge(stack));
   }

   public boolean isCorrectToolForDrops(@NotNull ItemStack stack, BlockState state) {
      return state.m_204336_(PETags.Blocks.MINEABLE_WITH_PE_SWORD) && TierSortingRegistry.isCorrectTierForDrops(this.matterType, state);
   }

   public int getNumCharges(@NotNull ItemStack stack) {
      return this.numCharges;
   }

   public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
      return (ICapabilityProvider)(this.supportedCapabilities.isEmpty() ? super.initCapabilities(stack, nbt) : new ItemCapabilityWrapper(stack, this.supportedCapabilities));
   }

   public boolean m_7579_(@NotNull ItemStack stack, @NotNull LivingEntity damaged, @NotNull LivingEntity damager) {
      ToolHelper.attackWithCharge(stack, damaged, damager, 1.0F);
      return true;
   }

   public @NotNull AABB getSweepHitBox(@NotNull ItemStack stack, @NotNull Player player, @NotNull Entity target) {
      int charge = this.getCharge(stack);
      return target.m_20191_().m_82377_((double)charge, (double)charge / 4.0, (double)charge);
   }

   public boolean doExtraFunction(@NotNull ItemStack stack, @NotNull Player player, InteractionHand hand) {
      if (player.m_36403_(0.0F) == 1.0F) {
         ToolHelper.attackAOE(stack, player, this.slayAll(stack), this.m_43299_(), 0L, hand);
         PlayerHelper.resetCooldown(player);
         return true;
      } else {
         return false;
      }
   }

   protected boolean slayAll(@NotNull ItemStack stack) {
      return false;
   }

   public @NotNull Multimap getAttributeModifiers(@NotNull EquipmentSlot slot, ItemStack stack) {
      return this.attributeCache.addChargeAttributeModifier(super.getAttributeModifiers(slot, stack), slot, stack);
   }
}
