package lexis.Hack.Utils.ThemeColors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;
import lexis.Hack.HackGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class ThemeManager {
   private static ThemeColors colors = new ThemeColors();
   private static final File CONFIG_FILE = new File("C:/karucn/Lexis/config/hack/HackGuiColor.json");
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();

   public static void load() {
      try {
         if (!CONFIG_FILE.exists()) {
            colors = new ThemeColors();
            save(colors);
            return;
         }

         FileReader reader = new FileReader(CONFIG_FILE);
         Map data = (Map)GSON.fromJson(reader, Map.class);
         reader.close();
         if (data != null) {
            colors = new ThemeColors();
            colors.fromMap(data);
         } else {
            colors = new ThemeColors();
            save(colors);
         }
      } catch (Exception var2) {
         var2.printStackTrace();
         colors = new ThemeColors();
         save(colors);
      }

   }

   public static void save(ThemeColors newColors) {
      colors = newColors;

      try {
         CONFIG_FILE.getParentFile().mkdirs();
         FileWriter writer = new FileWriter(CONFIG_FILE);
         GSON.toJson(colors.toMap(), writer);
         writer.close();
         Screen currentScreen = Minecraft.m_91087_().f_91080_;
         if (currentScreen instanceof HackGui) {
            HackGui newGui = new HackGui();
            newGui.setGuiVisible(((HackGui)currentScreen).isGuiVisible());
            Minecraft.m_91087_().m_91152_(newGui);
         }
      } catch (Exception var4) {
         var4.printStackTrace();
      }

   }

   public static ThemeColors getColors() {
      return colors;
   }
}
