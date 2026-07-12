package lexis.Hack.Hackutil.music;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LyricsParser {
   private static final Pattern P = Pattern.compile("\\[(\\d+):(\\d+)\\.(\\d+)](.*)");

   public static List parse(String lrc) {
      List lines = new ArrayList();
      if (lrc == null) {
         return lines;
      } else {
         String[] var2 = lrc.split("\n");
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            String l = var2[var4];
            Matcher m = P.matcher(l);
            if (m.find()) {
               long min = Long.parseLong(m.group(1));
               long sec = Long.parseLong(m.group(2));
               long ms = Long.parseLong(m.group(3));
               if (m.group(3).length() == 2) {
                  ms *= 10L;
               }

               String text = m.group(4).trim();
               if (!text.isEmpty()) {
                  lines.add(new Line(min * 60000L + sec * 1000L + ms, text));
               }
            }
         }

         lines.sort(Comparator.comparingLong((a) -> {
            return a.timeMs;
         }));
         return lines;
      }
   }

   public static String getCurrentLine(List lines, long ms) {
      if (lines.isEmpty()) {
         return "";
      } else {
         String current = "";

         Line l;
         for(Iterator var4 = lines.iterator(); var4.hasNext(); current = l.text) {
            l = (Line)var4.next();
            if (l.timeMs > ms) {
               break;
            }
         }

         return current;
      }
   }

   public static class Line {
      public long timeMs;
      public String text;

      public Line(long t, String s) {
         this.timeMs = t;
         this.text = s;
      }
   }
}
