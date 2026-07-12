package lexis.Hack.Hacks.Chat;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.NotificationManager;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

public class PlayerNotifierHack extends Hack {
   private static final Minecraft mc = Minecraft.m_91087_();
   private HackConfig config;
   private static final String CONFIG_KEY = "玩家通知";
   private NotifyMode mode;
   private final Set currentPlayers;
   private boolean initialized;

   public PlayerNotifierHack() {
      super("玩家通知", new String[]{"应该是美化玩家通知 退出/加入消息效果"}, Hack.Category.CHAT, true);
      this.mode = PlayerNotifierHack.NotifyMode.CHAT;
      this.currentPlayers = new HashSet();
      this.initialized = false;
      this.addSetting(new Hack.Setting("通知模式", "选择通知方式", "聊天模式", new String[]{"聊天模式", "通知系统"}));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      String modeStr = this.config.getStringSetting("玩家通知", "通知模式", "聊天模式");
      NotifyMode[] var2 = PlayerNotifierHack.NotifyMode.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         NotifyMode m = var2[var4];
         if (m.toString().equals(modeStr)) {
            this.mode = m;
            break;
         }
      }

      Iterator var6 = this.getSettings().iterator();

      while(var6.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var6.next();
         if (setting.getName().equals("通知模式")) {
            setting.setValue(this.mode.toString());
            break;
         }
      }

   }

   public void onEnable() {
      this.initialized = false;
      this.currentPlayers.clear();
   }

   public void onDisable() {
      this.currentPlayers.clear();
      this.initialized = false;
   }

   public void onUpdate() {
      Iterator var1 = this.getSettings().iterator();

      String player;
      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("通知模式")) {
            player = setting.getString();
            NotifyMode newNotifyMode = null;
            NotifyMode[] var5 = PlayerNotifierHack.NotifyMode.values();
            int var6 = var5.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               NotifyMode m = var5[var7];
               if (m.toString().equals(player)) {
                  newNotifyMode = m;
                  break;
               }
            }

            if (newNotifyMode != null && newNotifyMode != this.mode) {
               this.mode = newNotifyMode;
               this.config.saveHackSettings("玩家通知", this.getSettings());
            }
            break;
         }
      }

      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         Set newPlayers = new HashSet();
         Iterator var10;
         if (mc.f_91074_.f_108617_ != null) {
            var10 = mc.f_91074_.f_108617_.m_105142_().iterator();

            while(var10.hasNext()) {
               PlayerInfo playerInfo = (PlayerInfo)var10.next();
               if (playerInfo != null && playerInfo.m_105312_() != null) {
                  String playerName = playerInfo.m_105312_().getName();
                  if (playerName != null && !playerName.isEmpty()) {
                     newPlayers.add(playerName);
                  }
               }
            }
         }

         if (!this.initialized) {
            this.currentPlayers.addAll(newPlayers);
            this.initialized = true;
         } else {
            var10 = newPlayers.iterator();

            while(var10.hasNext()) {
               player = (String)var10.next();
               if (!this.currentPlayers.contains(player) && !player.equals(mc.f_91074_.m_7755_().getString())) {
                  if (this.mode == PlayerNotifierHack.NotifyMode.CHAT) {
                     mc.f_91065_.m_93076_().m_93785_(Component.m_237113_("§7[§a+§7] §f" + player));
                  } else {
                     NotificationManager.info("玩家通知", player + " §a加入了游戏", 3);
                  }
               }
            }

            var10 = this.currentPlayers.iterator();

            while(var10.hasNext()) {
               player = (String)var10.next();
               if (!newPlayers.contains(player) && !player.equals(mc.f_91074_.m_7755_().getString())) {
                  if (this.mode == PlayerNotifierHack.NotifyMode.CHAT) {
                     mc.f_91065_.m_93076_().m_93785_(Component.m_237113_("§7[§c-§7] §f" + player));
                  } else {
                     NotificationManager.warning("玩家通知", player + " §c退出了游戏", 3);
                  }
               }
            }

            this.currentPlayers.clear();
            this.currentPlayers.addAll(newPlayers);
         }
      } else {
         this.currentPlayers.clear();
         this.initialized = false;
      }
   }

   public void onClick() {
      this.toggle();
   }

   public static enum NotifyMode {
      CHAT("聊天模式"),
      NOTIFICATION("通知系统");

      private final String displayName;

      private NotifyMode(String name) {
         this.displayName = name;
      }

      public String toString() {
         return this.displayName;
      }

      // $FF: synthetic method
      private static NotifyMode[] $values() {
         return new NotifyMode[]{CHAT, NOTIFICATION};
      }
   }
}
