package fku.org.example.fku.features.liquidglass;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import fku.org.example.fku.Fku;
import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="fku", bus=Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public class LiquidGlassShaderRegistry {
    @Nullable
    private static ShaderInstance liquidGlassShader;

    @Nullable
    public static ShaderInstance getLiquidGlassShader() {
        return liquidGlassShader;
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation("fku", "liquid_glass"), DefaultVertexFormat.POSITION_COLOR), shader -> {
            liquidGlassShader = shader;
        });
        Fku.LOGGER.info("[LiquidGlass] \u5df2\u6ce8\u518c\u7740\u8272\u5668");
    }
}

