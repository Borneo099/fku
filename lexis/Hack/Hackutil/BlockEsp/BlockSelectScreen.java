package lexis.Hack.Hackutil.BlockEsp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lexis.Hack.Hacks.Render.BlockEspHack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockSelectScreen extends Screen {
   private final BlockEspHack hack;
   private final Screen parent;
   private List allBlocks = new ArrayList();
   private List filteredBlocks = new ArrayList();
   private Map blockSettings;
   private EditBox searchBox;
   private int scrollOffset = 0;
   private int selectedScrollOffset = 0;
   private static final int ENTRIES_PER_PAGE = 12;
   private static final int SELECTED_PER_PAGE = 8;
   private BlockEntry selectedBlock = null;
   private boolean showColorPicker = false;
   private boolean draggingRed = false;
   private boolean draggingGreen = false;
   private boolean draggingBlue = false;
   private boolean draggingDistance = false;
   private Map previousEnabledState = new HashMap();
   private long lastSaveTime = 0L;
   private static final long SAVE_DELAY = 500L;

   public BlockSelectScreen(BlockEspHack hack, Map blockSettings) {
      super(Component.m_237113_("选择透视方块"));
      this.hack = hack;
      this.parent = Minecraft.m_91087_().f_91080_;
      this.blockSettings = new HashMap();
      Iterator var3 = blockSettings.entrySet().iterator();

      while(var3.hasNext()) {
         Map.Entry entry = (Map.Entry)var3.next();
         BlockEspHack.BlockSettings bs = new BlockEspHack.BlockSettings();
         bs.enabled = ((BlockEspHack.BlockSettings)entry.getValue()).enabled;
         bs.color = ((BlockEspHack.BlockSettings)entry.getValue()).color;
         bs.maxDistance = ((BlockEspHack.BlockSettings)entry.getValue()).maxDistance;
         this.blockSettings.put((String)entry.getKey(), bs);
      }

      this.loadBlocks();
      this.saveEnabledStateSnapshot();
   }

   private void saveEnabledStateSnapshot() {
      this.previousEnabledState.clear();
      Iterator var1 = this.allBlocks.iterator();

      while(var1.hasNext()) {
         BlockEntry entry = (BlockEntry)var1.next();
         this.previousEnabledState.put(entry.id, entry.enabled);
      }

   }

   private void loadBlocks() {
      this.allBlocks.clear();
      Iterator var1 = ForgeRegistries.BLOCKS.getEntries().iterator();

      while(var1.hasNext()) {
         Map.Entry entry = (Map.Entry)var1.next();
         ResourceKey key = (ResourceKey)entry.getKey();
         ResourceLocation id = key.m_135782_();
         String blockId = id.toString();
         Block block = (Block)entry.getValue();
         if (block != Blocks.f_50016_) {
            String displayName = this.getBlockDisplayName(blockId);
            ItemStack icon = this.getBlockIcon(blockId, block);
            BlockEspHack.BlockSettings settings = (BlockEspHack.BlockSettings)this.blockSettings.get(blockId);
            this.allBlocks.add(new BlockEntry(blockId, displayName, icon, settings));
         }
      }

      this.allBlocks.sort(Comparator.comparing((e) -> {
         return e.displayName;
      }));
      this.filteredBlocks = new ArrayList(this.allBlocks);
   }

   private String getBlockDisplayName(String blockId) {
      String[] parts = blockId.split(":");
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
         return namespace.equals("minecraft") ? displayName + " §7(" + blockId + ")" : "§e[" + namespace + "]§f " + displayName + " §7(" + blockId + ")";
      } else {
         return blockId;
      }
   }

   private ItemStack getBlockIcon(String blockId, Block block) {
      try {
         if (block != null && block != Blocks.f_50016_) {
            ItemStack stack = new ItemStack(block);
            if (!stack.m_41619_()) {
               return stack;
            }
         }
      } catch (Exception var4) {
      }

      if (blockId.contains("chest")) {
         return new ItemStack(Items.f_42009_);
      } else if (blockId.contains("furnace")) {
         return new ItemStack(Items.f_41962_);
      } else if (blockId.contains("dispenser")) {
         return new ItemStack(Items.f_41855_);
      } else if (blockId.contains("dropper")) {
         return new ItemStack(Items.f_42162_);
      } else if (blockId.contains("hopper")) {
         return new ItemStack(Items.f_42155_);
      } else if (blockId.contains("piston")) {
         return new ItemStack(Items.f_41869_);
      } else if (blockId.contains("anvil")) {
         return new ItemStack(Items.f_42146_);
      } else if (blockId.contains("enchanting")) {
         return new ItemStack(Items.f_42100_);
      } else if (blockId.contains("ender_chest")) {
         return new ItemStack(Items.f_42108_);
      } else {
         return blockId.contains("beacon") ? new ItemStack(Items.f_42065_) : new ItemStack(Items.f_41905_);
      }
   }

   protected void m_7856_() {
      int centerX = this.f_96543_ / 2;
      int y = 20;
      this.searchBox = new EditBox(this.f_96547_, centerX - 150, y, 300, 20, Component.m_237113_("搜索方块..."));
      this.searchBox.m_94151_(this::filterBlocks);
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

   private void filterBlocks(String search) {
      if (search.isEmpty()) {
         this.filteredBlocks = new ArrayList(this.allBlocks);
      } else {
         String lowerSearch = search.toLowerCase();
         this.filteredBlocks = (List)this.allBlocks.stream().filter((e) -> {
            return e.displayName.toLowerCase().contains(lowerSearch) || e.id.toLowerCase().contains(lowerSearch);
         }).collect(Collectors.toList());
      }

      this.scrollOffset = 0;
      this.selectedBlock = null;
      this.showColorPicker = false;
   }

   public void m_88315_(GuiGraphics gui, int mouseX, int mouseY, float delta) {
      this.m_280273_(gui);
      super.m_88315_(gui, mouseX, mouseY, delta);
      int centerX = this.f_96543_ / 2;
      int startY = 70;
      gui.m_280137_(this.f_96547_, "选择要透视的方块", centerX, 5, 16766720);
      int listWidth = 260;
      int listHeight = 264;
      int listX = centerX - listWidth - 130;
      int listY = startY;
      gui.m_280509_(listX, startY - 15, listX + listWidth, startY - 5, -13421773);
      gui.m_280488_(this.f_96547_, "所有方块 (搜索: " + this.filteredBlocks.size() + ")", listX + 5, startY - 13, 16777130);
      int maxScroll = Math.max(0, this.filteredBlocks.size() - 12);
      int selectedX;
      int selectedMaxScroll;
      int rightX;
      if (maxScroll > 0) {
         selectedX = listX + listWidth + 6;
         gui.m_280509_(selectedX, startY, selectedX + 6, startY + listHeight, 1140850688);
         float scrollPercent = (float)this.scrollOffset / (float)maxScroll;
         selectedMaxScroll = Math.max(20, listHeight * 12 / this.filteredBlocks.size());
         rightX = startY + (int)(scrollPercent * (float)(listHeight - selectedMaxScroll));
         gui.m_280509_(selectedX, rightX, selectedX + 6, rightX + selectedMaxScroll, -5592406);
      }

      for(selectedX = 0; selectedX < 12; ++selectedX) {
         int index = this.scrollOffset + selectedX;
         if (index >= this.filteredBlocks.size()) {
            break;
         }

         BlockEntry entry = (BlockEntry)this.filteredBlocks.get(index);
         int y = listY + selectedX * 22;
         boolean hovered = mouseX >= listX && mouseX <= listX + listWidth && mouseY >= y && mouseY <= y + 20;
         rightX = entry.enabled ? -2142458032 : (hovered ? 1714631475 : 1143087650);
         gui.m_280509_(listX, y, listX + listWidth, y + 20, rightX);
         gui.m_280480_(entry.icon, listX + 2, y + 2);
         String name = entry.displayName.length() > 20 ? entry.displayName.substring(0, 20) + "..." : entry.displayName;
         gui.m_280488_(this.f_96547_, name, listX + 25, y + 6, entry.enabled ? 16777215 : 13421772);
         if (hovered) {
            gui.m_280509_(listX, y, listX + listWidth, y + 20, 872415231);
         }
      }

      selectedX = centerX - 110;
      int selectedWidth = 220;
      int selectedY = startY;
      gui.m_280509_(selectedX, startY - 15, selectedX + selectedWidth, startY - 5, -13421773);
      gui.m_280488_(this.f_96547_, "已选择的方块 (" + this.getSelectedCount() + ")", selectedX + 5, startY - 13, 16777130);
      List selectedList = this.getSelectedBlocks();
      selectedMaxScroll = Math.max(0, selectedList.size() - 8);
      short rightWidth;
      int color;
      if (selectedMaxScroll > 0) {
         rightX = selectedX + selectedWidth + 6;
         rightWidth = 176;
         gui.m_280509_(rightX, startY, rightX + 6, startY + rightWidth, 1140850688);
         float scrollPercent = (float)this.selectedScrollOffset / (float)selectedMaxScroll;
         int sliderHeight = Math.max(20, rightWidth * 8 / selectedList.size());
         color = startY + (int)(scrollPercent * (float)(rightWidth - sliderHeight));
         gui.m_280509_(rightX, color, rightX + 6, color + sliderHeight, -5592406);
      }

      int b;
      for(rightX = 0; rightX < 8; ++rightX) {
         int index = this.selectedScrollOffset + rightX;
         if (index >= selectedList.size()) {
            break;
         }

         BlockEntry entry = (BlockEntry)selectedList.get(index);
         int y = selectedY + rightX * 22;
         boolean hovered = mouseX >= selectedX && mouseX <= selectedX + selectedWidth && mouseY >= y && mouseY <= y + 20;
         boolean deleteHovered = mouseX >= selectedX + selectedWidth - 25 && mouseX <= selectedX + selectedWidth - 10 && mouseY >= y + 5 && mouseY <= y + 15;
         gui.m_280509_(selectedX, y, selectedX + selectedWidth, y + 20, hovered ? 1714631475 : 1143087650);
         gui.m_280480_(entry.icon, selectedX + 2, y + 2);
         String name = entry.displayName.length() > 12 ? entry.displayName.substring(0, 12) + "..." : entry.displayName;
         gui.m_280488_(this.f_96547_, name, selectedX + 25, y + 6, 16777215);
         if (entry.settings != null) {
            float[] color = entry.settings.getColorF();
            b = -16777216 | (int)(color[0] * 255.0F) << 16 | (int)(color[1] * 255.0F) << 8 | (int)(color[2] * 255.0F);
            gui.m_280509_(selectedX + selectedWidth - 55, y + 5, selectedX + selectedWidth - 35, y + 15, b);
         }

         gui.m_280509_(selectedX + selectedWidth - 25, y + 5, selectedX + selectedWidth - 10, y + 15, deleteHovered ? -43691 : -1426115789);
         gui.m_280488_(this.f_96547_, "✕", selectedX + selectedWidth - 20, y + 5, 16777215);
         if (hovered) {
            gui.m_280509_(selectedX, y, selectedX + selectedWidth, y + 20, 872415231);
         }
      }

      if (this.selectedBlock != null) {
         if (this.selectedBlock.settings == null) {
            this.selectedBlock.settings = new BlockEspHack.BlockSettings();
            this.selectedBlock.settings.enabled = true;
            this.selectedBlock.settings.color = -16711936;
            this.selectedBlock.settings.maxDistance = 64.0;
         }

         rightX = centerX + 140;
         rightWidth = 200;
         int rightHeight = 240;
         gui.m_280509_(rightX, startY, rightX + rightWidth, startY + rightHeight, -1439485133);
         String displayName = this.selectedBlock.displayName.length() > 15 ? this.selectedBlock.displayName.substring(0, 15) + "..." : this.selectedBlock.displayName;
         gui.m_280488_(this.f_96547_, "设置: " + displayName, rightX + 10, startY + 10, 16777130);
         color = this.selectedBlock.settings.color;
         int r = color >> 16 & 255;
         int g = color >> 8 & 255;
         b = color & 255;
         int sliderY = startY + 35;
         this.drawColorSlider(gui, rightX + 10, sliderY, "R", r, 16733525, this.draggingRed, mouseX, mouseY, this.selectedBlock);
         this.drawColorSlider(gui, rightX + 10, sliderY + 40, "G", g, 5635925, this.draggingGreen, mouseX, mouseY, this.selectedBlock);
         this.drawColorSlider(gui, rightX + 10, sliderY + 80, "B", b, 5592575, this.draggingBlue, mouseX, mouseY, this.selectedBlock);
         int distY = sliderY + 130;
         gui.m_280488_(this.f_96547_, "最大距离: " + (int)this.selectedBlock.settings.maxDistance + " 格", rightX + 10, distY, 16777215);
         gui.m_280509_(rightX + 10, distY + 12, rightX + 190, distY + 22, -13421773);

         int indicatorX;
         for(indicatorX = 0; indicatorX <= 180; indicatorX += 2) {
            float progress = (float)indicatorX / 180.0F;
            int gradientColor = -11162881;
            gui.m_280509_(rightX + 10 + indicatorX, distY + 12, rightX + 12 + indicatorX, distY + 22, gradientColor);
         }

         indicatorX = rightX + 10 + (int)((this.selectedBlock.settings.maxDistance - 16.0) * 180.0 / 1008.0);
         gui.m_280509_(indicatorX - 3, distY + 10, indicatorX + 3, distY + 24, -1);
         if (this.draggingDistance) {
            double newDist = (double)(16 + (mouseX - rightX - 10) * 1008 / 180);
            this.selectedBlock.settings.maxDistance = Math.max(16.0, Math.min(1024.0, newDist));
            this.blockSettings.put(this.selectedBlock.id, this.selectedBlock.settings);
            this.saveSettings();
         }

         int previewColor = this.selectedBlock.settings.color;
         gui.m_280509_(rightX + 10, distY + 35, rightX + 190, distY + 60, previewColor);
      }

      this.updateDragging(mouseX, mouseY);
   }

   private void drawColorSlider(GuiGraphics gui, int x, int y, String label, int value, int color, boolean dragging, int mouseX, int mouseY, BlockEntry entry) {
      gui.m_280488_(this.f_96547_, label + ": " + value, x, y, 16777215);
      gui.m_280509_(x, y + 12, x + 180, y + 22, -13421773);

      int indicatorX;
      int currentColor;
      int r;
      int g;
      int b;
      for(indicatorX = 0; indicatorX <= 180; indicatorX += 2) {
         float progress = (float)indicatorX / 180.0F;
         currentColor = label.equals("R") ? (int)(progress * 255.0F) : entry.settings.color >> 16 & 255;
         r = label.equals("G") ? (int)(progress * 255.0F) : entry.settings.color >> 8 & 255;
         g = label.equals("B") ? (int)(progress * 255.0F) : entry.settings.color & 255;
         b = -16777216 | currentColor << 16 | r << 8 | g;
         gui.m_280509_(x + indicatorX, y + 12, x + indicatorX + 2, y + 22, b);
      }

      indicatorX = x + value * 180 / 255;
      gui.m_280509_(indicatorX - 3, y + 10, indicatorX + 3, y + 24, -1);
      if (dragging) {
         int newValue = (mouseX - x) * 255 / 180;
         newValue = Math.max(0, Math.min(255, newValue));
         currentColor = entry.settings.color;
         r = label.equals("R") ? newValue : currentColor >> 16 & 255;
         g = label.equals("G") ? newValue : currentColor >> 8 & 255;
         b = label.equals("B") ? newValue : currentColor & 255;
         entry.settings.color = -16777216 | r << 16 | g << 8 | b;
         this.blockSettings.put(entry.id, entry.settings);
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
            if (index >= this.filteredBlocks.size()) {
               break;
            }

            rightX = startY + selectedX * 22;
            if (mouseX >= (double)listX && mouseX <= (double)(listX + listWidth) && mouseY >= (double)rightX && mouseY <= (double)(rightX + 20)) {
               BlockEntry entry = (BlockEntry)this.filteredBlocks.get(index);
               if (button == 0) {
                  if (entry.settings == null) {
                     entry.settings = new BlockEspHack.BlockSettings();
                     entry.settings.enabled = true;
                     entry.settings.color = -16711936;
                     entry.settings.maxDistance = 64.0;
                  }

                  entry.enabled = !entry.enabled;
                  entry.settings.enabled = entry.enabled;
                  this.blockSettings.put(entry.id, entry.settings);
               } else if (button == 1) {
                  this.selectedBlock = entry;
                  if (this.selectedBlock.settings == null) {
                     this.selectedBlock.settings = new BlockEspHack.BlockSettings();
                     this.selectedBlock.settings.enabled = true;
                     this.selectedBlock.settings.color = -16711936;
                     this.selectedBlock.settings.maxDistance = 64.0;
                  }

                  this.showColorPicker = true;
               }

               return true;
            }
         }

         selectedX = centerX - 110;
         int selectedWidth = 220;

         for(rightX = 0; rightX < 8; ++rightX) {
            int index = this.selectedScrollOffset + rightX;
            List selectedList = this.getSelectedBlocks();
            if (index >= selectedList.size()) {
               break;
            }

            int y = startY + rightX * 22;
            BlockEntry entry;
            if (mouseX >= (double)(selectedX + selectedWidth - 25) && mouseX <= (double)(selectedX + selectedWidth - 10) && mouseY >= (double)(y + 5) && mouseY <= (double)(y + 15)) {
               entry = (BlockEntry)selectedList.get(index);
               entry.enabled = false;
               if (entry.settings != null) {
                  entry.settings.enabled = false;
               }

               this.blockSettings.put(entry.id, entry.settings);
               return true;
            }

            if (mouseX >= (double)selectedX && mouseX <= (double)(selectedX + selectedWidth) && mouseY >= (double)y && mouseY <= (double)(y + 20)) {
               entry = (BlockEntry)selectedList.get(index);
               this.selectedBlock = entry;
               if (this.selectedBlock.settings == null) {
                  this.selectedBlock.settings = new BlockEspHack.BlockSettings();
                  this.selectedBlock.settings.enabled = true;
                  this.selectedBlock.settings.color = -16711936;
                  this.selectedBlock.settings.maxDistance = 64.0;
               }

               this.showColorPicker = true;
               return true;
            }
         }

         if (this.selectedBlock != null && this.showColorPicker) {
            rightX = centerX + 140;
            int sliderY = startY + 35;
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

               if (mouseY >= (double)(sliderY + 142) && mouseY <= (double)(sliderY + 152)) {
                  this.draggingDistance = true;
                  return true;
               }
            }
         }

         return false;
      }
   }

   public boolean m_6348_(double mouseX, double mouseY, int button) {
      if (this.draggingRed || this.draggingGreen || this.draggingBlue || this.draggingDistance) {
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
      return super.m_6348_(mouseX, mouseY, button);
   }

   private void updateDragging(int mouseX, int mouseY) {
   }

   public boolean m_6050_(double mouseX, double mouseY, double delta) {
      int centerX = this.f_96543_ / 2;
      int maxSelectedScroll;
      if (mouseX < (double)(centerX - 50)) {
         maxSelectedScroll = Math.max(0, this.filteredBlocks.size() - 12);
         this.scrollOffset = (int)Math.max(0.0, Math.min((double)maxSelectedScroll, (double)this.scrollOffset - delta * 3.0));
      } else if (mouseX < (double)(centerX + 100)) {
         maxSelectedScroll = Math.max(0, this.getSelectedBlocks().size() - 8);
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
      return (int)this.allBlocks.stream().filter((e) -> {
         return e.enabled;
      }).count();
   }

   private List getSelectedBlocks() {
      return (List)this.allBlocks.stream().filter((e) -> {
         return e.enabled;
      }).collect(Collectors.toList());
   }

   private void saveSettings() {
      Iterator var1 = this.allBlocks.iterator();

      BlockEntry entry;
      while(var1.hasNext()) {
         entry = (BlockEntry)var1.next();
         if (entry.settings != null) {
            this.blockSettings.put(entry.id, entry.settings);
         }
      }

      this.hack.getBlockSettings().clear();
      var1 = this.blockSettings.entrySet().iterator();

      while(var1.hasNext()) {
         Map.Entry entry = (Map.Entry)var1.next();
         BlockEspHack.BlockSettings bs = new BlockEspHack.BlockSettings();
         bs.enabled = ((BlockEspHack.BlockSettings)entry.getValue()).enabled;
         bs.color = ((BlockEspHack.BlockSettings)entry.getValue()).color;
         bs.maxDistance = ((BlockEspHack.BlockSettings)entry.getValue()).maxDistance;
         this.hack.getBlockSettings().put((String)entry.getKey(), bs);
      }

      this.hack.saveBlockSettings();
      var1 = this.allBlocks.iterator();

      while(var1.hasNext()) {
         entry = (BlockEntry)var1.next();
         boolean wasEnabled = (Boolean)this.previousEnabledState.getOrDefault(entry.id, false);
         if (entry.enabled && !wasEnabled) {
            this.hack.enableBlockType(entry.id);
         }
      }

      this.saveEnabledStateSnapshot();
   }

   public boolean m_7043_() {
      return false;
   }

   private static class BlockEntry {
      String id;
      String displayName;
      ItemStack icon;
      BlockEspHack.BlockSettings settings;
      boolean enabled;

      BlockEntry(String id, String displayName, ItemStack icon, BlockEspHack.BlockSettings settings) {
         this.id = id;
         this.displayName = displayName;
         this.icon = icon;
         this.settings = settings;
         this.enabled = settings != null && settings.enabled;
      }
   }
}
