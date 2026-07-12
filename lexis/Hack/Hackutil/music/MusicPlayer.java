package lexis.Hack.Hackutil.music;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.FloatControl.Type;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;

public class MusicPlayer {
   private static SourceDataLine line;
   private static Thread playThread;
   private static volatile boolean playing = false;
   private static volatile boolean paused = false;
   private static volatile long currentMs = 0L;
   private static volatile long totalMs = 0L;
   private static volatile float volume = 0.7F;
   private static volatile boolean loop = false;
   private static volatile String currentUrl = null;

   public static void play(String url) {
      stop();
      currentUrl = url;
      playThread = new Thread(() -> {
         playInternal(url);
      }, "Lexis-Music");
      playThread.setDaemon(true);
      playThread.start();
   }

   private static void playInternal(String url) {
      do {
         Bitstream bitstream = null;

         try {
            HttpURLConnection conn = (HttpURLConnection)(new URL(url)).openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);
            InputStream is = new BufferedInputStream(conn.getInputStream(), 8192);
            bitstream = new Bitstream(is);
            Decoder decoder = new Decoder();
            Header firstHeader = bitstream.readFrame();
            if (firstHeader != null) {
               int sampleRate = firstHeader.frequency();
               int channels = firstHeader.mode() == 3 ? 1 : 2;
               AudioFormat fmt = new AudioFormat((float)sampleRate, 16, channels, true, false);
               DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt);
               line = (SourceDataLine)AudioSystem.getLine(info);
               line.open(fmt, 8192);
               line.start();
               applyVolume();
               playing = true;
               currentMs = 0L;
               long startTime = System.currentTimeMillis();
               long pauseStart = 0L;
               long pausedTotal = 0L;
               writeFrame(decoder, bitstream, firstHeader);
               bitstream.closeFrame();

               Header header;
               while(playing && (header = bitstream.readFrame()) != null) {
                  for(; paused && playing; Thread.sleep(50L)) {
                     if (pauseStart == 0L) {
                        pauseStart = System.currentTimeMillis();
                     }
                  }

                  if (pauseStart != 0L) {
                     pausedTotal += System.currentTimeMillis() - pauseStart;
                     pauseStart = 0L;
                  }

                  writeFrame(decoder, bitstream, header);
                  bitstream.closeFrame();
                  currentMs = System.currentTimeMillis() - startTime - pausedTotal;
               }

               line.drain();
               continue;
            }

            System.err.println("[Music] 无法读取 MP3 帧");
         } catch (Exception var31) {
            var31.printStackTrace();
            continue;
         } finally {
            if (line != null) {
               try {
                  line.stop();
                  line.close();
               } catch (Exception var30) {
               }
            }

            if (bitstream != null) {
               try {
                  bitstream.close();
               } catch (Exception var29) {
               }
            }

            playing = false;
         }

         return;
      } while(loop && !playing && currentUrl != null);

   }

   private static void writeFrame(Decoder decoder, Bitstream bitstream, Header header) throws Exception {
      SampleBuffer output = (SampleBuffer)decoder.decodeFrame(header, bitstream);
      short[] samples = output.getBuffer();
      int len = output.getBufferLength();
      byte[] bytes = new byte[len * 2];

      for(int i = 0; i < len; ++i) {
         bytes[i * 2] = (byte)(samples[i] & 255);
         bytes[i * 2 + 1] = (byte)(samples[i] >> 8 & 255);
      }

      line.write(bytes, 0, bytes.length);
   }

   public static void pause() {
      paused = true;
   }

   public static void resume() {
      paused = false;
   }

   public static void stop() {
      playing = false;
      paused = false;
      if (line != null) {
         try {
            line.stop();
            line.close();
         } catch (Exception var1) {
         }
      }

      if (playThread != null) {
         playThread.interrupt();
      }

      currentUrl = null;
   }

   public static boolean isPlaying() {
      return playing && !paused;
   }

   public static long getCurrentMs() {
      return currentMs;
   }

   public static long getTotalMs() {
      return totalMs;
   }

   public static void setTotalMs(long ms) {
      totalMs = ms;
   }

   public static void setVolume(float v) {
      volume = Math.max(0.0F, Math.min(1.0F, v));
      applyVolume();
   }

   private static void applyVolume() {
      if (line != null) {
         try {
            FloatControl gain = (FloatControl)line.getControl(Type.MASTER_GAIN);
            float dB = (float)(Math.log10(Math.max(1.0E-4, (double)volume)) * 20.0);
            gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB)));
         } catch (Exception var2) {
         }

      }
   }

   public static void setLoop(boolean enable) {
      loop = enable;
   }

   public static boolean isLoop() {
      return loop;
   }
}
