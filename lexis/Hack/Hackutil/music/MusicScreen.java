package lexis.Hack.Hackutil.music;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MusicScreen extends Screen {
   private final Screen parent;
   private EditBox searchBox;
   private List results = new ArrayList();
   private int scrollOffset = 0;
   private Button searchButton;
   private Component searchStatus = Component.m_237113_("");
   private boolean isSearching = false;

   public MusicScreen(Screen parent) {
      super(Component.m_237113_("Lexis Music"));
      this.parent = parent;
   }

   protected void m_7856_() {
      int centerX = this.f_96543_ / 2;
      int y = 40;
      this.searchBox = new EditBox(this.f_96547_, centerX - 200, y, 320, 22, Component.m_237113_("搜索"));
      this.searchBox.m_257771_(Component.m_237113_("输入歌曲名/歌手..."));
      this.m_142416_(this.searchBox);
      this.searchButton = Button.m_253074_(Component.m_237113_("搜索"), (b) -> {
         this.doSearch();
      }).m_252987_(centerX + 130, y, 70, 22).m_253136_();
      this.m_142416_(this.searchButton);
      this.m_142416_(Button.m_253074_(Component.m_237113_("暂停"), (b) -> {
         if (MusicPlayer.isPlaying()) {
            MusicPlayer.pause();
         } else {
            MusicPlayer.resume();
         }

      }).m_252987_(20, 20, 80, 20).m_253136_());
      this.m_142416_(Button.m_253074_(Component.m_237113_(MusicState.isLoopEnabled() ? "循环开" : "循环关"), (b) -> {
         MusicState.toggleLoop();
         b.m_93666_(Component.m_237113_(MusicState.isLoopEnabled() ? "循环开" : "循环关"));
      }).m_252987_(110, 20, 80, 20).m_253136_());
      this.m_142416_(Button.m_253074_(Component.m_237113_("不想听了"), (b) -> {
         MusicState.stop();
      }).m_252987_(this.f_96543_ - 100, 20, 90, 20).m_253136_());
   }

   private void doSearch() {
      String kw = this.searchBox.m_94155_();
      if (!kw.isEmpty()) {
         if (!this.isSearching) {
            this.searchStatus = Component.m_237113_("正在搜索中...");
            this.searchButton.f_93623_ = false;
            this.isSearching = true;
            (new Thread(() -> {
               this.performSearchWithRetry(kw, 0);
            })).start();
         }
      }
   }

   private void performSearchWithRetry(String keyword, int attempt) {
      List list = null;

      try {
         list = NeteaseAPI.search(keyword);
         if (list != null && !list.isEmpty()) {
            this.f_96541_.execute(() -> {
               this.searchStatus = Component.m_237113_("搜索成功，找到 " + list.size() + " 首音乐");
               this.results = list;
               this.scrollOffset = 0;
               this.searchButton.f_93623_ = true;
               this.isSearching = false;
            });
         } else {
            throw new Exception("No results");
         }
      } catch (Exception var7) {
         if (attempt < 2) {
            try {
               Thread.sleep(1000L);
            } catch (InterruptedException var6) {
            }

            this.performSearchWithRetry(keyword, attempt + 1);
         } else {
            this.f_96541_.execute(() -> {
               this.searchStatus = Component.m_237113_("§c搜索失败，请检查网络 或 重新试试搜索 或 找不到的音乐");
               this.searchButton.f_93623_ = true;
               this.isSearching = false;
            });
         }

      }
   }

   public void m_88315_(GuiGraphics gfx, int mx, int my, float dt) {
      gfx.m_280509_(0, 0, this.f_96543_, this.f_96544_, -1072689131);
      super.m_88315_(gfx, mx, my, dt);
      int centerX = this.f_96543_ / 2;
      gfx.m_280137_(this.f_96547_, "§l网易云音乐", centerX, 15, 16777215);
      if (!this.searchStatus.getString().isEmpty()) {
         int statusColor = this.searchStatus.getString().contains("§a") ? 5635925 : (this.searchStatus.getString().contains("§c") ? 16733525 : 16777045);
         gfx.m_280653_(this.f_96547_, this.searchStatus, centerX, 70, statusColor);
      }

      int y = 95;
      gfx.m_280588_(0, y, this.f_96543_, this.f_96544_ - 30);

      int by;
      int coverX;
      for(by = this.scrollOffset; by < this.results.size() && by < this.scrollOffset + 20; ++by) {
         MusicInfo m = (MusicInfo)this.results.get(by);
         coverX = y + (by - this.scrollOffset) * 32;
         boolean hover = mx >= centerX - 250 && mx <= centerX + 250 && my >= coverX && my <= coverX + 30;
         gfx.m_280509_(centerX - 250, coverX, centerX + 250, coverX + 30, hover ? 1627389951 : 1073741824);
         gfx.m_280488_(this.f_96547_, m.name, centerX - 240, coverX + 5, 16777215);
         gfx.m_280488_(this.f_96547_, "§7" + m.artist + " - " + m.album, centerX - 240, coverX + 18, 11184810);
      }

      gfx.m_280618_();
      int bw;
      int bx;
      if (MusicState.current != null) {
         by = this.f_96544_ - 25;
         gfx.m_280509_(0, by, this.f_96543_, this.f_96544_, -536870912);
         int coverSize = 18;
         coverX = this.f_96543_ - coverSize - 10;
         bx = by + (25 - coverSize) / 2;
         if (MusicState.coverTexture != null) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            gfx.m_280163_(MusicState.coverTexture, coverX, bx, 0.0F, 0.0F, coverSize, coverSize, coverSize, coverSize);
         }

         String txt = "♪ " + MusicState.current.name + " - " + MusicState.current.artist;
         bw = coverX - 20;
         if (this.f_96547_.m_92895_(txt) > bw) {
            String var10000 = this.f_96547_.m_92834_(txt, bw - 6);
            txt = var10000 + "..";
         }

         gfx.m_280488_(this.f_96547_, txt, 10, by + 8, 16777215);
         long cur = MusicPlayer.getCurrentMs();
         long tot = MusicPlayer.getTotalMs();
         if (tot > 0L) {
            int barW = bw - 20;
            int bx = 10;
            gfx.m_280509_(bx, by + 18, bx + barW, by + 22, 1090519039);
            int fill = (int)((double)((long)barW * cur) / (double)tot);
            gfx.m_280509_(bx, by + 18, bx + fill, by + 22, -16733441);
         }
      }

      Iterator var21 = this.m_6702_().iterator();

      while(var21.hasNext()) {
         GuiEventListener widget = (GuiEventListener)var21.next();
         if (widget instanceof Button btn) {
            bx = btn.m_252754_();
            int by = btn.m_252907_();
            bw = btn.m_5711_();
            int bh = btn.m_93694_();
            if (mx >= bx && mx <= bx + bw && my >= by && my <= by + bh) {
               gfx.m_280509_(bx, by, bx + bw, by + bh, -1996488705);
            }
         }
      }

   }

   public boolean m_6375_(double mx, double my, int btn) {
      int centerX = this.f_96543_ / 2;
      int y = 95;

      for(int i = this.scrollOffset; i < this.results.size() && i < this.scrollOffset + 20; ++i) {
         int rowY = y + (i - this.scrollOffset) * 32;
         if (mx >= (double)(centerX - 250) && mx <= (double)(centerX + 250) && my >= (double)rowY && my <= (double)(rowY + 30)) {
            MusicState.stop();
            MusicState.playSong((MusicInfo)this.results.get(i));
            return true;
         }
      }

      return super.m_6375_(mx, my, btn);
   }

   public boolean m_6050_(double mouseX, double mouseY, double delta) {
      this.scrollOffset = Math.max(0, Math.min(this.results.size() - 1, this.scrollOffset - (int)delta));
      return true;
   }

   public void m_7379_() {
      if (this.parent != null) {
         Minecraft.m_91087_().m_91152_(this.parent);
      } else {
         super.m_7379_();
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

   public boolean m_7043_() {
      return false;
   }
}
