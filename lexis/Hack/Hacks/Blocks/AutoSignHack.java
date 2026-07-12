package lexis.Hack.Hacks.Blocks;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.mixin.accessor.AbstractSignEditScreenAccessor;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AutoSignHack extends Hack {
   private String line1 = "<player>";
   private String line2 = "Lexis Client!";
   private String line3 = "<------------->";
   private String line4 = "<date>";
   private String dateFormat = "dd/MM/yyyy";
   private HackConfig config = HackConfig.getInstance();
   private static final String CONFIG_KEY = "自动告示牌";

   public AutoSignHack() {
      super("自动告示牌", "打开告示牌时自动填写预设文本", Hack.Category.BLOCKS, true);
      this.addSetting(new Hack.Setting("第一行", "告示牌第一行文本", this.line1));
      this.addSetting(new Hack.Setting("第二行", "告示牌第二行文本", this.line2));
      this.addSetting(new Hack.Setting("第三行", "告示牌第三行文本", this.line3));
      this.addSetting(new Hack.Setting("第四行", "告示牌第四行文本", this.line4));
      this.addSetting(new Hack.Setting("日期格式", "日期格式(用于<date>占位符)", this.dateFormat));
      this.loadConfig();
   }

   private void loadConfig() {
      this.line1 = this.config.getStringSetting("自动告示牌", "第一行", "<player>");
      this.line2 = this.config.getStringSetting("自动告示牌", "第二行", "Lexis Client!");
      this.line3 = this.config.getStringSetting("自动告示牌", "第三行", "<------------->");
      this.line4 = this.config.getStringSetting("自动告示牌", "第四行", "<date>");
      this.dateFormat = this.config.getStringSetting("自动告示牌", "日期格式", "dd/MM/yyyy");
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting s = (Hack.Setting)var1.next();
         switch (s.getName()) {
            case "第一行":
               s.setValue(this.line1);
               break;
            case "第二行":
               s.setValue(this.line2);
               break;
            case "第三行":
               s.setValue(this.line3);
               break;
            case "第四行":
               s.setValue(this.line4);
               break;
            case "日期格式":
               s.setValue(this.dateFormat);
         }
      }

   }

   private void saveConfig() {
      this.config.saveHackSettings("自动告示牌", this.getSettings());
   }

   public void onEnable() {
      MinecraftForge.EVENT_BUS.register(this);
   }

   public void onDisable() {
      MinecraftForge.EVENT_BUS.unregister(this);
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting s = (Hack.Setting)var2.next();
         switch (s.getName()) {
            case "第一行":
               String l1 = s.getString();
               if (!l1.equals(this.line1)) {
                  this.line1 = l1;
                  needSave = true;
               }
               break;
            case "第二行":
               String l2 = s.getString();
               if (!l2.equals(this.line2)) {
                  this.line2 = l2;
                  needSave = true;
               }
               break;
            case "第三行":
               String l3 = s.getString();
               if (!l3.equals(this.line3)) {
                  this.line3 = l3;
                  needSave = true;
               }
               break;
            case "第四行":
               String l4 = s.getString();
               if (!l4.equals(this.line4)) {
                  this.line4 = l4;
                  needSave = true;
               }
               break;
            case "日期格式":
               String df = s.getString();
               if (!df.equals(this.dateFormat)) {
                  this.dateFormat = df;
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.saveConfig();
      }

   }

   @SubscribeEvent
   public void onScreenOpen(ScreenEvent.Opening event) {
      if (this.isEnabled() && mc.f_91074_ != null) {
         if (event.getScreen() instanceof SignEditScreen) {
            SignEditScreen signScreen = (SignEditScreen)event.getScreen();
            SignBlockEntity sign = ((AbstractSignEditScreenAccessor)signScreen).getSign();
            if (sign != null) {
               event.setCanceled(true);
               String[] lines = new String[]{this.formatText(this.line1), this.formatText(this.line2), this.formatText(this.line3), this.formatText(this.line4)};
               mc.f_91074_.f_108617_.m_104955_(new ServerboundSignUpdatePacket(sign.m_58899_(), true, lines[0], lines[1], lines[2], lines[3]));
            }
         }
      }
   }

   private String formatText(String text) {
      String result = text.replace("<player>", mc.m_91094_().m_92546_());
      if (text.contains("<date>")) {
         try {
            SimpleDateFormat sdf = new SimpleDateFormat(this.dateFormat);
            result = result.replace("<date>", sdf.format(new Date()));
         } catch (Exception var4) {
            result = result.replace("<date>", "DATE_ERROR");
         }
      }

      return result;
   }

   public void onClick() {
      this.toggle();
   }
}
