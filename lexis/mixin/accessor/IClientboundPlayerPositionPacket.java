package lexis.mixin.accessor;

import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({ClientboundPlayerPositionPacket.class})
public interface IClientboundPlayerPositionPacket {
   @Mutable
   @Accessor("yRot")
   void setYaw(float yaw);

   @Mutable
   @Accessor("xRot")
   void setPitch(float pitch);
}
