package lexis.Hack.Hackutil.Notebot;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

public enum InstrumentDetectMode {
   BlockState((noteBlock, blockPos) -> {
      return (NoteBlockInstrument)noteBlock.m_61143_(NoteBlock.f_55011_);
   }),
   BelowBlock((noteBlock, blockPos) -> {
      Minecraft mc = Minecraft.m_91087_();
      return mc.f_91073_ != null ? mc.f_91073_.m_8055_(blockPos.m_7495_()).m_280603_() : (NoteBlockInstrument)noteBlock.m_61143_(NoteBlock.f_55011_);
   });

   private final InstrumentDetectFunction function;

   private InstrumentDetectMode(InstrumentDetectFunction function) {
      this.function = function;
   }

   public InstrumentDetectFunction getFunction() {
      return this.function;
   }

   // $FF: synthetic method
   private static InstrumentDetectMode[] $values() {
      return new InstrumentDetectMode[]{BlockState, BelowBlock};
   }

   public interface InstrumentDetectFunction {
      NoteBlockInstrument detectInstrument(BlockState noteBlock, BlockPos blockPos);
   }
}
