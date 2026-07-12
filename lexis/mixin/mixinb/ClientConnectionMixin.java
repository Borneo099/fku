package lexis.mixin.mixinb;

import com.google.gson.Gson;
import io.netty.channel.ChannelHandlerContext;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Misc.PacketCancellerHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAddExperienceOrbPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundBlockEntityTagQuery;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundEntityTagQuery;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundPongPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookSeenRecipePacket;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.network.protocol.game.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSetJigsawBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Connection.class})
public class ClientConnectionMixin {
   private static Set cancelledS2C = new HashSet();
   private static Set cancelledC2S = new HashSet();

   private static void loadCancelledPackets() {
      try {
         cancelledS2C.clear();
         cancelledC2S.clear();
         File configFile = new File("C:/karucn/Lexis/config/hack/packet_canceller.json");
         if (!configFile.exists()) {
            return;
         }

         FileReader reader = new FileReader(configFile);
         Map data = (Map)(new Gson()).fromJson(reader, Map.class);
         reader.close();
         if (data != null) {
            List c2s;
            if (data.containsKey("s2c")) {
               c2s = (List)data.get("s2c");
               cancelledS2C.addAll(c2s);
            }

            if (data.containsKey("c2s")) {
               c2s = (List)data.get("c2s");
               cancelledC2S.addAll(c2s);
            }
         }
      } catch (Exception var4) {
         var4.printStackTrace();
      }

   }

   private static boolean isHackEnabled() {
      Iterator var0 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var0.hasNext()) {
            return false;
         }

         hack = (Hack)var0.next();
      } while(!(hack instanceof PacketCancellerHack) || !hack.isEnabled());

      return true;
   }

   private static String getPacketName(Packet packet) {
      if (packet instanceof ClientboundLoginPacket) {
         return "S2C:Login";
      } else if (packet instanceof ClientboundDisconnectPacket) {
         return "S2C:Disconnect";
      } else if (packet instanceof ClientboundKeepAlivePacket) {
         return "S2C:KeepAlive";
      } else if (packet instanceof ClientboundPlayerInfoUpdatePacket) {
         return "S2C:PlayerInfo";
      } else if (packet instanceof ClientboundPlayerPositionPacket) {
         return "S2C:PlayerPosLook";
      } else if (packet instanceof ClientboundSystemChatPacket) {
         return "S2C:ChatMessage";
      } else if (packet instanceof ClientboundSetTimePacket) {
         return "S2C:TimeUpdate";
      } else if (packet instanceof ClientboundEntityEventPacket) {
         return "S2C:EntityStatus";
      } else if (packet instanceof ClientboundSetEntityDataPacket) {
         return "S2C:EntityMetadata";
      } else if (packet instanceof ClientboundSetEntityMotionPacket) {
         return "S2C:EntityVelocity";
      } else if (packet instanceof ClientboundTeleportEntityPacket) {
         return "S2C:EntityTeleport";
      } else if (packet instanceof ClientboundSetEquipmentPacket) {
         return "S2C:EntityEquipment";
      } else if (packet instanceof ClientboundUpdateMobEffectPacket) {
         return "S2C:EntityEffect";
      } else if (packet instanceof ClientboundRemoveMobEffectPacket) {
         return "S2C:RemoveEntityEffect";
      } else if (packet instanceof ClientboundSetHealthPacket) {
         return "S2C:SetHealth";
      } else if (packet instanceof ClientboundRespawnPacket) {
         return "S2C:Respawn";
      } else if (packet instanceof ClientboundPlayerAbilitiesPacket) {
         return "S2C:PlayerAbilities";
      } else if (packet instanceof ClientboundSetCarriedItemPacket) {
         return "S2C:HeldItemChange";
      } else if (packet instanceof ClientboundContainerSetContentPacket) {
         return "S2C:WindowItems";
      } else if (packet instanceof ClientboundContainerSetDataPacket) {
         return "S2C:WindowProperty";
      } else if (packet instanceof ClientboundContainerSetSlotPacket) {
         return "S2C:SetSlot";
      } else if (packet instanceof ClientboundOpenScreenPacket) {
         return "S2C:OpenWindow";
      } else if (packet instanceof ClientboundContainerClosePacket) {
         return "S2C:CloseWindow";
      } else if (packet instanceof ClientboundBlockUpdatePacket) {
         return "S2C:BlockUpdate";
      } else if (packet instanceof ClientboundSectionBlocksUpdatePacket) {
         return "S2C:MultiBlockChange";
      } else if (packet instanceof ClientboundLevelChunkWithLightPacket) {
         return "S2C:ChunkData";
      } else if (packet instanceof ClientboundForgetLevelChunkPacket) {
         return "S2C:UnloadChunk";
      } else if (packet instanceof ClientboundAddEntityPacket) {
         return "S2C:SpawnEntity";
      } else if (packet instanceof ClientboundAddExperienceOrbPacket) {
         return "S2C:SpawnExperienceOrb";
      } else if (packet instanceof ClientboundAddEntityPacket) {
         return "S2C:SpawnLivingEntity";
      } else if (packet instanceof ClientboundAddEntityPacket) {
         return "S2C:SpawnPainting";
      } else if (packet instanceof ClientboundRemoveEntitiesPacket) {
         return "S2C:DestroyEntities";
      } else if (packet instanceof ClientboundTakeItemEntityPacket) {
         return "S2C:CollectItem";
      } else if (packet instanceof ClientboundExplodePacket) {
         return "S2C:Explosion";
      } else if (packet instanceof ClientboundSoundPacket) {
         return "S2C:SoundEffect";
      } else if (packet instanceof ClientboundLevelParticlesPacket) {
         return "S2C:Particle";
      } else if (packet instanceof ClientboundGameEventPacket) {
         return "S2C:GameStateChange";
      } else if (packet instanceof ClientboundSetScorePacket) {
         return "S2C:UpdateScore";
      } else if (packet instanceof ClientboundSetObjectivePacket) {
         return "S2C:UpdateObjective";
      } else if (packet instanceof ClientboundSetDisplayObjectivePacket) {
         return "S2C:UpdateObjective";
      } else if (packet instanceof ClientboundSetPlayerTeamPacket) {
         return "S2C:UpdateTeams";
      } else if (packet instanceof ClientboundSetTitleTextPacket) {
         return "S2C:Title";
      } else if (packet instanceof ClientboundTabListPacket) {
         return "S2C:TabList";
      } else if (packet instanceof ClientboundInitializeBorderPacket) {
         return "S2C:WorldBorder";
      } else if (packet instanceof ClientboundMapItemDataPacket) {
         return "S2C:MapData";
      } else if (packet instanceof ClientboundUpdateAdvancementsPacket) {
         return "S2C:Advancements";
      } else if (packet instanceof ClientboundCommandsPacket) {
         return "S2C:CommandTree";
      } else if (packet instanceof ClientboundPlayerLookAtPacket) {
         return "S2C:LookAt";
      } else if (packet instanceof ClientboundRecipePacket) {
         return "S2C:SyncRecipeBook";
      } else if (packet instanceof ClientboundUpdateTagsPacket) {
         return "S2C:Tags";
      } else if (packet instanceof ServerboundHelloPacket) {
         return "C2S:Login";
      } else if (packet instanceof ServerboundKeepAlivePacket) {
         return "C2S:KeepAlive";
      } else if (packet instanceof ServerboundChatPacket) {
         return "C2S:ChatMessage";
      } else if (packet instanceof ServerboundPlayerActionPacket) {
         return "C2S:PlayerAction";
      } else if (packet instanceof ServerboundPlayerInputPacket) {
         return "C2S:PlayerInput";
      } else if (packet instanceof ServerboundMovePlayerPacket) {
         if (packet instanceof ServerboundMovePlayerPacket.Pos) {
            return "C2S:PlayerPosition";
         } else if (packet instanceof ServerboundMovePlayerPacket.Rot) {
            return "C2S:PlayerRotation";
         } else {
            return packet instanceof ServerboundMovePlayerPacket.PosRot ? "C2S:PlayerPositionRotation" : "C2S:PlayerMovement";
         }
      } else if (packet instanceof ServerboundPlayerAbilitiesPacket) {
         return "C2S:PlayerAbilities";
      } else if (packet instanceof ServerboundSetCarriedItemPacket) {
         return "C2S:HeldItemChange";
      } else if (packet instanceof ServerboundSwingPacket) {
         return "C2S:Animation";
      } else if (packet instanceof ServerboundInteractPacket) {
         return "C2S:UseEntity";
      } else if (packet instanceof ServerboundUseItemPacket) {
         return "C2S:UseItem";
      } else if (packet instanceof ServerboundUseItemOnPacket) {
         return "C2S:UseItemOn";
      } else if (packet instanceof ServerboundContainerClickPacket) {
         return "C2S:ClickWindow";
      } else if (packet instanceof ServerboundContainerClosePacket) {
         return "C2S:CloseWindow";
      } else if (packet instanceof ServerboundSetCreativeModeSlotPacket) {
         return "C2S:CreativeInventoryAction";
      } else if (packet instanceof ServerboundContainerButtonClickPacket) {
         return "C2S:EnchantItem";
      } else if (packet instanceof ServerboundPickItemPacket) {
         return "C2S:PickItem";
      } else if (packet instanceof ServerboundSelectTradePacket) {
         return "C2S:SelectTrade";
      } else if (packet instanceof ServerboundRenameItemPacket) {
         return "C2S:RenameItem";
      } else if (packet instanceof ServerboundSetCommandBlockPacket) {
         return "C2S:UpdateCommandBlock";
      } else if (packet instanceof ServerboundSignUpdatePacket) {
         return "C2S:UpdateSign";
      } else if (packet instanceof ServerboundSetStructureBlockPacket) {
         return "C2S:UpdateStructureBlock";
      } else if (packet instanceof ServerboundSetJigsawBlockPacket) {
         return "C2S:UpdateJigsawBlock";
      } else if (packet instanceof ServerboundSetBeaconPacket) {
         return "C2S:UpdateBeacon";
      } else if (packet instanceof ServerboundAcceptTeleportationPacket) {
         return "C2S:TeleportConfirm";
      } else if (packet instanceof ServerboundBlockEntityTagQuery) {
         return "C2S:QueryBlockNBT";
      } else if (packet instanceof ServerboundEntityTagQuery) {
         return "C2S:QueryEntityNBT";
      } else if (packet instanceof ServerboundEditBookPacket) {
         return "C2S:EditBook";
      } else if (packet instanceof ServerboundRecipeBookSeenRecipePacket) {
         return "C2S:RecipeBookData";
      } else if (packet instanceof ServerboundRecipeBookChangeSettingsPacket) {
         return "C2S:RecipeBookData";
      } else if (packet instanceof ServerboundSeenAdvancementsPacket) {
         return "C2S:AdvancementTab";
      } else if (packet instanceof ServerboundCommandSuggestionPacket) {
         return "C2S:CommandSuggestion";
      } else if (packet instanceof ServerboundClientInformationPacket) {
         return "C2S:ClientSettings";
      } else if (packet instanceof ServerboundClientCommandPacket) {
         return "C2S:ClientStatus";
      } else if (packet instanceof ServerboundResourcePackPacket) {
         return "C2S:ResourcePack";
      } else {
         return packet instanceof ServerboundPongPacket ? "C2S:Pong" : packet.getClass().getSimpleName();
      }
   }

   @Inject(
      method = {"send(Lnet/minecraft/network/protocol/Packet;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onSendPacket(Packet packet, CallbackInfo ci) {
      if (isHackEnabled()) {
         loadCancelledPackets();
         String packetName = getPacketName(packet);
         if (cancelledC2S.contains(packetName)) {
            ci.cancel();
         }

      }
   }

   @Inject(
      method = {"channelRead0*"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onChannelRead(ChannelHandlerContext context, Packet packet, CallbackInfo ci) {
      if (isHackEnabled()) {
         loadCancelledPackets();
         String packetName = getPacketName(packet);
         if (cancelledS2C.contains(packetName)) {
            ci.cancel();
         }

      }
   }
}
