package fku.org.example.fku.features.selfdamage;

import fku.org.example.fku.features.arrowdmg.ArrowDmgFeature;
import fku.org.example.fku.features.nofall.NoFallFeature;
import fku.org.example.fku.features.selfdamage.SelfDamageConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid="fku", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public class SelfDamageFeature {
    private static Runnable hotkeyCallback = null;
    private static boolean waitingForKey = false;
    private static boolean pendingRestoreNoFall = false;
    private static boolean pendingRestoreArrow = false;
    private static int restoreDelayTicks = 0;

    private static Minecraft getMc() {
        return Minecraft.getInstance();
    }

    public static void init() {
        SelfDamageConfig.load();
    }

    public static void startHotkeyBind(Runnable onComplete) {
        waitingForKey = true;
        hotkeyCallback = onComplete;
    }

    public static void applyDamage() {
        Minecraft mc = SelfDamageFeature.getMc();
        if (mc == null || mc.player == null || mc.getConnection() == null) {
            if (mc != null && mc.player != null) {
                mc.player.displayClientMessage(Component.literal((String)"\u00a7c[\u81ea\u4f24] \u672a\u8fde\u63a5\u670d\u52a1\u5668"), false);
            }
            return;
        }
        if (mc.player.getAbilities().instabuild) {
            mc.player.displayClientMessage(Component.literal((String)"\u00a7c[\u81ea\u4f24] \u521b\u9020\u6a21\u5f0f\u65e0\u6cd5\u53d7\u4f24"), false);
            return;
        }
        boolean nofallWasOn = NoFallFeature.isEnabled();
        boolean arrowWasOn = ArrowDmgFeature.isEnabled();
        if (nofallWasOn) {
            NoFallFeature.setEnabled(false);
        }
        if (arrowWasOn) {
            ArrowDmgFeature.setEnabled(false);
        }
        int amount = SelfDamageConfig.getInstance().damageAmount;
        Vec3 pos = mc.player.position();
        int loops = 100;
        for (int i = 0; i < loops; ++i) {
            SelfDamageFeature.sendPos(pos.x, pos.y + amount + 2.1, pos.z, false);
            SelfDamageFeature.sendPos(pos.x, pos.y + 0.05, pos.z, false);
        }
        SelfDamageFeature.sendPos(pos.x, pos.y, pos.z, true);
        pendingRestoreNoFall = nofallWasOn;
        pendingRestoreArrow = arrowWasOn;
        restoreDelayTicks = 5;
        mc.player.displayClientMessage(Component.literal((String)("\u00a76[\u81ea\u4f24] \u00a7a\u5df2\u9020\u6210 " + amount + " \u70b9\u4f24\u5bb3\uff08\u9632\u64545tick\u540e\u6062\u590d\uff09")), false);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (restoreDelayTicks > 0 && --restoreDelayTicks == 0) {
            if (pendingRestoreNoFall) {
                NoFallFeature.setEnabled(true);
                pendingRestoreNoFall = false;
            }
            if (pendingRestoreArrow) {
                ArrowDmgFeature.setEnabled(true);
                pendingRestoreArrow = false;
            }
        }
    }

    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        if (waitingForKey) {
            int key = event.getKey();
            if (key == 256) {
                waitingForKey = false;
                if (hotkeyCallback != null) {
                    hotkeyCallback.run();
                }
                return;
            }
            if (key != -1) {
                Minecraft mc;
                SelfDamageConfig cfg = SelfDamageConfig.getInstance();
                cfg.hotkeyKey = key;
                cfg.hotkeyName = GLFW.glfwGetKeyName(key, 0);
                if (cfg.hotkeyName == null) {
                    cfg.hotkeyName = "Key#" + key;
                }
                cfg.save();
                waitingForKey = false;
                if (hotkeyCallback != null) {
                    hotkeyCallback.run();
                }
                if ((mc = SelfDamageFeature.getMc()) != null && mc.player != null) {
                    mc.player.displayClientMessage(Component.literal((String)("\u00a7a[\u81ea\u4f24] \u70ed\u952e\u5df2\u7ed1\u5b9a: " + cfg.hotkeyName)), false);
                }
            }
            return;
        }
    }

    private static void sendPos(double x, double y, double z, boolean onGround) {
        Minecraft mc = SelfDamageFeature.getMc();
        if (mc == null || mc.getConnection() == null) {
            return;
        }
        mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.Pos(x, y, z, onGround));
    }
}

