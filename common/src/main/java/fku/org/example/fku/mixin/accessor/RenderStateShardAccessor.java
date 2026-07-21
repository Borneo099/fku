package fku.org.example.fku.mixin.accessor;

import net.minecraft.client.renderer.RenderStateShard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={RenderStateShard.class})
public interface RenderStateShardAccessor {
    @Accessor(value="NO_DEPTH_TEST")
    public static RenderStateShard.DepthTestStateShard getNoDepthTest() {
        throw new AssertionError();
    }

    @Accessor(value="TRANSLUCENT_TRANSPARENCY")
    public static RenderStateShard.TransparencyStateShard getTranslucentTransparency() {
        throw new AssertionError();
    }

    @Accessor(value="NO_CULL")
    public static RenderStateShard.CullStateShard getNoCull() {
        throw new AssertionError();
    }

    @Accessor(value="RENDERTYPE_LINES_SHADER")
    public static RenderStateShard.ShaderStateShard getRenderTypeLinesShader() {
        throw new AssertionError();
    }

    @Accessor(value="COLOR_DEPTH_WRITE")
    public static RenderStateShard.WriteMaskStateShard getColorDepthWrite() {
        throw new AssertionError();
    }

    @Accessor(value="NO_TEXTURE")
    public static RenderStateShard.EmptyTextureStateShard getNoTexture() {
        throw new AssertionError();
    }
}

