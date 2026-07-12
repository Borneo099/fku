package lexis.Hack.Hacks.Combat;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;

public class AutoCriticalsHack extends Hack {
   private static final String CONFIG_KEY = "自动暴击";
   private HackConfig config = HackConfig.getInstance();
   public static boolean enabled = false;
   public static boolean onlyOnGround = true;
   public static int criticalMode = 0;
   public static ServerboundInteractPacket pendingPacket = null;
   public static long jumpTime = 0L;
   private static final long MAX_WAIT_MS = 800L;
   public static boolean bypass = false;

   public AutoCriticalsHack() {
      super("自动暴击", "攻击时自动触发暴击", Hack.Category.COMBAT, true);
      this.addSetting(new Hack.Setting("仅地面", "只在玩家在地面时触发暴击", true));
      this.addSetting(new Hack.Setting("跳跃模式", "暴击触发方式", "发包", new String[]{"发包", "跳跃"}));
      this.loadConfig();
   }

   private void loadConfig() {
      onlyOnGround = this.config.getBooleanSetting("自动暴击", "仅地面", true);
      String modeStr = this.config.getStringSetting("自动暴击", "跳跃模式", "发包");
      criticalMode = modeStr.equals("跳跃") ? 1 : 0;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting s = (Hack.Setting)var2.next();
         switch (s.getName()) {
            case "仅地面":
               s.setValue(onlyOnGround);
               break;
            case "跳跃模式":
               s.setValue(criticalMode == 0 ? "发包" : "跳跃");
         }
      }

   }

   private void saveConfig() {
      this.config.saveHackSettings("自动暴击", this.getSettings());
   }

   public void onEnable() {
      enabled = true;
      this.loadConfig();
      pendingPacket = null;
   }

   public void onDisable() {
      enabled = false;
      pendingPacket = null;
   }

   public void onUpdate() {
      if (enabled) {
         boolean needSave = false;
         Iterator var2 = this.getSettings().iterator();

         while(var2.hasNext()) {
            Hack.Setting s = (Hack.Setting)var2.next();
            switch (s.getName()) {
               case "仅地面":
                  boolean newOnly = s.getBoolean();
                  if (newOnly != onlyOnGround) {
                     onlyOnGround = newOnly;
                     needSave = true;
                  }
                  break;
               case "跳跃模式":
                  String newMode = s.getString();
                  int newModeVal = newMode.equals("跳跃") ? 1 : 0;
                  if (newModeVal != criticalMode) {
                     criticalMode = newModeVal;
                     needSave = true;
                  }
            }
         }

         if (needSave) {
            this.saveConfig();
         }

         if (criticalMode == 1 && pendingPacket != null && mc.f_91074_ != null) {
            long elapsed = System.currentTimeMillis() - jumpTime;
            if (mc.f_91074_.m_20184_().f_82480_ < -0.1 || elapsed > 800L) {
               bypass = true;
               mc.f_91074_.f_108617_.m_104955_(pendingPacket);
               bypass = false;
               pendingPacket = null;
            }
         }

      }
   }

   public void onClick() {
      this.toggle();
   }

   public String getDisplayName() {
      String mode = criticalMode == 0 ? "发包" : "跳跃";
      String var10000 = super.getName();
      return var10000 + " [" + mode + "]";
   }
}
