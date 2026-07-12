package moze_intel.projecte.gameObjs.items.armor;

import java.util.List;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.items.IFireProtector;
import moze_intel.projecte.handlers.InternalTimers;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GemChest extends GemArmorBase implements IFireProtector {
   public GemChest(Item.Properties props) {
      super(Type.CHESTPLATE, props);
   }

   public void m_7373_(@NotNull ItemStack stack, @Nullable Level level, @NotNull List tooltips, @NotNull TooltipFlag flags) {
      super.m_7373_(stack, level, tooltips, flags);
      tooltips.add(PELang.GEM_LORE_CHEST.translate(new Object[0]));
   }

   public void onArmorTick(ItemStack chest, Level level, Player player) {
      if (!level.f_46443_) {
         player.getCapability(InternalTimers.CAPABILITY).ifPresent((timers) -> {
            timers.activateFeed();
            if (player.m_36324_().m_38721_() && timers.canFeed()) {
               player.m_36324_().m_38707_(2, 10.0F);
               player.m_146850_(GameEvent.f_157806_);
            }

         });
      }

   }

   public void doExplode(Player player) {
      if (ProjectEConfig.server.difficulty.offensiveAbilities.get()) {
         WorldHelper.createNovaExplosion(player.m_9236_(), player, player.m_20185_(), player.m_20186_(), player.m_20189_(), 9.0F);
      }

   }

   public boolean canProtectAgainstFire(ItemStack stack, ServerPlayer player) {
      return player.m_6844_(EquipmentSlot.CHEST) == stack;
   }
}
