package moze_intel.projecte.gameObjs.items.rings;

import java.util.List;
import moze_intel.projecte.api.capabilities.item.IAlchBagItem;
import moze_intel.projecte.api.capabilities.item.IAlchChestItem;
import moze_intel.projecte.api.capabilities.item.IExtraFunction;
import moze_intel.projecte.api.capabilities.item.IPedestalItem;
import moze_intel.projecte.capability.ExtraFunctionItemCapabilityWrapper;
import moze_intel.projecte.capability.PedestalItemCapabilityWrapper;
import moze_intel.projecte.gameObjs.items.GemEternalDensity;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.utils.PlayerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class VoidRing extends GemEternalDensity implements IPedestalItem, IExtraFunction {
   public VoidRing(Item.Properties props) {
      super(props);
      this.addItemCapability(PedestalItemCapabilityWrapper::new);
      this.addItemCapability(ExtraFunctionItemCapabilityWrapper::new);
   }

   public void m_6883_(@NotNull ItemStack stack, Level level, @NotNull Entity entity, int slot, boolean isHeld) {
      super.m_6883_(stack, level, entity, slot, isHeld);
      ((BlackHoleBand)PEItems.BLACK_HOLE_BAND.get()).m_6883_(stack, level, entity, slot, isHeld);
   }

   public boolean updateInPedestal(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockEntity pedestal) {
      return ((IPedestalItem)PEItems.BLACK_HOLE_BAND.get()).updateInPedestal(stack, level, pos, pedestal);
   }

   public @NotNull List getPedestalDescription() {
      return ((IPedestalItem)PEItems.BLACK_HOLE_BAND.get()).getPedestalDescription();
   }

   public boolean doExtraFunction(@NotNull ItemStack stack, @NotNull Player player, InteractionHand hand) {
      if (player.m_36335_().m_41519_(this)) {
         return false;
      } else {
         BlockHitResult lookingAt = PlayerHelper.getBlockLookingAt(player, 64.0);
         BlockPos c;
         if (lookingAt.m_6662_() == Type.MISS) {
            c = BlockPos.m_274446_((Position)PlayerHelper.getLookVec(player, 32.0).getRight());
         } else {
            c = lookingAt.m_82425_();
         }

         EntityTeleportEvent event = new EntityTeleportEvent(player, (double)c.m_123341_(), (double)c.m_123342_(), (double)c.m_123343_());
         if (!MinecraftForge.EVENT_BUS.post(event)) {
            if (player.m_20159_()) {
               player.m_8127_();
            }

            player.m_6021_(event.getTargetX(), event.getTargetY(), event.getTargetZ());
            player.m_9236_().m_6263_((Player)null, player.m_20185_(), player.m_20186_(), player.m_20189_(), SoundEvents.f_11852_, SoundSource.PLAYERS, 1.0F, 1.0F);
            player.f_19789_ = 0.0F;
            player.m_36335_().m_41524_(this, 10);
            return true;
         } else {
            return false;
         }
      }
   }

   public boolean updateInAlchBag(@NotNull IItemHandler inv, @NotNull Player player, @NotNull ItemStack stack) {
      return super.updateInAlchBag(inv, player, stack) | ((IAlchBagItem)PEItems.BLACK_HOLE_BAND.get()).updateInAlchBag(inv, player, stack);
   }

   public boolean updateInAlchChest(@NotNull Level level, @NotNull BlockPos pos, @NotNull ItemStack stack) {
      return super.updateInAlchChest(level, pos, stack) | ((IAlchChestItem)PEItems.BLACK_HOLE_BAND.get()).updateInAlchChest(level, pos, stack);
   }
}
