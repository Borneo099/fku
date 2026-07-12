package lexis.Hack.Hacks.World;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.RotationUtils;
import lexis.Hack.Utils.HeadOnlyLookUtils.HeadOnlyLook;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.UpdateListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class EndermanLookHack extends Hack implements UpdateListener {
   private HackConfig config;
   private static final String CONFIG_KEY = "末影人看向";
   private Mode lookMode;
   private boolean stun;
   private Entity currentTarget;
   private boolean lookingUp;

   public EndermanLookHack() {
      super("末影人看向", "防止末影人激怒或注视末影人", Hack.Category.WORLD, true);
      this.lookMode = EndermanLookHack.Mode.Away;
      this.stun = true;
      this.currentTarget = null;
      this.lookingUp = false;
      this.addSetting(new Hack.Setting("模式", "模式行为", "移开视线", new String[]{"移开视线", "注视"}));
      this.addSetting(new Hack.Setting("激怒者晕眩", "自动注视激怒的末影人来眩晕它们", true));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      String modeStr = this.config.getStringSetting("末影人看向", "模式", "移开视线");
      Mode[] var2 = EndermanLookHack.Mode.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Mode mode = var2[var4];
         if (mode.toString().equals(modeStr)) {
            this.lookMode = mode;
            break;
         }
      }

      this.stun = this.config.getBooleanSetting("末影人看向", "激怒者晕眩", true);
      Iterator var6 = this.getSettings().iterator();

      while(var6.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var6.next();
         switch (setting.getName()) {
            case "模式":
               setting.setValue(this.lookMode.toString());
               break;
            case "激怒者晕眩":
               setting.setValue(this.stun);
         }
      }

   }

   public void onEnable() {
      EventManager.add(UpdateListener.class, this);
      this.currentTarget = null;
      this.lookingUp = false;
   }

   public void onDisable() {
      EventManager.remove(UpdateListener.class, this);
      HeadOnlyLook.stopLooking();
      this.currentTarget = null;
      this.lookingUp = false;
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      label122:
      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         switch (setting.getName()) {
            case "模式":
               String modeStr = setting.getString();
               Mode[] var7 = EndermanLookHack.Mode.values();
               int var8 = var7.length;
               int var9 = 0;

               while(true) {
                  if (var9 >= var8) {
                     continue label122;
                  }

                  Mode mode = var7[var9];
                  if (mode.toString().equals(modeStr) && this.lookMode != mode) {
                     this.lookMode = mode;
                     needSave = true;
                     HeadOnlyLook.stopLooking();
                     this.currentTarget = null;
                     this.lookingUp = false;
                     continue label122;
                  }

                  ++var9;
               }
            case "激怒者晕眩":
               if (setting.getBoolean() != this.stun) {
                  this.stun = setting.getBoolean();
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.config.saveHackSettings("末影人看向", this.getSettings());
      }

      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         if (!mc.f_91074_.m_6844_(EquipmentSlot.HEAD).m_150930_(Blocks.f_50143_.m_5456_()) && !mc.f_91074_.m_150110_().f_35937_) {
            boolean foundTarget = false;
            Iterator var12 = mc.f_91073_.m_104735_().iterator();

            while(var12.hasNext()) {
               Entity entity = (Entity)var12.next();
               if (entity instanceof EnderMan) {
                  EnderMan enderman = (EnderMan)entity;
                  if (enderman.m_6084_() && mc.f_91074_.m_142582_(enderman)) {
                     float yaw;
                     float pitch;
                     switch (this.lookMode) {
                        case Away:
                           if (enderman.m_32531_() && this.stun) {
                              if (this.currentTarget != enderman) {
                                 this.currentTarget = enderman;
                                 this.lookingUp = false;
                                 yaw = RotationUtils.getNeededRotations(enderman.m_146892_()).yaw();
                                 pitch = RotationUtils.getNeededRotations(enderman.m_146892_()).pitch();
                                 HeadOnlyLook.startRotation(yaw, pitch);
                              }

                              foundTarget = true;
                           } else if (this.isPlayerStaring(enderman)) {
                              if (!this.lookingUp) {
                                 this.lookingUp = true;
                                 this.currentTarget = null;
                                 HeadOnlyLook.startRotation(mc.f_91074_.m_146908_(), 90.0F);
                              }

                              foundTarget = true;
                           }
                           break;
                        case At:
                           if (!enderman.m_32531_()) {
                              if (this.currentTarget != enderman) {
                                 this.currentTarget = enderman;
                                 this.lookingUp = false;
                                 yaw = RotationUtils.getNeededRotations(enderman.m_146892_()).yaw();
                                 pitch = RotationUtils.getNeededRotations(enderman.m_146892_()).pitch();
                                 HeadOnlyLook.startRotation(yaw, pitch);
                              }

                              foundTarget = true;
                           }
                     }

                     if (foundTarget) {
                        break;
                     }
                  }
               }
            }

            if (!foundTarget && (this.currentTarget != null || this.lookingUp)) {
               HeadOnlyLook.stopLooking();
               this.currentTarget = null;
               this.lookingUp = false;
            }

         } else {
            HeadOnlyLook.stopLooking();
            this.currentTarget = null;
            this.lookingUp = false;
         }
      }
   }

   private boolean isPlayerStaring(EnderMan enderman) {
      Vec3 lookVec = mc.f_91074_.m_20252_(1.0F).m_82541_();
      Vec3 toEnderman = new Vec3(enderman.m_20185_() - mc.f_91074_.m_20185_(), enderman.m_20188_() - mc.f_91074_.m_20188_(), enderman.m_20189_() - mc.f_91074_.m_20189_());
      double distance = toEnderman.m_82553_();
      toEnderman = toEnderman.m_82541_();
      double dot = lookVec.m_82526_(toEnderman);
      return dot > 1.0 - 0.025 / distance;
   }

   public void onClick() {
      this.toggle();
   }

   public static enum Mode {
      Away("移开视线"),
      At("注视");

      private final String displayName;

      private Mode(String name) {
         this.displayName = name;
      }

      public String toString() {
         return this.displayName;
      }

      // $FF: synthetic method
      private static Mode[] $values() {
         return new Mode[]{Away, At};
      }
   }
}
