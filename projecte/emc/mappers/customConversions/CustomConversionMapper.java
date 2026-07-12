package moze_intel.projecte.emc.mappers.customConversions;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSFake;
import moze_intel.projecte.api.nss.NSSTag;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.emc.json.NSSSerializer;
import moze_intel.projecte.emc.mappers.customConversions.json.ConversionGroup;
import moze_intel.projecte.emc.mappers.customConversions.json.CustomConversion;
import moze_intel.projecte.emc.mappers.customConversions.json.CustomConversionDeserializer;
import moze_intel.projecte.emc.mappers.customConversions.json.CustomConversionFile;
import moze_intel.projecte.emc.mappers.customConversions.json.FixedValues;
import moze_intel.projecte.emc.mappers.customConversions.json.FixedValuesDeserializer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

@EMCMapper
public class CustomConversionMapper implements IEMCMapper {
   public static final Gson GSON;

   public String getName() {
      return "CustomConversionMapper";
   }

   public String getDescription() {
      return "Loads json files within datapacks (data/<domain>/pe_custom_conversions/*.json) to add values and conversions";
   }

   public void addMappings(IMappingCollector mapper, CommentedFileConfig config, ReloadableServerResources serverResources, RegistryAccess registryAccess, ResourceManager resourceManager) {
      Map files = load(resourceManager);
      Iterator var7 = files.values().iterator();

      while(var7.hasNext()) {
         CustomConversionFile file = (CustomConversionFile)var7.next();
         addMappingsFromFile(file, mapper);
      }

   }

   private static Map load(ResourceManager resourceManager) {
      Map loading = new HashMap();
      String folder = "pe_custom_conversions";
      String extension = ".json";
      int folderLength = folder.length();
      int extensionLength = extension.length();
      Iterator var6 = resourceManager.m_214160_(folder, (n) -> {
         return n.m_135815_().endsWith(extension);
      }).entrySet().iterator();

      label55:
      while(var6.hasNext()) {
         Map.Entry entry = (Map.Entry)var6.next();
         ResourceLocation file = (ResourceLocation)entry.getKey();
         ResourceLocation conversionId = new ResourceLocation(file.m_135827_(), file.m_135815_().substring(folderLength + 1, file.m_135815_().length() - extensionLength));
         PECore.LOGGER.info("Considering file {}, ID {}", file, conversionId);
         NSSFake.setCurrentNamespace(conversionId.toString());

         try {
            Iterator var10 = ((List)entry.getValue()).iterator();

            while(true) {
               CustomConversionFile result;
               while(true) {
                  if (!var10.hasNext()) {
                     continue label55;
                  }

                  Resource resource = (Resource)var10.next();

                  try {
                     Reader reader = resource.m_215508_();

                     try {
                        result = parseJson(reader);
                     } catch (Throwable var17) {
                        if (reader != null) {
                           try {
                              reader.close();
                           } catch (Throwable var16) {
                              var17.addSuppressed(var16);
                           }
                        }

                        throw var17;
                     }

                     if (reader != null) {
                        reader.close();
                     }
                     break;
                  } catch (JsonParseException var18) {
                     PECore.LOGGER.error("Malformed JSON", var18);
                  }
               }

               loading.merge(conversionId, result, CustomConversionFile::merge);
            }
         } catch (IOException var19) {
            PECore.LOGGER.error("Could not load resource {}", file, var19);
         }
      }

      NSSFake.resetNamespace();
      return loading;
   }

   private static void addMappingsFromFile(CustomConversionFile file, IMappingCollector mapper) {
      Iterator var2 = file.groups.entrySet().iterator();

      Map.Entry entry;
      while(var2.hasNext()) {
         entry = (Map.Entry)var2.next();
         PECore.debugLog("Adding conversions from group '{}' with comment '{}'", entry.getKey(), ((ConversionGroup)entry.getValue()).comment);
         Iterator var4 = ((ConversionGroup)entry.getValue()).conversions.iterator();

         while(var4.hasNext()) {
            CustomConversion conversion = (CustomConversion)var4.next();
            mapper.addConversion(conversion.count, conversion.output, (Map)conversion.ingredients);
         }
      }

      var2 = file.values.setValueBefore.entrySet().iterator();

      NormalizedSimpleStack out;
      NSSTag nssTag;
      while(var2.hasNext()) {
         entry = (Map.Entry)var2.next();
         out = (NormalizedSimpleStack)entry.getKey();
         mapper.setValueBefore(out, (Long)entry.getValue());
         if (out instanceof NSSTag nssTag) {
            nssTag.forEachElement((normalizedSimpleStack) -> {
               mapper.setValueBefore(normalizedSimpleStack, (Long)entry.getValue());
            });
         }
      }

      var2 = file.values.setValueAfter.entrySet().iterator();

      while(var2.hasNext()) {
         entry = (Map.Entry)var2.next();
         out = (NormalizedSimpleStack)entry.getKey();
         mapper.setValueAfter(out, (Long)entry.getValue());
         if (out instanceof NSSTag nssTag) {
            nssTag.forEachElement((normalizedSimpleStack) -> {
               mapper.setValueAfter(normalizedSimpleStack, (Long)entry.getValue());
            });
         }
      }

      CustomConversion conversion;
      for(var2 = file.values.conversion.iterator(); var2.hasNext(); mapper.setValueFromConversion(conversion.count, out, (Map)conversion.ingredients)) {
         conversion = (CustomConversion)var2.next();
         out = conversion.output;
         if (conversion.propagateTags && out instanceof NSSTag nssTag) {
            nssTag.forEachElement((normalizedSimpleStack) -> {
               mapper.setValueFromConversion(conversion.count, normalizedSimpleStack, (Map)conversion.ingredients);
            });
         }
      }

   }

   public static CustomConversionFile parseJson(Reader json) {
      return (CustomConversionFile)GSON.fromJson(new BufferedReader(json), CustomConversionFile.class);
   }

   static {
      GSON = (new GsonBuilder()).registerTypeAdapter(CustomConversion.class, new CustomConversionDeserializer()).registerTypeAdapter(FixedValues.class, new FixedValuesDeserializer()).registerTypeAdapter(NormalizedSimpleStack.class, NSSSerializer.INSTANCE).setPrettyPrinting().create();
   }
}
