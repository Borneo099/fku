package lexis.Hack.Hacks.Lexis;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.Colors.SettingColor;
import lexis.Hack.gui.screens.HUDSettingsScreen;
import net.minecraft.client.Minecraft;

public class HUDSettingsHack extends Hack {
   private HackConfig config;
   private static final String CONFIG_KEY = "显示HUD";
   private SettingColor textStartColor = new SettingColor(255, 255, 255, 255);
   private SettingColor textEndColor = new SettingColor(255, 255, 0, 255);
   private SettingColor bgColor = new SettingColor(0, 0, 0, 136);

   public HUDSettingsHack() {
      super("显示HUD", "在右上角显示已开启的功能", Hack.Category.LEXIS, true);
      this.addSetting(new Hack.Setting("文字起始颜色", "渐变开始颜色", this.textStartColor.getPacked()));
      this.addSetting(new Hack.Setting("文字结束颜色", "渐变结束颜色", this.textEndColor.getPacked()));
      this.addSetting(new Hack.Setting("背景颜色", "背景颜色", this.bgColor.getPacked()));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      int startPacked = this.config.getIntSetting("显示HUD", "文字起始颜色", this.textStartColor.getPacked());
      int endPacked = this.config.getIntSetting("显示HUD", "文字结束颜色", this.textEndColor.getPacked());
      int bgPacked = this.config.getIntSetting("显示HUD", "背景颜色", this.bgColor.getPacked());
      this.textStartColor = new SettingColor(startPacked);
      this.textEndColor = new SettingColor(endPacked);
      this.bgColor = new SettingColor(bgPacked);
      Iterator var4 = this.getSettings().iterator();

      while(var4.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var4.next();
         switch (setting.getName()) {
            case "文字起始颜色":
               setting.setValue(this.textStartColor.getPacked());
               break;
            case "文字结束颜色":
               setting.setValue(this.textEndColor.getPacked());
               break;
            case "背景颜色":
               setting.setValue(this.bgColor.getPacked());
         }
      }

   }

   private void saveConfig() {
      this.config.saveHackSettings("显示HUD", this.getSettings());
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
            case "文字起始颜色":
               int newStart = (Integer)setting.getValue();
               if (newStart != this.textStartColor.getPacked()) {
                  this.textStartColor = new SettingColor(newStart);
                  needSave = true;
               }
               break;
            case "文字结束颜色":
               int newEnd = (Integer)setting.getValue();
               if (newEnd != this.textEndColor.getPacked()) {
                  this.textEndColor = new SettingColor(newEnd);
                  needSave = true;
               }
               break;
            case "背景颜色":
               int newBg = (Integer)setting.getValue();
               if (newBg != this.bgColor.getPacked()) {
                  this.bgColor = new SettingColor(newBg);
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.saveConfig();
      }

   }

   public void onRightClick() {
      Minecraft.m_91087_().m_91152_(new HUDSettingsScreen(this, Minecraft.m_91087_().f_91080_));
   }

   public void onClick() {
      this.toggle();
   }

   public SettingColor getTextStartColor() {
      return this.textStartColor;
   }

   public SettingColor getTextEndColor() {
      return this.textEndColor;
   }

   public SettingColor getBgColor() {
      return this.bgColor;
   }
}
