package lexis.Hack.Hacks.Render;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.FakeGlowManager;
import lexis.Hack.Utils.Colors.SettingColor;
import lexis.Hack.Utils.Reach.RangeUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult.Type;

public class AimEntityGlowHack extends Hack {
   private static final String CONFIG_KEY = "瞄准实体发光";
   private static final String GLOW_SOURCE = "AimEntityGlow";
   private SettingColor glowColor = new SettingColor(13, 78, 91, 255);
   private Entity currentGlowEntity = null;
   private HackConfig config = HackConfig.getInstance();

   public AimEntityGlowHack() {
      super("瞄准实体发光", "瞄准实体有带发光 可以自定义颜色", Hack.Category.RENDER, true);
      this.addSetting(new Hack.Setting("发光颜色", "瞄准时实体的发光颜色", this.glowColor.getPacked()));
      this.loadConfig();
   }

   private void loadConfig() {
      int colorInt = this.config.getIntSetting("瞄准实体发光", "发光颜色", this.glowColor.getPacked());
      this.glowColor = new SettingColor(colorInt);
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting s = (Hack.Setting)var2.next();
         switch (s.getName()) {
            case "发光颜色":
               s.setValue(this.glowColor.getPacked());
         }
      }

   }

   private void saveConfig() {
      this.config.saveHackSettings("瞄准实体发光", this.getSettings());
   }

   public void onEnable() {
      this.currentGlowEntity = null;
   }

   public void onDisable() {
      if (this.currentGlowEntity != null) {
         FakeGlowManager.setGlow(this.currentGlowEntity, "AimEntityGlow", false, 0, 0.0);
         this.currentGlowEntity = null;
      }

   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting s = (Hack.Setting)var2.next();
         switch (s.getName()) {
            case "发光颜色":
               int newColor = (Integer)s.getValue();
               if (newColor != this.glowColor.getPacked()) {
                  this.glowColor = new SettingColor(newColor);
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.saveConfig();
      }

      if (this.isEnabled() && mc.f_91074_ != null && mc.f_91073_ != null) {
         Entity target = this.getTargetEntity();
         if (target == null) {
            if (this.currentGlowEntity != null) {
               FakeGlowManager.setGlow(this.currentGlowEntity, "AimEntityGlow", false, 0, 0.0);
               this.currentGlowEntity = null;
            }

         } else if (!RangeUtils.canHit(target)) {
            if (this.currentGlowEntity != null) {
               FakeGlowManager.setGlow(this.currentGlowEntity, "AimEntityGlow", false, 0, 0.0);
               this.currentGlowEntity = null;
            }

         } else {
            if (this.currentGlowEntity != target) {
               if (this.currentGlowEntity != null) {
                  FakeGlowManager.setGlow(this.currentGlowEntity, "AimEntityGlow", false, 0, 0.0);
               }

               FakeGlowManager.setGlow(target, "AimEntityGlow", true, this.glowColor.getPacked(), RangeUtils.get());
               this.currentGlowEntity = target;
            }

         }
      }
   }

   private Entity getTargetEntity() {
      return mc.f_91077_ != null && mc.f_91077_.m_6662_() == Type.ENTITY ? ((EntityHitResult)mc.f_91077_).m_82443_() : null;
   }

   public void onClick() {
      this.toggle();
   }
}
