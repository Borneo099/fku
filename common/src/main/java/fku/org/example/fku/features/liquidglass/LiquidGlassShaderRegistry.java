package fku.org.example.fku.features.liquidglass; /* water */

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
 * 液体玻璃着色器注册中心
 * 注册自定义的 V3 单通道玻璃着色器
 *
 * ★ 参考：LiquidGlassShader (https://github.com/Jacquesqwq/LiquidGlassShader)
 *   移植其 V3 单通道片源着色器方案，适配 Forge 1.20.1
 *
 * 该着色器由赛博教员实现
 */
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class LiquidGlassShaderRegistry {

    @Nullable private static ShaderInstance liquidGlassShader;

    @Nullable public static ShaderInstance getLiquidGlassShader() { return liquidGlassShader; }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        // 液体玻璃 V3 着色器（单通道 mipmap blur + glass pass）
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(),
                        new ResourceLocation(Fku.MOD_ID, "liquid_glass"),
                        DefaultVertexFormat.POSITION_COLOR),
                shader -> liquidGlassShader = shader
        );
        Fku.LOGGER.info("[LiquidGlass] 已注册着色器");
    }
}