package fku.org.example.fku.features.killfx; /* water */

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import fku.org.example.fku.Fku;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * KillFX 着色器注册中心
 * 注册所有自定义 GLSL 着色器程序
 */
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class KillFXShaderRegistry {

    @Nullable private static ShaderInstance blackHoleShader;
    @Nullable private static ShaderInstance skyBeamShader;
    @Nullable private static ShaderInstance skyRingShader;

    @Nullable public static ShaderInstance getBlackHoleShader() { return blackHoleShader; }
    @Nullable public static ShaderInstance getSkyBeamShader() { return skyBeamShader; }
    @Nullable public static ShaderInstance getSkyRingShader() { return skyRingShader; }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        // 黑洞着色器（含吸积盘+光子环+引力透镜）
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(),
                        new ResourceLocation(Fku.MOD_ID, "black_hole"),
                        DefaultVertexFormat.POSITION_COLOR),
                shader -> blackHoleShader = shader
        );
        // 天光光束着色器（从天而降的光柱）
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(),
                        new ResourceLocation(Fku.MOD_ID, "sky_beam"),
                        DefaultVertexFormat.POSITION_COLOR),
                shader -> skyBeamShader = shader
        );
        // 天光环着色器（旋转光环）
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(),
                        new ResourceLocation(Fku.MOD_ID, "sky_ring"),
                        DefaultVertexFormat.POSITION_COLOR),
                shader -> skyRingShader = shader
        );
        Fku.LOGGER.info("[KillFX] 已注册 {} 个GLSL着色器", 3);
    }
}
