package lexis.Hack.Hacks.Items;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.NotificationManager;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ArmorStandPrinterHack extends Hack {
   private HackConfig config;
   private static final String CONFIG_KEY = "盔甲架图片打印机";
   private final Object entityLock = new Object();
   private String imagePath = "";
   private int width = 50;
   private int height = 30;
   private double spacing = 1.0;
   private int delayMs = 100;
   private CharMode charMode;

   public ArmorStandPrinterHack() {
      super("盔甲架图片打印机", new String[]{"本地图片转换盔甲架自动放置的", "§c§l注意：仅创造模式可用"}, Hack.Category.ITEMS, false);
      this.charMode = ArmorStandPrinterHack.CharMode.FULL_BLOCK;
      this.addSetting(new Hack.Setting("图片路径", "本地图片完整路径", ""));
      this.addSetting(new Hack.Setting("宽度", "字符宽度", 50, 10, 200, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("高度", "字符高度", 30, 10, 200, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("间距", "盔甲架间距", 1.0, 0.1, 6.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("延迟", "每行延迟(毫秒)", 100, 1, 1000, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("字符样式", "选择显示的字符", "█", new String[]{"█", "▓", "▒", "·", "■", "▪"}));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.imagePath = this.config.getStringSetting("盔甲架图片打印机", "图片路径", "").replace("\\", "/");
      this.width = (int)this.config.getDoubleSetting("盔甲架图片打印机", "宽度", 50.0);
      this.height = (int)this.config.getDoubleSetting("盔甲架图片打印机", "高度", 30.0);
      this.spacing = this.config.getDoubleSetting("盔甲架图片打印机", "间距", 1.0);
      this.delayMs = (int)this.config.getDoubleSetting("盔甲架图片打印机", "延迟", 100.0);
      String modeStr = this.config.getStringSetting("盔甲架图片打印机", "字符样式", "█");
      CharMode[] var2 = ArmorStandPrinterHack.CharMode.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         CharMode mode = var2[var4];
         if (mode.getSymbol().equals(modeStr)) {
            this.charMode = mode;
            break;
         }
      }

      Iterator var6 = this.getSettings().iterator();

      while(var6.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var6.next();
         switch (setting.getName()) {
            case "图片路径":
               setting.setValue(this.imagePath);
               break;
            case "宽度":
               setting.setValue((double)this.width);
               break;
            case "高度":
               setting.setValue((double)this.height);
               break;
            case "间距":
               setting.setValue(this.spacing);
               break;
            case "延迟":
               setting.setValue((double)this.delayMs);
               break;
            case "字符样式":
               setting.setValue(this.charMode.getSymbol());
         }
      }

   }

   public void onEnable() {
      this.execute();
      this.setEnabled(false);
   }

   private void execute() {
      Iterator var1 = this.getSettings().iterator();

      while(true) {
         while(true) {
            while(var1.hasNext()) {
               Hack.Setting setting = (Hack.Setting)var1.next();
               switch (setting.getName()) {
                  case "图片路径":
                     this.imagePath = setting.getString().replace("\\", "/");
                     break;
                  case "宽度":
                     this.width = (int)setting.getDouble();
                     break;
                  case "高度":
                     this.height = (int)setting.getDouble();
                     break;
                  case "间距":
                     this.spacing = setting.getDouble();
                     break;
                  case "延迟":
                     this.delayMs = (int)setting.getDouble();
                     break;
                  case "字符样式":
                     String modeStr = setting.getString();
                     CharMode[] var6 = ArmorStandPrinterHack.CharMode.values();
                     int var7 = var6.length;

                     for(int var8 = 0; var8 < var7; ++var8) {
                        CharMode mode = var6[var8];
                        if (mode.getSymbol().equals(modeStr)) {
                           this.charMode = mode;
                           break;
                        }
                     }
               }
            }

            Minecraft mc = Minecraft.m_91087_();
            if (mc.f_91074_ == null) {
               return;
            }

            if (!mc.f_91074_.m_150110_().f_35937_) {
               NotificationManager.error("盔甲架图片打印机", "仅创造模式可用！");
               return;
            }

            if (this.imagePath != null && !this.imagePath.isEmpty()) {
               (new Thread(() -> {
                  try {
                     File file = new File(this.imagePath);
                     if (!file.exists()) {
                        NotificationManager.error("盔甲架图片打印机", "文件不存在: " + this.imagePath);
                        return;
                     }

                     BufferedImage image = ImageIO.read(file);
                     if (image == null) {
                        NotificationManager.error("盔甲架图片打印机", "无法读取图片");
                        return;
                     }

                     Image scaled = image.getScaledInstance(this.width, this.height, 4);
                     BufferedImage resized = new BufferedImage(this.width, this.height, 1);
                     Graphics2D g = resized.createGraphics();
                     g.translate(0, this.height);
                     g.scale(1.0, -1.0);
                     g.drawImage(scaled, 0, 0, (ImageObserver)null);
                     g.dispose();
                     List jsonLines = new ArrayList();
                     String symbol = this.charMode.getSymbol();

                     for(int y = 0; y < this.height; ++y) {
                        StringBuilder lineJson = new StringBuilder("[");

                        for(int x = 0; x < this.width; ++x) {
                           int rgb = resized.getRGB(x, y);
                           String colorHex = String.format("#%06X", rgb & 16777215);
                           lineJson.append("{\"color\":\"").append(colorHex).append("\",\"text\":\"").append(symbol).append("\"}");
                           if (x < this.width - 1) {
                              lineJson.append(",");
                           }
                        }

                        lineJson.append("]");
                        jsonLines.add(lineJson.toString());
                     }

                     this.placeArmorStands(jsonLines);
                     NotificationManager.info("盔甲架图片打印机", "放置完成！共 " + this.height + " 行，使用字符: " + symbol);
                  } catch (Exception var13) {
                     var13.printStackTrace();
                     NotificationManager.error("盔甲架图片打印机", "抱歉，出错了，请重试一下");
                  }

               })).start();
               return;
            }

            NotificationManager.error("盔甲架图片打印机", "请先在设置中输入图片路径");
            return;
         }
      }
   }

   private void placeArmorStands(List jsonLines) {
      Minecraft mc = Minecraft.m_91087_();
      if (mc.f_91074_ != null && mc.m_91403_() != null) {
         Vec3 pos = mc.f_91074_.m_20182_();
         double startX = pos.f_82479_;
         double startY = pos.f_82480_;
         double startZ = pos.f_82481_;
         (new Thread(() -> {
            for(int y = 0; y < jsonLines.size(); ++y) {
               String lineJson = (String)jsonLines.get(y);
               double posY = startY + (double)y * this.spacing;
               double posX = startX;
               double posZ = startZ;
               synchronized(this.entityLock) {
                  try {
                     String nbt = String.format("{id:bat_spawn_egg,Count:1b,tag:{EntityTag:{id:\"minecraft:armor_stand\",CustomName:'%s',CustomNameVisible:1b,NoGravity:1b,Small:1b,NoBasePlate:1b,Invisible:1b,Pos:[%fd,%fd,%fd]}}}", lineJson, posX, posY, posZ);
                     CompoundTag tag = TagParser.m_129359_(nbt);
                     ItemStack eggStack = ItemStack.m_41712_(tag);
                     int slot = mc.f_91074_.m_150109_().f_35977_;
                     int serverSlot = slot < 9 ? 36 + slot : slot;
                     mc.m_91403_().m_104955_(new ServerboundSetCreativeModeSlotPacket(serverSlot, eggStack));
                     BlockPos placePos = new BlockPos((int)Math.floor(posX), (int)Math.floor(posY), (int)Math.floor(posZ));
                     BlockHitResult hitResult = new BlockHitResult(new Vec3(posX, posY, posZ), Direction.UP, placePos, false);
                     mc.m_91403_().m_104955_(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, hitResult, 0));
                  } catch (Exception var27) {
                     var27.printStackTrace();
                  }
               }

               try {
                  Thread.sleep((long)this.delayMs);
               } catch (InterruptedException var26) {
               }
            }

         })).start();
      }
   }

   public void onDisable() {
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(true) {
         while(true) {
            while(var2.hasNext()) {
               Hack.Setting setting = (Hack.Setting)var2.next();
               switch (setting.getName()) {
                  case "图片路径":
                     String newPath = setting.getString().replace("\\", "/");
                     if (!newPath.equals(this.imagePath)) {
                        this.imagePath = newPath;
                        needSave = true;
                     }
                     break;
                  case "宽度":
                     int newWidth = (int)setting.getDouble();
                     if (newWidth != this.width) {
                        this.width = newWidth;
                        needSave = true;
                     }
                     break;
                  case "高度":
                     int newHeight = (int)setting.getDouble();
                     if (newHeight != this.height) {
                        this.height = newHeight;
                        needSave = true;
                     }
                     break;
                  case "间距":
                     double newSpacing = setting.getDouble();
                     if (newSpacing != this.spacing) {
                        this.spacing = newSpacing;
                        needSave = true;
                     }
                     break;
                  case "延迟":
                     int newDelay = (int)setting.getDouble();
                     if (newDelay != this.delayMs) {
                        this.delayMs = newDelay;
                        needSave = true;
                     }
                     break;
                  case "字符样式":
                     String newMode = setting.getString();
                     CharMode[] var13 = ArmorStandPrinterHack.CharMode.values();
                     int var14 = var13.length;

                     for(int var15 = 0; var15 < var14; ++var15) {
                        CharMode mode = var13[var15];
                        if (mode.getSymbol().equals(newMode) && this.charMode != mode) {
                           this.charMode = mode;
                           needSave = true;
                           break;
                        }
                     }
               }
            }

            if (needSave) {
               this.config.saveHackSettings("盔甲架图片打印机", this.getSettings());
            }

            return;
         }
      }
   }

   public void onClick() {
      this.execute();
   }

   public static enum CharMode {
      FULL_BLOCK("█"),
      MEDIUM_BLOCK("▓"),
      LIGHT_BLOCK("▒"),
      DOT("·"),
      SQUARE("■"),
      DARK_SQUARE("▪");

      private final String symbol;

      private CharMode(String symbol) {
         this.symbol = symbol;
      }

      public String getSymbol() {
         return this.symbol;
      }

      public String toString() {
         return this.symbol;
      }

      // $FF: synthetic method
      private static CharMode[] $values() {
         return new CharMode[]{FULL_BLOCK, MEDIUM_BLOCK, LIGHT_BLOCK, DOT, SQUARE, DARK_SQUARE};
      }
   }
}
