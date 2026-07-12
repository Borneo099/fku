package lexis.Hack.Hacks.Protect;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;

public class EntityNameLimiterHack extends Hack {
   private static boolean enabled = false;
   private static int maxNameLength = 32;
   private static final Set warnedEntities = ConcurrentHashMap.newKeySet();
   private HackConfig config;
   private static final String CONFIG_KEY = "实体名称限制";

   public EntityNameLimiterHack() {
      super("实体名称限制", "限制实体名称长度，防止过长名称卡顿", Hack.Category.PROTECT, true);
      this.addSetting(new Hack.Setting("最大长度", "实体名称最大长度", 32, 1, 512, Hack.ValueDisplay.INTEGER));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      maxNameLength = (int)this.config.getDoubleSetting("实体名称限制", "最大长度", 32.0);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("最大长度")) {
            setting.setValue((double)maxNameLength);
            break;
         }
      }

   }

   public void onEnable() {
      enabled = true;
      warnedEntities.clear();
   }

   public void onDisable() {
      enabled = false;
      warnedEntities.clear();
   }

   public void onUpdate() {
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("最大长度")) {
            maxNameLength = (int)setting.getDouble();
            break;
         }
      }

   }

   public void onClick() {
      this.toggle();
   }

   public boolean isEnabled() {
      return enabled;
   }

   public static int getMaxNameLength() {
      return maxNameLength;
   }

   public static boolean shouldWarn(int entityId) {
      return !warnedEntities.contains(entityId);
   }

   public static void addWarnedEntity(int entityId) {
      warnedEntities.add(entityId);
      (new Thread(() -> {
         try {
            Thread.sleep(2147483647L);
            warnedEntities.remove(entityId);
         } catch (InterruptedException var2) {
         }

      })).start();
   }
}
