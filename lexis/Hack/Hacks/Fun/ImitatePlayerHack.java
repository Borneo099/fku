package lexis.Hack.Hacks.Fun;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(
   modid = "lexis",
   value = {Dist.CLIENT}
)
public class ImitatePlayerHack extends Hack {
   private HackConfig config;
   private static final String CONFIG_KEY = "模仿玩家说话";
   private String targetPlayer = "";
   public static ImitatePlayerHack instance;

   public ImitatePlayerHack() {
      super("模仿玩家说话", new String[]{"自动模仿指定玩家说话", "§c§l警告：千万不要模仿自己 会循环刷屏多 logs文件会给你加内存多"}, Hack.Category.FUN, true);
      this.addSetting(new Hack.Setting("目标玩家", "要模仿的玩家名称", ""));
      this.config = HackConfig.getInstance();
      this.loadConfig();
      instance = this;
      MinecraftForge.EVENT_BUS.register(this);
   }

   private void loadConfig() {
      this.targetPlayer = this.config.getStringSetting("模仿玩家说话", "目标玩家", "");
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("目标玩家")) {
            setting.setValue(this.targetPlayer);
            break;
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
         if (setting.getName().equals("目标玩家")) {
            String newTarget = setting.getString();
            if (!newTarget.equals(this.targetPlayer)) {
               this.targetPlayer = newTarget;
               needSave = true;
            }
            break;
         }
      }

      if (needSave) {
         this.config.saveHackSettings("模仿玩家说话", this.getSettings());
      }

   }

   @SubscribeEvent
   public void onChatReceived(ClientChatReceivedEvent event) {
      if (this.isEnabled()) {
         if (!this.targetPlayer.isEmpty()) {
            String raw = event.getMessage().getString();
            if (raw.startsWith("<")) {
               int end = raw.indexOf(62);
               if (end > 1) {
                  String sender = raw.substring(1, end);
                  if (sender.equalsIgnoreCase(this.targetPlayer)) {
                     String content = raw.substring(end + 1).trim();
                     if (!content.isEmpty()) {
                        mc.execute(() -> {
                           mc.f_91074_.f_108617_.m_246175_(content);
                        });
                     }
                  }
               }
            }

         }
      }
   }

   public String getTargetPlayer() {
      return this.targetPlayer;
   }

   public void onClick() {
      this.toggle();
   }
}
