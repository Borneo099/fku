package lexis.Hack.Hacks.Chat;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber({Dist.CLIENT})
public class FancyChatHack extends Hack {
   private HackConfig config;
   private static final String CONFIG_KEY = "花哨聊天";
   private Style currentStyle;
   private final String blacklist;

   public FancyChatHack() {
      super("花哨聊天", new String[]{"花哨聊天，更化好字体！", "如：数字/字母？123abc ?", "§c§l注意：这可以晓过违规词 某什么插件/MOD 检测违禁词"}, Hack.Category.CHAT, true);
      this.currentStyle = FancyChatHack.Style.DEFAULT;
      this.blacklist = "(){}[]|";
      this.addSetting(new Hack.Setting("风格", "选择聊天字体风格", "默认", new String[]{"默认", "双线体", "粗体", "斜体", "全角"}));
      this.config = HackConfig.getInstance();
      this.loadConfig();
      MinecraftForge.EVENT_BUS.register(this);
   }

   private void loadConfig() {
      String styleStr = this.config.getStringSetting("花哨聊天", "风格", "默认");
      Style[] var2 = FancyChatHack.Style.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Style s = var2[var4];
         if (s.toString().equals(styleStr)) {
            this.currentStyle = s;
            break;
         }
      }

      Iterator var6 = this.getSettings().iterator();

      while(var6.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var6.next();
         if (setting.getName().equals("风格")) {
            setting.setValue(this.currentStyle.toString());
            break;
         }
      }

   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public String getDisplayName() {
      return String.format("%s [%s]", this.getName(), this.currentStyle.toString());
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      label33:
      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         if (setting.getName().equals("风格")) {
            String newStyle = setting.getString();
            Style[] var5 = FancyChatHack.Style.values();
            int var6 = var5.length;
            int var7 = 0;

            while(true) {
               if (var7 >= var6) {
                  break label33;
               }

               Style s = var5[var7];
               if (s.toString().equals(newStyle) && this.currentStyle != s) {
                  this.currentStyle = s;
                  needSave = true;
                  break label33;
               }

               ++var7;
            }
         }
      }

      if (needSave) {
         this.config.saveHackSettings("花哨聊天", this.getSettings());
      }

   }

   @SubscribeEvent
   public void onClientChat(ClientChatEvent event) {
      if (this.isEnabled()) {
         String message = event.getMessage();
         if (!message.startsWith("/") && !message.startsWith(".")) {
            String newMessage = this.convertString(message);
            event.setMessage(newMessage);
         }
      }
   }

   private String convertString(String input) {
      StringBuilder output = new StringBuilder();
      char[] var3 = input.toCharArray();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         char c = var3[var5];
         output.append(this.convertChar(c));
      }

      return output.toString();
   }

   private String convertChar(char c) {
      switch (this.currentStyle) {
         case DEFAULT:
            return this.convertDefault(c);
         case DOUBLE_STRUCK:
            return this.convertDoubleStruck(c);
         case BOLD:
            return this.convertBold(c);
         case ITALIC:
            return this.convertItalic(c);
         case FULLWIDTH:
            return this.convertFullwidth(c);
         default:
            return String.valueOf(c);
      }
   }

   private String convertDefault(char c) {
      if (c >= '!' && c <= 128) {
         return "(){}[]|".contains(Character.toString(c)) ? String.valueOf(c) : new String(Character.toChars(c + 'ﻠ'));
      } else {
         return String.valueOf(c);
      }
   }

   private String convertDoubleStruck(char c) {
      if (c >= 'A' && c <= 'Z') {
         return new String(Character.toChars(120120 + (c - 65)));
      } else if (c >= 'a' && c <= 'z') {
         return new String(Character.toChars(120146 + (c - 97)));
      } else {
         return c >= '0' && c <= '9' ? new String(Character.toChars(120792 + (c - 48))) : String.valueOf(c);
      }
   }

   private String convertBold(char c) {
      if (c >= 'A' && c <= 'Z') {
         return new String(Character.toChars(119808 + (c - 65)));
      } else if (c >= 'a' && c <= 'z') {
         return new String(Character.toChars(119834 + (c - 97)));
      } else {
         return c >= '0' && c <= '9' ? new String(Character.toChars(120782 + (c - 48))) : String.valueOf(c);
      }
   }

   private String convertItalic(char c) {
      if (c >= 'A' && c <= 'Z') {
         return new String(Character.toChars(119860 + (c - 65)));
      } else {
         return c >= 'a' && c <= 'z' ? new String(Character.toChars(119886 + (c - 97))) : String.valueOf(c);
      }
   }

   private String convertFullwidth(char c) {
      if (c >= 'A' && c <= 'Z') {
         return String.valueOf((char)('！' + (c - 65)));
      } else if (c >= 'a' && c <= 'z') {
         return String.valueOf((char)('Ａ' + (c - 97)));
      } else if (c >= '0' && c <= '9') {
         return String.valueOf((char)('０' + (c - 48)));
      } else {
         return c == ' ' ? "　" : String.valueOf(c);
      }
   }

   public void onClick() {
      this.toggle();
   }

   public static enum Style {
      DEFAULT("默认", 0),
      DOUBLE_STRUCK("双线体", 1),
      BOLD("粗体", 2),
      ITALIC("斜体", 3),
      FULLWIDTH("全角", 4);

      private final String displayName;
      private final int id;

      private Style(String name, int id) {
         this.displayName = name;
         this.id = id;
      }

      public String toString() {
         return this.displayName;
      }

      public int getId() {
         return this.id;
      }

      public static Style fromId(int id) {
         Style[] var1 = values();
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            Style s = var1[var3];
            if (s.id == id) {
               return s;
            }
         }

         return DEFAULT;
      }

      // $FF: synthetic method
      private static Style[] $values() {
         return new Style[]{DEFAULT, DOUBLE_STRUCK, BOLD, ITALIC, FULLWIDTH};
      }
   }
}
