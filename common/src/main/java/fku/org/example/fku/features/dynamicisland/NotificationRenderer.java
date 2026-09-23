package fku.org.example.fku.features.dynamicisland;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class NotificationRenderer {
   private static long shakeStartMs = 0L;
   private static float shakeMag = 0.0F;

   public static void pulseShake(float mag) {
      shakeMag = Math.max(shakeMag, mag);
      shakeStartMs = System.currentTimeMillis();
   }

   private static Motion computeMotion(DynamicIslandConfig cfg, ProgressProvider p, IslandNotification n, long now) {
      float dx = 0.0F;
      float dy = 0.0F;
      float rot = 0.0F;
      double t;
      if (shakeMag > 0.0F) {
         double age = (double)(now - shakeStartMs) / 1000.0;
         if (age > 0.7) {
            shakeMag = 0.0F;
         } else {
            t = Math.exp(-age * 7.0);
            dx += (float)((double)shakeMag * t * Math.sin(age * 55.0));
            dy += (float)((double)shakeMag * t * Math.cos(age * 47.0) * 0.5);
            rot += (float)((double)shakeMag * 0.0016 * t * Math.sin(age * 38.0));
         }
      }

      boolean damage = n != null && n.targetEntity != null;
      if (!damage && p != null && p.isMusicProvider() && cfg.musicSwayEnabled) {
         float freq = p.getBeatFrequency();
         t = (double)now / 1000.0;
         dx += (float)(2.5 * Math.sin(t * 2.0 * Math.PI * (double)freq));
         dy += (float)(1.0 * Math.sin(t * 2.0 * Math.PI * (double)freq * 2.0));
         rot += (float)(0.02 * Math.sin(t * 2.0 * Math.PI * (double)freq * 0.5));
      }

      if (damage) {
         rot = 0.0F;
      }

      return new Motion(dx, dy, rot);
   }

   public static void render(GuiGraphics g, AnimationController ctrl, DynamicIslandConfig cfg) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.getWindow() != null) {
         int sw = mc.getWindow().getGuiScaledWidth();
         int sh = mc.getWindow().getGuiScaledHeight();
         int w = Math.round(ctrl.getW());
         int h = Math.round(ctrl.getH());
         if (w >= 4 && h >= 4) {
            int x;
            int y;
            if (cfg.freeX >= 0 && cfg.freeY >= 0) {
               x = cfg.freeX - w / 2;
               y = cfg.freeY - h / 2;
            } else {
               x = computeX(cfg, sw, w);
               y = cfg.positionOffsetY;
            }

            // 自动适配：无论 HUD 变高/变宽，始终钳制在窗口内（靠近边缘也不超出）
            int margin = 2;
            x = Math.max(margin, Math.min(x, sw - w - margin));
            y = Math.max(margin, Math.min(y, sh - h - margin));
            int bg = parseColor(cfg.backgroundColor);
            int radius = Math.min(cfg.cornerRadius, h / 2);
            IslandNotification n = NotificationCenter.activeNotification();
            IslandNotification dmgN = DynamicIslandDamageHandler.toNotification();
            if (dmgN != null) {
               n = dmgN;
            }

            ProgressProvider p = ctrl.getActiveProgress();
            Font font = mc.font;
            Motion mo = computeMotion(cfg, p, n, System.currentTimeMillis());
            PoseStack pose = g.pose();
            pose.pushPose();
            float ccx = (float)x + (float)w / 2.0F;
            float ccy = (float)y + (float)h / 2.0F;
            pose.translate(ccx, ccy, 0.0F);
            pose.mulPose(Axis.ZP.rotation(mo.rot));
            pose.translate(-ccx, -ccy, 0.0F);
            pose.translate(mo.dx, mo.dy, 0.0F);
            drawShadow(g, x, y, w, h, radius);
            if (n != null) {
               if (n.targetEntity != null) {
                  renderDamage(g, ctrl, cfg, x, y, w, h, radius, bg, n, font, mo.dx, mo.dy);
               } else {
                  renderNotification(g, ctrl, cfg, x, y, w, h, radius, bg, n, font);
               }
            } else if (p != null) {
               if (p.isMusicProvider()) {
                  renderMusic(g, ctrl, cfg, x, y, w, h, radius, bg, p, font);
               } else {
                  renderProgress(g, ctrl, cfg, x, y, w, h, radius, bg, p, font);
               }
            } else if (ctrl.isPlayerList()) {
               renderPlayerList(g, ctrl, cfg, x, y, w, h, radius, bg, font);
            } else {
               renderIdle(g, cfg, x, y, w, h, radius, bg, ctrl, font);
            }

            pose.popPose();
         }
      }
   }

   private static void renderNotification(GuiGraphics g, AnimationController ctrl, DynamicIslandConfig cfg, int x, int y, int w, int h, int radius, int bg, IslandNotification n, Font font) {
      int accent = -16777216 | n.type.color;
      float prog = clamp01((ctrl.getW() - (float)cfg.capsuleWidth) / Math.max(1.0F, ctrl.getTargetW() - (float)cfg.capsuleWidth));
      int contentAlpha = (int)(255.0F * smoothstep(prog));
      GuiRenderHelper.drawRoundedRectSmooth(g, x, y, w, h, accent, radius);
      GuiRenderHelper.drawRoundedRectSmooth(g, x + 2, y + 2, w - 4, h - 4, bg, Math.max(1, radius - 2));
      int iconSize = Math.min(h - 14, 22);
      int ix = x + 12;
      int iy = y + (h - iconSize) / 2;
      GuiRenderHelper.drawRoundedRectSmooth(g, ix, iy, iconSize, iconSize, accent, iconSize / 2);
      if (n.skinLocation != null) {
         // 玩家头像：在彩色圆角底上叠加皮肤头部贴图（面部 + 帽子层）
         try {
            int inset = 2;
            int hs = iconSize - 2 * inset;
            g.blit(n.skinLocation, ix + inset, iy + inset, 8, 8, hs, hs);
            g.blit(n.skinLocation, ix + inset, iy + inset, 40, 8, hs, hs);
         } catch (Throwable var15) {
            drawTypeGlyph(g, ix, iy, iconSize, n.type, contentAlpha);
         }
      } else {
         drawTypeGlyph(g, ix, iy, iconSize, n.type, contentAlpha);
      }
      int tx = ix + iconSize + 10;
      int rightPad = cfg.showTimestamp ? 36 : 14;
      int maxTextW = Math.max(12, x + w - rightPad - tx);
      float titleScale = 1.2F;
      int titleH = (int)Math.ceil((double)(9.0F * titleScale));
      int blockH = titleH + 3 + 9;
      int ty = y + (h - blockH) / 2;
      String title = fit(n.displayName, maxTextW, titleScale);
      drawScaledText(g, title, (float)tx, (float)ty, titleScale, contentAlpha << 24 | 16777215);
      String sub = fit(n.text, maxTextW, 1.0F);
      g.drawString(font, sub, tx, ty + titleH + 3, contentAlpha << 24 | n.type.color & 16777215);
      if (cfg.showTimestamp) {
         String ts = String.format("%02d:%02d", n.createTime / 60000L % 60L, n.createTime / 1000L % 60L);
         g.drawString(font, ts, x + w - 10 - font.width(ts), y + 6, -2130706433);
      }

   }

   private static void renderDamage(GuiGraphics g, AnimationController ctrl, DynamicIslandConfig cfg, int x, int y, int w, int h, int radius, int bg, IslandNotification n, Font font, float offX, float offY) {
      int accent = -16777216 | n.type.color;
      float prog = clamp01((ctrl.getW() - (float)cfg.capsuleWidth) / Math.max(1.0F, ctrl.getTargetW() - (float)cfg.capsuleWidth));
      int contentAlpha = (int)(255.0F * smoothstep(prog));
      GuiRenderHelper.drawRoundedRectSmooth(g, x, y, w, h, accent, radius);
      GuiRenderHelper.drawRoundedRectSmooth(g, x + 2, y + 2, w - 4, h - 4, bg, Math.max(1, radius - 2));
      int avatar = 48;
      int ax = x + 12;
      int ay = y + (h - avatar) / 2;
      renderEntityAvatar(g, n.targetEntity, ax, ay, avatar, n.displayName, contentAlpha, offX, offY);
      int tx = ax + avatar + 10;
      String dmgText = "-" + trim1(n.damageAmount);
      int dmgW = Math.min(110, Math.max(64, (int)((float)font.width(dmgText) * 1.6F) + 18));
      int dmgX = x + w - 14 - dmgW;
      int barW = Math.max(40, dmgX - 10 - tx);
      float titleScale = 1.1F;
      drawScaledText(g, fit(n.targetEntity.getName().getString(), barW, titleScale), (float)tx, (float)(y + 7), titleScale, contentAlpha << 24 | 16777215);
      float hp = n.targetEntity.isRemoved() ? 0.0F : n.targetEntity.getHealth();
      float mx = Math.max(1.0F, n.targetEntity.getMaxHealth());
      float ratio = clamp01(hp / mx);
      int barY = y + 24;
      int barH = 6;
      GuiRenderHelper.drawRoundedRectSmooth(g, tx, barY, barW, barH, 1429879354, barH / 2);
      int fillW = (int)((float)barW * ratio);
      if (fillW > 1) {
         int fr = Math.min(barH / 2, fillW / 2);
         int hpColor = ratio > 0.5F ? 15022389 : (ratio > 0.2F ? 16740419 : 12000284);
         GuiRenderHelper.drawRoundedRectSmooth(g, tx, barY, fillW, barH, -16777216 | hpColor, fr);
         g.fill(tx, barY, tx + fillW, barY + 1, 1442840575);
      }

      String var10000 = trim1(hp);
      String hpText = var10000 + "/" + trim1(mx);
      g.drawString(font, hpText, tx, barY + barH + 3, contentAlpha << 24 | -1426063361);
      g.drawString(font, "伤害", dmgX + dmgW - font.width("伤害"), y + 9, contentAlpha << 24 | 13421772);
      float dmgScale = 1.6F;
      String dmgShown = n.damageMeasured ? dmgText : "…";
      String shown = fit(dmgShown, dmgW, dmgScale);
      int dw = (int)((float)font.width(shown) * dmgScale);
      int dmgColor = n.damageMeasured ? 16754470 : -2003199591;
      drawScaledText(g, shown, (float)(dmgX + dmgW - dw), (float)(y + h / 2 - 6), dmgScale, contentAlpha << 24 | dmgColor);
   }

   private static void renderEntityAvatar(GuiGraphics g, LivingEntity e, int x, int y, int size, String fallbackName, int alpha, float offX, float offY) {
      GuiRenderHelper.drawRoundedRectSmooth(g, x, y, size, size, (int)((long)alpha * 51L / 255L << 24) | 16777215, 8);
      if (e != null && !e.isRemoved()) {
         try {
            int scale = Math.max(8, Math.min(60, (int)(44.0F / Math.max(e.getBbHeight(), 0.6F))));
            g.enableScissor((int)((float)x + offX), (int)((float)y + offY), (int)((float)(x + size) + offX), (int)((float)(y + size) + offY));
            InventoryScreen.renderEntityInInventoryFollowsMouse(g, (int)((float)x + (float)size / 2.0F + offX), (int)((float)(y + size) - 2.0F + offY), scale, 0.0F, 0.0F, e);
            g.disableScissor();
         } catch (Throwable var10) {
            g.disableScissor();
            drawFallbackAvatar(g, x, y, size, fallbackName, alpha);
         }

      } else {
         drawFallbackAvatar(g, x, y, size, fallbackName, alpha);
      }
   }

   private static void drawFallbackAvatar(GuiGraphics g, int x, int y, int size, String name, int alpha) {
      String ch = name != null && !name.isEmpty() ? name.substring(0, 1) : "?";
      Font font = Minecraft.getInstance().font;
      float s = 1.6F;
      int w = (int)((float)font.width(ch) * s);
      drawScaledText(g, ch, (float)(x + (size - w) / 2), (float)(y + (size - 9) / 2 + 1), s, alpha << 24 | 14540253);
   }

   private static String trim1(float v) {
      return v == (float)((long)v) ? String.valueOf((long)v) : String.format("%.1f", v);
   }

   private static void renderMusic(GuiGraphics g, AnimationController ctrl, DynamicIslandConfig cfg, int x, int y, int w, int h, int radius, int bg, ProgressProvider p, Font font) {
      int color = -16777216 | p.getColor();
      float prog = clamp01((ctrl.getW() - (float)cfg.capsuleWidth) / Math.max(1.0F, ctrl.getTargetW() - (float)cfg.capsuleWidth));
      int contentAlpha = (int)(255.0F * smoothstep(prog));
      GuiRenderHelper.drawRoundedRectSmooth(g, x, y, w, h, color, radius);
      GuiRenderHelper.drawRoundedRectSmooth(g, x + 2, y + 2, w - 4, h - 4, bg, Math.max(1, radius - 2));
      int rightPad = 12;
      int slot = 22;
      int sx = x + 12;
      int sy = y + (h - slot) / 2;
      GuiRenderHelper.drawRoundedRectSmooth(g, sx, sy, slot, slot, 872415231, 5);
      ItemStack icon = p.getIcon();
      if (icon != null && !icon.isEmpty()) {
         g.renderItem(icon, sx + 3, sy + 3);
      }

      int tx = sx + slot + 10;
      int maxTextW = Math.max(12, x + w - rightPad - tx);
      g.drawString(font, fit(p.getTitle(), maxTextW, 1.0F), tx, y + 6, contentAlpha << 24 | 16777215);
      g.drawString(font, fit(p.getSubtitle(), maxTextW, 1.0F), tx, y + 17, contentAlpha << 24 | -1143087617);
      float freq = p.getBeatFrequency();
      double t = (double)System.currentTimeMillis() / 1000.0;
      float beat = (float)(0.5 + 0.5 * Math.sin(t * 2.0 * Math.PI * (double)freq));
      int waveY = y + h - 24;
      int waveX0 = tx;
      int waveX1 = x + w - rightPad;
      int waveW = Math.max(20, waveX1 - tx);
      int amp = (int)(2.0F + 5.0F * beat);
      int col = contentAlpha << 24 | p.getColor() & 16777215;
      float phase = (float)(t * 2.0 * Math.PI * (double)freq * 2.0);

      for(int i = 0; i < waveW; i += 2) {
         double env = Math.sin(Math.PI * (double)i / (double)waveW);
         float yy = (float)waveY + (float)(Math.sin((double)phase + (double)i * 0.25) * (double)amp * env);
         g.fill(waveX0 + i, (int)(yy - 1.0F), waveX0 + i + 2, (int)(yy + 1.0F), col);
      }

      int barY = y + h - 12;
      int barW2 = Math.max(40, x + w - rightPad - tx);
      int barH = 7;
      GuiRenderHelper.drawRoundedRectSmooth(g, tx, barY, barW2, barH, 872415231, barH / 2);
      float ratio = clamp01(p.getRatio());
      int fillW = (int)((float)barW2 * ratio);
      if (fillW > 1) {
         int fr = Math.min(barH / 2, fillW / 2);
         GuiRenderHelper.drawRoundedRectSmooth(g, tx, barY, fillW, barH, color, fr);
         g.fill(tx, barY, tx + fillW, barY + 1, 1442840575);
      }

      String pct = (int)(ratio * 100.0F) + "%";
      g.drawString(font, pct, x + w - rightPad - font.width(pct), y + 6, contentAlpha << 24 | -570425345);
   }

   private static void renderProgress(GuiGraphics g, AnimationController ctrl, DynamicIslandConfig cfg, int x, int y, int w, int h, int radius, int bg, ProgressProvider p, Font font) {
      int color = -16777216 | p.getColor();
      float prog = clamp01((ctrl.getW() - (float)cfg.capsuleWidth) / Math.max(1.0F, ctrl.getTargetW() - (float)cfg.capsuleWidth));
      int contentAlpha = (int)(255.0F * smoothstep(prog));
      GuiRenderHelper.drawRoundedRectSmooth(g, x, y, w, h, color, radius);
      GuiRenderHelper.drawRoundedRectSmooth(g, x + 2, y + 2, w - 4, h - 4, bg, Math.max(1, radius - 2));
      int rightPad = 12;
      int tx = x + 12 + 22 + 10;
      int maxTextW = Math.max(12, x + w - rightPad - tx);
      int slot = 22;
      int sx = x + 12;
      int sy = y + (h - slot) / 2;
      GuiRenderHelper.drawRoundedRectSmooth(g, sx, sy, slot, slot, 872415231, 5);
      ItemStack icon = p.getIcon();
      if (icon != null && !icon.isEmpty()) {
         g.renderItem(icon, sx + 3, sy + 3);
      }

      int barY;
      if (!p.hasBar()) {
         g.drawString(font, fit(p.getTitle(), maxTextW, 1.0F), tx, y + 7, contentAlpha << 24 | -1711276033);
         float pulse = (float)(Math.sin((double)System.currentTimeMillis() / 280.0) * 0.5 + 0.5);
         barY = (int)((float)contentAlpha * (0.75F + 0.25F * pulse));
         drawScaledText(g, fit(p.getSubtitle(), maxTextW, 1.15F), (float)tx, (float)(y + 20), 1.15F, barY << 24 | p.getColor() & 16777215);
      } else {
         g.drawString(font, fit(p.getTitle(), maxTextW, 1.0F), tx, y + 6, contentAlpha << 24 | 16777215);
         g.drawString(font, fit(p.getSubtitle(), maxTextW, 1.0F), tx, y + 17, contentAlpha << 24 | -1143087617);
         barY = y + h - 12;
         int barW = Math.max(40, x + w - rightPad - tx);
         int barH = 7;
         GuiRenderHelper.drawRoundedRectSmooth(g, tx, barY, barW, barH, 872415231, barH / 2);
         float ratio = clamp01(p.getRatio());
         int fillW = (int)((float)barW * ratio);
         if (fillW > 1) {
            int fr = Math.min(barH / 2, fillW / 2);
            GuiRenderHelper.drawRoundedRectSmooth(g, tx, barY, fillW, barH, color, fr);
            g.fill(tx, barY, tx + fillW, barY + 1, 1442840575);
         }

         String pct = (int)(ratio * 100.0F) + "%";
         g.drawString(font, pct, x + w - rightPad - font.width(pct), y + 6, contentAlpha << 24 | -570425345);
      }
   }

   private static void renderIdle(GuiGraphics g, DynamicIslandConfig cfg, int x, int y, int w, int h, int radius, int bg, AnimationController ctrl, Font font) {
      GuiRenderHelper.drawRoundedRectSmooth(g, x, y, w, h, bg, radius);
      String info = cfg.idleShowInfo ? ctrl.currentIdleInfo(cfg) : null;
      int dot;
      if (info != null) {
         dot = 6;
         int dx = x + 12;
         long t = System.currentTimeMillis();
         float pulse = (float)(Math.sin((double)t / 600.0) * 0.5 + 0.5);
         int dotA = (int)(150.0F + 90.0F * pulse);
         GuiRenderHelper.drawRoundedRectSmooth(g, dx, y + (h - dot) / 2, dot, dot, dotA << 24 | 10217640, dot / 2);
         int tx = dx + dot + 7;
         g.drawString(font, info, tx, y + (h - 9) / 2 + 1, -251658241);
      } else {
         dot = Math.min(h - 12, 7);
         long t = System.currentTimeMillis();
         float pulse = (float)(Math.sin((double)t / 500.0) * 0.5 + 0.5);
         int dotA = (int)(110.0F + 110.0F * pulse);
         GuiRenderHelper.drawRoundedRectSmooth(g, x + w / 2 - dot / 2, y + h / 2 - dot / 2, dot, dot, dotA << 24 | 11184810, dot / 2);
      }

   }

   private static void renderPlayerList(GuiGraphics g, AnimationController ctrl, DynamicIslandConfig cfg, int x, int y, int w, int h, int radius, int bg, Font font) {
      float prog = clamp01((ctrl.getW() - (float)cfg.capsuleWidth) / Math.max(1.0F, ctrl.getTargetW() - (float)cfg.capsuleWidth));
      int contentAlpha = (int)(255.0F * smoothstep(prog));
      GuiRenderHelper.drawRoundedRectSmooth(g, x, y, w, h, bg, radius);
      TabListRenderer.draw(g, cfg, x, y, w, h, radius, font, contentAlpha);
   }

   private static void drawShadow(GuiGraphics g, int x, int y, int w, int h, int radius) {
      for(int i = 3; i >= 1; --i) {
         int a = 26 - i * 6;
         if (a > 0) {
            GuiRenderHelper.drawRoundedRectSmooth(g, x - 1, y + i, w + 2, h, a << 24 | 0, radius);
         }
      }

   }

   private static void drawTypeGlyph(GuiGraphics g, int ix, int iy, int size, NotificationType type, int alpha) {
      int white = alpha << 24 | 16777215;
      int cx = ix + size / 2;
      int cy = iy + size / 2;
      switch (type) {
         case ENABLED:
            drawThickLine(g, cx - 5, cy + 1, cx - 1, cy + 5, white, 2);
            drawThickLine(g, cx - 1, cy + 5, cx + 5, cy - 3, white, 2);
            break;
         case DISABLED:
            drawThickLine(g, cx - 5, cy - 5, cx + 5, cy + 5, white, 2);
            drawThickLine(g, cx - 5, cy + 5, cx + 5, cy - 5, white, 2);
            break;
         case WARNING:
            drawThickLine(g, cx, cy - 5, cx, cy + 1, white, 2);
            g.fill(cx - 1, cy + 3, cx + 1, cy + 5, white);
            break;
         case SESSION: {
            // 会话指示灯：实心圆点（在线/连接意象）
            int r = Math.max(4, size / 4);
            GuiRenderHelper.drawRoundedRectSmooth(g, cx - r / 2, cy - r / 2, r, r, white, r / 2);
            break;
         }
         case PLAYER_JOIN:
            drawChevron(g, cx, cy, white, true);
            break;
         case PLAYER_LEAVE:
            drawChevron(g, cx, cy, white, false);
            break;
         default:
            int d = Math.max(3, size / 4);
            GuiRenderHelper.drawRoundedRectSmooth(g, cx - d, cy - d, d * 2, d * 2, white, d);
      }

   }

   private static void drawThickLine(GuiGraphics g, int x0, int y0, int x1, int y1, int color, int thick) {
      int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
      int half = thick / 2;

      for(int s = 0; s <= steps; ++s) {
         float t = steps == 0 ? 0.0F : (float)s / (float)steps;
         int px = Math.round((float)x0 + (float)(x1 - x0) * t);
         int py = Math.round((float)y0 + (float)(y1 - y0) * t);
         g.fill(px - half, py - half, px - half + thick, py - half + thick, color);
      }

   }

   /** 上下箭头图标：up=true 表示「进入」（向上箭头），up=false 表示「退出」（向下箭头） */
   private static void drawChevron(GuiGraphics g, int cx, int cy, int color, boolean up) {
      int tip = up ? cy - 4 : cy + 4;
      int tail = up ? cy + 4 : cy - 4;
      drawThickLine(g, cx, tail, cx, tip, color, 2);
      drawThickLine(g, cx - 4, up ? cy - 1 : cy + 1, cx, tip, color, 2);
      drawThickLine(g, cx + 4, up ? cy - 1 : cy + 1, cx, tip, color, 2);
   }

   private static void drawScaledText(GuiGraphics g, String text, float x, float y, float scale, int color) {
      PoseStack pose = g.pose();
      pose.pushPose();
      pose.translate(x, y, 0.0F);
      pose.scale(scale, scale, 1.0F);
      g.drawString(Minecraft.getInstance().font, text, 0, 0, color);
      pose.popPose();
   }

   private static int computeX(DynamicIslandConfig cfg, int sw, int w) {
      int var10000;
      switch (cfg.islandPosition) {
         case "TOP_LEFT":
            var10000 = cfg.positionOffsetX + 4;
            break;
         case "TOP_RIGHT":
            var10000 = sw - w - 4 - cfg.positionOffsetX;
            break;
         default:
            var10000 = (sw - w) / 2 + cfg.positionOffsetX;
      }

      return var10000;
   }

   public static int parseColor(String hex) {
      if (hex == null) {
         return -301331953;
      } else {
         try {
            String s = hex.startsWith("#") ? hex.substring(1) : hex.trim();
            long v = Long.parseLong(s, 16);
            if (s.length() == 8) {
               int r = (int)(v >> 24 & 255L);
               int gg = (int)(v >> 16 & 255L);
               int b = (int)(v >> 8 & 255L);
               int a = (int)(v & 255L);
               return a << 24 | r << 16 | gg << 8 | b;
            } else {
               return s.length() == 6 ? (int)(3992977408L | v) : (int)v;
            }
         } catch (Exception var8) {
            return -301331953;
         }
      }
   }

   private static float clamp01(float v) {
      return Math.max(0.0F, Math.min(1.0F, v));
   }

   private static float smoothstep(float t) {
      t = clamp01(t);
      return t * t * (3.0F - 2.0F * t);
   }

   private static String fit(String s, int maxW, float scale) {
      if (s == null) {
         return "";
      } else {
         Font f = Minecraft.getInstance().font;
         int lim = (int)((float)maxW / scale);
         if (f.width(s) <= lim) {
            return s;
         } else {
            StringBuilder sb = new StringBuilder(s);

            while(sb.length() > 1 && f.width("" + sb + "…") > lim) {
               sb.setLength(sb.length() - 1);
            }

            return "" + sb + "…";
         }
      }
   }

   private static final class Motion {
      final float dx;
      final float dy;
      final float rot;

      Motion(float dx, float dy, float rot) {
         this.dx = dx;
         this.dy = dy;
         this.rot = rot;
      }
   }
}
