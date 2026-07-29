package fku.org.example.fku.features.standattack; /* water */

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fku.org.example.fku.Fku;
import fku.org.example.fku.features.freecam.FreecamFeature;
import fku.org.example.fku.features.freecam.FreecamManager;
import fku.org.example.fku.features.healthtag.HealthTagManager;
import fku.org.example.fku.features.killicon.KillIconFeature;
import fku.org.example.fku.utils.pathfinding.AStarPathFinder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 替身攻击（StandAttack）核心逻辑
 *
 * ★ 移植自 TpAuraFeature 的完整攻击逻辑，关键改进：
 *   1. 自由相机模式（灵魂出窍）：相机完全脱离玩家实体，可自由移动，
 *      玩家回传时传至相机当前位置
 *   2. 攻击距离修复：目标检测使用原始玩家位置，不受自由相机位置影响
 *   3. 完整流程：垫包预热 → V-Clip上升(Paper模式) → 瞬移至目标附近
 *      → 攻击(支持图腾绕过多次攻击) → 回传至相机位置(自由相机模式)
 *
 * ★ 参考来源：
 *   TpAuraFeature (Meteor Client)
 *   Wurst Freecam (https://www.wurstclient.net/)
 * 该功能由赛博教员实现
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class StandAttackFeature {

    private static final Minecraft mc = Minecraft.getInstance();
    private static StandAttackFeature instance;

    // ════════ 运行时状态 ════════
    public Entity currentTarget;
    public final List<Vec3> renderPathNodes = new ArrayList<>();
    private final List<Entity> targets = new ArrayList<>();

    private List<Vec3> fullPath;
    private int pathIndex = 0;
    private Vec3 originalPos;          // 攻击前玩家原始位置（用于目标检测）
    private Vec3 originalLookVec;      // 攻击前玩家视角方向
    private boolean active = false;
    private long lastPacketTime = 0;
    private int delayTicks = 0;
    private boolean attacking = false;
    private boolean returning = false;
    private int totemAttackIndex = 0;

    // ════════ 自由相机状态 — 由 FreecamManager 统一管理 ════════

    // ════════ 选中模式（32k弓式）状态 ════════
    /** 手动选中的目标（选中模式使用） */
    public Entity selectedEntity;
    /** 上一帧左键状态（用于检测左键按下，防止连点） */
    private boolean wasLeftClicking = false;

    // ════════ 死亡回传状态 ════════
    /** 上次随机偏移传送时间 */
    private long lastTeleportTime = 0;

    // ════════ 待关闭标记 ════════
    /** ★ 用户关闭功能时，先完成回传再彻底关闭，防止瞬移半路上 */
    private boolean pendingDisable = false;

    /** ★ 攻击计数器 — 供AttackIndicator/KillIcon等模块检测StandAttack攻击事件 */
    public static int attackCounter = 0;

    // 武器切换状态
    private int originalSlot = -1;
    private int silentSwapSlot = -1;
    private int silentSwapPrevSlot = -1;

    private static long overlayShowUntil = 0;
    private static boolean wasHotkeyDown = false;
    private static boolean waitingKeyBind = false;
    private static Runnable onKeyBoundCallback;

    /** 显示聊天消息（受 showMessages 配置控制） */
    private static void chatMsg(String msg) {
        if (StandAttackConfig.getInstance().showMessages && mc.player != null) {
            mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal(msg), false);
        }
    }

    /** 设置 overlay 显示时段 */
    private static void flashOverlay(long ms) {
        overlayShowUntil = System.currentTimeMillis() + ms;
    }

    public static boolean isEnabled() { return StandAttackConfig.getInstance().enabled; }

    /** 替身攻击是否处于激活状态（正在传送/攻击/回传） */
    public boolean isActiveState() { return this.active; }

    public static void setEnabled(boolean v) {
        StandAttackConfig cfg = StandAttackConfig.getInstance();
        cfg.setEnabled(v);
        flashOverlay(3000);
        if (v) {
            chatMsg("§6[替身攻击] §a已启用 §7(范围=" + cfg.maxRange + ")");
            // ★ 相机锁开启时自动开启灵魂出窍
            if (cfg.cameraLock) {
                FreecamFeature.activateForStandAttack();
            }
        } else {
            StandAttackFeature self = getInstance();
            // ★ 如果激活中（传送/攻击中），先启动回传，待回传完成后再彻底关闭
            if (self.active && self.fullPath != null && !self.fullPath.isEmpty()) {
                self.returning = true;
                self.pathIndex = self.fullPath.size() - 1;
                self.attacking = false;
                self.delayTicks = 0;
                self.pendingDisable = true;
                chatMsg("§6[替身攻击] §e正在回传至起点...");
            } else {
            // ★ 未激活状态，但相机锁开启时：使用寻路+分段传送回传至相机位置
            if (cfg.cameraLock && FreecamManager.isActive()) {
                Vec3 camPos = FreecamManager.getPosition();
                Vec3 playerPos = mc.player.position();
                // 构建从相机到玩家的路径（反向），回传时从玩家→相机
                // 回传路径索引0=相机位置，索引N=玩家位置，回传时从N递减到0
                List<Vec3> steps = self.segmentLine(camPos, playerPos, cfg.maxStepLength);
                List<Vec3> returnPath = new ArrayList<>();
                returnPath.add(camPos);
                if (steps != null) returnPath.addAll(steps);
                self.fullPath = returnPath;
                self.renderPathNodes.clear();
                self.renderPathNodes.addAll(returnPath);
                self.pathIndex = returnPath.size() - 1;
                self.returning = true;
                self.attacking = false;
                self.active = true;
                self.pendingDisable = true;
                self.delayTicks = 0;
                self.lastPacketTime = 0;
                self.originalPos = playerPos;
                chatMsg("§6[替身攻击] §e正在回传至相机位置...");
            } else if (cfg.cameraLock) {
                FreecamFeature.deactivateForStandAttack();
                self.cleanup();
                chatMsg("§6[替身攻击] §c已禁用");
            } else {
                self.cleanup();
                chatMsg("§6[替身攻击] §c已禁用");
            }
        }
        }
    }

    public static StandAttackFeature getInstance() {
        if (instance == null) instance = new StandAttackFeature();
        return instance;
    }

    public static void init() {
        StandAttackConfig.load();
        getInstance();
    }

    public static void startHotkeyBind(Runnable onBound) {
        waitingKeyBind = true;
        onKeyBoundCallback = onBound;
        chatMsg("§6[替身攻击] §e按下键盘上的按键绑定热键... (Esc取消)");
    }

    public static void cancelHotkeyBind() {
        waitingKeyBind = false;
        onKeyBoundCallback = null;
        chatMsg("§6[替身攻击] §7热键绑定已取消");
    }

    // ════════ 自动飞行 ════════

    private static void updateAutoFlight() {
        LocalPlayer p = mc.player;
        if (p == null) return;
        StandAttackConfig cfg = StandAttackConfig.getInstance();
        if (cfg.autoFlight && cfg.enabled) {
            p.getAbilities().flying = true;
            p.connection.send(new ServerboundPlayerAbilitiesPacket(p.getAbilities()));
            float fwd = p.input.forwardImpulse;
            float str = -p.input.leftImpulse;
            float camYaw = mc.gameRenderer.getMainCamera().getYRot();
            var h = Vec3.directionFromRotation(0, camYaw).multiply(fwd, 0, fwd)
                    .add(Vec3.directionFromRotation(0, camYaw + 90).multiply(str, 0, str));
            double hSpeed = cfg.autoFlightHorizontalSpeed;
            if (h.lengthSqr() > 1e-4) h = h.normalize().scale(hSpeed);
            else h = Vec3.ZERO;
            double vy = p.input.jumping ? cfg.autoFlightSpeed
                      : p.input.shiftKeyDown ? -cfg.autoFlightSpeed : 0;
            p.setDeltaMovement(h.x, vy, h.z);
            p.hurtMarked = true;
        }
    }

    // ════════ 自由相机（灵魂出窍）控制 — 使用 FreecamManager ════════

    /**
     * ★ 处理自由相机（每tick调用）
     *
     * 只负责激活/停用 FreecamManager，不处理 WASD 移动。
     * WASD 移动由 FreecamFeature.onClientTick 统一处理，
     * 避免双重调用导致相机位置冲突（移动回弹）。
     *
     * ★ 关键：只要 cfg.cameraLock 开启，就保持 FreecamManager 激活，
     *   不因 this.active=false 而停用。这样攻击完成后玩家回传至相机位置后，
     *   相机不弹回，用户可继续用灵魂出窍自由移动。
     * 该方法由赛博教员实现
     */
    private void handleFreecam() {
        StandAttackConfig cfg = StandAttackConfig.getInstance();
        // ★ 仅当 cameraLock 关闭时才停用 FreecamManager
        if (!cfg.cameraLock) {
            if (FreecamManager.isActive()) {
                FreecamManager.deactivate();
            }
            return;
        }

        if (!FreecamManager.isActive()) {
            // 首次进入：激活 FreecamManager
            var cam = mc.gameRenderer.getMainCamera();
            var pos = cam.getPosition();
            FreecamManager.activate(pos.x, pos.y, pos.z, cam.getYRot(), cam.getXRot());
            chatMsg("§6[替身攻击] §e自由相机已激活，WASD移动相机，回传时玩家传至相机位置");
        }
        // ★ WASD 移动由 FreecamFeature.onClientTick 统一处理，此处不做任何移动操作
    }

    // ════════ ClientTick — 主循环 ════════

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (mc.player == null || mc.level == null) return;

        StandAttackFeature self = getInstance();
        StandAttackConfig cfg = StandAttackConfig.getInstance();

        updateAutoFlight();

        if (!cfg.enabled) {
            // ★ 待关闭状态（pendingDisable）：让回传流程继续执行，不清理
            if (!self.pendingDisable) {
                if (self.active) self.cleanup();
                return;
            }
        }

        // ★ 处理自由相机（每帧将玩家实体设为自由相机位置）
        self.handleFreecam();

        // ── 处理传送路径 ──
        if (self.active && self.fullPath != null && !self.fullPath.isEmpty()) {
            if (self.delayTicks > 0) { self.delayTicks--; return; }

            // 攻击阶段
            if (self.attacking) {
                // ★ 先执行攻击（无论是否死亡回传，都调用 performAttack）
                self.performAttack(cfg);
                attackCounter++;

                // ★ 死亡回传模式：按 attackDelay 攻击，按 teleportInterval 传送
                if (cfg.deathReturn) {
                    long now = System.currentTimeMillis();
                    // 检查目标是否死亡
                    if (self.currentTarget == null || !self.currentTarget.isAlive()) {
                        // 目标已死亡，回传
                        chatMsg("§6[替身攻击] §a目标已击杀，开始回传");
                        self.attacking = false;
                        self.delayTicks = cfg.attackDelay;
                        if (cfg.returnPos) {
                            self.returning = true;
                            self.pathIndex = self.fullPath.size() - 1;
                        } else {
                            self.cleanup(true);
                        }
                        return;
                    }
                    // 检查是否到达传送间隔（仅传送，不控制攻击）
                    if (now - self.lastTeleportTime >= cfg.teleportInterval) {
                        self.lastTeleportTime = now;
                        // 随机偏移传送
                        java.util.Random rand = new java.util.Random();
                        double ox = (rand.nextDouble() - 0.5) * 2.0 * cfg.tpOffset;
                        double oz = (rand.nextDouble() - 0.5) * 2.0 * cfg.tpOffset;
                        Vec3 targetPos = self.currentTarget.position();
                        Vec3 offsetDest = new Vec3(targetPos.x + ox, targetPos.y, targetPos.z + oz);
                        if (!self.invalid(offsetDest)) {
                            self.sendMovePacket(offsetDest, cfg.antiFall);
                            mc.player.setPos(offsetDest.x, offsetDest.y, offsetDest.z);
                            chatMsg("§6[替身攻击] §e偏移传送: §7" + String.format("%.1f,%.1f", ox, oz));
                        }
                    }
                    // 攻击间隔由 attackDelay 控制
                    self.delayTicks = cfg.attackDelay;
                    return;
                }

                // ★ 普通模式（非死亡回传）：攻击后处理图腾绕过/回传
                self.totemAttackIndex++;
                if (cfg.totemBypass && self.totemAttackIndex < cfg.totemAttacks) {
                    self.delayTicks = cfg.attackDelay;
                    self.attacking = false;
                    Vec3 above = new Vec3(self.originalPos.x,
                        self.originalPos.y + cfg.maxStepLength + self.totemAttackIndex * cfg.totemHeightIncrease,
                        self.originalPos.z);
                    self.fullPath.add(above);
                    self.pathIndex = self.fullPath.size() - 1;
                    self.sendMovePacket(above, cfg.antiFall);
                    self.attacking = true;
                    return;
                }
                self.attacking = false;
                self.delayTicks = cfg.attackDelay;
                if (cfg.returnPos) {
                    self.returning = true;
                    self.pathIndex = self.fullPath.size() - 1;
                } else {
                    self.cleanup();
                }
                return;
            }

            // 发包控制
            long now = System.currentTimeMillis();
            if (now - self.lastPacketTime < cfg.packetInterval) return;

            if (!self.returning) {
                // 前向传送：发送包并更新客户端玩家位置
                // ★ 即使 cameraLock 开启也更新客户端玩家位置，确保玩家实体与服务器同步，
                //    CameraMixin 会独立覆盖相机位置，实现灵魂出窍效果
                if (self.pathIndex < self.fullPath.size()) {
                    Vec3 dest = self.fullPath.get(self.pathIndex);
                    self.sendMovePacket(dest, cfg.antiFall);
                    mc.player.setPos(dest.x, dest.y, dest.z);
                    self.lastPacketTime = now;
                    self.pathIndex++;

                    if (self.pathIndex >= self.fullPath.size()) {
                        // ★ 到达目标点立即攻击，无需额外等待
                        //    (原版等待2tick仅为确保服务器接收位置包，但丢包时也无意义)
                        self.delayTicks = 0;
                        self.attacking = true;
                        self.totemAttackIndex = 0;
                    }
                }
            } else {
                // 回传阶段：沿路径节点反向传送
                // ★ 全程保持 FreecamManager 激活，相机始终在自由位置不跟随玩家
                if (self.pathIndex >= 0) {
                    Vec3 dest = self.fullPath.get(self.pathIndex);
                    // ★ 相机锁模式：最后一步（pathIndex==0）回传至自由相机当前位置
                    //   不减去眼高，直接传至相机位置，避免坐标偏移导致逐次上升
                    //   相机位置由 FreecamManager 独立维护，不受玩家移动影响
                    if (cfg.cameraLock && self.pathIndex == 0 && FreecamManager.isActive()) {
                        dest = FreecamManager.getPosition();
                    }
                    self.sendMovePacket(dest, cfg.antiFall);
                    mc.player.setPos(dest.x, dest.y, dest.z);
                    self.lastPacketTime = now;
                    self.pathIndex--;

                    if (self.pathIndex < 0) {
                        // ★ 待关闭状态：回传完成后彻底关闭（不保持 Freecam）
                        if (self.pendingDisable) {
                            self.pendingDisable = false;
                            self.cleanup();
                            if (cfg.cameraLock) {
                                FreecamFeature.deactivateForStandAttack();
                            }
                            chatMsg("§6[替身攻击] §c已禁用");
                        } else {
                            // ★ 正常攻击完成，保持 Freecam 激活（灵魂出窍不关闭）
                            self.cleanup(true);
                            chatMsg("§6[替身攻击] §a攻击完成，已回传");
                        }
                    }
                }
            }
            return;
        }

        // ── 空闲状态：寻找目标 ──
        if (!self.active) {
            // ★ 选中模式（32k弓风格）：准星靠近实体自动显示红框，长按左键持续攻击
            //   每tick检测准星附近最近的实体，选中后渲染红框，不限制玩家移动
            //   ★ 长按左键：只要选中实体且未在攻击中，自动传送攻击，无需重新点击
            if (cfg.selectMode) {
                // 每tick检测准星附近最近的实体（用于显示红框）
                Entity nearTarget = self.raycastEntity(128.0);
                if (nearTarget != null && nearTarget != mc.player && nearTarget.isAlive() && nearTarget instanceof LivingEntity) {
                    // ★ 选中实体变化时更新HealthTag（32k弓风格，选中即显示血量标签）
                    if (nearTarget != self.selectedEntity) {
                        HealthTagManager.onAttack(nearTarget);
                    }
                    self.selectedEntity = nearTarget;
                } else {
                    self.selectedEntity = null;
                }

                // ★ 长按右键：只要选中实体且不在攻击中，立即传送攻击
                //   松开右键后停止动作，无需每次重新点击
                //   使用右键而非左键，避免武器冷却干扰
                boolean rightClicking = mc.options.keyUse.isDown();
                if (rightClicking && !self.active && self.selectedEntity != null) {
                    self.currentTarget = self.selectedEntity;
                    self.startAttack(self.selectedEntity, cfg);
                }
            } else {
                // 普通模式：自动搜索目标
                Entity target = self.findTarget(cfg);
                if (target != null) {
                    self.currentTarget = target;
                    self.startAttack(target, cfg);
                }
            }
        }
    }

    // ════════ 渲染 ════════

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        if (mc.player == null || mc.level == null) return;

        StandAttackFeature self = getInstance();
        StandAttackConfig cfg = StandAttackConfig.getInstance();

        if (!cfg.enabled && !self.active) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        VertexConsumer consumer = mc.renderBuffers().bufferSource().getBuffer(net.minecraft.client.renderer.RenderType.LINES);

        // ★ 选中模式：渲染选中框（红色边框，32k弓风格，准星靠近即显示）
        if (cfg.selectMode && self.selectedEntity != null && self.selectedEntity.isAlive()) {
            renderBox(poseStack, consumer, self.selectedEntity.getBoundingBox(), 0xFFFF4444);
        }

        // 渲染路径（受 renderPath 控制）
        if (cfg.renderPath && !self.renderPathNodes.isEmpty()) {

            int pathColor = cfg.getPathColor();
            float pr = ((pathColor >> 16) & 0xFF) / 255f;
            float pg = ((pathColor >> 8) & 0xFF) / 255f;
            float pb = (pathColor & 0xFF) / 255f;
            float pa = ((pathColor >> 24) & 0xFF) / 255f;

            for (int i = 0; i < self.renderPathNodes.size(); i++) {
                Vec3 n = self.renderPathNodes.get(i);
                renderBox(poseStack, consumer, new AABB(n.x - 0.2, n.y, n.z - 0.2, n.x + 0.2, n.y + 2, n.z + 0.2), pathColor);
                if (i < self.renderPathNodes.size() - 1) {
                    Vec3 next = self.renderPathNodes.get(i + 1);
                    Matrix4f mat = poseStack.last().pose();
                    consumer.vertex(mat, (float) n.x, (float) (n.y + 1), (float) n.z).color(pr, pg, pb, pa).normal(0f, 1f, 0f).endVertex();
                    consumer.vertex(mat, (float) next.x, (float) (next.y + 1), (float) next.z).color(pr, pg, pb, pa).normal(0f, 1f, 0f).endVertex();
                }
            }

            if (self.currentTarget != null) {
                renderBox(poseStack, consumer, self.currentTarget.getBoundingBox(), cfg.getTargetColor());
            }
        }

        poseStack.popPose();
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;
        if (System.currentTimeMillis() > overlayShowUntil) return;
        StandAttackFeature self = getInstance();
        StandAttackConfig cfg = StandAttackConfig.getInstance();
        String status = "§6[替身攻击 " + (cfg.enabled ? "§aON" : "§cOFF") + "§6]";
        if (self.active) {
            status += " §e" + (self.returning ? "回传中" : self.attacking ? "攻击中" : "传送中");
            if (FreecamManager.isActive()) status += " §b[灵魂出窍]";
        }
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();
        int textX = w / 2 - mc.font.width(status) / 2;
        int textY = h - 62;
        event.getGuiGraphics().drawString(mc.font, status, textX, textY, 0xFFFFFF);
    }

    // ════════ GUI 叠加层渲染 — 选中模式提示文字 ════════

    /**
     * 在GUI叠加层渲染选中模式的操作提示文字（屏幕下方居中显示）
     * 使用 RenderGuiOverlayEvent.Post 确保在渲染完所有GUI元素后绘制
     */
    @SubscribeEvent
    public static void onRenderGuiOverlay(net.minecraftforge.client.event.RenderGuiOverlayEvent.Post event) {
        StandAttackConfig cfg = StandAttackConfig.getInstance();
        if (!cfg.enabled || !cfg.selectMode) return;
        if (mc.player == null || mc.font == null) return;
        if (event.getOverlay().id().getPath().equals("all")) {
            String hint = "§7[选中模式] 长按左键持续攻击，准星靠近实体自动显示红框";
            int w = mc.getWindow().getGuiScaledWidth();
            int h = mc.getWindow().getGuiScaledHeight();
            event.getGuiGraphics().drawString(mc.font, hint, (w - mc.font.width(hint)) / 2, h - 30, 0xFFFFFF, true);
        }
    }

    // ════════ 渲染辅助 ════════

    private static void renderBox(PoseStack poseStack, VertexConsumer consumer, AABB box, int color) {
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;
        Matrix4f mat = poseStack.last().pose();
        double minX = box.minX, minY = box.minY, minZ = box.minZ;
        double maxX = box.maxX, maxY = box.maxY, maxZ = box.maxZ;
        // 底部矩形
        consumer.vertex(mat, (float) minX, (float) minY, (float) minZ).color(r, g, b, a).normal(0f, -1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) minY, (float) minZ).color(r, g, b, a).normal(0f, -1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) minY, (float) minZ).color(r, g, b, a).normal(0f, -1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) minY, (float) maxZ).color(r, g, b, a).normal(0f, -1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) minY, (float) maxZ).color(r, g, b, a).normal(0f, -1f, 0f).endVertex();
        consumer.vertex(mat, (float) minX, (float) minY, (float) maxZ).color(r, g, b, a).normal(0f, -1f, 0f).endVertex();
        consumer.vertex(mat, (float) minX, (float) minY, (float) maxZ).color(r, g, b, a).normal(0f, -1f, 0f).endVertex();
        consumer.vertex(mat, (float) minX, (float) minY, (float) minZ).color(r, g, b, a).normal(0f, -1f, 0f).endVertex();
        // 顶部矩形
        consumer.vertex(mat, (float) minX, (float) maxY, (float) minZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) maxY, (float) minZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) maxY, (float) minZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) maxY, (float) maxZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) maxY, (float) maxZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) minX, (float) maxY, (float) maxZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) minX, (float) maxY, (float) maxZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) minX, (float) maxY, (float) minZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        // 竖线
        consumer.vertex(mat, (float) minX, (float) minY, (float) minZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) minX, (float) maxY, (float) minZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) minY, (float) minZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) maxY, (float) minZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) minY, (float) maxZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) maxY, (float) maxZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) minX, (float) minY, (float) maxZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) minX, (float) maxY, (float) maxZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
    }

    // ════════ 核心攻击逻辑 ════════

    /**
     * ★ 寻找目标
     *
     * 使用原始玩家位置（originalPos）进行距离判断，而不是当前玩家位置。
     * 这是因为自由相机模式下，当前玩家位置被设为自由相机位置，
     * 如果使用当前玩家位置判断距离，会导致远距离目标检测失败。
     */
    private Entity findTarget(StandAttackConfig cfg) {
        if (mc.level == null || mc.player == null) return null;

        // ★ 选中模式：只返回手动选中的目标（右键选中，长按左键攻击）
        if (cfg.selectMode) {
            // ★ 选中模式不受范围限制，选中即攻击
            if (this.selectedEntity == null || !this.selectedEntity.isAlive()) return null;
            return this.selectedEntity;
        }

        // ★ 使用原始位置进行距离判断（自由相机模式下使用攻击前保存的位置）
        Vec3 playerPos = this.originalPos != null ? this.originalPos : mc.player.position();
        // 如果原始位置未设置（首次运行），使用当前玩家位置
        if (this.originalPos == null) {
            playerPos = mc.player.position();
        }

        Set<String> allowedTypes = cfg.getEntityTypeSet();

        // 1. 准星瞄准实体
        if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.ENTITY) {
            Entity target = ((net.minecraft.world.phys.EntityHitResult) mc.hitResult).getEntity();
            if (entityFilter(target, cfg, allowedTypes, playerPos)) return target;
        }

        // 2. 准星方块附近实体
        if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = ((BlockHitResult) mc.hitResult).getBlockPos();
            Vec3 blockCenter = new Vec3(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
            Entity best = null; double bestDist = Double.MAX_VALUE;
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (!entityFilter(entity, cfg, allowedTypes, playerPos)) continue;
                double dist = entity.position().distanceTo(blockCenter);
                if (dist < bestDist && dist <= 3.0) { bestDist = dist; best = entity; }
            }
            if (best != null) return best;
        }

        // 3. 最近实体（全量遍历）
        Entity best = null; double bestDist = Double.MAX_VALUE;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!entityFilter(entity, cfg, allowedTypes, playerPos)) continue;
            double dist = entity.position().distanceTo(playerPos);
            if (dist < bestDist && dist <= cfg.maxRange) { bestDist = dist; best = entity; }
        }
        return best;
    }

    /**
     * ★ 从玩家/自由相机发射射线检测实体（用于选中模式→32k弓风格自动选中）
     *
     * 手动遍历实体，检测准星指向的最近实体。
     * 灵魂出窍激活时使用自由相机位置/旋转，否则使用玩家眼位。
     * 不用 mc.hitResult，因为右键时 Minecraft 优先检测方块，导致实体无法被选中。
     *
     * ★ 32k弓风格：使用角度阈值（10度）+ 评分排序（角度×距离），
     *   准星靠近实体即可选中，无需精确瞄准碰撞箱上某一点。
     *   评分越低越优先，因为角度越小+距离越近越容易命中。
     * 该方法由赛博教员实现
     */
    private Entity raycastEntity(double range) {
        if (mc.player == null || mc.level == null) return null;
        // ★ 灵魂出窍时使用自由相机位置/旋转
        Vec3 eyePos;
        Vec3 lookVec;
        if (FreecamManager.isActive()) {
            eyePos = FreecamManager.getPosition();
            lookVec = Vec3.directionFromRotation(FreecamManager.getXRot(), FreecamManager.getYRot());
        } else {
            eyePos = mc.player.getEyePosition(1.0f);
            lookVec = mc.player.getLookAngle();
        }

        Entity closest = null;
        double closestScore = Double.MAX_VALUE;
        AABB searchBox = mc.player.getBoundingBox().inflate(range);

        for (Entity entity : mc.level.getEntities(mc.player, searchBox)) {
            if (entity == mc.player || !entity.isAlive() || !(entity instanceof LivingEntity)) continue;

            Vec3 boxCenter = entity.getBoundingBox().getCenter();
            Vec3 toEntity = boxCenter.subtract(eyePos);
            double dist = toEntity.length();
            if (dist > range || dist < 0.1) continue;

            // ★ 计算实体中心与准星方向的夹角
            double angle = Math.acos(Math.min(1.0, Math.max(-1.0,
                toEntity.dot(lookVec) / (dist * lookVec.length())))) * 180.0 / Math.PI;

            // ★ 32k弓风格：10度阈值，准星靠近实体即可选中
            if (angle > 10.0) continue;

            // ★ 评分 = 角度 × 距离（角度越小+距离越近越好）
            double score = angle * dist;
            if (score < closestScore) {
                closestScore = score;
                closest = entity;
            }
        }
        return closest;
    }

    /** 实体过滤器（使用指定原点进行距离判断） */
    private boolean entityFilter(Entity entity, StandAttackConfig cfg, Set<String> allowedTypes, Vec3 origin) {
        if (!(entity instanceof LivingEntity) || !entity.isAlive() || entity == mc.player) return false;
        if (!cfg.attackAllEntities) {
            String entityTypeKey = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).getPath().toLowerCase();
            if (!allowedTypes.contains(entityTypeKey)) return false;
        }
        // ★ 使用传入的 origin 进行距离判断，而非 mc.player.position()
        if (entity.position().distanceTo(origin) > cfg.maxRange) return false;
        if (cfg.ignoreNamed && entity.hasCustomName()) return false;
        if (cfg.ignoreTamed && entity instanceof TamableAnimal ta && ta.isTame()) return false;
        if (cfg.whitelistEnabled) {
            String entityType = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).getPath().toLowerCase();
            List<String> wl = Arrays.stream(cfg.whitelist.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
            if (wl.contains(entityType)) return false;
        }
        if (entity instanceof Player p) {
            if (p.isCreative() || p.isSpectator()) return false;
        }
        return true;
    }

    /** 启动攻击 */
    private void startAttack(Entity target, StandAttackConfig cfg) {
        if (mc.player == null || mc.level == null) return;
        if (mc.player.connection == null) return;

        // ★ 保存原始玩家位置（用于自由相机模式下的距离判断和回传）
        this.originalPos = mc.player.position();
        this.originalLookVec = mc.player.getLookAngle();
        this.active = true;
        this.returning = false;
        this.attacking = false;
        this.pathIndex = 0;
        this.delayTicks = 0;
        this.lastPacketTime = 0;
        this.totemAttackIndex = 0;

        // 计算目标位置：在目标周围 attackDistance 格内
        Vec3 targetPos = target.position();
        Vec3 toTarget = targetPos.subtract(this.originalPos).normalize();
        int atkDist = cfg.attackDistance;
        // ★ 确保至少攻击距离为1格（否则可能TP到目标内部导致卡住）
        atkDist = Math.max(1, atkDist);
        Vec3 attackPos = targetPos.add(toTarget.scale(-atkDist));

        // 验证攻击位置是否有效，无效则找最近有效点
        if (invalid(attackPos)) {
            Vec3 nearest = findNearestPos(attackPos);
            if (nearest != null) attackPos = nearest;
            else {
                // 仍然无效，尝试直接使用目标位置
                attackPos = targetPos;
            }
        }

        // 计算传送路径
        List<Vec3> path = calculatePath(this.originalPos, attackPos, cfg);
        if (path == null || path.isEmpty()) {
            this.cleanup();
            chatMsg("§6[替身攻击] §c无法计算路径");
            return;
        }

        // 构建完整路径
        this.fullPath = new ArrayList<>();
        this.renderPathNodes.clear();
        this.renderPathNodes.addAll(path);

        // 去程路径
        for (Vec3 p : path) this.fullPath.add(p);

        // 垫包预热
        int spam = cfg.pathfindingMode == 1 ? cfg.paperPackets : 4;
        if (spam > 100) spam = 100;
        for (int i = 0; i < spam; i++) {
            mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(
                mc.player.getX(), mc.player.getY(), mc.player.getZ(), false));
        }

        if (mc.player != null) chatMsg("§6[替身攻击] §a开始传送，节点: " + path.size()
                + "  距离目标: " + String.format("%.1f", targetPos.distanceTo(attackPos)) + "格");
    }

    /** 计算传送路径 */
    private List<Vec3> calculatePath(Vec3 start, Vec3 end, StandAttackConfig cfg) {
        List<Vec3> rawPath = new ArrayList<>();
        switch (cfg.pathfindingMode) {
            case 0 -> rawPath = segmentLine(start, end, cfg.maxStepLength);
            case 1 -> {
                double riseHeight = Math.min(cfg.maxStepLength, cfg.maxRange);
                Vec3 aboveStart = start.add(0, riseHeight, 0);
                rawPath.add(aboveStart);
                Vec3 aboveEnd = end.add(0, riseHeight, 0);
                rawPath.addAll(segmentLine(aboveStart, aboveEnd, cfg.maxStepLength));
                rawPath.add(end);
            }
            case 2 -> {
                AStarPathFinder finder = new AStarPathFinder(mc.level);
                finder.setAirPath(true);
                finder.setAttackRange(3.0);
                List<Vec3> astarPath = finder.findPath(start, end, cfg.maxStepLength);
                if (astarPath != null && astarPath.size() >= 2) rawPath = astarPath;
                else rawPath = segmentLine(start, end, cfg.maxStepLength);
            }
        }
        if (rawPath == null || rawPath.size() < 1) return null;
        if (rawPath.get(0).distanceTo(start) > 0.5) rawPath.add(0, start);
        return rawPath;
    }

    /** 直线分段（含垂直约束≤10格） */
    private List<Vec3> segmentLine(Vec3 from, Vec3 to, double maxStep) {
        List<Vec3> segments = new ArrayList<>();
        double totalDist = from.distanceTo(to);
        double vertDist = Math.abs(to.y - from.y);
        int steps = (int) Math.ceil(totalDist / maxStep);
        int vertSteps = (int) Math.ceil(vertDist / 10.0);
        steps = Math.max(steps, vertSteps);
        if (steps <= 1) { segments.add(to); return segments; }
        for (int i = 1; i <= steps; i++) {
            double t = (double) i / steps;
            segments.add(new Vec3(from.x + (to.x - from.x) * t, from.y + (to.y - from.y) * t, from.z + (to.z - from.z) * t));
        }
        return segments;
    }

    /** 发送位置包（仅发包，不更新客户端玩家位置） */
    private void sendMovePacket(Vec3 pos, boolean onGround) {
        if (mc.player == null || mc.player.connection == null) return;
        mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(pos.x, pos.y, pos.z, onGround));
    }

    /** 执行攻击 */
    private void performAttack(StandAttackConfig cfg) {
        if (mc.player == null || mc.player.connection == null) return;
        if (currentTarget == null) return;
        if (!currentTarget.isAlive()) return;

        mc.player.connection.send(ServerboundInteractPacket.createAttackPacket(currentTarget, mc.player.isShiftKeyDown()));
        if (cfg.swingHand) mc.player.swing(InteractionHand.MAIN_HAND);
        mc.player.resetAttackStrengthTicker();

        // ★ 通知KillIconFeature记录攻击（与TpAuraFeature的attackCounter同理）
        KillIconFeature.markAttackedByTpAura(currentTarget.getId());
        // ★ 通知HealthTagManager记录攻击目标
        HealthTagManager.onAttack(currentTarget);
    }

    /** 检查位置是否无效 */
    private boolean invalid(Vec3 pos) {
        if (mc.level == null || mc.player == null) return true;
        BlockPos bp = BlockPos.containing(pos.x, pos.y, pos.z);
        if (bp.getY() < mc.level.getMinBuildHeight() || bp.getY() >= mc.level.getMaxBuildHeight()) return true;
        if (mc.level.getChunk(bp.getX() >> 4, bp.getZ() >> 4) == null) return true;
        AABB box = mc.player.getBoundingBox().move(pos.subtract(mc.player.position()));
        for (BlockPos bPos : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ), BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
            var state = mc.level.getBlockState(bPos);
            if (!state.getCollisionShape(mc.level, bPos).isEmpty() || state.getBlock() == net.minecraft.world.level.block.Blocks.LAVA) return true;
        }
        return false;
    }

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

    /**
     * ★ 清理攻击状态
     * @param keepFreecam 是否保持 Freecam 激活（攻击完成回传后保持灵魂出窍）
     */
    private void cleanup(boolean keepFreecam) {
        this.active = false;
        this.returning = false;
        this.attacking = false;
        this.currentTarget = null;
        this.fullPath = null;
        this.renderPathNodes.clear();
        this.pathIndex = 0;
        this.delayTicks = 0;
        this.lastPacketTime = 0;
        this.totemAttackIndex = 0;
        this.originalPos = null;
        this.targets.clear();
        this.lastTeleportTime = 0;
        if (!keepFreecam) {
            this.selectedEntity = null;
            // ★ 停用 FreecamManager（仅当 StandAttack 完全禁用时）
            if (FreecamManager.isActive()) {
                FreecamManager.deactivate();
            }
        }
    }

    private void cleanup() {
        cleanup(false);
    }
}