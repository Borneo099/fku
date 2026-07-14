package fku.org.example.fku.features.autodrop; /* water */

import fku.org.example.fku.config.FkuConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;
import java.util.List;

@Mod.EventBusSubscriber(modid = "fku", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AutoDropPanel {
    private static final int PANEL_WIDTH = 112;  // 增加宽度以容纳6个物品
    private static final int PANEL_HEIGHT = 188;
    private static final int ICON_SIZE = 16;
    private static final int ICONS_PER_ROW = 6;
    private static final int ICON_SPACING = 2;
    private static final int SCROLL_SPEED = 8;
    private static final int ADD_SLOT_SIZE = 18;
    private static final int TITLE_BAR_HEIGHT = 20;
    private static final int ITEMS_START_Y = 64;  // 增加间距，避免与加号槽位重叠
    private static int scrollOffset = 0;
    
    // 拖拽状态
    private static boolean dragging = false;
    private static int dragOffsetX = 0;
    private static int dragOffsetY = 0;

    private static boolean isInventoryScreen(Screen screen) {
        return screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen;
    }

    private static int getPanelX(Minecraft mc) {
        int configX = FkuConfig.autoDropPanelXPos.get();
        if (configX == 0) {
            // 默认位置：背包右侧
            return mc.getWindow().getGuiScaledWidth() / 2 + 90;
        }
        return configX;
    }

    private static int getPanelY(Minecraft mc) {
        int configY = FkuConfig.autoDropPanelYPos.get();
        if (configY == 0) {
            // 默认位置：背包顶部对齐
            return mc.getWindow().getGuiScaledHeight() / 2 - 94;
        }
        return configY;
    }

    private static void savePosition(int x, int y) {
        FkuConfig.autoDropPanelXPos.set(x);
        FkuConfig.autoDropPanelYPos.set(y);
    }

    @SubscribeEvent
    public static void onRender(ScreenEvent.Render.Post event) {
        AutoDropConfig config = AutoDropConfig.getInstance();
        if (!config.enabled) return;

        if (!isInventoryScreen(event.getScreen())) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        Minecraft mc = Minecraft.getInstance();

        int panelX = getPanelX(mc);
        int panelY = getPanelY(mc);

        drawPanel(guiGraphics, panelX, panelY);
        drawAddSlot(guiGraphics, panelX + 4, panelY + TITLE_BAR_HEIGHT + 24);
        drawItems(guiGraphics, panelX + 4, panelY + ITEMS_START_Y);
    }

    private static void drawPanel(GuiGraphics guiGraphics, int x, int y) {
        Color bgColor = new Color(30, 30, 30, 128);
        Color borderColor = new Color(60, 60, 60, 200);
        Color titleBarColor = new Color(0, 102, 204, 200);

        // 标题栏
        guiGraphics.fill(x, y, x + PANEL_WIDTH, y + TITLE_BAR_HEIGHT, titleBarColor.getRGB());
        guiGraphics.drawString(Minecraft.getInstance().font, "黑名单", x + 5, y + 6, 0xFFFFFF);
        
        // 主体
        guiGraphics.fill(x, y + TITLE_BAR_HEIGHT, x + PANEL_WIDTH, y + PANEL_HEIGHT, bgColor.getRGB());
        guiGraphics.renderOutline(x, y, PANEL_WIDTH, PANEL_HEIGHT, borderColor.getRGB());
        
        // 提示
        String hint1 = "拖动物品长按添加黑名单";
        String hint2 = "右键物品取消黑名单";
        guiGraphics.drawString(Minecraft.getInstance().font, hint1, x + 5, y + TITLE_BAR_HEIGHT + 4, 0x888888);
        guiGraphics.drawString(Minecraft.getInstance().font, hint2, x + 5, y + TITLE_BAR_HEIGHT + 14, 0x888888);
    }

    private static void drawAddSlot(GuiGraphics guiGraphics, int x, int y) {
        Color slotColor = new Color(60, 120, 60, 180);
        Color borderColor = new Color(100, 200, 100, 255);
        
        guiGraphics.fill(x, y, x + ADD_SLOT_SIZE, y + ADD_SLOT_SIZE, slotColor.getRGB());
        guiGraphics.renderOutline(x, y, ADD_SLOT_SIZE, ADD_SLOT_SIZE, borderColor.getRGB());
        
        String plus = "+";
        int textX = x + ADD_SLOT_SIZE / 2 - 4;
        int textY = y + ADD_SLOT_SIZE / 2 - 5;
        guiGraphics.drawString(Minecraft.getInstance().font, plus, textX, textY, 0xFFFFFF);
    }

    private static void drawItems(GuiGraphics guiGraphics, int x, int y) {
        AutoDropConfig config = AutoDropConfig.getInstance();
        List<String> blacklist = config.blacklist;

        // 计算可见区域高度（从物品开始位置到面板底部）
        int visibleHeight = PANEL_HEIGHT - ITEMS_START_Y - 4;
        int maxRows = visibleHeight / (ICON_SIZE + ICON_SPACING);
        int maxVisibleItems = maxRows * ICONS_PER_ROW;
        
        if (scrollOffset < 0) scrollOffset = 0;
        int maxOffset = Math.max(0, blacklist.size() - maxVisibleItems);
        if (scrollOffset > maxOffset) scrollOffset = maxOffset;

        // 计算物品区域总宽度，确保居中对齐
        int itemsWidth = ICONS_PER_ROW * ICON_SIZE + (ICONS_PER_ROW - 1) * ICON_SPACING;
        int startX = x + (PANEL_WIDTH - 8 - itemsWidth) / 2;  // 8 = 左右边距各4
        
        int drawnCount = 0;
        for (int i = scrollOffset; i < blacklist.size() && drawnCount < maxVisibleItems; i++) {
            String itemId = blacklist.get(i);
            int row = drawnCount / ICONS_PER_ROW;
            int col = drawnCount % ICONS_PER_ROW;
            
            int itemX = startX + col * (ICON_SIZE + ICON_SPACING);
            int itemY = y + row * (ICON_SIZE + ICON_SPACING);

            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
            ItemStack stack = new ItemStack(item != null ? item : Items.PAPER);

            guiGraphics.fill(itemX, itemY, itemX + ICON_SIZE, itemY + ICON_SIZE, 0x44444444);
            guiGraphics.renderItem(stack, itemX, itemY);

            drawnCount++;
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(ScreenEvent.MouseScrolled.Pre event) {
        AutoDropConfig config = AutoDropConfig.getInstance();
        if (!config.enabled) return;

        if (!isInventoryScreen(event.getScreen())) return;

        Minecraft mc = Minecraft.getInstance();
        int panelX = getPanelX(mc);
        int panelY = getPanelY(mc);

        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();

        if (mouseX >= panelX && mouseX <= panelX + PANEL_WIDTH &&
            mouseY >= panelY && mouseY <= panelY + PANEL_HEIGHT) {
            
            scrollOffset -= (int) (event.getScrollDelta() * SCROLL_SPEED);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
        AutoDropConfig config = AutoDropConfig.getInstance();
        if (!config.enabled) return;

        if (!isInventoryScreen(event.getScreen())) return;

        Minecraft mc = Minecraft.getInstance();
        int panelX = getPanelX(mc);
        int panelY = getPanelY(mc);

        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();

        // 点击标题栏开始拖拽
        if (mouseX >= panelX && mouseX <= panelX + PANEL_WIDTH &&
            mouseY >= panelY && mouseY <= panelY + TITLE_BAR_HEIGHT) {
            if (event.getButton() == 0) {
                dragging = true;
                dragOffsetX = (int) mouseX - panelX;
                dragOffsetY = (int) mouseY - panelY;
                event.setCanceled(true);
                return;
            }
        }

        // 点击黑名单物品区域移除
        if (mouseX >= panelX && mouseX <= panelX + PANEL_WIDTH &&
            mouseY >= panelY + ITEMS_START_Y && mouseY <= panelY + PANEL_HEIGHT) {
            
            if (event.getButton() == 1) {
                // 计算物品区域起始位置（与drawItems保持一致）
                int itemsWidth = ICONS_PER_ROW * ICON_SIZE + (ICONS_PER_ROW - 1) * ICON_SPACING;
                int startX = panelX + 4 + (PANEL_WIDTH - 8 - itemsWidth) / 2;
                
                int relX = (int) (mouseX - startX);
                int relY = (int) (mouseY - panelY - ITEMS_START_Y);

                int col = relX / (ICON_SIZE + ICON_SPACING);
                int row = relY / (ICON_SIZE + ICON_SPACING);
                int index = row * ICONS_PER_ROW + col + scrollOffset;

                if (index >= 0 && index < config.blacklist.size()) {
                    String itemId = config.blacklist.get(index);
                    config.removeFromBlacklist(itemId);
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onMouseDrag(ScreenEvent.MouseDragged.Pre event) {
        AutoDropConfig config = AutoDropConfig.getInstance();
        if (!config.enabled) return;

        if (!isInventoryScreen(event.getScreen())) return;

        Minecraft mc = Minecraft.getInstance();
        int panelX = getPanelX(mc);
        int panelY = getPanelY(mc);

        // 处理面板拖拽
        if (dragging) {
            int newX = (int) event.getMouseX() - dragOffsetX;
            int newY = (int) event.getMouseY() - dragOffsetY;
            
            // 边界限制
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();
            if (newX < 0) newX = 0;
            if (newY < 0) newY = 0;
            if (newX + PANEL_WIDTH > screenWidth) newX = screenWidth - PANEL_WIDTH;
            if (newY + PANEL_HEIGHT > screenHeight) newY = screenHeight - PANEL_HEIGHT;
            
            savePosition(newX, newY);
            event.setCanceled(true);
            return;
        }

        // 处理拖拽物品到添加槽位
        Player player = mc.player;
        if (player == null) return;

        int addSlotX = panelX + 4;
        int addSlotY = panelY + TITLE_BAR_HEIGHT + 24;

        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();

        if (mouseX >= addSlotX && mouseX <= addSlotX + ADD_SLOT_SIZE &&
            mouseY >= addSlotY && mouseY <= addSlotY + ADD_SLOT_SIZE) {
            
            ItemStack carried = mc.player.containerMenu.getCarried();
            if (!carried.isEmpty()) {
                String itemId = AutoDropHandler.getItemId(carried);
                config.addToBlacklist(itemId);
                mc.player.containerMenu.setCarried(ItemStack.EMPTY);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onMouseRelease(ScreenEvent.MouseButtonReleased.Pre event) {
        if (dragging) {
            dragging = false;
        }
    }

    public static void addItemFromInventory(ItemStack stack) {
        AutoDropConfig config = AutoDropConfig.getInstance();
        if (!config.enabled) return;

        String itemId = AutoDropHandler.getItemId(stack);
        config.addToBlacklist(itemId);
    }

    public static void resetScroll() {
        scrollOffset = 0;
    }
}