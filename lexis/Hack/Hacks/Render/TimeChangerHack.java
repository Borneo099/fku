package lexis.Hack.Hacks.Render;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.PacketEvent;
import lexis.Hack.events.PacketReceiveListener;
import lexis.Hack.events.UpdateListener;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;

public class TimeChangerHack extends Hack implements UpdateListener, PacketReceiveListener {
   private HackConfig config;
   private static final String CONFIG_KEY = "时间修改";
   private long customTime = 6000L;
   private long oldTime = 0L;
   private boolean timeFlow = false;
   private double flowSpeed = 1.0;

   public TimeChangerHack() {
      super("时间修改", "自定义世界时间", Hack.Category.RENDER, true);
      this.addSetting(new Hack.Setting("时间值", "自定义时间值 (0-24000)", 6000, 0, 24000, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("循环流动", "时间自动循环流动", false));
      this.addSetting(new Hack.Setting("流动速度", "时间流动速度(正常速度为1.0)(改更快,想变成天堂制造？)", 1.0, 0.1, 51200.0, Hack.ValueDisplay.DECIMAL));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.customTime = (long)this.config.getDoubleSetting("时间修改", "时间值", 6000.0);
      this.timeFlow = this.config.getBooleanSetting("时间修改", "循环流动", false);
      this.flowSpeed = this.config.getDoubleSetting("时间修改", "流动速度", 1.0);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "时间值":
               setting.setValue((double)this.customTime);
               break;
            case "循环流动":
               setting.setValue(this.timeFlow);
               break;
            case "流动速度":
               setting.setValue(this.flowSpeed);
         }
      }

   }

   public void onEnable() {
      if (mc.f_91073_ != null) {
         this.oldTime = mc.f_91073_.m_46468_();
      }

      EventManager.add(UpdateListener.class, this);
      EventManager.add(PacketReceiveListener.class, this);
   }

   public void onDisable() {
      EventManager.remove(UpdateListener.class, this);
      EventManager.remove(PacketReceiveListener.class, this);
      if (mc.f_91073_ != null) {
         mc.f_91073_.m_104746_(this.oldTime);
      }

   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         switch (setting.getName()) {
            case "时间值":
               long newTime = (long)setting.getDouble();
               if (newTime != this.customTime && !this.timeFlow) {
                  this.customTime = newTime;
                  needSave = true;
               }
               break;
            case "循环流动":
               boolean newFlow = setting.getBoolean();
               if (newFlow != this.timeFlow) {
                  this.timeFlow = newFlow;
                  needSave = true;
                  if (mc.f_91073_ != null) {
                     this.customTime = mc.f_91073_.m_46468_();
                  }
               }
               break;
            case "流动速度":
               double newSpeed = setting.getDouble();
               if (Math.abs(newSpeed - this.flowSpeed) > 0.001) {
                  this.flowSpeed = newSpeed;
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.config.saveHackSettings("时间修改", this.getSettings());
      }

      if (mc.f_91073_ != null && this.isEnabled()) {
         long newDayTime;
         if (this.timeFlow) {
            this.customTime = (long)(((double)this.customTime + this.flowSpeed) % 24000.0);
            if (this.customTime < 0L) {
               this.customTime += 24000L;
            }

            newDayTime = this.customTime;
         } else {
            newDayTime = this.customTime;
         }

         mc.f_91073_.m_104746_(newDayTime);
      }

   }

   public void onPacketReceive(PacketEvent.Receive event) {
      if (event.packet instanceof ClientboundSetTimePacket) {
         this.oldTime = ((ClientboundSetTimePacket)event.packet).m_133361_();
         event.cancel();
      }

   }

   public void onClick() {
      this.toggle();
   }
}
