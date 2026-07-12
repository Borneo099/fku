package lexis.Hack.Hacks.World;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraft.core.BlockPos;

public class AntiKickMineHack extends Hack {
   private int maxBlocks = 3;
   private long resetTime = 1000L;
   private Queue breakTimes = new LinkedList();
   private boolean isBlocked = false;
   private HackConfig config;

   public AntiKickMineHack() {
      super("晓过防踢opmod", "烦狗opmod(网易)触发nuker踢出，可以自动防止踢出！", Hack.Category.WORLD, true);
      this.addSetting(new Hack.Setting("最大数量", "在时间内最大破坏数量", 3, 1, 10));
      this.addSetting(new Hack.Setting("重置时间", "重置计数的时间(毫秒)", 1000, 500, 5000));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.maxBlocks = this.config.getIntSetting("晓过防踢opmod", "最大数量", 3);
      this.resetTime = (long)this.config.getIntSetting("晓过防踢opmod", "重置时间", 1000);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "最大数量":
               setting.setValue(this.maxBlocks);
               break;
            case "重置时间":
               setting.setValue((int)this.resetTime);
         }
      }

   }

   public void onEnable() {
      this.breakTimes.clear();
      this.isBlocked = false;
   }

   public void onDisable() {
      this.breakTimes.clear();
      this.isBlocked = false;
   }

   public void onUpdate() {
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "最大数量":
               this.maxBlocks = setting.getInt();
               break;
            case "重置时间":
               this.resetTime = (long)setting.getInt();
         }
      }

      long currentTime = System.currentTimeMillis();

      while(!this.breakTimes.isEmpty() && currentTime - (Long)this.breakTimes.peek() > this.resetTime) {
         this.breakTimes.poll();
      }

      if (this.breakTimes.size() >= this.maxBlocks) {
         this.isBlocked = true;
      } else {
         this.isBlocked = false;
      }

   }

   public void onClick() {
      this.toggle();
   }

   public boolean shouldCancelBreak(BlockPos pos) {
      if (!this.isEnabled()) {
         return false;
      } else {
         long currentTime = System.currentTimeMillis();

         while(!this.breakTimes.isEmpty() && currentTime - (Long)this.breakTimes.peek() > this.resetTime) {
            this.breakTimes.poll();
         }

         if (this.breakTimes.size() >= this.maxBlocks) {
            return true;
         } else {
            this.breakTimes.offer(currentTime);
            return false;
         }
      }
   }

   public int getCurrentCount() {
      return this.breakTimes.size();
   }

   public int getMaxBlocks() {
      return this.maxBlocks;
   }

   public long getResetTime() {
      return this.resetTime;
   }
}
