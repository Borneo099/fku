package lexis.Hack.Hacks.Misc;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import org.lwjgl.glfw.GLFW;

public class ThrowHack extends Hack {
   private HackConfig config;
   private static final String CONFIG_KEY = "多次使用";
   private int amount = 16;
   private int speed = 32;
   private boolean wasRightClicked = false;
   private long lastUseTime = 0L;

   public ThrowHack() {
      super("多次使用", new String[]{"每次使用次数物品", "§c§l警告：使用这过多数量可能服务器最高延迟！发包限制！"}, Hack.Category.MISC, true);
      this.addSetting(new Hack.Setting("数量", "每次点击的使用次数", 16.0, 1.0, 1000000.0, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("点击速度", "点击间隔(如太快可能产生很卡)", 32.0, 1.0, 1000.0, Hack.ValueDisplay.INTEGER));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.amount = (int)this.config.getDoubleSetting("多次使用", "数量", 16.0);
      this.speed = (int)this.config.getDoubleSetting("多次使用", "点击速度", 32.0);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "数量":
               setting.setValue((double)this.amount);
               break;
            case "点击速度":
               setting.setValue((double)this.speed);
         }
      }

   }

   public String getDisplayName() {
      String var10000 = super.getDisplayName();
      return var10000 + " [" + this.amount + "]";
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         switch (setting.getName()) {
            case "数量":
               int newAmount = (int)setting.getDouble();
               if (newAmount != this.amount) {
                  this.amount = newAmount;
                  needSave = true;
               }
               break;
            case "点击速度":
               int newSpeed = (int)setting.getDouble();
               if (newSpeed != this.speed) {
                  this.speed = newSpeed;
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.config.saveHackSettings("多次使用", this.getSettings());
      }

      Minecraft mc = Minecraft.m_91087_();
      if (mc.f_91074_ != null && mc.f_91072_ != null) {
         if (mc.f_91080_ != null) {
            this.wasRightClicked = false;
         } else {
            long window = mc.m_91268_().m_85439_();
            boolean rightPressed = GLFW.glfwGetMouseButton(window, 1) == 1;
            long currentTime = System.currentTimeMillis();
            if (rightPressed && currentTime - this.lastUseTime > (long)this.speed) {
               for(int i = 0; i < this.amount; ++i) {
                  if (mc.f_91077_ != null && mc.f_91077_.m_6662_() == Type.BLOCK) {
                     BlockHitResult blockHit = (BlockHitResult)mc.f_91077_;
                     mc.f_91072_.m_233732_(mc.f_91074_, InteractionHand.MAIN_HAND, blockHit);
                  }

                  mc.f_91072_.m_233721_(mc.f_91074_, InteractionHand.MAIN_HAND);
               }

               this.lastUseTime = currentTime;
            }

            this.wasRightClicked = rightPressed;
         }
      }
   }

   public void onClick() {
      this.toggle();
   }
}
