package lexis.Hack.Utils.ESP;

import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lexis.Hack.Hackutil.config.ConfigUtils;
import lexis.Hack.Utils.font.LexisFont;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.joml.Quaternionf;

public class BlockSelectScreen extends Screen {
   private final Screen parent;
   private final String configKey;
   private final File configFile;
   private final Runnable onSave;
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
   private final int windowWidth = 520;
   private final int windowHeight = 370;
   private int windowX;
   private int windowY;
   private boolean dragging = false;
   private int dragOffsetX;
   private int dragOffsetY;
   private int leftPaneWidth = 200;
   private static final int TITLE_H = 26;
   private static final int ROW_H = 28;
   private EditBox searchBox;
   private final List allBlocks = new ArrayList();
   private List filteredBlocks = new ArrayList();
   private int selectedIndex = -1;
   private int scrollOffset = 0;
   private int maxScroll = 0;
   private float blockRotation = 0.0F;
   private final Set selectedBlockIds = new LinkedHashSet();
   private ArmorStand previewStand;

   public BlockSelectScreen(Screen parent, String configKey, Runnable onSave) {
      super(Component.m_237113_("方块选择"));
      this.parent = parent;
      this.configKey = configKey;
      this.configFile = new File("C:/karucn/Lexis/config/hack/block_select_" + configKey + ".json");
      this.onSave = onSave;
   }

   protected void m_7856_() {
      this.windowX = (this.f_96543_ - 520) / 2;
      this.windowY = (this.f_96544_ - 370) / 2;
      int boxX = this.windowX + 8;
      int boxY = this.windowY + 26 + 6;
      this.searchBox = new EditBox(this.f_96547_, boxX, boxY, this.leftPaneWidth - 12, 16, Component.m_237113_("搜索方块"));
      this.searchBox.m_257771_(Component.m_237113_("搜索方块..."));
      this.searchBox.m_94182_(true);
      this.searchBox.m_94151_((s) -> {
         this.rebuildList();
      });
      this.m_142416_(this.searchBox);
      this.loadBlocks();
      this.loadConfig();
   }

   private void loadBlocks() {
      this.allBlocks.clear();
      Iterator var1 = BuiltInRegistries.f_256975_.iterator();

      while(var1.hasNext()) {
         Block block = (Block)var1.next();
         ResourceLocation key = BuiltInRegistries.f_256975_.m_7981_(block);
         if (key != null) {
            String fullId = key.toString();
            ItemStack icon = new ItemStack(block.m_5456_());
            if (!icon.m_41619_()) {
               String name = this.getBlockDisplayName(fullId);
               this.allBlocks.add(new BlockEntry(block, name, fullId, icon));
            }
         }
      }

      this.allBlocks.sort(Comparator.comparing((e) -> {
         return e.displayName;
      }));
      this.filteredBlocks = new ArrayList(this.allBlocks);
      this.recalcScroll();
   }

   private String getBlockDisplayName(String blockId) {
      String[] parts = blockId.split(":");
      if (parts.length > 1) {
         String name = parts[1].replace('_', ' ');
         return (String)Arrays.stream(name.split(" ")).map((w) -> {
            return w.isEmpty() ? w : Character.toUpperCase(w.charAt(0)) + w.substring(1).toLowerCase();
         }).collect(Collectors.joining(" "));
      } else {
         return blockId;
      }
   }

   private void rebuildList() {
      String q = this.searchBox == null ? "" : this.searchBox.m_94155_().toLowerCase().trim();
      this.filteredBlocks = (List)this.allBlocks.stream().filter((e) -> {
         return q.isEmpty() || e.displayName.toLowerCase().contains(q) || e.fullId.toLowerCase().contains(q);
      }).collect(Collectors.toList());
      this.selectedIndex = this.filteredBlocks.isEmpty() ? -1 : 0;
      this.scrollOffset = 0;
      this.recalcScroll();
   }

   private void recalcScroll() {
      int listH = this.getListAreaHeight();
      int contentH = this.filteredBlocks.size() * 28;
      this.maxScroll = Math.max(0, contentH - listH);
      this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, this.maxScroll));
   }

   private int getListTop() {
      return this.windowY + 26 + 26;
   }

   private int getListAreaHeight() {
      return this.windowY + 370 - 10 - this.getListTop();
   }

   public void m_88315_(GuiGraphics g, int mouseX, int mouseY, float pt) {
      this.blockRotation += pt * 3.0F;
      if (this.blockRotation > 360.0F) {
         this.blockRotation -= 360.0F;
      }

      this.roundedRect(g, this.windowX, this.windowY, this.windowX + 520, this.windowY + 370, 6, -434628576);
      this.roundedBorder(g, this.windowX, this.windowY, this.windowX + 520, this.windowY + 370, 6, -12961208);
      g.m_280509_(this.windowX, this.windowY, this.windowX + 520, this.windowY + 26, -14540240);
      g.m_280509_(this.windowX, this.windowY, this.windowX + 520, this.windowY + 2, -10715910);
      g.m_280614_(this.f_96547_, LexisFont.component("§l方块选择"), this.windowX + 10, this.windowY + 9, -1513232, false);
      boolean closeHover = this.inRect(mouseX, mouseY, this.windowX + 520 - 22, this.windowY + 6, 16, 16);
      g.m_280614_(this.f_96547_, LexisFont.component("✕"), this.windowX + 520 - 18, this.windowY + 9, closeHover ? -43691 : -7303008, false);
      this.renderLeftPane(g, mouseX, mouseY, pt);
      this.renderRightPane(g, mouseX, mouseY);
      super.m_88315_(g, mouseX, mouseY, pt);
   }

   private void renderLeftPane(GuiGraphics g, int mouseX, int mouseY, float pt) {
      int leftX = this.windowX + 4;
      int leftRight = this.windowX + this.leftPaneWidth;
      int paneTop = this.windowY + 26 + 2;
      int paneBottom = this.windowY + 370 - 4;
      this.roundedRect(g, leftX, paneTop, leftRight, paneBottom, 4, -1072689131);
      this.searchBox.m_88315_(g, mouseX, mouseY, pt);
      int listTop = this.getListTop();
      int listBottom = paneBottom - 6;
      g.m_280588_(leftX, listTop, leftRight, listBottom);
      int trackX;
      int i;
      int rowY;
      if (this.filteredBlocks.isEmpty()) {
         g.m_280614_(this.f_96547_, LexisFont.component("§7没有匹配的方块"), leftX + 8, listTop + 6, -7303008, false);
      } else {
         trackX = listTop - this.scrollOffset;

         for(i = 0; i < this.filteredBlocks.size(); ++i) {
            rowY = trackX + i * 28;
            if (rowY + 28 >= listTop && rowY <= listBottom) {
               BlockEntry entry = (BlockEntry)this.filteredBlocks.get(i);
               boolean hover = this.inRect(mouseX, mouseY, leftX, rowY, leftRight - leftX, 28) && mouseY >= listTop && mouseY <= listBottom;
               boolean isSelected = this.selectedBlockIds.contains(entry.fullId);
               if (i == this.selectedIndex) {
                  g.m_280509_(leftX, rowY, leftRight, rowY + 28, 1616674042);
                  g.m_280509_(leftX, rowY, leftX + 2, rowY + 28, -10715910);
               } else if (hover) {
                  g.m_280509_(leftX, rowY, leftRight, rowY + 28, 1090519039);
               }

               g.m_280480_(entry.icon, leftX + 6, rowY + 6);
               int nameColor = i == this.selectedIndex ? -1 : -1513232;
               g.m_280614_(this.f_96547_, LexisFont.component(entry.displayName), leftX + 30, rowY + 10, nameColor, false);
               if (isSelected) {
                  g.m_280614_(this.f_96547_, LexisFont.component("§a√"), leftRight - 18, rowY + 10, -16711936, false);
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
      int rightRight = this.windowX + 520 - 4;
      int paneTop = this.windowY + 26 + 2;
      int paneBottom = this.windowY + 370 - 4;
      this.roundedRect(g, rightX, paneTop, rightRight, paneBottom, 4, -1072689131);
      if (this.selectedIndex >= 0 && this.selectedIndex < this.filteredBlocks.size()) {
         BlockEntry entry = (BlockEntry)this.filteredBlocks.get(this.selectedIndex);
         int modelSize = 100;
         int modelX = rightX + (rightRight - rightX) / 2;
         int modelY = paneTop + 120;
         this.renderBlockPreview(g, modelX, modelY, modelSize, entry);
         g.m_280614_(this.f_96547_, LexisFont.component("§f§l" + entry.displayName), rightX + 15, paneBottom - 70, -1513232, false);
         g.m_280614_(this.f_96547_, LexisFont.component("§7" + entry.fullId), rightX + 15, paneBottom - 56, -7303008, false);
         int btnY = paneBottom - 30;
         int selectBtnX = rightRight - 180;
         int confirmBtnX = rightRight - 86;
         boolean isCurrentSelected = this.selectedBlockIds.contains(entry.fullId);
         boolean selectHover = this.inRect(mouseX, mouseY, selectBtnX, btnY, 76, 22);
         boolean confirmHover = this.inRect(mouseX, mouseY, confirmBtnX, btnY, 76, 22);
         int selectBg = isCurrentSelected ? -14501052 : (selectHover ? -10715910 : -12756007);
         this.roundedRect(g, selectBtnX, btnY, selectBtnX + 76, btnY + 22, 4, selectBg);
         g.m_280653_(this.f_96547_, LexisFont.component(isCurrentSelected ? "已选择 √" : "选择"), selectBtnX + 38, btnY + 7, -1513232);
         int confirmBg = confirmHover ? -10715910 : -12756007;
         this.roundedRect(g, confirmBtnX, btnY, confirmBtnX + 76, btnY + 22, 4, confirmBg);
         g.m_280653_(this.f_96547_, LexisFont.component("确定"), confirmBtnX + 38, btnY + 7, -1513232);
         if (!this.selectedBlockIds.isEmpty()) {
            g.m_280614_(this.f_96547_, LexisFont.component("§a已选: " + this.selectedBlockIds.size() + " 个"), rightX + 15, paneBottom - 42, -16711936, false);
         }

      } else {
         g.m_280614_(this.f_96547_, LexisFont.component("§7选择左侧方块查看详情"), rightX + 15, paneTop + 60, -7303008, false);
      }
   }

   private ArmorStand getOrCreatePreviewStand() {
      if (this.previewStand == null && mc.f_91073_ != null) {
         this.previewStand = (ArmorStand)EntityType.f_20529_.m_20615_(mc.f_91073_);
         if (this.previewStand != null) {
            this.previewStand.m_6842_(true);
            this.previewStand.m_31678_(true);
         }
      }

      return this.previewStand;
   }

   private void renderBlockPreview(GuiGraphics g, int centerX, int centerY, int size, BlockEntry entry) {
      ArmorStand stand = this.getOrCreatePreviewStand();
      if (stand != null) {
         stand.m_8061_(EquipmentSlot.HEAD, entry.icon.m_41777_());
         Quaternionf pose = (new Quaternionf()).rotateZ(3.1415927F);
         Quaternionf yaw = (new Quaternionf()).rotateY((float)Math.toRadians((double)this.blockRotation));
         pose.mul(yaw);
         int scale = (int)((float)size * 0.45F);
         InventoryScreen.m_280432_(g, centerX, centerY, scale, pose, (Quaternionf)null, stand);
      }
   }

   public boolean m_6375_(double mx, double my, int button) {
      if (this.inRect((int)mx, (int)my, this.windowX + 520 - 22, this.windowY + 6, 16, 16)) {
         this.m_7379_();
         return true;
      } else if (button == 0 && mx >= (double)this.windowX && mx <= (double)(this.windowX + 520) && my >= (double)this.windowY && my <= (double)(this.windowY + 26) && !this.inRect((int)mx, (int)my, this.windowX + 520 - 22, this.windowY + 6, 16, 16)) {
         this.dragging = true;
         this.dragOffsetX = (int)mx - this.windowX;
         this.dragOffsetY = (int)my - this.windowY;
         return true;
      } else {
         int leftX = this.windowX + 4;
         int leftRight = this.windowX + this.leftPaneWidth;
         int listTop = this.getListTop();
         int listBottom = this.windowY + 370 - 10;
         if (my >= (double)listTop && my <= (double)listBottom && mx >= (double)leftX && mx <= (double)leftRight) {
            int idx = (int)((my - (double)listTop + (double)this.scrollOffset) / 28.0);
            if (idx >= 0 && idx < this.filteredBlocks.size()) {
               this.selectedIndex = idx;
               return true;
            }
         }

         if (this.selectedIndex >= 0 && this.selectedIndex < this.filteredBlocks.size()) {
            BlockEntry entry = (BlockEntry)this.filteredBlocks.get(this.selectedIndex);
            int paneBottom = this.windowY + 370 - 4;
            int btnY = paneBottom - 30;
            int rightRight = this.windowX + 520 - 4;
            int selectBtnX = rightRight - 180;
            int confirmBtnX = rightRight - 86;
            if (this.inRect((int)mx, (int)my, selectBtnX, btnY, 76, 22)) {
               this.toggleSelection(entry.fullId);
               return true;
            }

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
         this.scrollOffset = (int)Math.max(0.0, Math.min((double)this.maxScroll, (double)this.scrollOffset - delta * 28.0));
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
      this.previewStand = null;
      mc.m_91152_(this.parent);
   }

   private void saveAndClose() {
      this.saveConfig();
      if (this.onSave != null) {
         this.onSave.run();
      }

      this.previewStand = null;
      mc.m_91152_(this.parent);
   }

   private void loadConfig() {
      Type type = (new TypeToken() {
      }).getType();
      List loaded = (List)ConfigUtils.readConfig(this.configFile, type);
      if (loaded != null && !loaded.isEmpty()) {
         this.selectedBlockIds.addAll(loaded);
      }

   }

   private void saveConfig() {
      ConfigUtils.saveConfig(this.configFile, new ArrayList(this.selectedBlockIds));
   }

   public Set getSelectedBlockIds() {
      return this.selectedBlockIds;
   }

   private void toggleSelection(String blockId) {
      if (this.selectedBlockIds.contains(blockId)) {
         this.selectedBlockIds.remove(blockId);
      } else {
         this.selectedBlockIds.add(blockId);
      }

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

   private static class BlockEntry {
      Block block;
      String displayName;
      String fullId;
      ItemStack icon;

      BlockEntry(Block block, String displayName, String fullId, ItemStack icon) {
         this.block = block;
         this.displayName = displayName;
         this.fullId = fullId;
         this.icon = icon;
      }
   }
}
