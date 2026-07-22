package fku.org.example.fku.features.quickcommand;

import fku.org.example.fku.Fku;
import fku.org.example.fku.features.quickcommand.QuickCommandConfig;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid="fku", value={Dist.CLIENT})
public class QuickCommandFeature {
    public static void init() {
        QuickCommandConfig.getInstance();
    }

    private static Minecraft getMc() {
        return Minecraft.getInstance();
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        QuickCommandConfig cfg = QuickCommandConfig.getInstance();
        Minecraft mc = QuickCommandFeature.getMc();
        if (mc == null || !cfg.enabled || mc.player == null || mc.level == null) {
            return;
        }
        if (mc.screen != null) {
            return;
        }
        boolean shift = GLFW.glfwGetKey(mc.getWindow().getWindow(), 340) == 1 || GLFW.glfwGetKey(mc.getWindow().getWindow(), 344) == 1;
        boolean ctrl = GLFW.glfwGetKey(mc.getWindow().getWindow(), 341) == 1 || GLFW.glfwGetKey(mc.getWindow().getWindow(), 345) == 1;
        boolean alt = GLFW.glfwGetKey(mc.getWindow().getWindow(), 342) == 1 || GLFW.glfwGetKey(mc.getWindow().getWindow(), 346) == 1;
        int action = event.getAction();
        if (action != 1) {
            return;
        }
        for (int i = 0; i < cfg.commands.size(); ++i) {
            boolean altMatch;
            QuickCommandConfig.CommandEntry cmd = cfg.commands.get(i);
            if (!cmd.enabled || cmd.hotkeyKey < 0 || event.getKey() != cmd.hotkeyKey) continue;
            boolean shiftMatch = (cmd.hotkeyModifiers & 1) != 0;
            boolean ctrlMatch = (cmd.hotkeyModifiers & 2) != 0;
            boolean bl = altMatch = (cmd.hotkeyModifiers & 4) != 0;
            if (shift != shiftMatch || ctrl != ctrlMatch || alt != altMatch) continue;
            String rawCmd = cmd.command.trim();
            if (rawCmd.startsWith("/")) {
                rawCmd = rawCmd.substring(1);
            }
            mc.player.connection.sendCommand(rawCmd);
            Fku.LOGGER.info("[QuickCommand] \u6267\u884c: /{}", rawCmd);
        }
    }
}

