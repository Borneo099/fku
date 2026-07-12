package lexis.Server;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class PlayerMessageInterceptor {
   private static final Set silentPlayers = new HashSet();

   public static void addSilentPlayer(UUID uuid) {
      silentPlayers.add(uuid);
   }

   public static void removeSilentPlayer(UUID uuid) {
      silentPlayers.remove(uuid);
   }

   public static boolean isSilentPlayer(UUID uuid) {
      return silentPlayers.contains(uuid);
   }

   public static void clearSilentPlayers() {
      silentPlayers.clear();
   }

   @SubscribeEvent
   public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
   }

   @SubscribeEvent
   public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
      Player var2 = event.getEntity();
      if (var2 instanceof ServerPlayer player) {
         removeSilentPlayer(player.m_20148_());
      }

   }
}
