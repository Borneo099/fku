package moze_intel.projecte.network.packets.to_client.knowledge;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.gameObjs.container.TransmutationContainer;
import moze_intel.projecte.gameObjs.container.inventory.TransmutationInventory;
import moze_intel.projecte.network.packets.IPEPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public record KnowledgeSyncInputsAndLocksPKT(Map stacksToSync, IKnowledgeProvider.TargetUpdateType updateTargets) implements IPEPacket {
   public KnowledgeSyncInputsAndLocksPKT(Map stacksToSync, IKnowledgeProvider.TargetUpdateType updateTargets) {
      this.stacksToSync = stacksToSync;
      this.updateTargets = updateTargets;
   }

   public void handle(NetworkEvent.Context context) {
      LocalPlayer player = Minecraft.m_91087_().f_91074_;
      if (player != null) {
         player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent((cap) -> {
            cap.receiveInputsAndLocks(this.stacksToSync);
            if (this.updateTargets != IKnowledgeProvider.TargetUpdateType.NONE) {
               AbstractContainerMenu patt1196$temp = player.f_36096_;
               if (patt1196$temp instanceof TransmutationContainer) {
                  TransmutationContainer container = (TransmutationContainer)patt1196$temp;
                  TransmutationInventory transmutationInventory = container.transmutationInventory;
                  if (this.updateTargets == IKnowledgeProvider.TargetUpdateType.ALL) {
                     transmutationInventory.updateClientTargets();
                  } else {
                     transmutationInventory.checkForUpdates();
                  }
               }
            }

         });
      }

      PECore.debugLog("** RECEIVED TRANSMUTATION INPUT AND LOCK DATA CLIENTSIDE **");
   }

   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130130_(this.stacksToSync.size());
      Iterator var2 = this.stacksToSync.entrySet().iterator();

      while(var2.hasNext()) {
         Map.Entry entry = (Map.Entry)var2.next();
         buffer.m_130130_((Integer)entry.getKey());
         buffer.m_130055_((ItemStack)entry.getValue());
      }

      buffer.m_130068_(this.updateTargets);
   }

   public static KnowledgeSyncInputsAndLocksPKT decode(FriendlyByteBuf buffer) {
      int size = buffer.m_130242_();
      Map syncedStacks = new HashMap(size);

      for(int i = 0; i < size; ++i) {
         syncedStacks.put(buffer.m_130242_(), buffer.m_130267_());
      }

      return new KnowledgeSyncInputsAndLocksPKT(syncedStacks, (IKnowledgeProvider.TargetUpdateType)buffer.m_130066_(IKnowledgeProvider.TargetUpdateType.class));
   }

   public Map stacksToSync() {
      return this.stacksToSync;
   }

   public IKnowledgeProvider.TargetUpdateType updateTargets() {
      return this.updateTargets;
   }
}
