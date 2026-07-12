package lexis.Hack.Hackutil.music;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

public class MusicState {
   public static MusicInfo current;
   public static ResourceLocation coverTexture;
   public static List lyrics;
   private static ResourceLocation lastCoverTexture;

   public static void playSong(MusicInfo info) {
      current = info;
      lyrics = null;
      (new Thread(() -> {
         try {
            String url = NeteaseAPI.getPlayUrl(info.id);
            if (url == null || url.isEmpty()) {
               System.err.println("[Music] 无法获取播放 URL: " + info.name);
               return;
            }

            info.playUrl = url;
            if (info.duration <= 0L) {
               info.duration = estimateDuration(url);
            }

            MusicPlayer.setTotalMs(info.duration);
            String picUrl = NeteaseAPI.getPicUrl(info.picId);
            info.coverUrl = picUrl;
            if (picUrl != null) {
               loadCover(picUrl, info.id);
            }

            String lrc = NeteaseAPI.getLyrics(info.id);
            lyrics = LyricsParser.parse(lrc);
            MusicPlayer.play(url);
         } catch (Exception var4) {
            var4.printStackTrace();
         }

      }, "Lexis-MusicLoader")).start();
   }

   private static long estimateDuration(String url) {
      try {
         HttpURLConnection c = (HttpURLConnection)(new URL(url)).openConnection();
         c.setRequestMethod("HEAD");
         c.setConnectTimeout(8000);
         c.setReadTimeout(8000);
         c.setRequestProperty("User-Agent", "Mozilla/5.0");
         long size = c.getContentLengthLong();
         c.disconnect();
         if (size > 0L) {
            return size * 1000L / 32000L;
         }
      } catch (Exception var4) {
      }

      return 0L;
   }

   private static void loadCover(String url, String id) {
      try {
         InputStream is = (new URL(url)).openStream();

         try {
            NativeImage img = NativeImage.m_85058_(is);
            Minecraft.m_91087_().execute(() -> {
               try {
                  if (lastCoverTexture != null) {
                     Minecraft.m_91087_().m_91097_().m_118513_(lastCoverTexture);
                  }

                  DynamicTexture tex = new DynamicTexture(img);
                  ResourceLocation rl = Minecraft.m_91087_().m_91097_().m_118490_("lexis_cover_" + id, tex);
                  coverTexture = rl;
                  lastCoverTexture = rl;
               } catch (Exception var4) {
                  var4.printStackTrace();
               }

            });
         } catch (Throwable var6) {
            if (is != null) {
               try {
                  is.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (is != null) {
            is.close();
         }
      } catch (Exception var7) {
         var7.printStackTrace();
      }

   }

   public static String getCurrentLyric() {
      return lyrics != null && !lyrics.isEmpty() ? LyricsParser.getCurrentLine(lyrics, MusicPlayer.getCurrentMs()) : "";
   }

   public static void stop() {
      MusicPlayer.stop();
      current = null;
      lyrics = null;
      coverTexture = null;
   }

   public static void toggleLoop() {
      MusicPlayer.setLoop(!MusicPlayer.isLoop());
   }

   public static boolean isLoopEnabled() {
      return MusicPlayer.isLoop();
   }
}
