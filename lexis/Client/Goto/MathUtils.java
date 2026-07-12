package lexis.Client.Goto;

public class MathUtils {
   public static boolean isInteger(String s) {
      try {
         Integer.parseInt(s);
         return true;
      } catch (NumberFormatException var2) {
         return false;
      }
   }

   public static int clamp(int num, int min, int max) {
      return num < min ? min : Math.min(num, max);
   }

   public static float clamp(float num, float min, float max) {
      return num < min ? min : Math.min(num, max);
   }

   public static double clamp(double num, double min, double max) {
      return num < min ? min : Math.min(num, max);
   }
}
