package moze_intel.projecte.emc.mappers.customConversions.json;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import moze_intel.projecte.emc.mappers.customConversions.CustomConversionMapper;

public class CustomConversionFile {
   public boolean replace = false;
   public String comment;
   public final Map groups = new HashMap();
   public final FixedValues values = new FixedValues();

   public static CustomConversionFile merge(CustomConversionFile left, CustomConversionFile right) {
      if (right.replace) {
         return right;
      } else {
         Iterator var2 = right.groups.entrySet().iterator();

         while(var2.hasNext()) {
            Map.Entry e = (Map.Entry)var2.next();
            left.groups.merge((String)e.getKey(), (ConversionGroup)e.getValue(), ConversionGroup::merge);
         }

         left.values.merge(right.values);
         return left;
      }
   }

   public void write(File file) throws IOException {
      FileWriter fileWriter = new FileWriter(file);

      try {
         CustomConversionMapper.GSON.toJson(this, fileWriter);
      } catch (Throwable var6) {
         try {
            fileWriter.close();
         } catch (Throwable var5) {
            var6.addSuppressed(var5);
         }

         throw var6;
      }

      fileWriter.close();
   }
}
