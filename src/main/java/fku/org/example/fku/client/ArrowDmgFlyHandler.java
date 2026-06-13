package fku.org.example.fku.client;

import fku.org.example.fku.config.MovementConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "fku", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ArrowDmgFlyHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        MovementConfig config = MovementConfig.getInstance();
        if (!config.arrowDmgFlyEnabled) {
            // 如果功能关闭且玩家还在飞（且不是创造/旁观模式），则停止飞行
            if (!mc.player.isCreative() && !mc.player.isSpectator()) {
                if (mc.player.getAbilities().mayfly || mc.player.getAbilities().flying) {
                    mc.player.getAbilities().mayfly = false;
                    mc.player.getAbilities().flying = false;
                    mc.player.onUpdateAbilities();
                }
            }
            return;
        }

        ItemStack mainHand = mc.player.getMainHandItem();
        ItemStack offHand = mc.player.getOffhandItem();
        
        // 检测是否正在蓄力弓或弩
        boolean isChargingBow = false;
        if (mc.player.isUsingItem()) {
            ItemStack usingItem = mc.player.getUseItem();
            if (usingItem.getItem() instanceof BowItem || usingItem.getItem() instanceof CrossbowItem) {
                // 一蓄力就开启飞行
                isChargingBow = true;
            }
        }

        if (isChargingBow) {
            // 蓄力弓时开启飞行
            if (!mc.player.getAbilities().mayfly || !mc.player.getAbilities().flying) {
                mc.player.getAbilities().mayfly = true;
                mc.player.getAbilities().flying = true;
                mc.player.onUpdateAbilities();
            }
        }
    }
}
