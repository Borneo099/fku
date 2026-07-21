package fku.org.example.fku.mixin;

import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ServerboundInteractPacket.class})
public interface ServerboundInteractPacketAccessor {
    @Accessor(value="entityId")
    public int getEntityId();
}

