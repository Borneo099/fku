package moze_intel.projecte.api.nss;

import java.util.function.Consumer;

public interface NSSTag extends NormalizedSimpleStack {
   boolean representsTag();

   void forEachElement(Consumer var1);
}
