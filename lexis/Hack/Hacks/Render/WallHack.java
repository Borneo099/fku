package lexis.Hack.Hacks.Render;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class WallHack extends Hack {
   private HackConfig config;
   private static final String CONFIG_KEY = "墙体透视";
   private boolean otherPlayers = true;
   private boolean mobs = false;
   private boolean self = false;

   public WallHack() {
      super("墙体透视", new String[]{"让玩家/生物皮肤穿过方块", "这有默认关闭生物墙体透视->你要右键打开功能设置->这防止多实体崩溃你游戏", "§c§l注意：这功能 和 OptiFine(高清修复) + 铷(Rubidium) + 镁(Magnesium) 不兼容！大率会失败无法穿方块bug！"}, Hack.Category.RENDER, true);
      this.addSetting(new Hack.Setting("透视其他玩家", "是否透视其他玩家", true));
      this.addSetting(new Hack.Setting("透视生物", "是否透视生物（怪物、动物等）", false));
      this.addSetting(new Hack.Setting("透视自己", "是否透视自己", false));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.otherPlayers = this.config.getBooleanSetting("墙体透视", "透视其他玩家", true);
      this.mobs = this.config.getBooleanSetting("墙体透视", "透视生物", false);
      this.self = this.config.getBooleanSetting("墙体透视", "透视自己", false);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "透视其他玩家":
               setting.setValue(this.otherPlayers);
               break;
            case "透视生物":
               setting.setValue(this.mobs);
               break;
            case "透视自己":
               setting.setValue(this.self);
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
         switch (setting.getName()) {
            case "透视其他玩家":
               if (setting.getBoolean() != this.otherPlayers) {
                  this.otherPlayers = setting.getBoolean();
                  needSave = true;
               }
               break;
            case "透视生物":
               if (setting.getBoolean() != this.mobs) {
                  this.mobs = setting.getBoolean();
                  needSave = true;
               }
               break;
            case "透视自己":
               if (setting.getBoolean() != this.self) {
                  this.self = setting.getBoolean();
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.config.saveHackSettings("墙体透视", this.getSettings());
      }

   }

   public boolean shouldPerspective(LivingEntity entity) {
      if (!this.isEnabled()) {
         return false;
      } else if (entity == null) {
         return false;
      } else if (entity instanceof Player) {
         return entity == mc.f_91074_ ? this.self : this.otherPlayers;
      } else {
         return this.mobs;
      }
   }

   public void onClick() {
      this.toggle();
   }
}
