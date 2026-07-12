package lexis.Hack.Hacks.Blocks;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.events.BlockBreakingProgressEvent;
import lexis.Hack.events.BlockBreakingProgressListener;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.UpdateListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;

public class AutoToolHack extends Hack implements BlockBreakingProgressListener, UpdateListener {
   private boolean useSwords = false;
   private boolean useHands = true;
   private int repairMode = 0;
   private boolean switchBack = true;
   private int prevSelectedSlot = -1;
   private HackConfig config;

   public AutoToolHack() {
      super("自动工具", new String[]{"挖掘时自动切换到最佳工具", "§l注：其地客户端/服务端mod（更好的工具/自动工具）这肯定是会闪退！"}, Hack.Category.BLOCKS, true);
      this.addSetting(new Hack.Setting("使用剑", "用剑破坏树叶、蜘蛛网等", false));
      this.addSetting(new Hack.Setting("使用空手", "没有合适工具时使用空手", true));
      this.addSetting(new Hack.Setting("修复模式", "耐久度低于此值时停止使用工具 (0=关闭)", 0.0, 0.0, 100.0, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("切换回原槽位", "使用后自动切换回原物品", true));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.useSwords = this.config.getBooleanSetting("自动工具", "使用剑", false);
      this.useHands = this.config.getBooleanSetting("自动工具", "使用空手", true);
      this.repairMode = (int)this.config.getDoubleSetting("自动工具", "修复模式", 0.0);
      this.switchBack = this.config.getBooleanSetting("自动工具", "切换回原槽位", true);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "使用剑":
               setting.setValue(this.useSwords);
               break;
            case "使用空手":
               setting.setValue(this.useHands);
               break;
            case "修复模式":
               setting.setValue((double)this.repairMode);
               break;
            case "切换回原槽位":
               setting.setValue(this.switchBack);
         }
      }

   }

   public void onEnable() {
      EventManager.add(BlockBreakingProgressListener.class, this);
      EventManager.add(UpdateListener.class, this);
      this.prevSelectedSlot = -1;
   }

   public void onDisable() {
      EventManager.remove(BlockBreakingProgressListener.class, this);
      EventManager.remove(UpdateListener.class, this);
      this.prevSelectedSlot = -1;
   }

   public void onBlockBreakingProgress(BlockBreakingProgressEvent event) {
      if (mc.f_91074_ != null) {
         BlockPos pos = event.getBlockPos();
         if (this.prevSelectedSlot == -1) {
            this.prevSelectedSlot = mc.f_91074_.m_150109_().f_35977_;
         }

         this.onBlockBreaking(pos);
      }
   }

   public void onBlockBreaking(BlockPos pos) {
      if (this.isEnabled() && mc.f_91074_ != null) {
         if (this.prevSelectedSlot == -1) {
            this.prevSelectedSlot = mc.f_91074_.m_150109_().f_35977_;
         }

         this.equipBestTool(pos);
      }
   }

   public void onUpdate() {
      if (mc.f_91074_ != null) {
         Iterator var1 = this.getSettings().iterator();

         while(var1.hasNext()) {
            Hack.Setting setting = (Hack.Setting)var1.next();
            switch (setting.getName()) {
               case "使用剑":
                  this.useSwords = setting.getBoolean();
                  break;
               case "使用空手":
                  this.useHands = setting.getBoolean();
                  break;
               case "修复模式":
                  this.repairMode = (int)setting.getDouble();
                  break;
               case "切换回原槽位":
                  this.switchBack = setting.getBoolean();
            }
         }

         if (this.prevSelectedSlot != -1 && !mc.f_91072_.m_105296_()) {
            if (this.switchBack) {
               mc.f_91074_.m_150109_().f_35977_ = this.prevSelectedSlot;
            }

            this.prevSelectedSlot = -1;
         }

      }
   }

   private void equipBestTool(BlockPos pos) {
      LocalPlayer player = mc.f_91074_;
      Inventory inventory = player.m_150109_();
      BlockState state = mc.f_91073_.m_8055_(pos);
      int bestSlot = this.getBestSlot(state);
      if (bestSlot != -1) {
         inventory.f_35977_ = bestSlot;
      } else if (this.useHands) {
         this.selectFallbackSlot();
      }

   }

   private int getBestSlot(BlockState state) {
      Inventory inventory = mc.f_91074_.m_150109_();
      ItemStack currentItem = inventory.m_36056_();
      float bestSpeed = this.getMiningSpeed(currentItem, state);
      if (this.isTooDamaged(currentItem)) {
         bestSpeed = 1.0F;
      }

      int bestSlot = -1;

      for(int slot = 0; slot < 9; ++slot) {
         if (slot != inventory.f_35977_) {
            ItemStack stack = inventory.m_8020_(slot);
            float speed = this.getMiningSpeed(stack, state);
            boolean canUse = (this.useSwords || !(stack.m_41720_() instanceof SwordItem)) && !this.isTooDamaged(stack);
            if (speed > bestSpeed && canUse) {
               bestSpeed = speed;
               bestSlot = slot;
            }
         }
      }

      return bestSlot;
   }

   private float getMiningSpeed(ItemStack stack, BlockState state) {
      if (stack.m_41619_()) {
         return 1.0F;
      } else {
         float speed = stack.m_41691_(state);
         if (speed > 1.0F) {
            int efficiency = EnchantmentHelper.m_44843_(Enchantments.f_44984_, stack);
            if (efficiency > 0) {
               speed += (float)(efficiency * efficiency + 1);
            }
         }

         return speed;
      }
   }

   private boolean isTooDamaged(ItemStack stack) {
      if (!stack.m_41619_() && this.repairMode != 0) {
         int maxDamage = stack.m_41776_();
         int currentDamage = stack.m_41773_();
         return maxDamage - currentDamage <= this.repairMode;
      } else {
         return false;
      }
   }

   private void selectFallbackSlot() {
      Inventory inventory = mc.f_91074_.m_150109_();

      for(int slot = 0; slot < 9; ++slot) {
         if (slot != inventory.f_35977_) {
            ItemStack stack = inventory.m_8020_(slot);
            if (stack.m_41619_() || !stack.m_41720_().m_41465_()) {
               inventory.f_35977_ = slot;
               return;
            }
         }
      }

      if (inventory.f_35977_ == 8) {
         inventory.f_35977_ = 0;
      } else {
         ++inventory.f_35977_;
      }

   }

   public void onClick() {
      this.toggle();
   }
}
