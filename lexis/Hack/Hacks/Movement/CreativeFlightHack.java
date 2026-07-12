package lexis.Hack.Hacks.Movement;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.UpdateListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public class CreativeFlightHack extends Hack implements UpdateListener {
   private HackConfig config;
   private static final String CONFIG_KEY = "创意飞行";
   private boolean antiKick = false;
   private int antiKickInterval = 30;
   private double antiKickDistance = 0.07;
   private int tickCounter = 0;

   public CreativeFlightHack() {
      super("创意飞行", new String[]{"双按下空格飞行模式创造模式一样", "使用是来生存+冒险"}, Hack.Category.MOVEMENT, true);
      this.addSetting(new Hack.Setting("反踢出", "防止被服务器踢出", false));
      this.addSetting(new Hack.Setting("反踢间隔", "反踢出间隔（tick）", 30.0, 5.0, 80.0, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("反踢距离", "反踢出下落距离", 0.07, 0.01, 0.2, Hack.ValueDisplay.DECIMAL));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.antiKick = this.config.getBooleanSetting("创意飞行", "反踢出", false);
      this.antiKickInterval = (int)this.config.getDoubleSetting("创意飞行", "反踢间隔", 30.0);
      this.antiKickDistance = this.config.getDoubleSetting("创意飞行", "反踢距离", 0.07);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "反踢出":
               setting.setValue(this.antiKick);
               break;
            case "反踢间隔":
               setting.setValue((double)this.antiKickInterval);
               break;
            case "反踢距离":
               setting.setValue(this.antiKickDistance);
         }
      }

   }

   public void onEnable() {
      if (mc.f_91074_ != null) {
         LocalPlayer player = mc.f_91074_;
         player.m_150110_().f_35936_ = true;
         player.m_6885_();
         this.tickCounter = 0;
         EventManager.add(UpdateListener.class, this);
      }
   }

   public void onDisable() {
      EventManager.remove(UpdateListener.class, this);
      if (mc.f_91074_ != null) {
         LocalPlayer player = mc.f_91074_;
         boolean canFly = player.m_7500_() || player.m_5833_();
         player.m_150110_().f_35936_ = canFly;
         if (!canFly && player.m_150110_().f_35935_) {
            player.m_150110_().f_35935_ = false;
         }

         player.m_6885_();
      }
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         switch (setting.getName()) {
            case "反踢出":
               boolean newAntiKick = setting.getBoolean();
               if (newAntiKick != this.antiKick) {
                  this.antiKick = newAntiKick;
                  needSave = true;
               }
               break;
            case "反踢间隔":
               int newInterval = (int)setting.getDouble();
               if (newInterval != this.antiKickInterval) {
                  this.antiKickInterval = newInterval;
                  needSave = true;
               }
               break;
            case "反踢距离":
               double newDistance = setting.getDouble();
               if (newDistance != this.antiKickDistance) {
                  this.antiKickDistance = newDistance;
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.config.saveHackSettings("创意飞行", this.getSettings());
      }

      if (mc.f_91074_ != null) {
         LocalPlayer player = mc.f_91074_;
         if (!player.m_150110_().f_35936_) {
            player.m_150110_().f_35936_ = true;
         }

         if (this.antiKick && player.m_150110_().f_35935_) {
            this.doAntiKick();
         }

      }
   }

   private void doAntiKick() {
      LocalPlayer player = mc.f_91074_;
      if (player != null) {
         if (this.tickCounter > this.antiKickInterval + 2) {
            this.tickCounter = 0;
         }

         switch (this.tickCounter) {
            case 0:
               if (mc.f_91066_.f_92090_.m_90857_()) {
                  this.tickCounter = 3;
               } else {
                  this.setMotionY(-this.antiKickDistance);
               }
               break;
            case 1:
               this.setMotionY(this.antiKickDistance);
               break;
            case 2:
               this.setMotionY(0.0);
            case 3:
         }

         ++this.tickCounter;
      }
   }

   private void setMotionY(double motionY) {
      if (mc.f_91074_ != null) {
         Vec3 velocity = mc.f_91074_.m_20184_();
         mc.f_91074_.m_20334_(velocity.f_82479_, motionY, velocity.f_82481_);
      }
   }

   public void onClick() {
      this.toggle();
   }
}
