package fku.org.example.fku.features.nofall;

import fku.org.example.fku.features.flight.FlightFeature;
import fku.org.example.fku.features.nofall.NoFallConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid="fku", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public class NoFallFeature {
    public static void init() {
        NoFallConfig.load();
    }

    public static void toggleEnabled() {
        NoFallFeature.setEnabled(!NoFallFeature.isEnabled());
    }

    public static void setEnabled(boolean val) {
        NoFallConfig cfg = NoFallConfig.getInstance();
        cfg.enabled = val;
        cfg.save();
    }

    public static boolean isEnabled() {
        return NoFallConfig.getInstance().enabled;
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (!NoFallFeature.isEnabled()) {
            return;
        }
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        NoFallConfig cfg = NoFallConfig.getInstance();
        if (cfg.onlyWhenFlying && !FlightFeature.isFlightActive()) {
            return;
        }
        if (event.getDistance() < cfg.minFallDistance) {
            return;
        }
        if (cfg.immune) {
            event.setCanceled(true);
        }
    }
}

