package moze_intel.projecte.emc;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.event.EMCRemapEvent;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IExtendedMappingCollector;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.generator.IValueGenerator;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.emc.arithmetic.HiddenBigFractionArithmetic;
import moze_intel.projecte.emc.collector.DumpToFileCollector;
import moze_intel.projecte.emc.collector.LongToBigFractionCollector;
import moze_intel.projecte.emc.generator.BigFractionToLongGenerator;
import moze_intel.projecte.emc.mappers.TagMapper;
import moze_intel.projecte.emc.pregenerated.PregeneratedEMC;
import moze_intel.projecte.gameObjs.container.TransmutationContainer;
import moze_intel.projecte.impl.capability.KnowledgeImpl;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.to_client.SyncEmcPKT;
import moze_intel.projecte.network.packets.to_client.knowledge.UpdateTransmutationTargetsPkt;
import moze_intel.projecte.utils.AnnotationHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public final class EMCMappingHandler {
   private static final List mappers = new ArrayList();
   private static final Map emc = new HashMap();
   private static int loadIndex = -1;

   public static void loadMappers() {
      if (mappers.isEmpty()) {
         mappers.addAll(AnnotationHelper.getEMCMappers());
         mappers.add(new TagMapper());
      }

   }

   public static Object getOrSetDefault(CommentedFileConfig config, String key, String comment, Object defaultValue) {
      Object val = config.get(key);
      if (val == null) {
         val = defaultValue;
         config.set(key, defaultValue);
         config.setComment(key, comment);
      }

      return val;
   }

   public static void map(ReloadableServerResources serverResources, RegistryAccess registryAccess, ResourceManager resourceManager) {
      clearEmcMap();
      SimpleGraphMapper mapper = new SimpleGraphMapper(new HiddenBigFractionArithmetic());
      IValueGenerator valueGenerator = new BigFractionToLongGenerator(mapper);
      IExtendedMappingCollector mappingCollector = new LongToBigFractionCollector(mapper);
      Path path = ProjectEConfig.CONFIG_DIR.resolve("mapping.toml");

      try {
         if (path.toFile().createNewFile()) {
            PECore.debugLog("Created mapping.toml");
         }
      } catch (IOException var19) {
         PECore.LOGGER.error("Couldn't create mapping.toml", var19);
      }

      CommentedFileConfig config = (CommentedFileConfig)CommentedFileConfig.builder(path).build();
      config.load();
      boolean dumpToFile = (Boolean)getOrSetDefault(config, "general.dumpEverythingToFile", "Want to take a look at the internals of EMC Calculation? Enable this to write all the conversions and setValue-Commands to config/ProjectE/mappingdump.json", false);
      boolean shouldUsePregenerated = (Boolean)getOrSetDefault(config, "general.pregenerate", "When the next EMC mapping occurs write the results to config/ProjectE/pregenerated_emc.json and only ever run the mapping again when that file does not exist, this setting is set to false, or an error occurred parsing that file.", false);
      boolean logFoundExploits = (Boolean)getOrSetDefault(config, "general.logEMCExploits", "Log known EMC Exploits. This can not and will not find all possible exploits. This will only find exploits that result in fixed/custom emc values that the algorithm did not overwrite. Exploits that derive from conversions that are unknown to ProjectE will not be found.", true);
      if (dumpToFile) {
         mappingCollector = new DumpToFileCollector(ProjectEConfig.CONFIG_DIR.resolve("mappingdump.json").toFile(), (IExtendedMappingCollector)mappingCollector);
      }

      File pregeneratedEmcFile = Paths.get("config", "ProjectE", "pregenerated_emc.json").toFile();
      Object graphMapperValues;
      Iterator var13;
      if (shouldUsePregenerated && pregeneratedEmcFile.canRead() && PregeneratedEMC.tryRead(pregeneratedEmcFile, (Map)(graphMapperValues = new HashMap()))) {
         PECore.LOGGER.info("Loaded {} values from pregenerated EMC File", ((Map)graphMapperValues).size());
      } else {
         SimpleGraphMapper.setLogFoundExploits(logFoundExploits);
         PECore.debugLog("Starting to collect Mappings...");
         var13 = mappers.iterator();

         while(var13.hasNext()) {
            IEMCMapper emcMapper = (IEMCMapper)var13.next();

            try {
               if ((Boolean)getOrSetDefault(config, "enabledMappers." + emcMapper.getName(), emcMapper.getDescription(), emcMapper.isAvailable())) {
                  DumpToFileCollector.currentGroupName = emcMapper.getName();
                  emcMapper.addMappings((IMappingCollector)mappingCollector, config, serverResources, registryAccess, resourceManager);
                  PECore.debugLog("Collected Mappings from " + emcMapper.getClass().getName());
               }
            } catch (Exception var18) {
               PECore.LOGGER.error(LogUtils.FATAL_MARKER, "Exception during Mapping Collection from Mapper {}. PLEASE REPORT THIS! EMC VALUES MIGHT BE INCONSISTENT!", emcMapper.getClass().getName(), var18);
            }
         }

         DumpToFileCollector.currentGroupName = "NSSHelper";
         PECore.debugLog("Mapping Collection finished");
         ((IExtendedMappingCollector)mappingCollector).finishCollection();
         PECore.debugLog("Starting to generate Values:");
         config.save();
         config.close();
         graphMapperValues = valueGenerator.generateValues();
         PECore.debugLog("Generated Values...");
         filterEMCMap((Map)graphMapperValues);
         if (shouldUsePregenerated) {
            try {
               PregeneratedEMC.write(pregeneratedEmcFile, (Map)graphMapperValues);
               PECore.debugLog("Wrote Pregen-file!");
            } catch (IOException var17) {
               PECore.LOGGER.error("Failed to write Pregen-file", var17);
            }
         }
      }

      var13 = ((Map)graphMapperValues).entrySet().iterator();

      while(var13.hasNext()) {
         Map.Entry entry = (Map.Entry)var13.next();
         NSSItem normStackItem = (NSSItem)entry.getKey();
         ItemInfo obj = ItemInfo.fromNSS(normStackItem);
         if (obj != null) {
            emc.put(obj, (Long)entry.getValue());
         } else {
            PECore.LOGGER.warn("Could not add EMC value for {}, item does not exist!", normStackItem.getResourceLocation());
         }
      }

      fireEmcRemapEvent();
   }

   private static void fireEmcRemapEvent() {
      FuelMapper.loadMap();
      ++loadIndex;
      MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
      if (server != null) {
         Iterator var1 = server.m_6846_().m_11314_().iterator();

         while(var1.hasNext()) {
            ServerPlayer player = (ServerPlayer)var1.next();
            player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent((knowledge) -> {
               if (knowledge instanceof KnowledgeImpl.DefaultImpl impl) {
                  if (impl.pruneStaleKnowledge()) {
                     knowledge.sync(player);
                     return;
                  }
               }

               if (player.f_36096_ instanceof TransmutationContainer) {
                  PacketHandler.sendTo(new UpdateTransmutationTargetsPkt(), player);
               }

            });
         }
      }

      MinecraftForge.EVENT_BUS.post(new EMCRemapEvent());
   }

   public static int getLoadIndex() {
      return loadIndex;
   }

   private static void filterEMCMap(Map map) {
      map.entrySet().removeIf((e) -> {
         Object patt9021$temp = e.getKey();
         boolean var10000;
         if (patt9021$temp instanceof NSSItem nssItem) {
            if (!nssItem.representsTag() && (Long)e.getValue() > 0L) {
               var10000 = false;
               return var10000;
            }
         }

         var10000 = true;
         return var10000;
      });
   }

   public static int getEmcMapSize() {
      return emc.size();
   }

   public static boolean hasEmcValue(@NotNull ItemInfo info) {
      return emc.containsKey(info);
   }

   public static @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long getStoredEmcValue(@NotNull ItemInfo info) {
      return (Long)emc.getOrDefault(info, 0L);
   }

   public static void clearEmcMap() {
      emc.clear();
   }

   public static Set getMappedItems() {
      return new HashSet(emc.keySet());
   }

   public static void fromPacket(SyncEmcPKT.EmcPKTInfo[] data) {
      emc.clear();
      SyncEmcPKT.EmcPKTInfo[] var1 = data;
      int var2 = data.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         SyncEmcPKT.EmcPKTInfo info = var1[var3];
         emc.put(ItemInfo.fromItem(info.item(), info.nbt()), info.emc());
      }

   }

   public static SyncEmcPKT.EmcPKTInfo[] createPacketData() {
      SyncEmcPKT.EmcPKTInfo[] ret = new SyncEmcPKT.EmcPKTInfo[emc.size()];
      int i = 0;

      for(Iterator var2 = emc.entrySet().iterator(); var2.hasNext(); ++i) {
         Map.Entry entry = (Map.Entry)var2.next();
         ItemInfo info = (ItemInfo)entry.getKey();
         ret[i] = new SyncEmcPKT.EmcPKTInfo(info.getItem(), info.getNBT(), (Long)entry.getValue());
      }

      return ret;
   }
}
