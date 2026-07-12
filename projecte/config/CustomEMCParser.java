package moze_intel.projecte.config;

import com.google.common.base.Charsets;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.emc.json.NSSSerializer;

public final class CustomEMCParser {
   private static final Gson GSON;
   private static final File CONFIG;
   public static CustomEMCFile currentEntries;
   private static boolean dirty;

   public static void init() {
      flush();
      if (!CONFIG.exists()) {
         try {
            if (CONFIG.createNewFile()) {
               writeDefaultFile();
            }
         } catch (IOException var6) {
            PECore.LOGGER.error(LogUtils.FATAL_MARKER, "Exception in file I/O: couldn't create custom configuration files.");
         }
      }

      try {
         BufferedReader reader = new BufferedReader(new FileReader(CONFIG));

         try {
            currentEntries = (CustomEMCFile)GSON.fromJson(reader, CustomEMCFile.class);
            currentEntries.entries.removeIf((e) -> {
               return !(e.item instanceof NSSItem) || e.emc < 0L;
            });
         } catch (Throwable var4) {
            try {
               reader.close();
            } catch (Throwable var3) {
               var4.addSuppressed(var3);
            }

            throw var4;
         }

         reader.close();
      } catch (JsonParseException | IOException var5) {
         PECore.LOGGER.error(LogUtils.FATAL_MARKER, "Couldn't read custom emc file", var5);
         currentEntries = new CustomEMCFile(new ArrayList());
      }

   }

   private static NormalizedSimpleStack getNss(String str) {
      return NSSSerializer.INSTANCE.deserialize(str);
   }

   public static void addToFile(String toAdd, long emc) {
      NormalizedSimpleStack nss = getNss(toAdd);
      CustomEMCEntry entry = new CustomEMCEntry(nss, emc);
      int setAt = -1;

      for(int i = 0; i < currentEntries.entries.size(); ++i) {
         if (((CustomEMCEntry)currentEntries.entries.get(i)).item.equals(nss)) {
            setAt = i;
            break;
         }
      }

      if (setAt == -1) {
         currentEntries.entries.add(entry);
      } else {
         currentEntries.entries.set(setAt, entry);
      }

      dirty = true;
   }

   public static boolean removeFromFile(String toRemove) {
      NormalizedSimpleStack nss = getNss(toRemove);
      Iterator iter = currentEntries.entries.iterator();
      boolean removed = false;

      while(iter.hasNext()) {
         if (((CustomEMCEntry)iter.next()).item.equals(nss)) {
            iter.remove();
            dirty = true;
            removed = true;
         }
      }

      return removed;
   }

   public static void flush() {
      if (dirty) {
         try {
            Files.asCharSink(CONFIG, Charsets.UTF_8, new FileWriteMode[0]).write(GSON.toJson(currentEntries));
         } catch (IOException var1) {
            PECore.LOGGER.error("Failed to write custom EMC file", var1);
         }

         dirty = false;
      }

   }

   private static void writeDefaultFile() {
      JsonObject elem = (JsonObject)GSON.toJsonTree(new CustomEMCFile(new ArrayList()));
      elem.add("__comment", new JsonPrimitive("Use the in-game commands to edit this file"));

      try {
         Files.asCharSink(CONFIG, Charsets.UTF_8, new FileWriteMode[0]).write(GSON.toJson(elem));
      } catch (IOException var2) {
         PECore.LOGGER.error("Failed to write default custom EMC file", var2);
      }

   }

   static {
      GSON = (new GsonBuilder()).registerTypeAdapter(NormalizedSimpleStack.class, NSSSerializer.INSTANCE).setPrettyPrinting().create();
      CONFIG = ProjectEConfig.CONFIG_DIR.resolve("custom_emc.json").toFile();
      dirty = false;
   }

   public static class CustomEMCFile {
      public final List entries;

      public CustomEMCFile(List entries) {
         this.entries = entries;
      }
   }

   public static class CustomEMCEntry {
      public final NormalizedSimpleStack item;
      public final long emc;

      private CustomEMCEntry(NormalizedSimpleStack item, long emc) {
         this.item = item;
         this.emc = emc;
      }

      public boolean equals(Object o) {
         return o == this || o instanceof CustomEMCEntry && this.item.equals(((CustomEMCEntry)o).item) && this.emc == ((CustomEMCEntry)o).emc;
      }

      public int hashCode() {
         int result = this.item != null ? this.item.hashCode() : 0;
         result = 31 * result + (int)(this.emc ^ this.emc >>> 32);
         return result;
      }
   }
}
