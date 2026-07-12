package moze_intel.projecte.gameObjs.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import moze_intel.projecte.api.block_entity.IDMPedestal;
import moze_intel.projecte.api.capabilities.item.IPedestalItem;
import moze_intel.projecte.api.capabilities.item.IProjectileShooter;
import moze_intel.projecte.capability.PedestalItemCapabilityWrapper;
import moze_intel.projecte.capability.ProjectileShooterItemCapabilityWrapper;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.entity.EntityLavaProjectile;
import moze_intel.projecte.gameObjs.registries.PESoundEvents;
import moze_intel.projecte.integration.IntegrationHelper;
import moze_intel.projecte.utils.ClientKeyHelper;
import moze_intel.projecte.utils.MathUtils;
import moze_intel.projecte.utils.PEKeybind;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VolcaniteAmulet extends ItemPE implements IProjectileShooter, IPedestalItem, IFireProtector {
   public VolcaniteAmulet(Item.Properties props) {
      super(props);
      this.addItemCapability(PedestalItemCapabilityWrapper::new);
      this.addItemCapability(ProjectileShooterItemCapabilityWrapper::new);
      this.addItemCapability("curios", IntegrationHelper.CURIO_CAP_SUPPLIER);
   }

   public boolean hasCraftingRemainingItem(ItemStack stack) {
      return true;
   }

   public ItemStack getCraftingRemainingItem(ItemStack stack) {
      return stack.m_41777_();
   }

   public @NotNull InteractionResult m_6225_(UseOnContext ctx) {
      Level level = ctx.m_43725_();
      Player player = ctx.m_43723_();
      BlockPos pos = ctx.m_8083_();
      ItemStack stack = ctx.m_43722_();
      if (player != null && !level.f_46443_ && PlayerHelper.hasEditPermission((ServerPlayer)player, pos) && consumeFuel(player, stack, 32L, true)) {
         BlockEntity blockEntity = WorldHelper.getBlockEntity(level, pos);
         Direction sideHit = ctx.m_43719_();
         if (blockEntity != null) {
            Optional capability = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, sideHit).resolve();
            if (capability.isPresent()) {
               ((IFluidHandler)capability.get()).fill(new FluidStack(Fluids.f_76195_, 1000), FluidAction.EXECUTE);
               return InteractionResult.CONSUME;
            }
         }

         WorldHelper.placeFluid((ServerPlayer)player, level, pos, sideHit, Fluids.f_76195_, false);
         level.m_6263_((Player)null, player.m_20185_(), player.m_20186_(), player.m_20189_(), (SoundEvent)PESoundEvents.TRANSMUTE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
      }

      return InteractionResult.m_19078_(level.f_46443_);
   }

   public boolean shootProjectile(@NotNull Player player, @NotNull ItemStack stack, InteractionHand hand) {
      player.m_9236_().m_6263_((Player)null, player.m_20185_(), player.m_20186_(), player.m_20189_(), (SoundEvent)PESoundEvents.TRANSMUTE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
      EntityLavaProjectile ent = new EntityLavaProjectile(player, player.m_9236_());
      ent.m_37251_(player, player.m_146909_(), player.m_146908_(), 0.0F, 1.5F, 1.0F);
      player.m_9236_().m_7967_(ent);
      return true;
   }

   public void m_7373_(@NotNull ItemStack stack, @Nullable Level level, @NotNull List tooltips, @NotNull TooltipFlag flags) {
      super.m_7373_(stack, level, tooltips, flags);
      tooltips.add(PELang.TOOLTIP_VOLCANITE_1.translate(new Object[]{ClientKeyHelper.getKeyName(PEKeybind.FIRE_PROJECTILE)}));
      tooltips.add(PELang.TOOLTIP_VOLCANITE_2.translate(new Object[0]));
      tooltips.add(PELang.TOOLTIP_VOLCANITE_3.translate(new Object[0]));
      tooltips.add(PELang.TOOLTIP_VOLCANITE_4.translate(new Object[0]));
   }

   public boolean updateInPedestal(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockEntity pedestal) {
      if (!level.f_46443_ && ProjectEConfig.server.cooldown.pedestal.volcanite.get() != -1) {
         if (((IDMPedestal)pedestal).getActivityCooldown() == 0) {
            LevelData var6 = level.m_6106_();
            if (var6 instanceof ServerLevelData) {
               ServerLevelData worldInfo = (ServerLevelData)var6;
               worldInfo.m_6399_(0);
               worldInfo.m_6398_(0);
               worldInfo.m_5565_(false);
               worldInfo.m_5557_(false);
            }

            ((IDMPedestal)pedestal).setActivityCooldown(ProjectEConfig.server.cooldown.pedestal.volcanite.get());
         } else {
            ((IDMPedestal)pedestal).decrementActivityCooldown();
         }
      }

      return false;
   }

   public @NotNull List getPedestalDescription() {
      List list = new ArrayList();
      if (ProjectEConfig.server.cooldown.pedestal.volcanite.get() != -1) {
         list.add(PELang.PEDESTAL_VOLCANITE_1.translateColored(ChatFormatting.BLUE, new Object[0]));
         list.add(PELang.PEDESTAL_VOLCANITE_2.translateColored(ChatFormatting.BLUE, new Object[]{MathUtils.tickToSecFormatted(ProjectEConfig.server.cooldown.pedestal.volcanite.get())}));
      }

      return list;
   }

   public boolean canProtectAgainstFire(ItemStack stack, ServerPlayer player) {
      return true;
   }
}
