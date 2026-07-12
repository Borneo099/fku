package lexis.mixin.mixinc;

import lexis.Hack.Hacks.Combat.AutoRevengeHack;
import lexis.Hack.Utils.FriendsManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPacketListener.class})
public class ClientPacketListenerMixin {
   @Inject(
      method = {"handleDamageEvent"},
      at = {@At("HEAD")}
   )
   private void onDamageEvent(ClientboundDamageEventPacket packet, CallbackInfo ci) {
      Minecraft mc = Minecraft.m_91087_();
      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         if (AutoRevengeHack.isActive()) {
            Entity entity = mc.f_91073_.m_6815_(packet.f_268504_());
            if (entity instanceof Player) {
               Player player = (Player)entity;
               if (player.m_7578_()) {
                  DamageSource source = packet.m_269591_(mc.f_91073_);
                  Entity attackerEntity = source.m_7639_();
                  if (!(attackerEntity instanceof EndCrystal)) {
                     Entity directEntity = source.m_7640_();
                     if (!(directEntity instanceof EndCrystal)) {
                        if (!source.m_269415_().f_268677_().contains("explosion")) {
                           if (attackerEntity instanceof LivingEntity) {
                              LivingEntity attacker = (LivingEntity)attackerEntity;
                              if (attacker instanceof Player && FriendsManager.getInstance().isFriend((Player)attacker)) {
                                 return;
                              }

                              long now = System.currentTimeMillis();
                              if (now - AutoRevengeHack.lastAttack < AutoRevengeHack.getCooldown()) {
                                 return;
                              }

                              AutoRevengeHack.lastAttack = now;
                              ServerboundInteractPacket attackPacket = ServerboundInteractPacket.m_179605_(attacker, mc.f_91074_.m_6144_());
                              mc.m_91403_().m_104955_(attackPacket);
                           }

                        }
                     }
                  }
               }
            }
         }
      }
   }
}
