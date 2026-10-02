package fku.org.example.fku.features.tacz; /* water */

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import net.minecraftforge.client.event.RenderLevelStageEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Ghost Peek（幽灵窥视）— 移植自 02の日常 NoSpread 的 ghostpeek
 * 按住激活键：在身侧计算四个窥视点——左右（水平垂直视线，优先）、上下（垂直 y±，次优先），
 * 选中最优点后于本 tick 内：把【服务端】瞬移到窥视点（Pos/Rot 包）→ 锁头/锁身瞄准目标模型点
 * → 直接发武器模组的开火包（TaCZ: ClientMessagePlayerShoot；卓越前线: ClientEventHandler.shootClient）
 * → 立即把服务端拉回本体。因服务端位置已在开火那一瞬位于窥视点，子弹从窥视点发出，
 * 做到“从墙后/矮墙上/地缝里偷射”。客户端位置不移动（仅卓越前线开火瞬间短暂借位视角），
 * 所以本地镜头不闪跳。本方案用射击包而非模拟左键——避免左键开火被延迟到下一 tick 输入阶段、
 * 导致实际出弹点落在回位后的本体。
 * 该功能由赛博教员移植到 fku
 */
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, value = {Dist.CLIENT})
public class GhostPeekFeature {

    private static final Minecraft mc = Minecraft.getInstance();
    private static boolean initialized = false;

    // ── 状态 ──
    private static Vec3 ghostPeekLeftPos = null;    // 左（优先）
    private static Vec3 ghostPeekRightPos = null;   // 右（优先）
    private static Vec3 ghostPeekUpPos = null;      // 上（垂直+，次优先）
    private static Vec3 ghostPeekDownPos = null;    // 下（垂直−，次优先）
    private static Vec3 ghostPeekOriginPos = null;  // 原点（玩家原地，最优先：无需位移即可命中）
    private static int ghostPeekStage = 0;
    private static Vec3 ghostPeekTargetPos = null;
    private static Vec3 ghostPeekSelectedPos = null;
    private static Vec3 ghostPeekAimPos = null;
    private static LivingEntity ghostPeekTarget = null;
    private static int ghostPeekCooldown = 0;
    // 分 tick 模式：客户端“站到窥视点”所需的回位/角度备份
    private static Vec3 ghostPeekReturnPos = null;
    private static float ghostPeekOldYaw = 0.0F;
    private static float ghostPeekOldPitch = 0.0F;
    private static float ghostPeekArmedYaw = 0.0F;
    private static float ghostPeekArmedPitch = 0.0F;

    // ── 武器模组开火包反射（TaCZ / 卓越前线）──
    private static boolean taczAvailable = false;
    private static Class<?> taczOperatorClass;        // com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator
    private static Method taczFromLocalPlayer;
    private static Method taczGetDataHolder;
    private static Method taczShoot;
    private static Field taczClientBaseTimestamp;
    private static Class<?> taczShootMsgClass;         // com.tacz.guns.network.message.ClientMessagePlayerShoot
    private static Constructor<?> taczShootMsgCtor;
    private static Field taczChannelField;             // com.tacz.guns.network.NetworkHandler.CHANNEL
    private static Method taczSendToServer;

    private static boolean swAvailable = false;
    private static Class<?> swClientEventHandler;      // com.atsuishio.superbwarfare.event.ClientEventHandler
    private static Method swShootClient;               // shootClient(Lnet/minecraft/world/entity/player/Player;)V
    private static Field swHoldingFireKey;             // holdingFireKey:Z

    public static void init() {
        if (initialized) return;
        initialized = true;
        initShootReflection();
        // 注意：本类已由 @Mod.EventBusSubscriber 自动注册，不要再用 EVENT_BUS.register 重复注册，
        // 否则 onClientTick / onRenderLevel 会跑两遍，渲染 PoseStack 易失衡。
        Fku.LOGGER.info("[GhostPeekFeature] 幽灵窥视已初始化 (TaCZ=" + taczAvailable + ", 卓越前线=" + swAvailable + ")");
    }

    public static boolean isEnabled() {
        TaCZConfig cfg = TaCZConfig.getInstance();
        return cfg.masterEnabled && cfg.ghostPeekEnabled;
    }

    /** 幽灵窥视是否正在“闪身”（单 tick 瞬间开火进行中，或分 tick 的 stage1/2）。
     *  自瞄据此抑制自身旋转发包，避免用本体角度覆盖窥视开火角度。 */
    public static boolean isBusy() {
        return ghostPeekStage != 0;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (mc.player == null || mc.level == null) return;
        if (event.phase == TickEvent.Phase.START) {
            // 客户端从不移动（与 NoSpread 一致）：窥视点瞬移与角度只发【服务端】包，
            // 本地镜头不闪跳。开火前仅把客户端旋转借到目标（fireGhostPeek 里 setYRot/setXRot），
            // 待开火包发出后立即复原，不影响本地渲染。
            return;
        }
        TaCZConfig cfg = TaCZConfig.getInstance();
        if (!isEnabled()) return;
        handleGhostPeek(mc);
    }

    // ── 主逻辑 ──

    private static void handleGhostPeek(Minecraft mc) {
        TaCZConfig cfg = TaCZConfig.getInstance();
        if (!isKeyPressed(mc, cfg.ghostPeekKey)) {
            resetState();
            return;
        }
        // 持续计算左右闪身点，便于渲染预览（圈随视角实时更新）；只在松键时清除
        if (isHoldingGun(mc)) {
            calculateGhostPeekPositions(mc);
        }
        // 回本体（stage2）：开火后下一 tick 才把服务端拉回本体，与开火错开一拍。
        // 关键：若回位包与开火包同 tick，服务端批量结算时玩家已落回 body，子弹从 body 出 → 对侧偏移。
        if (ghostPeekStage == 2) {
            returnGhostPeek(mc);
            return;
        }
        // 开火一拍（stage1→stage2）：上一 tick 已把【服务端】瞬移到窥视点，本 tick 发角度+开火（不回位）
        // 关键：无论 ghostPeekSinglePlayer 开关如何，统一走“先瞬移→下一 tick 开火”的两拍流程
        // （与 NoSpread 默认一致）。否则同 tick 把瞬移包+开火包塞进同一批次，
        // 服务端结算时先用本体位置出弹再被拉到 peek → 子弹从 body 出，按 peek 角度飞，
        // 被平行推到对侧（偏移量恰为 body-peek 距离）。
        if (ghostPeekStage == 1) {
            fireGhostPeek(mc);
            return;
        }
        if (ghostPeekCooldown > 0) {
            --ghostPeekCooldown;
            return;
        }
        if (!isHoldingGun(mc)) return;
        LivingEntity target = getTarget(mc.player, true);
        if (target != null) {
            Vec3[] best = selectBestGhostPeekPoint(mc, target);
            if (best != null) {
                ghostPeekSelectedPos = best[0];
                ghostPeekAimPos = best[1];
                ghostPeekTarget = target;
                // 始终走“先瞬移(server)→下一 tick 开火”的两拍流程（与 NoSpread 默认一致，最稳）：
                // 服务端有整整一 tick 把玩家位置结算到窥视点，开火包处理时玩家已在 peek，子弹从 peek 出。
                armGhostPeek(mc, best[0], best[1], target);
                ghostPeekCooldown = Math.max(1, cfg.ghostPeekFrequency);
            }
        }
    }

    private static void resetState() {
        // 中途松键：若服务端还停在窥视点（stage2 待回位），立即拉回本体，避免卡在 peek
        if (ghostPeekReturnPos != null && Minecraft.getInstance().getConnection() != null) {
            Minecraft.getInstance().getConnection().send(new ServerboundMovePlayerPacket.PosRot(
                    ghostPeekReturnPos.x, ghostPeekReturnPos.y, ghostPeekReturnPos.z, ghostPeekOldYaw, ghostPeekOldPitch, true));
        }
        ghostPeekLeftPos = null;
        ghostPeekRightPos = null;
        ghostPeekUpPos = null;
        ghostPeekDownPos = null;
        ghostPeekOriginPos = null;
        ghostPeekSelectedPos = null;
        ghostPeekAimPos = null;
        ghostPeekTarget = null;
        ghostPeekStage = 0;
        ghostPeekCooldown = 0;
        ghostPeekReturnPos = null;
        ghostPeekOldYaw = 0.0F;
        ghostPeekOldPitch = 0.0F;
    }

    /** 分 tick 第一拍（stage1）：把【服务端】瞬移到窥视点（仅位置，裸 Pos 包）。
     *  客户端不动（不 setPos），角度留到下一拍与开火同一批发出，避免“位置跳变+角度”被拆/被丢。
     *  逻辑与 NoSpread.executeGhostPeekStage1 一致。 */
    private static void armGhostPeek(Minecraft mc, Vec3 peekPos, Vec3 aimPos, LivingEntity target) {
        ghostPeekReturnPos = mc.player.position();
        ghostPeekOldYaw = mc.player.getYRot();
        ghostPeekOldPitch = mc.player.getXRot();
        ghostPeekSelectedPos = peekPos;
        ghostPeekAimPos = aimPos;
        ghostPeekTarget = target;
        ghostPeekStage = 1;

        // 服务端瞬移到窥视点（位置分量），角度下一拍再发
        teleportPackets(mc, ghostPeekReturnPos, peekPos);
        mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(peekPos.x, peekPos.y, peekPos.z, true));
    }

    /** 分 tick 第二拍（stage2）：与开火同一批把角度（Rot + noSpread 的 PosRot）送达服务端，
     *  服务端那一瞬位于窥视点、角度锁定目标，子弹即从窥视点朝目标出弹。随后立刻把服务端拉回本体。
     *  客户端仅临时借位旋转（setYRot/setXRot），开火包发出后立即复原，不闪镜头。
     *  逻辑与 NoSpread.executeGhostPeekStage2 一致。 */
    private static void fireGhostPeek(Minecraft mc) {
        TaCZConfig cfg = TaCZConfig.getInstance();
        if (ghostPeekSelectedPos == null || ghostPeekAimPos == null || ghostPeekTarget == null) {
            ghostPeekStage = 0;
            return;
        }
        Vec3 peekPos = ghostPeekSelectedPos;
        Vec3 aimPos = ghostPeekAimPos;
        LivingEntity target = ghostPeekTarget;
        Vec3 eyePos = peekPos.add(0.0, mc.player.getEyeHeight(), 0.0);
        Vec3 velocity = target.getDeltaMovement();
        // 回带量（刻）：默认 0（直接锁当前点，最近距离最精准）；>0 时按配置反向补偿（玩家用配置值，怪物用 2.5 倍提前量）
        double factor = cfg.ghostPeekBacktrack > 0
                ? (target instanceof Player ? -cfg.ghostPeekBacktrack : -cfg.ghostPeekBacktrack * 2.5)
                : 0.0;
        Vec3 predictedAimPos = aimPos.add(velocity.scale(factor));
        float[] rotations = calculateAngles(eyePos, predictedAimPos);
        float oldYaw = mc.player.getYRot();
        float oldPitch = mc.player.getXRot();
        double oldPX = mc.player.getX(), oldPY = mc.player.getY(), oldPZ = mc.player.getZ();

        // 客户端仅临时借位旋转（与 NoSpread 一致，不 setPos 到 peek）：
        // 开火走 ClientMessagePlayerShoot(timestamp) 由服务端权威出弹，服务端位置由上面的 PosRot(peek) 决定；
        // 客户端设位置到 peek 反而会让同 tick 的常规移动包报告 peek 再被拉回，徒增冲突。
        mc.player.setYRot(rotations[0]);
        mc.player.setXRot(rotations[1]);
        // 角度包：独立 Rot 先发（多数服接受）
        mc.getConnection().send(new ServerboundMovePlayerPacket.Rot(rotations[0], rotations[1], true));
        // 关键：noSpread 的 PosRot(peek,rot) 同时携带位置+角度，服务端据此出弹方向。
        // 无论 ghostPeekNoSpread 开关如何，至少发 1 个保证角度生效（避免被当成纯 Rot 丢弃）。
        mc.getConnection().send(new ServerboundMovePlayerPacket.PosRot(
                peekPos.x, peekPos.y, peekPos.z, rotations[0], rotations[1], true));
        if (cfg.ghostPeekNoSpread) {
            for (int i = 1; i < cfg.ghostPeekNoSpreadPackets; ++i) {
                mc.getConnection().send(new ServerboundMovePlayerPacket.PosRot(
                        peekPos.x, peekPos.y, peekPos.z, rotations[0], rotations[1], true));
            }
        }
        // 开火：TaCZ 用 ClientMessagePlayerShoot(timestamp)；服务端在（窥视点+锁定角度）的玩家身上出弹
        boolean fired = fireCurrentWeapon(mc);
        if (!fired) {
            Fku.LOGGER.warn("[GhostPeekFeature] 未检测到可用武器模组开火包（需装 TaCZ 或 卓越前线）");
        }
        // 复原客户端旋转 + 位置（在渲染前还原，不闪镜头）
        mc.player.setYRot(oldYaw);
        mc.player.setXRot(oldPitch);
        mc.player.setPos(oldPX, oldPY, oldPZ);
        // ★ 回本体包【不在此 tick 发】：改由下一 tick 的 returnGhostPeek 发送。
        //   否则开火包与回位包同拍，服务端批量结算时玩家已落回 body，子弹从 body 出 → 对侧偏移。

        // 进入“待回位”状态（stage2）；回位交下一 tick 处理，确保开火那 tick 服务端停在窥视点
        ghostPeekStage = 2;
        ghostPeekSelectedPos = null;
        ghostPeekAimPos = null;
        ghostPeekTarget = null;
        // ghostPeekReturnPos / ghostPeekOldYaw / ghostPeekOldPitch 保留给 returnGhostPeek
    }

    /** 回本体一拍（stage2→0）：开火后下一 tick 才把服务端拉回本体，
     *  使开火那 tick 服务端停在窥视点（不被回位包围而批量结算到 body），子弹即从窥视点出。 */
    private static void returnGhostPeek(Minecraft mc) {
        if (ghostPeekReturnPos == null) {
            ghostPeekStage = 0;
            return;
        }
        mc.getConnection().send(new ServerboundMovePlayerPacket.PosRot(
                ghostPeekReturnPos.x, ghostPeekReturnPos.y, ghostPeekReturnPos.z, ghostPeekOldYaw, ghostPeekOldPitch, true));
        mc.getConnection().send(new ServerboundMovePlayerPacket.Rot(ghostPeekOldYaw, ghostPeekOldPitch, true));
        ghostPeekStage = 0;
        ghostPeekReturnPos = null;
        ghostPeekOldYaw = 0.0F;
        ghostPeekOldPitch = 0.0F;
    }

    private static void calculateGhostPeekPositions(Minecraft mc) {
        TaCZConfig cfg = TaCZConfig.getInstance();
        Vec3 playerPos = mc.player.position();
        Vec3 lookAngle = mc.player.getLookAngle();
        Vec3 horizontalLook = new Vec3(lookAngle.x, 0.0, lookAngle.z).normalize();
        Vec3 rightDir = new Vec3(-horizontalLook.z, 0.0, horizontalLook.x);
        Vec3 leftDir = new Vec3(horizontalLook.z, 0.0, -horizontalLook.x);
        double distance = cfg.ghostPeekBlocks;
        double upDist = cfg.ghostPeekUpBlocks;
        double downDist = cfg.ghostPeekDownBlocks;
        // 左右（优先）：垂直于视线方向
        Vec3 leftPos = playerPos.add(leftDir.scale(distance));
        Vec3 rightPos = playerPos.add(rightDir.scale(distance));
        // 上下（次优先）：纯垂直方向（翻越矮墙 y+ / 钻地缝 y−），水平位置不变；上/下距离独立配置
        Vec3 upPos = new Vec3(playerPos.x, playerPos.y + upDist, playerPos.z);
        Vec3 downPos = new Vec3(playerPos.x, playerPos.y - downDist, playerPos.z);
        ghostPeekLeftPos = isValidGhostPeekPosition(mc, playerPos, leftPos) ? leftPos : null;
        ghostPeekRightPos = isValidGhostPeekPosition(mc, playerPos, rightPos) ? rightPos : null;
        ghostPeekUpPos = isValidGhostPeekPosition(mc, playerPos, upPos) ? upPos : null;
        ghostPeekDownPos = isValidGhostPeekPosition(mc, playerPos, downPos) ? downPos : null;
        // 原点（玩家原地）：无需位移即可命中则最优先；开启后恒为候选
        ghostPeekOriginPos = cfg.ghostPeekOriginEnabled ? playerPos : null;
    }

    private static boolean isValidGhostPeekPosition(Minecraft mc, Vec3 playerPos, Vec3 peekPos) {
        if (mc.level == null) return false;
        BlockPos pos1 = new BlockPos((int) Math.floor(peekPos.x), (int) Math.floor(peekPos.y), (int) Math.floor(peekPos.z));
        BlockPos pos2 = pos1.above();
        if (mc.level.isEmptyBlock(pos1) && mc.level.isEmptyBlock(pos2)) {
            ClipContext context = new ClipContext(
                    playerPos.add(0.0, 1.0, 0.0), peekPos.add(0.0, 1.0, 0.0),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null);
            return mc.level.clip(context).getType() == HitResult.Type.MISS;
        }
        return false;
    }

    private static Vec3[] selectBestGhostPeekPoint(Minecraft mc, LivingEntity target) {
        // ① 原点（玩家原地）最优先：原地即可命中目标则直接原地点开火（无需位移）
        Vec3 originPos = ghostPeekOriginPos;
        Vec3 originAimPos = originPos != null ? getBestAimPositionFromGhostPeek(mc, originPos, target) : null;
        if (originAimPos != null) {
            return new Vec3[]{originPos, originAimPos};
        }
        // ② 左右（优先）：垂直于视线方向；能命中则取距目标更近的一侧
        Vec3 leftPos = ghostPeekLeftPos;
        Vec3 rightPos = ghostPeekRightPos;
        Vec3 leftAimPos = leftPos != null ? getBestAimPositionFromGhostPeek(mc, leftPos, target) : null;
        Vec3 rightAimPos = rightPos != null ? getBestAimPositionFromGhostPeek(mc, rightPos, target) : null;
        boolean leftCanHit = leftAimPos != null;
        boolean rightCanHit = rightAimPos != null;
        if (leftCanHit && rightCanHit) {
            double leftDist = leftPos.distanceTo(target.position());
            double rightDist = rightPos.distanceTo(target.position());
            return leftDist <= rightDist ? new Vec3[]{leftPos, leftAimPos} : new Vec3[]{rightPos, rightAimPos};
        } else if (leftCanHit) {
            return new Vec3[]{leftPos, leftAimPos};
        } else if (rightCanHit) {
            return new Vec3[]{rightPos, rightAimPos};
        }
        // 左右都无法命中：退而求其次，尝试上下（垂直 y±）
        Vec3 upPos = ghostPeekUpPos;
        Vec3 downPos = ghostPeekDownPos;
        Vec3 upAimPos = upPos != null ? getBestAimPositionFromGhostPeek(mc, upPos, target) : null;
        Vec3 downAimPos = downPos != null ? getBestAimPositionFromGhostPeek(mc, downPos, target) : null;
        boolean upCanHit = upAimPos != null;
        boolean downCanHit = downAimPos != null;
        if (upCanHit && downCanHit) {
            double upDist = upPos.distanceTo(target.position());
            double downDist = downPos.distanceTo(target.position());
            return upDist <= downDist ? new Vec3[]{upPos, upAimPos} : new Vec3[]{downPos, downAimPos};
        } else if (upCanHit) {
            return new Vec3[]{upPos, upAimPos};
        } else if (downCanHit) {
            return new Vec3[]{downPos, downAimPos};
        }
        return null;
    }

    /** 取窥视点可见的瞄准点。
     *  按配置部位取碰撞箱几何中心（头部=顶部 25% 区间中点；身体=碰撞箱中点）并校验可见；
     *  目标部位被墙遮挡不可见时，再退回另一部位或体素采样质心。
     *  这样「头部/身体」开关真正生效，且瞄点落在部位中心（比可见点质心更准：露头时质心易被墙边拉低 → 擦弹/打身体）。 */
    private static Vec3 getBestAimPositionFromGhostPeek(Minecraft mc, Vec3 peekPos, LivingEntity target) {
        TaCZConfig cfg = TaCZConfig.getInstance();
        Vec3 eyePos = peekPos.add(0.0, mc.player.getEyeHeight(), 0.0);
        AABB box = target.getBoundingBox();
        double h = box.maxY - box.minY;
        // 头部中心：碰撞箱顶部 25% 区间的中点
        Vec3 headCenter = new Vec3((box.minX + box.maxX) / 2.0, box.maxY - h * 0.125, (box.minZ + box.maxZ) / 2.0);
        // 身体中心：碰撞箱中点
        Vec3 bodyCenter = new Vec3((box.minX + box.maxX) / 2.0, box.minY + h * 0.5, (box.minZ + box.maxZ) / 2.0);
        boolean aimBody = "身体".equals(cfg.ghostPeekAimPart);
        if (aimBody) {
            if (isGhostPeekVisible(eyePos, bodyCenter, target)) return bodyCenter;
            if (isGhostPeekVisible(eyePos, headCenter, target)) return headCenter;
            Vec3 sampled = getVisibleCenterPointFromGhostPeek(eyePos, target);
            return sampled;
        } else {
            if (isGhostPeekVisible(eyePos, headCenter, target)) return headCenter;
            if (isGhostPeekVisible(eyePos, bodyCenter, target)) return bodyCenter;
            Vec3 sampled = getVisibleHeadPointFromGhostPeek(eyePos, target);
            return sampled;
        }
    }

    /** 在目标头部区域（碰撞箱顶部 25%）体素采样，返回对窥视点可见点的质心；无可见点返回 null。 */
    private static Vec3 getVisibleHeadPointFromGhostPeek(Vec3 start, LivingEntity target) {
        if (mc.level == null) return null;
        AABB box = target.getBoundingBox();
        double headRatio = 0.25;
        double headMinY = box.maxY - (box.maxY - box.minY) * headRatio;
        List<Vec3> visibleHeadPoints = new ArrayList<>();
        double minX = box.minX, maxX = box.maxX;
        double minY = headMinY, maxY = box.maxY;
        double minZ = box.minZ, maxZ = box.maxZ;
        int stepsX = 5, stepsY = 4, stepsZ = 5;
        for (int ix = 0; ix <= stepsX; ++ix) {
            for (int iy = 0; iy <= stepsY; ++iy) {
                for (int iz = 0; iz <= stepsZ; ++iz) {
                    double x = minX + (maxX - minX) * (double) ix / stepsX;
                    double y = minY + (maxY - minY) * (double) iy / stepsY;
                    double z = minZ + (maxZ - minZ) * (double) iz / stepsZ;
                    Vec3 point = new Vec3(x, y, z);
                    if (isGhostPeekVisible(start, point, target)) visibleHeadPoints.add(point);
                }
            }
        }
        if (visibleHeadPoints.isEmpty()) return null;
        if (visibleHeadPoints.size() <= 4) return visibleHeadPoints.get(0);
        double sumX = 0.0, sumY = 0.0, sumZ = 0.0;
        for (Vec3 p : visibleHeadPoints) { sumX += p.x; sumY += p.y; sumZ += p.z; }
        Vec3 center = new Vec3(sumX / visibleHeadPoints.size(), sumY / visibleHeadPoints.size(), sumZ / visibleHeadPoints.size());
        if (isGhostPeekVisible(start, center, target)) return center;
        Vec3 bestPoint = null;
        double bestDistSq = Double.MAX_VALUE;
        for (Vec3 p : visibleHeadPoints) {
            double d = p.distanceToSqr(center);
            if (d < bestDistSq) { bestDistSq = d; bestPoint = p; }
        }
        return bestPoint;
    }

    /** 在目标整体碰撞箱体素采样，返回对窥视点可见点的质心；无可见点返回 null。 */
    private static Vec3 getVisibleCenterPointFromGhostPeek(Vec3 start, LivingEntity target) {
        if (mc.level == null) return null;
        AABB box = target.getBoundingBox();
        List<Vec3> visiblePoints = new ArrayList<>();
        double minX = box.minX, maxX = box.maxX;
        double minY = box.minY, maxY = box.maxY;
        double minZ = box.minZ, maxZ = box.maxZ;
        int stepsX = 8, stepsY = 12, stepsZ = 8;
        for (int ix = 0; ix <= stepsX; ++ix) {
            for (int iy = 0; iy <= stepsY; ++iy) {
                for (int iz = 0; iz <= stepsZ; ++iz) {
                    double x = minX + (maxX - minX) * (double) ix / stepsX;
                    double y = minY + (maxY - minY) * (double) iy / stepsY;
                    double z = minZ + (maxZ - minZ) * (double) iz / stepsZ;
                    Vec3 point = new Vec3(x, y, z);
                    if (isGhostPeekVisible(start, point, target)) visiblePoints.add(point);
                }
            }
        }
        if (visiblePoints.isEmpty()) return null;
        if (visiblePoints.size() <= 8) return visiblePoints.get(0);
        double sumX = 0.0, sumY = 0.0, sumZ = 0.0;
        for (Vec3 p : visiblePoints) { sumX += p.x; sumY += p.y; sumZ += p.z; }
        Vec3 center = new Vec3(sumX / visiblePoints.size(), sumY / visiblePoints.size(), sumZ / visiblePoints.size());
        if (isGhostPeekVisible(start, center, target)) return center;
        Vec3 bestPoint = null;
        double bestDistSq = Double.MAX_VALUE;
        for (Vec3 p : visiblePoints) {
            double d = p.distanceToSqr(center);
            if (d < bestDistSq) { bestDistSq = d; bestPoint = p; }
        }
        return bestPoint;
    }

    private static boolean isGhostPeekVisible(Vec3 start, Vec3 end, LivingEntity target) {
        TaCZConfig cfg = TaCZConfig.getInstance();
        if (cfg.ghostPeekWallBang) {
            double distance = start.distanceTo(end);
            if (distance <= cfg.ghostPeekWallBangRange) return true;
        }
        if (mc.level == null) return false;
        ClipContext context = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null);
        return mc.level.clip(context).getType() == HitResult.Type.MISS;
    }

    // ── 索敌 ──

    private static LivingEntity getTarget(Player player, boolean ignoreVisibility) {
        if (player == null || mc.level == null) return null;
        TaCZConfig cfg = TaCZConfig.getInstance();
        double range = cfg.ghostPeekRange;
        double rangeSq = range * range;
        double fov = Math.max(0.0, cfg.ghostPeekFov); // 准星锥角限制，避免锁到身后/侧面无关目标
        Vec3 look = player.getLookAngle();
        LivingEntity best = null;
        double bestAngle = Double.MAX_VALUE;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof LivingEntity)) continue;
            LivingEntity living = (LivingEntity) e;
            if (e == player || !living.isAlive()) continue;
            if (!isValidTarget(living)) continue;
            double distSq = player.distanceToSqr(e);
            if (distSq > rangeSq) continue;
            Vec3 center = e.position().add(0.0, living.getBbHeight() / 2.0, 0.0);
            Vec3 to = center.subtract(player.getEyePosition()).normalize();
            double dot = look.dot(to);
            dot = Math.max(-1.0, Math.min(1.0, dot));
            double angle = Math.toDegrees(Math.acos(dot));
            if (fov < 360.0 && angle > fov / 2.0) continue;
            if (angle < bestAngle) { bestAngle = angle; best = living; }
        }
        return best;
    }

    private static boolean isValidTarget(LivingEntity e) {
        if (e.isSpectator()) return false;
        TaCZConfig cfg = TaCZConfig.getInstance();
        if (cfg.ghostPeekPlayersOnly) {
            if (!(e instanceof Player)) return false;
        }
        // 不打队友（队伍颜色相同）：开启后无论是否“仅玩家”都排除队友玩家
        if (cfg.dontHitTeammates && e instanceof Player p && isTeammate(p)) return false;
        if (!cfg.ghostPeekPlayersOnly) {
            if (e instanceof Player p && p.isCreative()) return false;
        }
        return true;
    }

    private static boolean isTeammate(Player other) {
        LocalPlayer self = mc.player;
        if (self == null) return false;
        net.minecraft.world.scores.Team myTeam = self.getTeam();
        net.minecraft.world.scores.Team otherTeam = other.getTeam();
        if (myTeam == null || otherTeam == null) return false;
        return myTeam.getColor() == otherTeam.getColor();
    }

    // ── 角度计算 ──

    private static float[] calculateAngles(Vec3 start, Vec3 targetPos) {
        double dX = targetPos.x - start.x;
        double dY = targetPos.y - start.y;
        double dZ = targetPos.z - start.z;
        double distH = Math.sqrt(dX * dX + dZ * dZ);
        float yaw = (float) (Math.toDegrees(Math.atan2(dZ, dX)) - 90.0);
        float pitch = (float) (-Math.toDegrees(Math.atan2(dY, distH)));
        return new float[]{yaw, pitch};
    }

    // ── 瞬移发包 ──（把服务端位置按 8 格步进插值发包，覆盖长距离瞬移，短距离退化为单个 Pos 包）

    private static void teleportPackets(Minecraft mc, Vec3 start, Vec3 end) {
        double dist = start.distanceTo(end);
        double stepSize = 8.0;
        int packets = (int) Math.ceil(dist / stepSize);
        if (packets <= 1) {
            mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(end.x, end.y, end.z, true));
            return;
        }
        Vec3 diff = end.subtract(start);
        Vec3 step = diff.scale(1.0 / packets);
        for (int i = 1; i < packets; ++i) {
            Vec3 pos = start.add(step.scale(i));
            mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(pos.x, pos.y, pos.z, true));
        }
        mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(end.x, end.y, end.z, true));
    }

    // ── 按键 ──

    private static boolean isKeyPressed(Minecraft mc, int key) {
        if (key < 0) return false;
        long window = mc.getWindow().getWindow();
        if (key >= 1000) {
            int button = key - 1000;
            return GLFW.glfwGetMouseButton(window, button) == GLFW.GLFW_PRESS;
        }
        return InputConstants.isKeyDown(window, key);
    }

    // ── 武器模组开火包（反射：TaCZ / 卓越前线） ──

    /** 是否手持可开火武器：与自瞄一致——仅手持 TaCZ / 卓越前线 枪械时生效（复用 AimbotFeature 判定）。 */
    private static boolean isHoldingGun(Minecraft mc) {
        return AimbotFeature.isHoldingGunWeapon();
    }

    /** 初始化两个武器模组的开火反射；未安装则对应 available=false（不报错）。 */
    private static void initShootReflection() {
        // TaCZ：IClientPlayerGunOperator.fromLocalPlayer(player).getDataHolder().clientBaseTimestamp
        //       >0 时发 ClientMessagePlayerShoot(timestamp)；否则直接 .shoot()
        try {
            taczOperatorClass = Class.forName("com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator");
            taczFromLocalPlayer = taczOperatorClass.getMethod("fromLocalPlayer", LocalPlayer.class);
            taczGetDataHolder = taczOperatorClass.getMethod("getDataHolder");
            taczShoot = taczOperatorClass.getMethod("shoot");
            taczClientBaseTimestamp = taczGetDataHolder.getReturnType().getDeclaredField("clientBaseTimestamp");
            taczClientBaseTimestamp.setAccessible(true);
            taczShootMsgClass = Class.forName("com.tacz.guns.network.message.ClientMessagePlayerShoot");
            taczShootMsgCtor = taczShootMsgClass.getConstructor(long.class);
            taczChannelField = Class.forName("com.tacz.guns.network.NetworkHandler").getDeclaredField("CHANNEL");
            taczChannelField.setAccessible(true);
            taczSendToServer = taczChannelField.getType().getMethod("sendToServer", Object.class);
            taczAvailable = true;
        } catch (Throwable t) {
            taczAvailable = false;
            Fku.LOGGER.warn("[GhostPeekFeature] 未检测到 TaCZ，开火包走卓越前线/兜底: " + t);
        }
        // 卓越前线：ClientEventHandler.shootClient(player)；需先置 holdingFireKey=true 才能强制开火
        try {
            swClientEventHandler = Class.forName("com.atsuishio.superbwarfare.event.ClientEventHandler");
            swShootClient = swClientEventHandler.getMethod("shootClient", net.minecraft.world.entity.player.Player.class);
            swHoldingFireKey = swClientEventHandler.getField("holdingFireKey");
            swHoldingFireKey.setAccessible(true);
            swAvailable = true;
        } catch (Throwable t) {
            swAvailable = false;
            Fku.LOGGER.warn("[GhostPeekFeature] 未检测到卓越前线，开火包走 TaCZ/兜底: " + t);
        }
    }

    /** 发开火包：优先 TaCZ（服务端包最稳），其次卓越前线；都不可用返回 false。 */
    private static boolean fireCurrentWeapon(Minecraft mc) {
        if (taczAvailable && tryTaczFire(mc)) return true;
        if (swAvailable && trySuperbWarfareFire(mc)) return true;
        return false;
    }

    private static boolean tryTaczFire(Minecraft mc) {
        try {
            Object operator = taczFromLocalPlayer.invoke(null, mc.player);
            if (operator == null) return false;
            Object holder = taczGetDataHolder.invoke(operator);
            long base = (long) taczClientBaseTimestamp.get(holder);
            if (base > 0L) {
                Object msg = taczShootMsgCtor.newInstance(System.currentTimeMillis() - base);
                Object channel = taczChannelField.get(null);
                taczSendToServer.invoke(channel, msg);
            } else {
                taczShoot.invoke(operator);
            }
            return true;
        } catch (Throwable t) {
            Fku.LOGGER.warn("[GhostPeekFeature] TaCZ 开火异常: " + t);
            return false;
        }
    }

    private static boolean trySuperbWarfareFire(Minecraft mc) {
        try {
            boolean old = swHoldingFireKey.getBoolean(null);
            swHoldingFireKey.setBoolean(null, true);
            try {
                swShootClient.invoke(null, mc.player);
            } finally {
                swHoldingFireKey.setBoolean(null, old);
            }
            return true;
        } catch (Throwable t) {
            Fku.LOGGER.warn("[GhostPeekFeature] 卓越前线开火异常: " + t);
            return false;
        }
    }

    // ── 渲染：闪身点预览 ──
    // 按住窥视键时绘制左/右候选闪身点（青）与最终选中闪身点（绿 + 站位框），
    // 让你清楚会瞬移/偷射到哪个位置，而不是只能盲猜。

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        if (!isEnabled()) return;
        if (ghostPeekLeftPos == null && ghostPeekRightPos == null
                && ghostPeekUpPos == null && ghostPeekDownPos == null && ghostPeekOriginPos == null) return;
        // ★ 用 try/finally 兜底：任何异常都恢复 RenderSystem 状态并 pop 掉 pose，
        //   否则 PoseStack 失衡会导致后续整帧世界渲染（含本圈）崩溃、再也画不出来（切人称后尤甚）。
        PoseStack poseStack = event.getPoseStack();
        Vec3 cam = event.getCamera().getPosition();
        poseStack.pushPose();
        try {
            poseStack.translate(-cam.x, -cam.y, -cam.z);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.getBuilder();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            RenderSystem.disableCull();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            int segments = 32;
            // 候选闪身点：左右=青，上下(垂直)=黄，原点(玩家原地)=品红
            if (ghostPeekLeftPos != null) drawGroundCircle(buffer, poseStack, ghostPeekLeftPos, 0.5, segments, 0.0f, 1.0f, 1.0f);
            if (ghostPeekRightPos != null) drawGroundCircle(buffer, poseStack, ghostPeekRightPos, 0.5, segments, 0.0f, 1.0f, 1.0f);
            if (ghostPeekUpPos != null) drawGroundCircle(buffer, poseStack, ghostPeekUpPos, 0.5, segments, 1.0f, 1.0f, 0.0f);
            if (ghostPeekDownPos != null) drawGroundCircle(buffer, poseStack, ghostPeekDownPos, 0.5, segments, 1.0f, 1.0f, 0.0f);
            if (ghostPeekOriginPos != null) drawGroundCircle(buffer, poseStack, ghostPeekOriginPos, 0.5, segments, 1.0f, 0.0f, 1.0f);
            if (ghostPeekSelectedPos != null) {
                drawGroundCircle(buffer, poseStack, ghostPeekSelectedPos, 0.6, segments, 0.0f, 1.0f, 0.0f);
                drawStandBox(buffer, poseStack, ghostPeekSelectedPos);
            }
        } finally {
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            poseStack.popPose();
        }
    }

    private static void drawGroundCircle(BufferBuilder buffer, PoseStack poseStack, Vec3 pos, double radius, int segments, float r, float g, float b) {
        var matrix = poseStack.last().pose();
        double y = pos.y + 0.01;
        // 填充
        buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(matrix, (float) pos.x, (float) y, (float) pos.z).color(r, g, b, 0.4f).endVertex();
        for (int i = 0; i <= segments; i++) {
            double a = 6.283185307179586 * i / (double) segments;
            buffer.vertex(matrix, (float) (pos.x + radius * Math.cos(a)), (float) y, (float) (pos.z + radius * Math.sin(a))).color(r, g, b, 0.4f).endVertex();
        }
        Tesselator.getInstance().end();
        // 描边
        buffer.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            double a = 6.283185307179586 * i / (double) segments;
            buffer.vertex(matrix, (float) (pos.x + radius * Math.cos(a)), (float) y, (float) (pos.z + radius * Math.sin(a))).color(r, g, b, 1.0f).endVertex();
        }
        Tesselator.getInstance().end();
    }

    private static void drawStandBox(BufferBuilder buffer, PoseStack poseStack, Vec3 pos) {
        var matrix = poseStack.last().pose();
        double x0 = pos.x - 0.3, x1 = pos.x + 0.3;
        double z0 = pos.z - 0.3, z1 = pos.z + 0.3;
        double y0 = pos.y, y1 = pos.y + 1.8;
        buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
        line(buffer, matrix, x0, y0, z0, x1, y0, z0);
        line(buffer, matrix, x1, y0, z0, x1, y0, z1);
        line(buffer, matrix, x1, y0, z1, x0, y0, z1);
        line(buffer, matrix, x0, y0, z1, x0, y0, z0);
        line(buffer, matrix, x0, y1, z0, x1, y1, z0);
        line(buffer, matrix, x1, y1, z0, x1, y1, z1);
        line(buffer, matrix, x1, y1, z1, x0, y1, z1);
        line(buffer, matrix, x0, y1, z1, x0, y1, z0);
        line(buffer, matrix, x0, y0, z0, x0, y1, z0);
        line(buffer, matrix, x1, y0, z0, x1, y1, z0);
        line(buffer, matrix, x1, y0, z1, x1, y1, z1);
        line(buffer, matrix, x0, y0, z1, x0, y1, z1);
        Tesselator.getInstance().end();
    }

    private static void line(BufferBuilder buffer, org.joml.Matrix4f matrix, double x1, double y1, double z1, double x2, double y2, double z2) {
        buffer.vertex(matrix, (float) x1, (float) y1, (float) z1).color(0.0f, 1.0f, 0.0f, 1.0f).endVertex();
        buffer.vertex(matrix, (float) x2, (float) y2, (float) z2).color(0.0f, 1.0f, 0.0f, 1.0f).endVertex();
    }
}
