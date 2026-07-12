package lexis.Hack.Utils.ESP;

import com.mojang.blaze3d.platform.NativeImage;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lexis.Hack.Hackutil.config.ConfigUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class FontSelectScreen extends Screen {
   private final Screen parent;
   private final String configKey;
   private final File configFile;
   private final Consumer onSelect;
   private static final Minecraft mc = Minecraft.m_91087_();
   private static final int BG_WIN = -434628576;
   private static final int TITLE_BAR = -14540240;
   private static final int PANE_BG = -1072689131;
   private static final int ACCENT = -10715910;
   private static final int ACCENT_D = -12756007;
   private static final int BORDER = -12961208;
   private static final int TEXT = -1513232;
   private static final int TEXT_DIM = -7303008;
   private static final int ROW_HOVER = 1090519039;
   private static final int ROW_SEL = 1616674042;
   private final int windowWidth = 560;
   private final int windowHeight = 390;
   private int windowX;
   private int windowY;
   private boolean dragging = false;
   private int dragOffsetX;
   private int dragOffsetY;
   private int leftPaneWidth = 220;
   private static final int TITLE_H = 26;
   private static final int ROW_H = 24;
   private EditBox searchBox;
   private final List allFonts = new ArrayList();
   private List filteredFonts = new ArrayList();
   private int selectedIndex = -1;
   private int scrollOffset = 0;
   private int maxScroll = 0;
   private String selectedFontName = "";
   private String lastPreviewFont = "";
   private ResourceLocation previewTexId;
   private DynamicTexture previewTexture;
   private int previewImgW;
   private int previewImgH;
   private static final String SAMPLE_TEXT = "ABCD 你好世界\n1234 Hello!";

   public FontSelectScreen(Screen parent, String configKey, Consumer onSelect) {
      super(Component.m_237113_("字体选择"));
      this.parent = parent;
      this.configKey = configKey;
      this.configFile = new File("C:/karucn/Lexis/config/hack/font_select_" + configKey + ".json");
      this.onSelect = onSelect;
   }

   protected void m_7856_() {
      this.windowX = (this.f_96543_ - 560) / 2;
      this.windowY = (this.f_96544_ - 390) / 2;
      int boxX = this.windowX + 8;
      int boxY = this.windowY + 26 + 6;
      this.searchBox = new EditBox(this.f_96547_, boxX, boxY, this.leftPaneWidth - 12, 16, Component.m_237113_("搜索字体"));
      this.searchBox.m_257771_(Component.m_237113_("搜索字体..."));
      this.searchBox.m_94182_(true);
      this.searchBox.m_94151_((s) -> {
         this.rebuildList();
      });
      this.m_142416_(this.searchBox);
      this.loadFonts();
      this.loadConfig();
   }

   private void loadFonts() {
      this.allFonts.clear();
      String[] fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
      String[] var2 = fontNames;
      int var3 = fontNames.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         String name = var2[var4];
         this.allFonts.add(name);
      }

      this.allFonts.sort(Comparator.naturalOrder());
      this.filteredFonts = new ArrayList(this.allFonts);
      this.recalcScroll();
      if (!this.filteredFonts.isEmpty() && this.selectedIndex < 0) {
         this.selectedIndex = 0;
      }

   }

   private void rebuildList() {
      String q = this.searchBox == null ? "" : this.searchBox.m_94155_().toLowerCase().trim();
      this.filteredFonts = (List)this.allFonts.stream().filter((f) -> {
         return q.isEmpty() || f.toLowerCase().contains(q);
      }).collect(Collectors.toList());
      this.selectedIndex = this.filteredFonts.isEmpty() ? -1 : 0;
      this.scrollOffset = 0;
      this.recalcScroll();
   }

   private void recalcScroll() {
      int listH = this.getListAreaHeight();
      int contentH = this.filteredFonts.size() * 24;
      this.maxScroll = Math.max(0, contentH - listH);
      this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, this.maxScroll));
   }

   private int getListTop() {
      return this.windowY + 26 + 26;
   }

   private int getListAreaHeight() {
      return this.windowY + 390 - 10 - this.getListTop();
   }

   public void m_88315_(GuiGraphics g, int mouseX, int mouseY, float pt) {
      this.roundedRect(g, this.windowX, this.windowY, this.windowX + 560, this.windowY + 390, 6, -434628576);
      this.roundedBorder(g, this.windowX, this.windowY, this.windowX + 560, this.windowY + 390, 6, -12961208);
      g.m_280509_(this.windowX, this.windowY, this.windowX + 560, this.windowY + 26, -14540240);
      g.m_280509_(this.windowX, this.windowY, this.windowX + 560, this.windowY + 2, -10715910);
      g.m_280056_(this.f_96547_, "§l字体选择", this.windowX + 10, this.windowY + 9, -1513232, false);
      boolean closeHover = this.inRect(mouseX, mouseY, this.windowX + 560 - 22, this.windowY + 6, 16, 16);
      g.m_280056_(this.f_96547_, "✕", this.windowX + 560 - 18, this.windowY + 9, closeHover ? -43691 : -7303008, false);
      this.renderLeftPane(g, mouseX, mouseY, pt);
      this.renderRightPane(g, mouseX, mouseY);
      super.m_88315_(g, mouseX, mouseY, pt);
   }

   private void renderLeftPane(GuiGraphics g, int mouseX, int mouseY, float pt) {
      int leftX = this.windowX + 4;
      int leftRight = this.windowX + this.leftPaneWidth;
      int paneTop = this.windowY + 26 + 2;
      int paneBottom = this.windowY + 390 - 4;
      this.roundedRect(g, leftX, paneTop, leftRight, paneBottom, 4, -1072689131);
      this.searchBox.m_88315_(g, mouseX, mouseY, pt);
      int listTop = this.getListTop();
      int listBottom = paneBottom - 6;
      g.m_280588_(leftX, listTop, leftRight, listBottom);
      int trackX;
      int i;
      int rowY;
      if (this.filteredFonts.isEmpty()) {
         g.m_280056_(this.f_96547_, "§7没有匹配的字体", leftX + 8, listTop + 6, -7303008, false);
      } else {
         trackX = listTop - this.scrollOffset;

         for(i = 0; i < this.filteredFonts.size(); ++i) {
            rowY = trackX + i * 24;
            if (rowY + 24 >= listTop && rowY <= listBottom) {
               String fontName = (String)this.filteredFonts.get(i);
               boolean hover = this.inRect(mouseX, mouseY, leftX, rowY, leftRight - leftX, 24) && mouseY >= listTop && mouseY <= listBottom;
               boolean isChosen = fontName.equals(this.selectedFontName);
               if (i == this.selectedIndex) {
                  g.m_280509_(leftX, rowY, leftRight, rowY + 24, 1616674042);
                  g.m_280509_(leftX, rowY, leftX + 2, rowY + 24, -10715910);
               } else if (hover) {
                  g.m_280509_(leftX, rowY, leftRight, rowY + 24, 1090519039);
               }

               int nameColor = i == this.selectedIndex ? -1 : -1513232;
               String displayName = fontName.length() > 22 ? fontName.substring(0, 20) + ".." : fontName;
               g.m_280056_(this.f_96547_, displayName, leftX + 8, rowY + 8, nameColor, false);
               if (isChosen) {
                  g.m_280056_(this.f_96547_, "§a●", leftRight - 18, rowY + 8, -16711936, false);
               }
            }
         }
      }

      g.m_280618_();
      if (this.maxScroll > 0) {
         trackX = leftRight - 4;
         i = listBottom - listTop;
         g.m_280509_(trackX, listTop, trackX + 3, listBottom, 1073741824);
         rowY = Math.max(20, (int)((float)i * (float)i / (float)(i + this.maxScroll)));
         int barY = listTop + (int)((float)this.scrollOffset / (float)this.maxScroll * (float)(i - rowY));
         g.m_280509_(trackX, barY, trackX + 3, barY + rowY, -10715910);
      }

   }

   private void renderRightPane(GuiGraphics g, int mouseX, int mouseY) {
      int rightX = this.windowX + this.leftPaneWidth + 6;
      int rightRight = this.windowX + 560 - 4;
      int paneTop = this.windowY + 26 + 2;
      int paneBottom = this.windowY + 390 - 4;
      this.roundedRect(g, rightX, paneTop, rightRight, paneBottom, 4, -1072689131);
      if (this.selectedIndex >= 0 && this.selectedIndex < this.filteredFonts.size()) {
         String currentFont = (String)this.filteredFonts.get(this.selectedIndex);
         g.m_280056_(this.f_96547_, "§f§l" + currentFont, rightX + 15, paneTop + 12, -1513232, false);
         g.m_280056_(this.f_96547_, "§7预览:", rightX + 15, paneTop + 28, -7303008, false);
         this.updatePreviewIfNeeded(currentFont);
         int btnY;
         int confirmBg;
         if (this.previewTexId != null && this.previewImgW > 0 && this.previewImgH > 0) {
            int availW = rightRight - rightX - 30;
            btnY = paneBottom - paneTop - 100;
            float scale = Math.min((float)availW / (float)this.previewImgW, (float)btnY / (float)this.previewImgH);
            scale = Math.min(scale, 1.0F);
            int dispW = (int)((float)this.previewImgW * scale);
            confirmBg = (int)((float)this.previewImgH * scale);
            int imgX = rightX + (rightRight - rightX - dispW) / 2;
            int imgY = paneTop + 44;
            g.m_280509_(imgX - 2, imgY - 2, imgX + dispW + 2, imgY + confirmBg + 2, -16777216);
            g.m_280163_(this.previewTexId, imgX, imgY, 0.0F, 0.0F, dispW, confirmBg, this.previewImgW, this.previewImgH);
         }

         boolean isChosen = currentFont.equals(this.selectedFontName);
         if (isChosen) {
            g.m_280056_(this.f_96547_, "§a✔ 已选择此字体", rightX + 15, paneBottom - 42, -16711936, false);
         }

         btnY = paneBottom - 30;
         int confirmBtnX = rightRight - 86;
         boolean confirmHover = this.inRect(mouseX, mouseY, confirmBtnX, btnY, 76, 22);
         confirmBg = confirmHover ? -10715910 : -12756007;
         this.roundedRect(g, confirmBtnX, btnY, confirmBtnX + 76, btnY + 22, 4, confirmBg);
         g.m_280137_(this.f_96547_, "确定", confirmBtnX + 38, btnY + 7, -1513232);
      } else {
         g.m_280056_(this.f_96547_, "§7选择左侧字体查看预览", rightX + 15, paneTop + 60, -7303008, false);
      }
   }

   private void updatePreviewIfNeeded(String fontName) {
      if (!fontName.equals(this.lastPreviewFont)) {
         this.lastPreviewFont = fontName;

         try {
            int fontSize = 32;
            Font awtFont = new Font(fontName, 0, fontSize);
            BufferedImage tmp = new BufferedImage(1, 1, 2);
            Graphics2D tmpG = tmp.createGraphics();
            tmpG.setFont(awtFont);
            FontMetrics fm = tmpG.getFontMetrics();
            String[] lines = "ABCD 你好世界\n1234 Hello!".split("\n");
            int maxW = 0;
            String[] var9 = lines;
            int totalH = lines.length;

            int imgW;
            for(imgW = 0; imgW < totalH; ++imgW) {
               String line = var9[imgW];
               maxW = Math.max(maxW, fm.stringWidth(line));
            }

            int lineH = fm.getHeight();
            totalH = lineH * lines.length + 10;
            imgW = maxW + 20;
            int imgH = totalH;
            tmpG.dispose();
            BufferedImage img = new BufferedImage(imgW, totalH, 2);
            Graphics2D g2d = img.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setColor(new Color(0, 0, 0, 0));
            g2d.fillRect(0, 0, imgW, totalH);
            g2d.setFont(awtFont);
            g2d.setColor(Color.WHITE);

            for(int i = 0; i < lines.length; ++i) {
               g2d.drawString(lines[i], 10, fm.getAscent() + i * lineH + 5);
            }

            g2d.dispose();
            NativeImage nativeImg = new NativeImage(imgW, totalH, false);

            for(int y = 0; y < imgH; ++y) {
               for(int x = 0; x < imgW; ++x) {
                  int argb = img.getRGB(x, y);
                  int a = argb >> 24 & 255;
                  int r = argb >> 16 & 255;
                  int green = argb >> 8 & 255;
                  int b = argb & 255;
                  nativeImg.m_84988_(x, y, a << 24 | b << 16 | green << 8 | r);
               }
            }

            if (this.previewTexture != null) {
               this.previewTexture.close();
            }

            if (this.previewTexId != null) {
               mc.m_91097_().m_118513_(this.previewTexId);
            }

            this.previewTexture = new DynamicTexture(nativeImg);
            this.previewTexId = new ResourceLocation("lexis", "font_preview_dynamic");
            mc.m_91097_().m_118495_(this.previewTexId, this.previewTexture);
            this.previewImgW = imgW;
            this.previewImgH = imgH;
         } catch (Exception var23) {
            this.previewTexId = null;
         }

      }
   }

   public boolean m_6375_(double mx, double my, int button) {
      if (this.inRect((int)mx, (int)my, this.windowX + 560 - 22, this.windowY + 6, 16, 16)) {
         this.m_7379_();
         return true;
      } else if (button == 0 && mx >= (double)this.windowX && mx <= (double)(this.windowX + 560) && my >= (double)this.windowY && my <= (double)(this.windowY + 26) && !this.inRect((int)mx, (int)my, this.windowX + 560 - 22, this.windowY + 6, 16, 16)) {
         this.dragging = true;
         this.dragOffsetX = (int)mx - this.windowX;
         this.dragOffsetY = (int)my - this.windowY;
         return true;
      } else {
         int leftX = this.windowX + 4;
         int leftRight = this.windowX + this.leftPaneWidth;
         int listTop = this.getListTop();
         int listBottom = this.windowY + 390 - 10;
         int paneBottom;
         if (my >= (double)listTop && my <= (double)listBottom && mx >= (double)leftX && mx <= (double)leftRight) {
            paneBottom = (int)((my - (double)listTop + (double)this.scrollOffset) / 24.0);
            if (paneBottom >= 0 && paneBottom < this.filteredFonts.size()) {
               this.selectedIndex = paneBottom;
               this.selectedFontName = (String)this.filteredFonts.get(paneBottom);
               return true;
            }
         }

         if (this.selectedIndex >= 0 && this.selectedIndex < this.filteredFonts.size()) {
            paneBottom = this.windowY + 390 - 4;
            int btnY = paneBottom - 30;
            int rightRight = this.windowX + 560 - 4;
            int confirmBtnX = rightRight - 86;
            if (this.inRect((int)mx, (int)my, confirmBtnX, btnY, 76, 22)) {
               this.saveAndClose();
               return true;
            }
         }

         return super.m_6375_(mx, my, button);
      }
   }

   public boolean m_6348_(double mx, double my, int button) {
      if (button == 0) {
         this.dragging = false;
      }

      return super.m_6348_(mx, my, button);
   }

   public boolean m_7979_(double mx, double my, int button, double dx, double dy) {
      if (this.dragging && button == 0) {
         this.windowX = (int)mx - this.dragOffsetX;
         this.windowY = (int)my - this.dragOffsetY;
         if (this.searchBox != null) {
            this.searchBox.m_252865_(this.windowX + 8);
            this.searchBox.m_253211_(this.windowY + 26 + 6);
         }

         return true;
      } else {
         return super.m_7979_(mx, my, button, dx, dy);
      }
   }

   public boolean m_6050_(double mx, double my, double delta) {
      int leftX = this.windowX + 4;
      int leftRight = this.windowX + this.leftPaneWidth;
      if (mx >= (double)leftX && mx <= (double)leftRight) {
         this.scrollOffset = (int)Math.max(0.0, Math.min((double)this.maxScroll, (double)this.scrollOffset - delta * 24.0));
         return true;
      } else {
         return super.m_6050_(mx, my, delta);
      }
   }

   public boolean m_7933_(int key, int scan, int mods) {
      if (key == 256) {
         this.m_7379_();
         return true;
      } else {
         return super.m_7933_(key, scan, mods);
      }
   }

   public void m_7379_() {
      this.cleanupTexture();
      mc.m_91152_(this.parent);
   }

   private void saveAndClose() {
      this.saveConfig();
      if (this.onSelect != null) {
         this.onSelect.accept(this.selectedFontName);
      }

      this.cleanupTexture();
      mc.m_91152_(this.parent);
   }

   private void cleanupTexture() {
      if (this.previewTexture != null) {
         this.previewTexture.close();
         this.previewTexture = null;
      }

      if (this.previewTexId != null) {
         mc.m_91097_().m_118513_(this.previewTexId);
         this.previewTexId = null;
      }

   }

   private void loadConfig() {
      String loaded = (String)ConfigUtils.readConfig(this.configFile, String.class);
      if (loaded != null && !loaded.isEmpty()) {
         this.selectedFontName = loaded;
      }

   }

   private void saveConfig() {
      ConfigUtils.saveConfig(this.configFile, this.selectedFontName);
   }

   public String getSelectedFontName() {
      return this.selectedFontName;
   }

   public boolean m_7043_() {
      return false;
   }

   private boolean inRect(int mx, int my, int x, int y, int w, int h) {
      return mx >= x && mx <= x + w && my >= y && my <= y + h;
   }

   private void roundedRect(GuiGraphics g, int x1, int y1, int x2, int y2, int r, int color) {
      if (r <= 0) {
         g.m_280509_(x1, y1, x2, y2, color);
      } else {
         g.m_280509_(x1 + r, y1, x2 - r, y2, color);
         g.m_280509_(x1, y1 + r, x1 + r, y2 - r, color);
         g.m_280509_(x2 - r, y1 + r, x2, y2 - r, color);
         int rSq = r * r;

         for(int dy = 0; dy < r; ++dy) {
            for(int dx = 0; dx < r; ++dx) {
               int ddx = r - dx;
               int ddy = r - dy;
               if (ddx * ddx + ddy * ddy <= rSq) {
                  g.m_280509_(x1 + dx, y1 + dy, x1 + dx + 1, y1 + dy + 1, color);
                  g.m_280509_(x2 - dx - 1, y1 + dy, x2 - dx, y1 + dy + 1, color);
                  g.m_280509_(x1 + dx, y2 - dy - 1, x1 + dx + 1, y2 - dy, color);
                  g.m_280509_(x2 - dx - 1, y2 - dy - 1, x2 - dx, y2 - dy, color);
               }
            }
         }

      }
   }

   private void roundedBorder(GuiGraphics g, int x1, int y1, int x2, int y2, int r, int color) {
      g.m_280509_(x1 + r, y1, x2 - r, y1 + 1, color);
      g.m_280509_(x1 + r, y2 - 1, x2 - r, y2, color);
      g.m_280509_(x1, y1 + r, x1 + 1, y2 - r, color);
      g.m_280509_(x2 - 1, y1 + r, x2, y2 - r, color);
   }
}
