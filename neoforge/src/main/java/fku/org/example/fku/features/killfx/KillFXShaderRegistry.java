package fku.org.example.fku.features.killfx; /* water */

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import fku.org.example.fku.Fku;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;

import javax.annotation.Nullable;

/**
 * KillFX 着色器注册中心
 * 注册所有自定义 GLSL 着色器程序
 */
@EventBusSubscriber(modid = Fku.MOD_ID, value = Dist.CLIENT)
public class KillFXShaderRegistry {

    @Nullable private static Object blackHoleShader;
    @Nullable private static Object skyBeamShader;
    @Nullable private static Object skyRingShader;

    @Nullable public static Object getBlackHoleShader() { return blackHoleShader; }
    @Nullable public static Object getSkyBeamShader() { return skyBeamShader; }
    @Nullable public static Object getSkyRingShader() { return skyRingShader; }

    public static void registerShaders() {
        Fku.LOGGER.info("[KillFX] 着色器将在客户端初始化时加载");
    }
}