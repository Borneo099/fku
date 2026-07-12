package moze_intel.projecte.gameObjs.items.rings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import moze_intel.projecte.api.block_entity.IDMPedestal;
import moze_intel.projecte.api.capabilities.item.IPedestalItem;
import moze_intel.projecte.capability.PedestalItemCapabilityWrapper;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.registries.PESoundEvents;
import moze_intel.projecte.handlers.InternalTimers;
import moze_intel.projecte.integration.IntegrationHelper;
import moze_intel.projecte.utils.MathUtils;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class SoulStone extends PEToggleItem implements IPedestalItem {
   public SoulStone(Item.Properties props) {
      super(props);
      this.addItemCapability(PedestalItemCapabilityWrapper::new);
      this.addItemCapability("curios", IntegrationHelper.CURIO_CAP_SUPPLIER);
   }

   public void m_6883_(@NotNull ItemStack stack, Level level, @NotNull Entity entity, int slot, boolean held) {
      if (!level.f_46443_ && slot < Inventory.m_36059_() && entity instanceof Player player) {
         super.m_6883_(stack, level, entity, slot, held);
         CompoundTag nbt = stack.m_41784_();
         if (nbt.m_128471_("Active")) {
            if (getEmc(stack) < 64L && !consumeFuel(player, stack, 64L, false)) {
               nbt.m_128379_("Active", false);
            } else {
               player.getCapability(InternalTimers.CAPABILITY, (Direction)null).ifPresent((timers) -> {
                  timers.activateHeal();
                  if (player.m_21223_() < player.m_21233_() && timers.canHeal()) {
                     level.m_6263_((Player)null, player.m_20185_(), player.m_20186_(), player.m_20189_(), (SoundEvent)PESoundEvents.HEAL.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                     player.m_5634_(2.0F);
                     removeEmc(stack, 64L);
                  }

               });
            }
         }

      }
   }

   public boolean updateInPedestal(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockEntity pedestal) {
      if (!level.f_46443_ && ProjectEConfig.server.cooldown.pedestal.soul.get() != -1) {
         if (((IDMPedestal)pedestal).getActivityCooldown() == 0) {
            Iterator var5 = level.m_6443_(ServerPlayer.class, ((IDMPedestal)pedestal).getEffectBounds(), (ent) -> {
               return !ent.m_5833_() && ent.m_21223_() < ent.m_21233_();
            }).iterator();

            while(var5.hasNext()) {
               ServerPlayer player = (ServerPlayer)var5.next();
               level.m_6263_((Player)null, player.m_20185_(), player.m_20186_(), player.m_20189_(), (SoundEvent)PESoundEvents.HEAL.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
               player.m_5634_(1.0F);
            }

            ((IDMPedestal)pedestal).setActivityCooldown(ProjectEConfig.server.cooldown.pedestal.soul.get());
         } else {
            ((IDMPedestal)pedestal).decrementActivityCooldown();
         }
      }

      return false;
   }

   public @NotNull List getPedestalDescription() {
      List list = new ArrayList();
      if (ProjectEConfig.server.cooldown.pedestal.soul.get() != -1) {
         list.add(PELang.PEDESTAL_SOUL_STONE_1.translateColored(ChatFormatting.BLUE, new Object[0]));
         list.add(PELang.PEDESTAL_SOUL_STONE_2.translateColored(ChatFormatting.BLUE, new Object[]{MathUtils.tickToSecFormatted(ProjectEConfig.server.cooldown.pedestal.soul.get())}));
      }

      return list;
   }
}
