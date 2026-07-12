package lexis.Hack.Hackutil.settings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemListScreen extends Screen {
   private final ItemListSetting setting;
   private final Screen parent;
   private List items;
   private int selectedIndex = -1;
   private int scrollOffset = 0;
   private static final int ITEM_HEIGHT = 30;
   private static final int VISIBLE_ITEMS = 12;
   private EditBox searchBox;
   private List filteredItems;
   private Button addButton;
   private Button removeButton;
   private Button resetButton;
   private Button doneButton;
   private Item itemToAdd;
   private boolean isDragging = false;
   private int dragX;
   private int dragY;
   private int windowX;
   private int windowY;
   private int windowWidth = 500;
   private int windowHeight = 450;
   private final int[] gradientColors = new int[]{-2461482, -2252579, -1146130, -18751, -38476};

   public ItemListScreen(ItemListSetting setting, Screen parent) {
      super(Component.m_237113_("物品列表"));
      this.setting = setting;
      this.parent = parent;
      this.items = new ArrayList(setting.getItemNames());
      this.filteredItems = new ArrayList(this.items);
   }

   protected void m_7856_() {
      this.windowX = (this.f_96543_ - this.windowWidth) / 2;
      this.windowY = (this.f_96544_ - this.windowHeight) / 2;
      this.searchBox = new EditBox(this.f_96547_, this.windowX + 10, this.windowY + 35, 200, 20, Component.m_237113_(""));
      this.searchBox.m_94199_(50);
      this.searchBox.m_94151_(this::filterItems);
      this.m_142416_(this.searchBox);
      this.addButton = Button.m_253074_(Component.m_237113_("添加"), (btn) -> {
         if (this.itemToAdd != null) {
            this.setting.add(this.itemToAdd);
            this.items = new ArrayList(this.setting.getItemNames());
            this.filterItems(this.searchBox.m_94155_());
         }

      }).m_252987_(this.windowX + 220, this.windowY + 35, 50, 20).m_253136_();
      this.m_142416_(this.addButton);
      this.removeButton = Button.m_253074_(Component.m_237113_("移除选中"), (btn) -> {
         if (this.selectedIndex >= 0 && this.selectedIndex < this.filteredItems.size()) {
            int realIndex = this.items.indexOf(this.filteredItems.get(this.selectedIndex));
            if (realIndex >= 0) {
               this.setting.remove(realIndex);
               this.items = new ArrayList(this.setting.getItemNames());
               this.filterItems(this.searchBox.m_94155_());
               this.selectedIndex = -1;
            }
         }

      }).m_252987_(this.windowX + 280, this.windowY + 35, 80, 20).m_253136_();
      this.m_142416_(this.removeButton);
      this.resetButton = Button.m_253074_(Component.m_237113_("重置"), (btn) -> {
         Minecraft.m_91087_().m_91152_(new ConfirmScreen((result) -> {
            if (result) {
               this.setting.resetToDefaults();
               this.items = new ArrayList(this.setting.getItemNames());
               this.filterItems(this.searchBox.m_94155_());
               this.selectedIndex = -1;
            }

            Minecraft.m_91087_().m_91152_(this);
         }, Component.m_237113_("重置物品列表"), Component.m_237113_("确定要重置为默认物品吗？")));
      }).m_252987_(this.windowX + 370, this.windowY + 35, 50, 20).m_253136_();
      this.m_142416_(this.resetButton);
      this.doneButton = Button.m_253074_(Component.m_237113_("完成"), (btn) -> {
         this.m_7379_();
      }).m_252987_(this.windowX + this.windowWidth - 100, this.windowY + this.windowHeight - 30, 80, 20).m_253136_();
      this.m_142416_(this.doneButton);
      this.filterItems("");
   }

   public void m_88315_(GuiGraphics gui, int mouseX, int mouseY, float delta) {
      if (this.searchBox != null) {
         this.searchBox.m_252865_(this.windowX + 10);
         this.searchBox.m_253211_(this.windowY + 35);
      }

      this.addButton.m_252865_(this.windowX + 220);
      this.addButton.m_253211_(this.windowY + 35);
      this.removeButton.m_252865_(this.windowX + 280);
      this.removeButton.m_253211_(this.windowY + 35);
      this.resetButton.m_252865_(this.windowX + 370);
      this.resetButton.m_253211_(this.windowY + 35);
      this.doneButton.m_252865_(this.windowX + this.windowWidth - 100);
      this.doneButton.m_253211_(this.windowY + this.windowHeight - 30);
      this.m_280273_(gui);
      gui.m_280509_(this.windowX, this.windowY, this.windowX + this.windowWidth, this.windowY + this.windowHeight, -13816518);
      gui.m_280509_(this.windowX, this.windowY, this.windowX + this.windowWidth, this.windowY + 30, -12763830);
      gui.m_280488_(this.f_96547_, "物品列表", this.windowX + 10, this.windowY + 8, 16777215);
      int listY = this.windowY + 65;
      gui.m_280509_(this.windowX + 5, listY, this.windowX + this.windowWidth - 5, listY + 360 + 5, -1439485133);
      int start = this.scrollOffset;
      int end = Math.min(start + 12, this.filteredItems.size());

      int maxScroll;
      for(maxScroll = start; maxScroll < end; ++maxScroll) {
         String id = (String)this.filteredItems.get(maxScroll);
         int y = listY + 5 + (maxScroll - start) * 30;
         boolean selected = maxScroll == this.selectedIndex;
         int bg = selected ? -2008199846 : (mouseX >= this.windowX + 5 && mouseX <= this.windowX + this.windowWidth - 5 && mouseY >= y && mouseY <= y + 30 - 2 ? 1714631475 : 0);
         if (bg != 0) {
            gui.m_280509_(this.windowX + 5, y, this.windowX + this.windowWidth - 5, y + 30 - 2, bg);
         }

         Item item = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
         if (item != null) {
            ItemStack stack = new ItemStack(item);
            gui.m_280480_(stack, this.windowX + 10, y + 5);
            gui.m_280370_(this.f_96547_, stack, this.windowX + 10, y + 5);
            String displayName = stack.m_41786_().getString();
            gui.m_280488_(this.f_96547_, displayName, this.windowX + 35, y + 5, 16777215);
            gui.m_280488_(this.f_96547_, id, this.windowX + 35, y + 18, 11184810);
         } else {
            gui.m_280488_(this.f_96547_, "§c" + id, this.windowX + 35, y + 5, 16733525);
         }
      }

      maxScroll = Math.max(0, this.filteredItems.size() - 12);
      int addY;
      if (maxScroll > 0) {
         addY = this.windowX + this.windowWidth - 10;
         int scrollbarHeight = 365;
         gui.m_280509_(addY, listY, addY + 4, listY + scrollbarHeight, -1436129690);
         float percent = (float)this.scrollOffset / (float)maxScroll;
         int sliderHeight = Math.max(20, (int)((float)(scrollbarHeight * 12) / (float)this.filteredItems.size()));
         int sliderY = listY + (int)(percent * (float)(scrollbarHeight - sliderHeight));
         gui.m_280509_(addY, sliderY, addY + 4, sliderY + sliderHeight, -5592406);
      }

      addY = this.windowY + this.windowHeight - 20;
      gui.m_280488_(this.f_96547_, "输入物品 ID 或名称:", this.windowX + 10, addY, 13421772);
      this.searchBox.m_88315_(gui, mouseX, mouseY, delta);
      if (this.itemToAdd != null) {
         ItemStack stack = new ItemStack(this.itemToAdd);
         gui.m_280480_(stack, this.windowX + 280, addY - 2);
         gui.m_280370_(this.f_96547_, stack, this.windowX + 280, addY - 2);
      }

      this.addButton.f_93623_ = this.itemToAdd != null;
      this.removeButton.f_93623_ = this.selectedIndex != -1;
      super.m_88315_(gui, mouseX, mouseY, delta);
      this.drawButtonHoverBorders(gui, mouseX, mouseY);
   }

   private void drawButtonHoverBorders(GuiGraphics gui, int mouseX, int mouseY) {
      if (this.addButton.m_274382_()) {
         this.drawFlowingGradientBorder(gui, this.addButton.m_252754_(), this.addButton.m_252907_(), this.addButton.m_252754_() + this.addButton.m_5711_(), this.addButton.m_252907_() + this.addButton.m_93694_());
      }

      if (this.removeButton.m_274382_()) {
         this.drawFlowingGradientBorder(gui, this.removeButton.m_252754_(), this.removeButton.m_252907_(), this.removeButton.m_252754_() + this.removeButton.m_5711_(), this.removeButton.m_252907_() + this.removeButton.m_93694_());
      }

      if (this.resetButton.m_274382_()) {
         this.drawFlowingGradientBorder(gui, this.resetButton.m_252754_(), this.resetButton.m_252907_(), this.resetButton.m_252754_() + this.resetButton.m_5711_(), this.resetButton.m_252907_() + this.resetButton.m_93694_());
      }

      if (this.doneButton.m_274382_()) {
         this.drawFlowingGradientBorder(gui, this.doneButton.m_252754_(), this.doneButton.m_252907_(), this.doneButton.m_252754_() + this.doneButton.m_5711_(), this.doneButton.m_252907_() + this.doneButton.m_93694_());
      }

   }

   private void drawFlowingGradientBorder(GuiGraphics gui, int left, int top, int right, int bottom) {
      int width = right - left;
      int height = bottom - top;
      long time = System.currentTimeMillis();
      float offset = (float)(time % 3000L) / 3000.0F;

      int i;
      float progress;
      int color;
      for(i = 0; i < width; ++i) {
         progress = ((float)i / (float)width + offset) % 1.0F;
         color = this.interpolateColor(this.gradientColors, progress);
         gui.m_280509_(left + i, top, left + i + 1, top + 1, color);
      }

      for(i = 0; i < width; ++i) {
         progress = (1.0F - (float)i / (float)width + offset) % 1.0F;
         color = this.interpolateColor(this.gradientColors, progress);
         gui.m_280509_(left + i, bottom - 1, left + i + 1, bottom, color);
      }

      for(i = 0; i < height; ++i) {
         progress = ((float)i / (float)height + offset) % 1.0F;
         color = this.interpolateColor(this.gradientColors, progress);
         gui.m_280509_(left, top + i, left + 1, top + i + 1, color);
      }

      for(i = 0; i < height; ++i) {
         progress = (1.0F - (float)i / (float)height + offset) % 1.0F;
         color = this.interpolateColor(this.gradientColors, progress);
         gui.m_280509_(right - 1, top + i, right, top + i + 1, color);
      }

   }

   private int interpolateColor(int[] colors, float progress) {
      int colorIndex = (int)(progress * (float)(colors.length - 1));
      float blend = progress * (float)(colors.length - 1) - (float)colorIndex;
      int color1 = colors[colorIndex];
      int color2 = colors[Math.min(colorIndex + 1, colors.length - 1)];
      int r = (int)((float)(color1 >> 16 & 255) * (1.0F - blend) + (float)(color2 >> 16 & 255) * blend);
      int g = (int)((float)(color1 >> 8 & 255) * (1.0F - blend) + (float)(color2 >> 8 & 255) * blend);
      int b = (int)((float)(color1 & 255) * (1.0F - blend) + (float)(color2 & 255) * blend);
      return -16777216 | r << 16 | g << 8 | b;
   }

   public boolean m_6375_(double mouseX, double mouseY, int button) {
      if (mouseX >= (double)this.windowX && mouseX <= (double)(this.windowX + this.windowWidth) && mouseY >= (double)this.windowY && mouseY <= (double)(this.windowY + 30)) {
         this.isDragging = true;
         this.dragX = (int)(mouseX - (double)this.windowX);
         this.dragY = (int)(mouseY - (double)this.windowY);
         return true;
      } else {
         boolean clickedButton = super.m_6375_(mouseX, mouseY, button);
         if (clickedButton) {
            return true;
         } else {
            int listY = this.windowY + 65;
            int start = this.scrollOffset;
            int end = Math.min(start + 12, this.filteredItems.size());
            boolean clickedOnList = false;

            for(int i = start; i < end; ++i) {
               int y = listY + 5 + (i - start) * 30;
               if (mouseX >= (double)(this.windowX + 5) && mouseX <= (double)(this.windowX + this.windowWidth - 5) && mouseY >= (double)y && mouseY <= (double)(y + 30 - 2)) {
                  this.selectedIndex = i;
                  clickedOnList = true;
                  break;
               }
            }

            if (!clickedOnList) {
               this.selectedIndex = -1;
            }

            this.itemToAdd = this.getItemFromSearch(this.searchBox.m_94155_());
            return true;
         }
      }
   }

   public boolean m_6348_(double mouseX, double mouseY, int button) {
      this.isDragging = false;
      return super.m_6348_(mouseX, mouseY, button);
   }

   public boolean m_7979_(double mouseX, double mouseY, int button, double dragX, double dragY) {
      if (this.isDragging) {
         this.windowX = (int)(mouseX - (double)this.dragX);
         this.windowY = (int)(mouseY - (double)this.dragY);
         this.windowX = Math.max(0, Math.min(this.windowX, this.f_96543_ - this.windowWidth));
         this.windowY = Math.max(0, Math.min(this.windowY, this.f_96544_ - this.windowHeight));
         return true;
      } else {
         return super.m_7979_(mouseX, mouseY, button, dragX, dragY);
      }
   }

   public boolean m_6050_(double mouseX, double mouseY, double delta) {
      if (mouseX >= (double)this.windowX && mouseX <= (double)(this.windowX + this.windowWidth) && mouseY >= (double)(this.windowY + 65) && mouseY <= (double)(this.windowY + this.windowHeight - 60)) {
         int maxScroll = Math.max(0, this.filteredItems.size() - 12);
         this.scrollOffset = (int)Math.max(0.0, Math.min((double)maxScroll, (double)this.scrollOffset - delta));
         return true;
      } else {
         return false;
      }
   }

   public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         this.m_7379_();
         return true;
      } else {
         return super.m_7933_(keyCode, scanCode, modifiers);
      }
   }

   private void filterItems(String search) {
      if (search == null) {
         search = "";
      }

      String lower = search.toLowerCase();
      this.filteredItems.clear();
      Iterator var3 = this.items.iterator();

      while(true) {
         String id;
         do {
            if (!var3.hasNext()) {
               this.selectedIndex = -1;
               this.scrollOffset = 0;
               this.itemToAdd = this.getItemFromSearch(search);
               if (this.addButton != null) {
                  this.addButton.f_93623_ = this.itemToAdd != null;
               }

               if (this.removeButton != null) {
                  this.removeButton.f_93623_ = false;
               }

               return;
            }

            id = (String)var3.next();
         } while(!lower.isEmpty() && !id.toLowerCase().contains(lower));

         this.filteredItems.add(id);
      }
   }

   public void m_7379_() {
      Minecraft.m_91087_().m_91152_(this.parent);
   }

   private Item getItemFromSearch(String input) {
      if (input.isEmpty()) {
         return null;
      } else {
         ResourceLocation id = ResourceLocation.m_135820_(input);
         if (id != null) {
            Item item = (Item)ForgeRegistries.ITEMS.getValue(id);
            if (item != null) {
               return item;
            }
         }

         Iterator var6 = ForgeRegistries.ITEMS.iterator();

         Item item;
         String name;
         do {
            if (!var6.hasNext()) {
               return null;
            }

            item = (Item)var6.next();
            name = item.m_41466_().getString();
         } while(!name.equalsIgnoreCase(input) && !name.contains(input));

         return item;
      }
   }

   public boolean m_7043_() {
      return false;
   }
}
