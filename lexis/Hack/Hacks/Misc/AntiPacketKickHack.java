package lexis.Hack.Hacks.Misc;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;

public class AntiPacketKickHack extends Hack {
   private HackConfig config;
   private static final String CONFIG_KEY = "反数据包踢出";
   private boolean catchExceptions = true;
   private boolean logExceptions = false;
   private boolean limitPackets = true;
   private int packetLimit = 800;
   private int resetTime = 1000;
   public static boolean isLimitEnabled = true;
   public static int currentLimit = 800;
   public static int currentResetTime = 1000;
   public static int packetCount = 0;
   public static boolean isLimited = false;

   public AntiPacketKickHack() {
      super("反数据包踢出", new String[]{"防止异常数据包和数据包过多被服务器踢出", "如果服务器没有限制 数据包过多可能服务器很卡可以拦截这数据包"}, Hack.Category.MISC, true);
      this.addSetting(new Hack.Setting("捕获异常", "丢弃损坏的数据包", true));
      this.addSetting(new Hack.Setting("记录日志", "在控制台输出异常信息", false));
      this.addSetting(new Hack.Setting("限制发包", "自动限制发包速度", true));
      this.addSetting(new Hack.Setting("包数限制", "每秒最大发包数", 800, 100, 5000, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("重置时间", "计数重置时间（毫秒）", 1000, 100, 5000, Hack.ValueDisplay.INTEGER));
      this.config = HackConfig.getInstance();
      this.loadConfig();
      this.updateStaticFields();
   }

   private void loadConfig() {
      this.catchExceptions = this.config.getBooleanSetting("反数据包踢出", "捕获异常", true);
      this.logExceptions = this.config.getBooleanSetting("反数据包踢出", "记录日志", false);
      this.limitPackets = this.config.getBooleanSetting("反数据包踢出", "限制发包", true);
      this.packetLimit = (int)this.config.getDoubleSetting("反数据包踢出", "包数限制", 800.0);
      this.resetTime = (int)this.config.getDoubleSetting("反数据包踢出", "重置时间", 1000.0);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "捕获异常":
               setting.setValue(this.catchExceptions);
               break;
            case "记录日志":
               setting.setValue(this.logExceptions);
               break;
            case "限制发包":
               setting.setValue(this.limitPackets);
               break;
            case "包数限制":
               setting.setValue((double)this.packetLimit);
               break;
            case "重置时间":
               setting.setValue((double)this.resetTime);
         }
      }

   }

   private void updateStaticFields() {
      isLimitEnabled = this.isEnabled() && this.limitPackets;
      currentLimit = this.packetLimit;
      currentResetTime = this.resetTime;
   }

   public static void resetPacketCount() {
      packetCount = 0;
      isLimited = false;
   }

   public void onEnable() {
      resetPacketCount();
      this.updateStaticFields();
   }

   public void onDisable() {
      resetPacketCount();
      isLimitEnabled = false;
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         switch (setting.getName()) {
            case "捕获异常":
               if (setting.getBoolean() != this.catchExceptions) {
                  this.catchExceptions = setting.getBoolean();
                  needSave = true;
               }
               break;
            case "记录日志":
               if (setting.getBoolean() != this.logExceptions) {
                  this.logExceptions = setting.getBoolean();
                  needSave = true;
               }
               break;
            case "限制发包":
               if (setting.getBoolean() != this.limitPackets) {
                  this.limitPackets = setting.getBoolean();
                  needSave = true;
               }
               break;
            case "包数限制":
               int newLimit = (int)setting.getDouble();
               if (newLimit != this.packetLimit) {
                  this.packetLimit = newLimit;
                  needSave = true;
               }
               break;
            case "重置时间":
               int newTime = (int)setting.getDouble();
               if (newTime != this.resetTime) {
                  this.resetTime = newTime;
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.config.saveHackSettings("反数据包踢出", this.getSettings());
         this.updateStaticFields();
      }

   }

   public boolean catchExceptions() {
      return this.isEnabled() && this.catchExceptions;
   }

   public boolean logExceptions() {
      return this.isEnabled() && this.logExceptions;
   }

   public void onClick() {
      this.toggle();
   }
}
