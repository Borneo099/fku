package lexis.Hack.Hacks.Render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.Colors.SettingColor;
import lexis.Hack.Utils.Render.OutlineShaderRenderer;
import net.minecraft.client.Minecraft;

public class ItemOutlineHack extends Hack {
   private static final String CONFIG_KEY = "物品描边";
   private static final String[] MODES = new String[]{"仅空心", "仅描边", "空心+描边"};
   private static final int[] SHAPE_OF = new int[]{0, 1, 2};
   public static ItemOutlineHack INSTANCE;
   public static volatile boolean WORLD_RENDERING = false;
   private int modeIndex = 1;
   private SettingColor outlineColor = new SettingColor(0, 255, 255, 255);
   private SettingColor fillColor = new SettingColor(0, 255, 255, 80);
   private int outlineWidth = 2;
   private RenderTarget handTarget;
   private final HackConfig config;

   public ItemOutlineHack() {
      super("物品描边", new String[]{"给手上 描边颜色 好看"}, Hack.Category.RENDER, true);
      INSTANCE = this;
      this.addSetting(new Hack.Setting("模式", "仅空心 / 仅描边 / 空心+描边", MODES[this.modeIndex], MODES));
      this.addSetting(new Hack.Setting("描边颜色", "描边发光颜色", this.outlineColor.getPacked()));
      this.addSetting(new Hack.Setting("空心颜色", "空心填充颜色", this.fillColor.getPacked()));
      this.addSetting(new Hack.Setting("描边宽度", "边缘轮廓线宽度", this.outlineWidth, 1, 32, Hack.ValueDisplay.INTEGER));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private int modeIndexFromString(String s) {
      for(int i = 0; i < MODES.length; ++i) {
         if (MODES[i].equals(s)) {
            return i;
         }
      }

      return 1;
   }

   private void loadConfig() {
      this.modeIndex = this.modeIndexFromString(this.config.getStringSetting("物品描边", "模式", MODES[this.modeIndex]));
      this.outlineColor = new SettingColor(this.config.getIntSetting("物品描边", "描边颜色", this.outlineColor.getPacked()));
      this.fillColor = new SettingColor(this.config.getIntSetting("物品描边", "空心颜色", this.fillColor.getPacked()));
      this.outlineWidth = this.config.getIntSetting("物品描边", "描边宽度", this.outlineWidth);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting s = (Hack.Setting)var1.next();
         switch (s.getName()) {
            case "模式":
               s.setValue(MODES[this.modeIndex]);
               break;
            case "描边颜色":
               s.setValue(this.outlineColor.getPacked());
               break;
            case "空心颜色":
               s.setValue(this.fillColor.getPacked());
               break;
            case "描边宽度":
               s.setValue(this.outlineWidth);
         }
      }

   }

   public void onEnable() {
   }

   public void onDisable() {
      if (this.handTarget != null) {
         this.handTarget.m_83930_();
         this.handTarget = null;
      }

   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting s = (Hack.Setting)var2.next();
         int w;
         switch (s.getName()) {
            case "模式":
               w = this.modeIndexFromString(s.getString());
               if (w != this.modeIndex) {
                  this.modeIndex = w;
                  needSave = true;
               }
               break;
            case "描边颜色":
               w = (Integer)s.getValue();
               if (w != this.outlineColor.getPacked()) {
                  this.outlineColor = new SettingColor(w);
                  needSave = true;
               }
               break;
            case "空心颜色":
               w = (Integer)s.getValue();
               if (w != this.fillColor.getPacked()) {
                  this.fillColor = new SettingColor(w);
                  needSave = true;
               }
               break;
            case "描边宽度":
               w = s.getInt();
               if (w != this.outlineWidth) {
                  this.outlineWidth = w;
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.config.saveHackSettings("物品描边", this.getSettings());
      }

   }

   public void onClick() {
      this.toggle();
   }

   public static boolean isShaderActive() {
      return INSTANCE != null && INSTANCE.isEnabled();
   }

   public void beginCapture() {
      Minecraft mc = Minecraft.m_91087_();
      RenderTarget main = mc.m_91385_();
      int w = main.f_83915_;
      int h = main.f_83916_;
      if (w > 0 && h > 0) {
         if (this.handTarget == null) {
            this.handTarget = new TextureTarget(w, h, true, Minecraft.f_91002_);
         } else if (this.handTarget.f_83915_ != w || this.handTarget.f_83916_ != h) {
            this.handTarget.m_83941_(w, h, Minecraft.f_91002_);
         }

         this.handTarget.m_83931_(0.0F, 0.0F, 0.0F, 0.0F);
         this.handTarget.m_83954_(Minecraft.f_91002_);
         this.handTarget.m_83947_(true);
      }
   }

   public void endCaptureAndComposite() {
      if (this.handTarget != null) {
         Minecraft mc = Minecraft.m_91087_();
         RenderTarget main = mc.m_91385_();
         main.m_83947_(true);
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         this.handTarget.m_83957_(main.f_83915_, main.f_83916_, false);
         OutlineShaderRenderer.get().render(this.handTarget.m_83975_(), this.handTarget.f_83915_, this.handTarget.f_83916_, this.outlineWidth, SHAPE_OF[this.modeIndex], false, this.fillColor.getPacked(), this.outlineColor.getPacked());
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }
}
