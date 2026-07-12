package lexis.Hack.Utils.GIF;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.NativeImage.Format;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

public class GifAnimation {
   private final List frames;
   private final long[] frameEndTimes;
   private int currentFrame;
   private long startTime;
   private boolean playing;
   private double speedFactor;

   public GifAnimation(List frames) {
      this(frames, 1.0);
   }

   public GifAnimation(List frames, double speedFactor) {
      this.currentFrame = 0;
      this.startTime = 0L;
      this.playing = true;
      this.speedFactor = 1.0;
      this.frames = frames;
      this.speedFactor = speedFactor;
      this.frameEndTimes = new long[frames.size()];
      long acc = 0L;

      for(int i = 0; i < frames.size(); ++i) {
         long adjustedDelay = (long)((double)((GifDecoder.GifFrame)frames.get(i)).delayMs / speedFactor);
         acc += adjustedDelay;
         this.frameEndTimes[i] = acc;
      }

   }

   public void setSpeedFactor(double factor) {
      if (factor <= 0.0) {
         factor = 1.0;
      }

      this.speedFactor = factor;
      long acc = 0L;

      for(int i = 0; i < this.frames.size(); ++i) {
         long adjustedDelay = (long)((double)((GifDecoder.GifFrame)this.frames.get(i)).delayMs / factor);
         acc += adjustedDelay;
         this.frameEndTimes[i] = acc;
      }

      this.start();
   }

   public void start() {
      this.startTime = System.currentTimeMillis();
      this.currentFrame = 0;
      this.playing = true;
   }

   public void stop() {
      this.playing = false;
   }

   public int getCurrentFrameIndex() {
      if (!this.playing) {
         return this.currentFrame;
      } else {
         if (this.startTime == 0L) {
            this.start();
         }

         long elapsed = System.currentTimeMillis() - this.startTime;
         long total = this.frameEndTimes[this.frameEndTimes.length - 1];
         if (total == 0L) {
            return 0;
         } else {
            long mod = elapsed % total;

            for(int i = 0; i < this.frameEndTimes.length; ++i) {
               if (mod < this.frameEndTimes[i]) {
                  return i;
               }
            }

            return 0;
         }
      }
   }

   public BufferedImage getCurrentFrameImage() {
      return ((GifDecoder.GifFrame)this.frames.get(this.getCurrentFrameIndex())).image;
   }

   public DynamicTexture getCurrentFrameTexture() {
      BufferedImage img = this.getCurrentFrameImage();
      NativeImage nativeImage = convertToNativeImage(img);
      return new DynamicTexture(nativeImage);
   }

   public ResourceLocation getCurrentFrameTextureLocation() {
      DynamicTexture tex = this.getCurrentFrameTexture();
      return Minecraft.m_91087_().m_91097_().m_118490_("gif_frame_" + System.nanoTime(), tex);
   }

   private static NativeImage convertToNativeImage(BufferedImage img) {
      if (img.getType() != 2) {
         BufferedImage c = new BufferedImage(img.getWidth(), img.getHeight(), 2);
         Graphics2D g = c.createGraphics();
         g.drawImage(img, 0, 0, (ImageObserver)null);
         g.dispose();
         img = c;
      }

      int w = img.getWidth();
      int h = img.getHeight();
      NativeImage ni = new NativeImage(Format.RGBA, w, h, false);

      for(int y = 0; y < h; ++y) {
         for(int x = 0; x < w; ++x) {
            int argb = img.getRGB(x, y);
            int a = argb >>> 24 & 255;
            int r = argb >>> 16 & 255;
            int g = argb >>> 8 & 255;
            int b = argb & 255;
            ni.m_84988_(x, y, a << 24 | b << 16 | g << 8 | r);
         }
      }

      return ni;
   }
}
