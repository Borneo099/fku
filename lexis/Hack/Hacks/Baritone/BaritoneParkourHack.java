package lexis.Hack.Hacks.Baritone;

import java.lang.reflect.Field;
import java.util.List;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.BaritoneBridge;

public class BaritoneParkourHack extends Hack {
   public static boolean enabled = false;
   private static final String[][] SETTING_DEFS = new String[][]{{"allowBreak", "false", "允许破坏", "寻路时允许破坏方块"}, {"allowPlace", "false", "允许放置", "寻路时允许放置方块"}, {"allowSprint", "true", "允许疾跑", "寻路时允许疾跑"}, {"allowParkour", "true", "允许跑酷", "寻路时允许跑酷跳跃"}, {"allowParkourPlace", "false", "允许跑酷放置", "跑酷时允许放置辅助方块"}, {"allowInventory", "false", "允许背包操作", "寻路时允许管理背包"}};
   private final Boolean[] savedValues;
   private final boolean[] currentApplied;
   private boolean hackActive;
   private final HackConfig config;

   public BaritoneParkourHack() {
      super("Baritone跑酷模式", "跑酷大神更大神！", Hack.Category.BARITONE, true);
      this.savedValues = new Boolean[SETTING_DEFS.length];
      this.currentApplied = new boolean[SETTING_DEFS.length];
      this.hackActive = false;
      String[][] var1 = SETTING_DEFS;
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         String[] def = var1[var3];
         this.addSetting(new Hack.Setting(def[2], def[3], Boolean.parseBoolean(def[1])));
      }

      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      List settings = this.getSettings();

      for(int i = 0; i < SETTING_DEFS.length; ++i) {
         boolean val = this.config.getBooleanSetting(this.getName(), SETTING_DEFS[i][2], Boolean.parseBoolean(SETTING_DEFS[i][1]));
         ((Hack.Setting)settings.get(i)).setValue(val);
      }

   }

   public void onEnable() {
      enabled = true;
      this.saveAndApply();
   }

   public void onDisable() {
      enabled = false;
      this.restore();
   }

   public void onUpdate() {
      if (this.hackActive && mc.f_91074_ != null) {
         boolean needSave = false;
         List settings = this.getSettings();

         for(int i = 0; i < SETTING_DEFS.length; ++i) {
            boolean wanted = ((Hack.Setting)settings.get(i)).getBoolean();
            if (wanted != this.currentApplied[i]) {
               String var10000 = SETTING_DEFS[i][0];
               execBaritoneCommand("set " + var10000 + " " + wanted);
               this.currentApplied[i] = wanted;
               needSave = true;
            }
         }

         if (needSave) {
            this.config.saveHackSettings(this.getName(), this.getSettings());
         }

      }
   }

   public void onClick() {
      this.toggle();
   }

   public String getDisplayName() {
      boolean active = BaritoneBridge.isActive();
      String var10000 = this.getName();
      return var10000 + (enabled ? " §d[ON]" : "") + (active ? " §6正在运行中" : "");
   }

   private void saveAndApply() {
      if (!this.hackActive) {
         if (BaritoneBridge.isAvailable()) {
            List settings = this.getSettings();

            for(int i = 0; i < SETTING_DEFS.length; ++i) {
               String name = SETTING_DEFS[i][0];
               this.savedValues[i] = readBaritoneSetting(name);
               boolean wanted = ((Hack.Setting)settings.get(i)).getBoolean();
               execBaritoneCommand("set " + name + " " + wanted);
               this.currentApplied[i] = wanted;
            }

            this.hackActive = true;
         }
      }
   }

   private void restore() {
      if (this.hackActive) {
         if (BaritoneBridge.isAvailable()) {
            for(int i = 0; i < SETTING_DEFS.length; ++i) {
               if (this.savedValues[i] != null) {
                  String name = SETTING_DEFS[i][0];
                  execBaritoneCommand("set " + name + " " + this.savedValues[i]);
                  this.savedValues[i] = null;
               }
            }

            this.hackActive = false;
         }
      }
   }

   private static void execBaritoneCommand(String cmd) {
      try {
         BaritoneBridge.suppressNextSetMessage();
         Class apiClass = Class.forName("baritone.api.BaritoneAPI");
         Object provider = apiClass.getMethod("getProvider").invoke((Object)null);
         Object baritone = provider.getClass().getMethod("getPrimaryBaritone").invoke(provider);
         Object cmdManager = baritone.getClass().getMethod("getCommandManager").invoke(baritone);
         cmdManager.getClass().getMethod("execute", String.class).invoke(cmdManager, cmd);
      } catch (Throwable var5) {
      }

   }

   private static Boolean readBaritoneSetting(String fieldName) {
      try {
         Class apiClass = Class.forName("baritone.api.BaritoneAPI");
         Object provider = apiClass.getMethod("getProvider").invoke((Object)null);
         Object baritone = provider.getClass().getMethod("getPrimaryBaritone").invoke(provider);
         Object settings = baritone.getClass().getMethod("getSettings").invoke(baritone);
         Field field = settings.getClass().getDeclaredField(fieldName);
         field.setAccessible(true);
         Object setting = field.get(settings);
         Field valueField = setting.getClass().getDeclaredField("value");
         valueField.setAccessible(true);
         return (Boolean)valueField.get(setting);
      } catch (Throwable var8) {
         return true;
      }
   }
}
