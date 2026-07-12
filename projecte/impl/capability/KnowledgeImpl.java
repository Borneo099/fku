package moze_intel.projecte.impl.capability;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.event.PlayerKnowledgeChangeEvent;
import moze_intel.projecte.capability.managing.SerializableCapabilityResolver;
import moze_intel.projecte.emc.EMCMappingHandler;
import moze_intel.projecte.emc.nbt.NBTManager;
import moze_intel.projecte.gameObjs.items.Tome;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.to_client.knowledge.KnowledgeSyncChangePKT;
import moze_intel.projecte.network.packets.to_client.knowledge.KnowledgeSyncEmcPKT;
import moze_intel.projecte.network.packets.to_client.knowledge.KnowledgeSyncInputsAndLocksPKT;
import moze_intel.projecte.network.packets.to_client.knowledge.KnowledgeSyncPKT;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class KnowledgeImpl {
   public static IKnowledgeProvider getDefault() {
      return new DefaultImpl((Player)null);
   }

   private KnowledgeImpl() {
   }

   public static class DefaultImpl implements IKnowledgeProvider {
      private final @Nullable Player player;
      private final Set knowledge = new HashSet();
      private final ItemStackHandler inputLocks = new ItemStackHandler(9);
      private BigInteger emc;
      private boolean fullKnowledge;

      private DefaultImpl(@Nullable Player player) {
         this.emc = BigInteger.ZERO;
         this.fullKnowledge = false;
         this.player = player;
      }

      private void fireChangedEvent() {
         if (this.player != null && !this.player.m_9236_().f_46443_) {
            MinecraftForge.EVENT_BUS.post(new PlayerKnowledgeChangeEvent(this.player));
         }

      }

      public boolean hasFullKnowledge() {
         return this.fullKnowledge;
      }

      public void setFullKnowledge(boolean fullKnowledge) {
         boolean changed = this.fullKnowledge != fullKnowledge;
         this.fullKnowledge = fullKnowledge;
         if (changed) {
            this.fireChangedEvent();
         }

      }

      public void clearKnowledge() {
         boolean hasKnowledge = this.fullKnowledge || !this.knowledge.isEmpty();
         this.knowledge.clear();
         this.fullKnowledge = false;
         if (hasKnowledge) {
            this.fireChangedEvent();
         }

      }

      private @Nullable ItemInfo getIfPersistent(@NotNull ItemInfo info) {
         if (info.hasNBT() && !EMCMappingHandler.hasEmcValue(info)) {
            ItemInfo cleanedInfo = NBTManager.getPersistentInfo(info);
            return cleanedInfo.hasNBT() && !EMCMappingHandler.hasEmcValue(cleanedInfo) ? cleanedInfo : null;
         } else {
            return null;
         }
      }

      public boolean hasKnowledge(@NotNull ItemInfo info) {
         if (!this.fullKnowledge) {
            return this.knowledge.contains(NBTManager.getPersistentInfo(info));
         } else {
            ItemInfo persistentInfo = this.getIfPersistent(info);
            return persistentInfo == null || this.knowledge.contains(persistentInfo);
         }
      }

      public boolean addKnowledge(@NotNull ItemInfo info) {
         if (this.fullKnowledge) {
            ItemInfo persistentInfo = this.getIfPersistent(info);
            return persistentInfo == null ? false : this.tryAdd(persistentInfo);
         } else if (info.getItem() instanceof Tome) {
            if (info.hasNBT()) {
               info = ItemInfo.fromItem(info.getItem());
            }

            this.knowledge.add(info);
            this.fullKnowledge = true;
            this.fireChangedEvent();
            return true;
         } else {
            return this.tryAdd(NBTManager.getPersistentInfo(info));
         }
      }

      private boolean tryAdd(@NotNull ItemInfo cleanedInfo) {
         if (this.knowledge.add(cleanedInfo)) {
            this.fireChangedEvent();
            return true;
         } else {
            return false;
         }
      }

      public boolean removeKnowledge(@NotNull ItemInfo info) {
         if (this.fullKnowledge) {
            if (info.getItem() instanceof Tome) {
               if (info.hasNBT()) {
                  info = ItemInfo.fromItem(info.getItem());
               }

               this.knowledge.remove(info);
               this.fullKnowledge = false;
               this.fireChangedEvent();
               return true;
            } else {
               ItemInfo persistentInfo = this.getIfPersistent(info);
               return persistentInfo != null && this.tryRemove(persistentInfo);
            }
         } else {
            return this.tryRemove(NBTManager.getPersistentInfo(info));
         }
      }

      private boolean tryRemove(@NotNull ItemInfo cleanedInfo) {
         if (this.knowledge.remove(cleanedInfo)) {
            this.fireChangedEvent();
            return true;
         } else {
            return false;
         }
      }

      public @NotNull Set getKnowledge() {
         if (this.fullKnowledge) {
            Set allKnowledge = EMCMappingHandler.getMappedItems();
            allKnowledge.addAll(this.knowledge);
            return Collections.unmodifiableSet(allKnowledge);
         } else {
            return Collections.unmodifiableSet(this.knowledge);
         }
      }

      public @NotNull IItemHandlerModifiable getInputAndLocks() {
         return this.inputLocks;
      }

      public BigInteger getEmc() {
         return this.emc;
      }

      public void setEmc(BigInteger emc) {
         this.emc = emc;
      }

      public void sync(@NotNull ServerPlayer player) {
         PacketHandler.sendTo(new KnowledgeSyncPKT(this.serializeNBT()), player);
      }

      public void syncEmc(@NotNull ServerPlayer player) {
         PacketHandler.sendTo(new KnowledgeSyncEmcPKT(this.getEmc()), player);
      }

      public void syncKnowledgeChange(@NotNull ServerPlayer player, ItemInfo change, boolean learned) {
         PacketHandler.sendTo(new KnowledgeSyncChangePKT(change, learned), player);
      }

      public void syncInputAndLocks(@NotNull ServerPlayer player, List slotsChanged, IKnowledgeProvider.TargetUpdateType updateTargets) {
         if (!slotsChanged.isEmpty()) {
            int slots = this.inputLocks.getSlots();
            Map stacksToSync = new HashMap();
            Iterator var6 = slotsChanged.iterator();

            while(var6.hasNext()) {
               int slot = (Integer)var6.next();
               if (slot >= 0 && slot < slots) {
                  stacksToSync.put(slot, this.inputLocks.getStackInSlot(slot));
               }
            }

            if (!stacksToSync.isEmpty()) {
               PacketHandler.sendTo(new KnowledgeSyncInputsAndLocksPKT(stacksToSync, updateTargets), player);
            }
         }

      }

      public void receiveInputsAndLocks(Map changes) {
         int slots = this.inputLocks.getSlots();
         Iterator var3 = changes.entrySet().iterator();

         while(var3.hasNext()) {
            Map.Entry entry = (Map.Entry)var3.next();
            int slot = (Integer)entry.getKey();
            if (slot >= 0 && slot < slots) {
               this.inputLocks.setStackInSlot(slot, (ItemStack)entry.getValue());
            }
         }

      }

      public CompoundTag serializeNBT() {
         CompoundTag properties = new CompoundTag();
         properties.m_128359_("transmutationEmc", this.emc.toString());
         ListTag knowledgeWrite = new ListTag();
         Iterator var3 = this.knowledge.iterator();

         while(var3.hasNext()) {
            ItemInfo i = (ItemInfo)var3.next();
            knowledgeWrite.add(i.write(new CompoundTag()));
         }

         properties.m_128365_("knowledge", knowledgeWrite);
         properties.m_128365_("inputlock", this.inputLocks.serializeNBT());
         properties.m_128379_("fullknowledge", this.fullKnowledge);
         return properties;
      }

      public void deserializeNBT(CompoundTag properties) {
         String transmutationEmc = properties.m_128461_("transmutationEmc");
         this.emc = transmutationEmc.isEmpty() ? BigInteger.ZERO : new BigInteger(transmutationEmc);
         ListTag list = properties.m_128437_("knowledge", 10);

         int i;
         for(i = 0; i < list.size(); ++i) {
            ItemInfo info = ItemInfo.read(list.m_128728_(i));
            if (info != null) {
               this.knowledge.add(info);
            }
         }

         for(i = 0; i < this.inputLocks.getSlots(); ++i) {
            this.inputLocks.setStackInSlot(i, ItemStack.f_41583_);
         }

         this.inputLocks.deserializeNBT(properties.m_128469_("inputlock"));
         this.fullKnowledge = properties.m_128471_("fullknowledge");
      }

      public final boolean pruneStaleKnowledge() {
         List toAdd = new ArrayList();
         boolean hasRemoved = this.knowledge.removeIf((info) -> {
            ItemInfo persistentInfo = NBTManager.getPersistentInfo(info);
            if (!info.equals(persistentInfo)) {
               if (EMCHelper.doesItemHaveEmc(persistentInfo)) {
                  toAdd.add(persistentInfo);
               }

               return true;
            } else {
               return !EMCHelper.doesItemHaveEmc(info);
            }
         });
         return this.knowledge.addAll(toAdd) || hasRemoved;
      }
   }

   public static class Provider extends SerializableCapabilityResolver {
      public static final ResourceLocation NAME = PECore.rl("knowledge");

      public Provider(Player player) {
         super(new DefaultImpl(player));
      }

      public @NotNull Capability getMatchingCapability() {
         return PECapabilities.KNOWLEDGE_CAPABILITY;
      }
   }
}
