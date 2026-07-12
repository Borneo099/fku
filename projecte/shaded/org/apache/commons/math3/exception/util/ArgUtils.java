package moze_intel.projecte.shaded.org.apache.commons.math3.exception.util;

import java.util.ArrayList;
import java.util.List;

public class ArgUtils {
   private ArgUtils() {
   }

   public static Object[] flatten(Object[] array) {
      List list = new ArrayList();
      if (array != null) {
         Object[] arr$ = array;
         int len$ = array.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Object o = arr$[i$];
            if (o instanceof Object[]) {
               Object[] arr$ = flatten((Object[])((Object[])o));
               int len$ = arr$.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  Object oR = arr$[i$];
                  list.add(oR);
               }
            } else {
               list.add(o);
            }
         }
      }

      return list.toArray();
   }
}
