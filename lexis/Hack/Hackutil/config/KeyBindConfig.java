package lexis.Hack.Hackutil.config;

import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class KeyBindConfig {
   private static final File CONFIG_FILE = new File("C:/karucn/Lexis/config/hack/keybinds.json");
   private static KeyBindConfig INSTANCE;
   private Map keyBinds = new HashMap();

   public static KeyBindConfig getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new KeyBindConfig();
         INSTANCE.load();
      }

      return INSTANCE;
   }

   public void load() {
      Type type = (new TypeToken() {
      }).getType();
      Map loaded = (Map)ConfigUtils.readConfig(CONFIG_FILE, type);
      if (loaded != null) {
         this.keyBinds = loaded;
      } else {
         this.save();
      }

   }

   public void save() {
      ConfigUtils.saveConfig(CONFIG_FILE, this.keyBinds);
   }

   public Map getKeyBinds() {
      return this.keyBinds;
   }

   public void setKeyBind(String hackName, int keyCode) {
      if (keyCode == -1) {
         this.keyBinds.remove(hackName);
      } else {
         this.keyBinds.put(hackName, keyCode);
      }

      this.save();
   }
}
