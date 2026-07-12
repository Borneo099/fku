package lexis.Hack.Hacks.Chat;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.Colors.SettingColor;

public class AntiSpamHack extends Hack {
   public static final Pattern COUNTER_PATTERN = Pattern.compile(" \\[x(\\d+)]$");
   public static boolean enabled = false;
   public static SettingColor counterColor = new SettingColor(245, 169, 245, 255);
   private final HackConfig config;
   private static final String CONFIG_KEY = "反刷屏";

   public AntiSpamHack() {
      super("反刷屏", "和 更好聊天消息 的功能 不兼容", Hack.Category.CHAT, true);
      this.addSetting(new Hack.Setting("计数器颜色", "重复次数显示的颜色", counterColor.getPacked()));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      int color = this.config.getIntSetting("反刷屏", "计数器颜色", counterColor.getPacked());
      counterColor = new SettingColor(color);
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting s = (Hack.Setting)var2.next();
         if ("计数器颜色".equals(s.getName())) {
            s.setValue(counterColor.getPacked());
            break;
         }
      }

   }

   private void saveConfig() {
      this.config.saveHackSettings("反刷屏", this.getSettings());
   }

   public void onEnable() {
      enabled = true;
   }

   public void onDisable() {
      enabled = false;
   }

   public void onUpdate() {
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting s = (Hack.Setting)var1.next();
         if ("计数器颜色".equals(s.getName())) {
            Object v = s.getValue();
            if (v instanceof Number) {
               Number n = (Number)v;
               int newColor = n.intValue();
               if (newColor != counterColor.getPacked()) {
                  counterColor = new SettingColor(newColor);
                  this.saveConfig();
               }
            }
            break;
         }
      }

   }

   public void onClick() {
      this.toggle();
   }

   public static int extractCount(String text) {
      Matcher m = COUNTER_PATTERN.matcher(text);
      return m.find() ? Integer.parseInt(m.group(1)) : 1;
   }

   public static String stripCounter(String text) {
      return COUNTER_PATTERN.matcher(text).replaceFirst("");
   }

   public static int counterColorRGB() {
      return counterColor.getPacked() & 16777215;
   }
}
