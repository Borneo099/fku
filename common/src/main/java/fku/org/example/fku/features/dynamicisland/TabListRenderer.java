package fku.org.example.fku.features.dynamicisland;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;

public class TabListRenderer {
   private static final int LINE_H = 22;
   private static final int PAD_X = 16;
   private static final int PAD_Y = 12;
   private static final int TITLE_H = 26;
   private static final int GAP = 6;
   private static final int DOT = 7;
   private static final int DOT_GAP = 6;

   static final class Row {
      final GameType mode;
      final Component name;
      final int ping;

      Row(GameType mode, Component name, int ping) {
         this.mode = mode;
         this.name = name;
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
         rows.add(new Row(pi.getGameMode(), nameComponent(pi), pi.getLatency()));
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
         int contentMax = 0;

         for(Row r : rows) {
            String modeStr = "[" + modeName(r.mode) + "] ";
            int cw = font.width(modeStr) + font.width(r.name) + 12 + font.width(r.ping + "ms");
            if (cw > contentMax) {
               contentMax = cw;
            }
         }

         if (contentMax > maxW) {
            maxW = contentMax;
         }

         int boxW = maxW + PAD_X * 2 + DOT + DOT_GAP;
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
         int y0 = rowY + (LINE_H - 9) / 2;
         // 斑马行背景，提升长列表可读性
         if (i % 2 == 1) {
            GuiRenderHelper.drawRoundedRectSmooth(g, x + PAD_X / 2, rowY - 1, w - PAD_X, LINE_H - 2, alpha << 24 | 0x16181C, 4);
         }

         // 模式色圆点（创造橘/生存绿/冒险红/旁观蓝）
         int modeCol = modeColor(r.mode) & 0xFFFFFF;
         GuiRenderHelper.drawRoundedRectSmooth(g, x + PAD_X, rowY + (LINE_H - DOT) / 2, DOT, DOT, alpha << 24 | modeCol, DOT / 2);
         int cx = x + PAD_X + DOT + DOT_GAP;
         // 模式名（彩色）
         String modeStr = "[" + modeName(r.mode) + "] ";
         g.drawString(font, modeStr, cx, y0, alpha << 24 | modeCol);
         cx += font.width(modeStr);
         // 玩家名（常态纯白，有队伍随队伍色）
         g.drawString(font, r.name, cx, y0, alpha << 24 | 0xFFFFFF);
         // ping（低绿/中黄/高红，右对齐）
         String pingStr = r.ping + "ms";
         int px = x + w - PAD_X - font.width(pingStr);
         g.drawString(font, pingStr, px, y0, alpha << 24 | (pingColor(r.ping) & 0xFFFFFF));
      }
   }

   /** 取玩家在 Tab 列表应显示的名字：常态纯白；只有在队伍里才把「队伍颜色」套到玩家 id 上，
    *  不拼接任何前缀/后缀文本（避免和灵动岛自绘的延迟数重叠）。 */
   private static Component nameComponent(PlayerInfo pi) {
      String id = pi.getProfile().getName();
      TextColor c = teamColor(pi);
      if (c != null) {
         return Component.literal(id).withStyle(style -> style.withColor(c));
      }
      return Component.literal(id).withStyle(style -> style.withColor(TextColor.fromRgb(0xFFFFFF)));
   }

   /** 取玩家的队伍颜色：★ 只认「队伍本身的颜色」（/team modify <队> color <色> 设置的那个）。
    *  不再解析队伍前缀/后缀里塞的颜色 —— 很多服务器会自动把玩家塞进 scoreboard 队伍并把
    *  Rank/名字色写进 prefix/suffix，那不是真正的队伍色，会把玩家 id 染得像跟着模式色走。
    *  无队伍 / 队伍未设置颜色 → 返回 null，名字保持纯白。 */
   private static TextColor teamColor(PlayerInfo pi) {
      Minecraft mc = Minecraft.getInstance();
      // 取关卡计分板：mc.level 在刚进服/维度切换瞬间可能暂时为 null，但 mc.player 已就绪，
      // 用 player.level() 兜底，避免计分板分支被跳过导致队伍色取不到（表现为要来回切一次才上色）。
      Level lvl = mc.level;
      if (lvl == null && mc.player != null) lvl = mc.player.level();
      if (lvl != null) {
         PlayerTeam team = lvl.getScoreboard().getPlayersTeam(pi.getProfile().getName());
         if (team != null) {
            ChatFormatting cf = team.getColor();
            if (cf != null && cf != ChatFormatting.RESET) {
               Integer rgb = cf.getColor();
               if (rgb != null) return TextColor.fromRgb(rgb);
            }
         }
      }
      return null;
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

   private static int modeColor(GameType t) {
      if (t == GameType.CREATIVE) {
         return 0xFF9F1C;
      } else if (t == GameType.ADVENTURE) {
         return 0xFF5C5C;
      } else if (t == GameType.SPECTATOR) {
         return 0x4DA6FF;
      } else {
         return 0x57C84D;
      }
   }

   private static int pingColor(int ping) {
      int rgb;
      if (ping < 120) {
         rgb = 0x57C84D;
      } else if (ping < 200) {
         rgb = 0xE6C84B;
      } else {
         rgb = 0xFF5C5C;
      }

      return -16777216 | rgb;
   }
}
