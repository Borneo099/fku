package moze_intel.projecte.api.event;

import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.NotNull;

public class PlayerKnowledgeChangeEvent extends Event {
   private final UUID playerUUID;

   public PlayerKnowledgeChangeEvent(@NotNull Player player) {
      this(player.m_20148_());
   }

   public PlayerKnowledgeChangeEvent(@NotNull UUID playerUUID) {
      this.playerUUID = playerUUID;
   }

   public @NotNull UUID getPlayerUUID() {
      return this.playerUUID;
   }
}
