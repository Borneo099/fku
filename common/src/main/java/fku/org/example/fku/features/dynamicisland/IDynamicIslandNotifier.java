package fku.org.example.fku.features.dynamicisland;

import net.minecraft.world.item.ItemStack;

public interface IDynamicIslandNotifier {
   String getFeatureId();

   String getFeatureDisplayName();

   default ItemStack getFeatureIcon() {
      return ItemStack.EMPTY;
   }

   default void pushIsland(NotificationType type, String text) {
      NotificationCenter.push(this.getFeatureId(), this.getFeatureDisplayName(), type, text);
   }
}
