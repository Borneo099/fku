package lexis.Client.keybind;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class KeyBindConfig {
   private static final File CONFIG_FILE = new File("C:/karucn/Lexis/config/keybind/keybind.json");
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
   private static KeyBindConfig INSTANCE;
   public Map keyBinds = new HashMap();

   public static KeyBindConfig getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new KeyBindConfig();
         INSTANCE.load();
      }

      return INSTANCE;
   }

   public void load() {
      try {
         if (!CONFIG_FILE.exists()) {
            this.save();
            return;
         }

         FileReader reader = new FileReader(CONFIG_FILE);
         Type type = (new TypeToken() {
         }).getType();
         Map loaded = (Map)GSON.fromJson(reader, type);
         reader.close();
         if (loaded != null) {
            this.keyBinds = loaded;
         } else {
            this.save();
         }
      } catch (Exception var4) {
         System.out.println("[Lexis] 加载按键配置失败: " + var4.getMessage());
         this.save();
      }

   }

   public void save() {
      try {
         CONFIG_FILE.getParentFile().mkdirs();
         FileWriter writer = new FileWriter(CONFIG_FILE);
         GSON.toJson(this.keyBinds, writer);
         writer.close();
      } catch (Exception var2) {
         System.out.println("[Lexis] 保存按键配置失败: " + var2.getMessage());
      }

   }

   public static class KeyBindData {
      private String name = "未设置";
      private String command;
      private boolean toggleMode;
      private String toggleValue1;
      private String toggleValue2;
      private boolean isSet = false;

      public String getName() {
         return this.name;
      }

      public void setName(String name) {
         this.name = name;
      }

      public String getCommand() {
         return this.command;
      }

      public void setCommand(String command) {
         this.command = command;
      }

      public boolean isToggleMode() {
         return this.toggleMode;
      }

      public void setToggleMode(boolean toggleMode) {
         this.toggleMode = toggleMode;
      }

      public String getToggleValue1() {
         return this.toggleValue1;
      }

      public void setToggleValue1(String value) {
         this.toggleValue1 = value;
      }

      public String getToggleValue2() {
         return this.toggleValue2;
      }

      public void setToggleValue2(String value) {
         this.toggleValue2 = value;
      }

      public boolean isSet() {
         return this.isSet;
      }

      public void setSet(boolean set) {
         this.isSet = set;
      }

      public void reset() {
         this.isSet = false;
         this.name = "未设置";
         this.command = null;
         this.toggleMode = false;
      }
   }
}
