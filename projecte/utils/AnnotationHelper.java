package moze_intel.projecte.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nbt.INBTProcessor;
import moze_intel.projecte.api.nbt.NBTProcessor;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

public class AnnotationHelper {
   private static final Type MAPPER_TYPE = Type.getType(EMCMapper.class);
   private static final Type RECIPE_TYPE_MAPPER_TYPE = Type.getType(RecipeTypeMapper.class);
   private static final Type NBT_PROCESSOR_TYPE = Type.getType(NBTProcessor.class);

   public static List getNBTProcessors() {
      ModList modList = ModList.get();
      List nbtProcessors = new ArrayList();
      Map priorities = new HashMap();
      Iterator var3 = modList.getAllScanData().iterator();

      while(var3.hasNext()) {
         ModFileScanData scanData = (ModFileScanData)var3.next();
         Iterator var5 = scanData.getAnnotations().iterator();

         while(var5.hasNext()) {
            ModFileScanData.AnnotationData data = (ModFileScanData.AnnotationData)var5.next();
            if (NBT_PROCESSOR_TYPE.equals(data.annotationType()) && checkRequiredMods(data)) {
               INBTProcessor processor = getNBTProcessor(data.memberName());
               if (processor != null) {
                  int priority = getPriority(data);
                  nbtProcessors.add(processor);
                  priorities.put(processor, priority);
                  PECore.LOGGER.info("Found and loaded NBT Processor: {}, with priority {}", processor.getName(), priority);
               }
            }
         }
      }

      Objects.requireNonNull(priorities);
      nbtProcessors.sort(Collections.reverseOrder(Comparator.comparing(priorities::get)));
      return nbtProcessors;
   }

   public static List getRecipeTypeMappers() {
      ModList modList = ModList.get();
      List recipeTypeMappers = new ArrayList();
      Map priorities = new HashMap();
      Iterator var3 = modList.getAllScanData().iterator();

      while(var3.hasNext()) {
         ModFileScanData scanData = (ModFileScanData)var3.next();
         Iterator var5 = scanData.getAnnotations().iterator();

         while(var5.hasNext()) {
            ModFileScanData.AnnotationData data = (ModFileScanData.AnnotationData)var5.next();
            if (RECIPE_TYPE_MAPPER_TYPE.equals(data.annotationType()) && checkRequiredMods(data)) {
               IRecipeTypeMapper mapper = getRecipeTypeMapper(data.memberName());
               if (mapper != null) {
                  int priority = getPriority(data);
                  recipeTypeMappers.add(mapper);
                  priorities.put(mapper, priority);
                  PECore.LOGGER.info("Found and loaded RecipeType Mapper: {}, with priority {}", mapper.getName(), priority);
               }
            }
         }
      }

      Objects.requireNonNull(priorities);
      recipeTypeMappers.sort(Collections.reverseOrder(Comparator.comparing(priorities::get)));
      return recipeTypeMappers;
   }

   public static List getEMCMappers() {
      ModList modList = ModList.get();
      List emcMappers = new ArrayList();
      Map priorities = new HashMap();
      Iterator var3 = modList.getAllScanData().iterator();

      while(var3.hasNext()) {
         ModFileScanData scanData = (ModFileScanData)var3.next();
         Iterator var5 = scanData.getAnnotations().iterator();

         while(var5.hasNext()) {
            ModFileScanData.AnnotationData data = (ModFileScanData.AnnotationData)var5.next();
            if (MAPPER_TYPE.equals(data.annotationType()) && checkRequiredMods(data)) {
               IEMCMapper mapper = getEMCMapper(data.memberName());
               if (mapper != null) {
                  try {
                     int priority = getPriority(data);
                     emcMappers.add(mapper);
                     priorities.put(mapper, priority);
                     PECore.LOGGER.info("Found and loaded EMC mapper: {}, with priority {}", mapper.getName(), priority);
                  } catch (ClassCastException var10) {
                     PECore.LOGGER.error("{}: Is not a mapper for {}, to {}", new Object[]{mapper.getClass(), NormalizedSimpleStack.class, Long.class, var10});
                  }
               }
            }
         }
      }

      Objects.requireNonNull(priorities);
      emcMappers.sort(Collections.reverseOrder(Comparator.comparing(priorities::get)));
      return emcMappers;
   }

   private static @Nullable IEMCMapper getEMCMapper(String className) {
      return (IEMCMapper)createOrGetInstance(className, IEMCMapper.class, EMCMapper.Instance.class, IEMCMapper::getName);
   }

   private static @Nullable IRecipeTypeMapper getRecipeTypeMapper(String className) {
      return (IRecipeTypeMapper)createOrGetInstance(className, IRecipeTypeMapper.class, RecipeTypeMapper.Instance.class, IRecipeTypeMapper::getName);
   }

   private static @Nullable INBTProcessor getNBTProcessor(String className) {
      return (INBTProcessor)createOrGetInstance(className, INBTProcessor.class, NBTProcessor.Instance.class, INBTProcessor::getName);
   }

   private static @Nullable Object createOrGetInstance(String className, Class baseClass, Class instanceAnnotation, Function nameFunction) {
      try {
         Class subClass = Class.forName(className).asSubclass(baseClass);
         Field[] fields = subClass.getDeclaredFields();
         Field[] var6 = fields;
         int var7 = fields.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            Field field = var6[var8];
            if (field.isAnnotationPresent(instanceAnnotation)) {
               if (Modifier.isStatic(field.getModifiers())) {
                  try {
                     Object fieldValue = field.get((Object)null);
                     if (baseClass.isInstance(fieldValue)) {
                        PECore.debugLog("Found specified {} instance for: {}. Using it rather than creating a new instance.", baseClass.getSimpleName(), nameFunction.apply(fieldValue));
                        return fieldValue;
                     }

                     PECore.LOGGER.error("{} annotation found on non {} field: {}", new Object[]{instanceAnnotation.getSimpleName(), baseClass.getSimpleName(), field});
                     return null;
                  } catch (IllegalAccessException var12) {
                     PECore.LOGGER.error("{} annotation found on inaccessible field: {}", instanceAnnotation.getSimpleName(), field);
                     return null;
                  }
               }

               PECore.LOGGER.error("{} annotation found on non static field: {}", instanceAnnotation.getSimpleName(), field);
               return null;
            }
         }

         return subClass.getDeclaredConstructor().newInstance();
      } catch (InstantiationException | IllegalAccessException | LinkageError | InvocationTargetException | NoSuchMethodException | ClassNotFoundException var13) {
         PECore.LOGGER.error("Failed to load: {}", className, var13);
         return null;
      }
   }

   private static boolean checkRequiredMods(ModFileScanData.AnnotationData data) {
      Map annotationData = data.annotationData();
      if (annotationData.containsKey("requiredMods")) {
         List requiredMods = (List)annotationData.get("requiredMods");
         if (requiredMods.stream().anyMatch((modid) -> {
            return !ModList.get().isLoaded(modid);
         })) {
            PECore.debugLog("Skipped checking class {}, as its required mods ({}) are not loaded.", data.memberName(), Arrays.toString(requiredMods.toArray()));
            return false;
         }
      }

      return true;
   }

   private static int getPriority(ModFileScanData.AnnotationData data) {
      Map annotationData = data.annotationData();
      return annotationData.containsKey("priority") ? (Integer)annotationData.get("priority") : 0;
   }
}
