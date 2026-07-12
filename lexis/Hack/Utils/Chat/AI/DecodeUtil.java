package lexis.Hack.Utils.Chat.AI;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class DecodeUtil {
   private static final String ENCODED_KEY = "c2stU2VsWnk5ZGpTWlQxdlBhU3hkdFgzUTBRM1VPSFNkMnFaczJLUk9STk14aUFDOEVI";

   public static String getApiKey() {
      try {
         byte[] decoded = Base64.getDecoder().decode("c2stU2VsWnk5ZGpTWlQxdlBhU3hkdFgzUTBRM1VPSFNkMnFaczJLUk9STk14aUFDOEVI");
         return new String(decoded, StandardCharsets.UTF_8);
      } catch (Exception var1) {
         var1.printStackTrace();
         return "";
      }
   }
}
