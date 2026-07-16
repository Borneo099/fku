package fku.org.example.fku.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * KeepAliveHandler — 连接保持预留
 * 当前不发送任何数据包，保留类结构便于后续按需启用。
 */
@Mod.EventBusSubscriber(modid = "fku", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class KeepAliveHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        // 不再发送任何数据包，避免干扰准星
    }
}