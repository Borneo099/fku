package fku.org.example.fku.features.dynamicisland;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;

public class FeatureStateRegistry {
   private static final ConcurrentHashMap states = new ConcurrentHashMap();

   public static void register(String name, BooleanSupplier supplier) {
      if (name != null && supplier != null) {
         states.put(name, supplier);
      }

   }

   public static Boolean isEnabled(String name) {
      BooleanSupplier s = (BooleanSupplier)states.get(name);
      return s == null ? null : s.getAsBoolean();
   }
}
