package lexis.Hack.Hackutil.Notebot;

import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import lexis.Hack.Hackutil.config.ConfigUtils;

public class NotebotConfig {
   private static final File CONFIG_FILE = new File("C:/karucn/Lexis/config/hack/Notebot/settings.json");
   private static NotebotConfig INSTANCE;
   private Map settings = new HashMap();

   public static NotebotConfig getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new NotebotConfig();
         INSTANCE.load();
      }

      return INSTANCE;
   }

   public void load() {
      Type type = (new TypeToken() {
      }).getType();
      Map loaded = (Map)ConfigUtils.readConfig(CONFIG_FILE, type);
      if (loaded != null) {
         this.settings = loaded;
      } else {
         this.save();
      }

   }

   public void save() {
      ConfigUtils.saveConfig(CONFIG_FILE, this.settings);
   }

   public int getIntSetting(String hackName, String settingName, int defaultValue) {
      Map hackSettings = this.getHackSettings(hackName);
      Object value = hackSettings.get(settingName);
      return value instanceof Number ? ((Number)value).intValue() : defaultValue;
   }

   public double getDoubleSetting(String hackName, String settingName, double defaultValue) {
      Map hackSettings = this.getHackSettings(hackName);
      Object value = hackSettings.get(settingName);
      return value instanceof Number ? ((Number)value).doubleValue() : defaultValue;
   }

   public boolean getBooleanSetting(String hackName, String settingName, boolean defaultValue) {
      Map hackSettings = this.getHackSettings(hackName);
      Object value = hackSettings.get(settingName);
      return value instanceof Boolean ? (Boolean)value : defaultValue;
   }

   public String getStringSetting(String hackName, String settingName, String defaultValue) {
      Map hackSettings = this.getHackSettings(hackName);
      Object value = hackSettings.get(settingName);
      return value instanceof String ? (String)value : defaultValue;
   }

   private Map getHackSettings(String hackName) {
      Map hackSettings = (Map)this.settings.get(hackName);
      if (hackSettings == null) {
         hackSettings = new HashMap();
         this.settings.put(hackName, hackSettings);
      }

      return (Map)hackSettings;
   }

   public void saveHackSettings(String hackName, Map hackSettings) {
      this.settings.put(hackName, hackSettings);
      this.save();
   }
}
