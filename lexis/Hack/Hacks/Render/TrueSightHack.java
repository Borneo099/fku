package lexis.Hack.Hacks.Render;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.FakeGlowManager;
import lexis.Hack.Utils.Colors.SettingColor;
import lexis.Hack.Utils.Render.RenderUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class TrueSightHack extends Hack {
   private static final String GLOW_SOURCE = "TrueSightHack";
   private EspMode espMode;
   private SettingColor boxColor;
   private SettingColor linesColor;
   private SettingColor sidesColor;
   private boolean glowEnabled;
   private int glowColor;
   private int maxDistance;
   private List invisibleEntities;
   private HackConfig config;
   private static final String CONFIG_KEY = "透视隐身实体";

   public TrueSightHack() {
      super("透视隐身实体", "以ESP方式显示隐身的实体（玩家、怪物等）", Hack.Category.RENDER, true);
      this.espMode = TrueSightHack.EspMode.ALL;
      this.boxColor = new SettingColor(255, 0, 0, 180);
      this.linesColor = new SettingColor(0, 255, 0, 180);
      this.sidesColor = new SettingColor(0, 0, 255, 180);
      this.glowEnabled = false;
      this.glowColor = 16777215;
      this.maxDistance = 64;
      this.invisibleEntities = new ArrayList();
      this.addSetting(new Hack.Setting("显示模式", "渲染方式", "全部", new String[]{"仅方框", "仅连线", "仅六面", "方框+连线", "方框+六面", "连线+六面", "全部"}));
      this.addSetting(new Hack.Setting("方框颜色", "方框边框颜色", this.boxColor.getPacked()));
      this.addSetting(new Hack.Setting("连线颜色", "准星到实体的连线颜色", this.linesColor.getPacked()));
      this.addSetting(new Hack.Setting("六面颜色", "半透明六面填充颜色", this.sidesColor.getPacked()));
      this.addSetting(new Hack.Setting("发光模式", "伪造原版发光效果（仅本地）", false));
      this.addSetting(new Hack.Setting("发光颜色", "伪造发光的颜色（RGB）", this.glowColor));
      this.addSetting(new Hack.Setting("最大距离", "显示距离", 64, 1, 1024, Hack.ValueDisplay.INTEGER));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      String modeStr = this.config.getStringSetting("透视隐身实体", "显示模式", "全部");
      EspMode[] var2 = TrueSightHack.EspMode.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         EspMode mode = var2[var4];
         if (mode.toString().equals(modeStr)) {
            this.espMode = mode;
            break;
         }
      }

      this.boxColor = new SettingColor(this.config.getIntSetting("透视隐身实体", "方框颜色", this.boxColor.getPacked()));
      this.linesColor = new SettingColor(this.config.getIntSetting("透视隐身实体", "连线颜色", this.linesColor.getPacked()));
      this.sidesColor = new SettingColor(this.config.getIntSetting("透视隐身实体", "六面颜色", this.sidesColor.getPacked()));
      this.glowEnabled = this.config.getBooleanSetting("透视隐身实体", "发光模式", false);
      this.glowColor = this.config.getIntSetting("透视隐身实体", "发光颜色", 16777215);
      this.maxDistance = this.config.getIntSetting("透视隐身实体", "最大距离", 64);
      Iterator var6 = this.getSettings().iterator();

      while(var6.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var6.next();
         switch (setting.getName()) {
            case "显示模式":
               setting.setValue(this.espMode.toString());
               break;
            case "方框颜色":
               setting.setValue(this.boxColor.getPacked());
               break;
            case "连线颜色":
               setting.setValue(this.linesColor.getPacked());
               break;
            case "六面颜色":
               setting.setValue(this.sidesColor.getPacked());
               break;
            case "发光模式":
               setting.setValue(this.glowEnabled);
               break;
            case "发光颜色":
               setting.setValue(this.glowColor);
               break;
            case "最大距离":
               setting.setValue(this.maxDistance);
         }
      }

   }

   public void onEnable() {
   }

   public void onDisable() {
      FakeGlowManager.clearSource("TrueSightHack");
      this.invisibleEntities.clear();
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      label139:
      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         int newLines;
         int newSides;
         switch (setting.getName()) {
            case "显示模式":
               String modeStr = setting.getString();
               EspMode[] var16 = TrueSightHack.EspMode.values();
               newLines = var16.length;
               newSides = 0;

               while(true) {
                  if (newSides >= newLines) {
                     continue label139;
                  }

                  EspMode mode = var16[newSides];
                  if (mode.toString().equals(modeStr) && this.espMode != mode) {
                     this.espMode = mode;
                     needSave = true;
                     continue label139;
                  }

                  ++newSides;
               }
            case "方框颜色":
               int newBox = (Integer)setting.getValue();
               if (newBox != this.boxColor.getPacked()) {
                  this.boxColor = new SettingColor(newBox);
                  needSave = true;
               }
               break;
            case "连线颜色":
               newLines = (Integer)setting.getValue();
               if (newLines != this.linesColor.getPacked()) {
                  this.linesColor = new SettingColor(newLines);
                  needSave = true;
               }
               break;
            case "六面颜色":
               newSides = (Integer)setting.getValue();
               if (newSides != this.sidesColor.getPacked()) {
                  this.sidesColor = new SettingColor(newSides);
                  needSave = true;
               }
               break;
            case "发光模式":
               if (setting.getBoolean() != this.glowEnabled) {
                  this.glowEnabled = setting.getBoolean();
                  needSave = true;
               }
               break;
            case "发光颜色":
               int newGlow = (Integer)setting.getValue();
               if (newGlow != this.glowColor) {
                  this.glowColor = newGlow;
                  needSave = true;
               }
               break;
            case "最大距离":
               int newDist = setting.getInt();
               if (newDist != this.maxDistance) {
                  this.maxDistance = newDist;
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.config.saveHackSettings("透视隐身实体", this.getSettings());
      }

      if (mc.f_91073_ != null && mc.f_91074_ != null) {
         List newEntities = new ArrayList();
         Iterator var13 = mc.f_91073_.m_104735_().iterator();

         while(var13.hasNext()) {
            Entity entity = (Entity)var13.next();
            if (entity instanceof LivingEntity) {
               LivingEntity living = (LivingEntity)entity;
               if (living != mc.f_91074_ && living.m_20145_() && !(mc.f_91074_.m_20270_(living) > (float)this.maxDistance)) {
                  newEntities.add(living);
               }
            }
         }

         var13 = this.invisibleEntities.iterator();

         LivingEntity entity;
         while(var13.hasNext()) {
            entity = (LivingEntity)var13.next();
            if (!newEntities.contains(entity)) {
               FakeGlowManager.setGlow(entity, "TrueSightHack", false, 0, 0.0);
            }
         }

         if (this.glowEnabled && this.isEnabled()) {
            var13 = newEntities.iterator();

            while(var13.hasNext()) {
               entity = (LivingEntity)var13.next();
               FakeGlowManager.setGlow(entity, "TrueSightHack", true, this.glowColor, (double)this.maxDistance);
            }
         } else {
            var13 = newEntities.iterator();

            while(var13.hasNext()) {
               entity = (LivingEntity)var13.next();
               FakeGlowManager.setGlow(entity, "TrueSightHack", false, 0, 0.0);
            }
         }

         this.invisibleEntities = newEntities;
      }
   }

   public void onClick() {
      this.toggle();
   }

   public void onRender(PoseStack poseStack, float partialTicks) {
      if (this.isEnabled() && !this.invisibleEntities.isEmpty() && mc.f_91074_ != null) {
         List boxes = new ArrayList();
         Iterator var4 = this.invisibleEntities.iterator();

         LivingEntity entity;
         while(var4.hasNext()) {
            entity = (LivingEntity)var4.next();
            boxes.add(entity.m_20191_().m_82400_(0.1));
         }

         if (this.espMode.showSides()) {
            RenderUtils.drawSolidBoxes(poseStack, boxes, this.sidesColor.getPacked(), false);
         }

         if (this.espMode.showBox()) {
            var4 = this.invisibleEntities.iterator();

            while(var4.hasNext()) {
               entity = (LivingEntity)var4.next();
               List singleBox = List.of(entity.m_20191_().m_82400_(0.1));
               RenderUtils.drawOutlinedBoxes(poseStack, singleBox, this.boxColor.getPacked(), false);
            }
         }

         if (this.espMode.showLines()) {
            List centers = new ArrayList();
            Iterator var8 = this.invisibleEntities.iterator();

            while(var8.hasNext()) {
               LivingEntity entity = (LivingEntity)var8.next();
               centers.add(entity.m_20191_().m_82399_());
            }

            RenderUtils.drawTracers(poseStack, partialTicks, centers, this.linesColor.getPacked(), false);
         }

      }
   }

   public static enum EspMode {
      BOX_ONLY("仅方框", true, false, false),
      LINES_ONLY("仅连线", false, true, false),
      SIDES_ONLY("仅六面", false, false, true),
      BOX_LINES("方框+连线", true, true, false),
      BOX_SIDES("方框+六面", true, false, true),
      LINES_SIDES("连线+六面", false, true, true),
      ALL("全部", true, true, true);

      private final String name;
      private final boolean box;
      private final boolean lines;
      private final boolean sides;

      private EspMode(String name, boolean box, boolean lines, boolean sides) {
         this.name = name;
         this.box = box;
         this.lines = lines;
         this.sides = sides;
      }

      public String toString() {
         return this.name;
      }

      public boolean showBox() {
         return this.box;
      }

      public boolean showLines() {
         return this.lines;
      }

      public boolean showSides() {
         return this.sides;
      }

      // $FF: synthetic method
      private static EspMode[] $values() {
         return new EspMode[]{BOX_ONLY, LINES_ONLY, SIDES_ONLY, BOX_LINES, BOX_SIDES, LINES_SIDES, ALL};
      }
   }
}
