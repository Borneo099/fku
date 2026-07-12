package lexis.Hack.Hacks.Movement;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.PacketEvent;
import lexis.Hack.events.PacketSendListener;
import lexis.Hack.events.UpdateListener;
import lexis.mixinterface.IPlayerMoveC2SPacket;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;

public class PacketFlyHack extends Hack implements UpdateListener, PacketSendListener {
   private static final int PACKET_TAG = 1338;
   private static final String CONFIG_KEY = "发包飞行";
   private HackConfig config;
   private float speed = 2.0F;
   private boolean antiKick = true;
   private float antiKickDistance = 0.06F;
   private int antiKickInterval = 20;
   private int tickCounter;

   public PacketFlyHack() {
      super("发包飞行", new String[]{"发包飞行，绕过反作弊检测"}, Hack.Category.MOVEMENT, true);
      this.addSetting(new Hack.Setting("速度", "飞行速度", (double)this.speed, 0.5, 100.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("反踢出", "防止踢出 服务器未启动飞行", true));
      this.addSetting(new Hack.Setting("反踢距离", "反踢距离", (double)this.antiKickDistance, 0.01, 0.5, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("反踢间隔", "反踢间隔(tick)", this.antiKickInterval, 5, 100, Hack.ValueDisplay.INTEGER));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.speed = (float)this.config.getDoubleSetting("发包飞行", "速度", 2.0);
      this.antiKick = this.config.getBooleanSetting("发包飞行", "反踢出", true);
      this.antiKickDistance = (float)this.config.getDoubleSetting("发包飞行", "反踢距离", 0.06);
      this.antiKickInterval = this.config.getIntSetting("发包飞行", "反踢间隔", 20);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "速度":
               setting.setValue((double)this.speed);
               break;
            case "反踢出":
               setting.setValue(this.antiKick);
               break;
            case "反踢距离":
               setting.setValue((double)this.antiKickDistance);
               break;
            case "反踢间隔":
               setting.setValue(this.antiKickInterval);
         }
      }

   }

   public void onEnable() {
      EventManager.add(UpdateListener.class, this);
      EventManager.add(PacketSendListener.class, this);
      this.tickCounter = 0;
   }

   public void onDisable() {
      EventManager.remove(UpdateListener.class, this);
      EventManager.remove(PacketSendListener.class, this);
      if (mc.f_91074_ != null) {
         mc.f_91074_.m_20256_(Vec3.f_82478_);
      }

   }

   public String getDisplayName() {
      return this.antiKick ? String.format("%s [%.1f,AK]", this.getName(), this.speed) : String.format("%s [%.1f]", this.getName(), this.speed);
   }

   public void onUpdate() {
      if (mc.f_91074_ != null && mc.m_91403_() != null) {
         boolean needSave = false;
         Iterator var2 = this.getSettings().iterator();

         while(var2.hasNext()) {
            Hack.Setting setting = (Hack.Setting)var2.next();
            switch (setting.getName()) {
               case "速度":
                  if ((float)setting.getDouble() != this.speed) {
                     this.speed = (float)setting.getDouble();
                     needSave = true;
                  }
                  break;
               case "反踢出":
                  if (setting.getBoolean() != this.antiKick) {
                     this.antiKick = setting.getBoolean();
                     needSave = true;
                  }
                  break;
               case "反踢距离":
                  if ((float)setting.getDouble() != this.antiKickDistance) {
                     this.antiKickDistance = (float)setting.getDouble();
                     needSave = true;
                  }
                  break;
               case "反踢间隔":
                  if (setting.getInt() != this.antiKickInterval) {
                     this.antiKickInterval = setting.getInt();
                     needSave = true;
                  }
            }
         }

         if (needSave) {
            this.config.saveHackSettings("发包飞行", this.getSettings());
         }

         LocalPlayer player = mc.f_91074_;
         float forward = 0.0F;
         float strafe = 0.0F;
         float vertical = 0.0F;
         if (mc.f_91066_.f_92085_.m_90857_()) {
            ++forward;
         }

         if (mc.f_91066_.f_92087_.m_90857_()) {
            --forward;
         }

         if (mc.f_91066_.f_92086_.m_90857_()) {
            ++strafe;
         }

         if (mc.f_91066_.f_92088_.m_90857_()) {
            --strafe;
         }

         if (mc.f_91066_.f_92089_.m_90857_()) {
            ++vertical;
         }

         if (mc.f_91066_.f_92090_.m_90857_()) {
            --vertical;
         }

         float actualSpeed = this.speed * 0.05F;
         Vec3 movement = this.calcMovement(player, forward, strafe, vertical, actualSpeed);
         player.m_20256_(movement);
         double x = player.m_20185_() + movement.f_82479_;
         double y = player.m_20186_() + movement.f_82480_;
         double z = player.m_20189_() + movement.f_82481_;
         if (this.antiKick && movement.f_82480_ == 0.0) {
            if (this.tickCounter > this.antiKickInterval + 1) {
               this.tickCounter = 0;
            }

            if (this.tickCounter == 0) {
               y -= (double)this.antiKickDistance;
            } else if (this.tickCounter == 1) {
               y += (double)this.antiKickDistance;
            }

            ++this.tickCounter;
         }

         ServerboundMovePlayerPacket.Pos packet = new ServerboundMovePlayerPacket.Pos(x, y, z, true);
         ((IPlayerMoveC2SPacket)packet).lexis$setTag(1338);
         mc.m_91403_().m_104955_(packet);
      }
   }

   public void onPacketSend(PacketEvent.Send event) {
      if (event.packet instanceof ServerboundMovePlayerPacket) {
         int tag = ((IPlayerMoveC2SPacket)event.packet).lexis$getTag();
         if (tag != 1338) {
            event.cancel();
         }
      }

   }

   public void onClick() {
      this.toggle();
   }

   private Vec3 calcMovement(LocalPlayer player, float forward, float strafe, float vertical, float speed) {
      if (forward == 0.0F && strafe == 0.0F && vertical == 0.0F) {
         return Vec3.f_82478_;
      } else {
         float yaw = player.m_146908_();
         double rad = Math.toRadians((double)yaw);
         double mx = 0.0;
         double mz = 0.0;
         if (forward != 0.0F) {
            mx += -Math.sin(rad) * (double)forward;
            mz += Math.cos(rad) * (double)forward;
         }

         if (strafe != 0.0F) {
            mx += -Math.sin(rad - 1.5707963267948966) * (double)strafe;
            mz += Math.cos(rad - 1.5707963267948966) * (double)strafe;
         }

         double len = Math.sqrt(mx * mx + mz * mz);
         if (len > 0.0) {
            mx = mx / len * (double)speed * 20.0;
            mz = mz / len * (double)speed * 20.0;
         }

         return new Vec3(mx, (double)(vertical * speed * 20.0F), mz);
      }
   }
}
