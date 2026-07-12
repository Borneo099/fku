package lexis.Hack.Utils;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.process.IBaritoneProcess;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraftforge.fml.ModList;

public final class BaritoneBridge {
   private static Boolean available = null;
   private static long lastActiveCheck = 0L;
   private static boolean lastActiveResult = false;
   private static volatile boolean suppressNextGotoMessage = false;
   private static volatile boolean suppressNextSetMessage = false;

   private BaritoneBridge() {
   }

   public static boolean isAvailable() {
      if (available == null) {
         try {
            available = ModList.get().isLoaded("baritoe");
         } catch (Throwable var1) {
            available = false;
         }
      }

      return available;
   }

   public static boolean isActive() {
      if (!isAvailable()) {
         return false;
      } else {
         long now = System.currentTimeMillis();
         if (now - lastActiveCheck < 50L) {
            return lastActiveResult;
         } else {
            lastActiveCheck = now;

            try {
               lastActiveResult = BaritoneBridge.Impl.isActive();
            } catch (NoClassDefFoundError var3) {
               lastActiveResult = false;
            }

            return lastActiveResult;
         }
      }
   }

   public static boolean isMining() {
      if (!isAvailable()) {
         return false;
      } else {
         try {
            return BaritoneBridge.Impl.isMining();
         } catch (NoClassDefFoundError var1) {
            return false;
         }
      }
   }

   public static boolean isElytraActive() {
      if (!isAvailable()) {
         return false;
      } else {
         try {
            return BaritoneBridge.Impl.isElytraActive();
         } catch (NoClassDefFoundError var1) {
            return false;
         }
      }
   }

   public static void forceStopElytra() {
      if (isAvailable()) {
         try {
            BaritoneBridge.Impl.forceStopElytra();
         } catch (NoClassDefFoundError var1) {
         }

      }
   }

   public static void gotoCoord(int x, int y, int z) {
      if (isAvailable()) {
         try {
            BaritoneBridge.Impl.gotoCoord(x, y, z);
         } catch (NoClassDefFoundError var4) {
         }

      }
   }

   public static void pause() {
      if (isAvailable()) {
         try {
            BaritoneBridge.Impl.pause();
         } catch (NoClassDefFoundError var1) {
         }

      }
   }

   public static void resume() {
      if (isAvailable()) {
         try {
            BaritoneBridge.Impl.resume();
         } catch (NoClassDefFoundError var1) {
         }

      }
   }

   public static void stop() {
      if (isAvailable()) {
         try {
            BaritoneBridge.Impl.stop();
         } catch (NoClassDefFoundError var1) {
         }

      }
   }

   public static void executeCommand(String command) {
      if (isAvailable()) {
         try {
            BaritoneBridge.Impl.executeCommand(command);
         } catch (NoClassDefFoundError var2) {
         }

      }
   }

   public static float[] getLookRotation() {
      if (!isAvailable()) {
         return null;
      } else {
         try {
            return BaritoneBridge.Impl.getLookRotation();
         } catch (Throwable var1) {
            return null;
         }
      }
   }

   public static void gotoCoordSilent(int x, int y, int z) {
      suppressNextGotoMessage = true;
      gotoCoord(x, y, z);
   }

   public static boolean consumeSuppressFlag() {
      if (suppressNextGotoMessage) {
         suppressNextGotoMessage = false;
         return true;
      } else {
         return false;
      }
   }

   public static void suppressNextSetMessage() {
      suppressNextSetMessage = true;
   }

   public static boolean consumeSetSuppressFlag() {
      if (suppressNextSetMessage) {
         suppressNextSetMessage = false;
         return true;
      } else {
         return false;
      }
   }

   private static final class Impl {
      static boolean isActive() {
         IBaritone b = BaritoneAPI.getProvider().getPrimaryBaritone();
         if (b.getPathingBehavior().isPathing()) {
            return true;
         } else {
            IBaritoneProcess p = (IBaritoneProcess)b.getPathingControlManager().mostRecentInControl().orElse((Object)null);
            return p != null && p.isActive();
         }
      }

      static boolean isMining() {
         return BaritoneAPI.getProvider().getPrimaryBaritone().getMineProcess().isActive();
      }

      static boolean isElytraActive() {
         return BaritoneAPI.getProvider().getPrimaryBaritone().getElytraProcess().isActive();
      }

      static void forceStopElytra() {
         BaritoneAPI.getProvider().getPrimaryBaritone().getElytraProcess().onLostControl();
      }

      static void gotoCoord(int x, int y, int z) {
         BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("goto " + x + " " + y + " " + z);
      }

      static void pause() {
         BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("pause");
      }

      static void resume() {
         BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("resume");
      }

      static void stop() {
         BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("stop");
      }

      static void executeCommand(String command) {
         BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute(command);
      }

      static float[] getLookRotation() {
         IBaritone b = BaritoneAPI.getProvider().getPrimaryBaritone();

         Object look;
         try {
            look = IBaritone.class.getMethod("getLookBehavior").invoke(b);
         } catch (Throwable var12) {
            return null;
         }

         if (look == null) {
            return null;
         } else {
            Class c = look.getClass();
            String[] var3 = new String[]{"target", "serverRotation", "prevRotation"};
            int var4 = var3.length;

            for(int var5 = 0; var5 < var4; ++var5) {
               String fn = var3[var5];

               try {
                  Field f = c.getDeclaredField(fn);
                  f.setAccessible(true);
                  Object val = f.get(look);
                  if (val != null) {
                     float[] r = extractYawPitch(val);
                     if (r != null) {
                        return r;
                     }
                  }
               } catch (NoSuchFieldException var10) {
               } catch (Throwable var11) {
               }
            }

            return null;
         }
      }

      private static float[] extractYawPitch(Object val) {
         Object rotation = val;

         try {
            Field rf = val.getClass().getDeclaredField("rotation");
            rf.setAccessible(true);
            Object inner = rf.get(val);
            if (inner != null) {
               rotation = inner;
            }
         } catch (Throwable var7) {
         }

         try {
            Method gy = rotation.getClass().getMethod("getYaw");
            Method gp = rotation.getClass().getMethod("getPitch");
            float yaw = ((Number)gy.invoke(rotation)).floatValue();
            float pitch = ((Number)gp.invoke(rotation)).floatValue();
            return !Float.isNaN(yaw) && !Float.isNaN(pitch) ? new float[]{yaw, pitch} : null;
         } catch (Throwable var6) {
            return null;
         }
      }
   }
}
