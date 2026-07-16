package fku.org.example.fku.features.selfdamage;

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
//? if neoforge {
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
//? }

/**
 * 自伤功能 — 移植 lexis DamageHack，支持热键绑定
 */
//? if neoforge {
@EventBusSubscriber(modid = Fku.MOD_ID, value = Dist.CLIENT)
//? } else {
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
//? }
public class SelfDamageFeature {

    private static final Minecraft mc = Minecraft.getInstance();
    private static Runnable hotkeyCallback = null;
    private static boolean waitingForKey = false;

    // ── 延迟恢复防摔/32k弓 ──
    private static boolean pendingRestoreNoFall = false;
    private static boolean pendingRestoreArrow = false;
    private static int restoreDelayTicks = 0; // 剩余等待 tick 数

    public static void init() { SelfDamageConfig.load(); }

    /** 启动热键绑定流程 */
    public static void startHotkeyBind(Runnable onComplete) {
        waitingForKey = true;
        hotkeyCallback = onComplete;
    }

    /** 执行自伤（临时关闭防摔和32k弓，延迟5tick后恢复） */
    public static void applyDamage() {
        if (mc.player == null || mc.getConnection() == null) {
            if (mc.player != null)
                mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c[自伤] 未连接服务器"), false);
            return;
        }

        if (mc.player.getAbilities().instabuild) {
            mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c[自伤] 创造模式无法受伤"), false);
            return;
        }

        // ── 临时关闭防摔和32k弓（否则摔落伤害被拦截） ──
        boolean nofallWasOn = fku.org.example.fku.features.nofall.NoFallFeature.isEnabled();
        boolean arrowWasOn = fku.org.example.fku.features.arrowdmg.ArrowDmgFeature.isEnabled();
        if (nofallWasOn) fku.org.example.fku.features.nofall.NoFallFeature.setEnabled(false);
        if (arrowWasOn) fku.org.example.fku.features.arrowdmg.ArrowDmgFeature.setEnabled(false);

        int amount = SelfDamageConfig.getInstance().damageAmount;
        Vec3 pos = mc.player.position();
        int loops = 100;

        for (int i = 0; i < loops; i++) {
            sendPos(pos.x, pos.y + amount + 2.1, pos.z, false);
            sendPos(pos.x, pos.y + 0.05, pos.z, false);
        }
        sendPos(pos.x, pos.y, pos.z, true);

        // ── 安排延迟恢复（给服务端足够时间处理摔落包） ──
        pendingRestoreNoFall = nofallWasOn;
        pendingRestoreArrow = arrowWasOn;
        restoreDelayTicks = 5; // 5 tick ≈ 250ms

        mc.player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("§6[自伤] §a已造成 " + amount + " 点伤害（防摔5tick后恢复）"), false);
    }

    /** 延迟恢复防摔/32k弓 */
    @SubscribeEvent
    //? if neoforge {
        public static void onClientTick(ClientTickEvent.Post event) {
    //? } else {
        public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
    //? }
        if (restoreDelayTicks > 0) {
            restoreDelayTicks--;
            if (restoreDelayTicks == 0) {
                if (pendingRestoreNoFall) {
                    fku.org.example.fku.features.nofall.NoFallFeature.setEnabled(true);
                    pendingRestoreNoFall = false;
                }
                if (pendingRestoreArrow) {
                    fku.org.example.fku.features.arrowdmg.ArrowDmgFeature.setEnabled(true);
                    pendingRestoreArrow = false;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        // 热键绑定模式：捕获按键
        if (waitingForKey) {
            int key = event.getKey();
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                waitingForKey = false;
                if (hotkeyCallback != null) hotkeyCallback.run();
                return;
            }
            if (key != GLFW.GLFW_KEY_UNKNOWN) {
                var cfg = SelfDamageConfig.getInstance();
                cfg.hotkeyKey = key;
                cfg.hotkeyName = GLFW.glfwGetKeyName(key, 0);
                if (cfg.hotkeyName == null) cfg.hotkeyName = "Key#" + key;
                cfg.save();
                waitingForKey = false;
                if (hotkeyCallback != null) hotkeyCallback.run();
                mc.player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§a[自伤] 热键已绑定: " + cfg.hotkeyName), false);
            }
            return;
        }

        // 正常模式由 HotkeySystem GLFW 轮询统一管理（避免双触发）
    }

    private static void sendPos(double x, double y, double z, boolean onGround) {
        mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(x, y, z, onGround));
    }
}
