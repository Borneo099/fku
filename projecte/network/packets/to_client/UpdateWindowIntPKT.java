package moze_intel.projecte.network.packets.to_client;

import moze_intel.projecte.gameObjs.container.PEContainer;
import moze_intel.projecte.network.packets.IPEPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public record UpdateWindowIntPKT(short windowId, short propId, int propVal) implements IPEPacket {
   public UpdateWindowIntPKT(short windowId, short propId, int propVal) {
      this.windowId = windowId;
      this.propId = propId;
      this.propVal = propVal;
   }

   public void handle(NetworkEvent.Context context) {
      LocalPlayer player = Minecraft.m_91087_().f_91074_;
      if (player != null) {
         AbstractContainerMenu var4 = player.f_36096_;
         if (var4 instanceof PEContainer) {
            PEContainer container = (PEContainer)var4;
            if (player.f_36096_.f_38840_ == this.windowId) {
               container.updateProgressBarInt(this.propId, this.propVal);
            }
         }
      }

   }

   public void encode(FriendlyByteBuf buffer) {
      buffer.writeShort(this.windowId);
      buffer.writeShort(this.propId);
      buffer.m_130130_(this.propVal);
   }

   public static UpdateWindowIntPKT decode(FriendlyByteBuf buffer) {
      return new UpdateWindowIntPKT(buffer.readShort(), buffer.readShort(), buffer.m_130242_());
   }

   public short windowId() {
      return this.windowId;
   }

   public short propId() {
      return this.propId;
   }

   public int propVal() {
      return this.propVal;
   }
}
