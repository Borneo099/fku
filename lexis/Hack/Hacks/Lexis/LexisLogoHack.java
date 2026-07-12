package lexis.Hack.Hacks.Lexis;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Hackutil.config.LexisLogoConfig;
import lexis.Hack.Utils.Colors.SettingColor;

public class LexisLogoHack extends Hack {
   private LexisLogoConfig logoConfig;
   private HackConfig config;
   private static final String CONFIG_KEY = "LexisLogo";
   private SettingColor textColor;
   private SettingColor bgColor;
   private LogoMode mode;
   private int currentFrame;
   private long lastFrameTime;
   private static final int FRAME_COUNT = 217;
   private static final long FRAME_DURATION_MS = 16L;

   public LexisLogoHack() {
      super("Lexis Logo", "显示Lexis Logo在左上角", Hack.Category.LEXIS, true);
      this.mode = LexisLogoHack.LogoMode.GIF;
      this.currentFrame = 0;
      this.lastFrameTime = 0L;
      this.logoConfig = LexisLogoConfig.getInstance();
      this.textColor = new SettingColor(this.logoConfig.textColorR, this.logoConfig.textColorG, this.logoConfig.textColorB, 255);
      this.bgColor = new SettingColor(this.logoConfig.bgColorR, this.logoConfig.bgColorG, this.logoConfig.bgColorB, this.logoConfig.bgAlpha);
      this.addSetting(new Hack.Setting("文字颜色", "Lexis文字颜色", this.textColor.getPacked()));
      this.addSetting(new Hack.Setting("背景颜色", "Logo背景颜色", this.bgColor.getPacked()));
      this.addSetting(new Hack.Setting("显示模式", "选择Logo显示模式", "GIF动画", new String[]{"GIF动画", "静态图片"}));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      int textPacked = this.config.getIntSetting("LexisLogo", "文字颜色", this.textColor.getPacked());
      int bgPacked = this.config.getIntSetting("LexisLogo", "背景颜色", this.bgColor.getPacked());
      String modeStr = this.config.getStringSetting("LexisLogo", "显示模式", "GIF动画");
      this.textColor = new SettingColor(textPacked);
      this.bgColor = new SettingColor(bgPacked);
      LogoMode[] var4 = LexisLogoHack.LogoMode.values();
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         LogoMode m = var4[var6];
         if (m.toString().equals(modeStr)) {
            this.mode = m;
            break;
         }
      }

      this.logoConfig.textColorR = this.textColor.r;
      this.logoConfig.textColorG = this.textColor.g;
      this.logoConfig.textColorB = this.textColor.b;
      this.logoConfig.bgColorR = this.bgColor.r;
      this.logoConfig.bgColorG = this.bgColor.g;
      this.logoConfig.bgColorB = this.bgColor.b;
      this.logoConfig.bgAlpha = this.bgColor.a;
      Iterator var8 = this.getSettings().iterator();

      while(var8.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var8.next();
         switch (setting.getName()) {
            case "文字颜色":
               setting.setValue(this.textColor.getPacked());
               break;
            case "背景颜色":
               setting.setValue(this.bgColor.getPacked());
               break;
            case "显示模式":
               setting.setValue(this.mode.toString());
         }
      }

   }

   private void saveConfig() {
      Iterator var1 = this.getSettings().iterator();

      while(true) {
         while(true) {
            while(var1.hasNext()) {
               Hack.Setting setting = (Hack.Setting)var1.next();
               switch (setting.getName()) {
                  case "文字颜色":
                     this.textColor = new SettingColor((Integer)setting.getValue());
                     break;
                  case "背景颜色":
                     this.bgColor = new SettingColor((Integer)setting.getValue());
                     break;
                  case "显示模式":
                     String modeStr = setting.getString();
                     LogoMode[] var6 = LexisLogoHack.LogoMode.values();
                     int var7 = var6.length;

                     for(int var8 = 0; var8 < var7; ++var8) {
                        LogoMode m = var6[var8];
                        if (m.toString().equals(modeStr)) {
                           this.mode = m;
                           break;
                        }
                     }
               }
            }

            this.logoConfig.textColorR = this.textColor.r;
            this.logoConfig.textColorG = this.textColor.g;
            this.logoConfig.textColorB = this.textColor.b;
            this.logoConfig.bgColorR = this.bgColor.r;
            this.logoConfig.bgColorG = this.bgColor.g;
            this.logoConfig.bgColorB = this.bgColor.b;
            this.logoConfig.bgAlpha = this.bgColor.a;
            this.config.saveHackSettings("LexisLogo", this.getSettings());
            this.logoConfig.save();
            return;
         }
      }
   }

   public void onEnable() {
      this.logoConfig.enabled = true;
      this.saveConfig();
   }

   public void onDisable() {
      this.logoConfig.enabled = false;
      this.saveConfig();
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(true) {
         while(true) {
            while(var2.hasNext()) {
               Hack.Setting setting = (Hack.Setting)var2.next();
               switch (setting.getName()) {
                  case "文字颜色":
                     if ((Integer)setting.getValue() != this.textColor.getPacked()) {
                        this.textColor = new SettingColor((Integer)setting.getValue());
                        needSave = true;
                     }
                     break;
                  case "背景颜色":
                     if ((Integer)setting.getValue() != this.bgColor.getPacked()) {
                        this.bgColor = new SettingColor((Integer)setting.getValue());
                        needSave = true;
                     }
                     break;
                  case "显示模式":
                     String modeStr = setting.getString();
                     LogoMode[] var7 = LexisLogoHack.LogoMode.values();
                     int var8 = var7.length;

                     for(int var9 = 0; var9 < var8; ++var9) {
                        LogoMode m = var7[var9];
                        if (m.toString().equals(modeStr) && this.mode != m) {
                           this.mode = m;
                           needSave = true;
                           break;
                        }
                     }
               }
            }

            if (needSave) {
               this.saveConfig();
            }

            if (this.isEnabled() && this.mode == LexisLogoHack.LogoMode.GIF) {
               long now = System.currentTimeMillis();
               if (this.lastFrameTime == 0L) {
                  this.lastFrameTime = now;
               }

               long elapsed = now - this.lastFrameTime;
               if (elapsed >= 16L) {
                  int steps = (int)(elapsed / 16L);
                  this.currentFrame = (this.currentFrame + steps) % 217;
                  this.lastFrameTime = now - elapsed % 16L;
               }
            }

            return;
         }
      }
   }

   public int getCurrentFrameIndex() {
      return this.currentFrame;
   }

   public LogoMode getMode() {
      return this.mode;
   }

   public void onClick() {
      this.toggle();
   }

   public static enum LogoMode {
      GIF("GIF动画"),
      STATIC("静态图片");

      private final String displayName;

      private LogoMode(String name) {
         this.displayName = name;
      }

      public String toString() {
         return this.displayName;
      }

      // $FF: synthetic method
      private static LogoMode[] $values() {
         return new LogoMode[]{GIF, STATIC};
      }
   }
}
