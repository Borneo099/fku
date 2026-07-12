package lexis.Client.OOCCommand.CommandBlockOOC;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CommandBlockOOCConfig {
   private static final String CONFIG_PATH = "C:/karucn/Lexis/config/CommandBlock_OOC/config.json";
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
   private static CommandBlockOOCConfig instance;
   private List commands = new ArrayList();

   public CommandBlockOOCConfig() {
      this.commands.add("say 输入你命令第一个");
      this.commands.add("say 输入你命令第二个");
      this.commands.add("say 输入你命令第三个");
   }

   public static CommandBlockOOCConfig getInstance() {
      if (instance == null) {
         instance = load();
      }

      return instance;
   }

   public List getCommands() {
      return this.commands;
   }

   public void setCommands(List commands) {
      this.commands = commands;
      this.save();
   }

   private static CommandBlockOOCConfig load() {
      File configFile = new File("C:/karucn/Lexis/config/CommandBlock_OOC/config.json");
      CommandBlockOOCConfig defaultConfig;
      if (!configFile.exists()) {
         defaultConfig = new CommandBlockOOCConfig();
         defaultConfig.save();
         return defaultConfig;
      } else {
         try {
            label46: {
               Reader reader = new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8);

               CommandBlockOOCConfig var4;
               label38: {
                  try {
                     Type type = (new TypeToken() {
                     }).getType();
                     CommandBlockOOCConfig loaded = (CommandBlockOOCConfig)GSON.fromJson(reader, type);
                     if (loaded != null && loaded.commands != null) {
                        var4 = loaded;
                        break label38;
                     }
                  } catch (Throwable var6) {
                     try {
                        reader.close();
                     } catch (Throwable var5) {
                        var6.addSuppressed(var5);
                     }

                     throw var6;
                  }

                  reader.close();
                  break label46;
               }

               reader.close();
               return var4;
            }
         } catch (Exception var7) {
            var7.printStackTrace();
         }

         defaultConfig = new CommandBlockOOCConfig();
         defaultConfig.save();
         return defaultConfig;
      }
   }

   public void save() {
      File configFile = new File("C:/karucn/Lexis/config/CommandBlock_OOC/config.json");
      configFile.getParentFile().mkdirs();

      try {
         Writer writer = new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8);

         try {
            GSON.toJson(this, writer);
            writer.flush();
         } catch (Throwable var6) {
            try {
               writer.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }

            throw var6;
         }

         writer.close();
      } catch (Exception var7) {
         var7.printStackTrace();
      }

   }
}
