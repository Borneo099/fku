package fku.org.example.fku.mixin.accessor; /* water */

import net.minecraft.client.renderer.RenderStateShard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderStateShard.class)
public interface RenderStateShardAccessor {
    @Accessor("NO_TEXTURE")
    static RenderStateShard.EmptyTextureStateShard getNoTexture() { throw new AssertionError(); }
}