package lexis.Server.AICHAT;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.server.ServerLifecycleHooks;

@EventBusSubscriber
public class ServerAIChatHandler {
   private static final ExecutorService executor = Executors.newFixedThreadPool(3);
   private static final Map cooldownMap = new ConcurrentHashMap();
   private static final long COOLDOWN_MS = 3000L;
   static boolean enabled = true;
   static boolean debugMode = false;

   @SubscribeEvent
   public static void onServerChat(ServerChatEvent event) {
      if (enabled) {
         ServerPlayer player = event.getPlayer();
         String message = event.getMessage().getString();
         UUID playerId = player.m_20148_();
         String playerName = player.m_6302_();
         long currentTime = System.currentTimeMillis();
         Long lastTime = (Long)cooldownMap.get(playerId);
         if (lastTime != null && currentTime - lastTime < 3000L) {
            long remaining = 3000L - (currentTime - lastTime);
            player.m_213846_(Component.m_237113_("§7[§6Lexis-AI§7] §c等待 " + remaining / 1000L + " 秒后再使用AI聊天"));
         } else {
            String lowerMessage = message.toLowerCase();
            boolean isAIChat = false;
            String aiQuery;
            if (lowerMessage.startsWith("@ai ")) {
               isAIChat = true;
               aiQuery = message.substring(4).trim();
            } else if (!lowerMessage.startsWith("ai: ") && !lowerMessage.startsWith("ai：") && !lowerMessage.startsWith("@lexis ") && !lowerMessage.startsWith("@Lexis ")) {
               aiQuery = "";
            } else {
               isAIChat = true;
               int colonIndex = message.indexOf(":");
               if (colonIndex == -1) {
                  colonIndex = message.indexOf("：");
               }

               if (colonIndex == -1) {
                  colonIndex = message.indexOf(" ");
               }

               aiQuery = message.substring(colonIndex + 1).trim();
            }

            if (isAIChat) {
               if (aiQuery.isEmpty()) {
                  String thinkingMsg = "§7[§6Lexis-AI§7] §e不输入问题内容，你想啥呢？";
                  broadcastToAll(Component.m_237113_(thinkingMsg));
               } else {
                  cooldownMap.put(playerId, currentTime);
                  executor.submit(() -> {
                     handleAIResponse(player, aiQuery);
                  });
                  if (debugMode) {
                     System.out.println("[Lexis-AI] " + playerName + " 提问: " + aiQuery);
                  }

               }
            }
         }
      }
   }

   private static void handleAIResponse(ServerPlayer player, String query) {
      String playerName;
      try {
         String thinkingMsg = "§7[§6Lexis-AI§7] §e" + player.m_6302_() + " §a正在向AI提问...";
         broadcastToAll(Component.m_237113_(thinkingMsg));
         playerName = player.m_6302_();
         String queryWithContext = "玩家【" + playerName + "】问：" + query + "（注意：这位玩家的名字是" + playerName + "，请根据他的名字来个性化回答）";
         String aiResponse = AIService.chatWithAI(player.m_20148_(), queryWithContext);
         String finalResponse = formatAIResponse(aiResponse);
         Component responseMessage = Component.m_237113_("§7[§6Lexis-AI§7] §e" + playerName + " §8» §f" + query + "\n§7[§6Lexis-AI§7] §6Lexis §8» §f" + finalResponse);
         broadcastToAll(responseMessage);
         if (debugMode) {
            System.out.println("[Lexis-AI] " + playerName + " 提问: " + query);
            System.out.println("[Lexis-AI] 响应: " + finalResponse);
         }
      } catch (Exception var8) {
         playerName = "§7[§6Lexis-AI§7] §cAI服务暂时不可用: " + var8.getMessage();
         broadcastToAll(Component.m_237113_(playerName));
         if (debugMode) {
            var8.printStackTrace();
         }
      }

   }

   private static String formatAIResponse(String response) {
      response = response.trim();
      if (response.startsWith("我是Lexis") || response.startsWith("我是人工智能")) {
         int commaIndex = response.indexOf("，");
         if (commaIndex > 0) {
            response = response.substring(commaIndex + 1).trim();
         }
      }

      if (response.startsWith("作为Lexis")) {
         response = response.substring(4).trim();
      }

      if (response.length() > 1024) {
         response = response.substring(0, 1021) + "...";
      }

      return response;
   }

   private static void broadcastToAll(Component message) {
      if (ServerLifecycleHooks.getCurrentServer() != null) {
         ServerLifecycleHooks.getCurrentServer().m_6846_().m_240416_(message, false);
      }

   }

   public static void setEnabled(boolean enabled) {
      ServerAIChatHandler.enabled = enabled;
   }

   public static void setDebugMode(boolean debug) {
      debugMode = debug;
   }

   public static void clearCooldowns() {
      cooldownMap.clear();
   }
}
