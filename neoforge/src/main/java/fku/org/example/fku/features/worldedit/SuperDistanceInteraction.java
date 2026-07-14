package fku.org.example.fku.features.worldedit; /* water */

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;

/**
 * 超远距离交互管理
 *
 * 核心机制：
 * - 激活时将带 BLOCK_INTERACTION_RANGE +114514 属性的橡木按钮装备至头盔槽
 * - 停用时恢复原头盔
 * - 拦截点击事件，自定义射线追踪，发送破坏/放置包
 * - 操作后自动恢复原手持物品
 */
public class SuperDistanceInteraction {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final SuperDistanceInteraction INSTANCE = new SuperDistanceInteraction();

    private static final ResourceLocation RANGE_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("fku", "worldedit_super_range");

    private ItemStack originalHelmet = ItemStack.EMPTY;
    private boolean helmetEquipped = false;
    private int originalSelectedSlot = -1;

    public static SuperDistanceInteraction getInstance() { return INSTANCE; }

    private SuperDistanceInteraction() {}

    /**
     * 激活超远距离交互
     * 1. 保存原头盔
     * 2. 创建橡木按钮 + BLOCK_REACH +9999
     * 3. 装备到头盔槽 + 发包同步到服务端
     */
    public void enable() {
        if (mc.player == null) return;

        // 保存原头盔
        originalHelmet = mc.player.getItemBySlot(EquipmentSlot.HEAD).copy();

        // 创建橡木按钮并添加属性修饰符
        ItemStack button = new ItemStack(Items.OAK_BUTTON, 1);
        AttributeModifier modifier = new AttributeModifier(
                RANGE_MODIFIER_ID,
                9999.0, AttributeModifier.Operation.ADD_VALUE);

        // 添加属性到物品
        ItemAttributeModifiers attrModifiers = button.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        button.set(DataComponents.ATTRIBUTE_MODIFIERS, attrModifiers.withModifierAdded(Attributes.BLOCK_INTERACTION_RANGE, modifier, EquipmentSlotGroup.HEAD));

        // 装备到头盔槽（客户端）
        mc.player.setItemSlot(EquipmentSlot.HEAD, button);

        // ★ 发包同步到服务端（创造模式）：
        //   容器槽 5 = 头盔位，同时发送 armor slot 100+ 兼容不同服务端实现
        if (mc.player.getAbilities().instabuild) {
            mc.player.connection.send(new ServerboundSetCreativeModeSlotPacket(5, button));
            mc.player.connection.send(new ServerboundSetCreativeModeSlotPacket(103, button)); // armor slot fallback
        }

        // 确保属性已应用（客户端）
        AttributeInstance attr = mc.player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (attr != null && !attr.hasModifier(RANGE_MODIFIER_ID)) {
            attr.addTransientModifier(modifier);
        }

        helmetEquipped = true;
        Fku.LOGGER.info("[WorldEdit] 超远距离交互已激活 (BLOCK_REACH +9999)");
    }

    /**
     * 停用超远距离交互 — 恢复原头盔 + 发包同步服务端
     */
    public void disable() {
        if (mc.player == null) return;

        // 移除属性修饰符
        AttributeInstance attr = mc.player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (attr != null) {
            attr.removeModifier(RANGE_MODIFIER_ID);
        }

        // 恢复原头盔（客户端）
        mc.player.setItemSlot(EquipmentSlot.HEAD, originalHelmet);

        // ★ 发包同步到服务端
        if (mc.player.getAbilities().instabuild) {
            mc.player.connection.send(new ServerboundSetCreativeModeSlotPacket(5, originalHelmet));
        }

        originalHelmet = ItemStack.EMPTY;
        helmetEquipped = false;

        Fku.LOGGER.info("[WorldEdit] 超远距离交互已停用");
    }

    /**
     * 处理鼠标点击 — 自定义射线追踪
     * @return true 如果点击已被处理（阻止原操作）
     */
    public boolean handleClick(int button, InteractionHand hand) {
        if (mc.player == null || mc.level == null) return false;
        if (!helmetEquipped) return false;

        WorldEditConfig cfg = WorldEditConfig.getInstance();
        if (!cfg.enabled) return false;

        // 保存当前手持物品槽位
        originalSelectedSlot = mc.player.getInventory().getSelectedSlot();

        // 自定义射线追踪
        Vec3 eyePos = mc.player.getEyePosition(1.0f);
        Vec3 lookVec = mc.player.getLookAngle();
        double range = cfg.rangeMultiplier;
        Vec3 endPos = eyePos.add(lookVec.scale(range));

        // 执行射线追踪
        BlockHitResult hitResult = customRayTrace(eyePos, endPos);
        if (hitResult == null || hitResult.getType() == HitResult.Type.MISS) {
            restoreSlot();
            return false;
        }

        BlockPos targetPos = hitResult.getBlockPos();

        if (button == 0) {
            // 左键 — 破坏方块
            mc.player.connection.send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            int seq = getSequence();
            mc.player.connection.send(new ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK,
                    targetPos, hitResult.getDirection(), seq));
            mc.player.connection.send(new ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
                    targetPos, hitResult.getDirection(), seq));
            mc.level.destroyBlock(targetPos, false);
        } else if (button == 1) {
            // 右键 — 放置/交互方块
            mc.player.connection.send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            Vec3 blockCenter = Vec3.atCenterOf(targetPos);
            Vec3 hitVec = blockCenter.add(
                    Vec3.atLowerCornerOf(hitResult.getDirection().getUnitVec3i()).scale(0.5));
            BlockHitResult placeHit = new BlockHitResult(
                    hitVec, hitResult.getDirection(), targetPos, false);
            mc.player.connection.send(new ServerboundUseItemOnPacket(
                    hand != null ? hand : InteractionHand.MAIN_HAND,
                    placeHit, getSequence()));
        }

        // 操作后自动恢复原手持物品
        if (cfg.autoRestoreSlot) {
            restoreSlot();
        }

        return true; // 阻止原操作
    }

    /**
     * 自定义射线追踪 — 支持超远距离
     */
    private BlockHitResult customRayTrace(Vec3 start, Vec3 end) {
        if (mc.level == null) return null;

        // 使用 Minecraft 的 clip 方法
        return mc.level.clip(new net.minecraft.world.level.ClipContext(
                start, end,
                net.minecraft.world.level.ClipContext.Block.OUTLINE,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                mc.player));
    }

    private int getSequence() {
        if (mc.level == null) return 0;
        var handler = ((fku.org.example.fku.mixin.ClientLevelAccessor) mc.level).getBlockStatePredictionHandler_CU();
        handler.startPredicting();
        int num = handler.currentSequence();
        handler.close();
        return num;
    }

    private void restoreSlot() {
        if (originalSelectedSlot >= 0 && mc.player != null) {
            mc.player.connection.send(new ServerboundSetCarriedItemPacket(originalSelectedSlot));
            originalSelectedSlot = -1;
        }
    }

    public boolean isHelmetEquipped() { return helmetEquipped; }
}
