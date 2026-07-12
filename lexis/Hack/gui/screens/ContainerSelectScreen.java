package lexis.Hack.gui.screens;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lexis.Hack.Hacks.Misc.ContainerCrashHack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

public class ContainerSelectScreen extends Screen {
   private final ContainerCrashHack hack;
   private final Screen parent;
   private List allContainers = new ArrayList();
   private List filteredContainers = new ArrayList();
   private Set selectedContainers;
   private EditBox searchBox;
   private int scrollOffset = 0;
   private static final int ITEMS_PER_PAGE = 15;
   private static final int ITEM_HEIGHT = 25;
   private static final File CONFIG_DIR = new File("C:/karucn/Lexis/config/hack/");
   private static final File CONFIG_FILE;
   private static final Gson GSON;

   public ContainerSelectScreen(ContainerCrashHack hack, Screen parent) {
      super(Component.m_237113_("选择容器方块"));
      this.hack = hack;
      this.parent = parent;
      this.selectedContainers = hack.getTargetContainers();
      this.loadAllContainers();
      this.loadConfig();
   }

   private void loadAllContainers() {
      this.allContainers.clear();
      Block[] commonContainers = new Block[]{Blocks.f_50087_, Blocks.f_50325_, Blocks.f_50265_, Blocks.f_50618_, Blocks.f_50456_, Blocks.f_50457_, Blocks.f_50458_, Blocks.f_50459_, Blocks.f_50460_, Blocks.f_50461_, Blocks.f_50462_, Blocks.f_50463_, Blocks.f_50464_, Blocks.f_50465_, Blocks.f_50466_, Blocks.f_50520_, Blocks.f_50521_, Blocks.f_50522_, Blocks.f_50523_, Blocks.f_50524_, Blocks.f_50525_, Blocks.f_50094_, Blocks.f_50620_, Blocks.f_50619_, Blocks.f_50332_, Blocks.f_50061_, Blocks.f_50286_, Blocks.f_50255_, Blocks.f_50091_, Blocks.f_50201_, Blocks.f_50322_, Blocks.f_50323_, Blocks.f_50324_, Blocks.f_50273_, Blocks.f_50624_};
      Block[] var2 = commonContainers;
      int var3 = commonContainers.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Block block = var2[var4];
         String registryName = ForgeRegistries.BLOCKS.getKey(block).toString();
         String displayName = this.getDisplayName(registryName);
         boolean enabled = this.selectedContainers.contains(block);
         this.allContainers.add(new BlockEntry(block, registryName, displayName, enabled));
      }

      this.allContainers.sort(Comparator.comparing((e) -> {
         return e.displayName;
      }));
      this.filteredContainers = new ArrayList(this.allContainers);
   }

   private String getDisplayName(String registryName) {
      String[] parts = registryName.split(":");
      if (parts.length > 1) {
         String name = parts[1].replace('_', ' ');
         return (String)Arrays.stream(name.split(" ")).map((word) -> {
            String var10000 = word.substring(0, 1).toUpperCase();
            return var10000 + word.substring(1).toLowerCase();
         }).collect(Collectors.joining(" "));
      } else {
         return registryName;
      }
   }

   private void loadConfig() {
      try {
         if (!CONFIG_FILE.exists()) {
            return;
         }

         FileReader reader = new FileReader(CONFIG_FILE);
         Map data = (Map)GSON.fromJson(reader, Map.class);
         reader.close();
         if (data != null && data.containsKey("selected")) {
            List selectedNames = (List)data.get("selected");

            BlockEntry entry;
            for(Iterator var4 = this.allContainers.iterator(); var4.hasNext(); entry.enabled = selectedNames.contains(entry.name)) {
               entry = (BlockEntry)var4.next();
            }
         }
      } catch (Exception var6) {
         var6.printStackTrace();
      }

   }

   private void saveConfig() {
      try {
         CONFIG_DIR.mkdirs();
         Map data = new HashMap();
         List selectedNames = new ArrayList();
         Iterator var3 = this.allContainers.iterator();

         while(var3.hasNext()) {
            BlockEntry entry = (BlockEntry)var3.next();
            if (entry.enabled) {
               selectedNames.add(entry.name);
            }
         }

         data.put("selected", selectedNames);
         FileWriter writer = new FileWriter(CONFIG_FILE);
         GSON.toJson(data, writer);
         writer.close();
      } catch (Exception var5) {
         var5.printStackTrace();
      }

   }

   protected void m_7856_() {
      super.m_7856_();
      int centerX = this.f_96543_ / 2;
      int y = 20;
      this.searchBox = new EditBox(this.f_96547_, centerX - 150, y, 300, 20, Component.m_237113_(""));
      this.searchBox.m_94199_(50);
      this.searchBox.m_94151_(this::filterContainers);
      this.m_142416_(this.searchBox);
      y += 25;
      this.m_142416_(Button.m_253074_(Component.m_237113_("全选"), (btn) -> {
         BlockEntry entry;
         for(Iterator var2 = this.filteredContainers.iterator(); var2.hasNext(); entry.enabled = true) {
            entry = (BlockEntry)var2.next();
         }

      }).m_252987_(centerX - 150, y, 80, 20).m_253136_());
      this.m_142416_(Button.m_253074_(Component.m_237113_("取消全选"), (btn) -> {
         BlockEntry entry;
         for(Iterator var2 = this.filteredContainers.iterator(); var2.hasNext(); entry.enabled = false) {
            entry = (BlockEntry)var2.next();
         }

      }).m_252987_(centerX - 60, y, 80, 20).m_253136_());
      this.m_142416_(Button.m_253074_(Component.m_237113_("反选"), (btn) -> {
         BlockEntry entry;
         for(Iterator var2 = this.filteredContainers.iterator(); var2.hasNext(); entry.enabled = !entry.enabled) {
            entry = (BlockEntry)var2.next();
         }

      }).m_252987_(centerX + 30, y, 60, 20).m_253136_());
      y += 25;
      this.m_142416_(Button.m_253074_(Component.m_237113_("保存"), (btn) -> {
         this.applySelection();
         this.saveConfig();
         this.m_7379_();
      }).m_252987_(centerX - 110, this.f_96544_ - 30, 100, 20).m_253136_());
      this.m_142416_(Button.m_253074_(Component.m_237113_("取消"), (btn) -> {
         this.m_7379_();
      }).m_252987_(centerX + 10, this.f_96544_ - 30, 100, 20).m_253136_());
   }

   private void filterContainers(String search) {
      if (search.isEmpty()) {
         this.filteredContainers = new ArrayList(this.allContainers);
      } else {
         String lowerSearch = search.toLowerCase();
         this.filteredContainers = (List)this.allContainers.stream().filter((e) -> {
            return e.displayName.toLowerCase().contains(lowerSearch) || e.name.toLowerCase().contains(lowerSearch);
         }).collect(Collectors.toList());
      }

      this.scrollOffset = 0;
   }

   private void applySelection() {
      Set selected = new HashSet();
      Iterator var2 = this.allContainers.iterator();

      while(var2.hasNext()) {
         BlockEntry entry = (BlockEntry)var2.next();
         if (entry.enabled) {
            selected.add(entry.block);
         }
      }

      this.hack.setTargetContainers(selected);
   }

   public void m_88315_(GuiGraphics gui, int mouseX, int mouseY, float delta) {
      this.m_280273_(gui);
      super.m_88315_(gui, mouseX, mouseY, delta);
      int centerX = this.f_96543_ / 2;
      int startY = 95;
      gui.m_280488_(this.f_96547_, "选择要攻击的容器方块", centerX - 100, 10, 16777130);
      gui.m_280509_(centerX - 152, startY - 2, centerX + 152, startY + 375 + 2, -1439485133);

      int scrollbarX;
      int bgColor;
      for(scrollbarX = 0; scrollbarX < 15; ++scrollbarX) {
         int index = this.scrollOffset + scrollbarX;
         if (index >= this.filteredContainers.size()) {
            break;
         }

         BlockEntry entry = (BlockEntry)this.filteredContainers.get(index);
         int y = startY + scrollbarX * 25;
         boolean hovered = mouseX >= centerX - 150 && mouseX <= centerX + 150 && mouseY >= y && mouseY <= y + 25 - 2;
         if (entry.enabled) {
            bgColor = -1437814960;
         } else if (hovered) {
            bgColor = -1436129690;
         } else {
            bgColor = -1439485133;
         }

         gui.m_280509_(centerX - 150, y, centerX + 150, y + 25 - 2, bgColor);
         gui.m_280509_(centerX - 150, y, centerX - 149, y + 25 - 2, -7829368);
         gui.m_280509_(centerX + 149, y, centerX + 150, y + 25 - 2, -7829368);
         gui.m_280488_(this.f_96547_, entry.displayName, centerX - 140, y + 5, entry.enabled ? 16777215 : 13421772);
         String status = entry.enabled ? "✓" : "✗";
         gui.m_280488_(this.f_96547_, status, centerX + 135, y + 5, entry.enabled ? 11206570 : 16755370);
      }

      if (this.filteredContainers.size() > 15) {
         scrollbarX = centerX + 155;
         int scrollbarHeight = 375;
         gui.m_280509_(scrollbarX, startY, scrollbarX + 4, startY + scrollbarHeight, -1436129690);
         float scrollPercent = (float)this.scrollOffset / (float)(this.filteredContainers.size() - 15);
         int sliderHeight = Math.max(20, (int)((float)(scrollbarHeight * 15) / (float)this.filteredContainers.size()));
         bgColor = startY + (int)(scrollPercent * (float)(scrollbarHeight - sliderHeight));
         gui.m_280509_(scrollbarX, bgColor, scrollbarX + 4, bgColor + sliderHeight, -5592406);
      }

   }

   public boolean m_6375_(double mouseX, double mouseY, int button) {
      if (super.m_6375_(mouseX, mouseY, button)) {
         return true;
      } else {
         int centerX = this.f_96543_ / 2;
         int startY = 95;

         for(int i = 0; i < 15; ++i) {
            int index = this.scrollOffset + i;
            if (index >= this.filteredContainers.size()) {
               break;
            }

            int y = startY + i * 25;
            if (mouseX >= (double)(centerX - 150) && mouseX <= (double)(centerX + 150) && mouseY >= (double)y && mouseY <= (double)(y + 25 - 2)) {
               BlockEntry entry = (BlockEntry)this.filteredContainers.get(index);
               entry.enabled = !entry.enabled;
               return true;
            }
         }

         return false;
      }
   }

   public boolean m_6050_(double mouseX, double mouseY, double delta) {
      int centerX = this.f_96543_ / 2;
      int startY = 95;
      if (mouseX >= (double)(centerX - 152) && mouseX <= (double)(centerX + 152) && mouseY >= (double)(startY - 2) && mouseY <= (double)(startY + 375 + 2)) {
         int maxScroll = Math.max(0, this.filteredContainers.size() - 15);
         this.scrollOffset = (int)Math.max(0.0, Math.min((double)maxScroll, (double)this.scrollOffset - delta * 3.0));
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
      CONFIG_FILE = new File(CONFIG_DIR, "container_crash.json");
      GSON = (new GsonBuilder()).setPrettyPrinting().create();
   }

   private static class BlockEntry {
      String name;
      String displayName;
      Block block;
      boolean enabled;

      BlockEntry(Block block, String name, String displayName, boolean enabled) {
         this.block = block;
         this.name = name;
         this.displayName = displayName;
         this.enabled = enabled;
      }
   }
}
