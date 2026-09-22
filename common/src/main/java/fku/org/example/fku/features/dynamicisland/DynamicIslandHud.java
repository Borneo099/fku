package fku.org.example.fku.features.dynamicisland;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(
   modid = "fku",
   bus = Bus.FORGE,
   value = {Dist.CLIENT}
)
public class DynamicIslandHud {
   private static AnimationController controller;
   private static long lastUpdate = 0L;
   private static long lastRenderMs = 0L;
   private static boolean dragging = false;
   private static int dragOffX = 0;
   private static int dragOffY = 0;
   private static long lastDragSave = 0L;

   @SubscribeEvent
   public static void onRenderGui(RenderGuiEvent.Post event) {
      Minecraft mc = Minecraft.getInstance();
      GuiGraphics g = event.getGuiGraphics();
      if (mc.screen == null) {
         if (dragging) {
            updateDragFromMouse();
         }

         handle(g);
      }

   }

   @SubscribeEvent
   public static void onScreenRender(ScreenEvent.Render.Post event) {
      handleTop(event.getGuiGraphics());
   }

   @SubscribeEvent
   public static void onGuiOverlay(RenderGuiOverlayEvent.Pre event) {
      DynamicIslandConfig cfg = DynamicIslandConfig.getInstance();
      if (cfg.tabShowEnabled && cfg.enabled && VanillaGuiOverlay.PLAYER_LIST.equals(event.getOverlay().overlay())) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public static void onMouseButton(InputEvent.MouseButton event) {
      if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
         return;
      }

      Minecraft mc = Minecraft.getInstance();
      if (mc.screen != null) {
         return;
      }

      if (event.getAction() == GLFW.GLFW_PRESS) {
         if (tryStartDrag((int)mc.mouseHandler.xpos(), (int)mc.mouseHandler.ypos())) {
            event.setCanceled(true);
         }
      } else if (event.getAction() == GLFW.GLFW_RELEASE && dragging) {
         dragging = false;
         DynamicIslandConfig.save();
      }
   }

   @SubscribeEvent
   public static void onScreenMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
      DynamicIslandConfig cfg = DynamicIslandConfig.getInstance();
      if (!cfg.dragEnabled) {
         return;
      }

      if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && tryStartDrag((int)event.getMouseX(), (int)event.getMouseY())) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public static void onScreenMouseDragged(ScreenEvent.MouseDragged event) {
      if (dragging) {
         DynamicIslandConfig cfg = DynamicIslandConfig.getInstance();
         Minecraft mc = Minecraft.getInstance();
         // 绝对坐标拖动（不随窗口比例位移）
         cfg.freeX = (int)(event.getMouseX() + (double)dragOffX);
         cfg.freeY = (int)(event.getMouseY() + (double)dragOffY);
         cfg.freeRelX = -1.0F;
         cfg.freeRelY = -1.0F;
         saveDragThrottled();
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public static void onScreenMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
      if (dragging && event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
         dragging = false;
         DynamicIslandConfig.save();
         event.setCanceled(true);
      }
   }

   private static boolean tryStartDrag(int mx, int my) {
      DynamicIslandConfig cfg = DynamicIslandConfig.getInstance();
      if (!cfg.enabled || !cfg.dragEnabled) {
         return false;
      }

      if (controller == null) {
         controller = new AnimationController(cfg);
      }

      Minecraft mc = Minecraft.getInstance();
      int sw = mc.getWindow() != null ? mc.getWindow().getGuiScaledWidth() : 0;
      int[] rect = computeRect(cfg, sw);
      if (rect == null) {
         return false;
      }

      boolean hit = mx >= rect[0] && mx <= rect[0] + rect[2] && my >= rect[1] && my <= rect[1] + rect[3];
      if (hit) {
         dragging = true;
         dragOffX = rect[0] + rect[2] / 2 - mx;
         dragOffY = rect[1] + rect[3] / 2 - my;
         // 绝对坐标拖动（不随窗口比例位移）
         cfg.freeX = (int)(mx + dragOffX);
         cfg.freeY = (int)(my + dragOffY);
         cfg.freeRelX = -1.0F;
         cfg.freeRelY = -1.0F;
         saveDragThrottled();
         return true;
      }

      return false;
   }

   private static void updateDragFromMouse() {
      DynamicIslandConfig cfg = DynamicIslandConfig.getInstance();
      Minecraft mc = Minecraft.getInstance();
      // 绝对坐标拖动（不随窗口比例位移）
      cfg.freeX = (int)(mc.mouseHandler.xpos() + (double)dragOffX);
      cfg.freeY = (int)(mc.mouseHandler.ypos() + (double)dragOffY);
      cfg.freeRelX = -1.0F;
      cfg.freeRelY = -1.0F;
      saveDragThrottled();
   }

   private static void saveDragThrottled() {
      long now = System.currentTimeMillis();
      if (now - lastDragSave > 200L) {
         lastDragSave = now;
         DynamicIslandConfig.save();
      }
   }

   private static int[] computeRect(DynamicIslandConfig cfg, int sw) {
      if (controller == null) {
         return null;
      }

      int w = Math.round(controller.getW());
      int h = Math.round(controller.getH());
      if (w < 4 || h < 4) {
         return null;
      }

      int sh = Minecraft.getInstance().getWindow() != null ? Minecraft.getInstance().getWindow().getGuiScaledHeight() : 0;
      int x;
      int y;
      if (cfg.freeX >= 0 && cfg.freeY >= 0) {
         x = cfg.freeX - w / 2;
         y = cfg.freeY - h / 2;
      } else {
         int cx;
         switch (cfg.islandPosition) {
            case "TOP_LEFT":
               cx = cfg.positionOffsetX + 4;
               break;
            case "TOP_RIGHT":
               cx = sw - w - 4 - cfg.positionOffsetX;
               break;
            default:
               cx = (sw - w) / 2 + cfg.positionOffsetX;
         }

         x = cx;
         y = cfg.positionOffsetY;
      }

      if (x < 2) {
         x = 2;
      }

      if (x + w > sw - 2) {
         x = Math.max(2, sw - 2 - w);
      }

      if (y < 2) {
         y = 2;
      }

      if (y + h > sh - 2) {
         y = Math.max(2, sh - 2 - h);
      }

      return new int[]{x, y, w, h};
   }

   public static void renderNow(GuiGraphics g) {
      handleTop(g);
   }

   private static void handleTop(GuiGraphics g) {
      long now = System.currentTimeMillis();
      if (now != lastRenderMs) {
         handle(g);
      }
   }

   private static void handle(GuiGraphics g) {
      try {
         DynamicIslandConfig cfg = DynamicIslandConfig.getInstance();
         if (!cfg.enabled) {
            return;
         }

         Minecraft mc = Minecraft.getInstance();
         if (mc.getWindow() == null) {
            return;
         }

         lastRenderMs = System.currentTimeMillis();
         if (controller == null) {
            controller = new AnimationController(cfg);
         }

         long now = System.currentTimeMillis();
         if (now - lastUpdate >= 8L) {
            controller.update(cfg);
            lastUpdate = now;
         }

         NotificationRenderer.render(g, controller, cfg);
      } catch (Exception var5) {
      }
   }
}
