package fku.org.example.fku.features.healthtag; /* water */

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "fku", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class HealthTagEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            HealthTagManager.tick();
        }
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (event.getEntity().level().isClientSide) {
            HealthTagManager.onAttack(event.getTarget());
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (HealthTagManager.isEditing()) return;
        
        Minecraft mc = Minecraft.getInstance();
        double mouseX = mc.mouseHandler.xpos() * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth();
        double mouseY = mc.mouseHandler.ypos() * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight();
        
        HealthTagRenderer.render(event.getGuiGraphics(), (int) mouseX, (int) mouseY, event.getPartialTick());
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (HealthTagManager.isEditing()) {
            HealthTagRenderer.render(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getPartialTick());
        }
    }

    @SubscribeEvent
    public static void onMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
        if (HealthTagRenderer.onMouseClicked(event.getMouseX(), event.getMouseY(), event.getButton())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseDrag(ScreenEvent.MouseDragged.Pre event) {
        HealthTagRenderer.onMouseDragged(event.getMouseX(), event.getMouseY(), event.getMouseButton());
    }

    @SubscribeEvent
    public static void onMouseRelease(ScreenEvent.MouseButtonReleased.Pre event) {
        HealthTagRenderer.onMouseReleased(event.getMouseX(), event.getMouseY(), event.getButton());
    }
}