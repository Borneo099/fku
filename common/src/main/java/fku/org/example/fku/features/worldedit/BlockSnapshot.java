package fku.org.example.fku.features.worldedit;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

class BlockSnapshot {
    final BlockPos pos;
    final BlockState oldState;
    final Object blockEntityData;

    BlockSnapshot(BlockPos pos, BlockState oldState, Object blockEntityData) {
        this.pos = pos;
        this.oldState = oldState;
        this.blockEntityData = blockEntityData;
    }
}

