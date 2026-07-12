package moze_intel.projecte.gameObjs.items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import moze_intel.projecte.api.block_entity.IDMPedestal;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.item.IAlchBagItem;
import moze_intel.projecte.api.capabilities.item.IAlchChestItem;
import moze_intel.projecte.api.capabilities.item.IPedestalItem;
import moze_intel.projecte.capability.AlchBagItemCapabilityWrapper;
import moze_intel.projecte.capability.AlchChestItemCapabilityWrapper;
import moze_intel.projecte.capability.PedestalItemCapabilityWrapper;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.block_entities.EmcBlockEntity;
import moze_intel.projecte.handlers.InternalTimers;
import moze_intel.projecte.integration.IntegrationHelper;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.MathUtils;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class RepairTalisman extends ItemPE implements IAlchBagItem, IAlchChestItem, IPedestalItem {
   private static final Predicate CAN_REPAIR_ITEM = (stack) -> {
      return !stack.m_41619_() && !stack.getCapability(PECapabilities.MODE_CHANGER_ITEM_CAPABILITY).isPresent() && ItemHelper.isRepairableDamagedItem(stack);
   };

   public RepairTalisman(Item.Properties props) {
      super(props);
      this.addItemCapability(AlchBagItemCapabilityWrapper::new);
      this.addItemCapability(AlchChestItemCapabilityWrapper::new);
      this.addItemCapability(PedestalItemCapabilityWrapper::new);
      this.addItemCapability("curios", IntegrationHelper.CURIO_CAP_SUPPLIER);
   }

   public void m_6883_(@NotNull ItemStack stack, Level level, @NotNull Entity entity, int invSlot, boolean isSelected) {
      if (!level.f_46443_ && entity instanceof Player player) {
         player.getCapability(InternalTimers.CAPABILITY).ifPresent((timers) -> {
            timers.activateRepair();
            if (timers.canRepair()) {
               repairAllItems(player);
            }

         });
      }

   }

   public boolean updateInPedestal(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockEntity pedestal) {
      if (!level.f_46443_ && ProjectEConfig.server.cooldown.pedestal.repair.get() != -1) {
         if (((IDMPedestal)pedestal).getActivityCooldown() == 0) {
            level.m_45976_(ServerPlayer.class, ((IDMPedestal)pedestal).getEffectBounds()).forEach(RepairTalisman::repairAllItems);
            ((IDMPedestal)pedestal).setActivityCooldown(ProjectEConfig.server.cooldown.pedestal.repair.get());
         } else {
            ((IDMPedestal)pedestal).decrementActivityCooldown();
         }
      }

      return false;
   }

   public @NotNull List getPedestalDescription() {
      List list = new ArrayList();
      if (ProjectEConfig.server.cooldown.pedestal.repair.get() != -1) {
         list.add(PELang.PEDESTAL_REPAIR_TALISMAN_1.translateColored(ChatFormatting.BLUE, new Object[0]));
         list.add(PELang.PEDESTAL_REPAIR_TALISMAN_2.translateColored(ChatFormatting.BLUE, new Object[]{MathUtils.tickToSecFormatted(ProjectEConfig.server.cooldown.pedestal.repair.get())}));
      }

      return list;
   }

   public boolean updateInAlchChest(@NotNull Level level, @NotNull BlockPos pos, @NotNull ItemStack stack) {
      if (!level.f_46443_) {
         EmcBlockEntity chest = (EmcBlockEntity)WorldHelper.getBlockEntity(EmcBlockEntity.class, level, pos, true);
         if (chest != null) {
            CompoundTag nbt = stack.m_41784_();
            byte coolDown = nbt.m_128445_("Cooldown");
            if (coolDown > 0) {
               nbt.m_128344_("Cooldown", (byte)(coolDown - 1));
            } else {
               chest.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent((inv) -> {
                  if (repairAllItems(inv, CAN_REPAIR_ITEM)) {
                     nbt.m_128344_("Cooldown", (byte)19);
                     chest.markDirty(false);
                  }

               });
            }
         }
      }

      return false;
   }

   public boolean updateInAlchBag(@NotNull IItemHandler inv, @NotNull Player player, @NotNull ItemStack stack) {
      if (player.m_9236_().f_46443_) {
         return false;
      } else {
         CompoundTag nbt = stack.m_41784_();
         byte coolDown = nbt.m_128445_("Cooldown");
         if (coolDown > 0) {
            nbt.m_128344_("Cooldown", (byte)(coolDown - 1));
         } else if (repairAllItems(inv, CAN_REPAIR_ITEM)) {
            nbt.m_128344_("Cooldown", (byte)19);
            return true;
         }

         return false;
      }
   }

   private static void repairAllItems(Player player) {
      Predicate canRepairPlayerItem = CAN_REPAIR_ITEM.and((stack) -> {
         return stack != player.m_21205_() || !player.f_20911_;
      });
      player.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent((inv) -> {
         repairAllItems(inv, canRepairPlayerItem);
      });
      IItemHandler curios = PlayerHelper.getCurios(player);
      if (curios != null) {
         repairAllItems(curios, canRepairPlayerItem);
      }

   }

   private static boolean repairAllItems(IItemHandler inv, Predicate canRepairStack) {
      boolean hasAction = false;

      for(int i = 0; i < inv.getSlots(); ++i) {
         ItemStack invStack = inv.getStackInSlot(i);
         if (canRepairStack.test(invStack)) {
            invStack.m_41721_(invStack.m_41773_() - 1);
            if (!hasAction) {
               hasAction = true;
            }
         }
      }

      return hasAction;
   }
}
