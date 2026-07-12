package lexis.Client.Goto;

import java.util.Comparator;
import java.util.PriorityQueue;

public class PathQueue {
   private final PriorityQueue queue = new PriorityQueue(Comparator.comparing((e) -> {
      return e.priority;
   }));

   public boolean isEmpty() {
      return this.queue.isEmpty();
   }

   public boolean add(PathPos pos, float priority) {
      return this.queue.add(new Entry(pos, priority));
   }

   public PathPos poll() {
      Entry entry = (Entry)this.queue.poll();
      return entry != null ? entry.pos : null;
   }

   public int size() {
      return this.queue.size();
   }

   private static class Entry {
      private final PathPos pos;
      private final float priority;

      public Entry(PathPos pos, float priority) {
         this.pos = pos;
         this.priority = priority;
      }
   }
}
