package lexis.Hack.Utils.Chat.AI;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class AIUtils {
   public static String translate(String text) throws IOException {
      String apiKey = DecodeUtil.getApiKey();
      if (apiKey != null && !apiKey.isEmpty()) {
         String apiUrl = "https://api.iamhc.cn/v1/chat/completions";
         String model = "openai/gpt-oss-120b";
         HttpURLConnection connection = (HttpURLConnection)(new URL(apiUrl)).openConnection();
         connection.setConnectTimeout(30000);
         connection.setReadTimeout(60000);
         connection.setRequestMethod("POST");
         connection.setRequestProperty("Content-Type", "application/json");
         connection.setRequestProperty("Authorization", "Bearer " + apiKey);
         connection.setDoOutput(true);
         JsonObject requestBody = new JsonObject();
         requestBody.addProperty("model", model);
         requestBody.addProperty("stream", false);
         requestBody.addProperty("max_tokens", 500);
         JsonArray messages = new JsonArray();
         String prompt = "请将下面这段话翻译成中文。只输出翻译结果，不要输出原文本，不要加引号，不要加任何注释或额外说明。\n" + text;
         JsonObject userMessage = new JsonObject();
         userMessage.addProperty("role", "user");
         userMessage.addProperty("content", prompt);
         messages.add(userMessage);
         requestBody.add("messages", messages);
         OutputStream os = connection.getOutputStream();

         try {
            os.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
         } catch (Throwable var18) {
            if (os != null) {
               try {
                  os.close();
               } catch (Throwable var15) {
                  var18.addSuppressed(var15);
               }
            }

            throw var18;
         }

         if (os != null) {
            os.close();
         }

         int responseCode = connection.getResponseCode();
         if (responseCode == 200) {
            InputStream is = connection.getInputStream();

            String var12;
            try {
               String response = new String(is.readAllBytes(), StandardCharsets.UTF_8);
               var12 = parseResponse(response);
            } catch (Throwable var17) {
               if (is != null) {
                  try {
                     is.close();
                  } catch (Throwable var14) {
                     var17.addSuppressed(var14);
                  }
               }

               throw var17;
            }

            if (is != null) {
               is.close();
            }

            return var12;
         } else {
            String errorMsg;
            try {
               InputStream es = connection.getErrorStream();

               try {
                  errorMsg = new String(es.readAllBytes(), StandardCharsets.UTF_8);
               } catch (Throwable var19) {
                  if (es != null) {
                     try {
                        es.close();
                     } catch (Throwable var16) {
                        var19.addSuppressed(var16);
                     }
                  }

                  throw var19;
               }

               if (es != null) {
                  es.close();
               }
            } catch (Exception var20) {
               errorMsg = "无详细错误信息";
            }

            throw new IOException("API错误 (" + responseCode + "): " + errorMsg);
         }
      } else {
         throw new IOException("API密钥未配置");
      }
   }

   private static String parseResponse(String response) {
      try {
         JsonObject json = JsonParser.parseString(response).getAsJsonObject();
         JsonObject choice = json.getAsJsonArray("choices").get(0).getAsJsonObject();
         String content = choice.getAsJsonObject("message").get("content").getAsString().trim();
         return content;
      } catch (Exception var4) {
         return "翻译失败，请重试。";
      }
   }
}
