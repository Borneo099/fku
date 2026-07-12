package lexis.Hack.Hacks.Protect;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;

public class ParticleProtectHack extends Hack {
   private HackConfig config;
   private static final String CONFIG_KEY = "粒子保护";
   public static boolean blockParticlePackets = true;
   public static int maxParticleCount = 1000;
   public static boolean useMaxCount = true;

   public ParticleProtectHack() {
      super("粒子保护", "限制服务器粒子数量，保护卡死", Hack.Category.PROTECT, true);
      this.addSetting(new Hack.Setting("阻止粒子", "阻止显示粒子效果 是不可显示粒子！", false));
      this.addSetting(new Hack.Setting("启用数量限制", "限制粒子显示数量", true));
      this.addSetting(new Hack.Setting("最大粒子数", "0 = 不显示, 100000 = 无限制", 1000, 0, 100000, Hack.ValueDisplay.INTEGER));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      blockParticlePackets = this.config.getBooleanSetting("粒子保护", "阻止粒子", false);
      useMaxCount = this.config.getBooleanSetting("粒子保护", "启用数量限制", true);
      maxParticleCount = (int)this.config.getDoubleSetting("粒子保护", "最大粒子数", 1000.0);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "阻止粒子":
               setting.setValue(blockParticlePackets);
               break;
            case "启用数量限制":
               setting.setValue(useMaxCount);
               break;
            case "最大粒子数":
               setting.setValue((double)maxParticleCount);
         }
      }

   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void onUpdate() {
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "阻止粒子":
               blockParticlePackets = setting.getBoolean();
               break;
            case "启用数量限制":
               useMaxCount = setting.getBoolean();
               break;
            case "最大粒子数":
               maxParticleCount = (int)setting.getDouble();
         }
      }

   }

   public void onClick() {
      this.toggle();
   }

   public static boolean shouldBlockParticle() {
      if (blockParticlePackets) {
         return true;
      } else if (!useMaxCount) {
         return false;
      } else {
         return maxParticleCount == 0;
      }
   }

   public static boolean shouldLimitCount() {
      return useMaxCount && maxParticleCount > 0;
   }

   public static int getMaxParticleCount() {
      return maxParticleCount;
   }
}
