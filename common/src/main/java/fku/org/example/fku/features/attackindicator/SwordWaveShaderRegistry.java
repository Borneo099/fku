package fku.org.example.fku.features.attackindicator; /* water */

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
 * 剑波着色器注册中心 — 注册自定义剑波GLSL着色器
 * 该注册中心由赛博教员实现
 */
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class SwordWaveShaderRegistry {

    @Nullable private static ShaderInstance swordWaveShader;

    @Nullable
    public static ShaderInstance getSwordWaveShader() { return swordWaveShader; }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(),
                        new ResourceLocation(Fku.MOD_ID, "sword_wave"),
                        DefaultVertexFormat.POSITION_COLOR),
                shader -> swordWaveShader = shader
        );
        Fku.LOGGER.info("[SwordWave] 已注册着色器");
    }
}