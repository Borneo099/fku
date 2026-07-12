package moze_intel.projecte.gameObjs.items.armor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GemLegs extends GemArmorBase {
   private final Map lastJumpTracker = new HashMap();

   public GemLegs(Item.Properties props) {
      super(Type.LEGGINGS, props);
      MinecraftForge.EVENT_BUS.addListener(this::onJump);
   }

   public void m_7373_(@NotNull ItemStack stack, @Nullable Level level, @NotNull List tooltips, @NotNull TooltipFlag flags) {
      super.m_7373_(stack, level, tooltips, flags);
      tooltips.add(PELang.GEM_LORE_LEGS.translate(new Object[0]));
   }

   private void onJump(LivingEvent.LivingJumpEvent evt) {
      LivingEntity var3 = evt.getEntity();
      if (var3 instanceof Player player) {
         if (player.m_9236_().f_46443_) {
            this.lastJumpTracker.put(player.m_19879_(), player.m_9236_().m_46467_());
         }
      }

   }

   private boolean jumpedRecently(Player player) {
      return this.lastJumpTracker.containsKey(player.m_19879_()) && player.m_9236_().m_46467_() - (Long)this.lastJumpTracker.get(player.m_19879_()) < 5L;
   }

   public void onArmorTick(ItemStack stack, Level level, Player player) {
      if (level.f_46443_ && player.m_36341_() && !player.m_20096_() && player.m_20184_().m_7098_() > -8.0 && !this.jumpedRecently(player)) {
         player.m_20256_(player.m_20184_().m_82520_(0.0, -0.3199999928474426, 0.0));
      }

      if (player.m_36341_()) {
         AABB box = new AABB(player.m_20185_() - 3.5, player.m_20186_() - 3.5, player.m_20189_() - 3.5, player.m_20185_() + 3.5, player.m_20186_() + 3.5, player.m_20189_() + 3.5);
         WorldHelper.repelEntitiesSWRG(level, box, player);
         if (!level.f_46443_ && player.m_20184_().m_7098_() < -0.08) {
            List entities = player.m_9236_().m_6249_(player, player.m_20191_().m_82383_(player.m_20184_()).m_82400_(2.0), (entity) -> {
               return entity instanceof LivingEntity;
            });
            Iterator var6 = entities.iterator();

            while(var6.hasNext()) {
               Entity e = (Entity)var6.next();
               if (e.m_6087_()) {
                  e.m_6469_(level.m_269111_().m_269075_(player), (float)(-player.m_20184_().m_7098_()) * 6.0F);
               }
            }
         }
      }

   }
}
