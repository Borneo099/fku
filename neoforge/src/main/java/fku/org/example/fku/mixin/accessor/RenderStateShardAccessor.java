package fku.org.example.fku.mixin.accessor; /* water */

import net.minecraft.client.renderer.RenderStateShard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderStateShard.class)
public interface RenderStateShardAccessor {
    @Accessor("NO_DEPTH_TEST")
    static RenderStateShard.DepthTestStateShard getNoDepthTest() { throw new AssertionError(); }
    @Accessor("TRANSLUCENT_TRANSPARENCY")
    static RenderStateShard.TransparencyStateShard getTranslucentTransparency() { throw new AssertionError(); }
    @Accessor("NO_CULL")
    static RenderStateShard.CullStateShard getNoCull() { throw new AssertionError(); }
    @Accessor("RENDERTYPE_LINES_SHADER")
    static RenderStateShard.ShaderStateShard getRenderTypeLinesShader() { throw new AssertionError(); }
    @Accessor("COLOR_DEPTH_WRITE")
    static RenderStateShard.WriteMaskStateShard getColorDepthWrite() { throw new AssertionError(); }
    @Accessor("NO_TEXTURE")
    static RenderStateShard.EmptyTextureStateShard getNoTexture() { throw new AssertionError(); }
}
