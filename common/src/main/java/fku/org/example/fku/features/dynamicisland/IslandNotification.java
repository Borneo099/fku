package fku.org.example.fku.features.dynamicisland;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class IslandNotification {
   public final String featureId;
   public final String displayName;
   public NotificationType type;
   public String text;
   public long createTime;
   public int priority;
   public transient LivingEntity targetEntity;
   public transient float damageAmount;
   public transient boolean damageMeasured = true;
   /** 可选：玩家皮肤贴图（多人玩家进/退游戏提示时携带，用于渲染头像） */
   public transient ResourceLocation skinLocation;

   public IslandNotification(String featureId, String displayName, NotificationType type, String text) {
      this.featureId = featureId;
      this.displayName = displayName;
      this.type = type;
      this.text = text;
      this.createTime = System.currentTimeMillis();
      this.priority = type.priority;
   }
}
