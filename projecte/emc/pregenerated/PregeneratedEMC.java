package moze_intel.projecte.emc.pregenerated;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.emc.json.NSSSerializer;

public class PregeneratedEMC {
   private static final Gson gson;

   public static boolean tryRead(File f, Map map) {
      try {
         Map m = read(f);
         map.clear();
         map.putAll(m);
         return true;
      } catch (IOException var3) {
         throw new RuntimeException(var3);
      }
   }

   private static Map read(File file) throws IOException {
      Type type = (new TypeToken() {
      }).getType();
      BufferedReader reader = new BufferedReader(new FileReader(file));

      Map var4;
      try {
         Map map = (Map)gson.fromJson(reader, type);
         map.remove((Object)null);
         var4 = map;
      } catch (Throwable var6) {
         try {
            reader.close();
         } catch (Throwable var5) {
            var6.addSuppressed(var5);
         }

         throw var6;
      }

      reader.close();
      return var4;
   }

   public static void write(File file, Map map) throws IOException {
      Type type = (new TypeToken() {
      }).getType();
      BufferedWriter writer = new BufferedWriter(new FileWriter(file));

      try {
         gson.toJson(map, type, writer);
      } catch (Throwable var7) {
         try {
            writer.close();
         } catch (Throwable var6) {
            var7.addSuppressed(var6);
         }

         throw var7;
      }

      writer.close();
   }

   static {
      gson = (new GsonBuilder()).registerTypeAdapter(NormalizedSimpleStack.class, NSSSerializer.INSTANCE).enableComplexMapKeySerialization().setPrettyPrinting().create();
   }
}
