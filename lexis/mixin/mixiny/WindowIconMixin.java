package lexis.mixin.mixiny;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Minecraft.class})
public class WindowIconMixin {
   @Unique
   private static boolean iconSet = false;
   @Unique
   private static int tickCounter = 0;

   @Inject(
      method = {"runTick"},
      at = {@At("RETURN")}
   )
   private void onRunTick(CallbackInfo ci) {
      if (!iconSet) {
         ++tickCounter;
         if (tickCounter >= 40) {
            Minecraft mc = (Minecraft)this;
            long window = mc.m_91268_().m_85439_();
            if (window != 0L) {
               BufferedImage face = this.loadSkinFace(mc);
               if (face != null) {
                  this.applyIcon(window, face);
                  iconSet = true;
               }
            }
         }
      }
   }

   @Unique
   private BufferedImage loadSkinFace(Minecraft mc) {
      if (this.isNeteaseLoaded()) {
         BufferedImage f = this.loadNeteaseSkinFace(mc);
         if (f != null) {
            return f;
         }
      }

      return this.loadMinotarFace(mc);
   }

   @Unique
   private boolean isNeteaseLoaded() {
      try {
         Class.forName("com.netease.mc.mod.skin.SkinHandler");
         return true;
      } catch (Throwable var2) {
         return false;
      }
   }

   @Unique
   private BufferedImage loadNeteaseSkinFace(Minecraft mc) {
      try {
         String name = mc.m_91094_().m_92546_();
         Class skinHandler = Class.forName("com.netease.mc.mod.skin.SkinHandler");
         Map nameSkinMap = (Map)skinHandler.getField("nameSkinMap").get((Object)null);
         String skinPath = (String)nameSkinMap.get(name);
         if (skinPath == null) {
            return null;
         } else {
            File skinFile = new File(skinPath);
            return !skinFile.exists() ? null : this.extractFace(ImageIO.read(skinFile));
         }
      } catch (Throwable var7) {
         return null;
      }
   }

   @Unique
   private BufferedImage loadMinotarFace(Minecraft mc) {
      try {
         String name = mc.m_91094_().m_92546_();
         URL url = URI.create("https://minotar.net/helm/" + name + "/128.png").toURL();
         InputStream is = url.openStream();

         BufferedImage var5;
         try {
            var5 = ImageIO.read(is);
         } catch (Throwable var8) {
            if (is != null) {
               try {
                  is.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (is != null) {
            is.close();
         }

         return var5;
      } catch (Throwable var9) {
         return null;
      }
   }

   @Unique
   private BufferedImage extractFace(BufferedImage skin) {
      if (skin == null) {
         return null;
      } else {
         int w = skin.getWidth();
         if (w < 16) {
            return skin;
         } else {
            int s = w / 8;
            BufferedImage face = new BufferedImage(s, s, 2);
            Graphics2D g = face.createGraphics();
            g.drawImage(skin, 0, 0, s, s, s, s, s * 2, s * 2, (ImageObserver)null);
            g.drawImage(skin, 0, 0, s, s, s * 5, s, s * 6, s * 2, (ImageObserver)null);
            g.dispose();
            return face;
         }
      }
   }

   @Unique
   private void applyIcon(long window, BufferedImage face) {
      int[] sizes = new int[]{16, 32, 48, 64, 128, 256};
      List images = new ArrayList();
      int[] var6 = sizes;
      int i = sizes.length;

      for(int var8 = 0; var8 < i; ++var8) {
         int size = var6[var8];
         BufferedImage scaled = this.resizeImage(face, size, size);
         ByteBuffer buffer = this.imageToByteBuffer(scaled);
         if (buffer != null) {
            GLFWImage glfwImage = GLFWImage.malloc();
            glfwImage.set(size, size, buffer);
            images.add(glfwImage);
         }
      }

      if (!images.isEmpty()) {
         GLFWImage.Buffer iconBuffer = GLFWImage.malloc(images.size());

         for(i = 0; i < images.size(); ++i) {
            iconBuffer.put(i, (GLFWImage)images.get(i));
         }

         GLFW.glfwSetWindowIcon(window, iconBuffer);
         iconBuffer.free();
         Iterator var14 = images.iterator();

         while(var14.hasNext()) {
            GLFWImage img = (GLFWImage)var14.next();
            img.free();
         }
      }

   }

   @Unique
   private BufferedImage resizeImage(BufferedImage original, int targetWidth, int targetHeight) {
      if (original == null) {
         return null;
      } else {
         BufferedImage resized = new BufferedImage(targetWidth, targetHeight, 2);
         Graphics2D g = resized.createGraphics();
         g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
         g.drawImage(original, 0, 0, targetWidth, targetHeight, (ImageObserver)null);
         g.dispose();
         return resized;
      }
   }

   @Unique
   private ByteBuffer imageToByteBuffer(BufferedImage image) {
      if (image == null) {
         return null;
      } else {
         int w = image.getWidth();
         int h = image.getHeight();
         ByteBuffer buffer = ByteBuffer.allocateDirect(w * h * 4);

         for(int y = 0; y < h; ++y) {
            for(int x = 0; x < w; ++x) {
               int rgb = image.getRGB(x, y);
               buffer.put((byte)(rgb >> 16 & 255));
               buffer.put((byte)(rgb >> 8 & 255));
               buffer.put((byte)(rgb & 255));
               buffer.put((byte)(rgb >> 24 & 255));
            }
         }

         buffer.flip();
         return buffer;
      }
   }
}
