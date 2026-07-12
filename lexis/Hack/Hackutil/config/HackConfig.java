package lexis.Hack.Hackutil.config;

import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import lexis.Hack.Hack;

public class HackConfig {
   private static final File CONFIG_FILE = new File("C:/karucn/Lexis/config/hack/gui.json");
   private static final File SETTINGS_FILE = new File("C:/karucn/Lexis/config/hack/settings.json");
   private static HackConfig INSTANCE;
   private Map windowPositions = new HashMap();
   private Map hackSettings = new HashMap();

   public static HackConfig getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new HackConfig();
         INSTANCE.load();
         INSTANCE.loadSettings();
      }

      return INSTANCE;
   }

   public void load() {
      Type type = (new TypeToken() {
      }).getType();
      Map loaded = (Map)ConfigUtils.readConfig(CONFIG_FILE, type);
      if (loaded != null && !loaded.isEmpty()) {
         this.windowPositions = loaded;
      } else {
         this.setAutoLayout();
         this.save();
      }

   }

   private void setAutoLayout() {
      this.windowPositions.clear();
      String[] categories = new String[]{"COMBAT", "MOVEMENT", "RENDER", "WORLD", "CHAT", "BLOCKS", "FUN", "ITEMS", "MISC", "LEXIS"};

      int i;
      WindowPos pos;
      for(i = 0; i < 5; ++i) {
         pos = new WindowPos();
         pos.x = 50 + i * 140;
         pos.y = 50;
         this.windowPositions.put(categories[i], pos);
      }

      for(i = 5; i < 10; ++i) {
         pos = new WindowPos();
         pos.x = 50 + (i - 5) * 140;
         pos.y = 300;
         this.windowPositions.put(categories[i], pos);
      }

   }

   public void save() {
      ConfigUtils.saveConfig(CONFIG_FILE, this.windowPositions);
   }

   public void loadSettings() {
      Type type = (new TypeToken() {
      }).getType();
      Map loaded = (Map)ConfigUtils.readConfig(SETTINGS_FILE, type);
      if (loaded != null) {
         this.hackSettings = loaded;
      } else {
         this.saveSettings();
      }

   }

   public void saveSettings() {
      ConfigUtils.saveConfig(SETTINGS_FILE, this.hackSettings);
   }

   public void saveHackSettings(Hack hack) {
      this.saveHackSettings(hack.getName(), hack.getSettings());
   }

   public void saveHackSettings(String hackFixedName, List settings) {
      Map settingMap = new HashMap();
      Iterator var4 = settings.iterator();

      while(var4.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var4.next();
         settingMap.put(setting.getName(), setting.getValue());
      }

      this.hackSettings.put(hackFixedName, settingMap);
      this.saveSettings();
   }

   public Map getHackSettings(String hackFixedName) {
      return (Map)this.hackSettings.getOrDefault(hackFixedName, new HashMap());
   }

   public int getIntSetting(String hackFixedName, String settingName, int defaultValue) {
      Object value = this.getHackSettings(hackFixedName).get(settingName);
      return value instanceof Number ? ((Number)value).intValue() : defaultValue;
   }

   public double getDoubleSetting(String hackFixedName, String settingName, double defaultValue) {
      Object value = this.getHackSettings(hackFixedName).get(settingName);
      return value instanceof Number ? ((Number)value).doubleValue() : defaultValue;
   }

   public boolean getBooleanSetting(String hackFixedName, String settingName, boolean defaultValue) {
      Object value = this.getHackSettings(hackFixedName).get(settingName);
      return value instanceof Boolean ? (Boolean)value : defaultValue;
   }

   public String getStringSetting(String hackFixedName, String settingName, String defaultValue) {
      Object value = this.getHackSettings(hackFixedName).get(settingName);
      return value instanceof String ? (String)value : defaultValue;
   }

   public int getWindowX(String category) {
      WindowPos pos = (WindowPos)this.windowPositions.get(category);
      return pos != null ? pos.x : 50;
   }

   public int getWindowY(String category) {
      WindowPos pos = (WindowPos)this.windowPositions.get(category);
      return pos != null ? pos.y : 50;
   }

   public boolean isWindowCollapsed(String category) {
      WindowPos pos = (WindowPos)this.windowPositions.get(category);
      return pos != null ? pos.collapsed : false;
   }

   public void setWindowPos(String category, int x, int y) {
      ((WindowPos)this.windowPositions.computeIfAbsent(category, (k) -> {
         return new WindowPos();
      })).x = x;
      ((WindowPos)this.windowPositions.computeIfAbsent(category, (k) -> {
         return new WindowPos();
      })).y = y;
   }

   public void setWindowCollapsed(String category, boolean collapsed) {
      ((WindowPos)this.windowPositions.computeIfAbsent(category, (k) -> {
         return new WindowPos();
      })).collapsed = collapsed;
   }

   public void resetAllWindows() {
      this.setAutoLayout();
      this.save();
   }

   private static class WindowPos {
      int x = 50;
      int y = 50;
      boolean collapsed = false;
   }
}
