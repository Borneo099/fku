package fku.org.example.fku.features.tpaura; /* water */

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix4f;

import fku.org.example.fku.features.healthtag.HealthTagManager;
import fku.org.example.fku.features.killfx.KillFXFeature;
import fku.org.example.fku.features.fakeplayer.FakePlayerFeature;
import org.lwjgl.glfw.GLFW;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TpAuraFeature — 如来神掌/瞬移攻击核心逻辑
 *
 * ★ 职责：
 *   通过瞬移机制实现超远距离近战攻击：
 *   1. 垫包预热 → 2. V-Clip上升(Paper模式) → 3. 瞬移至目标附近 → 4. 攻击(下落攻击加成)
 *   → 5. 回传原位
 *   纯客户端发包，支持静默切武、图腾绕过。
 *
 * ★ 设计思想（实践论）：
 *   完整复刻 Meteor TpAura 模块的核心逻辑，API 适配 Forge 1.20.1。
 *   使用 @SubscribeEvent 监听 ClientTickEvent / RenderLevelLastEvent。
 *   无需 Mixin，所有操作通过发包完成。
 *
 * ★ 参考来源：
 *   Meteor Client TpAura 模块
 *   (https://github.com/MeteorDevelopment/meteor-client)
 */
@OnlyIn(Dist.CLIENT)
public class TpAuraFeature {

    private static final Minecraft mc = Minecraft.getInstance();

    /** 单例实例（用于渲染等需要访问实例状态的场景） */
    private static TpAuraFeature instance;

    // ════════ 运行时状态 ════════
    /** 当前目标实体 */
    public Entity currentTarget;
    /** 瞬移路径节点列表（用于渲染） */
    public final List<Vec3> renderPathNodes = new ArrayList<>();
    /** 目标列表 */
    private final List<Entity> targets = new ArrayList<>();

    // 武器切换状态
    private int originalSlot = -1;
    private int silentSwapSlot = -1;
    private int silentSwapPrevSlot = -1;
    private int delayTimer = 0;

    /** 功能是否启用（由 TpAuraComponent 控制） */
    private static boolean enabled = false;

    /** Overlay 显示截止时间戳（毫秒，超时后自动隐藏） */
    private static long overlayShowUntil = 0;

    // ════════ 热键系统 ════════
    /** 热键按下状态（边沿检测） */
    private static boolean wasHotkeyDown = false;
    /** 是否正在等待按键绑定 */
    private static boolean waitingKeyBind = false;
    /** 等待绑定的回调，绑定完成后调用 */
    private static Runnable onKeyBoundCallback;

    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean v) {
        enabled = v;
        TpAuraConfig cfg = TpAuraConfig.getInstance();
        cfg.setEnabled(v);
        // 切换时显示 overlay 提示，3秒后自动消失
        overlayShowUntil = System.currentTimeMillis() + 3000;
        if (v) {
            if (mc.player != null) {
                mc.player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§6[TpAura] §a已启用 §7(范围=" + cfg.maxRange + ", 模式=" + cfg.mode + ")"),
                    false
                );
            }
        } else {
            TpAuraFeature feature = instance;
            if (feature != null) {
                feature.cleanup();
            }
            if (mc.player != null) {
                mc.player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§6[TpAura] §c已禁用"),
                    false
                );
            }
        }
    }

    public TpAuraFeature() {
        instance = this;
    }

    /** 获取单例 */
    public static TpAuraFeature getInstance() {
        if (instance == null) {
            instance = new TpAuraFeature();
        }
        return instance;
    }

    /** 初始化：加载配置，创建实例 */
    public static void init() {
        TpAuraConfig.load();
        getInstance();
    }

    // ══════════════════════════════════════════════
    //  热键系统方法
    // ══════════════════════════════════════════════

    /** 开始热键绑定（由 TpAuraComponent 中键触发） */
    public static void startHotkeyBind(Runnable onBound) {
        waitingKeyBind = true;
        onKeyBoundCallback = onBound;
        if (mc.player != null) {
            mc.player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("§6[TpAura] §e按下键盘上的按键绑定热键... (Esc取消)"),
                false
            );
        }
    }

    /** 取消热键绑定 */
    public static void cancelHotkeyBind() {
        waitingKeyBind = false;
        onKeyBoundCallback = null;
        if (mc.player != null) {
            mc.player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("§6[TpAura] §7热键绑定已取消"),
                false
            );
        }
    }

    /**
     * InputEvent.Key — 全局键盘输入处理
     * 两种用途：
     *   1. 热键绑定模式：捕获按下的键保存到配置
     *   2. 热键触发：检测已绑定的热键按下/释放边沿
     */
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (mc.player == null) return;

        // ── 热键绑定模式：捕获按下的键 ──
        if (waitingKeyBind) {
            // 忽略释放动作
            if (event.getAction() != GLFW.GLFW_PRESS) return;

            // Esc 取消
            if (event.getKey() == GLFW.GLFW_KEY_ESCAPE) {
                cancelHotkeyBind();
                return;
            }

            // 保存热键
            TpAuraConfig cfg = TpAuraConfig.getInstance();
            cfg.setHotkeyKey(event.getKey());
            // 获取键名
            String keyName = GLFW.glfwGetKeyName(event.getKey(), event.getScanCode());
            if (keyName == null || keyName.isEmpty()) {
                keyName = switch (event.getKey()) {
                    case GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT";
                    case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT";
                    case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCTRL";
                    case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCTRL";
                    case GLFW.GLFW_KEY_LEFT_ALT -> "LALT";
                    case GLFW.GLFW_KEY_RIGHT_ALT -> "RALT";
                    case GLFW.GLFW_KEY_SPACE -> "SPACE";
                    case GLFW.GLFW_KEY_TAB -> "TAB";
                    case GLFW.GLFW_KEY_ESCAPE -> "ESC";
                    case GLFW.GLFW_KEY_ENTER -> "ENTER";
                    case GLFW.GLFW_KEY_CAPS_LOCK -> "CAPS";
                    default -> "KEY_" + event.getKey();
                };
            } else {
                keyName = keyName.toUpperCase();
            }
            cfg.setHotkeyName(keyName);

            waitingKeyBind = false;
            if (mc.player != null) {
                mc.player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§6[TpAura] §a热键已绑定: §e" + keyName),
                    false
                );
            }
            if (onKeyBoundCallback != null) {
                onKeyBoundCallback.run();
                onKeyBoundCallback = null;
            }
            return;
        }

        // ── 正常模式：检查热键触发（仅无GUI时生效） ──
        TpAuraConfig cfg = TpAuraConfig.getInstance();
        if (cfg.hotkeyKey < 0) return;
        if (event.getKey() != cfg.hotkeyKey) return;
        if (mc.screen != null) return; // 有GUI打开时不触发

        // 边沿检测：只在按下时切换，忽略重复和释放
        if (event.getAction() == GLFW.GLFW_PRESS) {
            setEnabled(!enabled);
        }
    }

    // ══════════════════════════════════════════════
    //  ClientTickEvent — 主循环
    // ══════════════════════════════════════════════

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!enabled) return;
        if (mc.player == null || mc.level == null) return;

        TpAuraFeature self = getInstance();
        TpAuraConfig cfg = TpAuraConfig.getInstance();

        // 1. 武器切换
        if (cfg.autoSwitch) {
            self.checkAndSwapWeapon(cfg);
        }

        // 2. 蓄力检查（Smart / Universal 模式：等待蓄力，适用于任何武器）
        if ("Smart".equals(cfg.attackMode) || "Universal".equals(cfg.attackMode)) {
            if (mc.player.getAttackStrengthScale(0.5f) < (float) cfg.cooldownThreshold) {
                return;
            }
        }

        // 3. 额外延迟处理
        if (self.delayTimer > 0) {
            self.delayTimer--;
            self.swapBackWeapon();
            return;
        }

        // 4. 索敌
        self.targets.clear();
        Entity target = self.findTarget(cfg);
        if (target == null) {
            self.currentTarget = null;
            self.swapBackWeapon();
            // 不提示无目标（仅靠物品栏上方 HUD 文本显示状态，避免刷屏）
            return;
        }
        self.currentTarget = target;

        // 5. 执行瞬移轰炸
        self.executeTrouserAttack(target, cfg);
        self.swapBackWeapon();

        // 6. 重置延迟计时器
        self.delayTimer = cfg.attackDelay;
    }

    // ══════════════════════════════════════════════
    //  RenderLevelLastEvent — 3D 渲染
    // ══════════════════════════════════════════════

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        if (!enabled) return;
        if (mc.player == null || mc.level == null) return;

        TpAuraFeature self = getInstance();
        TpAuraConfig cfg = TpAuraConfig.getInstance();

        PoseStack poseStack = event.getPoseStack();
        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        VertexConsumer consumer = mc.renderBuffers().bufferSource().getBuffer(net.minecraft.client.renderer.RenderType.LINES);

        // ── 渲染目标边界框 ──
        if (self.currentTarget != null) {
            renderBox(poseStack, consumer, self.currentTarget.getBoundingBox(), cfg.getTargetColor());
        }

        // ── 渲染路径 ──
        if (cfg.renderPath && !self.renderPathNodes.isEmpty()) {
            int pathColor = cfg.getPathColor();
            float pr = ((pathColor >> 16) & 0xFF) / 255f;
            float pg = ((pathColor >> 8) & 0xFF) / 255f;
            float pb = (pathColor & 0xFF) / 255f;
            float pa = ((pathColor >> 24) & 0xFF) / 255f;

            for (int i = 0; i < self.renderPathNodes.size(); i++) {
                Vec3 n = self.renderPathNodes.get(i);
                // 渲染节点小框
                renderBox(poseStack, consumer,
                        new AABB(n.x - 0.2, n.y, n.z - 0.2, n.x + 0.2, n.y + 2, n.z + 0.2),
                        pathColor);

                // 渲染连线
                if (i < self.renderPathNodes.size() - 1) {
                    Vec3 next = self.renderPathNodes.get(i + 1);
                    Matrix4f mat = poseStack.last().pose();
                    consumer.vertex(mat, (float) n.x, (float) (n.y + 1), (float) n.z).color(pr, pg, pb, pa).normal(0f, 1f, 0f).endVertex();
                    consumer.vertex(mat, (float) next.x, (float) (next.y + 1), (float) next.z).color(pr, pg, pb, pa).normal(0f, 1f, 0f).endVertex();
                }
            }
        }

        poseStack.popPose();
    }

    /**
     * 在物品栏上方渲染 TpAura 开关状态指示器
     * 使用 HOTBAR 渲染后的时机，使文字叠加在物品栏上方居中位置
     */
    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;

        // 只在切换后短时间内显示，超时自动隐藏，避免常驻干扰
        if (System.currentTimeMillis() > overlayShowUntil) return;

        String text = "§6[TpAura " + (enabled ? "§aON" : "§cOFF") + "§6]";

        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();
        int textX = w / 2 - mc.font.width(text) / 2;
        int textY = h - 62; // 物品栏上方约两个槽位的高度

        event.getGuiGraphics().drawString(mc.font, text, textX, textY, 0xFFFFFF);
    }

    // ══════════════════════════════════════════════
    //  渲染辅助
    // ══════════════════════════════════════════════

    /** 渲染 AABB 边框 */
    private static void renderBox(PoseStack poseStack, VertexConsumer consumer, AABB box, int color) {
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;

        Matrix4f mat = poseStack.last().pose();
        double minX = box.minX, minY = box.minY, minZ = box.minZ;
        double maxX = box.maxX, maxY = box.maxY, maxZ = box.maxZ;

        // 底部矩形（4条边线）
        consumer.vertex(mat, (float) minX, (float) minY, (float) minZ).color(r, g, b, a).normal(0f, -1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) minY, (float) minZ).color(r, g, b, a).normal(0f, -1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) minY, (float) minZ).color(r, g, b, a).normal(0f, -1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) minY, (float) maxZ).color(r, g, b, a).normal(0f, -1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) minY, (float) maxZ).color(r, g, b, a).normal(0f, -1f, 0f).endVertex();
        consumer.vertex(mat, (float) minX, (float) minY, (float) maxZ).color(r, g, b, a).normal(0f, -1f, 0f).endVertex();
        consumer.vertex(mat, (float) minX, (float) minY, (float) maxZ).color(r, g, b, a).normal(0f, -1f, 0f).endVertex();
        consumer.vertex(mat, (float) minX, (float) minY, (float) minZ).color(r, g, b, a).normal(0f, -1f, 0f).endVertex();

        // 顶部矩形（4条边线）
        consumer.vertex(mat, (float) minX, (float) maxY, (float) minZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) maxY, (float) minZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) maxY, (float) minZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) maxY, (float) maxZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) maxY, (float) maxZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) minX, (float) maxY, (float) maxZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) minX, (float) maxY, (float) maxZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) minX, (float) maxY, (float) minZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();

        // 竖线（4条垂直边）
        consumer.vertex(mat, (float) minX, (float) minY, (float) minZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) minX, (float) maxY, (float) minZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) minY, (float) minZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) maxY, (float) minZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) minY, (float) maxZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) maxY, (float) maxZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) minX, (float) minY, (float) maxZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) minX, (float) maxY, (float) maxZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
    }

    // ══════════════════════════════════════════════
    //  核心攻击逻辑
    // ══════════════════════════════════════════════

    /**
     * 执行瞬移攻击（完整复刻 Meteor TpAura 逻辑）
     *
     * 流程：
     *   A. 垫包预热 → B. 上升+瞬移 → C. 攻击（单次/图腾绕过多次）→ D. 回传
     */
    private void executeTrouserAttack(Entity target, TpAuraConfig cfg) {
        // ★ 空指针防护：防止玩家死亡/切换维度时崩溃
        if (mc.player == null || mc.level == null) return;
        if (mc.player.connection == null) return;
        if (target == null || !target.isAlive()) return;

        try {
            executeTrouserAttackInternal(target, cfg);
        } catch (Exception e) {
            // ★ 全局异常捕获：防止任何未预期的异常导致游戏卡死
            //   记录异常并清理状态，确保下次 Tick 能正常恢复
            cleanup();
        }
    }

    /** 执行瞬移攻击内部逻辑（被 try-catch 包裹，防止异常卡死） */
    private void executeTrouserAttackInternal(Entity target, TpAuraConfig cfg) {
        Vec3 startPos = mc.player.position();
        Vec3 targetPos = target.position();

        // ★ NaN 防护：防止实体坐标异常导致后续计算卡死
        if (Double.isNaN(startPos.x) || Double.isNaN(startPos.y) || Double.isNaN(startPos.z)) return;
        if (Double.isNaN(targetPos.x) || Double.isNaN(targetPos.y) || Double.isNaN(targetPos.z)) return;
        double reach = cfg.maxRange;

        // ★ 世界边界检查：防止虚空/Y轴越界导致卡死
        int worldMinY = mc.level.getMinBuildHeight();
        int worldMaxY = mc.level.getMaxBuildHeight() - 1;
        if (startPos.y < worldMinY || startPos.y > worldMaxY) return;

        // 计算有效的攻击位置
        Vec3 finalPos = !invalid(targetPos) ? targetPos : findNearestPos(targetPos);
        if (finalPos == null) return;

        Vec3 highStart = startPos.add(0, reach, 0);
        Vec3 highTarget = finalPos.add(0, reach, 0);

        // 记录渲染路径
        renderPathNodes.clear();
        renderPathNodes.add(startPos);
        if ("Paper".equals(cfg.mode) && cfg.goUp) {
            renderPathNodes.add(highStart);
            renderPathNodes.add(highTarget);
        }
        renderPathNodes.add(finalPos);

        // A. 垫包预热 — 发送 onGround=false 包使服务端进入"下落"状态
        int spam = "Paper".equals(cfg.mode) ? cfg.paperPackets : 4;
        // ★ 安全上限：防止配置异常导致过量发包
        if (spam > 100) spam = 100;
        for (int i = 0; i < spam; i++) {
            if (mc.player == null || mc.player.connection == null) break;
            mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(
                mc.player.getX(), mc.player.getY(), mc.player.getZ(), false
            ));
        }

        boolean totemMode = cfg.totemBypass && "Paper".equals(cfg.mode);

        // B. 攻击阶段
        if (totemMode) {
            // 图腾绕过：多次递增高度攻击以突破无敌帧
            int attackCount = cfg.totemAttacks;
            int currentHeight = (int) reach;

            for (int i = 0; i < attackCount; i++) {
                int blocks = (i == 0) ? (int) reach : currentHeight;

                if (mc.level != null) {
                    int worldTop = mc.level.getMaxBuildHeight() - 1;
                    if (finalPos.y + blocks > worldTop) {
                        blocks = (int) (worldTop - finalPos.y);
                        if (blocks < 1) break;
                    }
                }

                Vec3 progressiveAbove = finalPos.add(0, blocks, 0);
                if (cfg.goUp) sendMove(progressiveAbove);
                sendMove(finalPos);

                performAttack(target, cfg);

                currentHeight += cfg.totemHeightIncrease;
            }
        } else {
            // 单次攻击
            if ("Paper".equals(cfg.mode) && cfg.goUp) {
                sendMove(highStart);
                sendMove(highTarget);
            }
            sendMove(finalPos);

            performAttack(target, cfg);
        }

        // C. 回传
        doReturn(startPos, finalPos, cfg);
    }

    /** 执行攻击发包 */
    private void performAttack(Entity target, TpAuraConfig cfg) {
        if (mc.player == null || mc.player.connection == null) return;
        if (target == null) return;

        // ★ 假人联动：如果目标是本地假人，直接在客户端模拟伤害，不发包
        //   服务端不认识客户端假人实体，发攻击包会被丢弃
        if (FakePlayerFeature.handleTpAuraAttack(target)) {
            if (cfg.swingHand) {
                mc.player.swing(InteractionHand.MAIN_HAND);
            }
            mc.player.resetAttackStrengthTicker();
            return;
        }

        // ★ 直接发送标准攻击包（原始发包方式，经测试最可靠）
        // 注意：LocalPlayer#attack() 在 Forge 1.20.1 中有额外客户端检查
        // （如 onClientAttack 事件），可能取消发包，因此直接发原始包。
        mc.player.connection.send(ServerboundInteractPacket.createAttackPacket(target, mc.player.isShiftKeyDown()));

        // ★ TpAura 联动 KillFX：手动记录攻击记录
        //   TpAura 直接发包攻击，绕过 AttackEntityEvent，
        //   导致 KillFX 的 onlyTargeted 模式无法识别攻击目标。
        //   此处手动调用 KillFX 接口写入攻击记录。
        KillFXFeature.markAttackedByTpAura(target.getId());

        // ★ TpAura 联动 HealthTag：手动锁定目标
        //   同样绕过 AttackEntityEvent，导致 HealthTag 无法锁定被攻击目标。
        HealthTagManager.onAttack(target);

        if (cfg.swingHand) {
            mc.player.swing(InteractionHand.MAIN_HAND);
        }

        // ★ 强制重置攻击蓄力计时器
        mc.player.resetAttackStrengthTicker();
    }

    // ══════════════════════════════════════════════
    //  位置包发送
    // ══════════════════════════════════════════════

    /** 发送位置包（模拟 PositionAndOnGround） */
    private void sendMove(Vec3 pos) {
        if (mc.player == null || mc.player.connection == null) return;
        mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(pos.x, pos.y, pos.z, false));
    }

    /** 计算回传位置并设置玩家位置 */
    private void doReturn(Vec3 startPos, Vec3 finalPos, TpAuraConfig cfg) {
        if (mc.player == null) return;
        if (cfg.returnPos) {
            if ("Paper".equals(cfg.mode) && cfg.goUp) {
                Vec3 highStart = startPos.add(0, cfg.maxRange, 0);
                Vec3 highTarget = finalPos.add(0, cfg.maxRange, 0);
                sendMove(highTarget);
                sendMove(highStart);
            }
            sendMove(startPos);

            if (cfg.offsetFix) {
                Vec3 offset = getOffset(startPos);
                sendMove(offset);
                mc.player.setPos(offset.x, offset.y, offset.z);
            } else {
                mc.player.setPos(startPos.x, startPos.y, startPos.z);
            }
        } else {
            if (cfg.offsetFix) {
                Vec3 offset = getOffset(finalPos);
                sendMove(offset);
                mc.player.setPos(offset.x, offset.y, offset.z);
            } else {
                mc.player.setPos(finalPos.x, finalPos.y, finalPos.z);
            }
        }
    }

    /** 生成微小偏移量，防止服务端拉回 */
    private Vec3 getOffset(Vec3 base) {
        double dx = 0.05, dy = 0.01;
        List<Vec3> offsets = Arrays.asList(
                base.add(dx, dy, 0),
                base.add(-dx, dy, 0),
                base.add(0, dy, dx),
                base.add(0, dy, -dx)
        );
        Collections.shuffle(offsets);
        for (Vec3 pos : offsets) {
            if (!invalid(pos)) return pos;
        }
        return base.add(0, dy, 0);
    }

    // ══════════════════════════════════════════════
    //  工具方法
    // ══════════════════════════════════════════════

    /** 检查位置是否无效（碰撞箱重叠或岩浆） */
    private boolean invalid(Vec3 pos) {
        if (mc.level == null || mc.player == null) return true;
        BlockPos bp = BlockPos.containing(pos.x, pos.y, pos.z);
        // ★ 世界边界检查：防止虚空/Y轴越界导致异常
        if (bp.getY() < mc.level.getMinBuildHeight() || bp.getY() >= mc.level.getMaxBuildHeight()) return true;
        if (mc.level.getChunk(bp.getX() >> 4, bp.getZ() >> 4) == null) return true;
        AABB box = mc.player.getBoundingBox().move(pos.subtract(mc.player.position()));
        for (BlockPos bPos : BlockPos.betweenClosed(
                BlockPos.containing(box.minX, box.minY, box.minZ),
                BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
            var state = mc.level.getBlockState(bPos);
            if (!state.getCollisionShape(mc.level, bPos).isEmpty()
                    || state.getBlock() == net.minecraft.world.level.block.Blocks.LAVA) {
                return true;
            }
        }
        return false;
    }

    /** 在目标位置附近寻找有效位置 */
    private Vec3 findNearestPos(Vec3 desired) {
        for (int dy = 0; dy <= 2; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    Vec3 test = desired.add(dx, dy, dz);
                    if (!invalid(test)) return test;
                }
            }
        }
        return null;
    }

    /** 查找最近的有效目标 */
    private Entity findTarget(TpAuraConfig cfg) {
        if (mc.level == null || mc.player == null) return null;

        Set<String> allowedTypes = cfg.getEntityTypeSet();

        Entity best = null;
        double bestDist = Double.MAX_VALUE;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!entityFilter(entity, cfg, allowedTypes)) continue;
            double dist = mc.player.distanceTo(entity);
            if (dist < bestDist && dist <= cfg.maxRange) {
                bestDist = dist;
                best = entity;
            }
        }
        return best;
    }

    /** 实体过滤器 */
    private boolean entityFilter(Entity entity, TpAuraConfig cfg, Set<String> allowedTypes) {
        if (!(entity instanceof LivingEntity) || !entity.isAlive() || entity == mc.player) return false;

        // ★ 全生物攻击模式：不按类型过滤
        if (!cfg.attackAllEntities) {
            String entityTypeKey = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).getPath().toLowerCase();
            if (!allowedTypes.contains(entityTypeKey)) return false;
        }

        if (mc.player.distanceTo(entity) > cfg.maxRange) return false;

        // 忽略条件
        if (cfg.ignoreNamed && entity.hasCustomName()) return false;
        if (cfg.ignoreTamed && entity instanceof TamableAnimal ta && ta.isTame()) return false;

        // ★ 白名单检查（所有实体均有效，不限于玩家）
        //   之前版本将白名单检查放在 Player 分支内，导致全生物模式下非玩家实体绕过白名单。
        if (cfg.whitelistEnabled) {
            String entityType = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES
                    .getKey(entity.getType()).getPath().toLowerCase();
            List<String> wl = Arrays.stream(cfg.whitelist.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            if (wl.contains(entityType)) return false; // 实体类型在白名单中 → 跳过
        }

        // 玩家特殊处理
        if (entity instanceof Player p) {
            if (p.isCreative() || p.isSpectator()) return false;
        }

        return true;
    }

    // ══════════════════════════════════════════════
    //  武器切换
    // ══════════════════════════════════════════════

    /** 查找武器在背包中的槽位 */
    private int findWeaponInventorySlot() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 45; i++) {
            String name = mc.player.getInventory().getItem(i).getItem().toString().toLowerCase();
            if (name.contains("sword") || name.contains("mace") || name.contains("axe")) {
                return i;
            }
        }
        return -1;
    }

    /** 检查并切换到武器 */
    private boolean checkAndSwapWeapon(TpAuraConfig cfg) {
        if (mc.player == null || mc.player.connection == null) return false;

        ItemStack mainHand = mc.player.getMainHandItem();
        String itemName = mainHand.getItem().toString().toLowerCase();
        boolean isWeapon = itemName.contains("sword") || itemName.contains("mace") || itemName.contains("axe");
        if (isWeapon && !(cfg.requireMace && !itemName.contains("mace"))) return true;

        if (cfg.silentSwap) {
            int slot = findWeaponInventorySlot();
            if (slot != -1) {
                silentSwapSlot = slot;
                silentSwapPrevSlot = mc.player.getInventory().selected;
                if (slot >= 36) {
                    // 热栏槽位，直接切换
                    mc.player.getInventory().selected = slot - 36;
                } else {
                    // 背包槽位，使用 SWAP 操作
                    mc.player.connection.send(new ServerboundContainerClickPacket(
                            0, // 玩家背包容器ID
                            mc.player.containerMenu.getStateId(),
                            slot, // 背包中武器槽位
                            0, // 热栏槽位 0
                            ClickType.SWAP,
                            mc.player.containerMenu.getCarried(),
                            new Int2ObjectOpenHashMap<>()
                    ));
                    mc.player.getInventory().selected = 0;
                }
                return true;
            }
        } else {
            // 非静默切换：直接搜索热栏
            for (int i = 0; i < 9; i++) {
                String name = mc.player.getInventory().getItem(i).getItem().toString().toLowerCase();
                if (name.contains("sword") || name.contains("mace") || name.contains("axe")) {
                    if (originalSlot == -1) originalSlot = mc.player.getInventory().selected;
                    mc.player.getInventory().selected = i;
                    return true;
                }
            }
        }
        return false;
    }

    /** 恢复武器槽位 */
    private void swapBackWeapon() {
        if (silentSwapSlot == -1 && originalSlot == -1) return;

        if (silentSwapSlot != -1 && mc.player != null && mc.player.connection != null) {
            if (silentSwapSlot >= 36) {
                mc.player.getInventory().selected = silentSwapPrevSlot;
            } else {
                mc.player.connection.send(new ServerboundContainerClickPacket(
                        0,
                        mc.player.containerMenu.getStateId(),
                        silentSwapSlot,
                        0,
                        ClickType.SWAP,
                        mc.player.containerMenu.getCarried(),
                        new Int2ObjectOpenHashMap<>()
                ));
                mc.player.getInventory().selected = silentSwapPrevSlot;
                mc.player.connection.send(new ServerboundContainerClosePacket(mc.player.containerMenu.containerId));
            }
            silentSwapSlot = -1;
            silentSwapPrevSlot = -1;
        }

        if (originalSlot != -1 && mc.player != null) {
            mc.player.getInventory().selected = originalSlot;
            originalSlot = -1;
        }
    }

    /** 清理所有状态 */
    private void cleanup() {
        swapBackWeapon();
        currentTarget = null;
        targets.clear();
        renderPathNodes.clear();
        delayTimer = 0;
    }
}
