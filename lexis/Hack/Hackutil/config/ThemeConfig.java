package lexis.Hack.Hackutil.config;

import java.io.File;

public class ThemeConfig {
   private static final File CONFIG_FILE = new File("C:/karucn/Lexis/config/hack/theme.json");
   private static ThemeConfig INSTANCE;
   private int currentTheme = 0;
   private int currentColor = -769226;
   private String themeType = "SOLID";

   public static ThemeConfig getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new ThemeConfig();
         INSTANCE.load();
      }

      return INSTANCE;
   }

   public void load() {
      ThemeConfig loaded = (ThemeConfig)ConfigUtils.readConfig(CONFIG_FILE, ThemeConfig.class);
      if (loaded != null) {
         this.currentTheme = loaded.currentTheme;
         this.currentColor = loaded.currentColor;
         this.themeType = loaded.themeType;
      } else {
         this.save();
      }

   }

   public void save() {
      ConfigUtils.saveConfig(CONFIG_FILE, this);
   }

   public int getCurrentTheme() {
      return this.currentTheme;
   }

   public void setCurrentTheme(int theme) {
      this.currentTheme = theme;
      this.save();
   }

   public int getCurrentColor() {
      return this.currentColor;
   }

   public void setCurrentColor(int color) {
      this.currentColor = color;
      this.save();
   }

   public String getThemeType() {
      return this.themeType;
   }

   public void setThemeType(String type) {
      this.themeType = type;
      this.save();
   }

   public static enum ThemeType {
      SOLID,
      RAINBOW,
      RAINBOW_FLOW,
      RAINBOW_SHIFT;

      // $FF: synthetic method
      private static ThemeType[] $values() {
         return new ThemeType[]{SOLID, RAINBOW, RAINBOW_FLOW, RAINBOW_SHIFT};
      }
   }
}
