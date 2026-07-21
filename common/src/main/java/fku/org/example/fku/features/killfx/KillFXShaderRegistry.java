package fku.org.example.fku.features.killfx;

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
public class KillFXShaderRegistry {
    @Nullable
    private static ShaderInstance blackHoleShader;
    @Nullable
    private static ShaderInstance skyBeamShader;
    @Nullable
    private static ShaderInstance skyRingShader;

    @Nullable
    public static ShaderInstance getBlackHoleShader() {
        return blackHoleShader;
    }

    @Nullable
    public static ShaderInstance getSkyBeamShader() {
        return skyBeamShader;
    }

    @Nullable
    public static ShaderInstance getSkyRingShader() {
        return skyRingShader;
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation("fku", "black_hole"), DefaultVertexFormat.POSITION_COLOR), shader -> {
            blackHoleShader = shader;
        });
        event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation("fku", "sky_beam"), DefaultVertexFormat.POSITION_COLOR), shader -> {
            skyBeamShader = shader;
        });
        event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation("fku", "sky_ring"), DefaultVertexFormat.POSITION_COLOR), shader -> {
            skyRingShader = shader;
        });
        Fku.LOGGER.info("[KillFX] \u5df2\u6ce8\u518c {} \u4e2aGLSL\u7740\u8272\u5668", 3);
    }
}

