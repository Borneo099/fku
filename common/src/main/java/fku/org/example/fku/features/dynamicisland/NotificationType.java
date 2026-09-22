package fku.org.example.fku.features.dynamicisland;

public enum NotificationType {
   ENABLED(5025616, 2),
   DISABLED(16007990, 2),
   INFO(2201331, 1),
   WARNING(16750592, 3),
   COMBAT(10233776, 4);

   public final int color;
   public final int priority;

   private NotificationType(int color, int priority) {
      this.color = color;
      this.priority = priority;
   }

   // $FF: synthetic method
   private static NotificationType[] $values() {
      return new NotificationType[]{ENABLED, DISABLED, INFO, WARNING, COMBAT};
   }
}
