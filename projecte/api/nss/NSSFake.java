package moze_intel.projecte.api.nss;

import com.google.common.base.Objects;
import org.jetbrains.annotations.NotNull;

public final class NSSFake implements NormalizedSimpleStack {
   private static String currentNamespace = "";
   private final String namespace;
   private final String description;

   private NSSFake(String namespace, String description) {
      this.namespace = namespace;
      this.description = description;
   }

   public static void resetNamespace() {
      setCurrentNamespace("");
   }

   public static void setCurrentNamespace(@NotNull String ns) {
      currentNamespace = ns;
   }

   public static @NotNull NormalizedSimpleStack create(String description) {
      return new NSSFake(currentNamespace, description);
   }

   public boolean equals(Object o) {
      boolean var10000;
      if (o instanceof NSSFake fake) {
         if (this.description.equals(fake.description) && this.namespace.equals(fake.namespace)) {
            var10000 = true;
            return var10000;
         }
      }

      var10000 = false;
      return var10000;
   }

   public int hashCode() {
      return Objects.hashCode(new Object[]{this.namespace, this.description});
   }

   public String json() {
      return "FAKE|" + this.description;
   }

   public String toString() {
      return "NSSFAKE:" + this.namespace + "/" + this.description;
   }
}
