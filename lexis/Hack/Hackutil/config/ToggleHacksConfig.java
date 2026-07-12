package lexis.Hack.Hackutil.config;

import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ToggleHacksConfig {
   private static final File CONFIG_FILE = new File("C:/karucn/Lexis/config/hack/togglehacksfunction.json");
   private static ToggleHacksConfig INSTANCE;
   private Map toggleStates = new HashMap();

   public static ToggleHacksConfig getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new ToggleHacksConfig();
         INSTANCE.load();
      }

      return INSTANCE;
   }

   public void load() {
      Type type = (new TypeToken() {
      }).getType();
      Map loaded = (Map)ConfigUtils.readConfig(CONFIG_FILE, type);
      if (loaded != null) {
         this.toggleStates = loaded;
      } else {
         this.save();
      }

   }

   public void save() {
      ConfigUtils.saveConfig(CONFIG_FILE, this.toggleStates);
   }

   public boolean isEnabled(String hackName) {
      return (Boolean)this.toggleStates.getOrDefault(hackName, false);
   }

   public void setEnabled(String hackName, boolean enabled) {
      this.toggleStates.put(hackName, enabled);
      this.save();
   }

   public Map getAllStates() {
      return this.toggleStates;
   }
}
