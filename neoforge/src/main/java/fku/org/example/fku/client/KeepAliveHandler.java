package fku.org.example.fku.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * KeepAliveHandler — 连接保持预留
 * 当前不发送任何数据包，保留类结构便于后续按需启用。
 */
@EventBusSubscriber(modid = "fku", value = Dist.CLIENT)
public class KeepAliveHandler {

    @SubscribeEvent
    public static void onClientTick(Post event) {
        // 不再发送任何数据包，避免干扰准星
    }
}