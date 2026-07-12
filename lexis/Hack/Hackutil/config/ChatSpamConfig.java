package lexis.Hack.Hackutil.config;

import java.io.File;

public class ChatSpamConfig {
   private static final File CONFIG_FILE = new File("C:/karucn/Lexis/config/hack/ChatSpam.json");
   private static ChatSpamConfig INSTANCE;
   private String message = "Lexis !!!";
   private double speed = 1.6;
   private int maxMessages = 6;
   private int cooldownTime = 8;

   public static ChatSpamConfig getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new ChatSpamConfig();
         INSTANCE.load();
      }

      return INSTANCE;
   }

   public void load() {
      ChatSpamConfig loaded = (ChatSpamConfig)ConfigUtils.readConfig(CONFIG_FILE, ChatSpamConfig.class);
      if (loaded != null) {
         this.message = loaded.message;
         this.speed = loaded.speed;
         this.maxMessages = loaded.maxMessages;
         this.cooldownTime = loaded.cooldownTime;
      } else {
         this.save();
      }

   }

   public void save() {
      ConfigUtils.saveConfig(CONFIG_FILE, this);
   }

   public String getMessage() {
      return this.message;
   }

   public void setMessage(String message) {
      this.message = message;
      this.save();
   }

   public double getSpeed() {
      return this.speed;
   }

   public void setSpeed(double speed) {
      this.speed = speed;
      this.save();
   }

   public int getMaxMessages() {
      return this.maxMessages;
   }

   public void setMaxMessages(int maxMessages) {
      this.maxMessages = maxMessages;
      this.save();
   }

   public int getCooldownTime() {
      return this.cooldownTime;
   }

   public void setCooldownTime(int cooldownTime) {
      this.cooldownTime = cooldownTime;
      this.save();
   }
}
