package lexis.Hack.Hacks.Render;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.events.IGuiRenderable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class ArmorOverlayHack extends Hack implements IGuiRenderable {
   private static final Minecraft mc = Minecraft.m_91087_();
   private HackConfig config;
   private static final String CONFIG_KEY = "盔甲耐久显示";
   private int x = 490;
   private int y = 473;
   private int spacing = 22;
   private DisplayMode displayMode;
   private int textColor;
   private int barColor;
   private int lowDurabilityColor;
   private int lowThreshold;

   public ArmorOverlayHack() {
      super("盔甲耐久显示", "在屏幕上显示盔甲耐久度", Hack.Category.RENDER, true);
      this.displayMode = ArmorOverlayHack.DisplayMode.BAR;
      this.textColor = -1;
      this.barColor = -16711936;
      this.lowDurabilityColor = -65536;
      this.lowThreshold = 20;
      this.addSetting(new Hack.Setting("X坐标", "显示位置 X 坐标", this.x, 0, 1920));
      this.addSetting(new Hack.Setting("Y坐标", "显示位置 Y 坐标", this.y, 0, 1080));
      this.addSetting(new Hack.Setting("间距", "盔甲图标之间的像素间距", this.spacing, 4, 64));
      this.addSetting(new Hack.Setting("显示模式", "选择显示耐久条或数值", ArmorOverlayHack.DisplayMode.BAR.toString(), new String[]{ArmorOverlayHack.DisplayMode.BAR.toString(), ArmorOverlayHack.DisplayMode.NUMBER.toString()}));
      this.addSetting(new Hack.Setting("文本颜色", "耐久数值颜色", this.textColor));
      this.addSetting(new Hack.Setting("满耐久颜色", "高耐久进度条颜色", this.barColor));
      this.addSetting(new Hack.Setting("低耐久颜色", "低耐久进度条颜色", this.lowDurabilityColor));
      this.addSetting(new Hack.Setting("低耐久阈值", "低于此百分比变红", this.lowThreshold, 0, 100));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.x = this.config.getIntSetting("盔甲耐久显示", "X坐标", 490);
      this.y = this.config.getIntSetting("盔甲耐久显示", "Y坐标", 473);
      this.spacing = this.config.getIntSetting("盔甲耐久显示", "间距", 22);
      String modeStr = this.config.getStringSetting("盔甲耐久显示", "显示模式", ArmorOverlayHack.DisplayMode.BAR.toString());
      DisplayMode[] var2 = ArmorOverlayHack.DisplayMode.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         DisplayMode mode = var2[var4];
         if (mode.toString().equals(modeStr)) {
            this.displayMode = mode;
            break;
         }
      }

      this.textColor = this.config.getIntSetting("盔甲耐久显示", "文本颜色", -1);
      this.barColor = this.config.getIntSetting("盔甲耐久显示", "满耐久颜色", -16711936);
      this.lowDurabilityColor = this.config.getIntSetting("盔甲耐久显示", "低耐久颜色", -65536);
      this.lowThreshold = this.config.getIntSetting("盔甲耐久显示", "低耐久阈值", 20);
      Iterator var6 = this.getSettings().iterator();

      while(var6.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var6.next();
         switch (setting.getName()) {
            case "X坐标":
               setting.setValue(this.x);
               break;
            case "Y坐标":
               setting.setValue(this.y);
               break;
            case "间距":
               setting.setValue(this.spacing);
               break;
            case "显示模式":
               setting.setValue(this.displayMode.toString());
               break;
            case "文本颜色":
               setting.setValue(this.textColor);
               break;
            case "满耐久颜色":
               setting.setValue(this.barColor);
               break;
            case "低耐久颜色":
               setting.setValue(this.lowDurabilityColor);
               break;
            case "低耐久阈值":
               setting.setValue(this.lowThreshold);
         }
      }

   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(true) {
         label85:
         while(var2.hasNext()) {
            Hack.Setting setting = (Hack.Setting)var2.next();
            int newBarColor;
            int newLowColor;
            switch (setting.getName()) {
               case "X坐标":
                  int newX = setting.getInt();
                  if (newX != this.x) {
                     this.x = newX;
                     needSave = true;
                  }
                  break;
               case "Y坐标":
                  int newY = setting.getInt();
                  if (newY != this.y) {
                     this.y = newY;
                     needSave = true;
                  }
                  break;
               case "间距":
                  int newSpacing = setting.getInt();
                  if (newSpacing != this.spacing) {
                     this.spacing = newSpacing;
                     needSave = true;
                  }
                  break;
               case "显示模式":
                  String newMode = setting.getString();
                  DisplayMode[] var14 = ArmorOverlayHack.DisplayMode.values();
                  newBarColor = var14.length;
                  newLowColor = 0;

                  while(true) {
                     if (newLowColor >= newBarColor) {
                        continue label85;
                     }

                     DisplayMode mode = var14[newLowColor];
                     if (mode.toString().equals(newMode) && this.displayMode != mode) {
                        this.displayMode = mode;
                        needSave = true;
                        continue label85;
                     }

                     ++newLowColor;
                  }
               case "文本颜色":
                  int newTextColor = (Integer)setting.getValue();
                  if (newTextColor != this.textColor) {
                     this.textColor = newTextColor;
                     needSave = true;
                  }
                  break;
               case "满耐久颜色":
                  newBarColor = (Integer)setting.getValue();
                  if (newBarColor != this.barColor) {
                     this.barColor = newBarColor;
                     needSave = true;
                  }
                  break;
               case "低耐久颜色":
                  newLowColor = (Integer)setting.getValue();
                  if (newLowColor != this.lowDurabilityColor) {
                     this.lowDurabilityColor = newLowColor;
                     needSave = true;
                  }
                  break;
               case "低耐久阈值":
                  int newThreshold = setting.getInt();
                  if (newThreshold != this.lowThreshold) {
                     this.lowThreshold = newThreshold;
                     needSave = true;
                  }
            }
         }

         if (needSave) {
            this.config.saveHackSettings("盔甲耐久显示", this.getSettings());
         }

         return;
      }
   }

   public void onRenderGui(GuiGraphics guiGraphics, float partialTick) {
      if (this.isEnabled() && mc.f_91074_ != null) {
         if (!mc.f_91074_.m_7500_() && !mc.f_91074_.m_5833_()) {
            int startX = this.x;
            int startY = this.y;

            for(int i = 3; i >= 0; --i) {
               ItemStack stack = (ItemStack)mc.f_91074_.m_150109_().f_35975_.get(i);
               if (!stack.m_41619_()) {
                  guiGraphics.m_280480_(stack, startX, startY);
                  guiGraphics.m_280370_(mc.f_91062_, stack, startX, startY);
                  if (stack.m_41720_() instanceof ArmorItem) {
                     int maxDamage = stack.m_41776_();
                     int damage = stack.m_41773_();
                     float percent = (float)(maxDamage - damage) / (float)maxDamage * 100.0F;
                     switch (this.displayMode) {
                        case BAR:
                           int barWidth = 14;
                           int barHeight = 2;
                           int barX = startX + 1;
                           int barY = startY + 14;
                           int filledWidth = (int)((float)barWidth * percent / 100.0F);
                           int color = percent <= (float)this.lowThreshold ? this.lowDurabilityColor : this.barColor;
                           guiGraphics.m_280509_(barX, barY, barX + barWidth, barY + barHeight, -13421773);
                           guiGraphics.m_280509_(barX, barY, barX + filledWidth, barY + barHeight, color);
                           break;
                        case NUMBER:
                           String text = String.format("%.0f%%", percent);
                           int textWidth = mc.f_91062_.m_92895_(text);
                           guiGraphics.m_280488_(mc.f_91062_, text, startX + (16 - textWidth) / 2, startY + 12, this.textColor);
                     }
                  }

                  startX += this.spacing;
               }
            }

         }
      }
   }

   public void onClick() {
      this.toggle();
   }

   public static enum DisplayMode {
      BAR("耐久条"),
      NUMBER("数值");

      private final String name;

      private DisplayMode(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }

      // $FF: synthetic method
      private static DisplayMode[] $values() {
         return new DisplayMode[]{BAR, NUMBER};
      }
   }
}
