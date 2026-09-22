package fku.org.example.fku.features.dynamicisland;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;

public class DynamicIslandConfig {
   public static final String POS_TOP_CENTER = "TOP_CENTER";
   public static final String POS_TOP_LEFT = "TOP_LEFT";
   public static final String POS_TOP_RIGHT = "TOP_RIGHT";
   public static final String IDLE_ROTATE = "ROTATE";
   public static final String IDLE_ALL = "ALL";
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
   private static DynamicIslandConfig instance;
   public boolean enabled = true;
   public String islandPosition = "TOP_CENTER";
   public int positionOffsetX = 0;
   public int positionOffsetY = 5;
   public boolean idleShowInfo = true;
   public String idleDisplayMode = "ROTATE";
   public int idleInfoRotateSeconds = 4;
   public boolean idleShowPlayer = true;
   public boolean idleShowTime = true;
   public boolean idleShowWeather = true;
   public boolean idleShowCoords = true;
   public boolean idleShowFps = true;
   public boolean idleShowDimension = false;
   public boolean idleShowBiome = false;
   public boolean showProgressBars = true;
   public boolean showProgMining = true;
   public boolean showProgEating = true;
   public boolean showProgBow = true;
   public boolean showProgShield = true;
   public boolean showProgFishing = true;
   public boolean showProgXp = false;
   public boolean showProgMusic = true;
   public boolean musicSwayEnabled = true;
   public boolean damageShakeEnabled = true;
   public boolean animationEnabled = true;
   public int expandDuration = 250;
   public float stiffness = 200.0F;
   public float damping = 20.0F;
   public int notificationDuration = 2500;
   public int maxQueueSize = 10;
   public boolean mergeSameFeature = true;
   public boolean showTimestamp = false;
   public boolean showDamage = true;
   public int capsuleWidth = 80;
   public int capsuleHeight = 24;
   public int expandedWidth = 300;
   public int expandedHeight = 44;
   public String backgroundColor = "#0A0A0AF0";
   public String textColor = "#FFFFFF";
   public int cornerRadius = 24;
   public boolean dragEnabled = true;
   public boolean tabShowEnabled = true;
   public int freeX = -1;
   public int freeY = -1;
   /** 拖动锚点相对位置（0~1，窗口宽高比例）。>=0 时优先于 freeX/freeY，窗口缩放保持相对位置 */
   public float freeRelX = -1.0F;
   public float freeRelY = -1.0F;
   public List<String> enabledFeatures = new ArrayList<>();
   public List<String> disabledFeatures = new ArrayList<>();

   private static File getConfigFile() {
      File dir = new File(getGameDirectory(), "fku");
      if (!dir.exists()) {
         dir.mkdirs();
      }

      return new File(dir, "dynamicisland.json");
   }

   private static File getGameDirectory() {
      try {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null) {
            return mc.gameDirectory;
         }
      } catch (Exception var1) {
      }

      return Paths.get(".").toAbsolutePath().normalize().toFile();
   }

   public static DynamicIslandConfig getInstance() {
      if (instance == null) {
         load();
      }

      return instance;
   }

   public static void load() {
      File f = getConfigFile();
      if (f.exists()) {
         try {
            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));

            try {
               instance = (DynamicIslandConfig)GSON.fromJson(r, DynamicIslandConfig.class);
            } catch (Throwable var5) {
               try {
                  r.close();
               } catch (Throwable var4) {
                  var5.addSuppressed(var4);
               }

               throw var5;
            }

            r.close();
         } catch (Exception var6) {
            instance = new DynamicIslandConfig();
         }
      } else {
         instance = new DynamicIslandConfig();
         save();
      }

      if (instance == null) {
         instance = new DynamicIslandConfig();
      }

      if (instance.enabledFeatures == null) {
         instance.enabledFeatures = new ArrayList();
      }

      if (instance.disabledFeatures == null) {
         instance.disabledFeatures = new ArrayList();
      }

      if (instance.cornerRadius == 12) {
         instance.cornerRadius = 24;
      }

      if (instance.expandedHeight == 48) {
         instance.expandedHeight = 44;
      }

   }

   public static void save() {
      if (instance != null) {
         try {
            BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getConfigFile()), StandardCharsets.UTF_8));

            try {
               GSON.toJson(instance, w);
            } catch (Throwable var4) {
               try {
                  w.close();
               } catch (Throwable var3) {
                  var4.addSuppressed(var3);
               }

               throw var4;
            }

            w.close();
         } catch (Exception var5) {
            var5.printStackTrace();
         }

      }
   }

   public void setEnabled(boolean v) {
      this.enabled = v;
      save();
   }

   public void setAnimationEnabled(boolean v) {
      this.animationEnabled = v;
      save();
   }

   public void setIslandPosition(String v) {
      if (v.equals("TOP_CENTER") || v.equals("TOP_LEFT") || v.equals("TOP_RIGHT")) {
         this.islandPosition = v;
      }

      save();
   }

   public void setStiffness(float v) {
      this.stiffness = Math.max(50.0F, Math.min(500.0F, v));
      save();
   }

   public void setDamping(float v) {
      this.damping = Math.max(5.0F, Math.min(50.0F, v));
      save();
   }

   public void setMaxQueueSize(int v) {
      this.maxQueueSize = Math.max(3, Math.min(30, v));
      save();
   }
}
