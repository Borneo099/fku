package lexis.Hack.Hackutil.music;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NeteaseAPI {
   private static final String BASE = "https://music-api.gdstudio.xyz/api.php";
   private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10L)).build();

   public static List search(String keyword) {
      List result = new ArrayList();

      try {
         String url = "https://music-api.gdstudio.xyz/api.php?types=search&source=netease&name=" + URLEncoder.encode(keyword, StandardCharsets.UTF_8) + "&count=20";
         String json = get(url);
         JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
         Iterator var5 = arr.iterator();

         while(var5.hasNext()) {
            JsonElement e = (JsonElement)var5.next();
            JsonObject o = e.getAsJsonObject();
            MusicInfo m = new MusicInfo();
            m.id = o.get("id").getAsString();
            m.name = o.get("name").getAsString();
            if (o.has("artist") && o.get("artist").isJsonArray()) {
               JsonArray artists = o.get("artist").getAsJsonArray();
               StringBuilder sb = new StringBuilder();

               for(int i = 0; i < artists.size(); ++i) {
                  if (i > 0) {
                     sb.append("/");
                  }

                  sb.append(artists.get(i).getAsString());
               }

               m.artist = sb.toString();
            } else {
               m.artist = "未知";
            }

            m.album = o.has("album") ? o.get("album").getAsString() : "";
            m.picId = o.has("pic_id") ? o.get("pic_id").getAsString() : "";
            m.duration = o.has("duration") ? o.get("duration").getAsLong() : 0L;
            result.add(m);
         }
      } catch (Exception var12) {
         var12.printStackTrace();
      }

      return result;
   }

   public static String getPlayUrl(String id) {
      try {
         String url = "https://music-api.gdstudio.xyz/api.php?types=url&source=netease&id=" + id + "&br=320";
         JsonObject o = JsonParser.parseString(get(url)).getAsJsonObject();
         if (o.has("url") && !o.get("url").isJsonNull()) {
            return o.get("url").getAsString();
         }
      } catch (Exception var3) {
         var3.printStackTrace();
      }

      return null;
   }

   public static String getPicUrl(String picId) {
      if (picId != null && !picId.isEmpty()) {
         try {
            String url = "https://music-api.gdstudio.xyz/api.php?types=pic&source=netease&id=" + picId + "&size=300";
            JsonObject o = JsonParser.parseString(get(url)).getAsJsonObject();
            if (o.has("url") && !o.get("url").isJsonNull()) {
               return o.get("url").getAsString();
            }
         } catch (Exception var3) {
            var3.printStackTrace();
         }

         return null;
      } else {
         return null;
      }
   }

   public static String getLyrics(String id) {
      try {
         String url = "https://music-api.gdstudio.xyz/api.php?types=lyric&source=netease&id=" + id;
         JsonObject o = JsonParser.parseString(get(url)).getAsJsonObject();
         if (o.has("lyric") && !o.get("lyric").isJsonNull()) {
            return o.get("lyric").getAsString();
         }
      } catch (Exception var3) {
         var3.printStackTrace();
      }

      return "";
   }

   private static String get(String url) throws Exception {
      HttpRequest req = HttpRequest.newBuilder(URI.create(url)).header("User-Agent", "Mozilla/5.0").timeout(Duration.ofSeconds(15L)).GET().build();
      return (String)HTTP.send(req, BodyHandlers.ofString(StandardCharsets.UTF_8)).body();
   }
}
