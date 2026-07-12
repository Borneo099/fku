package moze_intel.projecte.impl;

import com.google.common.base.Preconditions;
import java.util.Iterator;
import java.util.UUID;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.proxy.ITransmutationProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

public class TransmutationProxyImpl implements ITransmutationProxy {
   public @NotNull IKnowledgeProvider getKnowledgeProviderFor(@NotNull UUID playerUUID) {
      if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
         return (IKnowledgeProvider)DistExecutor.unsafeRunForDist(() -> {
            return () -> {
               Preconditions.checkState(Minecraft.m_91087_().f_91074_ != null, "Client player doesn't exist!");
               return (IKnowledgeProvider)Minecraft.m_91087_().f_91074_.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).orElseThrow(NullPointerException::new);
            };
         }, () -> {
            return () -> {
               throw new RuntimeException("unreachable");
            };
         });
      } else {
         Preconditions.checkNotNull(playerUUID);
         Preconditions.checkNotNull(ServerLifecycleHooks.getCurrentServer(), "Server must be running to query knowledge!");
         Player player = this.findOnlinePlayer(playerUUID);
         return player != null ? (IKnowledgeProvider)player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).orElseThrow(NullPointerException::new) : TransmutationOffline.forPlayer(playerUUID);
      }
   }

   private Player findOnlinePlayer(UUID playerUUID) {
      Iterator var2 = ServerLifecycleHooks.getCurrentServer().m_6846_().m_11314_().iterator();

      Player player;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         player = (Player)var2.next();
      } while(!player.m_20148_().equals(playerUUID));

      return player;
   }
}
