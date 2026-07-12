package lexis.Hack.Hacks.Misc;

import com.mojang.blaze3d.vertex.PoseStack;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import lexis.Client.Goto.PathFinder;
import lexis.Client.Goto.PathPos;
import lexis.Client.Goto.PathProcessor;
import lexis.Client.Goto.PathRenderer;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.RenderListener;
import lexis.Hack.events.UpdateListener;
import net.minecraft.core.BlockPos;

public class AntiAfkHack extends Hack implements UpdateListener, RenderListener {
   private HackConfig config;
   private static final String CONFIG_KEY = "防AFK";
   private int findRange = 5;
   private int restTime = 40;
   private int restTimer = 0;
   private Random random = new Random();
   private BlockPos startPos;
   private PathFinder pathFinder;
   private PathProcessor processor;
   private int pathFinderTimeout = 0;
   private static final int PATHFINDER_TIMEOUT = 100;
   private static AntiAfkHack instance;

   public AntiAfkHack() {
      super("防AFK", new String[]{"防止时长不动被踢出服务器"}, Hack.Category.MISC, true);
      this.addSetting(new Hack.Setting("找路范围", "最大找路距离 (格)", 5, 2, 128, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("休息时间", "到路后休息时间 (tick)", 40, 10, 200, Hack.ValueDisplay.INTEGER));
      this.config = HackConfig.getInstance();
      instance = this;
      this.loadConfig();
   }

   private void loadConfig() {
      this.findRange = (int)this.config.getDoubleSetting("防AFK", "找路范围", 5.0);
      this.restTime = (int)this.config.getDoubleSetting("防AFK", "休息时间", 40.0);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "找路范围":
               setting.setValue((double)this.findRange);
               break;
            case "休息时间":
               setting.setValue((double)this.restTime);
         }
      }

   }

   public static boolean isActive() {
      return instance != null && instance.isEnabled();
   }

   public void onEnable() {
      if (mc.f_91074_ == null) {
         this.setEnabled(false);
      } else {
         this.startPos = mc.f_91074_.m_20183_();
         this.pathFinder = null;
         this.processor = null;
         this.restTimer = 0;
         this.pathFinderTimeout = 0;
         EventManager.add(UpdateListener.class, this);
         EventManager.add(RenderListener.class, this);
         this.startNewPath();
      }
   }

   public void onDisable() {
      EventManager.remove(UpdateListener.class, this);
      EventManager.remove(RenderListener.class, this);
      this.stopMoving();
      this.pathFinder = null;
      this.processor = null;
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         switch (setting.getName()) {
            case "找路范围":
               int newRange = (int)setting.getDouble();
               if (newRange != this.findRange) {
                  this.findRange = newRange;
                  needSave = true;
               }
               break;
            case "休息时间":
               int newRest = (int)setting.getDouble();
               if (newRest != this.restTime) {
                  this.restTime = newRest;
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.config.saveHackSettings("防AFK", this.getSettings());
      }

      if (mc.f_91074_ != null) {
         if (mc.f_91074_.m_21223_() <= 0.0F) {
            this.setEnabled(false);
         } else {
            this.updateWithAi();
         }
      }
   }

   private void updateWithAi() {
      if (this.restTimer > 0) {
         --this.restTimer;
      } else {
         if (this.pathFinder != null && !this.pathFinder.isDone() && !this.pathFinder.isFailed()) {
            ++this.pathFinderTimeout;
            if (this.pathFinderTimeout > 100) {
               this.startNewPath();
               this.pathFinderTimeout = 0;
               return;
            }

            PathProcessor.lockControls();
            this.pathFinder.think();
            if (!this.pathFinder.isDone() && !this.pathFinder.isFailed()) {
               return;
            }

            this.pathFinder.formatPath();
            this.processor = this.pathFinder.getProcessor();
            this.pathFinderTimeout = 0;
         }

         if (this.processor != null) {
            if (!this.isPathStillValid()) {
               this.startNewPath();
               return;
            }

            if (!this.processor.isDone()) {
               this.processor.process();
            } else {
               this.stopMoving();
               this.restTimer = this.restTime;
               this.startNewPath();
            }
         } else {
            this.startNewPath();
         }

      }
   }

   private void startNewPath() {
      if (mc.f_91074_ != null) {
         this.stopMoving();
         BlockPos currentPos = mc.f_91074_.m_20183_();
         int range = this.findRange;
         int maxAttempts = 20;

         for(int attempt = 0; attempt < maxAttempts; ++attempt) {
            BlockPos goal = currentPos.m_7918_(this.random.nextInt(range * 2 + 1) - range, this.random.nextInt(3) - 1, this.random.nextInt(range * 2 + 1) - range);
            double distance = Math.sqrt(currentPos.m_123331_(goal));
            if (distance <= (double)range && distance >= 1.0 && mc.f_91073_.m_46749_(goal)) {
               PrintStream var10000 = System.out;
               String var10001 = String.valueOf(goal);
               var10000.println("找到合适目标: " + var10001 + " 距离: " + distance);
               this.pathFinder = new PathFinder(goal);
               this.pathFinder.thinkSpeed = 2048;
               this.pathFinder.thinkTime = 30;
               this.processor = null;
               this.pathFinderTimeout = 0;
               return;
            }
         }

         this.restTimer = 10;
      }
   }

   private boolean isPathStillValid() {
      if (this.processor != null && this.pathFinder != null && mc.f_91074_ != null) {
         int index = this.processor.getIndex();
         List path = this.pathFinder.getPath();
         if (index >= path.size()) {
            return false;
         } else {
            BlockPos currentPos = mc.f_91074_.m_20183_();
            PathPos expectedPos = (PathPos)path.get(Math.min(index, path.size() - 1));
            return currentPos.m_123331_(expectedPos) < 64.0;
         }
      } else {
         return false;
      }
   }

   private void stopMoving() {
      PathProcessor.releaseControls();
      if (mc.f_91074_ != null) {
         mc.f_91066_.f_92085_.m_7249_(false);
         mc.f_91066_.f_92087_.m_7249_(false);
         mc.f_91066_.f_92086_.m_7249_(false);
         mc.f_91066_.f_92088_.m_7249_(false);
         mc.f_91066_.f_92089_.m_7249_(false);
         mc.f_91066_.f_92090_.m_7249_(false);
      }

      this.processor = null;
      this.pathFinder = null;
   }

   public void onRender(PoseStack poseStack, float partialTicks) {
      if (this.isEnabled() && this.pathFinder != null) {
         List path = this.pathFinder.getPath();
         if (path != null && !path.isEmpty()) {
            PathRenderer.renderPath(poseStack, path, true, false);
         }
      }

   }

   public void forceRender(PoseStack poseStack, float partialTicks) {
      if (this.pathFinder != null) {
         List path = this.pathFinder.getPath();
         if (path != null && !path.isEmpty()) {
            PathRenderer.renderPath(poseStack, path, true, false);
         }
      }

   }

   public void onClick() {
      this.toggle();
   }
}
