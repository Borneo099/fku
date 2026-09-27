package fku.org.example.fku.features.spear; /* water */

import fku.org.example.fku.Fku;
import fku.org.example.fku.features.flight.FlightFeature;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 矛之冲锋（重写：配置项完全参考 meteoruth SpearDMG，仅核心 + 模式选择）
 *
 * 发包模式(packet)：完全照搬 meteoruth SpearDMG —— 蓄力 14 tick 后，朝你准星水平方向
 *   （horizontalLook，无需索敌）注入移动包偏移（位置来回弹：0→step*2→0），并带墙体检测。
 *   矛的伤害/冲刺判定取决于服务端读到的位置速度，因此最稳。
 *
 * 原版模式(vanilla)：直接改玩家本地水平速度让玩家真实突进（矛读到的速度 = 每tick位移 × 20）。
 *   方向用“准星小角度锥”索敌：只锁玩家视角正前方很小范围内（默认 8°）的最近敌人，
 *   避免乱窜；无目标则不突进。同时锁头正对目标。
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, value = Dist.CLIENT)
public class SpearChargeFeature {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final int CHARGE_START_TICKS = 10; // 蓄力 tick（原 14，缩短手感更跟手）
    private static final double MAX_ANGLE_DEG = 8.0;  // 原版模式：准星小锥（只锁正对的小范围敌人）
    private static final double LOCK_MAX_DIST = 40.0; // 原版模式：维持锁定的最大距离

    private static int heldTicks = 0;
    private static double travel = 0.0;
    private static int sign = 1;
    private static Vec3 lastDir = null;
    private static Vec3 boostOffset = null;
    private static Entity lastSmallTarget = null;
    /** 是否由本功能强制按下了空格：用于只在“真正需要”时按下、仅在“之前按过且现在不需要”时松开，
     *  其余情况完全不碰 keyJump，避免覆盖玩家真实的空格按键（平地正常跳跃失效的根因）。 */
    private static boolean forcedJump = false;

    public static boolean isEnabled() { return SpearChargeConfig.getInstance().enabled; }

    /** 供 LivingEntityTravelMixin 判断：原版模式且正在合法蓄力冲锋中（需在 travel 前强制离地） */
    public static boolean isVanillaCharging() {
        SpearChargeConfig c = SpearChargeConfig.getInstance();
        return c.enabled && "vanilla".equals(c.mode) && isCharging() && heldTicks >= CHARGE_START_TICKS;
    }

    public static void setEnabled(boolean v) {
        SpearChargeConfig c = SpearChargeConfig.getInstance();
        c.enabled = v;
        SpearChargeConfig.save();
        if (mc.player != null) {
            mc.player.displayClientMessage(
                Component.literal("§6[矛之冲锋] §" + (v ? "a已启用" : "c已禁用")), false);
        }
        if (!v) hardReset();
    }

    /** 供 MixinConnectionAttackInterceptor 注入移动包偏移（仅发包模式返回非 null） */
    public static Vec3 getBoostOffset() { return boostOffset; }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (mc.player == null || mc.level == null) { hardReset(); return; }
        SpearChargeConfig cfg = SpearChargeConfig.getInstance();
        // 空格统一管理：仅“原版模式 + 蓄力已满/冲刺中 + 非飞行”才按住。
        // 关键：绝不每 tick 强制 setDown(false) —— 那会覆盖玩家真实的空格按键，
        // 导致平地正常跳跃也失效。只在“需要强制按”时按下，并在“之前强制过、现在不需要”时松开；
        // 其余情况完全不碰 keyJump，让玩家自身按键畅通（见 forcedJump 标志）。
        boolean flight = FlightFeature.isFlightActive() || mc.player.getAbilities().flying || mc.player.isFallFlying();
        boolean wantJump = cfg.enabled && "vanilla".equals(cfg.mode) && isCharging() && heldTicks >= CHARGE_START_TICKS && !flight;
        if (wantJump) {
            mc.options.keyJump.setDown(true);
            forcedJump = true;
        } else if (forcedJump) {
            // 之前强制按过、现在不需要：仅这一次释放，之后不再碰 keyJump（保留玩家真实按键状态）
            mc.options.keyJump.setDown(false);
            forcedJump = false;
        }
        if (!cfg.enabled) {
            // 功能关闭：仅释放空格并静默清状态，不发包、不移动
            travel = 0.0; sign = 1; heldTicks = 0; lastDir = null; lastSmallTarget = null; boostOffset = null;
            return;
        }
        if (!isCharging()) { hardReset(); return; }

        boostOffset = null;
        heldTicks++;
        boolean packet = "packet".equals(cfg.mode);
        // 原版/发包模式统一蓄力 10 tick（等长矛举起再冲，否则戳击无伤害）
        if (heldTicks < CHARGE_START_TICKS) return;

        double step = cfg.boostDistance;
        double maxTravel = step * 2.0; // 固定摆动范围，与 packets 无关（修“距离×发包数”bug）
        if (packet) {
            // ── 发包模式（忠于 meteoruth SpearDMG）：每 tick 固定推进 step，packets 仅增加发包密度 ──
            Vec3 dir = horizontalLook();
            if (dir != null) {
                lastDir = dir;
                double next = travel + step * sign; // 每 tick 前进 step（不随 packets 放大）
                if (next >= maxTravel) { next = maxTravel; sign = -1; }
                else if (next <= 0.0) { next = 0.0; sign = 1; }

                if (!blocked(dir, next)) {
                    travel = next;
                    boostOffset = dir.scale(travel); // 叠加到本 tick 正常移动包（mixin）
                    // packets：每 tick 额外发 packets 个分段位置包，让服务器更平滑认可、攻击更频繁
                    // 总位移恒为 travel（≤ step*2），不会随 packets 放大
                    int n = Math.max(1, cfg.packets);
                    double bx = mc.player.getX(), by = mc.player.getY(), bz = mc.player.getZ();
                    for (int i = 1; i <= n; i++) {
                        double off = travel * i / n;
                        sendTagged(bx + dir.x * off, by + dir.y * off, bz + dir.z * off);
                    }
                } else {
                    boostOffset = null;
                }
            } else {
                boostOffset = null;
            }
        } else {
            // ── 原版模式：锁定并三维跟随目标（含上下，可高打底/底打高），真实本地移动 ──
            Vec3 dirH = aimDirSmall(); // 维护锁敌 + 锁头方向（水平）
            if (dirH != null && lastSmallTarget != null && lastSmallTarget.isAlive()) {
                lastDir = dirH;
                faceTarget(lastSmallTarget); // 锁头正对目标
                Vec3 eye = mc.player.getEyePosition();
                Vec3 to = lastSmallTarget.getBoundingBox().getCenter().subtract(eye);
                double dist3 = to.length();
                if (dist3 > 1e-4) {
                    Vec3 dir3 = to.scale(1.0 / dist3); // 三维单位方向（含上下）
                    // 撞墙才停，否则持续冲锋（疯狂冲锋，不留近身阈值）。
                    // 速度用 setDeltaMovement 设下；LocalPlayer 在地面时 travel() 会用输入加速度覆盖水平
                    // 分量，因此由 LivingEntityTravelMixin 在 travel() 头部强制 onGround=false，让 travel
                    // 走空中分支使用本速度（站地/蹲/飞/跳均可冲）。
                    if (!blocked3d(dir3, cfg.vanillaSpeed)) {
                        double speed = cfg.vanillaSpeed;
                        // 空格由 onClientTick 顶部统一管理（仅原版+蓄力满/冲刺+非飞行才按住），此处不再碰
                        if (flight) {
                            // 飞行中：矛冲锋接管移动，直接朝目标三维冲锋（覆盖 FlightFeature 的自由飞行方向，
                            // 因本 onClientTick 为 LOWEST 最后执行）。空格不碰，避免干扰飞行
                            mc.player.setDeltaMovement(dir3.x * speed, dir3.y * speed, dir3.z * speed);
                        } else {
                            // 地面/空中（非飞行）：空格已在顶部按住，这里只给速度 + 蓄力满起跳离地
                            mc.player.setDeltaMovement(dir3.x * speed, dir3.y * speed, dir3.z * speed);
                            // 蓄力刚满那一 tick 直接给一个起跳速度并强制离地，立即进入空中冲锋
                            // （不依赖 travel 内的 jumping&&onGround 判断，避免还要手动按空格）
                            if (heldTicks == CHARGE_START_TICKS) {
                                mc.player.setDeltaMovement(dir3.x * speed, 0.42D, dir3.z * speed);
                                mc.player.setOnGround(false);
                            }
                        }
                    }
                }
            }
        }
    }

    /** 朝你准星水平方向（meteoruth horizontalLook） */
    private static Vec3 horizontalLook() {
        Vec3 look = mc.player.getLookAngle();
        double len = Math.hypot(look.x, look.z);
        return len < 1e-4 ? null : new Vec3(look.x / len, 0.0, look.z / len);
    }

    /** 原版模式索敌：一旦锁定即持续跟随（脱离准星也跟，直到死亡/过远）；
     *  丢失时只在准星小锥（MAX_ANGLE_DEG）内重选最近活体，避免乱窜。无目标返回 null。
     *  注意：返回的是水平方向，仅用于“是否锁定/锁头”；实际冲锋速度用三维方向（见 onClientTick）。 */
    private static Vec3 aimDirSmall() {
        if (mc.level == null || mc.player == null) return null;
        Vec3 eye = mc.player.getEyePosition();
        // 维持已有锁定：只要目标存活且在合理范围内就继续跟随（正上方目标也返回非 null）
        if (lastSmallTarget != null && lastSmallTarget.isAlive()
                && mc.player.distanceTo(lastSmallTarget) < LOCK_MAX_DIST) {
            Vec3 d = lastSmallTarget.getBoundingBox().getCenter().subtract(eye);
            double h = Math.hypot(d.x, d.z);
            return new Vec3(d.x / (h > 1e-3 ? h : 1.0), 0.0, d.z / (h > 1e-3 ? h : 1.0));
        }
        // 初次/丢失：只在准星小锥内选最近活体
        Vec3 look = mc.player.getLookAngle();
        double maxAngle = Math.toRadians(MAX_ANGLE_DEG);
        Entity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof LivingEntity) || !e.isAlive() || e == mc.player) continue;
            if (e instanceof Player p && (p.isCreative() || p.isSpectator())) continue;
            Vec3 to = e.getBoundingBox().getCenter().subtract(eye);
            double len = to.length();
            if (len < 1e-3) continue;
            double ang = Math.acos(Math.max(-1.0, Math.min(1.0, look.dot(to.normalize()))));
            if (ang > maxAngle) continue; // 不在准星小范围，跳过 —— 防止乱窜
            if (len < bestDist) { bestDist = len; best = e; }
        }
        lastSmallTarget = best;
        if (best == null) return null;
        Vec3 d = best.getBoundingBox().getCenter().subtract(eye);
        double h = Math.hypot(d.x, d.z);
        return new Vec3(d.x / (h > 1e-3 ? h : 1.0), 0.0, d.z / (h > 1e-3 ? h : 1.0));
    }

    /** 锁头（视觉 + 服务端判定）：正对目标中心 */
    private static void faceTarget(Entity t) {
        Vec3 c = t.getBoundingBox().getCenter();
        Vec3 eye = mc.player.getEyePosition();
        Vec3 d = c.subtract(eye);
        double len = d.length();
        if (len > 1e-4) {
            float yaw = (float) Math.toDegrees(Math.atan2(d.z, d.x)) - 90f;
            float pitch = (float) -Math.toDegrees(Math.asin(d.y / len));
            mc.player.setYRot(yaw);
            mc.player.setXRot(pitch);
            mc.player.setYHeadRot(yaw);
        }
    }

    private static boolean blocked(Vec3 dir, double dist) {
        Vec3 from = mc.player.position();
        Vec3 to = from.add(dir.x * dist, 0.0, dir.z * dist);
        BlockHitResult hit = mc.level.clip(new ClipContext(from, to, Block.COLLIDER, Fluid.NONE, mc.player));
        return hit.getType() != HitResult.Type.MISS;
    }

    /** 三维墙检（原版模式含上下方向，用真实冲锋方向做线段检测，避免穿墙/被天花板地面卡住） */
    private static boolean blocked3d(Vec3 dir, double dist) {
        Vec3 from = mc.player.position();
        Vec3 to = from.add(dir.x * dist, dir.y * dist, dir.z * dist);
        BlockHitResult hit = mc.level.clip(new ClipContext(from, to, Block.COLLIDER, Fluid.NONE, mc.player));
        return hit.getType() != HitResult.Type.MISS;
    }

    private static boolean isCharging() {
        ItemStack stack = mc.player.getMainHandItem();
        if (stack.isEmpty() || !isSpearLike(stack)) return false;
        return mc.player.isUsingItem() || mc.options.keyUse.isDown();
    }

    /** 矛类物品集合：参考 meteoruth SpearDMG —— 仅在游戏内已注册的物品里，按注册表路径是否含 "spear"
     *  预先收集 Item 实例，运行时直接用 getItem() 比对（不再按显示名/含“矛”字匹配，避免模组武器误判）。
     *  三叉戟(trident)路径不含 spear，天然不会误触发。 */
    private static Set<Item> spearItems;

    private static Set<Item> getSpearItems() {
        if (spearItems == null) {
            spearItems = new HashSet<>();
            for (Item item : ForgeRegistries.ITEMS) {
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
                if (id != null && id.getPath().toLowerCase().contains("spear")) {
                    spearItems.add(item);
                }
            }
        }
        return spearItems;
    }

    /** 识别矛类物品：直接比对已注册的 Item 实例（参考 meteoruth），而非名称字符串。 */
    private static boolean isSpearLike(ItemStack stack) {
        return !stack.isEmpty() && getSpearItems().contains(stack.getItem());
    }

    /** 发一个“真实位置”移动包（meteoruth sendTagged：临时清空 boostOffset 避免被 mixin 再次叠加） */
    private static void sendTagged(double x, double y, double z) {
        Vec3 saved = boostOffset;
        boostOffset = null;
        try {
            mc.getConnection().send(new ServerboundMovePlayerPacket.PosRot(
                x, y, z, mc.player.getYRot(), mc.player.getXRot(), mc.player.onGround()));
        } finally {
            boostOffset = saved;
        }
    }

    private static void hardReset() {
        if (travel > 0.0 && lastDir != null && mc.player != null && mc.getConnection() != null) {
            sendTagged(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        }
        travel = 0.0;
        sign = 1;
        heldTicks = 0;
        lastDir = null;
        lastSmallTarget = null;
        boostOffset = null;
        if (forcedJump) {
            // 仅当之前强制按过空格时才释放，避免每次硬重置都覆盖玩家真实空格按键
            if (mc.player != null) mc.options.keyJump.setDown(false);
            forcedJump = false;
        }
    }
}
