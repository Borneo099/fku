package lexis.Client.OOCCommand.SignOOC;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lexis.Hack.Hackutil.config.ConfigUtils;

public class SignOOCConfig {
   private static final String CONFIG_PATH = "C:/karucn/Lexis/config/Sign_OOC/config.json";
   private static SignOOCConfig instance;
   private List commands = new ArrayList();

   public SignOOCConfig() {
      this.commands.add("say 输入你命令1");
      this.commands.add("say 输入你命令2");
      this.commands.add("say 输入你命令3");
   }

   public static SignOOCConfig getInstance() {
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

   private static SignOOCConfig load() {
      File configFile = new File("C:/karucn/Lexis/config/Sign_OOC/config.json");
      SignOOCConfig loaded = (SignOOCConfig)ConfigUtils.readConfig(configFile, SignOOCConfig.class);
      if (loaded != null && loaded.commands != null) {
         return loaded;
      } else {
         SignOOCConfig defaultConfig = new SignOOCConfig();
         defaultConfig.save();
         return defaultConfig;
      }
   }

   public void save() {
      File configFile = new File("C:/karucn/Lexis/config/Sign_OOC/config.json");
      ConfigUtils.saveConfig(configFile, this);
   }
}
