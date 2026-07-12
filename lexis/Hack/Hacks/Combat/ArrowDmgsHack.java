package lexis.Hack.Hacks.Combat;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class ArrowDmgsHack extends Hack {
   private int packets = 200;
   private boolean yeetTridents = false;
   private HackConfig config;
   private static final String CONFIG_KEY = "弓箭伤害加强";

   public ArrowDmgsHack() {
      super("弓箭伤害加强", "弓箭伤害加强", Hack.Category.COMBAT, true);
      this.addSetting(new Hack.Setting("数据包", "发送的数据包数量", 200, 2, 7000, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("三叉戟模式", "是否使用三叉戟加强？这是加速度快(可以打中)，没有加伤害", false));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.packets = this.config.getIntSetting("弓箭伤害加强", "数据包", 200);
      this.yeetTridents = this.config.getBooleanSetting("弓箭伤害加强", "三叉戟模式", false);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "数据包":
               setting.setValue(this.packets);
               break;
            case "三叉戟模式":
               setting.setValue(this.yeetTridents);
         }
      }

   }

   public String getDisplayName() {
      String var10000 = super.getDisplayName();
      return var10000 + " [" + this.packets + "]";
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void onUpdate() {
      if (this.isEnabled() && mc.f_91074_ != null) {
         boolean needSave = false;
         Iterator var2 = this.getSettings().iterator();

         while(var2.hasNext()) {
            Hack.Setting setting = (Hack.Setting)var2.next();
            switch (setting.getName()) {
               case "数据包":
                  int newPackets = setting.getInt();
                  if (newPackets != this.packets) {
                     this.packets = newPackets;
                     needSave = true;
                  }
                  break;
               case "三叉戟模式":
                  boolean newYeet = setting.getBoolean();
                  if (newYeet != this.yeetTridents) {
                     this.yeetTridents = newYeet;
                     needSave = true;
                  }
            }
         }

         if (needSave) {
            this.config.saveHackSettings("弓箭伤害加强", this.getSettings());
         }

         if (mc.f_91074_.m_6117_()) {
            Item usingItem = mc.f_91074_.m_21205_().m_41720_();
            if (this.isValidItem(usingItem)) {
               this.doArrowDMG();
            }
         }

      }
   }

   public void onClick() {
      this.toggle();
   }

   private void doArrowDMG() {
      mc.f_91074_.f_108617_.m_104955_(new ServerboundPlayerCommandPacket(mc.f_91074_, Action.START_SPRINTING));
      double x = mc.f_91074_.m_20185_();
      double y = mc.f_91074_.m_20186_();
      double z = mc.f_91074_.m_20189_();

      for(int i = 0; i < this.packets / 2; ++i) {
         mc.f_91074_.f_108617_.m_104955_(new ServerboundMovePlayerPacket.Pos(x, y - 1.0E-10, z, true));
         mc.f_91074_.f_108617_.m_104955_(new ServerboundMovePlayerPacket.Pos(x, y + 1.0E-10, z, false));
      }

   }

   private boolean isValidItem(Item item) {
      if (this.yeetTridents && item == Items.f_42713_) {
         return true;
      } else {
         return item == Items.f_42411_;
      }
   }
}
