package fku.org.example.fku.features.dynamicisland;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public class NotificationCenter {
   private static final List queue = new ArrayList();
   private static IslandNotification active = null;

   public static void push(IslandNotification n) {
      DynamicIslandConfig cfg = DynamicIslandConfig.getInstance();
      if (cfg.enabled) {
         if (!isFiltered(n.featureId, cfg)) {
            if (!cfg.mergeSameFeature || !tryMerge(n, cfg)) {
               if (queue.size() >= cfg.maxQueueSize) {
                  dropLowest();
               }

               queue.add(n);
            }
         }
      }
   }

   public static void push(String featureId, String displayName, NotificationType type, String text) {
      push(new IslandNotification(featureId, displayName, type, text));
   }

   public static void pushEnabled(String featureId, String displayName) {
      push(featureId, displayName, NotificationType.ENABLED, "已启用");
   }

   public static void pushDisabled(String featureId, String displayName) {
      push(featureId, displayName, NotificationType.DISABLED, "已禁用");
   }

   public static void pushToggle(String featureName, boolean enabled) {
      push(featureName, featureName, enabled ? NotificationType.ENABLED : NotificationType.DISABLED, enabled ? "已开启" : "已关闭");
   }

   /** 多人世界玩家进/退游戏：携带玩家皮肤贴图，渲染时绘制头像 */
   public static void pushPlayer(String name, NotificationType type, String text, ResourceLocation skin) {
      IslandNotification n = new IslandNotification("player_session_" + name, name, type, text);
      n.skinLocation = skin;
      push(n);
   }

   static IslandNotification activeNotification() {
      return active;
   }

   static void setActive(IslandNotification n) {
      active = n;
   }

   static boolean hasPending() {
      return !queue.isEmpty();
   }

   static IslandNotification takeNext() {
      if (queue.isEmpty()) {
         return null;
      } else {
         int best = 0;

         for(int i = 1; i < queue.size(); ++i) {
            IslandNotification a = (IslandNotification)queue.get(i);
            IslandNotification b = (IslandNotification)queue.get(best);
            if (a.priority > b.priority || a.priority == b.priority && a.createTime < b.createTime) {
               best = i;
            }
         }

         return (IslandNotification)queue.remove(best);
      }
   }

   public static void clear() {
      queue.clear();
      active = null;
   }

   public static void interruptForDamage() {
      queue.clear();
      active = null;
   }

   private static boolean tryMerge(IslandNotification n, DynamicIslandConfig cfg) {
      if (active != null && active.featureId.equals(n.featureId)) {
         active.type = n.type;
         active.text = n.text;
         active.createTime = System.currentTimeMillis();
         active.priority = n.priority;
         return true;
      } else {
         for(int i = 0; i < queue.size(); ++i) {
            IslandNotification old = (IslandNotification)queue.get(i);
            if (old.featureId.equals(n.featureId)) {
               old.type = n.type;
               old.text = n.text;
               old.createTime = System.currentTimeMillis();
               old.priority = n.priority;
               return true;
            }
         }

         return false;
      }
   }

   private static boolean isFiltered(String id, DynamicIslandConfig cfg) {
      if (cfg.disabledFeatures != null && cfg.disabledFeatures.contains(id)) {
         return true;
      } else {
         return cfg.enabledFeatures != null && !cfg.enabledFeatures.isEmpty() && !cfg.enabledFeatures.contains(id);
      }
   }

   private static void dropLowest() {
      if (!queue.isEmpty()) {
         int low = 0;

         for(int i = 1; i < queue.size(); ++i) {
            IslandNotification a = (IslandNotification)queue.get(i);
            IslandNotification b = (IslandNotification)queue.get(low);
            if (a.priority < b.priority || a.priority == b.priority && a.createTime > b.createTime) {
               low = i;
            }
         }

         queue.remove(low);
      }
   }
}
