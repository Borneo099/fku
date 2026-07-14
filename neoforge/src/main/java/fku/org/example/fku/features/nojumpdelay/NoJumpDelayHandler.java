package fku.org.example.fku.features.nojumpdelay; /* water */

import fku.org.example.fku.config.MovementConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "fku", value = Dist.CLIENT)
public class NoJumpDelayHandler {

    private static boolean wasOnGround = false;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        if (!(event instanceof PlayerTickEvent.Post)) return;
        if (!MovementConfig.getInstance().noJumpDelayEnabled) {
            wasOnGround = false;
            return;
        }

        Player player = event.getEntity();
        if (player.isSpectator() || player.isInWater() || player.isInLava()) return;
        if (!(player instanceof net.minecraft.client.player.LocalPlayer)) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != player) return;

        boolean isOnGround = player.onGround();
        boolean wantsToJump = mc.options.keyJump.isDown();

        if (wantsToJump && !wasOnGround && isOnGround) {
            player.jumpFromGround();
        }

        wasOnGround = isOnGround;
    }
}