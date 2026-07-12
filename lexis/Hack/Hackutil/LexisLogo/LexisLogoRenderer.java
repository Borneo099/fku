package lexis.Hack.Hackutil.LexisLogo;

import com.mojang.blaze3d.platform.NativeImage;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Lexis.LexisLogoHack;
import lexis.Hack.Hackutil.HackManager;
import lexis.Hack.Hackutil.config.LexisLogoConfig;
import lexis.Hack.Utils.GIF.GifAnimation;
import lexis.Hack.Utils.GIF.GifDecoder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class LexisLogoRenderer {
   private static final LexisLogoConfig CONFIG = LexisLogoConfig.getInstance();
   private static final ResourceLocation STATIC_LOGO = new ResourceLocation("lexis", "textures/dcc/lexis.png");
   private static final ResourceLocation GIF_LOGO = new ResourceLocation("lexis", "textures/dcc/lxaf/lxaf.gif");
   private static GifAnimation gifAnimation = null;
   private static List frameTextures = null;
   private static boolean gifLoaded = false;
   private static boolean loadingStarted = false;
   private static LexisLogoHack cachedLogoHack = null;
   private static long lastGlitchBuild = 0L;
   private static final long GLITCH_REBUILD_INTERVAL = 60L;
   private static DynamicTexture textTexture = null;
   private static ResourceLocation textTextureLoc = null;
   private static int textWidth = 0;
   private static int textHeight = 0;
   private static int lastTextColor = -1;
   private static final String FONT_NAME = "Microsoft YaHei";
   private static final int FONT_SIZE = 18;
   private static final boolean ITALIC = true;
   private static final int SHADOW_OFFSET = 1;
   private static boolean cherryModDetected = false;
   private static boolean hasCheckedMods = false;
   private static final Random RNG = new Random();
   private static final long GLITCH_INTERVAL = 4500L;
   private static final long GLITCH_DURATION = 800L;
   private static long lastGlitchTime = 0L;
   private static boolean inGlitch = false;
   private static long glitchStart = 0L;
   private static DynamicTexture glitchTexture = null;
   private static ResourceLocation glitchTexLoc = null;
   private static int glitchW = 0;
   private static int glitchH = 0;
   private static int glitchFrameCount = 0;
   private static final String ERROR_CHARS = "█▓▒░▌▀▄■□▪▫▬▲►▼◄▶◆◇○●◘◙◈♠♣♥♦♀♂☠☢☣⚠⚡⛔⛏✖✗✘☠⚠";
   private static final String HEX_CHARS = "0123456789ABCDEF";

   private static void rebuildTextTexture() {
      Minecraft mc = Minecraft.m_91087_();
      if (mc != null && mc.f_90987_ != null) {
         String text = "Lexis v" + LexisLogoConfig.getModVersion();
         String subText = "LX-1.20.1-Lexis Plus";
         Font baseFont = new Font("Microsoft YaHei", 1, 18);
         Font font = baseFont.deriveFont(2);
         BufferedImage tmp = new BufferedImage(1, 1, 2);
         Graphics2D g2d = tmp.createGraphics();
         g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         FontRenderContext frc = g2d.getFontRenderContext();
         Rectangle2D textBounds = font.getStringBounds(text, frc);
         Rectangle2D subBounds = font.getStringBounds(subText, frc);
         g2d.dispose();
         int textW = (int)Math.ceil(Math.max(textBounds.getWidth(), subBounds.getWidth())) + 1 + 8;
         int textH = (int)Math.ceil(textBounds.getHeight() + subBounds.getHeight()) + 1 + 8;
         BufferedImage img = new BufferedImage(textW, textH, 2);
         Graphics2D g = img.createGraphics();
         g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         g.setFont(font);
         g.setColor(new Color(1140850688, true));
         g.drawString(text, 1.0F, (float)textBounds.getHeight() + 1.0F);
         g.drawString(subText, 1.0F, (float)(textBounds.getHeight() + subBounds.getHeight()) + 1.0F);
         int mainColor = CONFIG.getTextColor();
         g.setColor(new Color(mainColor, true));
         g.drawString(text, 0.0F, (float)textBounds.getHeight());
         g.drawString(subText, 0.0F, (float)(textBounds.getHeight() + subBounds.getHeight()));
         g.dispose();
         NativeImage nativeImage = convertToNativeImage(img);
         if (textTexture != null) {
            textTexture.close();
         }

         textTexture = new DynamicTexture(nativeImage);
         if (textTextureLoc == null) {
            textTextureLoc = new ResourceLocation("lexis", "logo_text");
         }

         mc.f_90987_.m_118495_(textTextureLoc, textTexture);
         textWidth = textW;
         textHeight = textH;
         lastTextColor = mainColor;
      }
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
      NativeImage ni = new NativeImage(w, h, false);

      for(int y = 0; y < h; ++y) {
         for(int x = 0; x < w; ++x) {
            int argb = img.getRGB(x, y);
            int a = argb >>> 24 & 255;
            int r = argb >>> 16 & 255;
            int g = argb >>> 8 & 255;
            int b = argb & 255;
            int abgr = a << 24 | b << 16 | g << 8 | r;
            ni.m_84988_(x, y, abgr);
         }
      }

      return ni;
   }

   private static void checkGlitch() {
      long now = System.currentTimeMillis();
      if (!inGlitch && now - lastGlitchTime >= 4500L) {
         inGlitch = true;
         glitchStart = now;
         lastGlitchTime = now;
         glitchFrameCount = 0;
      }

      if (inGlitch && now - glitchStart > 800L) {
         inGlitch = false;
      }

   }

   private static void buildGlitchFrame() {
      Minecraft mc = Minecraft.m_91087_();
      if (mc != null && mc.f_90987_ != null) {
         String text = "Lexis v" + LexisLogoConfig.getModVersion();
         String subText = "LX-1.20.1-Lexis Plus";
         String glitchedText = corruptText(text);
         String glitchedSub = corruptText(subText);
         Font baseFont = new Font("Microsoft YaHei", 1, 18);
         Font font = baseFont.deriveFont(2);
         BufferedImage tmp = new BufferedImage(1, 1, 2);
         Graphics2D g2d = tmp.createGraphics();
         g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         FontRenderContext frc = g2d.getFontRenderContext();
         Rectangle2D textBounds = font.getStringBounds(text, frc);
         Rectangle2D subBounds = font.getStringBounds(subText, frc);
         g2d.dispose();
         int w = (int)Math.ceil(Math.max(textBounds.getWidth(), subBounds.getWidth())) + 1 + 8;
         int h = (int)Math.ceil(textBounds.getHeight() + subBounds.getHeight()) + 1 + 8;
         BufferedImage img = new BufferedImage(w, h, 2);
         Graphics2D g = img.createGraphics();
         g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         g.setFont(font);
         int mainColor = CONFIG.getTextColor();
         g.setColor(new Color(1140850688, true));
         g.drawString(glitchedText, 1.0F, (float)textBounds.getHeight() + 1.0F);
         g.drawString(glitchedSub, 1.0F, (float)(textBounds.getHeight() + subBounds.getHeight()) + 1.0F);
         g.setColor(new Color(mainColor, true));
         g.drawString(glitchedText, 0.0F, (float)textBounds.getHeight());
         g.drawString(glitchedSub, 0.0F, (float)(textBounds.getHeight() + subBounds.getHeight()));
         g.dispose();
         applyPixelGlitch(img);
         NativeImage nativeImage = convertToNativeImage(img);
         if (glitchTexture != null) {
            glitchTexture.close();
         }

         glitchTexture = new DynamicTexture(nativeImage);
         if (glitchTexLoc == null) {
            glitchTexLoc = new ResourceLocation("lexis", "logo_glitch");
         }

         mc.f_90987_.m_118495_(glitchTexLoc, glitchTexture);
         glitchW = w;
         glitchH = h;
         ++glitchFrameCount;
      }
   }

   private static String corruptText(String s) {
      StringBuilder sb = new StringBuilder(s);
      int swaps = 1 + RNG.nextInt(sb.length() / 2);

      for(int i = 0; i < swaps; ++i) {
         int idx = RNG.nextInt(sb.length());
         if (RNG.nextBoolean()) {
            sb.setCharAt(idx, "█▓▒░▌▀▄■□▪▫▬▲►▼◄▶◆◇○●◘◙◈♠♣♥♦♀♂☠☢☣⚠⚡⛔⛏✖✗✘☠⚠".charAt(RNG.nextInt("█▓▒░▌▀▄■□▪▫▬▲►▼◄▶◆◇○●◘◙◈♠♣♥♦♀♂☠☢☣⚠⚡⛔⛏✖✗✘☠⚠".length())));
         } else {
            sb.setCharAt(idx, "0123456789ABCDEF".charAt(RNG.nextInt("0123456789ABCDEF".length())));
         }
      }

      return sb.toString();
   }

   private static void applyPixelGlitch(BufferedImage img) {
      int w = img.getWidth();
      int h = img.getHeight();
      int tearCount = 3 + RNG.nextInt(6);

      int splitCount;
      int noiseCount;
      int i;
      int nx;
      int row;
      int x;
      int srcX;
      for(splitCount = 0; splitCount < tearCount; ++splitCount) {
         noiseCount = RNG.nextInt(h);
         i = 1 + RNG.nextInt(4);
         nx = -w / 4 + RNG.nextInt(w / 2);

         for(row = noiseCount; row < Math.min(noiseCount + i, h); ++row) {
            int[] rowPixels = new int[w];

            for(x = 0; x < w; ++x) {
               rowPixels[x] = img.getRGB(x, row);
            }

            for(x = 0; x < w; ++x) {
               srcX = x - nx;
               if (srcX < 0) {
                  srcX += w;
               }

               if (srcX >= w) {
                  srcX -= w;
               }

               img.setRGB(x, row, rowPixels[srcX]);
            }
         }
      }

      splitCount = 1 + RNG.nextInt(3);

      int dy;
      int dx;
      int nw;
      for(noiseCount = 0; noiseCount < splitCount; ++noiseCount) {
         i = RNG.nextInt(h);
         nx = Math.min(i + 2 + RNG.nextInt(6), h);
         row = -3 + RNG.nextInt(7);
         nw = -3 + RNG.nextInt(7);
         int[] orig = new int[w];

         for(srcX = i; srcX < nx; ++srcX) {
            for(dy = 0; dy < w; ++dy) {
               orig[dy] = img.getRGB(dy, srcX);
            }

            for(dy = 0; dy < w; ++dy) {
               dx = orig[dy] >> 24 & 255;
               int r = orig[clamp(dy - row, w)] >> 16 & 255;
               int g = orig[dy] >> 8 & 255;
               int b = orig[clamp(dy - nw, w)] & 255;
               img.setRGB(dy, srcX, dx << 24 | r << 16 | g << 8 | b);
            }
         }
      }

      noiseCount = 5 + RNG.nextInt(20);

      for(i = 0; i < noiseCount; ++i) {
         nx = RNG.nextInt(w);
         row = RNG.nextInt(h);
         nw = 1 + RNG.nextInt(5);
         x = 1 + RNG.nextInt(3);
         srcX = -16777216 | RNG.nextInt(256) << 16 | RNG.nextInt(256) << 8 | RNG.nextInt(256);

         for(dy = 0; dy < x && row + dy < h; ++dy) {
            for(dx = 0; dx < nw && nx + dx < w; ++dx) {
               img.setRGB(nx + dx, row + dy, srcX);
            }
         }
      }

   }

   private static int clamp(int v, int max) {
      if (v < 0) {
         return 0;
      } else {
         return v >= max ? max - 1 : v;
      }
   }

   private static void startLoadGif() {
      if (!loadingStarted) {
         loadingStarted = true;
         CompletableFuture.runAsync(() -> {
            try {
               Optional optional = Minecraft.m_91087_().m_91098_().m_213713_(GIF_LOGO);
               if (!optional.isPresent()) {
                  System.err.println("[LexisLogo] GIF not found.");
                  return;
               }

               InputStream inputStream = ((Resource)optional.get()).m_215507_();

               try {
                  List frames = GifDecoder.decode(inputStream);
                  if (frames != null && !frames.isEmpty()) {
                     double speed = 1.8;
                     gifAnimation = new GifAnimation(frames, speed);
                     gifAnimation.start();
                     frameTextures = new ArrayList();

                     for(int i = 0; i < frames.size(); ++i) {
                        NativeImage nativeImage = convertToNativeImage(((GifDecoder.GifFrame)frames.get(i)).image);
                        DynamicTexture texture = new DynamicTexture(nativeImage);
                        ResourceLocation loc = Minecraft.m_91087_().m_91097_().m_118490_("lexis_logo_frame_" + i, texture);
                        frameTextures.add(loc);
                     }

                     gifLoaded = true;
                  }
               } catch (Throwable var10) {
                  if (inputStream != null) {
                     try {
                        inputStream.close();
                     } catch (Throwable var9) {
                        var10.addSuppressed(var9);
                     }
                  }

                  throw var10;
               }

               if (inputStream != null) {
                  inputStream.close();
               }
            } catch (Exception var11) {
               var11.printStackTrace();
               gifLoaded = false;
            }

         });
      }
   }

   @SubscribeEvent
   public static void onRenderGui(RenderGuiEvent.Post event) {
      Minecraft mc = Minecraft.m_91087_();
      if (!mc.f_91066_.f_92063_) {
         if (cachedLogoHack == null) {
            Iterator var2 = HackManager.getInstance().getHacks().iterator();

            while(var2.hasNext()) {
               Hack h = (Hack)var2.next();
               if (h instanceof LexisLogoHack) {
                  cachedLogoHack = (LexisLogoHack)h;
                  break;
               }
            }
         }

         if (cachedLogoHack != null && cachedLogoHack.isEnabled()) {
            LexisLogoHack hack = cachedLogoHack;
            if (!hasCheckedMods) {
               cherryModDetected = ModList.get().isLoaded("karucn");
               hasCheckedMods = true;
            }

            GuiGraphics gui = event.getGuiGraphics();
            int logoWidth = 120;
            int logoHeight = 48;
            int xPos = 5;
            int yPos = cherryModDetected ? 70 : 5;
            int bgColor = CONFIG.getBackgroundColor();
            int currentColor = CONFIG.getTextColor();
            if (textTextureLoc == null || currentColor != lastTextColor) {
               rebuildTextTexture();
            }

            int totalWidth = logoWidth + textWidth + 8;
            if (bgColor >> 24 != 0) {
               gui.m_280509_(xPos, yPos, xPos + totalWidth, yPos + logoHeight, bgColor);
            }

            int textX;
            if (hack.getMode() == LexisLogoHack.LogoMode.GIF) {
               if (!gifLoaded && !loadingStarted) {
                  startLoadGif();
               }

               if (gifLoaded && frameTextures != null && !frameTextures.isEmpty()) {
                  textX = gifAnimation.getCurrentFrameIndex() % frameTextures.size();
                  gui.m_280163_((ResourceLocation)frameTextures.get(textX), xPos, yPos, 0.0F, 0.0F, logoWidth, logoHeight, logoWidth, logoHeight);
               } else {
                  gui.m_280163_(STATIC_LOGO, xPos, yPos, 0.0F, 0.0F, logoWidth, logoHeight, logoWidth, logoHeight);
               }
            } else {
               gui.m_280163_(STATIC_LOGO, xPos, yPos, 0.0F, 0.0F, logoWidth, logoHeight, logoWidth, logoHeight);
            }

            checkGlitch();
            textX = xPos + logoWidth + 4;
            int textY = yPos + (logoHeight - textHeight) / 2;
            if (inGlitch) {
               long nowMs = System.currentTimeMillis();
               if (glitchTexLoc == null || nowMs - lastGlitchBuild >= 60L) {
                  buildGlitchFrame();
                  lastGlitchBuild = nowMs;
               }

               if (glitchTexLoc != null) {
                  int gy = yPos + (logoHeight - glitchH) / 2;
                  gui.m_280163_(glitchTexLoc, textX, gy, 0.0F, 0.0F, glitchW, glitchH, glitchW, glitchH);
               }
            } else if (textTextureLoc != null) {
               gui.m_280163_(textTextureLoc, textX, textY, 0.0F, 0.0F, textWidth, textHeight, textWidth, textHeight);
            }

         }
      }
   }

   static {
      rebuildTextTexture();
   }
}
