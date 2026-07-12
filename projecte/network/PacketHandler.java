package moze_intel.projecte.network;

import io.netty.buffer.Unpooled;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import moze_intel.projecte.PECore;
import moze_intel.projecte.emc.EMCMappingHandler;
import moze_intel.projecte.emc.FuelMapper;
import moze_intel.projecte.network.packets.IPEPacket;
import moze_intel.projecte.network.packets.to_client.CooldownResetPKT;
import moze_intel.projecte.network.packets.to_client.SyncBagDataPKT;
import moze_intel.projecte.network.packets.to_client.SyncEmcPKT;
import moze_intel.projecte.network.packets.to_client.SyncFuelMapperPKT;
import moze_intel.projecte.network.packets.to_client.UpdateCondenserLockPKT;
import moze_intel.projecte.network.packets.to_client.UpdateWindowIntPKT;
import moze_intel.projecte.network.packets.to_client.UpdateWindowLongPKT;
import moze_intel.projecte.network.packets.to_client.knowledge.KnowledgeClearPKT;
import moze_intel.projecte.network.packets.to_client.knowledge.KnowledgeSyncChangePKT;
import moze_intel.projecte.network.packets.to_client.knowledge.KnowledgeSyncEmcPKT;
import moze_intel.projecte.network.packets.to_client.knowledge.KnowledgeSyncInputsAndLocksPKT;
import moze_intel.projecte.network.packets.to_client.knowledge.KnowledgeSyncPKT;
import moze_intel.projecte.network.packets.to_client.knowledge.UpdateTransmutationTargetsPkt;
import moze_intel.projecte.network.packets.to_server.KeyPressPKT;
import moze_intel.projecte.network.packets.to_server.LeftClickArchangelPKT;
import moze_intel.projecte.network.packets.to_server.SearchUpdatePKT;
import moze_intel.projecte.network.packets.to_server.UpdateGemModePKT;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.NetworkRegistry.ChannelBuilder;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

public final class PacketHandler {
   private static final String PROTOCOL_VERSION = Integer.toString(4);
   private static final SimpleChannel HANDLER;
   private static int index;

   public static void register() {
      registerClientToServer(KeyPressPKT.class, KeyPressPKT::decode);
      registerClientToServer(LeftClickArchangelPKT.class, LeftClickArchangelPKT::decode);
      registerClientToServer(SearchUpdatePKT.class, SearchUpdatePKT::decode);
      registerClientToServer(UpdateGemModePKT.class, UpdateGemModePKT::decode);
      registerServerToClient(CooldownResetPKT.class, CooldownResetPKT::decode);
      registerServerToClient(KnowledgeClearPKT.class, KnowledgeClearPKT::decode);
      registerServerToClient(KnowledgeSyncPKT.class, KnowledgeSyncPKT::decode);
      registerServerToClient(KnowledgeSyncEmcPKT.class, KnowledgeSyncEmcPKT::decode);
      registerServerToClient(KnowledgeSyncInputsAndLocksPKT.class, KnowledgeSyncInputsAndLocksPKT::decode);
      registerServerToClient(KnowledgeSyncChangePKT.class, KnowledgeSyncChangePKT::decode);
      registerServerToClient(SyncBagDataPKT.class, SyncBagDataPKT::decode);
      registerServerToClient(SyncEmcPKT.class, SyncEmcPKT::decode);
      registerServerToClient(SyncFuelMapperPKT.class, SyncFuelMapperPKT::decode);
      registerServerToClient(UpdateCondenserLockPKT.class, UpdateCondenserLockPKT::decode);
      registerServerToClient(UpdateTransmutationTargetsPkt.class, UpdateTransmutationTargetsPkt::decode);
      registerServerToClient(UpdateWindowIntPKT.class, UpdateWindowIntPKT::decode);
      registerServerToClient(UpdateWindowLongPKT.class, UpdateWindowLongPKT::decode);
   }

   private static void registerClientToServer(Class type, Function decoder) {
      registerMessage(type, decoder, NetworkDirection.PLAY_TO_SERVER);
   }

   private static void registerServerToClient(Class type, Function decoder) {
      registerMessage(type, decoder, NetworkDirection.PLAY_TO_CLIENT);
   }

   private static void registerMessage(Class type, Function decoder, NetworkDirection networkDirection) {
      HANDLER.registerMessage(index++, type, IPEPacket::encode, decoder, IPEPacket::handle, Optional.of(networkDirection));
   }

   private static boolean isLocal(ServerPlayer player) {
      return player.f_8924_.m_7779_(player.m_36316_());
   }

   public static void sendNonLocal(IPEPacket msg, ServerPlayer player) {
      if (!isLocal(player)) {
         sendTo(msg, player);
      }

   }

   private static void sendFragmentedEmcPacket(ServerPlayer player, SyncEmcPKT pkt, SyncFuelMapperPKT fuelPkt) {
      if (!isLocal(player)) {
         sendTo(pkt, player);
         sendTo(fuelPkt, player);
      }

   }

   public static void sendFragmentedEmcPacket(ServerPlayer player) {
      sendFragmentedEmcPacket(player, new SyncEmcPKT(serializeEmcData()), FuelMapper.getSyncPacket());
   }

   public static void sendFragmentedEmcPacketToAll() {
      if (ServerLifecycleHooks.getCurrentServer() != null) {
         SyncEmcPKT pkt = new SyncEmcPKT(serializeEmcData());
         SyncFuelMapperPKT fuelPkt = FuelMapper.getSyncPacket();
         Iterator var2 = ServerLifecycleHooks.getCurrentServer().m_6846_().m_11314_().iterator();

         while(var2.hasNext()) {
            ServerPlayer player = (ServerPlayer)var2.next();
            sendFragmentedEmcPacket(player, pkt, fuelPkt);
         }
      }

   }

   private static SyncEmcPKT.EmcPKTInfo[] serializeEmcData() {
      SyncEmcPKT.EmcPKTInfo[] data = EMCMappingHandler.createPacketData();
      FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
      int index = buf.writerIndex();
      (new SyncEmcPKT(data)).encode(buf);
      PECore.debugLog("EMC data size: {} bytes", buf.writerIndex() - index);
      buf.release();
      return data;
   }

   public static void sendToServer(IPEPacket msg) {
      HANDLER.sendToServer(msg);
   }

   public static void sendTo(IPEPacket msg, ServerPlayer player) {
      if (!(player instanceof FakePlayer)) {
         HANDLER.send(PacketDistributor.PLAYER.with(() -> {
            return player;
         }), msg);
      }

   }

   static {
      NetworkRegistry.ChannelBuilder var10000 = ChannelBuilder.named(PECore.rl("main_channel"));
      String var10001 = PROTOCOL_VERSION;
      Objects.requireNonNull(var10001);
      var10000 = var10000.clientAcceptedVersions(var10001::equals);
      var10001 = PROTOCOL_VERSION;
      Objects.requireNonNull(var10001);
      HANDLER = var10000.serverAcceptedVersions(var10001::equals).networkProtocolVersion(() -> {
         return PROTOCOL_VERSION;
      }).simpleChannel();
   }
}
