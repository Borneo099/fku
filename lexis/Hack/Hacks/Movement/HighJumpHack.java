package lexis.Hack.Hacks.Movement;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(
   modid = "lexis",
   value = {Dist.CLIENT}
)
public class HighJumpHack extends Hack {
   private HackConfig config;
   private static final String CONFIG_KEY = "高跳";
   private int jumpHeight = 6;

   public HighJumpHack() {
      super("高跳", "让你跳得更高", Hack.Category.MOVEMENT, true);
      this.addSetting(new Hack.Setting("跳跃高度", "跳跃高度（方块数）", 6.0, 1.0, 100.0, Hack.ValueDisplay.INTEGER));
      this.config = HackConfig.getInstance();
      this.loadConfig();
      MinecraftForge.EVENT_BUS.register(this);
   }

   private void loadConfig() {
      this.jumpHeight = (int)this.config.getDoubleSetting("高跳", "跳跃高度", 6.0);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("跳跃高度")) {
            setting.setValue((double)this.jumpHeight);
            break;
         }
      }

   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         if (setting.getName().equals("跳跃高度")) {
            int newHeight = (int)setting.getDouble();
            if (newHeight != this.jumpHeight) {
               this.jumpHeight = newHeight;
               needSave = true;
            }
            break;
         }
      }

      if (needSave) {
         this.config.saveHackSettings("高跳", this.getSettings());
      }

   }

   @SubscribeEvent
   public void onLivingJump(LivingEvent.LivingJumpEvent event) {
      if (this.isEnabled()) {
         LivingEntity entity = event.getEntity();
         if (entity instanceof Player && entity == mc.f_91074_) {
            double additional = (double)this.jumpHeight * 0.1;
            entity.m_20334_(entity.m_20184_().f_82479_, entity.m_20184_().f_82480_ + additional, entity.m_20184_().f_82481_);
         }

      }
   }

   public void onClick() {
      this.toggle();
   }
}
