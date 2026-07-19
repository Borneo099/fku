package fku.org.example.fku.features.quickcommand; /* water */

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 快捷指令 — 监听组合键热键，执行对应指令
 *
 * 组合键支持：Shift (340/344), Ctrl (341/345), Alt (342/346) + 任意键
 * 每 tick 检查键盘状态，检测组合键按下后执行命令
 *
 * 注意：由于 Forge 的 InputEvent.Key 只在首次按下时触发，
 * 组合键检测改用每 tick 轮询 Screen 或直接在 Feature 中用 ClientTickEvent
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, value = Dist.CLIENT)
public class QuickCommandFeature {

    public static void init() { QuickCommandConfig.getInstance(); }

    /**
     * 运行时获取 Minecraft 实例（避免 static final 字段在类加载时初始化导致 NPE）
     */
    private static Minecraft getMc() {
        return Minecraft.getInstance();
    }

    /**
     * 在键盘事件中检测组合键
     */
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        var cfg = QuickCommandConfig.getInstance();
        Minecraft mc = getMc();
        if (mc == null || !cfg.enabled || mc.player == null || mc.level == null) return;
        if (mc.screen != null) return; // 有界面时不触发

        // 检查当前修饰键状态
        boolean shift = org.lwjgl.glfw.GLFW.glfwGetKey(mc.getWindow().getWindow(), 340) == 1
                || org.lwjgl.glfw.GLFW.glfwGetKey(mc.getWindow().getWindow(), 344) == 1;
        boolean ctrl = org.lwjgl.glfw.GLFW.glfwGetKey(mc.getWindow().getWindow(), 341) == 1
                || org.lwjgl.glfw.GLFW.glfwGetKey(mc.getWindow().getWindow(), 345) == 1;
        boolean alt = org.lwjgl.glfw.GLFW.glfwGetKey(mc.getWindow().getWindow(), 342) == 1
                || org.lwjgl.glfw.GLFW.glfwGetKey(mc.getWindow().getWindow(), 346) == 1;

        int action = event.getAction(); // 1=press, 0=release
        if (action != 1) return; // 只检测按下

        for (int i = 0; i < cfg.commands.size(); i++) {
            var cmd = cfg.commands.get(i);
            if (!cmd.enabled || cmd.hotkeyKey < 0) continue;
            if (event.getKey() != cmd.hotkeyKey) continue;

            // 检查修饰键匹配
            boolean shiftMatch = (cmd.hotkeyModifiers & 1) != 0;
            boolean ctrlMatch = (cmd.hotkeyModifiers & 2) != 0;
            boolean altMatch = (cmd.hotkeyModifiers & 4) != 0;
            if (shift != shiftMatch || ctrl != ctrlMatch || alt != altMatch) continue;

            // 执行指令（与 StructureLocatorFeature 使用相同方式）
            String rawCmd = cmd.command.trim();
            if (rawCmd.startsWith("/")) rawCmd = rawCmd.substring(1);
            mc.player.connection.sendCommand(rawCmd);
            Fku.LOGGER.info("[QuickCommand] 执行: /{}", rawCmd);
        }
    }
}
