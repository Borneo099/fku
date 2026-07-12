package moze_intel.projecte.api.event;

import moze_intel.projecte.api.ItemInfo;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.NotNull;

@Cancelable
public class PlayerAttemptLearnEvent extends Event {
   private final Player player;
   private final ItemInfo sourceInfo;
   private final ItemInfo reducedInfo;

   public PlayerAttemptLearnEvent(@NotNull Player player, @NotNull ItemInfo sourceInfo, @NotNull ItemInfo reducedInfo) {
      this.player = player;
      this.sourceInfo = sourceInfo;
      this.reducedInfo = reducedInfo;
   }

   public @NotNull Player getPlayer() {
      return this.player;
   }

   public @NotNull ItemInfo getSourceInfo() {
      return this.sourceInfo;
   }

   public @NotNull ItemInfo getReducedInfo() {
      return this.reducedInfo;
   }
}
