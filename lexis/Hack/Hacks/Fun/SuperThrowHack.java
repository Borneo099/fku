package lexis.Hack.Hacks.Fun;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.PacketEvent;
import lexis.Hack.events.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class SuperThrowHack extends Hack implements PacketSendListener {
   private static final Set THROWABLES = new HashSet();
   private int packets = 10;
   private HackConfig config = HackConfig.getInstance();
   private static final String CONFIG_KEY = "超远投掷";
   private boolean isProcessing = false;

   public SuperThrowHack() {
      super("超远投掷", new String[]{"在投掷物品 会给加加速投掷物品了 没敌机关枪 和 弓箭伤害加强一样", "§d§l提示：需要在空中 就会加速投掷物品 地面无法 跳跃可以"}, Hack.Category.FUN, true);
      this.addSetting(new Hack.Setting("移动包数量", "发送的移动包数量 给加速投掷物品", 10, 1, 7000, Hack.ValueDisplay.INTEGER));
      this.loadConfig();
   }

   private void loadConfig() {
      this.packets = this.config.getIntSetting("超远投掷", "移动包数量", 10);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting s = (Hack.Setting)var1.next();
         if (s.getName().equals("移动包数量")) {
            s.setValue((double)this.packets);
         }
      }

   }

   private void saveConfig() {
      this.config.saveHackSettings("超远投掷", this.getSettings());
   }

   public void onEnable() {
      EventManager.add(PacketSendListener.class, this);
   }

   public void onDisable() {
      EventManager.remove(PacketSendListener.class, this);
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting s = (Hack.Setting)var2.next();
         if (s.getName().equals("移动包数量")) {
            int newVal = (int)s.getDouble();
            if (newVal != this.packets) {
               this.packets = newVal;
               needSave = true;
            }
            break;
         }
      }

      if (needSave) {
         this.saveConfig();
      }

   }

   public void onPacketSend(PacketEvent.Send event) {
      if (!this.isProcessing) {
         Packet var3 = event.packet;
         if (var3 instanceof ServerboundUseItemPacket) {
            ServerboundUseItemPacket packet = (ServerboundUseItemPacket)var3;
            InteractionHand hand = packet.m_134717_();
            Item item = mc.f_91074_.m_21120_(hand).m_41720_();
            if (THROWABLES.contains(item)) {
               event.cancel();
               this.isProcessing = true;

               try {
                  mc.f_91074_.f_108617_.m_104955_(new ServerboundPlayerCommandPacket(mc.f_91074_, Action.START_SPRINTING));
                  double x = mc.f_91074_.m_20185_();
                  double y = mc.f_91074_.m_20186_();
                  double z = mc.f_91074_.m_20189_();

                  for(int i = 0; i < this.packets; ++i) {
                     mc.f_91074_.f_108617_.m_104955_(new ServerboundMovePlayerPacket.Pos(x, y - 1.0E-10, z, true));
                     mc.f_91074_.f_108617_.m_104955_(new ServerboundMovePlayerPacket.Pos(x, y + 1.0E-10, z, false));
                  }

                  mc.f_91074_.f_108617_.m_104955_(packet);
               } finally {
                  this.isProcessing = false;
               }
            }
         }
      }
   }

   public void onClick() {
      this.toggle();
   }

   static {
      THROWABLES.add(Items.f_42452_);
      THROWABLES.add(Items.f_42521_);
      THROWABLES.add(Items.f_42584_);
      THROWABLES.add(Items.f_42612_);
      THROWABLES.add(Items.f_42736_);
      THROWABLES.add(Items.f_42739_);
   }
}
