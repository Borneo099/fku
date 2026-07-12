package lexis.Hack.Hacks.Chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ChatHistoryHack extends Hack {
   private HackConfig config;
   private static final String CONFIG_KEY = "聊天保留历史";
   private static final File HISTORY_FILE = new File("C:/karucn/Lexis/config/hack/chat_history.json");
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
   private List history = new ArrayList();
   private int maxHistory = 200;
   private static final long MAX_FILE_SIZE = 5242880L;

   public ChatHistoryHack() {
      super("聊天保留历史", new String[]{"保留退出服务器前的聊天记录", "§c§l注意：有保护内存，超过5MB文件就在你文件位置会自动清空吧就看不到聊天记录了"}, Hack.Category.CHAT, true);
      this.addSetting(new Hack.Setting("最大条数", "最多保留多少条聊天记录 (太多会卡)", 200, 50, 2000, Hack.ValueDisplay.INTEGER));
      this.config = HackConfig.getInstance();
      this.loadConfig();
      this.loadHistory();
   }

   private void loadConfig() {
      this.maxHistory = (int)this.config.getDoubleSetting("聊天保留历史", "最大条数", 200.0);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("最大条数")) {
            setting.setValue((double)this.maxHistory);
            break;
         }
      }

   }

   private void loadHistory() {
      try {
         if (!HISTORY_FILE.exists()) {
            return;
         }

         if (HISTORY_FILE.length() > 5242880L) {
            System.out.println("[聊天保留历史] 文件过大，已自动清空了");
            HISTORY_FILE.delete();
            this.history = new ArrayList();
            return;
         }

         FileReader reader = new FileReader(HISTORY_FILE);
         Type type = (new TypeToken() {
         }).getType();
         this.history = (List)GSON.fromJson(reader, type);
         reader.close();
         if (this.history == null) {
            this.history = new ArrayList();
         }

         while(this.history.size() > this.maxHistory) {
            this.history.remove(this.history.size() - 1);
         }
      } catch (Exception var3) {
         var3.printStackTrace();
         this.history = new ArrayList();
      }

   }

   private void saveHistory() {
      try {
         HISTORY_FILE.getParentFile().mkdirs();
         List toSave = new ArrayList(this.history);

         while(toSave.size() > this.maxHistory) {
            toSave.remove(toSave.size() - 1);
         }

         FileWriter writer = new FileWriter(HISTORY_FILE);
         GSON.toJson(toSave, writer);
         writer.close();
      } catch (Exception var3) {
         var3.printStackTrace();
      }

   }

   public List getHistory() {
      return new ArrayList(this.history);
   }

   public void onEnable() {
      MinecraftForge.EVENT_BUS.register(this);
   }

   public void onDisable() {
      MinecraftForge.EVENT_BUS.unregister(this);
      this.saveHistory();
   }

   @SubscribeEvent
   public void onChatMessage(ClientChatReceivedEvent event) {
      if (this.isEnabled()) {
         String message = event.getMessage().getString();
         String sender = "System";
         if (message.contains("<") && message.contains(">")) {
            int start = message.indexOf("<") + 1;
            int end = message.indexOf(">");
            if (start > 0 && end > start) {
               sender = message.substring(start, end);
               message = message.substring(end + 1).trim();
            }
         }

         this.history.add(0, new ChatMessage(message, sender));

         while(this.history.size() > this.maxHistory) {
            this.history.remove(this.history.size() - 1);
         }

         if (this.history.size() % 10 == 0) {
            this.saveHistory();
         }

      }
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         if (setting.getName().equals("最大条数")) {
            int newMax = (int)setting.getDouble();
            if (newMax != this.maxHistory) {
               this.maxHistory = newMax;
               needSave = true;

               while(this.history.size() > this.maxHistory) {
                  this.history.remove(this.history.size() - 1);
               }
            }
            break;
         }
      }

      if (needSave) {
         this.config.saveHackSettings("聊天保留历史", this.getSettings());
         this.saveHistory();
      }

   }

   public void onClick() {
      this.toggle();
   }

   private static class ChatMessage {
      String timestamp = (new SimpleDateFormat("HH:mm:ss")).format(new Date());
      String message;
      String sender;

      ChatMessage(String message, String sender) {
         this.message = message;
         this.sender = sender;
      }
   }
}
