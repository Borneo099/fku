package lexis.Hack.Utils;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public class FakeGlowManager {
   private static final Map GLOW_MAP = new ConcurrentHashMap();
   private static final Minecraft mc = Minecraft.m_91087_();
   private static final long GLOW_TTL_MS = 1000L;

   public static void setGlow(Entity entity, String sourceKey, boolean glow, int color, double maxDistance) {
      setGlow(entity, sourceKey, glow, color, maxDistance, false, 0.0F);
   }

   public static void setRainbowGlow(Entity entity, String sourceKey, double maxDistance, float speed) {
      setGlow(entity, sourceKey, true, 0, maxDistance, true, speed);
   }

   private static void setGlow(Entity entity, String sourceKey, boolean glow, int color, double maxDistance, boolean rainbow, float speed) {
      if (entity != null) {
         UUID uuid = entity.m_20148_();
         Map sourceMap = (Map)GLOW_MAP.computeIfAbsent(uuid, (k) -> {
            return new ConcurrentHashMap();
         });
         if (glow) {
            sourceMap.put(sourceKey, new GlowInfo(true, color, maxDistance, rainbow, speed));
         } else {
            sourceMap.remove(sourceKey);
         }

         if (sourceMap.isEmpty()) {
            GLOW_MAP.remove(uuid);
         }

      }
   }

   public static void clearAll() {
      GLOW_MAP.clear();
   }

   public static void clearSource(String sourceKey) {
      Iterator var1 = GLOW_MAP.entrySet().iterator();

      while(var1.hasNext()) {
         Map.Entry e = (Map.Entry)var1.next();
         Map sourceMap = (Map)e.getValue();
         sourceMap.remove(sourceKey);
         if (sourceMap.isEmpty()) {
            GLOW_MAP.remove(e.getKey());
         }
      }

   }

   public static int getGlowColor(Entity entity) {
      if (entity == null) {
         return -1;
      } else {
         Map sourceMap = (Map)GLOW_MAP.get(entity.m_20148_());
         if (sourceMap != null && !sourceMap.isEmpty()) {
            if (mc.f_91074_ == null) {
               return -1;
            } else {
               long now = System.currentTimeMillis();
               double dist = (double)mc.f_91074_.m_20270_(entity);
               GlowInfo best = null;
               long latest = 0L;
               Iterator it = sourceMap.entrySet().iterator();

               while(true) {
                  while(it.hasNext()) {
                     GlowInfo info = (GlowInfo)((Map.Entry)it.next()).getValue();
                     if (info.glow && now - info.lastUpdate <= 1000L) {
                        if (!(dist > info.maxDistance) && info.lastUpdate > latest) {
                           latest = info.lastUpdate;
                           best = info;
                        }
                     } else {
                        it.remove();
                     }
                  }

                  if (sourceMap.isEmpty()) {
                     GLOW_MAP.remove(entity.m_20148_());
                  }

                  if (best == null) {
                     return -1;
                  }

                  if (best.rainbow) {
                     long time = System.currentTimeMillis();
                     float hue = (float)time / 1000.0F * best.rainbowSpeed % 1.0F;
                     int color = java.awt.Color.HSBtoRGB(hue, 1.0F, 1.0F) & 16777215;
                     return color;
                  }

                  return best.color;
               }
            }
         } else {
            return -1;
         }
      }
   }

   public static boolean shouldGlow(Entity entity) {
      return getGlowColor(entity) != -1;
   }

   private static class GlowInfo {
      boolean glow;
      int color;
      double maxDistance;
      long lastUpdate;
      boolean rainbow;
      float rainbowSpeed;

      GlowInfo(boolean glow, int color, double maxDistance, boolean rainbow, float speed) {
         this.glow = glow;
         this.color = color;
         this.maxDistance = maxDistance;
         this.lastUpdate = System.currentTimeMillis();
         this.rainbow = rainbow;
         this.rainbowSpeed = speed;
      }
   }
}
