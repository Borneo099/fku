package moze_intel.projecte.network.packets.to_client;

import moze_intel.projecte.network.packets.IPEPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class CooldownResetPKT implements IPEPacket {
   public void handle(NetworkEvent.Context context) {
      if (Minecraft.m_91087_().f_91074_ != null) {
         Minecraft.m_91087_().f_91074_.m_36334_();
      }

   }

   public void encode(FriendlyByteBuf buffer) {
   }

   public static CooldownResetPKT decode(FriendlyByteBuf buffer) {
      return new CooldownResetPKT();
   }
}
