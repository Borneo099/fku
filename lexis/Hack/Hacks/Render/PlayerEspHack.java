package lexis.Hack.Hacks.Render;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.FakeGlowManager;
import lexis.Hack.Utils.Colors.Color;
import lexis.Hack.Utils.Colors.SettingColor;
import lexis.Hack.Utils.Render.RenderUtils;
import net.minecraft.world.entity.player.Player;

public class PlayerEspHack extends Hack {
   private EspMode espMode;
   private SettingColor boxColor;
   private SettingColor linesColor;
   private SettingColor sidesColor;
   private boolean forceTeamColor;
   private boolean glowEnabled;
   private int glowColor;
   private int maxDistance;
   private List players;
   private HackConfig config;
   private static final String CONFIG_KEY = "玩家ESP";

   public PlayerEspHack() {
      super("玩家ESP", "显示玩家位置", Hack.Category.RENDER, true);
      this.espMode = PlayerEspHack.EspMode.ALL;
      this.boxColor = new SettingColor(255, 0, 0, 180);
      this.linesColor = new SettingColor(0, 255, 0, 180);
      this.sidesColor = new SettingColor(0, 0, 255, 180);
      this.forceTeamColor = false;
      this.glowEnabled = false;
      this.glowColor = 16777215;
      this.maxDistance = 128;
      this.players = new ArrayList();
      this.addSetting(new Hack.Setting("显示模式", "渲染方式", "全部", new String[]{"仅方框", "仅连线", "仅六面", "方框+连线", "方框+六面", "连线+六面", "全部"}));
      this.addSetting(new Hack.Setting("方框颜色", "方框边框和填充的颜色", this.boxColor.getPacked()));
      this.addSetting(new Hack.Setting("连线颜色", "准星到玩家的连线颜色", this.linesColor.getPacked()));
      this.addSetting(new Hack.Setting("六面颜色", "半透明六面填充的颜色", this.sidesColor.getPacked()));
      this.addSetting(new Hack.Setting("队伍颜色", "方框使用队伍颜色(仅方框，覆盖方框颜色)", false));
      this.addSetting(new Hack.Setting("发光模式", "伪造原版发光效果", false));
      this.addSetting(new Hack.Setting("发光颜色", "伪造发光的颜色", this.glowColor));
      this.addSetting(new Hack.Setting("最大距离", "显示距离", 128, 1, 1024, Hack.ValueDisplay.INTEGER));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      String modeStr = this.config.getStringSetting("玩家ESP", "显示模式", "全部");
      EspMode[] var2 = PlayerEspHack.EspMode.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         EspMode mode = var2[var4];
         if (mode.toString().equals(modeStr)) {
            this.espMode = mode;
            break;
         }
      }

      this.boxColor = new SettingColor(this.config.getIntSetting("玩家ESP", "方框颜色", this.boxColor.getPacked()));
      this.linesColor = new SettingColor(this.config.getIntSetting("玩家ESP", "连线颜色", this.linesColor.getPacked()));
      this.sidesColor = new SettingColor(this.config.getIntSetting("玩家ESP", "六面颜色", this.sidesColor.getPacked()));
      this.forceTeamColor = this.config.getBooleanSetting("玩家ESP", "队伍颜色", false);
      this.glowEnabled = this.config.getBooleanSetting("玩家ESP", "发光模式", false);
      this.glowColor = this.config.getIntSetting("玩家ESP", "发光颜色", 16777215);
      this.maxDistance = this.config.getIntSetting("玩家ESP", "最大距离", 128);
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
            case "队伍颜色":
               setting.setValue(this.forceTeamColor);
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
      this.players.clear();
      FakeGlowManager.clearSource("PlayerEsp");
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      label140:
      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         int newLines;
         int newSides;
         switch (setting.getName()) {
            case "显示模式":
               String modeStr = setting.getString();
               EspMode[] var14 = PlayerEspHack.EspMode.values();
               newLines = var14.length;
               newSides = 0;

               while(true) {
                  if (newSides >= newLines) {
                     continue label140;
                  }

                  EspMode mode = var14[newSides];
                  if (mode.toString().equals(modeStr) && this.espMode != mode) {
                     this.espMode = mode;
                     needSave = true;
                     continue label140;
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
            case "队伍颜色":
               if (setting.getBoolean() != this.forceTeamColor) {
                  this.forceTeamColor = setting.getBoolean();
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
               int newGlowColor = (Integer)setting.getValue();
               if (newGlowColor != this.glowColor) {
                  this.glowColor = newGlowColor;
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
         this.config.saveHackSettings("玩家ESP", this.getSettings());
      }

      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         this.players.clear();
         var2 = mc.f_91073_.m_6907_().iterator();

         Player player;
         while(var2.hasNext()) {
            player = (Player)var2.next();
            if (player != mc.f_91074_ && !(player.m_20270_(mc.f_91074_) > (float)this.maxDistance)) {
               this.players.add(player);
            }
         }

         var2 = mc.f_91073_.m_6907_().iterator();

         while(var2.hasNext()) {
            player = (Player)var2.next();
            if (!this.players.contains(player)) {
               FakeGlowManager.setGlow(player, "PlayerEsp", false, 0, 0.0);
            }
         }

         if (this.glowEnabled && this.isEnabled()) {
            var2 = this.players.iterator();

            while(var2.hasNext()) {
               player = (Player)var2.next();
               int color = this.getGlowColorForPlayer(player);
               FakeGlowManager.setGlow(player, "PlayerEsp", true, color, (double)this.maxDistance);
            }
         } else {
            var2 = this.players.iterator();

            while(var2.hasNext()) {
               player = (Player)var2.next();
               FakeGlowManager.setGlow(player, "PlayerEsp", false, 0, 0.0);
            }
         }

      }
   }

   private int getGlowColorForPlayer(Player player) {
      if (this.forceTeamColor && player.m_5647_() != null) {
         String teamName = player.m_5647_().m_5758_().toLowerCase();
         if (teamName.contains("red")) {
            return 16711680;
         } else if (teamName.contains("blue")) {
            return 255;
         } else if (teamName.contains("green")) {
            return 65280;
         } else if (teamName.contains("yellow")) {
            return 16776960;
         } else if (teamName.contains("purple")) {
            return 8388736;
         } else if (teamName.contains("cyan")) {
            return 65535;
         } else if (teamName.contains("orange")) {
            return 16753920;
         } else {
            return teamName.contains("pink") ? 16761035 : 16777215;
         }
      } else {
         return this.glowColor;
      }
   }

   public void onClick() {
      this.toggle();
   }

   public boolean isGlowEnabled() {
      return this.isEnabled() && this.glowEnabled;
   }

   public int getMaxDistance() {
      return this.maxDistance;
   }

   public void onRender(PoseStack poseStack, float partialTicks) {
      if (this.isEnabled() && !this.players.isEmpty() && mc.f_91074_ != null) {
         List boxes = new ArrayList();
         Iterator var4 = this.players.iterator();

         Player player;
         while(var4.hasNext()) {
            player = (Player)var4.next();
            boxes.add(player.m_20191_().m_82400_(0.1));
         }

         if (this.espMode.showSides()) {
            RenderUtils.drawSolidBoxes(poseStack, boxes, this.sidesColor.getPacked(), false);
         }

         if (this.espMode.showBox()) {
            var4 = this.players.iterator();

            while(var4.hasNext()) {
               player = (Player)var4.next();
               Color boxRenderColor = this.getBoxColor(player);
               List singleBox = List.of(player.m_20191_().m_82400_(0.1));
               RenderUtils.drawOutlinedBoxes(poseStack, singleBox, boxRenderColor.getPacked(), false);
            }
         }

         if (this.espMode.showLines()) {
            List centers = new ArrayList();
            Iterator var9 = this.players.iterator();

            while(var9.hasNext()) {
               Player player = (Player)var9.next();
               centers.add(player.m_20191_().m_82399_());
            }

            RenderUtils.drawTracers(poseStack, partialTicks, centers, this.linesColor.getPacked(), false);
         }

      }
   }

   private Color getBoxColor(Player player) {
      if (this.forceTeamColor && player.m_5647_() != null) {
         String teamName = player.m_5647_().m_5758_().toLowerCase();
         int rgb;
         if (teamName.contains("red")) {
            rgb = 16711680;
         } else if (teamName.contains("blue")) {
            rgb = 255;
         } else if (teamName.contains("green")) {
            rgb = 65280;
         } else if (teamName.contains("yellow")) {
            rgb = 16776960;
         } else if (teamName.contains("purple")) {
            rgb = 8388736;
         } else if (teamName.contains("cyan")) {
            rgb = 65535;
         } else if (teamName.contains("orange")) {
            rgb = 16753920;
         } else if (teamName.contains("pink")) {
            rgb = 16761035;
         } else {
            rgb = 16777215;
         }

         return new Color(rgb >> 16 & 255, rgb >> 8 & 255, rgb & 255, this.boxColor.a);
      } else {
         return this.boxColor;
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
