package lexis.Hack.Hackutil.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ConfigUtils {
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
   private static final int MAX_RETRIES = 3;
   private static final long[] RETRY_DELAYS = new long[]{100L, 300L, 700L};

   private static Object readConfigWithRetry(File file, Object typeOrClass, boolean isClass) {
      File tmp = new File(file.getParentFile(), file.getName() + ".tmp");
      Object result;
      if (!file.exists()) {
         if (tmp.exists()) {
            System.out.println("[Config] 检测到残留临时文件，尝试恢复: " + file.getName());
            result = tryReadFile(tmp, typeOrClass, isClass);
            if (result != null) {
               saveConfig(file, result);
               tmp.delete();
               System.out.println("[Config] 临时文件恢复成功: " + file.getName());
               return result;
            }

            tmp.delete();
         }

         return null;
      } else {
         result = tryReadFile(file, typeOrClass, isClass);
         if (result != null) {
            if (tmp.exists()) {
               tmp.delete();
            }

            return result;
         } else {
            if (tmp.exists()) {
               System.out.println("[Config] 主文件损坏，尝试从临时文件恢复: " + file.getName());
               result = tryReadFile(tmp, typeOrClass, isClass);
               if (result != null) {
                  saveConfig(file, result);
                  tmp.delete();
                  System.out.println("[Config] 从临时文件恢复成功: " + file.getName());
                  return result;
               }

               tmp.delete();
            }

            System.out.println("[Config] 读取失败(文件已损坏): " + file.getName());
            return null;
         }
      }
   }

   private static Object tryReadFile(File file, Object typeOrClass, boolean isClass) {
      Exception lastException = null;
      int attempt = 0;

      PrintStream var10000;
      String var10001;
      while(attempt < 3) {
         try {
            Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);

            Object var13;
            try {
               Object result;
               if (isClass) {
                  Class clazz = (Class)typeOrClass;
                  result = GSON.fromJson(reader, clazz);
               } else {
                  Type type = (Type)typeOrClass;
                  result = GSON.fromJson(reader, type);
               }

               if (attempt > 0) {
                  var10000 = System.out;
                  var10001 = file.getName();
                  var10000.println("[Config] 重试成功: " + var10001 + " (第" + (attempt + 1) + "次)");
               }

               var13 = result;
            } catch (Throwable var10) {
               try {
                  reader.close();
               } catch (Throwable var9) {
                  var10.addSuppressed(var9);
               }

               throw var10;
            }

            reader.close();
            return var13;
         } catch (Exception var11) {
            lastException = var11;
            if (attempt < 2) {
               System.out.println("[Config] 读取失败，准备重试(" + (attempt + 2) + "/3): " + file.getName() + " - " + var11.getMessage());

               try {
                  Thread.sleep(RETRY_DELAYS[attempt]);
               } catch (InterruptedException var8) {
               }
            }

            ++attempt;
         }
      }

      var10000 = System.out;
      var10001 = file.getName();
      var10000.println("[Config] 读取失败(已重试3次): " + var10001 + " - " + lastException.getMessage());
      return null;
   }

   public static Object readConfig(File file, Class clazz) {
      return readConfigWithRetry(file, clazz, true);
   }

   public static Object readConfig(File file, Type type) {
      return readConfigWithRetry(file, type, false);
   }

   public static void saveConfig(File file, Object obj) {
      try {
         file.getParentFile().mkdirs();
         File tmp = new File(file.getParentFile(), file.getName() + ".tmp");
         Writer writer = new OutputStreamWriter(new FileOutputStream(tmp), StandardCharsets.UTF_8);

         try {
            GSON.toJson(obj, writer);
         } catch (Throwable var7) {
            try {
               writer.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }

            throw var7;
         }

         writer.close();
         if (file.exists()) {
            file.delete();
         }

         Files.move(tmp.toPath(), file.toPath());
      } catch (Exception var8) {
         PrintStream var10000 = System.out;
         String var10001 = file.getName();
         var10000.println("[Config] 保存失败: " + var10001 + " - " + var8.getMessage());
      }

   }
}
