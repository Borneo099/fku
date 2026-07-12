package moze_intel.projecte.gameObjs.items;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import moze_intel.projecte.api.capabilities.item.IItemCharge;
import moze_intel.projecte.capability.ChargeItemCapabilityWrapper;
import moze_intel.projecte.gameObjs.registries.PESoundEvents;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class DestructionCatalyst extends ItemPE implements IItemCharge, IBarHelper {
   public DestructionCatalyst(Item.Properties props) {
      super(props);
      this.addItemCapability(ChargeItemCapabilityWrapper::new);
   }

   public @NotNull InteractionResult m_6225_(UseOnContext ctx) {
      Player player = ctx.m_43723_();
      if (player == null) {
         return InteractionResult.FAIL;
      } else {
         Level level = ctx.m_43725_();
         if (level.f_46443_) {
            return InteractionResult.SUCCESS;
         } else {
            ItemStack stack = ctx.m_43722_();
            int numRows = this.calculateDepthFromCharge(stack);
            boolean hasAction = false;
            List drops = new ArrayList();
            BlockPos var10000 = ctx.m_8083_();
            Direction var10001 = ctx.m_43719_();
            --numRows;
            Iterator var8 = WorldHelper.getPositionsFromBox(WorldHelper.getDeepBox(var10000, var10001, numRows)).iterator();

            while(var8.hasNext()) {
               BlockPos pos = (BlockPos)var8.next();
               if (!level.m_46859_(pos)) {
                  BlockState state = level.m_8055_(pos);
                  float hardness = state.m_60800_(level, pos);
                  if (hardness != -1.0F && !(hardness >= 50.0F)) {
                     if (!consumeFuel(player, stack, 8L, true)) {
                        break;
                     }

                     hasAction = true;
                     pos = pos.m_7949_();
                     if (PlayerHelper.hasBreakPermission((ServerPlayer)player, pos)) {
                        List list = Block.m_49874_(state, (ServerLevel)level, pos, WorldHelper.getBlockEntity(level, pos), player, stack);
                        drops.addAll(list);
                        level.m_7471_(pos, false);
                        if (level.f_46441_.m_188503_(8) == 0) {
                           ((ServerLevel)level).m_8767_(level.f_46441_.m_188499_() ? ParticleTypes.f_123759_ : ParticleTypes.f_123755_, (double)pos.m_123341_(), (double)pos.m_123342_(), (double)pos.m_123343_(), 2, 0.0, 0.0, 0.0, 0.05);
                        }
                     }
                  }
               }
            }

            if (hasAction) {
               WorldHelper.createLootDrop(drops, level, ctx.m_8083_());
               level.m_6263_((Player)null, player.m_20185_(), player.m_20186_(), player.m_20189_(), (SoundEvent)PESoundEvents.DESTRUCT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }

            return InteractionResult.CONSUME;
         }
      }
   }

   private int calculateDepthFromCharge(ItemStack stack) {
      int charge = this.getCharge(stack);
      if (charge <= 0) {
         return 1;
      } else {
         return this instanceof CataliticLens ? 8 + 8 * charge : (int)Math.pow(2.0, (double)(1 + charge));
      }
   }

   public int getNumCharges(@NotNull ItemStack stack) {
      return 3;
   }

   public boolean m_142522_(@NotNull ItemStack stack) {
      return true;
   }

   public float getWidthForBar(ItemStack stack) {
      return 1.0F - this.getChargePercent(stack);
   }

   public int m_142158_(@NotNull ItemStack stack) {
      return this.getScaledBarWidth(stack);
   }

   public int m_142159_(@NotNull ItemStack stack) {
      return this.getColorForBar(stack);
   }
}
