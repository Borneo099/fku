package lexis.item;

import java.util.Iterator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class MenuUtils {
   public static void giveMenuCompass(ServerPlayer player) {
      ItemStack compass = createMenuCompass();
      boolean hasCompass = false;
      Iterator var3 = player.m_150109_().f_35974_.iterator();

      while(var3.hasNext()) {
         ItemStack stack = (ItemStack)var3.next();
         if (isMenuCompass(stack)) {
            hasCompass = true;
            break;
         }
      }

      if (!hasCompass) {
         player.m_150109_().m_36054_(compass);
      }

   }

   public static ItemStack createMenuCompass() {
      ItemStack compass = new ItemStack(Items.f_42522_);
      CompoundTag tag = new CompoundTag();
      tag.m_128344_("lexiscd", (byte)1);
      compass.m_41751_(tag);
      compass.m_41714_(Component.m_237113_("§d§lLexis菜单 §7(右键打开)"));
      return compass;
   }

   public static boolean isMenuCompass(ItemStack stack) {
      if (stack.m_41720_() != Items.f_42522_) {
         return false;
      } else {
         CompoundTag tag = stack.m_41783_();
         return tag != null && tag.m_128441_("lexiscd") && tag.m_128445_("lexiscd") == 1;
      }
   }
}
