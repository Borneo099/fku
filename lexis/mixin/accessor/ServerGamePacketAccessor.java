package lexis.mixin.accessor;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({ServerGamePacketListenerImpl.class})
public interface ServerGamePacketAccessor {
   @Accessor("player")
   ServerPlayer getPlayer();
}
