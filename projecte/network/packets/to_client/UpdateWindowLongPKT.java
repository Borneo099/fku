package moze_intel.projecte.network.packets.to_client;

import moze_intel.projecte.gameObjs.container.PEContainer;
import moze_intel.projecte.network.packets.IPEPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public record UpdateWindowLongPKT(short windowId, short propId, long propVal) implements IPEPacket {
   public UpdateWindowLongPKT(short windowId, short propId, long propVal) {
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
               container.updateProgressBarLong(this.propId, this.propVal);
            }
         }
      }

   }

   public void encode(FriendlyByteBuf buffer) {
      buffer.writeShort(this.windowId);
      buffer.writeShort(this.propId);
      buffer.writeLong(this.propVal);
   }

   public static UpdateWindowLongPKT decode(FriendlyByteBuf buffer) {
      return new UpdateWindowLongPKT(buffer.readShort(), buffer.readShort(), buffer.readLong());
   }

   public short windowId() {
      return this.windowId;
   }

   public short propId() {
      return this.propId;
   }

   public long propVal() {
      return this.propVal;
   }
}
