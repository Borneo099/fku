package lexis.Hack.Utils.Chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;

public final class RainbowTagProcessor {
   private static final String TAG = "[Lexis]";
   private static final long CYCLE_MS = 3000L;
   private static final float RED_FREQ = 2.0F;
   private static final float GREEN_FREQ = 1.3F;
   private static final float BLUE_FREQ = 1.7F;
   private static final float CHAR_PHASE_STEP = 0.4F;

   private RainbowTagProcessor() {
   }

   public static Component tryRebuild(FormattedCharSequence input, long timeMs) {
      List chars = collectChars(input);
      if (chars.isEmpty()) {
         return null;
      } else {
         int matchStart = findTag(chars);
         if (matchStart < 0) {
            return null;
         } else {
            MutableComponent result = Component.m_237119_();
            appendOriginal(result, chars, 0, matchStart);
            int len = "[Lexis]".length();
            double basePhase = 6.283185307179586 * (double)timeMs / 3000.0;

            for(int i = 0; i < len; ++i) {
               double phase = basePhase + (double)((float)i * 0.4F);
               int r = (int)((Math.sin(2.0 * phase) + 1.0) * 127.5);
               int g = (int)((Math.sin(1.2999999523162842 * phase) + 1.0) * 127.5);
               int b = (int)((Math.sin(1.7000000476837158 * phase) + 1.0) * 127.5);
               int rgb = r << 16 | g << 8 | b;
               Style baseStyle = ((CharInfo)chars.get(matchStart + i)).style;
               Style rainbowStyle = baseStyle.m_131148_(TextColor.m_131266_(rgb));
               result.m_7220_(Component.m_237113_(String.valueOf("[Lexis]".charAt(i))).m_6270_(rainbowStyle));
            }

            appendOriginal(result, chars, matchStart + len, chars.size());
            return result;
         }
      }
   }

   private static List collectChars(FormattedCharSequence seq) {
      List list = new ArrayList();
      seq.m_13731_((idx, style, codePoint) -> {
         list.add(new CharInfo(codePoint, style));
         return true;
      });
      return list;
   }

   private static int findTag(List chars) {
      int n = chars.size();
      int len = "[Lexis]".length();

      label24:
      for(int i = 0; i <= n - len; ++i) {
         for(int j = 0; j < len; ++j) {
            if (((CharInfo)chars.get(i + j)).codePoint != "[Lexis]".charAt(j)) {
               continue label24;
            }
         }

         return i;
      }

      return -1;
   }

   private static void appendOriginal(MutableComponent result, List chars, int from, int to) {
      if (from < to) {
         StringBuilder sb = new StringBuilder();
         Style currentStyle = ((CharInfo)chars.get(from)).style;

         for(int i = from; i < to; ++i) {
            CharInfo c = (CharInfo)chars.get(i);
            if (!Objects.equals(c.style, currentStyle)) {
               result.m_7220_(Component.m_237113_(sb.toString()).m_6270_(currentStyle));
               sb.setLength(0);
               currentStyle = c.style;
            }

            sb.appendCodePoint(c.codePoint);
         }

         if (sb.length() > 0) {
            result.m_7220_(Component.m_237113_(sb.toString()).m_6270_(currentStyle));
         }

      }
   }

   private static record CharInfo(int codePoint, Style style) {
      private CharInfo(int codePoint, Style style) {
         this.codePoint = codePoint;
         this.style = style;
      }

      public int codePoint() {
         return this.codePoint;
      }

      public Style style() {
         return this.style;
      }
   }
}
