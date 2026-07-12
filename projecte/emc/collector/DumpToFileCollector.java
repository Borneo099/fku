package moze_intel.projecte.emc.collector;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.mapper.arithmetic.IValueArithmetic;
import moze_intel.projecte.api.mapper.collector.IExtendedMappingCollector;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.emc.mappers.customConversions.json.ConversionGroup;
import moze_intel.projecte.emc.mappers.customConversions.json.CustomConversion;
import moze_intel.projecte.emc.mappers.customConversions.json.CustomConversionFile;

public class DumpToFileCollector extends AbstractMappingCollector {
   public static String currentGroupName = "default";
   private final CustomConversionFile out = new CustomConversionFile();
   private final IExtendedMappingCollector inner;
   private final File file;

   public DumpToFileCollector(File f, IExtendedMappingCollector inner) {
      super(inner.getArithmetic());
      this.file = f;
      this.inner = inner;
   }

   public void setValueFromConversion(int outnumber, NormalizedSimpleStack something, Map ingredientsWithAmount) {
      this.inner.setValueFromConversion(outnumber, something, ingredientsWithAmount);
      if (something != null && !ingredientsWithAmount.containsKey((Object)null)) {
         this.out.values.conversion.add(CustomConversion.getFor(outnumber, something, ingredientsWithAmount));
      }
   }

   public void addConversion(int outnumber, NormalizedSimpleStack output, Map ingredientsWithAmount, IValueArithmetic arithmeticForConversion) {
      this.inner.addConversion(outnumber, output, (Map)ingredientsWithAmount, arithmeticForConversion);
      if (output != null && !ingredientsWithAmount.containsKey((Object)null)) {
         if (!this.out.groups.containsKey(currentGroupName)) {
            this.out.groups.put(currentGroupName, new ConversionGroup());
         }

         ConversionGroup group = (ConversionGroup)this.out.groups.get(currentGroupName);
         group.conversions.add(CustomConversion.getFor(outnumber, output, ingredientsWithAmount));
      }
   }

   public void setValueBefore(NormalizedSimpleStack something, Long value) {
      this.inner.setValueBefore(something, value);
      if (something != null) {
         this.out.values.setValueBefore.put(something, value);
      }
   }

   public void setValueAfter(NormalizedSimpleStack something, Long value) {
      this.inner.setValueAfter(something, value);
      if (something != null) {
         this.out.values.setValueAfter.put(something, value);
      }
   }

   public void finishCollection() {
      try {
         this.out.write(this.file);
      } catch (IOException var2) {
         PECore.LOGGER.error("Failed to dump to file", var2);
      }

      this.inner.finishCollection();
   }
}
