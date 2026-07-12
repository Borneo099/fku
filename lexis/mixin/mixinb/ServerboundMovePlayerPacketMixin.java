package lexis.mixin.mixinb;

import lexis.mixinterface.IPlayerMoveC2SPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({ServerboundMovePlayerPacket.class})
public abstract class ServerboundMovePlayerPacketMixin implements IPlayerMoveC2SPacket {
   @Unique
   private int lexisTag;

   public void lexis$setTag(int tag) {
      this.lexisTag = tag;
   }

   public int lexis$getTag() {
      return this.lexisTag;
   }
}
