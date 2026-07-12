package moze_intel.projecte.gameObjs.items.rings;

import java.util.Iterator;
import java.util.List;
import moze_intel.projecte.api.capabilities.item.IExtraFunction;
import moze_intel.projecte.api.capabilities.item.IProjectileShooter;
import moze_intel.projecte.capability.ExtraFunctionItemCapabilityWrapper;
import moze_intel.projecte.capability.ModeChangerItemCapabilityWrapper;
import moze_intel.projecte.capability.ProjectileShooterItemCapabilityWrapper;
import moze_intel.projecte.gameObjs.entity.EntityFireProjectile;
import moze_intel.projecte.gameObjs.entity.EntitySWRGProjectile;
import moze_intel.projecte.gameObjs.items.IFireProtector;
import moze_intel.projecte.gameObjs.items.IFlightProvider;
import moze_intel.projecte.gameObjs.items.IItemMode;
import moze_intel.projecte.gameObjs.items.ItemPE;
import moze_intel.projecte.gameObjs.registries.PESoundEvents;
import moze_intel.projecte.integration.IntegrationHelper;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.text.ILangEntry;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Arcana extends ItemPE implements IItemMode, IFlightProvider, IFireProtector, IExtraFunction, IProjectileShooter {
   private static final ILangEntry[] modes;

   public Arcana(Item.Properties props) {
      super(props);
      this.addItemCapability(ExtraFunctionItemCapabilityWrapper::new);
      this.addItemCapability(ProjectileShooterItemCapabilityWrapper::new);
      this.addItemCapability(ModeChangerItemCapabilityWrapper::new);
      this.addItemCapability("curios", IntegrationHelper.CURIO_CAP_SUPPLIER);
   }

   public boolean hasCraftingRemainingItem(ItemStack stack) {
      return true;
   }

   public ItemStack getCraftingRemainingItem(ItemStack stack) {
      return stack.m_41777_();
   }

   public ILangEntry[] getModeLangEntries() {
      return modes;
   }

   private void tick(ItemStack stack, Level level, ServerPlayer player) {
      if (ItemHelper.checkItemNBT(stack, "Active")) {
         switch (this.getMode(stack)) {
            case 0:
               WorldHelper.freezeInBoundingBox(level, player.m_20191_().m_82400_(5.0), player, true);
               break;
            case 1:
               WorldHelper.igniteNearby(level, player);
               break;
            case 2:
               WorldHelper.growNearbyRandomly(true, level, player.m_20183_(), player);
               break;
            case 3:
               WorldHelper.repelEntitiesSWRG(level, player.m_20191_().m_82400_(5.0), player);
         }
      }

   }

   public void m_6883_(@NotNull ItemStack stack, Level level, @NotNull Entity entity, int slot, boolean held) {
      if (!level.f_46443_ && slot < Inventory.m_36059_() && entity instanceof ServerPlayer player) {
         this.tick(stack, level, player);
      }

   }

   public void m_7373_(@NotNull ItemStack stack, @Nullable Level level, @NotNull List tooltips, @NotNull TooltipFlag flags) {
      super.m_7373_(stack, level, tooltips, flags);
      if (ItemHelper.checkItemNBT(stack, "Active")) {
         tooltips.add(this.getToolTip(stack));
      } else {
         tooltips.add(PELang.TOOLTIP_ARCANA_INACTIVE.translateColored(ChatFormatting.RED, new Object[0]));
      }

   }

   public @NotNull InteractionResultHolder m_7203_(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      if (!level.f_46443_) {
         CompoundTag compound = player.m_21120_(hand).m_41784_();
         compound.m_128379_("Active", !compound.m_128471_("Active"));
      }

      return InteractionResultHolder.m_19090_(player.m_21120_(hand));
   }

   public @NotNull InteractionResult m_6225_(UseOnContext ctx) {
      if (this.getMode(ctx.m_43722_()) == 1) {
         InteractionResult result = WorldHelper.igniteBlock(ctx);
         if (result != InteractionResult.PASS) {
            return result;
         }
      }

      return super.m_6225_(ctx);
   }

   public boolean doExtraFunction(@NotNull ItemStack stack, @NotNull Player player, InteractionHand hand) {
      Level level = player.m_9236_();
      if (level.f_46443_) {
         return true;
      } else {
         if (this.getMode(stack) == 1) {
            Iterator var5;
            BlockPos pos;
            label33:
            switch (player.m_6350_()) {
               case SOUTH:
               case NORTH:
                  var5 = BlockPos.m_121940_(player.m_20183_().m_7918_(-30, -5, -3), player.m_20183_().m_7918_(30, 5, 3)).iterator();

                  while(true) {
                     if (!var5.hasNext()) {
                        break label33;
                     }

                     pos = (BlockPos)var5.next();
                     if (level.m_46859_(pos)) {
                        PlayerHelper.checkedPlaceBlock((ServerPlayer)player, pos.m_7949_(), Blocks.f_50083_.m_49966_());
                     }
                  }
               case WEST:
               case EAST:
                  var5 = BlockPos.m_121940_(player.m_20183_().m_7918_(-3, -5, -30), player.m_20183_().m_7918_(3, 5, 30)).iterator();

                  while(var5.hasNext()) {
                     pos = (BlockPos)var5.next();
                     if (level.m_46859_(pos)) {
                        PlayerHelper.checkedPlaceBlock((ServerPlayer)player, pos.m_7949_(), Blocks.f_50083_.m_49966_());
                     }
                  }
            }

            level.m_6263_((Player)null, player.m_20185_(), player.m_20186_(), player.m_20189_(), (SoundEvent)PESoundEvents.POWER.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
         }

         return true;
      }
   }

   public boolean shootProjectile(@NotNull Player player, @NotNull ItemStack stack, InteractionHand hand) {
      Level level = player.m_9236_();
      if (level.f_46443_) {
         return false;
      } else {
         switch (this.getMode(stack)) {
            case 0:
               Snowball snowball = new Snowball(level, player);
               snowball.m_37251_(player, player.m_146909_(), player.m_146908_(), 0.0F, 1.5F, 1.0F);
               level.m_7967_(snowball);
               snowball.m_5496_(SoundEvents.f_12473_, 1.0F, 1.0F);
               break;
            case 1:
               EntityFireProjectile fire = new EntityFireProjectile(player, true, level);
               fire.m_37251_(player, player.m_146909_(), player.m_146908_(), 0.0F, 1.5F, 1.0F);
               level.m_7967_(fire);
               fire.m_5496_((SoundEvent)PESoundEvents.POWER.get(), 1.0F, 1.0F);
            case 2:
            default:
               break;
            case 3:
               EntitySWRGProjectile lightning = new EntitySWRGProjectile(player, true, level);
               lightning.m_37251_(player, player.m_146909_(), player.m_146908_(), 0.0F, 1.5F, 1.0F);
               level.m_7967_(lightning);
         }

         return true;
      }
   }

   public boolean canProtectAgainstFire(ItemStack stack, ServerPlayer player) {
      return true;
   }

   public boolean canProvideFlight(ItemStack stack, ServerPlayer player) {
      return true;
   }

   static {
      modes = new ILangEntry[]{PELang.MODE_ARCANA_1, PELang.MODE_ARCANA_2, PELang.MODE_ARCANA_3, PELang.MODE_ARCANA_4};
   }
}
