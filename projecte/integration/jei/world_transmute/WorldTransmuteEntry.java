package moze_intel.projecte.integration.jei.world_transmute;

import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import moze_intel.projecte.api.imc.WorldTransmutationEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import org.jetbrains.annotations.Nullable;

public class WorldTransmuteEntry {
   private static final StateInfo EMPTY;
   private final StateInfo input;
   private final StateInfo leftOutput;
   private final StateInfo rightOutput;

   public WorldTransmuteEntry(WorldTransmutationEntry transmutationEntry) {
      BlockState leftOutputState = transmutationEntry.result();
      BlockState rightOutputState = transmutationEntry.altResult();
      if (leftOutputState == rightOutputState) {
         rightOutputState = null;
      }

      this.input = this.createInfo(transmutationEntry.origin());
      this.leftOutput = this.createInfo(leftOutputState);
      this.rightOutput = this.createInfo(rightOutputState);
   }

   private StateInfo createInfo(@Nullable BlockState output) {
      if (output == null) {
         return EMPTY;
      } else {
         FluidStack outputFluid = this.fluidFromBlock(output.m_60734_());
         return outputFluid.isEmpty() ? new StateInfo(this.itemFromBlock(output.m_60734_(), output), outputFluid) : new StateInfo(ItemStack.f_41583_, outputFluid);
      }
   }

   private FluidStack fluidFromBlock(Block block) {
      if (block instanceof LiquidBlock liquidBlock) {
         return new FluidStack(liquidBlock.getFluid(), 1000);
      } else if (block instanceof IFluidBlock fluidBlock) {
         return new FluidStack(fluidBlock.getFluid(), 1000);
      } else {
         return FluidStack.EMPTY;
      }
   }

   private ItemStack itemFromBlock(Block block, BlockState state) {
      try {
         return block.getCloneItemStack(state, (HitResult)null, (BlockGetter)null, (BlockPos)null, (Player)null);
      } catch (Exception var4) {
         return new ItemStack(block);
      }
   }

   public boolean isRenderable() {
      return !this.input.isEmpty() && (!this.leftOutput.isEmpty() || !this.rightOutput.isEmpty());
   }

   public Optional getInput() {
      return this.input.isEmpty() ? Optional.empty() : Optional.of(this.input.toEither());
   }

   public Iterable getOutput() {
      List outputs = new ArrayList();
      if (!this.leftOutput.isEmpty()) {
         outputs.add(this.leftOutput.toEither());
      }

      if (!this.rightOutput.isEmpty()) {
         outputs.add(this.rightOutput.toEither());
      }

      return outputs;
   }

   public ItemStack getInputItem() {
      return this.input.item();
   }

   public FluidStack getInputFluid() {
      return this.input.fluid();
   }

   static {
      EMPTY = new StateInfo(ItemStack.f_41583_, FluidStack.EMPTY);
   }

   private static record StateInfo(ItemStack item, FluidStack fluid) {
      private StateInfo(ItemStack item, FluidStack fluid) {
         this.item = item;
         this.fluid = fluid;
      }

      public boolean isEmpty() {
         return this.item.m_41619_() && this.fluid.isEmpty();
      }

      public Either toEither() {
         return this.fluid.isEmpty() ? Either.left(this.item) : Either.right(this.fluid);
      }

      public ItemStack item() {
         return this.item;
      }

      public FluidStack fluid() {
         return this.fluid;
      }
   }
}
