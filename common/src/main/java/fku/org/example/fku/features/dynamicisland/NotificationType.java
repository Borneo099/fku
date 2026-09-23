package fku.org.example.fku.features.dynamicisland;

public enum NotificationType {
   ENABLED(5025616, 2),
   DISABLED(16007990, 2),
   INFO(2201331, 1),
   WARNING(16750592, 3),
   COMBAT(10233776, 4),
   SESSION(5096993, 2),
   /** 多人世界：其他玩家进入游戏（绿色，与「启用」同色系） */
   PLAYER_JOIN(5025616, 2),
   /** 多人世界：其他玩家退出游戏（红色，与「禁用」同色系） */
   PLAYER_LEAVE(16007990, 2);

   public final int color;
   public final int priority;

   private NotificationType(int color, int priority) {
      this.color = color;
      this.priority = priority;
   }
}
