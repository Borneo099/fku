package lexis.mixin.accessor;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({LevelRenderer.class})
public interface WorldRendererAccessor {
   @Accessor("destroyingBlocks")
   Int2ObjectMap getDestroyingBlocks();
}
