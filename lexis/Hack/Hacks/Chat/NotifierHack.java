package lexis.Hack.Hacks.Chat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.FriendsManager;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.PacketEvent;
import lexis.Hack.events.PacketReceiveListener;
import lexis.Hack.events.TickListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class NotifierHack extends Hack implements PacketReceiveListener, TickListener {
   private static final String CONFIG_KEY = "事件通知器";
   private HackConfig config = HackConfig.getInstance();
   private boolean totemPops = true;
   private boolean totemsIgnoreOwn = false;
   private boolean totemsIgnoreFriends = false;
   private boolean pearl = true;
   private boolean pearlIgnoreOwn = false;
   private boolean pearlIgnoreFriends = false;
   private final Map totemPopMap = new ConcurrentHashMap();
   private final Map pearlStartPosMap = new HashMap();
   private final Random random = new Random();

   public NotifierHack() {
      super("事件通知器", new String[]{"统计图腾次数+末影珍珠落地", "§4§l源码来自: Meteor1.20.1Fabric"}, Hack.Category.CHAT, true);
      this.addSetting(new Hack.Setting("图腾统计", "统计玩家触发的图腾次数", true));
      this.addSetting(new Hack.Setting("忽略自己图腾", "不统计自己的图腾触发", false));
      this.addSetting(new Hack.Setting("忽略好友图腾", "不统计好友的图腾触发", false));
      this.addSetting(new Hack.Setting("珍珠落地提醒", "提醒玩家使用末影珍珠传送的位置", true));
      this.addSetting(new Hack.Setting("忽略自己珍珠", "不提醒自己的珍珠", false));
      this.addSetting(new Hack.Setting("忽略好友珍珠", "不提醒好友的珍珠", false));
      this.loadConfig();
   }

   private void loadConfig() {
      this.totemPops = this.config.getBooleanSetting("事件通知器", "图腾统计", true);
      this.totemsIgnoreOwn = this.config.getBooleanSetting("事件通知器", "忽略自己图腾", false);
      this.totemsIgnoreFriends = this.config.getBooleanSetting("事件通知器", "忽略好友图腾", false);
      this.pearl = this.config.getBooleanSetting("事件通知器", "珍珠落地提醒", true);
      this.pearlIgnoreOwn = this.config.getBooleanSetting("事件通知器", "忽略自己珍珠", false);
      this.pearlIgnoreFriends = this.config.getBooleanSetting("事件通知器", "忽略好友珍珠", false);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting s = (Hack.Setting)var1.next();
         switch (s.getName()) {
            case "图腾统计":
               s.setValue(this.totemPops);
               break;
            case "忽略自己图腾":
               s.setValue(this.totemsIgnoreOwn);
               break;
            case "忽略好友图腾":
               s.setValue(this.totemsIgnoreFriends);
               break;
            case "珍珠落地提醒":
               s.setValue(this.pearl);
               break;
            case "忽略自己珍珠":
               s.setValue(this.pearlIgnoreOwn);
               break;
            case "忽略好友珍珠":
               s.setValue(this.pearlIgnoreFriends);
         }
      }

   }

   private void saveConfig() {
      this.config.saveHackSettings("事件通知器", this.getSettings());
   }

   public void onEnable() {
      EventManager.add(PacketReceiveListener.class, this);
      EventManager.add(TickListener.class, this);
      MinecraftForge.EVENT_BUS.register(this);
   }

   public void onDisable() {
      EventManager.remove(PacketReceiveListener.class, this);
      EventManager.remove(TickListener.class, this);
      MinecraftForge.EVENT_BUS.unregister(this);
      this.totemPopMap.clear();
      this.pearlStartPosMap.clear();
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting s = (Hack.Setting)var2.next();
         switch (s.getName()) {
            case "图腾统计":
               if (s.getBoolean() != this.totemPops) {
                  this.totemPops = s.getBoolean();
                  needSave = true;
               }
               break;
            case "忽略自己图腾":
               if (s.getBoolean() != this.totemsIgnoreOwn) {
                  this.totemsIgnoreOwn = s.getBoolean();
                  needSave = true;
               }
               break;
            case "忽略好友图腾":
               if (s.getBoolean() != this.totemsIgnoreFriends) {
                  this.totemsIgnoreFriends = s.getBoolean();
                  needSave = true;
               }
               break;
            case "珍珠落地提醒":
               if (s.getBoolean() != this.pearl) {
                  this.pearl = s.getBoolean();
                  needSave = true;
               }
               break;
            case "忽略自己珍珠":
               if (s.getBoolean() != this.pearlIgnoreOwn) {
                  this.pearlIgnoreOwn = s.getBoolean();
                  needSave = true;
               }
               break;
            case "忽略好友珍珠":
               if (s.getBoolean() != this.pearlIgnoreFriends) {
                  this.pearlIgnoreFriends = s.getBoolean();
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.saveConfig();
      }

   }

   public void onPacketReceive(PacketEvent.Receive event) {
      if (this.totemPops) {
         Packet var3 = event.packet;
         if (var3 instanceof ClientboundEntityEventPacket) {
            ClientboundEntityEventPacket p = (ClientboundEntityEventPacket)var3;
            if (p.m_132102_() == 35) {
               Entity entity = p.m_132094_(mc.f_91073_);
               if (entity instanceof Player) {
                  Player player = (Player)entity;
                  if (player != mc.f_91074_ || !this.totemsIgnoreOwn) {
                     if (!FriendsManager.getInstance().isFriend(player) || !this.totemsIgnoreFriends) {
                        int pops = (Integer)this.totemPopMap.getOrDefault(player.m_20148_(), 0) + 1;
                        this.totemPopMap.put(player.m_20148_(), pops);
                        String msg = String.format("§d[§6Lexis§d] §7[事件通知器§7] §f%s §e§l爆了 §c§l%d §e个图腾！", player.m_7755_().getString(), pops);
                        mc.f_91074_.m_5661_(Component.m_237113_(msg), false);
                        mc.f_91073_.m_5594_(mc.f_91074_, mc.f_91074_.m_20183_(), SoundEvents.f_11871_, SoundSource.PLAYERS, 0.5F, 1.0F);
                     }
                  }
               }
            }
         }
      }
   }

   public void onTick() {
      if (this.totemPops) {
         Iterator var1 = mc.f_91073_.m_6907_().iterator();

         while(true) {
            Player player;
            do {
               do {
                  if (!var1.hasNext()) {
                     return;
                  }

                  player = (Player)var1.next();
               } while(!this.totemPopMap.containsKey(player.m_20148_()));
            } while(player.f_20919_ <= 0 && !(player.m_21223_() <= 0.0F));

            int pops = (Integer)this.totemPopMap.remove(player.m_20148_());
            String msg = String.format("§d[§6Lexis§d] §7[事件通知器§7] §f%s §7死亡了，共爆了 §c%d §7个图腾。", player.m_7755_().getString(), pops);
            mc.f_91074_.m_5661_(Component.m_237113_(msg), false);
         }
      }
   }

   @SubscribeEvent
   public void onEntityJoin(EntityJoinLevelEvent event) {
      if (this.pearl) {
         Entity e = event.getEntity();
         if (e instanceof ThrownEnderpearl) {
            ThrownEnderpearl pearlEnt = (ThrownEnderpearl)e;
            this.pearlStartPosMap.put(pearlEnt.m_19879_(), pearlEnt.m_20182_());
         }

      }
   }

   @SubscribeEvent
   public void onEntityLeave(EntityLeaveLevelEvent event) {
      if (this.pearl) {
         Entity e = event.getEntity();
         Integer id = e.m_19879_();
         if (this.pearlStartPosMap.containsKey(id)) {
            ThrownEnderpearl pearlEnt = (ThrownEnderpearl)e;
            Entity var6 = pearlEnt.m_19749_();
            if (var6 instanceof Player) {
               Player owner = (Player)var6;
               if (owner == mc.f_91074_ && this.pearlIgnoreOwn) {
                  return;
               }

               if (FriendsManager.getInstance().isFriend(owner) && this.pearlIgnoreFriends) {
                  return;
               }

               Vec3 startPos = (Vec3)this.pearlStartPosMap.get(id);
               Vec3 endPos = e.m_20182_();
               double distance = startPos.m_82554_(endPos);
               BlockPos landPos = e.m_20183_();
               String msg = String.format("§d[§6Lexis§d] §7[事件通知器§7] §f%s §e的珍珠落地坐标 §a%d %d %d §7(距离 %.1f 米)", owner.m_7755_().getString(), landPos.m_123341_(), landPos.m_123342_(), landPos.m_123343_(), distance);
               mc.f_91074_.m_5661_(Component.m_237113_(msg), false);
               mc.f_91073_.m_5594_(mc.f_91074_, mc.f_91074_.m_20183_(), SoundEvents.f_11857_, SoundSource.PLAYERS, 0.3F, 1.2F);
            }

            this.pearlStartPosMap.remove(id);
         }

      }
   }

   public void onClick() {
      this.toggle();
   }
}
