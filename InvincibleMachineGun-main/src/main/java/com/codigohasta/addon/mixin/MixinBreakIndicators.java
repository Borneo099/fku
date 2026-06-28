package com.codigohasta.addon.mixin;

import meteordevelopment.meteorclient.systems.modules.render.BreakIndicators;
import meteordevelopment.meteorclient.systems.modules.world.PacketMine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collections;
import java.util.List;

@Mixin(value = BreakIndicators.class, remap = false)
public class MixinBreakIndicators {

    /**
     * 拦截 BreakIndicators 去读取 PacketMine 的 blocks 字段的动作。
     * 当原版 PacketMine 因为任何原因丢失变成 null 时，这招能救命。
     */
    @Redirect(
        method = "onRender",
        at = @At(
            value = "FIELD",
            target = "Lmeteordevelopment/meteorclient/systems/modules/world/PacketMine;blocks:Ljava/util/List;"
        )
    )
    private List<?> redirectGetBlocks(PacketMine instance) {
        // 如果原版模块丢失（instance 为 null）
        if (instance == null) {
            // 返回一个空列表骗过它，这样 isEmpty() 就会为真，它直接跳过渲染，完美防止空指针崩溃！
            return Collections.emptyList();
        }
        // 如果模块还在，就正常返回原版数据
        return instance.blocks;
    }
}