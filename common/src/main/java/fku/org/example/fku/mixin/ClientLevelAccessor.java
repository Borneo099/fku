package fku.org.example.fku.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ClientLevel.class})
public interface ClientLevelAccessor {
    @Accessor(value="blockStatePredictionHandler")
    public BlockStatePredictionHandler getBlockStatePredictionHandler_CU();
}

