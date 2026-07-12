package lexis.Hack.Hackutil.tpaura;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lexis.Hack.Hacks.Combat.TpAurasHack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;

public class EntitySelectScreen extends Screen {
   private final TpAurasHack hack;
   private final Screen parent;
   private List allEntities = new ArrayList();
   private List filteredEntities = new ArrayList();
   private Set selectedEntities = new HashSet();
   private EditBox searchBox;
   private int scrollOffset = 0;
   private static final int ENTRIES_PER_PAGE = 15;
   private boolean selectAll = false;

   public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         Minecraft.m_91087_().m_91152_(this.parent);
         return true;
      } else {
         return super.m_7933_(keyCode, scanCode, modifiers);
      }
   }

   public EntitySelectScreen(TpAurasHack hack, Set currentWhitelist) {
      super(Component.m_237113_("选择晓过实体"));
      this.hack = hack;
      this.parent = Minecraft.m_91087_().f_91080_;
      this.selectedEntities.addAll(currentWhitelist);
      this.loadEntities();
   }

   private void loadEntities() {
      this.allEntities.clear();
      Iterator var1 = BuiltInRegistries.f_256780_.m_6579_().iterator();

      while(var1.hasNext()) {
         Map.Entry entry = (Map.Entry)var1.next();
         ResourceKey key = (ResourceKey)entry.getKey();
         String entityId = key.m_135782_().toString();
         String displayName = this.getEntityDisplayName(entityId);
         ItemStack icon = this.getEntityIcon(entityId);
         this.allEntities.add(new EntityEntry(entityId, displayName, icon));
      }

      this.allEntities.sort(Comparator.comparing((e) -> {
         return e.displayName;
      }));
      this.filteredEntities = new ArrayList(this.allEntities);
   }

   private String getEntityDisplayName(String entityId) {
      String[] parts = entityId.split(":");
      if (parts.length > 1) {
         String name = parts[1];
         name = name.replace('_', ' ');
         return (String)Arrays.stream(name.split(" ")).map((word) -> {
            if (word.isEmpty()) {
               return word;
            } else {
               String var10000 = word.substring(0, 1).toUpperCase();
               return var10000 + word.substring(1).toLowerCase();
            }
         }).collect(Collectors.joining(" "));
      } else {
         return entityId;
      }
   }

   private ItemStack getEntityIcon(String entityId) {
      try {
         ResourceLocation id = new ResourceLocation(entityId);
         EntityType type = (EntityType)BuiltInRegistries.f_256780_.m_7745_(id);
         if (type != null) {
            SpawnEggItem egg = SpawnEggItem.m_43213_(type);
            if (egg != null) {
               return new ItemStack(egg);
            }
         }
      } catch (Exception var5) {
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
         this.selectAll = !this.selectAll;
         if (this.selectAll) {
            Iterator var2 = this.filteredEntities.iterator();

            while(var2.hasNext()) {
               EntityEntry entry = (EntityEntry)var2.next();
               this.selectedEntities.add(entry.id);
            }

            btn.m_93666_(Component.m_237113_("取消全选"));
         } else {
            this.selectedEntities.clear();
            btn.m_93666_(Component.m_237113_("全选"));
         }

      }).m_252987_(centerX - 150, y, 100, 20).m_253136_());
      this.m_142416_(Button.m_253074_(Component.m_237113_("清空"), (btn) -> {
         this.selectedEntities.clear();
         this.selectAll = false;
         Button allBtn = (Button)this.m_6702_().get(1);
         allBtn.m_93666_(Component.m_237113_("全选"));
      }).m_252987_(centerX - 40, y, 80, 20).m_253136_());
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
         this.selectAll = false;
         Button allBtn = (Button)this.m_6702_().get(1);
         allBtn.m_93666_(Component.m_237113_("全选"));
      }).m_252987_(centerX + 50, y, 80, 20).m_253136_());
      y += 25;
      this.m_142416_(Button.m_253074_(Component.m_237113_("保存"), (btn) -> {
         this.hack.saveWhitelist(this.selectedEntities);
         Minecraft.m_91087_().m_91152_(this.parent);
      }).m_252987_(centerX - 110, this.f_96544_ - 30, 100, 20).m_253136_());
      this.m_142416_(Button.m_253074_(Component.m_237113_("返回"), (btn) -> {
         Minecraft.m_91087_().m_91152_(this.parent);
      }).m_252987_(centerX + 10, this.f_96544_ - 30, 100, 20).m_253136_());
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
   }

   public void m_88315_(GuiGraphics gui, int mouseX, int mouseY, float delta) {
      this.m_280273_(gui);
      super.m_88315_(gui, mouseX, mouseY, delta);
      int centerX = this.f_96543_ / 2;
      int startY = 90;
      gui.m_280137_(this.f_96547_, "选择不攻击的实体（传送光环专用）", centerX, 5, 16766720);
      gui.m_280137_(this.f_96547_, "已选择: " + this.selectedEntities.size() + " 个实体", centerX, 70, 11184810);
      int listHeight = 330;
      int maxScroll = Math.max(0, this.filteredEntities.size() - 15);
      int i;
      int x;
      if (maxScroll > 0) {
         i = centerX + 160;
         gui.m_280509_(i, startY, i + 6, startY + listHeight, 1140850688);
         float scrollPercent = (float)this.scrollOffset / (float)maxScroll;
         x = Math.max(20, listHeight * 15 / this.filteredEntities.size());
         int sliderY = startY + (int)(scrollPercent * (float)(listHeight - x));
         gui.m_280509_(i, sliderY, i + 6, sliderY + x, -5592406);
      }

      for(i = 0; i < 15; ++i) {
         int index = this.scrollOffset + i;
         if (index >= this.filteredEntities.size()) {
            break;
         }

         EntityEntry entry = (EntityEntry)this.filteredEntities.get(index);
         int y = startY + i * 22;
         x = centerX - 150;
         boolean hovered = mouseX >= x && mouseX <= x + 300 && mouseY >= y && mouseY <= y + 20;
         boolean selected = this.selectedEntities.contains(entry.id);
         int bgColor = selected ? -2142458032 : (hovered ? 1714631475 : 1143087650);
         gui.m_280509_(x, y, x + 300, y + 20, bgColor);
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
            int index = this.scrollOffset + i;
            if (index >= this.filteredEntities.size()) {
               break;
            }

            int y = startY + i * 22;
            int x = centerX - 150;
            if (mouseX >= (double)x && mouseX <= (double)(x + 300) && mouseY >= (double)y && mouseY <= (double)(y + 20)) {
               EntityEntry entry = (EntityEntry)this.filteredEntities.get(index);
               if (this.selectedEntities.contains(entry.id)) {
                  this.selectedEntities.remove(entry.id);
               } else {
                  this.selectedEntities.add(entry.id);
               }

               this.selectAll = false;
               Button allBtn = (Button)this.m_6702_().get(1);
               allBtn.m_93666_(Component.m_237113_("全选"));
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

   public boolean m_7043_() {
      return false;
   }

   private static class EntityEntry {
      String id;
      String displayName;
      ItemStack icon;

      EntityEntry(String id, String displayName, ItemStack icon) {
         this.id = id;
         this.displayName = displayName;
         this.icon = icon;
      }
   }
}
