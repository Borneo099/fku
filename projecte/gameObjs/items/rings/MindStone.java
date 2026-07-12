package moze_intel.projecte.gameObjs.items.rings;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import moze_intel.projecte.api.block_entity.IDMPedestal;
import moze_intel.projecte.api.capabilities.item.IPedestalItem;
import moze_intel.projecte.capability.PedestalItemCapabilityWrapper;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MindStone extends PEToggleItem implements IPedestalItem {
   private static final int TRANSFER_RATE = 50;

   public MindStone(Item.Properties props) {
      super(props);
      this.addItemCapability(PedestalItemCapabilityWrapper::new);
   }

   public void m_6883_(@NotNull ItemStack stack, Level level, @NotNull Entity entity, int slot, boolean held) {
      if (!level.f_46443_ && slot < Inventory.m_36059_() && entity instanceof Player player) {
         super.m_6883_(stack, level, entity, slot, held);
         if (ItemHelper.checkItemNBT(stack, "Active") && this.getXP(player) > 0) {
            int toAdd = Math.min(this.getXP(player), 50);
            this.addStoredXP(stack, toAdd);
            this.removeXP(player, 50);
         }

      }
   }

   public @NotNull InteractionResultHolder m_7203_(Level level, Player player, @NotNull InteractionHand hand) {
      ItemStack stack = player.m_21120_(hand);
      if (!level.f_46443_ && !stack.m_41784_().m_128471_("Active") && this.getStoredXP(stack) != 0) {
         int toAdd = this.removeStoredXP(stack, 50);
         if (toAdd > 0) {
            this.addXP(player, toAdd);
         }
      }

      return InteractionResultHolder.m_19090_(stack);
   }

   public void m_7373_(@NotNull ItemStack stack, @Nullable Level level, @NotNull List tooltips, @NotNull TooltipFlag flags) {
      super.m_7373_(stack, level, tooltips, flags);
      if (stack.m_41782_()) {
         tooltips.add(PELang.TOOLTIP_STORED_XP.translateColored(ChatFormatting.DARK_GREEN, new Object[]{ChatFormatting.GREEN, String.format("%,d", this.getStoredXP(stack))}));
      }

   }

   private void removeXP(Player player, int amount) {
      int totalExperience = this.getXP(player) - amount;
      if (totalExperience < 0) {
         player.f_36079_ = 0;
         player.f_36078_ = 0;
         player.f_36080_ = 0.0F;
      } else {
         player.f_36079_ = totalExperience;
         player.f_36078_ = this.getLvlForXP(totalExperience);
         player.f_36080_ = (float)(totalExperience - this.getXPForLvl(player.f_36078_)) / (float)player.m_36323_();
      }

   }

   private void addXP(Player player, int amount) {
      int experiencetotal = this.getXP(player) + amount;
      player.f_36079_ = experiencetotal;
      player.f_36078_ = this.getLvlForXP(experiencetotal);
      player.f_36080_ = (float)(experiencetotal - this.getXPForLvl(player.f_36078_)) / (float)player.m_36323_();
   }

   private int getXP(Player player) {
      return (int)((float)this.getXPForLvl(player.f_36078_) + player.f_36080_ * (float)player.m_36323_());
   }

   private int getXPForLvl(int level) {
      if (level < 0) {
         return Integer.MAX_VALUE;
      } else if (level <= 16) {
         return level * level + 6 * level;
      } else {
         return level <= 31 ? (int)((double)(level * level) * 2.5 - 40.5 * (double)level + 360.0) : (int)((double)(level * level) * 4.5 - 162.5 * (double)level + 2220.0);
      }
   }

   private int getLvlForXP(int totalXP) {
      int result;
      for(result = 0; this.getXPForLvl(result) <= totalXP; ++result) {
      }

      --result;
      return result;
   }

   private int getStoredXP(ItemStack stack) {
      return stack.m_41782_() ? stack.m_41784_().m_128451_("StoredXP") : 0;
   }

   private void setStoredXP(ItemStack stack, int XP) {
      stack.m_41784_().m_128405_("StoredXP", XP);
   }

   private void addStoredXP(ItemStack stack, int XP) {
      long result = (long)this.getStoredXP(stack) + (long)XP;
      if (result > 2147483647L) {
         result = 2147483647L;
      }

      this.setStoredXP(stack, (int)result);
   }

   private int removeStoredXP(ItemStack stack, int XP) {
      int currentXP = this.getStoredXP(stack);
      int result;
      int returnResult;
      if (currentXP < XP) {
         result = 0;
         returnResult = currentXP;
      } else {
         result = currentXP - XP;
         returnResult = XP;
      }

      this.setStoredXP(stack, result);
      return returnResult;
   }

   public boolean updateInPedestal(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockEntity pedestal) {
      boolean sucked = false;
      Iterator var6 = level.m_45976_(ExperienceOrb.class, ((IDMPedestal)pedestal).getEffectBounds()).iterator();

      while(var6.hasNext()) {
         ExperienceOrb orb = (ExperienceOrb)var6.next();
         WorldHelper.gravitateEntityTowards(orb, (double)pos.m_123341_() + 0.5, (double)pos.m_123342_() + 0.5, (double)pos.m_123343_() + 0.5);
         if (!level.f_46443_ && orb.m_20275_((double)pos.m_123341_() + 0.5, (double)pos.m_123342_() + 0.5, (double)pos.m_123343_() + 0.5) < 1.21) {
            this.suckXP(orb, stack);
            sucked = true;
         }
      }

      return sucked;
   }

   private void suckXP(ExperienceOrb orb, ItemStack mindStone) {
      long l = (long)this.getStoredXP(mindStone);
      if (l + (long)orb.f_20770_ > 2147483647L) {
         orb.f_20770_ = (int)(l + (long)orb.f_20770_ - 2147483647L);
         this.setStoredXP(mindStone, Integer.MAX_VALUE);
      } else {
         this.addStoredXP(mindStone, orb.f_20770_);
         orb.m_146870_();
      }

   }

   public @NotNull List getPedestalDescription() {
      return Lists.newArrayList(new Component[]{PELang.PEDESTAL_MIND_STONE.translateColored(ChatFormatting.BLUE, new Object[0])});
   }
}
