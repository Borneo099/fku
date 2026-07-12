package lexis.Hack.Hacks.Chat;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.HackManager;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.Colors.SettingColor;

public class BetterChatHack extends Hack {
   private static final String CONFIG_KEY = "更好聊天消息";
   private boolean showCopy = true;
   private boolean showPlus = true;
   private boolean showFull = true;
   private boolean doubleClickMode = true;
   private boolean showTranslate = true;
   private boolean beautifyMessages = true;
   private boolean showTimestamp = false;
   private SettingColor separatorColor = new SettingColor(-2461482);
   private SettingColor timestampColor = new SettingColor(-38476);
   private boolean highlightSelf = true;
   private SettingColor highlightSelfColor = new SettingColor(16777215);

   public BetterChatHack() {
      super("更好聊天消息", "增强聊天功能：按钮+美化+翻译", Hack.Category.CHAT, true);
      this.addSetting(new Hack.Setting("显示[复制]", "是否显示复制按钮", true));
      this.addSetting(new Hack.Setting("显示[+1]", "是否显示+1按钮", true));
      this.addSetting(new Hack.Setting("显示[全发]", "是否显示全发按钮", true));
      this.addSetting(new Hack.Setting("双击模式", "+1和全发需要双击才发送，复制按钮单击复制", true));
      this.addSetting(new Hack.Setting("显示[翻译]", "是否显示翻译按钮", true));
      this.addSetting(new Hack.Setting("美化消息", "将玩家消息格式化为统一样式", true));
      this.addSetting(new Hack.Setting("显示时间戳", "在美化消息前显示时间 (HH:mm:ss)", false));
      this.addSetting(new Hack.Setting("分隔符颜色", "美化消息中 ┋ 的颜色", this.separatorColor.getPacked()));
      this.addSetting(new Hack.Setting("时间戳颜色", "时间文本的颜色", this.timestampColor.getPacked()));
      this.addSetting(new Hack.Setting("高亮自己名称", "在聊天消息中高亮显示自己的名字", true));
      this.addSetting(new Hack.Setting("高亮颜色", "自己名称的高亮颜色", this.highlightSelfColor.getPacked()));
      this.loadConfig();
   }

   private void loadConfig() {
      HackConfig config = HackConfig.getInstance();
      this.showCopy = config.getBooleanSetting("更好聊天消息", "显示[复制]", true);
      this.showPlus = config.getBooleanSetting("更好聊天消息", "显示[+1]", true);
      this.showFull = config.getBooleanSetting("更好聊天消息", "显示[全发]", true);
      this.doubleClickMode = config.getBooleanSetting("更好聊天消息", "双击模式", true);
      this.showTranslate = config.getBooleanSetting("更好聊天消息", "显示[翻译]", true);
      this.beautifyMessages = config.getBooleanSetting("更好聊天消息", "美化消息", true);
      this.showTimestamp = config.getBooleanSetting("更好聊天消息", "显示时间戳", false);
      this.highlightSelf = config.getBooleanSetting("更好聊天消息", "高亮自己名称", true);
      int sepColor = config.getIntSetting("更好聊天消息", "分隔符颜色", this.separatorColor.getPacked());
      this.separatorColor = new SettingColor(sepColor);
      int timeColor = config.getIntSetting("更好聊天消息", "时间戳颜色", this.timestampColor.getPacked());
      this.timestampColor = new SettingColor(timeColor);
      int highColor = config.getIntSetting("更好聊天消息", "高亮颜色", this.highlightSelfColor.getPacked());
      this.highlightSelfColor = new SettingColor(highColor);
      Iterator var5 = this.getSettings().iterator();

      while(var5.hasNext()) {
         Hack.Setting s = (Hack.Setting)var5.next();
         switch (s.getName()) {
            case "显示[复制]":
               s.setValue(this.showCopy);
               break;
            case "显示[+1]":
               s.setValue(this.showPlus);
               break;
            case "显示[全发]":
               s.setValue(this.showFull);
               break;
            case "双击模式":
               s.setValue(this.doubleClickMode);
               break;
            case "显示[翻译]":
               s.setValue(this.showTranslate);
               break;
            case "美化消息":
               s.setValue(this.beautifyMessages);
               break;
            case "显示时间戳":
               s.setValue(this.showTimestamp);
               break;
            case "分隔符颜色":
               s.setValue(this.separatorColor.getPacked());
               break;
            case "时间戳颜色":
               s.setValue(this.timestampColor.getPacked());
               break;
            case "高亮自己名称":
               s.setValue(this.highlightSelf);
               break;
            case "高亮颜色":
               s.setValue(this.highlightSelfColor.getPacked());
         }
      }

   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void onUpdate() {
      boolean needSave = false;
      HackConfig config = HackConfig.getInstance();
      Iterator var3 = this.getSettings().iterator();

      while(var3.hasNext()) {
         Hack.Setting s = (Hack.Setting)var3.next();
         switch (s.getName()) {
            case "显示[复制]":
               if (s.getBoolean() != this.showCopy) {
                  this.showCopy = s.getBoolean();
                  needSave = true;
               }
               break;
            case "显示[+1]":
               if (s.getBoolean() != this.showPlus) {
                  this.showPlus = s.getBoolean();
                  needSave = true;
               }
               break;
            case "显示[全发]":
               if (s.getBoolean() != this.showFull) {
                  this.showFull = s.getBoolean();
                  needSave = true;
               }
               break;
            case "双击模式":
               if (s.getBoolean() != this.doubleClickMode) {
                  this.doubleClickMode = s.getBoolean();
                  needSave = true;
               }
               break;
            case "显示[翻译]":
               if (s.getBoolean() != this.showTranslate) {
                  this.showTranslate = s.getBoolean();
                  needSave = true;
               }
               break;
            case "美化消息":
               if (s.getBoolean() != this.beautifyMessages) {
                  this.beautifyMessages = s.getBoolean();
                  needSave = true;
               }
               break;
            case "显示时间戳":
               if (s.getBoolean() != this.showTimestamp) {
                  this.showTimestamp = s.getBoolean();
                  needSave = true;
               }
               break;
            case "分隔符颜色":
               int newSep = (Integer)s.getValue();
               if (newSep != this.separatorColor.getPacked()) {
                  this.separatorColor = new SettingColor(newSep);
                  needSave = true;
               }
               break;
            case "时间戳颜色":
               int newTime = (Integer)s.getValue();
               if (newTime != this.timestampColor.getPacked()) {
                  this.timestampColor = new SettingColor(newTime);
                  needSave = true;
               }
               break;
            case "高亮自己名称":
               if (s.getBoolean() != this.highlightSelf) {
                  this.highlightSelf = s.getBoolean();
                  needSave = true;
               }
               break;
            case "高亮颜色":
               int newHigh = (Integer)s.getValue();
               if (newHigh != this.highlightSelfColor.getPacked()) {
                  this.highlightSelfColor = new SettingColor(newHigh);
                  needSave = true;
               }
         }
      }

      if (needSave) {
         config.saveHackSettings("更好聊天消息", this.getSettings());
      }

   }

   public void onClick() {
      this.toggle();
   }

   public static boolean isShowCopy() {
      Iterator var0 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var0.hasNext()) {
            return false;
         }

         hack = (Hack)var0.next();
      } while(!(hack instanceof BetterChatHack) || !hack.isEnabled());

      return ((BetterChatHack)hack).showCopy;
   }

   public static boolean isShowPlus() {
      Iterator var0 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var0.hasNext()) {
            return false;
         }

         hack = (Hack)var0.next();
      } while(!(hack instanceof BetterChatHack) || !hack.isEnabled());

      return ((BetterChatHack)hack).showPlus;
   }

   public static boolean isShowFull() {
      Iterator var0 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var0.hasNext()) {
            return false;
         }

         hack = (Hack)var0.next();
      } while(!(hack instanceof BetterChatHack) || !hack.isEnabled());

      return ((BetterChatHack)hack).showFull;
   }

   public static boolean isDoubleClickMode() {
      Iterator var0 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var0.hasNext()) {
            return false;
         }

         hack = (Hack)var0.next();
      } while(!(hack instanceof BetterChatHack) || !hack.isEnabled());

      return ((BetterChatHack)hack).doubleClickMode;
   }

   public static boolean isShowTranslate() {
      Iterator var0 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var0.hasNext()) {
            return false;
         }

         hack = (Hack)var0.next();
      } while(!(hack instanceof BetterChatHack) || !hack.isEnabled());

      return ((BetterChatHack)hack).showTranslate;
   }

   public static boolean isBeautifyMessages() {
      Iterator var0 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var0.hasNext()) {
            return false;
         }

         hack = (Hack)var0.next();
      } while(!(hack instanceof BetterChatHack) || !hack.isEnabled());

      return ((BetterChatHack)hack).beautifyMessages;
   }

   public static boolean isShowTimestamp() {
      Iterator var0 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var0.hasNext()) {
            return false;
         }

         hack = (Hack)var0.next();
      } while(!(hack instanceof BetterChatHack) || !hack.isEnabled());

      return ((BetterChatHack)hack).showTimestamp;
   }

   public static int getSeparatorColor() {
      Iterator var0 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var0.hasNext()) {
            return -2461482;
         }

         hack = (Hack)var0.next();
      } while(!(hack instanceof BetterChatHack) || !hack.isEnabled());

      return ((BetterChatHack)hack).separatorColor.getPacked();
   }

   public static int getTimestampColor() {
      Iterator var0 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var0.hasNext()) {
            return -38476;
         }

         hack = (Hack)var0.next();
      } while(!(hack instanceof BetterChatHack) || !hack.isEnabled());

      return ((BetterChatHack)hack).timestampColor.getPacked();
   }

   public static boolean isHighlightSelf() {
      Iterator var0 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var0.hasNext()) {
            return false;
         }

         hack = (Hack)var0.next();
      } while(!(hack instanceof BetterChatHack) || !hack.isEnabled());

      return ((BetterChatHack)hack).highlightSelf;
   }

   public static int getHighlightSelfColor() {
      Iterator var0 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var0.hasNext()) {
            return 16777215;
         }

         hack = (Hack)var0.next();
      } while(!(hack instanceof BetterChatHack) || !hack.isEnabled());

      return ((BetterChatHack)hack).highlightSelfColor.getPacked();
   }
}
