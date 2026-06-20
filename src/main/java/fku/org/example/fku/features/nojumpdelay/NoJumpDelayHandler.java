package fku.org.example.fku.features.nojumpdelay; /* water */

import fku.org.example.fku.config.MovementConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "fku", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class NoJumpDelayHandler {

    private static boolean wasOnGround = false;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!MovementConfig.getInstance().noJumpDelayEnabled) {
            wasOnGround = false;
            return;
        }

        Player player = event.player;
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