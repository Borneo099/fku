package fku.org.example.fku.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="fku", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public class KeepAliveHandler {
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
    }
}

