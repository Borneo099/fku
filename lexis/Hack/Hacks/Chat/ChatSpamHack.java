package lexis.Hack.Hacks.Chat;

import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import lexis.Hack.Hack;
import lexis.Hack.HackButton;
import lexis.Hack.Hackutil.ChatSpam.ChatSpamSettingsScreen;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraft.client.Minecraft;

public class ChatSpamHack extends Hack {
   private String message = "Lexis !!!";
   private double speed = 1.6;
   private int maxMessages = 6;
   private int cooldownTime = 8;
   private Timer timer;
   private int messageCount = 0;
   private boolean onCooldown = false;
   private HackConfig config;

   public ChatSpamHack() {
      super("发送消息", "自动发送消息", Hack.Category.CHAT, true);
      this.addSetting(new Hack.Setting("消息", "要发送的消息内容", "Lexis !!!"));
      this.addSetting(new Hack.Setting("速度", "发送间隔（秒）", 1.6, 0.1, 100.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("最大数量", "最大连续发送数", 6, 1, 10000, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("冷却时间", "冷却时间（秒）", 8, 1, 100, Hack.ValueDisplay.INTEGER));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      Map settings = this.config.getHackSettings("发送消息");
      if (settings.containsKey("消息")) {
         this.message = (String)settings.get("消息");
      }

      if (settings.containsKey("速度")) {
         this.speed = (Double)settings.get("速度");
      }

      if (settings.containsKey("最大数量")) {
         this.maxMessages = (int)(Double)settings.get("最大数量");
      }

      if (settings.containsKey("冷却时间")) {
         this.cooldownTime = (int)(Double)settings.get("冷却时间");
      }

      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         switch (setting.getName()) {
            case "消息":
               setting.setValue(this.message);
               break;
            case "速度":
               setting.setValue(this.speed);
               break;
            case "最大数量":
               setting.setValue(this.maxMessages);
               break;
            case "冷却时间":
               setting.setValue(this.cooldownTime);
         }
      }

   }

   public void onEnable() {
      if (mc.f_91074_ != null) {
         Iterator var1 = this.getSettings().iterator();

         while(var1.hasNext()) {
            Hack.Setting setting = (Hack.Setting)var1.next();
            switch (setting.getName()) {
               case "消息":
                  this.message = (String)setting.getValue();
                  break;
               case "速度":
                  this.speed = setting.getDouble();
                  break;
               case "最大数量":
                  this.maxMessages = setting.getInt();
                  break;
               case "冷却时间":
                  this.cooldownTime = setting.getInt();
            }
         }

         this.messageCount = 0;
         this.onCooldown = false;
         this.timer = new Timer();
         this.timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
               if (Hack.mc.f_91074_ != null && !ChatSpamHack.this.onCooldown) {
                  if (ChatSpamHack.this.messageCount >= ChatSpamHack.this.maxMessages) {
                     ChatSpamHack.this.onCooldown = true;
                     ChatSpamHack.this.messageCount = 0;
                     (new Timer()).schedule(new TimerTask() {
                        public void run() {
                           ChatSpamHack.this.onCooldown = false;
                        }
                     }, (long)(ChatSpamHack.this.cooldownTime * 1000));
                  } else {
                     Hack.mc.f_91074_.f_108617_.m_246175_(ChatSpamHack.this.message);
                     ++ChatSpamHack.this.messageCount;
                  }
               }
            }
         }, 0L, (long)(this.speed * 1000.0));
      }
   }

   public void onDisable() {
      if (this.timer != null) {
         this.timer.cancel();
         this.timer = null;
      }

      this.messageCount = 0;
      this.onCooldown = false;
   }

   public void onUpdate() {
   }

   public void onClick() {
      this.toggle();
   }

   public void onRightClick() {
      Minecraft.m_91087_().m_91152_(new ChatSpamSettingsScreen(this, (HackButton)null));
   }

   public void updateSettings(String newMessage, double newSpeed, int newMaxMessages, int newCooldownTime) {
      this.message = newMessage;
      this.speed = newSpeed;
      this.maxMessages = newMaxMessages;
      this.cooldownTime = newCooldownTime;
      this.config.saveHackSettings("发送消息", this.getSettings());
      if (this.isEnabled()) {
         this.onDisable();
         this.onEnable();
      }

   }

   public String getMessage() {
      return this.message;
   }

   public double getSpeed() {
      return this.speed;
   }

   public int getMaxMessages() {
      return this.maxMessages;
   }

   public int getCooldownTime() {
      return this.cooldownTime;
   }
}
