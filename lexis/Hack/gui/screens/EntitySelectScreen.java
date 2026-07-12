package lexis.Hack.gui.screens;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lexis.Hack.Hacks.Combat.TpAuraHack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

public class EntitySelectScreen extends Screen {
   private final TpAuraHack hack;
   private final Screen parent;
   private final Set whitelist;
   private final List allEntities = new ArrayList();
   private List filteredEntities = new ArrayList();
   private EditBox searchBox;
   private int scrollOffset = 0;
   private static final int ITEM_HEIGHT = 20;
   private static final int VISIBLE_ITEMS = 15;
   private static final File CONFIG_DIR = new File("C:/karucn/Lexis/config/hack/");
   private static final File CONFIG_FILE;
   private static final Gson GSON;

   public EntitySelectScreen(TpAuraHack hack, Set whitelist) {
      super(Component.m_237113_("选择忽略的实体"));
      this.hack = hack;
      this.parent = Minecraft.m_91087_().f_91080_;
      this.whitelist = whitelist;
      this.loadEntities();
      this.loadConfig();
   }

   private void loadEntities() {
      Iterator var1 = ForgeRegistries.ENTITY_TYPES.getEntries().iterator();

      while(var1.hasNext()) {
         Map.Entry entry = (Map.Entry)var1.next();
         EntityType type = (EntityType)entry.getValue();
         ResourceLocation key = ((ResourceKey)entry.getKey()).m_135782_();
         String id = key.toString();
         String name = id.replace("minecraft:", "").replace('_', ' ');
         name = (String)Arrays.stream(name.split(" ")).map((word) -> {
            return word.isEmpty() ? word : Character.toUpperCase(word.charAt(0)) + word.substring(1);
         }).collect(Collectors.joining(" "));
         boolean selected = this.whitelist.contains(type);
         this.allEntities.add(new EntityEntry(type, name + " §7(" + id + ")", selected));
      }

      this.allEntities.sort(Comparator.comparing((e) -> {
         return e.displayName;
      }));
      this.filteredEntities = new ArrayList(this.allEntities);
   }

   private void loadConfig() {
      try {
         if (!CONFIG_FILE.exists()) {
            return;
         }

         FileReader reader = new FileReader(CONFIG_FILE);
         Type type = (new TypeToken() {
         }).getType();
         Set savedIds = (Set)GSON.fromJson(reader, type);
         reader.close();
         if (savedIds != null) {
            Iterator var4 = this.allEntities.iterator();

            while(var4.hasNext()) {
               EntityEntry entry = (EntityEntry)var4.next();
               ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entry.type);
               if (id != null && savedIds.contains(id.toString())) {
                  entry.selected = true;
               }
            }
         }
      } catch (Exception var7) {
         var7.printStackTrace();
      }

   }

   private void saveConfig() {
      Set selectedIds = (Set)this.allEntities.stream().filter((e) -> {
         return e.selected;
      }).map((e) -> {
         return ForgeRegistries.ENTITY_TYPES.getKey(e.type).toString();
      }).collect(Collectors.toSet());

      try {
         CONFIG_DIR.mkdirs();
         FileWriter writer = new FileWriter(CONFIG_FILE);
         GSON.toJson(selectedIds, writer);
         writer.close();
      } catch (Exception var3) {
         var3.printStackTrace();
      }

   }

   protected void m_7856_() {
      int centerX = this.f_96543_ / 2;
      int y = 20;
      this.searchBox = new EditBox(this.f_96547_, centerX - 150, y, 300, 20, Component.m_237113_("搜索..."));
      this.searchBox.m_94151_(this::filter);
      this.m_142416_(this.searchBox);
      y += 25;
      this.m_142416_(Button.m_253074_(Component.m_237113_("保存"), (btn) -> {
         this.saveConfig();
         this.whitelist.clear();
         Iterator var2 = this.allEntities.iterator();

         while(var2.hasNext()) {
            EntityEntry entry = (EntityEntry)var2.next();
            if (entry.selected) {
               this.whitelist.add(entry.type);
            }
         }

         this.m_7379_();
      }).m_252987_(centerX - 110, this.f_96544_ - 30, 100, 20).m_253136_());
      this.m_142416_(Button.m_253074_(Component.m_237113_("取消"), (btn) -> {
         this.m_7379_();
      }).m_252987_(centerX + 10, this.f_96544_ - 30, 100, 20).m_253136_());
   }

   private void filter(String text) {
      String lower = text.toLowerCase();
      this.filteredEntities = (List)this.allEntities.stream().filter((e) -> {
         return e.displayName.toLowerCase().contains(lower);
      }).collect(Collectors.toList());
      this.scrollOffset = 0;
   }

   public void m_88315_(GuiGraphics gui, int mouseX, int mouseY, float delta) {
      this.m_280273_(gui);
      super.m_88315_(gui, mouseX, mouseY, delta);
      int centerX = this.f_96543_ / 2;
      int listY = 50;
      gui.m_280509_(centerX - 152, listY - 2, centerX + 152, listY + 300 + 2, -1439485133);

      int scrollbarX;
      int sliderY;
      for(scrollbarX = 0; scrollbarX < 15; ++scrollbarX) {
         int index = this.scrollOffset + scrollbarX;
         if (index >= this.filteredEntities.size()) {
            break;
         }

         EntityEntry entry = (EntityEntry)this.filteredEntities.get(index);
         int y = listY + scrollbarX * 20;
         boolean hovered = mouseX >= centerX - 150 && mouseX <= centerX + 150 && mouseY >= y && mouseY <= y + 20 - 2;
         sliderY = entry.selected ? -1437814960 : (hovered ? -1436129690 : -1439485133);
         gui.m_280509_(centerX - 150, y, centerX + 150, y + 20 - 2, sliderY);
         gui.m_280488_(this.f_96547_, entry.displayName, centerX - 140, y + 5, 16777215);
         if (entry.selected) {
            gui.m_280488_(this.f_96547_, "✓", centerX + 135, y + 5, 16777215);
         }
      }

      if (this.filteredEntities.size() > 15) {
         scrollbarX = centerX + 155;
         int scrollbarHeight = 300;
         gui.m_280509_(scrollbarX, listY, scrollbarX + 4, listY + scrollbarHeight, -1436129690);
         float scrollPercent = (float)this.scrollOffset / (float)(this.filteredEntities.size() - 15);
         int sliderHeight = Math.max(20, (int)((float)(scrollbarHeight * 15) / (float)this.filteredEntities.size()));
         sliderY = listY + (int)(scrollPercent * (float)(scrollbarHeight - sliderHeight));
         gui.m_280509_(scrollbarX, sliderY, scrollbarX + 4, sliderY + sliderHeight, -5592406);
      }

      gui.m_280137_(this.f_96547_, "选择要忽略的实体", centerX, 10, 16777130);
   }

   public boolean m_6375_(double mouseX, double mouseY, int button) {
      if (super.m_6375_(mouseX, mouseY, button)) {
         return true;
      } else {
         int centerX = this.f_96543_ / 2;
         int listY = 50;

         for(int i = 0; i < 15; ++i) {
            int index = this.scrollOffset + i;
            if (index >= this.filteredEntities.size()) {
               break;
            }

            int y = listY + i * 20;
            if (mouseX >= (double)(centerX - 150) && mouseX <= (double)(centerX + 150) && mouseY >= (double)y && mouseY <= (double)(y + 20 - 2)) {
               EntityEntry entry = (EntityEntry)this.filteredEntities.get(index);
               entry.selected = !entry.selected;
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
         this.m_7379_();
         return true;
      } else {
         return super.m_7933_(keyCode, scanCode, modifiers);
      }
   }

   public void m_7379_() {
      if (this.parent != null) {
         Minecraft.m_91087_().m_91152_(this.parent);
      } else {
         Minecraft.m_91087_().m_91152_((Screen)null);
      }

   }

   public boolean m_7043_() {
      return false;
   }

   static {
      CONFIG_FILE = new File(CONFIG_DIR, "tpaura_whitelist.json");
      GSON = (new GsonBuilder()).setPrettyPrinting().create();
   }

   private static class EntityEntry {
      EntityType type;
      String displayName;
      boolean selected;

      EntityEntry(EntityType type, String displayName, boolean selected) {
         this.type = type;
         this.displayName = displayName;
         this.selected = selected;
      }
   }
}
