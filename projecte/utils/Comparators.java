package moze_intel.projecte.utils;

import java.util.Comparator;
import net.minecraft.world.item.Item;

public final class Comparators {
   public static final Comparator ITEMSTACK_ASCENDING = (o1, o2) -> {
      if (o1.m_41619_() && o2.m_41619_()) {
         return 0;
      } else if (o1.m_41619_()) {
         return 1;
      } else if (o2.m_41619_()) {
         return -1;
      } else {
         return o1.m_41720_() != o2.m_41720_() ? o1.m_41613_() - o2.m_41613_() : Item.m_41393_(o1.m_41720_()) - Item.m_41393_(o2.m_41720_());
      }
   };
}
