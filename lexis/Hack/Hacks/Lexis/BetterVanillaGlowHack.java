package lexis.Hack.Hacks.Lexis;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.Colors.SettingColor;
import lexis.Hack.Utils.Render.OutlineShaderRenderer;

public class BetterVanillaGlowHack extends Hack {
   private static final String CONFIG_KEY = "更好的原版发光";
   private static final String[] MODES = new String[]{"仅空心(挂esp颜色)", "仅描边(挂esp颜色)", "空心+描边(挂esp颜色)", "仅描边", "仅空心", "空心+描边"};
   private static final int[] SHAPE_OF = new int[]{0, 1, 2, 1, 0, 2};
   public static BetterVanillaGlowHack INSTANCE;
   private int modeIndex = 2;
   private int outlineWidth = 2;
   private SettingColor fillColor = new SettingColor(255, 0, 255, 80);
   private SettingColor outlineColor = new SettingColor(255, 0, 255, 255);
   private final HackConfig config;

   public BetterVanillaGlowHack() {
      super("更好的原版发光", new String[]{"改进原版发光的显示 内部填充 和 边缘描边"}, Hack.Category.LEXIS, true);
      INSTANCE = this;
      this.addSetting(new Hack.Setting("模式", "挂esp颜色=用各ESP发光色 / 无挂=用下方自定义颜色", "空心+描边(挂esp颜色)", (String[])MODES.clone()));
      this.addSetting(new Hack.Setting("空心颜色", "无挂esp模式下，模型内部填充颜色", this.fillColor.getPacked()));
      this.addSetting(new Hack.Setting("描边颜色", "无挂esp模式下，模型边缘描边颜色", this.outlineColor.getPacked()));
      this.addSetting(new Hack.Setting("描边宽度", "边缘轮廓线宽度", this.outlineWidth, 1, 32, Hack.ValueDisplay.INTEGER));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.modeIndex = parseMode(this.config.getStringSetting("更好的原版发光", "模式", MODES[2]));
      this.fillColor = new SettingColor(this.config.getIntSetting("更好的原版发光", "空心颜色", this.fillColor.getPacked()));
      this.outlineColor = new SettingColor(this.config.getIntSetting("更好的原版发光", "描边颜色", this.outlineColor.getPacked()));
      this.outlineWidth = this.config.getIntSetting("更好的原版发光", "描边宽度", this.outlineWidth);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "模式":
               setting.setValue(MODES[this.modeIndex]);
               break;
            case "空心颜色":
               setting.setValue(this.fillColor.getPacked());
               break;
            case "描边颜色":
               setting.setValue(this.outlineColor.getPacked());
               break;
            case "描边宽度":
               setting.setValue(this.outlineWidth);
         }
      }

   }

   private static int parseMode(String s) {
      if (s != null) {
         for(int i = 0; i < MODES.length; ++i) {
            if (MODES[i].equals(s)) {
               return i;
            }
         }
      }

      return 2;
   }

   public void onEnable() {
   }

   public void onDisable() {
      OutlineShaderRenderer.get().destroy();
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         switch (setting.getName()) {
            case "模式":
               int nm = parseMode(setting.getString());
               if (nm != this.modeIndex) {
                  this.modeIndex = nm;
                  needSave = true;
               }
               break;
            case "空心颜色":
               int nf = (Integer)setting.getValue();
               if (nf != this.fillColor.getPacked()) {
                  this.fillColor = new SettingColor(nf);
                  needSave = true;
               }
               break;
            case "描边颜色":
               int no = (Integer)setting.getValue();
               if (no != this.outlineColor.getPacked()) {
                  this.outlineColor = new SettingColor(no);
                  needSave = true;
               }
               break;
            case "描边宽度":
               int nw = setting.getInt();
               if (nw != this.outlineWidth) {
                  this.outlineWidth = nw;
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.config.saveHackSettings("更好的原版发光", this.getSettings());
      }

   }

   public void onClick() {
      this.toggle();
   }

   public static boolean isShaderActive() {
      return INSTANCE != null && INSTANCE.isEnabled();
   }

   public int getWidth() {
      return this.outlineWidth;
   }

   public int getShapeMode() {
      return SHAPE_OF[this.modeIndex];
   }

   public boolean isEspColor() {
      return this.modeIndex < 3;
   }

   public int getFillColor() {
      return this.fillColor.getPacked();
   }

   public int getOutlineColor() {
      return this.outlineColor.getPacked();
   }
}
