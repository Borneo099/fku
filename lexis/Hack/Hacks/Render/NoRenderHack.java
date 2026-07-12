package lexis.Hack.Hacks.Render;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;

public class NoRenderHack extends Hack {
   private HackConfig config;
   private static final String CONFIG_KEY = "不渲染";
   private boolean noPortalOverlay = false;
   private boolean noSpyglassOverlay = false;
   private boolean noPotionIcons = false;
   private boolean noBossBar = false;
   private boolean noScoreboard = false;
   private boolean noCrosshair = false;
   private boolean noWeather = false;
   private boolean noWorldBorder = false;
   private boolean noFog = false;
   private boolean noEnchTableBook = false;
   private boolean noBeaconBeams = false;
   private boolean noParticles = false;
   private boolean noArmor = false;

   public NoRenderHack() {
      super("不渲染", new String[]{"禁用各种渲染效果", "§c§l警告：如果其地mod有这功能问题可能会冲突 闪退/崩溃游戏"}, Hack.Category.RENDER, true);
      this.addSetting(new Hack.Setting("禁用传送门覆盖", "进下界时屏幕上那个紫色漩涡效果", false));
      this.addSetting(new Hack.Setting("禁用望远镜覆盖", "用望远镜时屏幕周围的黑色圆圈", false));
      this.addSetting(new Hack.Setting("禁用Boss血条", "BOSS时屏幕上方的血条", false));
      this.addSetting(new Hack.Setting("禁用计分板", "屏幕右侧的计分板", false));
      this.addSetting(new Hack.Setting("禁用准星", "屏幕中心的十字准星", false));
      this.addSetting(new Hack.Setting("禁用药水图标", "屏幕右上角的状态效果图标", false));
      this.addSetting(new Hack.Setting("禁用天气", "雨、雪等天气效果", false));
      this.addSetting(new Hack.Setting("禁用世界边界", "世界边界的那道红色光墙", false));
      this.addSetting(new Hack.Setting("禁用雾", "远处的雾效果", false));
      this.addSetting(new Hack.Setting("禁用附魔台书本", "附魔台上方飘动的书本", false));
      this.addSetting(new Hack.Setting("禁用信标光束", "信标发出的光束", false));
      this.addSetting(new Hack.Setting("禁用粒子", "各种粒子效果（破坏方块、药水效果等）", false));
      this.addSetting(new Hack.Setting("禁用盔甲", "实体身上的盔甲渲染", false));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.noPortalOverlay = this.config.getBooleanSetting("不渲染", "禁用传送门覆盖", false);
      this.noSpyglassOverlay = this.config.getBooleanSetting("不渲染", "禁用望远镜覆盖", false);
      this.noPotionIcons = this.config.getBooleanSetting("不渲染", "禁用药水图标", false);
      this.noBossBar = this.config.getBooleanSetting("不渲染", "禁用Boss血条", false);
      this.noScoreboard = this.config.getBooleanSetting("不渲染", "禁用计分板", false);
      this.noCrosshair = this.config.getBooleanSetting("不渲染", "禁用准星", false);
      this.noWeather = this.config.getBooleanSetting("不渲染", "禁用天气", false);
      this.noWorldBorder = this.config.getBooleanSetting("不渲染", "禁用世界边界", false);
      this.noFog = this.config.getBooleanSetting("不渲染", "禁用雾", false);
      this.noEnchTableBook = this.config.getBooleanSetting("不渲染", "禁用附魔台书本", false);
      this.noBeaconBeams = this.config.getBooleanSetting("不渲染", "禁用信标光束", false);
      this.noParticles = this.config.getBooleanSetting("不渲染", "禁用粒子", false);
      this.noArmor = this.config.getBooleanSetting("不渲染", "禁用盔甲", false);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "禁用传送门覆盖":
               setting.setValue(this.noPortalOverlay);
               break;
            case "禁用望远镜覆盖":
               setting.setValue(this.noSpyglassOverlay);
               break;
            case "禁用药水图标":
               setting.setValue(this.noPotionIcons);
               break;
            case "禁用Boss血条":
               setting.setValue(this.noBossBar);
               break;
            case "禁用计分板":
               setting.setValue(this.noScoreboard);
               break;
            case "禁用准星":
               setting.setValue(this.noCrosshair);
               break;
            case "禁用天气":
               setting.setValue(this.noWeather);
               break;
            case "禁用世界边界":
               setting.setValue(this.noWorldBorder);
               break;
            case "禁用雾":
               setting.setValue(this.noFog);
               break;
            case "禁用附魔台书本":
               setting.setValue(this.noEnchTableBook);
               break;
            case "禁用信标光束":
               setting.setValue(this.noBeaconBeams);
               break;
            case "禁用粒子":
               setting.setValue(this.noParticles);
               break;
            case "禁用盔甲":
               setting.setValue(this.noArmor);
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

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         switch (setting.getName()) {
            case "禁用传送门覆盖":
               if (setting.getBoolean() != this.noPortalOverlay) {
                  this.noPortalOverlay = setting.getBoolean();
                  needSave = true;
               }
               break;
            case "禁用望远镜覆盖":
               if (setting.getBoolean() != this.noSpyglassOverlay) {
                  this.noSpyglassOverlay = setting.getBoolean();
                  needSave = true;
               }
               break;
            case "禁用药水图标":
               if (setting.getBoolean() != this.noPotionIcons) {
                  this.noPotionIcons = setting.getBoolean();
                  needSave = true;
               }
               break;
            case "禁用Boss血条":
               if (setting.getBoolean() != this.noBossBar) {
                  this.noBossBar = setting.getBoolean();
                  needSave = true;
               }
               break;
            case "禁用计分板":
               if (setting.getBoolean() != this.noScoreboard) {
                  this.noScoreboard = setting.getBoolean();
                  needSave = true;
               }
               break;
            case "禁用准星":
               if (setting.getBoolean() != this.noCrosshair) {
                  this.noCrosshair = setting.getBoolean();
                  needSave = true;
               }
               break;
            case "禁用天气":
               if (setting.getBoolean() != this.noWeather) {
                  this.noWeather = setting.getBoolean();
                  needSave = true;
               }
               break;
            case "禁用世界边界":
               if (setting.getBoolean() != this.noWorldBorder) {
                  this.noWorldBorder = setting.getBoolean();
                  needSave = true;
               }
               break;
            case "禁用雾":
               if (setting.getBoolean() != this.noFog) {
                  this.noFog = setting.getBoolean();
                  needSave = true;
               }
               break;
            case "禁用附魔台书本":
               if (setting.getBoolean() != this.noEnchTableBook) {
                  this.noEnchTableBook = setting.getBoolean();
                  needSave = true;
               }
               break;
            case "禁用信标光束":
               if (setting.getBoolean() != this.noBeaconBeams) {
                  this.noBeaconBeams = setting.getBoolean();
                  needSave = true;
               }
               break;
            case "禁用粒子":
               if (setting.getBoolean() != this.noParticles) {
                  this.noParticles = setting.getBoolean();
                  needSave = true;
               }
               break;
            case "禁用盔甲":
               if (setting.getBoolean() != this.noArmor) {
                  this.noArmor = setting.getBoolean();
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.config.saveHackSettings("不渲染", this.getSettings());
      }

   }

   public void onClick() {
      this.toggle();
   }

   public boolean noPortalOverlay() {
      return this.isEnabled() && this.noPortalOverlay;
   }

   public boolean noSpyglassOverlay() {
      return this.isEnabled() && this.noSpyglassOverlay;
   }

   public boolean noPotionIcons() {
      return this.isEnabled() && this.noPotionIcons;
   }

   public boolean noBossBar() {
      return this.isEnabled() && this.noBossBar;
   }

   public boolean noScoreboard() {
      return this.isEnabled() && this.noScoreboard;
   }

   public boolean noCrosshair() {
      return this.isEnabled() && this.noCrosshair;
   }

   public boolean noWeather() {
      return this.isEnabled() && this.noWeather;
   }

   public boolean noWorldBorder() {
      return this.isEnabled() && this.noWorldBorder;
   }

   public boolean noFog() {
      return this.isEnabled() && this.noFog;
   }

   public boolean noEnchTableBook() {
      return this.isEnabled() && this.noEnchTableBook;
   }

   public boolean noBeaconBeams() {
      return this.isEnabled() && this.noBeaconBeams;
   }

   public boolean noParticles() {
      return this.isEnabled() && this.noParticles;
   }

   public boolean noArmor() {
      return this.isEnabled() && this.noArmor;
   }
}
