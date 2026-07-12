package moze_intel.projecte.network.packets.to_server;

import moze_intel.projecte.gameObjs.items.GemEternalDensity;
import moze_intel.projecte.network.packets.IPEPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public record UpdateGemModePKT(boolean mode) implements IPEPacket {
   public UpdateGemModePKT(boolean mode) {
      this.mode = mode;
   }

   public void handle(NetworkEvent.Context context) {
      Player player = context.getSender();
      if (player != null) {
         ItemStack stack = player.m_21205_();
         if (stack.m_41619_()) {
            stack = player.m_21206_();
         }

         if (!stack.m_41619_() && stack.m_41720_() instanceof GemEternalDensity) {
            stack.m_41784_().m_128379_("Whitelist", this.mode);
         }
      }

   }

   public void encode(FriendlyByteBuf buffer) {
      buffer.writeBoolean(this.mode);
   }

   public static UpdateGemModePKT decode(FriendlyByteBuf buffer) {
      return new UpdateGemModePKT(buffer.readBoolean());
   }

   public boolean mode() {
      return this.mode;
   }
}
