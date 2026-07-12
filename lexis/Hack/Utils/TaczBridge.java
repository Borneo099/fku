package lexis.Hack.Utils;

import net.minecraftforge.fml.ModList;

public final class TaczBridge {
   private static Boolean available = null;

   private TaczBridge() {
   }

   public static boolean isAvailable() {
      if (available == null) {
         try {
            available = ModList.get().isLoaded("tacz");
         } catch (Throwable var1) {
            available = false;
         }
      }

      return available;
   }

   public static boolean isHoldingGun() {
      if (!isAvailable()) {
         return false;
      } else {
         try {
            return TaczBridge.Impl.isHoldingGun();
         } catch (NoClassDefFoundError var1) {
            return false;
         }
      }
   }

   public static boolean isShooting() {
      if (!isAvailable()) {
         return false;
      } else {
         try {
            return TaczBridge.Impl.isShooting();
         } catch (NoClassDefFoundError var1) {
            return false;
         }
      }
   }

   private static class Impl {
      static boolean isHoldingGun() {
         return false;
      }

      static boolean isShooting() {
         return false;
      }
   }
}
