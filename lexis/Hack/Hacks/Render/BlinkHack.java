package lexis.Hack.Hacks.Render;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.FakePlayer.FakePlayerEntity;
import lexis.Hack.Utils.FakePlayer.FakePlayerManager;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class BlinkHack extends Hack {
   private final ArrayDeque packets = new ArrayDeque();
   private FakePlayerEntity fakePlayer;
   private int limit = 0;
   private boolean isBlocked = false;
   private long restartTime = 0L;
   private static final long RESTART_DELAY = 100L;
   private HackConfig config;
   private static final String CONFIG_KEY = "Blink";

   public BlinkHack() {
      super("Blink", "延迟发送移动数据包", Hack.Category.RENDER, true);
      this.addSetting(new Hack.Setting("限制", "自动重启的最大数据包数量 (0 = 无限制)", 0, 0, 10000, Hack.ValueDisplay.INTEGER));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.limit = this.config.getIntSetting("Blink", "限制", 0);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("限制")) {
            setting.setValue(this.limit);
            break;
         }
      }

   }

   public String getDisplayName() {
      if (this.limit == 0) {
         return "Blink [" + this.packets.size() + "]";
      } else {
         int var10000 = this.packets.size();
         return "Blink [" + var10000 + "/" + this.limit + "]";
      }
   }

   public void onEnable() {
      if (mc.f_91074_ != null) {
         this.fakePlayer = FakePlayerManager.spawnClone();
         this.packets.clear();
         this.isBlocked = false;
      }
   }

   public void onDisable() {
      if (!this.packets.isEmpty()) {
         List packetsToSend = new ArrayList(this.packets);
         this.packets.clear();
         Iterator var2 = packetsToSend.iterator();

         while(var2.hasNext()) {
            ServerboundMovePlayerPacket packet = (ServerboundMovePlayerPacket)var2.next();
            if (mc.f_91074_ != null && mc.f_91074_.f_108617_ != null) {
               mc.f_91074_.f_108617_.m_104955_(packet);
            }
         }
      }

      FakePlayerManager.removeAll();
      this.fakePlayer = null;
      this.isBlocked = false;
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         if (setting.getName().equals("限制")) {
            int newLimit = setting.getInt();
            if (newLimit != this.limit) {
               this.limit = newLimit;
               needSave = true;
            }
            break;
         }
      }

      if (needSave) {
         this.config.saveHackSettings("Blink", this.getSettings());
      }

      if (this.limit > 0 && this.packets.size() >= this.limit && !this.isBlocked) {
         FakePlayerManager.removeAll();
         this.fakePlayer = FakePlayerManager.spawnClone();
         this.isBlocked = true;
         this.restartTime = System.currentTimeMillis() + 100L;
      }

      if (this.isBlocked && System.currentTimeMillis() >= this.restartTime) {
         if (!this.packets.isEmpty()) {
            List packetsToSend = new ArrayList(this.packets);
            this.packets.clear();
            Iterator var6 = packetsToSend.iterator();

            while(var6.hasNext()) {
               ServerboundMovePlayerPacket packet = (ServerboundMovePlayerPacket)var6.next();
               if (mc.f_91074_ != null && mc.f_91074_.f_108617_ != null) {
                  mc.f_91074_.f_108617_.m_104955_(packet);
               }
            }
         }

         this.isBlocked = false;
      }

   }

   public boolean shouldCancelPacket(Packet packet) {
      if (this.isEnabled() && mc.f_91074_ != null && !this.isBlocked) {
         if (packet instanceof ServerboundMovePlayerPacket) {
            this.packets.addLast((ServerboundMovePlayerPacket)packet);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public void onClick() {
      this.toggle();
   }
}
