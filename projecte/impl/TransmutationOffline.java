package moze_intel.projecte.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.impl.capability.KnowledgeImpl;
import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

public class TransmutationOffline {
   private static final IKnowledgeProvider NOT_FOUND_PROVIDER = immutableCopy(KnowledgeImpl.getDefault());
   private static final Map cachedKnowledgeProviders = new HashMap();

   public static void cleanAll() {
      cachedKnowledgeProviders.clear();
   }

   public static void clear(UUID playerUUID) {
      cachedKnowledgeProviders.remove(playerUUID);
   }

   static IKnowledgeProvider forPlayer(UUID playerUUID) {
      if (!cachedKnowledgeProviders.containsKey(playerUUID) && !cacheOfflineData(playerUUID)) {
         cachedKnowledgeProviders.put(playerUUID, NOT_FOUND_PROVIDER);
      }

      return (IKnowledgeProvider)cachedKnowledgeProviders.get(playerUUID);
   }

   private static boolean cacheOfflineData(UUID playerUUID) {
      Preconditions.checkState(Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER, "CRITICAL: Trying to read filesystem on client!!");
      MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
      File playerData = server.m_129843_(LevelResource.f_78176_).toFile();
      if (playerData.exists()) {
         File player = new File(playerData, playerUUID.toString() + ".dat");
         if (player.exists() && player.isFile()) {
            try {
               FileInputStream in = new FileInputStream(player);

               boolean var8;
               try {
                  CompoundTag playerDat = NbtIo.m_128939_(in);
                  CompoundTag knowledgeProvider = playerDat.m_128469_("ForgeCaps").m_128469_(KnowledgeImpl.Provider.NAME.toString());
                  IKnowledgeProvider provider = KnowledgeImpl.getDefault();
                  provider.deserializeNBT(knowledgeProvider);
                  cachedKnowledgeProviders.put(playerUUID, immutableCopy(provider));
                  PECore.debugLog("Caching offline data for UUID: {}", playerUUID);
                  var8 = true;
               } catch (Throwable var10) {
                  try {
                     in.close();
                  } catch (Throwable var9) {
                     var10.addSuppressed(var9);
                  }

                  throw var10;
               }

               in.close();
               return var8;
            } catch (IOException var11) {
               PECore.LOGGER.warn("Failed to cache offline data for API calls for UUID: {}", playerUUID);
            }
         }
      }

      return false;
   }

   private static IKnowledgeProvider immutableCopy(final IKnowledgeProvider toCopy) {
      return new IKnowledgeProvider() {
         final Set immutableKnowledge = ImmutableSet.copyOf(toCopy.getKnowledge());
         final IItemHandlerModifiable immutableInputLocks = ItemHelper.immutableCopy(toCopy.getInputAndLocks());

         public boolean hasFullKnowledge() {
            return toCopy.hasFullKnowledge();
         }

         public void setFullKnowledge(boolean fullKnowledge) {
         }

         public void clearKnowledge() {
         }

         public boolean hasKnowledge(@NotNull ItemInfo info) {
            return toCopy.hasKnowledge(info);
         }

         public boolean addKnowledge(@NotNull ItemInfo info) {
            return false;
         }

         public boolean removeKnowledge(@NotNull ItemInfo info) {
            return false;
         }

         public @NotNull Set getKnowledge() {
            return this.immutableKnowledge;
         }

         public @NotNull IItemHandler getInputAndLocks() {
            return this.immutableInputLocks;
         }

         public BigInteger getEmc() {
            return toCopy.getEmc();
         }

         public void setEmc(BigInteger emc) {
         }

         public void sync(@NotNull ServerPlayer player) {
            toCopy.sync(player);
         }

         public void syncEmc(@NotNull ServerPlayer player) {
            toCopy.syncEmc(player);
         }

         public void syncKnowledgeChange(@NotNull ServerPlayer player, ItemInfo change, boolean learned) {
            toCopy.syncKnowledgeChange(player, change, learned);
         }

         public void syncInputAndLocks(@NotNull ServerPlayer player, List slotsChanged, IKnowledgeProvider.TargetUpdateType updateTargets) {
            toCopy.syncInputAndLocks(player, slotsChanged, updateTargets);
         }

         public void receiveInputsAndLocks(Map changes) {
         }

         public CompoundTag serializeNBT() {
            return (CompoundTag)toCopy.serializeNBT();
         }

         public void deserializeNBT(CompoundTag nbt) {
         }
      };
   }
}
