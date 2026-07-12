package lexis.Hack.Hacks.Movement;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.UpdateListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FastLadderHack extends Hack implements UpdateListener {
   private HackConfig config;
   private static final String CONFIG_KEY = "快速爬梯";
   private double climbSpeed = 0.2872;

   public FastLadderHack() {
      super("快速爬梯", "在梯子和藤蔓上快速爬升", Hack.Category.MOVEMENT, true);
      this.addSetting(new Hack.Setting("爬升速度", "爬梯时的向上速度", 0.3, 0.1, 4.5, Hack.ValueDisplay.DECIMAL));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.climbSpeed = this.config.getDoubleSetting("快速爬梯", "爬升速度", 0.3);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("爬升速度")) {
            setting.setValue(this.climbSpeed);
            break;
         }
      }

   }

   public void onEnable() {
      EventManager.add(UpdateListener.class, this);
   }

   public void onDisable() {
      EventManager.remove(UpdateListener.class, this);
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         if (setting.getName().equals("爬升速度")) {
            double newSpeed = setting.getDouble();
            if (newSpeed != this.climbSpeed) {
               this.climbSpeed = newSpeed;
               needSave = true;
            }
            break;
         }
      }

      if (needSave) {
         this.config.saveHackSettings("快速爬梯", this.getSettings());
      }

      if (mc.f_91074_ != null) {
         Player player = mc.f_91074_;
         boolean onLadder = false;
         if (player.m_9236_() != null) {
            BlockState blockState = player.m_9236_().m_8055_(player.m_20183_());
            onLadder = blockState.m_60713_(Blocks.f_50155_) || blockState.m_60713_(Blocks.f_50191_);
         }

         if (onLadder && (player.f_20900_ != 0.0F || player.f_20902_ != 0.0F)) {
            Vec3 velocity = player.m_20184_();
            if (velocity.f_82480_ < this.climbSpeed) {
               player.m_20334_(velocity.f_82479_, this.climbSpeed, velocity.f_82481_);
            }
         }

      }
   }

   public void onClick() {
      this.toggle();
   }
}
