package lexis.Hack.Hacks.Chat;

import java.awt.Color;
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

public class AsciiArtPrinterHack extends Hack {
   private HackConfig config;
   private static final String CONFIG_KEY = "图片打印机";
   private String imagePath = "";
   private int colorThreshold = 128;
   private int charWidth = 30;
   private boolean autoSend = true;
   private int sendSpeed = 5;

   public AsciiArtPrinterHack() {
      super("图片打印机", new String[]{"本地图片转换ASCII艺术字发送到聊天", "§c§l警告：你需要有权限，防止被刷屏踢出"}, Hack.Category.CHAT, false);
      this.addSetting(new Hack.Setting("图片路径", "本地图片完整路径 (如：C:/fu*k_japan/China_yydsnb666.png)", ""));
      this.addSetting(new Hack.Setting("色彩阈值", "颜色转换阈值 (0-255)", 128, 0, 255, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("字符宽度", "每行字符数", 30, 10, 100, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("自动发送", "转换后自动发送到聊天(关闭功能就复制到剪贴板)", true));
      this.addSetting(new Hack.Setting("发送速度", "发送速度 (0=光速, 10=最慢)", 5, 0, 10, Hack.ValueDisplay.INTEGER));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.imagePath = this.config.getStringSetting("图片打印机", "图片路径", "").replace("\\", "/");
      this.colorThreshold = (int)this.config.getDoubleSetting("图片打印机", "色彩阈值", 128.0);
      this.charWidth = (int)this.config.getDoubleSetting("图片打印机", "字符宽度", 30.0);
      this.autoSend = this.config.getBooleanSetting("图片打印机", "自动发送", true);
      this.sendSpeed = (int)this.config.getDoubleSetting("图片打印机", "发送速度", 5.0);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "图片路径":
               setting.setValue(this.imagePath);
               break;
            case "色彩阈值":
               setting.setValue((double)this.colorThreshold);
               break;
            case "字符宽度":
               setting.setValue((double)this.charWidth);
               break;
            case "自动发送":
               setting.setValue(this.autoSend);
               break;
            case "发送速度":
               setting.setValue((double)this.sendSpeed);
         }
      }

   }

   public void onEnable() {
      this.execute();
      this.setEnabled(false);
   }

   private void execute() {
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "图片路径":
               this.imagePath = setting.getString().replace("\\", "/");
               break;
            case "色彩阈值":
               this.colorThreshold = (int)setting.getDouble();
               break;
            case "字符宽度":
               this.charWidth = (int)setting.getDouble();
               break;
            case "自动发送":
               this.autoSend = setting.getBoolean();
               break;
            case "发送速度":
               this.sendSpeed = (int)setting.getDouble();
         }
      }

      if (this.imagePath != null && !this.imagePath.isEmpty()) {
         (new Thread(() -> {
            try {
               List asciiArt = this.convertImageToAscii();
               if (asciiArt != null && !asciiArt.isEmpty()) {
                  if (this.autoSend) {
                     this.sendToChat(asciiArt);
                     NotificationManager.info("图片打印机", "转换成功，已发送到聊天");
                  } else {
                     this.copyToClipboard(asciiArt);
                     NotificationManager.info("图片打印机", "ASCII艺术已复制到剪贴板");
                  }
               } else {
                  NotificationManager.error("图片打印机", "转换失败，请检查图片路径");
               }
            } catch (Exception var2) {
               var2.printStackTrace();
               NotificationManager.error("图片打印机", "抱歉，出错了，请重试一下");
            }

         })).start();
      } else {
         NotificationManager.error("图片打印机", "请先在设置中输入图片路径");
      }
   }

   private List convertImageToAscii() throws Exception {
      List result = new ArrayList();
      File file = new File(this.imagePath);
      if (!file.exists()) {
         NotificationManager.error("图片打印机", "文件不存在: " + this.imagePath);
         return result;
      } else {
         BufferedImage image = ImageIO.read(file);
         if (image == null) {
            NotificationManager.error("图片打印机", "无法读取图片");
            return result;
         } else {
            int brailleWidth = this.charWidth * 2;
            int brailleHeight = (int)((double)brailleWidth * ((double)image.getHeight() / (double)image.getWidth()) * 2.0);
            Image scaled = image.getScaledInstance(brailleWidth, brailleHeight, 4);
            BufferedImage resized = new BufferedImage(brailleWidth, brailleHeight, 1);
            Graphics2D g = resized.createGraphics();
            g.drawImage(scaled, 0, 0, (ImageObserver)null);
            g.dispose();

            for(int y = 0; y < brailleHeight; y += 4) {
               StringBuilder line = new StringBuilder();

               for(int x = 0; x < brailleWidth; x += 2) {
                  int index = 0;
                  Color p8;
                  if (x < brailleWidth && y < brailleHeight) {
                     p8 = new Color(resized.getRGB(x, y));
                     if ((p8.getRed() + p8.getGreen() + p8.getBlue()) / 3 < this.colorThreshold) {
                        index |= 1;
                     }
                  }

                  if (x + 1 < brailleWidth && y < brailleHeight) {
                     p8 = new Color(resized.getRGB(x + 1, y));
                     if ((p8.getRed() + p8.getGreen() + p8.getBlue()) / 3 < this.colorThreshold) {
                        index |= 8;
                     }
                  }

                  if (x < brailleWidth && y + 1 < brailleHeight) {
                     p8 = new Color(resized.getRGB(x, y + 1));
                     if ((p8.getRed() + p8.getGreen() + p8.getBlue()) / 3 < this.colorThreshold) {
                        index |= 2;
                     }
                  }

                  if (x + 1 < brailleWidth && y + 1 < brailleHeight) {
                     p8 = new Color(resized.getRGB(x + 1, y + 1));
                     if ((p8.getRed() + p8.getGreen() + p8.getBlue()) / 3 < this.colorThreshold) {
                        index |= 16;
                     }
                  }

                  if (x < brailleWidth && y + 2 < brailleHeight) {
                     p8 = new Color(resized.getRGB(x, y + 2));
                     if ((p8.getRed() + p8.getGreen() + p8.getBlue()) / 3 < this.colorThreshold) {
                        index |= 4;
                     }
                  }

                  if (x + 1 < brailleWidth && y + 2 < brailleHeight) {
                     p8 = new Color(resized.getRGB(x + 1, y + 2));
                     if ((p8.getRed() + p8.getGreen() + p8.getBlue()) / 3 < this.colorThreshold) {
                        index |= 32;
                     }
                  }

                  if (x < brailleWidth && y + 3 < brailleHeight) {
                     p8 = new Color(resized.getRGB(x, y + 3));
                     if ((p8.getRed() + p8.getGreen() + p8.getBlue()) / 3 < this.colorThreshold) {
                        index |= 64;
                     }
                  }

                  if (x + 1 < brailleWidth && y + 3 < brailleHeight) {
                     p8 = new Color(resized.getRGB(x + 1, y + 3));
                     if ((p8.getRed() + p8.getGreen() + p8.getBlue()) / 3 < this.colorThreshold) {
                        index |= 128;
                     }
                  }

                  line.append((char)(10240 + index));
               }

               result.add(line.toString());
            }

            return result;
         }
      }
   }

   private void sendToChat(List lines) {
      Iterator var2 = lines.iterator();

      while(var2.hasNext()) {
         String line = (String)var2.next();
         if (mc.m_91403_() != null) {
            mc.m_91403_().m_246175_(line);
         }

         if (this.sendSpeed > 0) {
            try {
               Thread.sleep((long)(this.sendSpeed * 50));
            } catch (InterruptedException var5) {
               var5.printStackTrace();
            }
         }
      }

   }

   private void copyToClipboard(List lines) {
      StringBuilder sb = new StringBuilder();
      Iterator var3 = lines.iterator();

      while(var3.hasNext()) {
         String line = (String)var3.next();
         sb.append(line).append("\n");
      }

      mc.f_91068_.m_90911_(sb.toString());
   }

   public void onDisable() {
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

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
            case "色彩阈值":
               int newThreshold = (int)setting.getDouble();
               if (newThreshold != this.colorThreshold) {
                  this.colorThreshold = newThreshold;
                  needSave = true;
               }
               break;
            case "字符宽度":
               int newWidth = (int)setting.getDouble();
               if (newWidth != this.charWidth) {
                  this.charWidth = newWidth;
                  needSave = true;
               }
               break;
            case "自动发送":
               boolean newAuto = setting.getBoolean();
               if (newAuto != this.autoSend) {
                  this.autoSend = newAuto;
                  needSave = true;
               }
               break;
            case "发送速度":
               int newSpeed = (int)setting.getDouble();
               if (newSpeed != this.sendSpeed) {
                  this.sendSpeed = newSpeed;
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.config.saveHackSettings("图片打印机", this.getSettings());
      }

   }

   public void onClick() {
      this.execute();
   }
}
