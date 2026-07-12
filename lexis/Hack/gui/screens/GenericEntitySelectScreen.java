package lexis.Hack.gui.screens;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lexis.Hack.Hackutil.config.ConfigUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;

public class GenericEntitySelectScreen extends Screen {
   private final Screen parent;
   private final String configFileName;
   private final Consumer onSaveCallback;
   private final Set selectedEntities = new HashSet();
   private List allEntities = new ArrayList();
   private List filteredEntities = new ArrayList();
   private EditBox searchBox;
   private int scrollOffset = 0;
   private static final int ENTRIES_PER_PAGE = 15;
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
   private static final Type SET_TYPE = (new TypeToken() {
   }).getType();

   public GenericEntitySelectScreen(Screen parent, String configFileName, Set initialSelected, Consumer onSaveCallback) {
      super(Component.m_237113_("选择实体"));
      this.parent = parent;
      this.configFileName = configFileName;
      this.onSaveCallback = onSaveCallback;
      if (initialSelected != null) {
         this.selectedEntities.addAll(initialSelected);
      } else {
         Set loaded = this.loadFromConfig();
         if (loaded != null) {
            this.selectedEntities.addAll(loaded);
         }
      }

      this.loadEntities();
   }

   private Set loadFromConfig() {
      File configFile = new File("C:/karucn/Lexis/config/hack/" + this.configFileName + ".json");
      return (Set)ConfigUtils.readConfig(configFile, SET_TYPE);
   }

   private void saveToConfig() {
      File configFile = new File("C:/karucn/Lexis/config/hack/" + this.configFileName + ".json");
      ConfigUtils.saveConfig(configFile, this.selectedEntities);
   }

   private void loadEntities() {
      this.allEntities.clear();
      Iterator var1 = BuiltInRegistries.f_256780_.iterator();

      while(var1.hasNext()) {
         EntityType type = (EntityType)var1.next();
         ResourceLocation key = BuiltInRegistries.f_256780_.m_7981_(type);
         String id = key.toString();
         String displayName = this.buildDisplayName(id);
         ItemStack icon = this.getEntityIcon(id, type);
         this.allEntities.add(new EntityEntry(id, displayName, icon));
      }

      this.allEntities.sort(Comparator.comparing((e) -> {
         return e.displayName;
      }));
      this.filteredEntities = new ArrayList(this.allEntities);
   }

   private String buildDisplayName(String entityId) {
      String[] parts = entityId.split(":");
      if (parts.length > 1) {
         String name = parts[1].replace('_', ' ');
         return (String)Arrays.stream(name.split(" ")).map((word) -> {
            return word.isEmpty() ? word : word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
         }).collect(Collectors.joining(" "));
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

      return new ItemStack(Items.f_41905_);
   }

   protected void m_7856_() {
      int centerX = this.f_96543_ / 2;
      int y = 20;
      this.searchBox = new EditBox(this.f_96547_, centerX - 150, y, 300, 20, Component.m_237113_("搜索实体..."));
      this.searchBox.m_94151_(this::filterEntities);
      this.m_142416_(this.searchBox);
      y += 25;
      this.m_142416_(Button.m_253074_(Component.m_237113_("全选"), (btn) -> {
         Iterator var2 = this.filteredEntities.iterator();

         while(var2.hasNext()) {
            EntityEntry entry = (EntityEntry)var2.next();
            this.selectedEntities.add(entry.id);
         }

      }).m_252987_(centerX - 150, y, 80, 20).m_253136_());
      this.m_142416_(Button.m_253074_(Component.m_237113_("清空"), (btn) -> {
         this.selectedEntities.clear();
      }).m_252987_(centerX - 60, y, 80, 20).m_253136_());
      this.m_142416_(Button.m_253074_(Component.m_237113_("反选"), (btn) -> {
         Set newSelected = new HashSet();
         Iterator var3 = this.filteredEntities.iterator();

         while(var3.hasNext()) {
            EntityEntry entry = (EntityEntry)var3.next();
            if (!this.selectedEntities.contains(entry.id)) {
               newSelected.add(entry.id);
            }
         }

         this.selectedEntities.clear();
         this.selectedEntities.addAll(newSelected);
      }).m_252987_(centerX + 30, y, 80, 20).m_253136_());
      y += 25;
      this.m_142416_(Button.m_253074_(Component.m_237113_("保存"), (btn) -> {
         this.saveToConfig();
         if (this.onSaveCallback != null) {
            this.onSaveCallback.accept(new HashSet(this.selectedEntities));
         }

         Minecraft.m_91087_().m_91152_(this.parent);
      }).m_252987_(centerX - 110, this.f_96544_ - 30, 100, 20).m_253136_());
      this.m_142416_(Button.m_253074_(Component.m_237113_("取消"), (btn) -> {
         Minecraft.m_91087_().m_91152_(this.parent);
      }).m_252987_(centerX + 10, this.f_96544_ - 30, 100, 20).m_253136_());
   }

   private void filterEntities(String search) {
      if (search.isEmpty()) {
         this.filteredEntities = new ArrayList(this.allEntities);
      } else {
         String lower = search.toLowerCase();
         this.filteredEntities = (List)this.allEntities.stream().filter((e) -> {
            return e.displayName.toLowerCase().contains(lower) || e.id.toLowerCase().contains(lower);
         }).collect(Collectors.toList());
      }

      this.scrollOffset = 0;
   }

   public void m_88315_(GuiGraphics gui, int mouseX, int mouseY, float delta) {
      super.m_88315_(gui, mouseX, mouseY, delta);
      int centerX = this.f_96543_ / 2;
      int startY = 90;
      gui.m_280653_(this.f_96547_, Component.m_237113_("选择的实体"), centerX, 5, 16766720);
      gui.m_280653_(this.f_96547_, Component.m_237113_("已选中: " + this.selectedEntities.size()), centerX, 70, 11184810);
      int listHeight = 330;
      int maxScroll = Math.max(0, this.filteredEntities.size() - 15);
      int i;
      int y;
      int x;
      if (maxScroll > 0) {
         i = centerX + 160;
         gui.m_280509_(i, startY, i + 6, startY + listHeight, 1140850688);
         float percent = (float)this.scrollOffset / (float)maxScroll;
         y = Math.max(20, listHeight * 15 / this.filteredEntities.size());
         x = startY + (int)(percent * (float)(listHeight - y));
         gui.m_280509_(i, x, i + 6, x + y, -5592406);
      }

      for(i = 0; i < 15; ++i) {
         int idx = this.scrollOffset + i;
         if (idx >= this.filteredEntities.size()) {
            break;
         }

         EntityEntry entry = (EntityEntry)this.filteredEntities.get(idx);
         y = startY + i * 22;
         x = centerX - 150;
         boolean hovered = mouseX >= x && mouseX <= x + 300 && mouseY >= y && mouseY <= y + 20;
         boolean selected = this.selectedEntities.contains(entry.id);
         int bg = selected ? -2142458032 : (hovered ? 1714631475 : 1143087650);
         gui.m_280509_(x, y, x + 300, y + 20, bg);
         gui.m_280480_(entry.icon, x + 2, y + 2);
         gui.m_280488_(this.f_96547_, entry.displayName, x + 25, y + 6, selected ? 16777215 : 13421772);
         gui.m_280488_(this.f_96547_, entry.id, x + 180, y + 6, 6710886);
         if (selected) {
            gui.m_280488_(this.f_96547_, "✓", x + 290, y + 5, 16777215);
         }
      }

   }

   public boolean m_6375_(double mouseX, double mouseY, int button) {
      if (super.m_6375_(mouseX, mouseY, button)) {
         return true;
      } else {
         int centerX = this.f_96543_ / 2;
         int startY = 90;

         for(int i = 0; i < 15; ++i) {
            int idx = this.scrollOffset + i;
            if (idx >= this.filteredEntities.size()) {
               break;
            }

            int y = startY + i * 22;
            int x = centerX - 150;
            if (mouseX >= (double)x && mouseX <= (double)(x + 300) && mouseY >= (double)y && mouseY <= (double)(y + 20)) {
               EntityEntry entry = (EntityEntry)this.filteredEntities.get(idx);
               if (this.selectedEntities.contains(entry.id)) {
                  this.selectedEntities.remove(entry.id);
               } else {
                  this.selectedEntities.add(entry.id);
               }

               return true;
            }
         }

         return false;
      }
   }

   public boolean m_6050_(double mouseX, double mouseY, double delta) {
      int maxScroll = Math.max(0, this.filteredEntities.size() - 15);
      this.scrollOffset = (int)Math.max(0.0, Math.min((double)maxScroll, (double)this.scrollOffset - delta * 3.0));
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

   public boolean m_7043_() {
      return false;
   }

   private static class EntityEntry {
      final String id;
      final String displayName;
      final ItemStack icon;

      EntityEntry(String id, String displayName, ItemStack icon) {
         this.id = id;
         this.displayName = displayName;
         this.icon = icon;
      }
   }
}
