package lexis.Hack.Hackutil.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileWriter;

public class LexisLogoConfig {
   private static final File CONFIG_FILE = new File("C:/karucn/Lexis/config/Logo/Lexis_Logo.json");
   private static LexisLogoConfig INSTANCE;
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
   public boolean enabled = true;
   public int textColorR = 255;
   public int textColorG = 255;
   public int textColorB = 255;
   public int bgColorR = 0;
   public int bgColorG = 0;
   public int bgColorB = 0;
   public int bgAlpha = 180;

   public static LexisLogoConfig getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new LexisLogoConfig();
         INSTANCE.load();
      }

      return INSTANCE;
   }

   public void load() {
      LexisLogoConfig loaded = (LexisLogoConfig)ConfigUtils.readConfig(CONFIG_FILE, LexisLogoConfig.class);
      if (loaded != null) {
         this.enabled = loaded.enabled;
         this.textColorR = loaded.textColorR;
         this.textColorG = loaded.textColorG;
         this.textColorB = loaded.textColorB;
         this.bgColorR = loaded.bgColorR;
         this.bgColorG = loaded.bgColorG;
         this.bgColorB = loaded.bgColorB;
         this.bgAlpha = loaded.bgAlpha;
      } else {
         this.save();
      }

   }

   public void save() {
      try {
         CONFIG_FILE.getParentFile().mkdirs();
         FileWriter writer = new FileWriter(CONFIG_FILE);
         GSON.toJson(this, writer);
         writer.close();
      } catch (Exception var2) {
         var2.printStackTrace();
      }

   }

   public int getTextColor() {
      return -16777216 | this.textColorR << 16 | this.textColorG << 8 | this.textColorB;
   }

   public int getBackgroundColor() {
      return this.bgAlpha << 24 | this.bgColorR << 16 | this.bgColorG << 8 | this.bgColorB;
   }

   public static String getModVersion() {
      return "1.6.2";
   }
}
