package moze_intel.projecte.network.packets.to_server;

import moze_intel.projecte.gameObjs.items.rings.ArchangelSmite;
import moze_intel.projecte.network.packets.IPEPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class LeftClickArchangelPKT implements IPEPacket {
   public void handle(NetworkEvent.Context context) {
      Player player = context.getSender();
      if (player != null) {
         ItemStack main = player.m_21205_();
         if (!main.m_41619_()) {
            Item var5 = main.m_41720_();
            if (var5 instanceof ArchangelSmite) {
               ArchangelSmite archangelSmite = (ArchangelSmite)var5;
               archangelSmite.fireVolley(main, player);
            }
         }
      }

   }

   public void encode(FriendlyByteBuf buffer) {
   }

   public static LeftClickArchangelPKT decode(FriendlyByteBuf buffer) {
      return new LeftClickArchangelPKT();
   }
}
