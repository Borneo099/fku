package lexis.Hack.Hackutil.Notebot;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lexis.Hack.Hacks.Misc.NotebotHack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class NotebotSongsScreen extends Screen {
   private final NotebotHack notebot;
   private final Screen parent;
   private List songs = new ArrayList();
   private List filteredSongs = new ArrayList();
   private EditBox searchBox;
   private int scrollOffset = 0;
   private static final int SONGS_PER_PAGE = 10;

   public NotebotSongsScreen(NotebotHack notebot) {
      super(Component.m_237113_("选择歌曲"));
      this.notebot = notebot;
      this.parent = Minecraft.m_91087_().f_91080_;
      this.loadSongs();
   }

   private void loadSongs() {
      this.songs.clear();
      File folder = new File("C:/karucn/Lexis/config/hack/Notebot/");
      if (!folder.exists()) {
         folder.mkdirs();
      }

      File[] files = folder.listFiles();
      if (files != null) {
         File[] var3 = files;
         int var4 = files.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            File file = var3[var5];
            if (file.isFile() && SongDecoders.hasDecoder(file)) {
               this.songs.add(file);
            }
         }
      }

      this.songs.sort((a, b) -> {
         return a.getName().compareToIgnoreCase(b.getName());
      });
      this.filteredSongs = new ArrayList(this.songs);
   }

   protected void m_7856_() {
      int centerX = this.f_96543_ / 2;
      int y = 20;
      this.searchBox = new EditBox(this.f_96547_, centerX - 150, y, 300, 20, Component.m_237113_("搜索歌曲..."));
      this.searchBox.m_94151_(this::filterSongs);
      this.m_142416_(this.searchBox);
      y += 30;
      this.m_142416_(Button.m_253074_(Component.m_237113_("随机播放"), (btn) -> {
         this.notebot.playRandomSong();
         Minecraft.m_91087_().m_91152_(this.parent);
      }).m_252987_(centerX - 100, this.f_96544_ - 55, 200, 20).m_253136_());
      this.m_142416_(Button.m_253074_(Component.m_237113_("返回"), (btn) -> {
         Minecraft.m_91087_().m_91152_(this.parent);
      }).m_252987_(centerX - 100, this.f_96544_ - 30, 200, 20).m_253136_());
   }

   private void filterSongs(String search) {
      if (search.isEmpty()) {
         this.filteredSongs = new ArrayList(this.songs);
      } else {
         String lowerSearch = search.toLowerCase();
         this.filteredSongs = new ArrayList();
         Iterator var3 = this.songs.iterator();

         while(var3.hasNext()) {
            File song = (File)var3.next();
            if (song.getName().toLowerCase().contains(lowerSearch)) {
               this.filteredSongs.add(song);
            }
         }
      }

      this.scrollOffset = 0;
   }

   public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         Minecraft.m_91087_().m_91152_(this.parent);
         return true;
      } else {
         return super.m_7933_(keyCode, scanCode, modifiers);
      }
   }

   public void m_88315_(GuiGraphics gui, int mouseX, int mouseY, float delta) {
      this.m_280273_(gui);
      super.m_88315_(gui, mouseX, mouseY, delta);
      int centerX = this.f_96543_ / 2;
      int startY = 50;
      gui.m_280137_(this.f_96547_, "选择要播放的歌曲", centerX, 5, 16766720);
      gui.m_280137_(this.f_96547_, "找到 " + this.filteredSongs.size() + " 首歌曲", centerX, 269, 11184810);
      gui.m_280488_(this.f_96547_, "§7提示：需要放在 C:/karucn/Lexis/config/hack/Notebot/<歌名>.nbs/txt", 5, this.f_96544_ - 25, 11184810);
      gui.m_280488_(this.f_96547_, "§7支持格式: .nbs/.txt", 5, this.f_96544_ - 15, 11184810);
      int listHeight = 220;
      int maxScroll = Math.max(0, this.filteredSongs.size() - 10);
      int i;
      int x;
      if (maxScroll > 0) {
         i = centerX + 160;
         gui.m_280509_(i, startY, i + 6, startY + listHeight, 1140850688);
         float scrollPercent = (float)this.scrollOffset / (float)maxScroll;
         x = Math.max(20, listHeight * 10 / this.filteredSongs.size());
         int sliderY = startY + (int)(scrollPercent * (float)(listHeight - x));
         gui.m_280509_(i, sliderY, i + 6, sliderY + x, -5592406);
      }

      for(i = 0; i < 10; ++i) {
         int index = this.scrollOffset + i;
         if (index >= this.filteredSongs.size()) {
            break;
         }

         File song = (File)this.filteredSongs.get(index);
         int y = startY + i * 22;
         x = centerX - 150;
         boolean hovered = mouseX >= x && mouseX <= x + 300 && mouseY >= y && mouseY <= y + 20;
         int bgColor = hovered ? 1714631475 : 1143087650;
         gui.m_280509_(x, y, x + 300, y + 20, bgColor);
         String name = song.getName();
         if (name.length() > 40) {
            name = name.substring(0, 37) + "...";
         }

         gui.m_280488_(this.f_96547_, name, x + 5, y + 6, 16777215);
         gui.m_280509_(x + 250, y + 2, x + 280, y + 18, hovered ? -11751600 : -1439793870);
         gui.m_280137_(this.f_96547_, "播放", x + 265, y + 5, 16777215);
         gui.m_280509_(x + 210, y + 2, x + 240, y + 18, hovered ? -14575885 : -1441438272);
         gui.m_280137_(this.f_96547_, "预览", x + 225, y + 5, 16777215);
      }

   }

   public boolean m_6375_(double mouseX, double mouseY, int button) {
      if (super.m_6375_(mouseX, mouseY, button)) {
         return true;
      } else {
         int centerX = this.f_96543_ / 2;
         int startY = 50;

         for(int i = 0; i < 10; ++i) {
            int index = this.scrollOffset + i;
            if (index >= this.filteredSongs.size()) {
               break;
            }

            int y = startY + i * 22;
            int x = centerX - 150;
            File song = (File)this.filteredSongs.get(index);
            if (mouseX >= (double)(x + 250) && mouseX <= (double)(x + 280) && mouseY >= (double)(y + 2) && mouseY <= (double)(y + 18)) {
               this.notebot.loadSong(song);
               Minecraft.m_91087_().m_91152_(this.parent);
               return true;
            }

            if (mouseX >= (double)(x + 210) && mouseX <= (double)(x + 240) && mouseY >= (double)(y + 2) && mouseY <= (double)(y + 18)) {
               this.notebot.previewSong(song);
               Minecraft.m_91087_().m_91152_(this.parent);
               return true;
            }
         }

         return false;
      }
   }

   public boolean m_6050_(double mouseX, double mouseY, double delta) {
      int maxScroll = Math.max(0, this.filteredSongs.size() - 10);
      this.scrollOffset = (int)Math.max(0.0, Math.min((double)maxScroll, (double)this.scrollOffset - delta * 3.0));
      return true;
   }

   public boolean m_7043_() {
      return false;
   }
}
