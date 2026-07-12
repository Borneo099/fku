package lexis.Server.AICHAT;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AIService {
   private static final ConcurrentHashMap playerHistories = new ConcurrentHashMap();
   private static final int MAX_HISTORY_SIZE = 128;

   public static String chatWithAI(UUID playerId, String message) throws Exception {
      try {
         List history = (List)playerHistories.computeIfAbsent(playerId, (k) -> {
            return new ArrayList();
         });
         String response = AIUtils.chatWithAIWithHistory(message, history);
         updateHistory(history, message, response);
         return response;
      } catch (Exception var4) {
         playerHistories.remove(playerId);
         throw new Exception("AI服务暂时不可用: " + var4.getMessage());
      }
   }

   private static void updateHistory(List history, String userMessage, String aiResponse) {
      history.add("用户: " + userMessage);
      history.add("助手: " + aiResponse);

      while(history.size() > 128) {
         history.remove(0);
         history.remove(0);
      }

   }

   public static void clearPlayerHistory(UUID playerId) {
      playerHistories.remove(playerId);
   }

   public static void clearAllHistories() {
      playerHistories.clear();
   }

   public static String getAPIStatus() {
      try {
         String apiKey = DecodeUtil.getApiKey();
         return apiKey != null && !apiKey.isEmpty() ? "§aAPI密钥已配置 (长度: " + apiKey.length() + ")" : "§cAPI密钥未设置";
      } catch (Exception var1) {
         return "§cAPI密钥错误: " + var1.getMessage();
      }
   }

   public static int getActivePlayersCount() {
      return playerHistories.size();
   }
}
