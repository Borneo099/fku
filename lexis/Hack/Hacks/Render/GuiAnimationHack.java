package lexis.Hack.Hacks.Render;

import java.util.Iterator;
import lexis.Gui.GuiAnimator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;

public class GuiAnimationHack extends Hack {
   private HackConfig config;
   private static final String CONFIG_KEY = "GUI动画";
   private AnimMode animMode;
   private double animSpeed;
   private double intensity;

   public GuiAnimationHack() {
      super("GUI动画", "打开背包时的动画效果", Hack.Category.RENDER, true);
      this.animMode = GuiAnimationHack.AnimMode.BOUNCE;
      this.animSpeed = 1.0;
      this.intensity = 1.0;
      this.addSetting(new Hack.Setting("动画模式", "选择动画效果", "弹跳", new String[]{"弹跳", "平滑", "放大", "淡入", "旋转", "变焦", "上滑", "下滑", "左滑", "右滑", "旋转进入", "翻转", "抖动"}));
      this.addSetting(new Hack.Setting("动画速度", "动画播放速度", 1.0, 0.1, 4.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("强度", "动画强度", 1.0, 0.1, 4.0, Hack.ValueDisplay.DECIMAL));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      String modeStr = this.config.getStringSetting("GUI动画", "动画模式", "弹跳");
      AnimMode[] var2 = GuiAnimationHack.AnimMode.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         AnimMode mode = var2[var4];
         if (mode.toString().equals(modeStr)) {
            this.animMode = mode;
            break;
         }
      }

      this.animSpeed = this.config.getDoubleSetting("GUI动画", "动画速度", 1.0);
      this.intensity = this.config.getDoubleSetting("GUI动画", "强度", 1.0);
      Iterator var6 = this.getSettings().iterator();

      while(var6.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var6.next();
         switch (setting.getName()) {
            case "动画模式":
               setting.setValue(this.animMode.toString());
               break;
            case "动画速度":
               setting.setValue(this.animSpeed);
               break;
            case "强度":
               setting.setValue(this.intensity);
         }
      }

   }

   public void onEnable() {
      GuiAnimator.setEnabled(true);
      GuiAnimator.setMode(this.animMode.ordinal());
      GuiAnimator.setSpeed(this.animSpeed);
      GuiAnimator.setIntensity(this.intensity);
   }

   public void onDisable() {
      GuiAnimator.setEnabled(false);
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(true) {
         label55:
         while(var2.hasNext()) {
            Hack.Setting setting = (Hack.Setting)var2.next();
            switch (setting.getName()) {
               case "动画模式":
                  String modeStr = setting.getString();
                  AnimMode[] var11 = GuiAnimationHack.AnimMode.values();
                  int var8 = var11.length;
                  int var12 = 0;

                  while(true) {
                     if (var12 >= var8) {
                        continue label55;
                     }

                     AnimMode mode = var11[var12];
                     if (mode.toString().equals(modeStr) && this.animMode != mode) {
                        this.animMode = mode;
                        needSave = true;
                        GuiAnimator.setMode(mode.ordinal());
                        continue label55;
                     }

                     ++var12;
                  }
               case "动画速度":
                  double newSpeed = setting.getDouble();
                  if (newSpeed != this.animSpeed) {
                     this.animSpeed = newSpeed;
                     needSave = true;
                     GuiAnimator.setSpeed(this.animSpeed);
                  }
                  break;
               case "强度":
                  double newIntensity = setting.getDouble();
                  if (newIntensity != this.intensity) {
                     this.intensity = newIntensity;
                     needSave = true;
                     GuiAnimator.setIntensity(this.intensity);
                  }
            }
         }

         if (needSave) {
            this.config.saveHackSettings("GUI动画", this.getSettings());
         }

         return;
      }
   }

   public void onClick() {
      this.toggle();
   }

   public static enum AnimMode {
      BOUNCE("弹跳"),
      SMOOTH("平滑"),
      EXPAND("放大"),
      FADE("淡入"),
      ROTATE("旋转"),
      ZOOM("变焦"),
      SLIDE_UP("上滑"),
      SLIDE_DOWN("下滑"),
      SLIDE_LEFT("左滑"),
      SLIDE_RIGHT("右滑"),
      SPIN("旋转进入"),
      FLIP("翻转"),
      SHAKE("抖动");

      private final String displayName;

      private AnimMode(String name) {
         this.displayName = name;
      }

      public String toString() {
         return this.displayName;
      }

      // $FF: synthetic method
      private static AnimMode[] $values() {
         return new AnimMode[]{BOUNCE, SMOOTH, EXPAND, FADE, ROTATE, ZOOM, SLIDE_UP, SLIDE_DOWN, SLIDE_LEFT, SLIDE_RIGHT, SPIN, FLIP, SHAKE};
      }
   }
}
