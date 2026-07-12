package lexis.Hack.Hackutil.config;

import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class KillauraConfig {
   private static final File CONFIG_FILE = new File("C:/karucn/Lexis/config/hack/killaura.json");
   private static KillauraConfig INSTANCE;
   private Set whitelist = new HashSet();

   public static KillauraConfig getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new KillauraConfig();
         INSTANCE.load();
      }

      return INSTANCE;
   }

   public void load() {
      Type type = (new TypeToken() {
      }).getType();
      Set loaded = (Set)ConfigUtils.readConfig(CONFIG_FILE, type);
      if (loaded != null) {
         this.whitelist = loaded;
      } else {
         this.whitelist.add("minecraft:player");
         this.save();
      }

   }

   public void save() {
      ConfigUtils.saveConfig(CONFIG_FILE, this.whitelist);
   }

   public Set getWhitelist() {
      return this.whitelist;
   }

   public void setWhitelist(Set whitelist) {
      this.whitelist = whitelist;
      this.save();
   }
}
