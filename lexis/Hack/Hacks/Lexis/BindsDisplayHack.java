package lexis.Hack.Hacks.Lexis;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.SettingsWindow;
import lexis.Hack.Hackutil.HUD.BindsDisplayWidget;
import lexis.Hack.Hackutil.HUD.MoveBindsDisplayScreen;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.Colors.SettingColor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.fml.ModList;

public class BindsDisplayHack extends Hack {
   private static BindsDisplayWidget widget;
   private HackConfig config;
   private static final String CONFIG_KEY = "按键显示";
   private boolean movingMode = false;
   private SettingColor backgroundColor = new SettingColor(0, 0, 0, 180);
   private SettingColor textColor = new SettingColor(255, 255, 255, 255);

   public BindsDisplayHack() {
      super("按键显示", new String[]{"在HUD左上角(默认位置)显示已绑定按键的功能", "右键功能打开gui找到按钮的可以设置移动模式"}, Hack.Category.LEXIS, true);
      this.addSetting(new Hack.Setting("背景颜色", "窗口背景色", this.backgroundColor.getPacked()));
      this.addSetting(new Hack.Setting("文本颜色", "按键文本颜色", this.textColor.getPacked()));
      this.addSetting(new Hack.Setting("移动按键显示", "进入移动模式", "移动", this::openMoveMode));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      int bg = this.config.getIntSetting("按键显示", "背景颜色", this.backgroundColor.getPacked());
      this.backgroundColor = new SettingColor(bg);
      int txt = this.config.getIntSetting("按键显示", "文本颜色", this.textColor.getPacked());
      this.textColor = new SettingColor(txt);
      Iterator var3 = this.getSettings().iterator();

      while(var3.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var3.next();
         if (setting.getName().equals("背景颜色")) {
            setting.setValue(this.backgroundColor.getPacked());
         }

         if (setting.getName().equals("文本颜色")) {
            setting.setValue(this.textColor.getPacked());
         }
      }

   }

   public void onEnable() {
      if (widget == null) {
         widget = new BindsDisplayWidget(this);
      }

      widget.setVisible(true);
      this.updateColors();
      int x = this.config.getWindowX("按键显示");
      int y = this.config.getWindowY("按键显示");
      if (x != -1 && y != -1) {
         widget.setPosition(x, y);
      } else {
         boolean hasKarucn = ModList.get().isLoaded("karucn");
         widget.setPosition(5, hasKarucn ? 120 : 5);
      }

   }

   public void onDisable() {
      if (widget != null) {
         widget.setVisible(false);
         this.config.setWindowPos("按键显示", widget.getX(), widget.getY());
         this.config.save();
      }

   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         int newTxt;
         if (setting.getName().equals("背景颜色")) {
            newTxt = (Integer)setting.getValue();
            if (newTxt != this.backgroundColor.getPacked()) {
               this.backgroundColor = new SettingColor(newTxt);
               needSave = true;
               this.updateColors();
            }
         } else if (setting.getName().equals("文本颜色")) {
            newTxt = (Integer)setting.getValue();
            if (newTxt != this.textColor.getPacked()) {
               this.textColor = new SettingColor(newTxt);
               needSave = true;
               this.updateColors();
            }
         }
      }

      if (needSave) {
         this.config.saveHackSettings("按键显示", this.getSettings());
      }

   }

   private void updateColors() {
      if (widget != null) {
         widget.setBackgroundColor(this.backgroundColor);
         widget.setTextColor(this.textColor);
      }

   }

   public void onClick() {
      this.toggle();
   }

   public void onRightClick() {
      if (this.getSettings() != null && !this.getSettings().isEmpty()) {
         mc.m_91152_(new SettingsWindow(this, mc.f_91080_));
      }

   }

   public BindsDisplayWidget getWidget() {
      return widget;
   }

   public SettingColor getBackgroundColor() {
      return this.backgroundColor;
   }

   public SettingColor getTextColor() {
      return this.textColor;
   }

   public boolean isMovingMode() {
      return this.movingMode;
   }

   public void setMovingMode(boolean moving) {
      this.movingMode = moving;
   }

   public void autoSavePosition(int x, int y) {
      this.config.setWindowPos("按键显示", x, y);
      this.config.save();
   }

   private void openMoveMode() {
      Screen prev = mc.f_91080_;
      mc.m_91152_(new MoveBindsDisplayScreen(this, prev));
      this.setMovingMode(true);
   }
}
