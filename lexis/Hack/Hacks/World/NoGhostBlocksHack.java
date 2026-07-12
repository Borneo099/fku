package lexis.Hack.Hacks.World;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.HackManager;
import lexis.Hack.Hackutil.config.HackConfig;

public class NoGhostBlocksHack extends Hack {
   private static final String CONFIG_KEY = "防幽灵方块";

   public NoGhostBlocksHack() {
      super("防幽灵方块", new String[]{"破坏/放置方块时不再让客户端抢先改方块, 等服务器真包来才改", "服务器拒绝操作(没权限/领地/反作弊)时, 就不会留下'破坏了其实还在'的幽灵方块", "代价: 高延迟下挖掉的方块会晚约 1 tick 才消失; 单人不生效(本地服无幽灵)"}, Hack.Category.WORLD, true);
      this.addSetting(new Hack.Setting("防破坏", "破坏方块时等服务器确认", true));
      this.addSetting(new Hack.Setting("防放置", "放置方块时等服务器确认", true));
      this.loadConfig();
   }

   private void loadConfig() {
      HackConfig config = HackConfig.getInstance();
      boolean antiBreak = config.getBooleanSetting("防幽灵方块", "防破坏", true);
      boolean antiPlace = config.getBooleanSetting("防幽灵方块", "防放置", true);
      Iterator var4 = this.getSettings().iterator();

      while(var4.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var4.next();
         if (setting.getName().equals("防破坏")) {
            setting.setValue(antiBreak);
         } else if (setting.getName().equals("防放置")) {
            setting.setValue(antiPlace);
         }
      }

   }

   public boolean isAntiBreak() {
      return this.boolSetting("防破坏");
   }

   public boolean isAntiPlace() {
      return this.boolSetting("防放置");
   }

   private boolean boolSetting(String name) {
      Iterator var2 = this.getSettings().iterator();

      Hack.Setting setting;
      do {
         if (!var2.hasNext()) {
            return true;
         }

         setting = (Hack.Setting)var2.next();
      } while(!setting.getName().equals(name));

      return setting.getBoolean();
   }

   public static NoGhostBlocksHack get() {
      Iterator var0 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var0.hasNext()) {
            return null;
         }

         hack = (Hack)var0.next();
      } while(!(hack instanceof NoGhostBlocksHack));

      return (NoGhostBlocksHack)hack;
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void onUpdate() {
   }

   public void onClick() {
      this.toggle();
   }
}
