package lexis.Hack.Hackutil.EntityEsp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.EntityEspHack;
import lexis.Hack.Utils.FakeGlowManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.registries.ForgeRegistries;

public class EntitySelectScreen extends Screen {
   private final EntityEspHack hack;
   private final Screen parent;
   private List allEntities = new ArrayList();
   private List filteredEntities = new ArrayList();
   private Map entitySettings;
   private EditBox searchBox;
   private int scrollOffset = 0;
   private int selectedScrollOffset = 0;
   private static final int ENTRIES_PER_PAGE = 12;
   private static final int SELECTED_PER_PAGE = 8;
   private EntityEntry selectedEntity = null;
   private boolean showColorPicker = false;
   private int tempRed = 0;
   private int tempGreen = 255;
   private int tempBlue = 0;
   private boolean draggingRed = false;
   private boolean draggingGreen = false;
   private boolean draggingBlue = false;
   private boolean draggingDistance = false;
   private int tempGlowRed = 255;
   private int tempGlowGreen = 255;
   private int tempGlowBlue = 255;
   private boolean draggingGlowRed = false;
   private boolean draggingGlowGreen = false;
   private boolean draggingGlowBlue = false;
   private long lastSaveTime = 0L;
   private static final long SAVE_DELAY = 500L;

   public EntitySelectScreen(EntityEspHack hack, Map entitySettings) {
      super(Component.m_237113_("选择透视实体"));
      this.hack = hack;
      this.parent = Minecraft.m_91087_().f_91080_;
      this.entitySettings = new HashMap();
      Iterator var3 = entitySettings.entrySet().iterator();

      while(var3.hasNext()) {
         Map.Entry entry = (Map.Entry)var3.next();
         EntityEspHack.EntitySettings es = new EntityEspHack.EntitySettings();
         es.enabled = ((EntityEspHack.EntitySettings)entry.getValue()).enabled;
         es.color = ((EntityEspHack.EntitySettings)entry.getValue()).color;
         es.maxDistance = ((EntityEspHack.EntitySettings)entry.getValue()).maxDistance;
         es.fakeGlow = ((EntityEspHack.EntitySettings)entry.getValue()).fakeGlow;
         es.glowColor = ((EntityEspHack.EntitySettings)entry.getValue()).glowColor;
         this.entitySettings.put((String)entry.getKey(), es);
      }

      this.loadEntities();
   }

   private void loadEntities() {
      this.allEntities.clear();
      Iterator var1 = ForgeRegistries.ENTITY_TYPES.getEntries().iterator();

      while(var1.hasNext()) {
         Map.Entry entry = (Map.Entry)var1.next();
         ResourceKey key = (ResourceKey)entry.getKey();
         ResourceLocation id = key.m_135782_();
         String entityId = id.toString();
         EntityType type = (EntityType)entry.getValue();
         String displayName = this.getEntityDisplayName(entityId);
         ItemStack icon = this.getEntityIcon(entityId, type);
         EntityEspHack.EntitySettings settings = (EntityEspHack.EntitySettings)this.entitySettings.get(entityId);
         this.allEntities.add(new EntityEntry(entityId, displayName, icon, settings));
      }

      this.allEntities.sort(Comparator.comparing((e) -> {
         return e.displayName;
      }));
      this.filteredEntities = new ArrayList(this.allEntities);
   }

   private String getEntityDisplayName(String entityId) {
      String[] parts = entityId.split(":");
      if (parts.length > 1) {
         String namespace = parts[0];
         String name = parts[1];
         name = name.replace('_', ' ');
         String displayName = (String)Arrays.stream(name.split(" ")).map((word) -> {
            if (word.isEmpty()) {
               return word;
            } else {
               String var10000 = word.substring(0, 1).toUpperCase();
               return var10000 + word.substring(1).toLowerCase();
            }
         }).collect(Collectors.joining(" "));
         return namespace.equals("minecraft") ? displayName + " §7(" + entityId + ")" : "§e[" + namespace + "]§f " + displayName + " §7(" + entityId + ")";
      } else {
         return entityId;
      }
   }

   private ItemStack getEntityIcon(String entityId, EntityType type) {
      try {
         SpawnEggItem egg = SpawnEggItem.m_43213_(type);
         if (egg != null) {
            return new ItemStack(egg);
         }
      } catch (Exception var4) {
      }

      if (entityId.contains("zombie")) {
         return new ItemStack(Items.f_42583_);
      } else if (entityId.contains("skeleton")) {
         return new ItemStack(Items.f_42411_);
      } else if (entityId.contains("spider")) {
         return new ItemStack(Items.f_42401_);
      } else if (entityId.contains("creeper")) {
         return new ItemStack(Items.f_42403_);
      } else if (entityId.contains("enderman")) {
         return new ItemStack(Items.f_42584_);
      } else if (entityId.contains("cow")) {
         return new ItemStack(Items.f_42579_);
      } else if (entityId.contains("pig")) {
         return new ItemStack(Items.f_42485_);
      } else if (entityId.contains("sheep")) {
         return new ItemStack(Items.f_42658_);
      } else if (entityId.contains("chicken")) {
         return new ItemStack(Items.f_42402_);
      } else if (entityId.contains("villager")) {
         return new ItemStack(Items.f_42616_);
      } else if (entityId.contains("iron_golem")) {
         return new ItemStack(Items.f_42416_);
      } else if (entityId.contains("snow_golem")) {
         return new ItemStack(Items.f_42452_);
      } else if (entityId.contains("blaze")) {
         return new ItemStack(Items.f_42585_);
      } else if (entityId.contains("ghast")) {
         return new ItemStack(Items.f_42586_);
      } else if (entityId.contains("slime")) {
         return new ItemStack(Items.f_42518_);
      } else {
         return entityId.contains("player") ? new ItemStack(Items.f_42680_) : new ItemStack(Items.f_41905_);
      }
   }

   protected void m_7856_() {
      int centerX = this.f_96543_ / 2;
      int y = 20;
      this.searchBox = new EditBox(this.f_96547_, centerX - 150, y, 300, 20, Component.m_237113_("搜索实体..."));
      this.searchBox.m_94151_(this::filterEntities);
      this.m_142416_(this.searchBox);
      y += 25;
      this.m_142416_(Button.m_253074_(Component.m_237113_("保存并返回"), (btn) -> {
         this.saveSettings();
         Minecraft.m_91087_().m_91152_(this.parent);
      }).m_252987_(centerX - 100, this.f_96544_ - 30, 200, 20).m_253136_());
      this.m_142416_(Button.m_253074_(Component.m_237113_("不保存返回"), (btn) -> {
         Minecraft.m_91087_().m_91152_(this.parent);
      }).m_252987_(centerX - 100, this.f_96544_ - 55, 200, 20).m_253136_());
   }

   private void filterEntities(String search) {
      if (search.isEmpty()) {
         this.filteredEntities = new ArrayList(this.allEntities);
      } else {
         String lowerSearch = search.toLowerCase();
         this.filteredEntities = (List)this.allEntities.stream().filter((e) -> {
            return e.displayName.toLowerCase().contains(lowerSearch) || e.id.toLowerCase().contains(lowerSearch);
         }).collect(Collectors.toList());
      }

      this.scrollOffset = 0;
      this.selectedEntity = null;
      this.showColorPicker = false;
   }

   public void m_88315_(GuiGraphics gui, int mouseX, int mouseY, float delta) {
      this.m_280273_(gui);
      super.m_88315_(gui, mouseX, mouseY, delta);
      int centerX = this.f_96543_ / 2;
      int startY = 70;
      gui.m_280653_(this.f_96547_, Component.m_237113_("选择要透视的实体"), centerX, 5, 16766720);
      int listWidth = 260;
      int listHeight = 264;
      int listX = centerX - listWidth - 130;
      int listY = startY;
      gui.m_280509_(listX, startY - 15, listX + listWidth, startY - 5, -13421773);
      gui.m_280430_(this.f_96547_, Component.m_237113_("所有实体 (搜索: " + this.filteredEntities.size() + ")"), listX + 5, startY - 13, 16777130);
      int maxScroll = Math.max(0, this.filteredEntities.size() - 12);
      int selectedX;
      int selectedMaxScroll;
      int rightX;
      if (maxScroll > 0) {
         selectedX = listX + listWidth + 6;
         gui.m_280509_(selectedX, startY, selectedX + 6, startY + listHeight, 1140850688);
         float scrollPercent = (float)this.scrollOffset / (float)maxScroll;
         selectedMaxScroll = Math.max(20, listHeight * 12 / this.filteredEntities.size());
         rightX = startY + (int)(scrollPercent * (float)(listHeight - selectedMaxScroll));
         gui.m_280509_(selectedX, rightX, selectedX + 6, rightX + selectedMaxScroll, -5592406);
      }

      for(selectedX = 0; selectedX < 12; ++selectedX) {
         int index = this.scrollOffset + selectedX;
         if (index >= this.filteredEntities.size()) {
            break;
         }

         EntityEntry entry = (EntityEntry)this.filteredEntities.get(index);
         int y = listY + selectedX * 22;
         boolean hovered = mouseX >= listX && mouseX <= listX + listWidth && mouseY >= y && mouseY <= y + 20;
         rightX = entry.enabled ? -2142458032 : (hovered ? 1714631475 : 1143087650);
         gui.m_280509_(listX, y, listX + listWidth, y + 20, rightX);
         gui.m_280480_(entry.icon, listX + 2, y + 2);
         String name = entry.displayName.length() > 20 ? entry.displayName.substring(0, 20) + "..." : entry.displayName;
         gui.m_280430_(this.f_96547_, Component.m_237113_(name), listX + 25, y + 6, entry.enabled ? 16777215 : 13421772);
         if (hovered) {
            gui.m_280509_(listX, y, listX + listWidth, y + 20, 872415231);
         }
      }

      selectedX = centerX - 110;
      int selectedWidth = 220;
      int selectedY = startY;
      gui.m_280509_(selectedX, startY - 15, selectedX + selectedWidth, startY - 5, -13421773);
      gui.m_280430_(this.f_96547_, Component.m_237113_("已选择的实体 (" + this.getSelectedCount() + ")"), selectedX + 5, startY - 13, 16777130);
      List selectedList = this.getSelectedEntities();
      selectedMaxScroll = Math.max(0, selectedList.size() - 8);
      short rightWidth;
      int extraHeight;
      int rightHeight;
      if (selectedMaxScroll > 0) {
         rightX = selectedX + selectedWidth + 6;
         rightWidth = 176;
         gui.m_280509_(rightX, startY, rightX + 6, startY + rightWidth, 1140850688);
         float scrollPercent = (float)this.selectedScrollOffset / (float)selectedMaxScroll;
         extraHeight = Math.max(20, rightWidth * 8 / selectedList.size());
         rightHeight = startY + (int)(scrollPercent * (float)(rightWidth - extraHeight));
         gui.m_280509_(rightX, rightHeight, rightX + 6, rightHeight + extraHeight, -5592406);
      }

      String displayName;
      int colorInt;
      for(rightX = 0; rightX < 8; ++rightX) {
         int index = this.selectedScrollOffset + rightX;
         if (index >= selectedList.size()) {
            break;
         }

         EntityEntry entry = (EntityEntry)selectedList.get(index);
         int y = selectedY + rightX * 22;
         boolean hovered = mouseX >= selectedX && mouseX <= selectedX + selectedWidth && mouseY >= y && mouseY <= y + 20;
         boolean deleteHovered = mouseX >= selectedX + selectedWidth - 25 && mouseX <= selectedX + selectedWidth - 10 && mouseY >= y + 5 && mouseY <= y + 15;
         gui.m_280509_(selectedX, y, selectedX + selectedWidth, y + 20, hovered ? 1714631475 : 1143087650);
         gui.m_280480_(entry.icon, selectedX + 2, y + 2);
         displayName = entry.displayName.length() > 12 ? entry.displayName.substring(0, 12) + "..." : entry.displayName;
         gui.m_280430_(this.f_96547_, Component.m_237113_(displayName), selectedX + 25, y + 6, 16777215);
         if (entry.settings != null && entry.settings.fakeGlow) {
            gui.m_280430_(this.f_96547_, Component.m_237113_("✨"), selectedX + selectedWidth - 75, y + 5, 16776960);
         }

         if (entry.settings != null) {
            float[] color = entry.settings.getColorF();
            colorInt = -16777216 | (int)(color[0] * 255.0F) << 16 | (int)(color[1] * 255.0F) << 8 | (int)(color[2] * 255.0F);
            gui.m_280509_(selectedX + selectedWidth - 55, y + 5, selectedX + selectedWidth - 35, y + 15, colorInt);
         }

         gui.m_280509_(selectedX + selectedWidth - 25, y + 5, selectedX + selectedWidth - 10, y + 15, deleteHovered ? -43691 : -1426115789);
         gui.m_280430_(this.f_96547_, Component.m_237113_("✕"), selectedX + selectedWidth - 20, y + 5, 16777215);
         if (hovered) {
            gui.m_280509_(selectedX, y, selectedX + selectedWidth, y + 20, 872415231);
         }
      }

      if (this.selectedEntity != null) {
         if (this.selectedEntity.settings == null) {
            this.selectedEntity.settings = new EntityEspHack.EntitySettings();
            this.selectedEntity.settings.enabled = true;
            this.selectedEntity.settings.color = -16711936;
            this.selectedEntity.settings.maxDistance = 64.0;
            this.selectedEntity.settings.fakeGlow = false;
            this.selectedEntity.settings.glowColor = 16777215;
         }

         rightX = centerX + 140;
         rightWidth = 200;
         int baseHeight = 250;
         extraHeight = this.selectedEntity.settings.fakeGlow ? 135 : 0;
         rightHeight = baseHeight + extraHeight;
         gui.m_280509_(rightX, startY, rightX + rightWidth, startY + rightHeight, -1439485133);
         displayName = this.selectedEntity.displayName.length() > 15 ? this.selectedEntity.displayName.substring(0, 15) + "..." : this.selectedEntity.displayName;
         gui.m_280430_(this.f_96547_, Component.m_237113_("设置: " + displayName), rightX + 10, startY + 10, 16777130);
         int sliderY = startY + 35;
         colorInt = this.selectedEntity.settings.color;
         int r = colorInt >> 16 & 255;
         int g = colorInt >> 8 & 255;
         int b = colorInt & 255;
         this.drawColorSlider(gui, rightX + 10, sliderY, "R", r, 16733525, this.draggingRed, mouseX, mouseY, this.selectedEntity, true);
         this.drawColorSlider(gui, rightX + 10, sliderY + 40, "G", g, 5635925, this.draggingGreen, mouseX, mouseY, this.selectedEntity, true);
         this.drawColorSlider(gui, rightX + 10, sliderY + 80, "B", b, 5592575, this.draggingBlue, mouseX, mouseY, this.selectedEntity, true);
         int distY = sliderY + 130;
         gui.m_280430_(this.f_96547_, Component.m_237113_("最大距离: " + (int)this.selectedEntity.settings.maxDistance + " 格"), rightX + 10, distY, 16777215);
         gui.m_280509_(rightX + 10, distY + 12, rightX + 190, distY + 22, -13421773);

         int indicatorX;
         for(indicatorX = 0; indicatorX <= 180; indicatorX += 2) {
            float progress = (float)indicatorX / 180.0F;
            int gradientColor = -11162881;
            gui.m_280509_(rightX + 10 + indicatorX, distY + 12, rightX + 12 + indicatorX, distY + 22, gradientColor);
         }

         indicatorX = rightX + 10 + (int)((this.selectedEntity.settings.maxDistance - 64.0) * 180.0 / 960.0);
         gui.m_280509_(indicatorX - 3, distY + 10, indicatorX + 3, distY + 24, -1);
         if (this.draggingDistance) {
            double newDist = (double)(64 + (mouseX - rightX - 10) * 960 / 180);
            this.selectedEntity.settings.maxDistance = Math.max(64.0, Math.min(1024.0, newDist));
            this.entitySettings.put(this.selectedEntity.id, this.selectedEntity.settings);
            this.saveSettings();
         }

         int glowY = distY + 40;
         gui.m_280430_(this.f_96547_, Component.m_237113_("伪造发光:"), rightX + 10, glowY, 16777215);
         String glowText = this.selectedEntity.settings.fakeGlow ? "§a开启" : "§c关闭";
         int glowSwitchX = rightX + 80;
         gui.m_280509_(glowSwitchX, glowY, glowSwitchX + 40, glowY + 10, -13421773);
         gui.m_280430_(this.f_96547_, Component.m_237113_(glowText), glowSwitchX + 5, glowY, this.selectedEntity.settings.fakeGlow ? 11206570 : 16777215);
         int previewY = startY + rightHeight - 30;
         gui.m_280509_(rightX + 10, previewY, rightX + 190, previewY + 20, this.selectedEntity.settings.color);
         if (this.selectedEntity.settings.fakeGlow) {
            int glowColor = this.selectedEntity.settings.glowColor;
            int gr = glowColor >> 16 & 255;
            int gg = glowColor >> 8 & 255;
            int gb = glowColor & 255;
            int glowSliderY = glowY + 35;
            this.drawColorSlider(gui, rightX + 10, glowSliderY, "发光 R", gr, 16733525, this.draggingGlowRed, mouseX, mouseY, this.selectedEntity, false);
            this.drawColorSlider(gui, rightX + 10, glowSliderY + 40, "发光 G", gg, 5635925, this.draggingGlowGreen, mouseX, mouseY, this.selectedEntity, false);
            this.drawColorSlider(gui, rightX + 10, glowSliderY + 80, "发光 B", gb, 5592575, this.draggingGlowBlue, mouseX, mouseY, this.selectedEntity, false);
            gui.m_280509_(rightX + 10, glowSliderY + 115, rightX + 190, glowSliderY + 135, this.selectedEntity.settings.glowColor);
         }
      }

   }

   private void drawColorSlider(GuiGraphics gui, int x, int y, String label, int value, int color, boolean dragging, int mouseX, int mouseY, EntityEntry entry, boolean isEspColor) {
      gui.m_280430_(this.f_96547_, Component.m_237113_(label + ": " + value), x, y, 16777215);
      gui.m_280509_(x, y + 12, x + 180, y + 22, -13421773);

      int indicatorX;
      int currentColor;
      int r;
      int g;
      int b;
      for(indicatorX = 0; indicatorX <= 180; indicatorX += 2) {
         float progress = (float)indicatorX / 180.0F;
         currentColor = isEspColor ? entry.settings.color : entry.settings.glowColor;
         r = label.contains("R") ? (int)(progress * 255.0F) : currentColor >> 16 & 255;
         g = label.contains("G") ? (int)(progress * 255.0F) : currentColor >> 8 & 255;
         b = label.contains("B") ? (int)(progress * 255.0F) : currentColor & 255;
         int gradientColor = -16777216 | r << 16 | g << 8 | b;
         gui.m_280509_(x + indicatorX, y + 12, x + indicatorX + 2, y + 22, gradientColor);
      }

      indicatorX = x + value * 180 / 255;
      gui.m_280509_(indicatorX - 3, y + 10, indicatorX + 3, y + 24, -1);
      if (dragging) {
         int newValue = (mouseX - x) * 255 / 180;
         newValue = Math.max(0, Math.min(255, newValue));
         if (isEspColor) {
            currentColor = entry.settings.color;
            r = label.contains("R") ? newValue : currentColor >> 16 & 255;
            g = label.contains("G") ? newValue : currentColor >> 8 & 255;
            b = label.contains("B") ? newValue : currentColor & 255;
            entry.settings.color = -16777216 | r << 16 | g << 8 | b;
         } else {
            currentColor = entry.settings.glowColor;
            r = label.contains("R") ? newValue : currentColor >> 16 & 255;
            g = label.contains("G") ? newValue : currentColor >> 8 & 255;
            b = label.contains("B") ? newValue : currentColor & 255;
            entry.settings.glowColor = -16777216 | r << 16 | g << 8 | b;
         }

         this.entitySettings.put(entry.id, entry.settings);
      }

   }

   public boolean m_6375_(double mouseX, double mouseY, int button) {
      if (super.m_6375_(mouseX, mouseY, button)) {
         return true;
      } else {
         int centerX = this.f_96543_ / 2;
         int startY = 70;
         int listWidth = 260;
         int listX = centerX - listWidth - 130;

         int selectedX;
         int rightX;
         for(selectedX = 0; selectedX < 12; ++selectedX) {
            int index = this.scrollOffset + selectedX;
            if (index >= this.filteredEntities.size()) {
               break;
            }

            rightX = startY + selectedX * 22;
            if (mouseX >= (double)listX && mouseX <= (double)(listX + listWidth) && mouseY >= (double)rightX && mouseY <= (double)(rightX + 20)) {
               EntityEntry entry = (EntityEntry)this.filteredEntities.get(index);
               if (button == 0) {
                  if (entry.settings == null) {
                     entry.settings = new EntityEspHack.EntitySettings();
                     entry.settings.enabled = true;
                     entry.settings.color = -16711936;
                     entry.settings.maxDistance = 64.0;
                     entry.settings.fakeGlow = false;
                     entry.settings.glowColor = 16777215;
                  }

                  entry.enabled = !entry.enabled;
                  entry.settings.enabled = entry.enabled;
                  this.entitySettings.put(entry.id, entry.settings);
                  if (Hack.mc.f_91073_ != null) {
                     Iterator var14 = Hack.mc.f_91073_.m_104735_().iterator();

                     while(var14.hasNext()) {
                        Entity e = (Entity)var14.next();
                        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(e.m_6095_());
                        if (key != null && key.toString().equals(entry.id)) {
                           if (entry.enabled && entry.settings.fakeGlow) {
                              FakeGlowManager.setGlow(e, "EntityEsp", true, entry.settings.glowColor, entry.settings.maxDistance);
                           } else {
                              FakeGlowManager.setGlow(e, "EntityEsp", false, 0, 0.0);
                           }
                           break;
                        }
                     }
                  }
               } else if (button == 1) {
                  this.selectedEntity = entry;
                  if (this.selectedEntity.settings == null) {
                     this.selectedEntity.settings = new EntityEspHack.EntitySettings();
                     this.selectedEntity.settings.enabled = true;
                     this.selectedEntity.settings.color = -16711936;
                     this.selectedEntity.settings.maxDistance = 64.0;
                     this.selectedEntity.settings.fakeGlow = false;
                     this.selectedEntity.settings.glowColor = 16777215;
                  }

                  this.showColorPicker = true;
               }

               return true;
            }
         }

         selectedX = centerX - 110;
         int selectedWidth = 220;

         int distY;
         for(rightX = 0; rightX < 8; ++rightX) {
            int index = this.selectedScrollOffset + rightX;
            List selectedList = this.getSelectedEntities();
            if (index >= selectedList.size()) {
               break;
            }

            distY = startY + rightX * 22;
            EntityEntry entry;
            if (mouseX >= (double)(selectedX + selectedWidth - 25) && mouseX <= (double)(selectedX + selectedWidth - 10) && mouseY >= (double)(distY + 5) && mouseY <= (double)(distY + 15)) {
               entry = (EntityEntry)selectedList.get(index);
               entry.enabled = false;
               if (entry.settings != null) {
                  entry.settings.enabled = false;
               }

               this.entitySettings.put(entry.id, entry.settings);
               this.hack.removeEntityByType(entry.id);
               return true;
            }

            if (mouseX >= (double)selectedX && mouseX <= (double)(selectedX + selectedWidth) && mouseY >= (double)distY && mouseY <= (double)(distY + 20)) {
               entry = (EntityEntry)selectedList.get(index);
               this.selectedEntity = entry;
               if (this.selectedEntity.settings == null) {
                  this.selectedEntity.settings = new EntityEspHack.EntitySettings();
                  this.selectedEntity.settings.enabled = true;
                  this.selectedEntity.settings.color = -16711936;
                  this.selectedEntity.settings.maxDistance = 64.0;
                  this.selectedEntity.settings.fakeGlow = false;
                  this.selectedEntity.settings.glowColor = 16777215;
               }

               this.showColorPicker = true;
               return true;
            }
         }

         if (this.selectedEntity != null && this.showColorPicker) {
            rightX = centerX + 140;
            int sliderY = startY + 35;
            distY = sliderY + 130;
            int glowY = distY + 40;
            if (mouseX >= (double)(rightX + 10) && mouseX <= (double)(rightX + 190)) {
               if (mouseY >= (double)(sliderY + 12) && mouseY <= (double)(sliderY + 22)) {
                  this.draggingRed = true;
                  return true;
               }

               if (mouseY >= (double)(sliderY + 52) && mouseY <= (double)(sliderY + 62)) {
                  this.draggingGreen = true;
                  return true;
               }

               if (mouseY >= (double)(sliderY + 92) && mouseY <= (double)(sliderY + 102)) {
                  this.draggingBlue = true;
                  return true;
               }
            }

            if (mouseX >= (double)(rightX + 10) && mouseX <= (double)(rightX + 190) && mouseY >= (double)(distY + 12) && mouseY <= (double)(distY + 22)) {
               this.draggingDistance = true;
               return true;
            }

            int glowSwitchX = rightX + 80;
            if (mouseX >= (double)glowSwitchX && mouseX <= (double)(glowSwitchX + 40) && mouseY >= (double)glowY && mouseY <= (double)(glowY + 10)) {
               this.selectedEntity.settings.fakeGlow = !this.selectedEntity.settings.fakeGlow;
               this.entitySettings.put(this.selectedEntity.id, this.selectedEntity.settings);
               if (Hack.mc.f_91073_ != null) {
                  Iterator var28 = Hack.mc.f_91073_.m_104735_().iterator();

                  while(var28.hasNext()) {
                     Entity e = (Entity)var28.next();
                     ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(e.m_6095_());
                     if (key != null && key.toString().equals(this.selectedEntity.id)) {
                        if (this.selectedEntity.settings.fakeGlow) {
                           FakeGlowManager.setGlow(e, "EntityEsp", true, this.selectedEntity.settings.glowColor, this.selectedEntity.settings.maxDistance);
                        } else {
                           FakeGlowManager.setGlow(e, "EntityEsp", false, 0, 0.0);
                        }
                        break;
                     }
                  }
               }

               this.saveSettings();
               return true;
            }

            if (this.selectedEntity.settings.fakeGlow) {
               int glowSliderY = glowY + 25;
               if (mouseX >= (double)(rightX + 10) && mouseX <= (double)(rightX + 190)) {
                  if (mouseY >= (double)(glowSliderY + 12) && mouseY <= (double)(glowSliderY + 22)) {
                     this.draggingGlowRed = true;
                     return true;
                  }

                  if (mouseY >= (double)(glowSliderY + 52) && mouseY <= (double)(glowSliderY + 62)) {
                     this.draggingGlowGreen = true;
                     return true;
                  }

                  if (mouseY >= (double)(glowSliderY + 92) && mouseY <= (double)(glowSliderY + 102)) {
                     this.draggingGlowBlue = true;
                     return true;
                  }
               }
            }
         }

         return false;
      }
   }

   public boolean m_6348_(double mouseX, double mouseY, int button) {
      if (this.draggingRed || this.draggingGreen || this.draggingBlue || this.draggingDistance || this.draggingGlowRed || this.draggingGlowGreen || this.draggingGlowBlue) {
         long currentTime = System.currentTimeMillis();
         if (currentTime - this.lastSaveTime > 500L) {
            this.saveSettings();
            this.lastSaveTime = currentTime;
         }
      }

      this.draggingRed = false;
      this.draggingGreen = false;
      this.draggingBlue = false;
      this.draggingDistance = false;
      this.draggingGlowRed = false;
      this.draggingGlowGreen = false;
      this.draggingGlowBlue = false;
      return super.m_6348_(mouseX, mouseY, button);
   }

   private void updateDragging(int mouseX, int mouseY) {
   }

   public boolean m_6050_(double mouseX, double mouseY, double delta) {
      int centerX = this.f_96543_ / 2;
      int maxSelectedScroll;
      if (mouseX < (double)(centerX - 50)) {
         maxSelectedScroll = Math.max(0, this.filteredEntities.size() - 12);
         this.scrollOffset = (int)Math.max(0.0, Math.min((double)maxSelectedScroll, (double)this.scrollOffset - delta * 3.0));
      } else if (mouseX < (double)(centerX + 100)) {
         maxSelectedScroll = Math.max(0, this.getSelectedEntities().size() - 8);
         this.selectedScrollOffset = (int)Math.max(0.0, Math.min((double)maxSelectedScroll, (double)this.selectedScrollOffset - delta * 3.0));
      }

      return true;
   }

   public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         Minecraft.m_91087_().m_91152_(this.parent);
         return true;
      } else {
         return super.m_7933_(keyCode, scanCode, modifiers);
      }
   }

   private int getSelectedCount() {
      return (int)this.allEntities.stream().filter((e) -> {
         return e.enabled;
      }).count();
   }

   private List getSelectedEntities() {
      return (List)this.allEntities.stream().filter((e) -> {
         return e.enabled;
      }).collect(Collectors.toList());
   }

   private void saveSettings() {
      Iterator var1 = this.allEntities.iterator();

      while(var1.hasNext()) {
         EntityEntry entry = (EntityEntry)var1.next();
         if (entry.settings != null) {
            this.entitySettings.put(entry.id, entry.settings);
         }
      }

      this.hack.getEntitySettings().clear();
      var1 = this.entitySettings.entrySet().iterator();

      while(var1.hasNext()) {
         Map.Entry entry = (Map.Entry)var1.next();
         EntityEspHack.EntitySettings es = new EntityEspHack.EntitySettings();
         es.enabled = ((EntityEspHack.EntitySettings)entry.getValue()).enabled;
         es.color = ((EntityEspHack.EntitySettings)entry.getValue()).color;
         es.maxDistance = ((EntityEspHack.EntitySettings)entry.getValue()).maxDistance;
         es.fakeGlow = ((EntityEspHack.EntitySettings)entry.getValue()).fakeGlow;
         es.glowColor = ((EntityEspHack.EntitySettings)entry.getValue()).glowColor;
         this.hack.getEntitySettings().put((String)entry.getKey(), es);
      }

      this.hack.saveEntitySettings();
   }

   public boolean m_7043_() {
      return false;
   }

   private static class EntityEntry {
      String id;
      String displayName;
      ItemStack icon;
      EntityEspHack.EntitySettings settings;
      boolean enabled;

      EntityEntry(String id, String displayName, ItemStack icon, EntityEspHack.EntitySettings settings) {
         this.id = id;
         this.displayName = displayName;
         this.icon = icon;
         this.settings = settings;
         this.enabled = settings != null && settings.enabled;
      }
   }
}
