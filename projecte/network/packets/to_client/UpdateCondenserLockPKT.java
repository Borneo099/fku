package moze_intel.projecte.network.packets.to_client;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.gameObjs.container.CondenserContainer;
import moze_intel.projecte.network.packets.IPEPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public record UpdateCondenserLockPKT(short windowId, @Nullable ItemInfo lockInfo) implements IPEPacket {
   public UpdateCondenserLockPKT(short windowId, @Nullable ItemInfo lockInfo) {
      this.windowId = windowId;
      this.lockInfo = lockInfo;
   }

   public void handle(NetworkEvent.Context context) {
      LocalPlayer player = Minecraft.m_91087_().f_91074_;
      if (player != null) {
         AbstractContainerMenu var4 = player.f_36096_;
         if (var4 instanceof CondenserContainer) {
            CondenserContainer container = (CondenserContainer)var4;
            if (player.f_36096_.f_38840_ == this.windowId) {
               container.updateLockInfo(this.lockInfo);
            }
         }
      }

   }

   public void encode(FriendlyByteBuf buffer) {
      buffer.writeShort(this.windowId);
      if (this.lockInfo == null) {
         buffer.writeBoolean(false);
      } else {
         buffer.writeBoolean(true);
         buffer.writeRegistryIdUnsafe(ForgeRegistries.ITEMS, this.lockInfo.getItem());
         buffer.m_130079_(this.lockInfo.getNBT());
      }

   }

   public static UpdateCondenserLockPKT decode(FriendlyByteBuf buffer) {
      short windowId = buffer.readShort();
      ItemInfo lockInfo = null;
      if (buffer.readBoolean()) {
         lockInfo = ItemInfo.fromItem((ItemLike)buffer.readRegistryIdUnsafe(ForgeRegistries.ITEMS), buffer.m_130260_());
      }

      return new UpdateCondenserLockPKT(windowId, lockInfo);
   }

   public short windowId() {
      return this.windowId;
   }

   public @Nullable ItemInfo lockInfo() {
      return this.lockInfo;
   }
}
