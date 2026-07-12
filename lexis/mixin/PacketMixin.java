package lexis.mixin;

import lexis.Server.AntiPacket.PacketBlocker;
import lexis.mixin.accessor.ServerGamePacketAccessor;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ServerGamePacketListenerImpl.class})
public class PacketMixin {
   @Shadow
   public ServerPlayer f_9743_;

   @Inject(
      method = {"handleChat"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void onHandleChat(ServerboundChatPacket packet, CallbackInfo ci) {
      if (!PacketBlocker.checkPacket(((ServerGamePacketAccessor)this).getPlayer())) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"handleChatCommand"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void onHandleChatCommand(ServerboundChatCommandPacket packet, CallbackInfo ci) {
      if (!PacketBlocker.checkPacket(((ServerGamePacketAccessor)this).getPlayer())) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"handleMovePlayer"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void onHandleMovePlayer(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
      if (!PacketBlocker.checkPacket(((ServerGamePacketAccessor)this).getPlayer())) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"handleUseItem"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void onHandleUseItem(ServerboundUseItemPacket packet, CallbackInfo ci) {
      if (!PacketBlocker.checkPacket(((ServerGamePacketAccessor)this).getPlayer())) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"handleUseItemOn"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void onHandleUseItemOn(ServerboundUseItemOnPacket packet, CallbackInfo ci) {
      if (!PacketBlocker.checkPacket(((ServerGamePacketAccessor)this).getPlayer())) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"handleContainerClick"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void onHandleContainerClick(ServerboundContainerClickPacket packet, CallbackInfo ci) {
      if (!PacketBlocker.checkPacket(((ServerGamePacketAccessor)this).getPlayer())) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"handlePlayerAction"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void onHandlePlayerAction(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
      if (!PacketBlocker.checkPacket(((ServerGamePacketAccessor)this).getPlayer())) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"handleAnimate"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void onHandleAnimate(ServerboundSwingPacket packet, CallbackInfo ci) {
      if (!PacketBlocker.checkPacket(((ServerGamePacketAccessor)this).getPlayer())) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"handleSetCreativeModeSlot"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void onHandleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket packet, CallbackInfo ci) {
      if (!PacketBlocker.checkPacket(((ServerGamePacketAccessor)this).getPlayer())) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"handleEditBook"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void onHandleEditBook(ServerboundEditBookPacket packet, CallbackInfo ci) {
      if (!PacketBlocker.checkPacket(((ServerGamePacketAccessor)this).getPlayer())) {
         ci.cancel();
      }

   }
}
