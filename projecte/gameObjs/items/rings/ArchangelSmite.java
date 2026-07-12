package moze_intel.projecte.gameObjs.items.rings;

import java.util.ArrayList;
import java.util.List;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.block_entity.IDMPedestal;
import moze_intel.projecte.api.capabilities.item.IPedestalItem;
import moze_intel.projecte.capability.PedestalItemCapabilityWrapper;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.entity.EntityHomingArrow;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.to_server.LeftClickArchangelPKT;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.MathUtils;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import org.jetbrains.annotations.NotNull;

public class ArchangelSmite extends PEToggleItem implements IPedestalItem {
   public ArchangelSmite(Item.Properties props) {
      super(props);
      MinecraftForge.EVENT_BUS.addListener(this::emptyLeftClick);
      MinecraftForge.EVENT_BUS.addListener(this::leftClickBlock);
      this.addItemCapability(PedestalItemCapabilityWrapper::new);
   }

   public void fireVolley(ItemStack stack, Player player) {
      for(int i = 0; i < 10; ++i) {
         this.fireArrow(stack, player.m_9236_(), player, 4.0F);
      }

   }

   private void emptyLeftClick(PlayerInteractEvent.LeftClickEmpty evt) {
      PacketHandler.sendToServer(new LeftClickArchangelPKT());
   }

   private void leftClickBlock(PlayerInteractEvent.LeftClickBlock evt) {
      if (!evt.getLevel().f_46443_ && evt.getUseItem() != Result.DENY && !evt.getItemStack().m_41619_() && evt.getItemStack().m_41720_() == this) {
         this.fireVolley(evt.getItemStack(), evt.getEntity());
      }

   }

   public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
      if (!player.m_9236_().f_46443_) {
         this.fireVolley(stack, player);
      }

      return super.onLeftClickEntity(stack, player, entity);
   }

   public void m_6883_(@NotNull ItemStack stack, Level level, @NotNull Entity entity, int invSlot, boolean isSelected) {
      if (!level.f_46443_ && this.getMode(stack) == 1 && entity instanceof LivingEntity) {
         this.fireArrow(stack, level, (LivingEntity)entity, 1.0F);
      }

   }

   public @NotNull InteractionResultHolder m_7203_(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      if (!level.f_46443_) {
         this.fireArrow(player.m_21120_(hand), level, player, 1.0F);
      }

      return InteractionResultHolder.m_19090_(player.m_21120_(hand));
   }

   private void fireArrow(ItemStack ring, Level level, LivingEntity shooter, float inaccuracy) {
      EntityHomingArrow arrow = new EntityHomingArrow(level, shooter, 2.0F);
      if (shooter instanceof Player player) {
         if (!consumeFuel(player, ring, EMCHelper.getEmcValue((ItemLike)Items.f_42412_), true)) {
            return;
         }
      }

      arrow.m_37251_(shooter, shooter.m_146909_(), shooter.m_146908_(), 0.0F, 3.0F, inaccuracy);
      level.m_6263_((Player)null, shooter.m_20185_(), shooter.m_20186_(), shooter.m_20189_(), SoundEvents.f_11687_, SoundSource.PLAYERS, 1.0F, 1.0F / (level.f_46441_.m_188501_() * 0.4F + 1.2F));
      level.m_7967_(arrow);
   }

   public boolean updateInPedestal(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockEntity pedestal) {
      if (!level.f_46443_ && ProjectEConfig.server.cooldown.pedestal.archangel.get() != -1) {
         if (((IDMPedestal)pedestal).getActivityCooldown() == 0) {
            if (!level.m_45976_(Mob.class, ((IDMPedestal)pedestal).getEffectBounds()).isEmpty()) {
               double centeredX = (double)pos.m_123341_() + 0.5;
               double centeredY = (double)pos.m_123342_() + 0.5;
               double centeredZ = (double)pos.m_123343_() + 0.5;

               for(int i = 0; i < 3; ++i) {
                  EntityHomingArrow arrow = new EntityHomingArrow(level, FakePlayerFactory.get((ServerLevel)level, PECore.FAKEPLAYER_GAMEPROFILE), 2.0F);
                  arrow.m_20343_(centeredX, centeredY + 2.0, centeredZ);
                  arrow.m_20334_(0.0, 1.0, 0.0);
                  arrow.m_5496_(SoundEvents.f_11687_, 1.0F, 1.0F / (level.f_46441_.m_188501_() * 0.4F + 1.2F) + 0.5F);
                  level.m_7967_(arrow);
               }
            }

            ((IDMPedestal)pedestal).setActivityCooldown(ProjectEConfig.server.cooldown.pedestal.archangel.get());
         } else {
            ((IDMPedestal)pedestal).decrementActivityCooldown();
         }
      }

      return false;
   }

   public @NotNull List getPedestalDescription() {
      List list = new ArrayList();
      if (ProjectEConfig.server.cooldown.pedestal.archangel.get() != -1) {
         list.add(PELang.PEDESTAL_ARCHANGEL_1.translateColored(ChatFormatting.BLUE, new Object[0]));
         list.add(PELang.PEDESTAL_ARCHANGEL_2.translateColored(ChatFormatting.BLUE, new Object[]{MathUtils.tickToSecFormatted(ProjectEConfig.server.cooldown.pedestal.archangel.get())}));
      }

      return list;
   }
}
