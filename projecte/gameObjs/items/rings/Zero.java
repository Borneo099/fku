package moze_intel.projecte.gameObjs.items.rings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import moze_intel.projecte.api.block_entity.IDMPedestal;
import moze_intel.projecte.api.capabilities.item.IItemCharge;
import moze_intel.projecte.api.capabilities.item.IPedestalItem;
import moze_intel.projecte.capability.ChargeItemCapabilityWrapper;
import moze_intel.projecte.capability.PedestalItemCapabilityWrapper;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.items.IBarHelper;
import moze_intel.projecte.gameObjs.registries.PESoundEvents;
import moze_intel.projecte.integration.IntegrationHelper;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.MathUtils;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public class Zero extends PEToggleItem implements IPedestalItem, IItemCharge, IBarHelper {
   public Zero(Item.Properties props) {
      super(props);
      this.addItemCapability(PedestalItemCapabilityWrapper::new);
      this.addItemCapability(ChargeItemCapabilityWrapper::new);
      this.addItemCapability("curios", IntegrationHelper.CURIO_CAP_SUPPLIER);
   }

   public boolean hasCraftingRemainingItem(ItemStack stack) {
      return true;
   }

   public ItemStack getCraftingRemainingItem(ItemStack stack) {
      return stack.m_41777_();
   }

   public void m_6883_(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean held) {
      super.m_6883_(stack, level, entity, slot, held);
      if (!level.f_46443_ && entity instanceof Player && slot < Inventory.m_36059_() && ItemHelper.checkItemNBT(stack, "Active")) {
         AABB box = new AABB(entity.m_20185_() - 3.0, entity.m_20186_() - 3.0, entity.m_20189_() - 3.0, entity.m_20185_() + 3.0, entity.m_20186_() + 3.0, entity.m_20189_() + 3.0);
         WorldHelper.freezeInBoundingBox(level, box, (Player)entity, true);
      }

   }

   public @NotNull InteractionResultHolder m_7203_(Level level, Player player, @NotNull InteractionHand hand) {
      ItemStack stack = player.m_21120_(hand);
      if (!level.f_46443_) {
         int offset = 3 + this.getCharge(stack);
         AABB box = player.m_20191_().m_82400_((double)offset);
         level.m_6263_((Player)null, player.m_20185_(), player.m_20186_(), player.m_20189_(), (SoundEvent)PESoundEvents.POWER.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
         WorldHelper.freezeInBoundingBox(level, box, player, false);
      }

      return InteractionResultHolder.m_19090_(stack);
   }

   public boolean updateInPedestal(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockEntity pedestal) {
      if (!level.f_46443_ && ProjectEConfig.server.cooldown.pedestal.zero.get() != -1) {
         if (((IDMPedestal)pedestal).getActivityCooldown() == 0) {
            AABB aabb = ((IDMPedestal)pedestal).getEffectBounds();
            WorldHelper.freezeInBoundingBox(level, aabb, (Player)null, false);
            Iterator var6 = level.m_6443_(Entity.class, aabb, (e) -> {
               return !e.m_5833_() && e.m_6060_();
            }).iterator();

            while(var6.hasNext()) {
               Entity ent = (Entity)var6.next();
               ent.m_20095_();
            }

            ((IDMPedestal)pedestal).setActivityCooldown(ProjectEConfig.server.cooldown.pedestal.zero.get());
         } else {
            ((IDMPedestal)pedestal).decrementActivityCooldown();
         }
      }

      return false;
   }

   public @NotNull List getPedestalDescription() {
      List list = new ArrayList();
      if (ProjectEConfig.server.cooldown.pedestal.zero.get() != -1) {
         list.add(PELang.PEDESTAL_ZERO_1.translateColored(ChatFormatting.BLUE, new Object[0]));
         list.add(PELang.PEDESTAL_ZERO_2.translateColored(ChatFormatting.BLUE, new Object[0]));
         list.add(PELang.PEDESTAL_ZERO_3.translateColored(ChatFormatting.BLUE, new Object[]{MathUtils.tickToSecFormatted(ProjectEConfig.server.cooldown.pedestal.zero.get())}));
      }

      return list;
   }

   public int getNumCharges(@NotNull ItemStack stack) {
      return 4;
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
}
