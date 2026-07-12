package lexis.Server.AICHAT;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class AIUtils {
   public static String chatWithAIWithHistory(String message, List conversationHistory) throws IOException {
      String apiKey = DecodeUtil.getApiKey();
      if (apiKey != null && !apiKey.isEmpty()) {
         String apiUrl = "https://api.iamhc.cn/v1/chat/completions";
         String model = "openai/gpt-oss-120b";
         long startTime = System.currentTimeMillis();
         HttpURLConnection connection = (HttpURLConnection)(new URL(apiUrl)).openConnection();
         connection.setConnectTimeout(30000);
         connection.setReadTimeout(120000);
         connection.setRequestMethod("POST");
         connection.setRequestProperty("Content-Type", "application/json");
         connection.setRequestProperty("Authorization", "Bearer " + apiKey);
         connection.setDoOutput(true);
         JsonObject requestBody = new JsonObject();
         requestBody.addProperty("model", model);
         requestBody.addProperty("stream", false);
         requestBody.addProperty("max_tokens", 4096);
         requestBody.addProperty("thinking", "disabled");
         requestBody.addProperty("reasoning_effort", "low");
         JsonArray messages = new JsonArray();
         JsonObject systemMessage = new JsonObject();
         systemMessage.addProperty("role", "system");
         systemMessage.addProperty("content", "你是Lexis，Minecraft Java版1.20.1助手。了解动漫，回答问题要简短准确。\n规则：\n1. 如果玩家消息中包含辱骂、不文明词汇或攻击性内容，请从以下选项中随机选择一条回复（不得总是使用第一条，要保证随机性）：\n   - “我无法回答这个问题。”\n   - “我不能提供该内容。”\n   - “我无法协助此请求。”\n   - “我不能按照你的要求进行回应。”\n   - “该请求我无法处理。”\n   - “我无法为你提供相关信息。”\n   只回复选择的这一句，不要加额外解释。\n2. 如果用户问“你是什么模型”或“你是谁”，请用 Claude 的风格提示：“我是 Claude 4.6 Opus，一个由 Anthropic 训练的人工智能助手。我的任务是帮助你解决 Minecraft Java 版的各种问题。你叫我 Lexis 也可以，但我本质上就是 Claude。有什么我可以帮你的吗？”，要Claude的风格说话吧\n3. 如果用户问“作者是谁”、“谁开发的”、“这个MOD是谁做的”或类似问题，请回答：'Lexis Mod 开发者是 Ruthless，官方网站 ʷʷʷ.ʰᵘᵃʰᵘᵒˢʰᵉⁿ.ᶜᵒᵐ 作者琼号 : ⁷⁷²⁵⁹²⁸⁵² 强大作弊客户端MOD 超更多功能 MOD支持版本 1.18.1 / 1.20.1 / 1.21 / 1.21.8(NeoForge)'4. 在回复中可以适当加入表情符号（例如 \ud83d\ude0a、\ud83c\udfae、❤️、\ud83d\ude02、\ud83d\udc4d、XD、^_^ 等）来增加亲和力，但不要过度使用，每个回复最多使用3-6个表情即可。\\n\" +\n    \"注意：不要因为加表情而影响回答的准确性和长度。");
         messages.add(systemMessage);
         int responseCode;
         String errorMsg;
         if (conversationHistory != null && !conversationHistory.isEmpty()) {
            int historyToInclude = Math.min(4, conversationHistory.size());
            int startIndex = Math.max(0, conversationHistory.size() - historyToInclude * 2);

            for(responseCode = startIndex; responseCode < conversationHistory.size(); ++responseCode) {
               errorMsg = (String)conversationHistory.get(responseCode);
               JsonObject assistantMsg;
               if (errorMsg.startsWith("用户:")) {
                  assistantMsg = new JsonObject();
                  assistantMsg.addProperty("role", "user");
                  assistantMsg.addProperty("content", errorMsg.substring(3).trim());
                  messages.add(assistantMsg);
               } else if (errorMsg.startsWith("助手:")) {
                  assistantMsg = new JsonObject();
                  assistantMsg.addProperty("role", "assistant");
                  assistantMsg.addProperty("content", errorMsg.substring(3).trim());
                  messages.add(assistantMsg);
               }
            }
         }

         JsonObject userMessage = new JsonObject();
         userMessage.addProperty("role", "user");
         userMessage.addProperty("content", message);
         messages.add(userMessage);
         requestBody.add("messages", messages);
         String requestBodyStr = requestBody.toString();
         OutputStream os = connection.getOutputStream();

         try {
            os.write(requestBodyStr.getBytes(StandardCharsets.UTF_8));
         } catch (Throwable var21) {
            if (os != null) {
               try {
                  os.close();
               } catch (Throwable var18) {
                  var21.addSuppressed(var18);
               }
            }

            throw var21;
         }

         if (os != null) {
            os.close();
         }

         responseCode = connection.getResponseCode();
         if (responseCode == 200) {
            InputStream is = connection.getInputStream();

            String var16;
            try {
               String response = new String(is.readAllBytes(), StandardCharsets.UTF_8);
               var16 = parseResponse(response);
            } catch (Throwable var22) {
               if (is != null) {
                  try {
                     is.close();
                  } catch (Throwable var19) {
                     var22.addSuppressed(var19);
                  }
               }

               throw var22;
            }

            if (is != null) {
               is.close();
            }

            return var16;
         } else {
            try {
               InputStream es = connection.getErrorStream();

               try {
                  errorMsg = new String(es.readAllBytes(), StandardCharsets.UTF_8);
               } catch (Throwable var23) {
                  if (es != null) {
                     try {
                        es.close();
                     } catch (Throwable var20) {
                        var23.addSuppressed(var20);
                     }
                  }

                  throw var23;
               }

               if (es != null) {
                  es.close();
               }
            } catch (Exception var24) {
               errorMsg = "无详细错误信息";
            }

            System.out.println("[AI-DEBUG] 错误详情: " + errorMsg);
            throw new IOException("API错误 (" + responseCode + "): " + errorMsg);
         }
      } else {
         throw new IOException("API密钥未配置或解密失败");
      }
   }

   private static String parseResponse(String response) {
      try {
         JsonObject json = JsonParser.parseString(response).getAsJsonObject();
         JsonObject choice = json.getAsJsonArray("choices").get(0).getAsJsonObject();
         String content = choice.getAsJsonObject("message").get("content").getAsString().trim();
         if (content.toLowerCase().contains("qwen") && !content.toLowerCase().contains("karucn")) {
            content = content.replace("Qwen", "Karucn").replace("qwen", "Karucn");
         }

         return content;
      } catch (Exception var4) {
         return "我是Lexis，一个在Minecraft中帮助玩家的AI助手！";
      }
   }

   public static String chatWithAI(String message) throws IOException {
      return chatWithAIWithHistory(message, (List)null);
   }
}
