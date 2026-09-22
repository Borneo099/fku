package fku.org.example.fku.features.dynamicisland;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.level.GameType;

public class TabListRenderer {
   private static final int LINE_H = 22;
   private static final int PAD_X = 16;
   private static final int PAD_Y = 12;
   private static final int TITLE_H = 26;
   private static final int GAP = 6;

   static final class Row {
      final String text;
      final int ping;

      Row(String text, int ping) {
         this.text = text;
         this.ping = ping;
      }
   }

   public static boolean isActive(DynamicIslandConfig cfg) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null || mc.getConnection() == null || mc.screen != null) {
         return false;
      } else if (!cfg.tabShowEnabled || !cfg.enabled) {
         return false;
      } else if (!mc.options.keyPlayerList.isDown()) {
         return false;
      } else {
         var players = mc.getConnection().getListedOnlinePlayers();
         return players != null && !players.isEmpty();
      }
   }

   static List<Row> collectRows() {
      Minecraft mc = Minecraft.getInstance();
      var players = mc.getConnection().getListedOnlinePlayers();
      List<PlayerInfo> list = new ArrayList<>(players);
      list.sort(Comparator.comparingInt(PlayerInfo::getLatency));
      List<Row> rows = new ArrayList<>();

      for(PlayerInfo pi : list) {
         String mode = modeName(pi.getGameMode());
         String id = pi.getProfile().getName();
         int ping = pi.getLatency();
         rows.add(new Row("[" + mode + "] " + id + " [" + ping + "ms]", ping));
      }

      return rows;
   }

   public static int[] measure(DynamicIslandConfig cfg) {
      if (!isActive(cfg)) {
         return new int[]{cfg.capsuleWidth, cfg.capsuleHeight};
      } else {
         List<Row> rows = collectRows();
         Font font = Minecraft.getInstance().font;
         int maxW = font.width("玩家列表 (" + rows.size() + ")");

         for(Row r : rows) {
            int w = font.width(r.text);
            if (w > maxW) {
               maxW = w;
            }
         }

         int boxW = maxW + PAD_X * 2;
         int boxH = TITLE_H + GAP + LINE_H * rows.size() + PAD_Y;
         return new int[]{boxW, boxH};
      }
   }

   public static void draw(GuiGraphics g, DynamicIslandConfig cfg, int x, int y, int w, int h, int radius, Font font, int alpha) {
      List<Row> rows = collectRows();
      int headerColor = NotificationRenderer.parseColor(cfg.textColor) & 0xFFFFFF;
      g.drawString(font, "玩家列表 (" + rows.size() + ")", x + PAD_X, y + 8, alpha << 24 | headerColor);
      int sepY = y + TITLE_H;
      g.fill(x + PAD_X, sepY, x + w - PAD_X, sepY + 1, alpha << 24 | 0x3A3A3A);
      int ly = y + TITLE_H + GAP;

      for(int i = 0; i < rows.size(); ++i) {
         int rowY = ly + i * LINE_H;
         Row r = rows.get(i);
         int tc = pingColor(r.ping) & 0xFFFFFF;
         g.drawString(font, r.text, x + PAD_X, rowY + (LINE_H - 9) / 2, alpha << 24 | tc);
      }
   }

   private static String modeName(GameType t) {
      if (t == GameType.CREATIVE) {
         return "创造";
      } else if (t == GameType.ADVENTURE) {
         return "冒险";
      } else if (t == GameType.SPECTATOR) {
         return "旁观";
      } else {
         return "生存";
      }
   }

   private static int pingColor(int ping) {
      int rgb = ping < 80 ? 14335846 : (ping < 180 ? 16762570 : 16734037);
      return -16777216 | rgb;
   }
}
