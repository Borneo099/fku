package lexis.Client.Commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import lexis.Hack.events.PacketCancelManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ReplayPacketCommand {
   private static final List ALL_PACKET_CLASS_NAMES = Arrays.asList("ServerboundSwingPacket", "ServerboundInteractPacket", "ServerboundUseItemPacket", "ServerboundUseItemOnPacket", "ServerboundMovePlayerPacket", "ServerboundPlayerActionPacket", "ServerboundChatPacket", "ServerboundContainerClickPacket", "ServerboundContainerClosePacket", "ServerboundSetCarriedItemPacket", "ServerboundPlayerAbilitiesPacket", "ServerboundClientInformationPacket", "ServerboundCommandSuggestionPacket", "ServerboundEditBookPacket", "ServerboundKeepAlivePacket", "ServerboundResourcePackPacket", "ServerboundSelectTradePacket", "ServerboundSetCommandBlockPacket", "ServerboundSetCreativeModeSlotPacket", "ServerboundSetJigsawBlockPacket", "ServerboundSetStructureBlockPacket", "ServerboundSignUpdatePacket", "ServerboundRecipeBookChangeSettingsPacket", "ServerboundRecipeBookSeenRecipePacket", "ServerboundSeenAdvancementsPacket", "ServerboundTeleportToEntityPacket", "ServerboundPlayerInputPacket", "ServerboundPickItemPacket", "ServerboundRenameItemPacket", "ServerboundContainerButtonClickPacket", "ServerboundSetBeaconPacket", "ServerboundBlockEntityTagQuery", "ServerboundEntityTagQuery", "ServerboundAcceptTeleportationPacket", "ServerboundClientCommandPacket", "ServerboundPongPacket", "ClientboundChatPacket", "ClientboundSystemChatPacket", "ClientboundDisconnectPacket", "ClientboundLoginPacket", "ClientboundKeepAlivePacket", "ClientboundPlayerInfoUpdatePacket", "ClientboundPlayerPositionPacket", "ClientboundSetTimePacket", "ClientboundEntityEventPacket", "ClientboundSetEntityDataPacket", "ClientboundSetEntityMotionPacket", "ClientboundTeleportEntityPacket", "ClientboundSetEquipmentPacket", "ClientboundUpdateMobEffectPacket", "ClientboundRemoveMobEffectPacket", "ClientboundSetHealthPacket", "ClientboundRespawnPacket", "ClientboundPlayerAbilitiesPacket", "ClientboundSetCarriedItemPacket", "ClientboundContainerSetContentPacket", "ClientboundContainerSetDataPacket", "ClientboundContainerSetSlotPacket", "ClientboundOpenScreenPacket", "ClientboundContainerClosePacket", "ClientboundBlockUpdatePacket", "ClientboundSectionBlocksUpdatePacket", "ClientboundLevelChunkWithLightPacket", "ClientboundForgetLevelChunkPacket", "ClientboundAddEntityPacket", "ClientboundAddExperienceOrbPacket", "ClientboundRemoveEntitiesPacket", "ClientboundTakeItemEntityPacket", "ClientboundExplodePacket", "ClientboundSoundPacket", "ClientboundLevelParticlesPacket", "ClientboundGameEventPacket", "ClientboundSetScorePacket", "ClientboundSetObjectivePacket", "ClientboundSetDisplayObjectivePacket", "ClientboundSetPlayerTeamPacket", "ClientboundSetTitleTextPacket", "ClientboundTabListPacket", "ClientboundInitializeBorderPacket", "ClientboundMapItemDataPacket", "ClientboundUpdateAdvancementsPacket", "ClientboundCommandsPacket", "ClientboundPlayerLookAtPacket", "ClientboundRecipePacket", "ClientboundUpdateTagsPacket");
   private static final SuggestionProvider PACKET_SUGGESTIONS = (ctx, builder) -> {
      Iterator var2 = ALL_PACKET_CLASS_NAMES.iterator();

      while(var2.hasNext()) {
         String name = (String)var2.next();
         builder.suggest(name);
      }

      return builder.buildFuture();
   };

   @SubscribeEvent
   public static void onRegisterCommands(RegisterClientCommandsEvent event) {
      CommandDispatcher dispatcher = event.getDispatcher();
      dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_("lexis").then(Commands.m_82127_("client").then(Commands.m_82127_("ReplayPacket").then(Commands.m_82129_("packetName", StringArgumentType.word()).suggests(PACKET_SUGGESTIONS).executes(ReplayPacketCommand::setCancelPacket)))));
   }

   private static int setCancelPacket(CommandContext ctx) {
      String className = StringArgumentType.getString(ctx, "packetName");
      PacketCancelManager.setCancelPacket(className);
      ((CommandSourceStack)ctx.getSource()).m_288197_(() -> {
         return Component.m_237113_("§d[§6Lexis§d] §f已重新修复发包： " + className);
      }, false);
      return 1;
   }
}
