package fku.org.example.fku.features.nojumpdelay;

import fku.org.example.fku.config.MovementConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="fku", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public class NoJumpDelayHandler {
    private static boolean wasOnGround = false;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!MovementConfig.getInstance().noJumpDelayEnabled) {
            wasOnGround = false;
            return;
        }
        Player player = event.player;
        if (player.m_5833_() || player.m_20069_() || player.m_20077_()) {
            return;
        }
        if (!(player instanceof LocalPlayer)) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != player) {
            return;
        }
        boolean isOnGround = player.m_20096_();
        boolean wantsToJump = mc.f_91066_.f_92089_.m_90857_();
        if (wantsToJump && !wasOnGround && isOnGround) {
            player.m_6135_();
        }
        wasOnGround = isOnGround;
    }
}

