package moze_intel.projecte.gameObjs.items.rings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import moze_intel.projecte.api.block_entity.IDMPedestal;
import moze_intel.projecte.api.capabilities.item.IPedestalItem;
import moze_intel.projecte.api.capabilities.item.IProjectileShooter;
import moze_intel.projecte.capability.PedestalItemCapabilityWrapper;
import moze_intel.projecte.capability.ProjectileShooterItemCapabilityWrapper;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.entity.EntityFireProjectile;
import moze_intel.projecte.gameObjs.items.IFireProtector;
import moze_intel.projecte.integration.IntegrationHelper;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.MathUtils;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class Ignition extends PEToggleItem implements IPedestalItem, IFireProtector, IProjectileShooter {
   public Ignition(Item.Properties props) {
      super(props);
      this.addItemCapability(PedestalItemCapabilityWrapper::new);
      this.addItemCapability(ProjectileShooterItemCapabilityWrapper::new);
      this.addItemCapability("curios", IntegrationHelper.CURIO_CAP_SUPPLIER);
   }

   public void m_6883_(@NotNull ItemStack stack, Level level, @NotNull Entity entity, int inventorySlot, boolean held) {
      if (!level.f_46443_ && inventorySlot < Inventory.m_36059_() && entity instanceof Player player) {
         super.m_6883_(stack, level, entity, inventorySlot, held);
         CompoundTag nbt = stack.m_41784_();
         if (nbt.m_128471_("Active")) {
            if (getEmc(stack) == 0L && !consumeFuel(player, stack, 64L, false)) {
               nbt.m_128379_("Active", false);
            } else {
               WorldHelper.igniteNearby(level, player);
               removeEmc(stack, EMCHelper.removeFractionalEMC(stack, 0.3199999928474426));
            }
         } else {
            WorldHelper.extinguishNearby(level, player);
         }

      }
   }

   public @NotNull InteractionResult m_6225_(@NotNull UseOnContext ctx) {
      return WorldHelper.igniteBlock(ctx);
   }

   public boolean updateInPedestal(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockEntity pedestal) {
      if (!level.f_46443_ && ProjectEConfig.server.cooldown.pedestal.ignition.get() != -1) {
         if (((IDMPedestal)pedestal).getActivityCooldown() == 0) {
            DamageSource fire = level.m_269111_().m_269387_();
            Iterator var6 = level.m_45976_(Mob.class, ((IDMPedestal)pedestal).getEffectBounds()).iterator();

            while(var6.hasNext()) {
               Mob living = (Mob)var6.next();
               living.m_6469_(fire, 3.0F);
               living.m_20254_(8);
            }

            ((IDMPedestal)pedestal).setActivityCooldown(ProjectEConfig.server.cooldown.pedestal.ignition.get());
         } else {
            ((IDMPedestal)pedestal).decrementActivityCooldown();
         }
      }

      return false;
   }

   public @NotNull List getPedestalDescription() {
      List list = new ArrayList();
      if (ProjectEConfig.server.cooldown.pedestal.ignition.get() != -1) {
         list.add(PELang.PEDESTAL_IGNITION_1.translateColored(ChatFormatting.BLUE, new Object[0]));
         list.add(PELang.PEDESTAL_IGNITION_2.translateColored(ChatFormatting.BLUE, new Object[]{MathUtils.tickToSecFormatted(ProjectEConfig.server.cooldown.pedestal.ignition.get())}));
      }

      return list;
   }

   public boolean shootProjectile(@NotNull Player player, @NotNull ItemStack stack, InteractionHand hand) {
      Level level = player.m_9236_();
      if (level.f_46443_) {
         return false;
      } else {
         EntityFireProjectile fire = new EntityFireProjectile(player, false, level);
         fire.m_37251_(player, player.m_146909_(), player.m_146908_(), 0.0F, 1.5F, 1.0F);
         level.m_7967_(fire);
         return true;
      }
   }

   public boolean canProtectAgainstFire(ItemStack stack, ServerPlayer player) {
      return true;
   }
}
