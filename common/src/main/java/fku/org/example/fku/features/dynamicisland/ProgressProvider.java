package fku.org.example.fku.features.dynamicisland;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

public interface ProgressProvider {
   boolean isActive(LocalPlayer var1, Minecraft var2);

   float getRatio();

   ItemStack getIcon();

   String getTitle();

   String getSubtitle();

   int getColor();

   boolean enabled(DynamicIslandConfig var1);

   default boolean hasBar() {
      return true;
   }

   default boolean isMusicProvider() {
      return false;
   }

   default float getBeatFrequency() {
      return 2.0F;
   }
}
