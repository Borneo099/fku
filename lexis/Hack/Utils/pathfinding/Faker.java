package lexis.Hack.Utils.pathfinding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Faker {
   private static final File CONFIG_FILE = new File("C:/karucn/Lexis/config/hack/AHT.txt");
   private static Boolean enabled = null;

   public static boolean isEnabled() {
      if (enabled != null) {
         return enabled;
      } else if (!CONFIG_FILE.exists()) {
         enabled = false;
         return false;
      } else {
         try {
            BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE));

            try {
               String line = reader.readLine();
               if (line != null && line.trim().equalsIgnoreCase("true")) {
                  enabled = true;
               } else {
                  enabled = false;
               }
            } catch (Throwable var4) {
               try {
                  reader.close();
               } catch (Throwable var3) {
                  var4.addSuppressed(var3);
               }

               throw var4;
            }

            reader.close();
         } catch (IOException var5) {
            enabled = false;
         }

         return enabled;
      }
   }

   public static void reload() {
      enabled = null;
   }
}
