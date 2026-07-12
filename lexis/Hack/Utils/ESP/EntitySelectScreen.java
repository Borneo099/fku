package lexis.Hack.Utils.ESP;

import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import org.joml.Quaternionf;

public class EntitySelectScreen extends Screen {
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
   private final List allEntities = new ArrayList();
   private List filteredEntities = new ArrayList();
   private int selectedIndex = -1;
   private int scrollOffset = 0;
   private int maxScroll = 0;
   private float entityRotation = 0.0F;
   private final Set selectedEntityIds = new LinkedHashSet();
   private final Map entityCache = new HashMap();

   public EntitySelectScreen(Screen parent, String configKey, Runnable onSave) {
      super(Component.m_237113_("实体选择"));
      this.parent = parent;
      this.configKey = configKey;
      this.configFile = new File("C:/karucn/Lexis/config/hack/entity_select_" + configKey + ".json");
      this.onSave = onSave;
   }

   protected void m_7856_() {
      this.windowX = (this.f_96543_ - 520) / 2;
      this.windowY = (this.f_96544_ - 370) / 2;
      int boxX = this.windowX + 8;
      int boxY = this.windowY + 26 + 6;
      this.searchBox = new EditBox(this.f_96547_, boxX, boxY, this.leftPaneWidth - 12, 16, Component.m_237113_("搜索实体"));
      this.searchBox.m_257771_(Component.m_237113_("搜索实体..."));
      this.searchBox.m_94182_(true);
      this.searchBox.m_94151_((s) -> {
         this.rebuildList();
      });
      this.m_142416_(this.searchBox);
      this.loadEntities();
      this.loadConfig();
   }

   private void loadEntities() {
      this.allEntities.clear();
      Iterator var1 = BuiltInRegistries.f_256780_.m_6579_().iterator();

      while(var1.hasNext()) {
         Map.Entry entry = (Map.Entry)var1.next();
         EntityType type = (EntityType)entry.getValue();
         ResourceLocation key = ((ResourceKey)entry.getKey()).m_135782_();
         String fullId = key.toString();
         String name = this.getEntityDisplayName(fullId);
         ItemStack icon = this.getEntityIcon(type, key);
         this.allEntities.add(new EntityEntry(type, name, fullId, icon));
      }

      this.allEntities.sort(Comparator.comparing((e) -> {
         return e.displayName;
      }));
      this.filteredEntities = new ArrayList(this.allEntities);
      this.recalcScroll();
   }

   private String getEntityDisplayName(String entityId) {
      String[] parts = entityId.split(":");
      if (parts.length > 1) {
         String name = parts[1].replace('_', ' ');
         return (String)Arrays.stream(name.split(" ")).map((w) -> {
            return w.isEmpty() ? w : Character.toUpperCase(w.charAt(0)) + w.substring(1).toLowerCase();
         }).collect(Collectors.joining(" "));
      } else {
         return entityId;
      }
   }

   private ItemStack getEntityIcon(EntityType type, ResourceLocation key) {
      try {
         SpawnEggItem egg = SpawnEggItem.m_43213_(type);
         if (egg != null) {
            return new ItemStack(egg);
         }
      } catch (Exception var4) {
      }

      return new ItemStack(Items.f_41905_);
   }

   private LivingEntity getOrCreateEntity(EntityType type) {
      return mc.f_91073_ == null ? null : (LivingEntity)this.entityCache.computeIfAbsent(type, (t) -> {
         try {
            Entity e = t.m_20615_(mc.f_91073_);
            if (e instanceof LivingEntity) {
               return (LivingEntity)e;
            }
         } catch (Exception var2) {
         }

         return null;
      });
   }

   private void rebuildList() {
      String q = this.searchBox == null ? "" : this.searchBox.m_94155_().toLowerCase().trim();
      this.filteredEntities = (List)this.allEntities.stream().filter((e) -> {
         return q.isEmpty() || e.displayName.toLowerCase().contains(q) || e.fullId.toLowerCase().contains(q);
      }).collect(Collectors.toList());
      this.selectedIndex = this.filteredEntities.isEmpty() ? -1 : 0;
      this.scrollOffset = 0;
      this.recalcScroll();
   }

   private void recalcScroll() {
      int listH = this.getListAreaHeight();
      int contentH = this.filteredEntities.size() * 28;
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
      this.entityRotation += pt * 3.0F;
      if (this.entityRotation > 360.0F) {
         this.entityRotation -= 360.0F;
      }

      this.roundedRect(g, this.windowX, this.windowY, this.windowX + 520, this.windowY + 370, 6, -434628576);
      this.roundedBorder(g, this.windowX, this.windowY, this.windowX + 520, this.windowY + 370, 6, -12961208);
      g.m_280509_(this.windowX, this.windowY, this.windowX + 520, this.windowY + 26, -14540240);
      g.m_280509_(this.windowX, this.windowY, this.windowX + 520, this.windowY + 2, -10715910);
      g.m_280614_(this.f_96547_, LexisFont.component("§l实体选择"), this.windowX + 10, this.windowY + 9, -1513232, false);
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
      if (this.filteredEntities.isEmpty()) {
         g.m_280614_(this.f_96547_, LexisFont.component("§7没有匹配的实体"), leftX + 8, listTop + 6, -7303008, false);
      } else {
         trackX = listTop - this.scrollOffset;

         for(i = 0; i < this.filteredEntities.size(); ++i) {
            rowY = trackX + i * 28;
            if (rowY + 28 >= listTop && rowY <= listBottom) {
               EntityEntry entry = (EntityEntry)this.filteredEntities.get(i);
               boolean hover = this.inRect(mouseX, mouseY, leftX, rowY, leftRight - leftX, 28) && mouseY >= listTop && mouseY <= listBottom;
               boolean isSelected = this.selectedEntityIds.contains(entry.fullId);
               if (i == this.selectedIndex) {
                  g.m_280509_(leftX, rowY, leftRight, rowY + 28, 1616674042);
                  g.m_280509_(leftX, rowY, leftX + 2, rowY + 28, -10715910);
               } else if (hover) {
                  g.m_280509_(leftX, rowY, leftRight, rowY + 28, 1090519039);
               }

               int iconX = leftX + 6;
               int iconY = rowY + 4;
               int iconSize = 20;
               LivingEntity renderEntity = this.getOrCreateEntity(entry.type);
               if (renderEntity != null) {
                  this.renderMiniEntity(g, iconX + iconSize / 2, iconY + iconSize, iconSize, renderEntity);
               } else {
                  g.m_280480_(entry.icon, iconX + 2, iconY + 2);
               }

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
      if (this.selectedIndex >= 0 && this.selectedIndex < this.filteredEntities.size()) {
         EntityEntry entry = (EntityEntry)this.filteredEntities.get(this.selectedIndex);
         int modelSize = 80;
         int modelX = rightX + (rightRight - rightX) / 2;
         int modelY = paneTop + 120;
         LivingEntity renderEntity = this.getOrCreateEntity(entry.type);
         if (renderEntity != null) {
            this.renderLargeEntity(g, modelX, modelY, modelSize, renderEntity);
         } else {
            g.m_280480_(entry.icon, modelX - 8, modelY - 16);
            g.m_280614_(this.f_96547_, LexisFont.component("§7(无3D模型)"), modelX + 20, modelY - 8, -7303008, false);
         }

         g.m_280614_(this.f_96547_, LexisFont.component("§f§l" + entry.displayName), rightX + 15, paneBottom - 70, -1513232, false);
         g.m_280614_(this.f_96547_, LexisFont.component("§7" + entry.fullId), rightX + 15, paneBottom - 56, -7303008, false);
         int btnY = paneBottom - 30;
         int selectBtnX = rightRight - 180;
         int confirmBtnX = rightRight - 86;
         boolean isCurrentSelected = this.selectedEntityIds.contains(entry.fullId);
         boolean selectHover = this.inRect(mouseX, mouseY, selectBtnX, btnY, 76, 22);
         boolean confirmHover = this.inRect(mouseX, mouseY, confirmBtnX, btnY, 76, 22);
         int selectBg = isCurrentSelected ? -14501052 : (selectHover ? -10715910 : -12756007);
         this.roundedRect(g, selectBtnX, btnY, selectBtnX + 76, btnY + 22, 4, selectBg);
         g.m_280653_(this.f_96547_, LexisFont.component(isCurrentSelected ? "已选择 √" : "选择"), selectBtnX + 38, btnY + 7, -1513232);
         int confirmBg = confirmHover ? -10715910 : -12756007;
         this.roundedRect(g, confirmBtnX, btnY, confirmBtnX + 76, btnY + 22, 4, confirmBg);
         g.m_280653_(this.f_96547_, LexisFont.component("确定"), confirmBtnX + 38, btnY + 7, -1513232);
         if (!this.selectedEntityIds.isEmpty()) {
            g.m_280614_(this.f_96547_, LexisFont.component("§a已选: " + this.selectedEntityIds.size() + " 个"), rightX + 15, paneBottom - 42, -16711936, false);
         }

      } else {
         g.m_280614_(this.f_96547_, LexisFont.component("§7选择左侧实体查看详情"), rightX + 15, paneTop + 60, -7303008, false);
      }
   }

   private void renderMiniEntity(GuiGraphics g, int centerX, int centerY, int size, LivingEntity entity) {
      float savedYBodyRot = entity.f_20883_;
      float savedYRot = entity.m_146908_();
      float savedXRot = entity.m_146909_();
      float savedYHeadRot = entity.f_20885_;
      entity.f_20883_ = 160.0F;
      entity.m_146922_(160.0F);
      entity.m_146926_(0.0F);
      entity.f_20885_ = 160.0F;
      Quaternionf pose = (new Quaternionf()).rotateZ(3.1415927F);
      Quaternionf yaw = (new Quaternionf()).rotateY((float)Math.toRadians((double)this.entityRotation));
      pose.mul(yaw);
      int scale = (int)((float)size * 0.45F);
      InventoryScreen.m_280432_(g, centerX, centerY, scale, pose, (Quaternionf)null, entity);
      entity.f_20883_ = savedYBodyRot;
      entity.m_146922_(savedYRot);
      entity.m_146926_(savedXRot);
      entity.f_20885_ = savedYHeadRot;
   }

   private void renderLargeEntity(GuiGraphics g, int centerX, int centerY, int size, LivingEntity entity) {
      float savedYBodyRot = entity.f_20883_;
      float savedYRot = entity.m_146908_();
      float savedXRot = entity.m_146909_();
      float savedYHeadRot = entity.f_20885_;
      entity.f_20883_ = 160.0F;
      entity.m_146922_(160.0F);
      entity.m_146926_(0.0F);
      entity.f_20885_ = 160.0F;
      Quaternionf pose = (new Quaternionf()).rotateZ(3.1415927F);
      Quaternionf yaw = (new Quaternionf()).rotateY((float)Math.toRadians((double)this.entityRotation));
      pose.mul(yaw);
      int scale = (int)((float)size * 0.5F);
      InventoryScreen.m_280432_(g, centerX, centerY, scale, pose, (Quaternionf)null, entity);
      entity.f_20883_ = savedYBodyRot;
      entity.m_146922_(savedYRot);
      entity.m_146926_(savedXRot);
      entity.f_20885_ = savedYHeadRot;
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
            if (idx >= 0 && idx < this.filteredEntities.size()) {
               this.selectedIndex = idx;
               return true;
            }
         }

         if (this.selectedIndex >= 0 && this.selectedIndex < this.filteredEntities.size()) {
            EntityEntry entry = (EntityEntry)this.filteredEntities.get(this.selectedIndex);
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
      this.entityCache.clear();
      mc.m_91152_(this.parent);
   }

   private void saveAndClose() {
      this.saveConfig();
      this.entityCache.clear();
      if (this.onSave != null) {
         this.onSave.run();
      }

      mc.m_91152_(this.parent);
   }

   private void loadConfig() {
      Type type = (new TypeToken() {
      }).getType();
      List loaded = (List)ConfigUtils.readConfig(this.configFile, type);
      if (loaded != null && !loaded.isEmpty()) {
         this.selectedEntityIds.addAll(loaded);
      }

   }

   private void saveConfig() {
      ConfigUtils.saveConfig(this.configFile, new ArrayList(this.selectedEntityIds));
   }

   public Set getSelectedEntityIds() {
      return this.selectedEntityIds;
   }

   private void toggleSelection(String entityId) {
      if (this.selectedEntityIds.contains(entityId)) {
         this.selectedEntityIds.remove(entityId);
      } else {
         this.selectedEntityIds.add(entityId);
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

   private static class EntityEntry {
      EntityType type;
      String displayName;
      String fullId;
      ItemStack icon;

      EntityEntry(EntityType type, String displayName, String fullId, ItemStack icon) {
         this.type = type;
         this.displayName = displayName;
         this.fullId = fullId;
         this.icon = icon;
      }
   }
}
