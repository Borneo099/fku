package fku.org.example.fku.features.autodrop;

import fku.org.example.fku.config.FkuConfig;
import fku.org.example.fku.features.autodrop.AutoDropConfig;
import fku.org.example.fku.features.autodrop.AutoDropHandler;
import java.awt.Color;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid="fku", bus=Mod.EventBusSubscriber.Bus.FORGE)
public class AutoDropPanel {
    private static final int PANEL_WIDTH = 112;
    private static final int PANEL_HEIGHT = 188;
    private static final int ICON_SIZE = 16;
    private static final int ICONS_PER_ROW = 6;
    private static final int ICON_SPACING = 2;
    private static final int SCROLL_SPEED = 8;
    private static final int ADD_SLOT_SIZE = 18;
    private static final int TITLE_BAR_HEIGHT = 20;
    private static final int ITEMS_START_Y = 64;
    private static int scrollOffset = 0;
    private static boolean dragging = false;
    private static int dragOffsetX = 0;
    private static int dragOffsetY = 0;

    private static boolean isInventoryScreen(Screen screen) {
        return screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen;
    }

    private static int getPanelX(Minecraft mc) {
        int configX = (Integer)FkuConfig.autoDropPanelXPos.get();
        if (configX == 0) {
            return mc.getWindow().m_85445_() / 2 + 90;
        }
        return configX;
    }

    private static int getPanelY(Minecraft mc) {
        int configY = (Integer)FkuConfig.autoDropPanelYPos.get();
        if (configY == 0) {
            return mc.getWindow().m_85446_() / 2 - 94;
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
        if (!config.enabled) {
            return;
        }
        if (!AutoDropPanel.isInventoryScreen(event.getScreen())) {
            return;
        }
        GuiGraphics guiGraphics = event.getGuiGraphics();
        Minecraft mc = Minecraft.getInstance();
        int panelX = AutoDropPanel.getPanelX(mc);
        int panelY = AutoDropPanel.getPanelY(mc);
        AutoDropPanel.drawPanel(guiGraphics, panelX, panelY);
        AutoDropPanel.drawAddSlot(guiGraphics, panelX + 4, panelY + 20 + 24);
        AutoDropPanel.drawItems(guiGraphics, panelX + 4, panelY + 64);
    }

    private static void drawPanel(GuiGraphics guiGraphics, int x, int y) {
        Color bgColor = new Color(30, 30, 30, 128);
        Color borderColor = new Color(60, 60, 60, 200);
        Color titleBarColor = new Color(0, 102, 204, 200);
        guiGraphics.m_280509_(x, y, x + 112, y + 20, titleBarColor.getRGB());
        guiGraphics.drawString(Minecraft.getInstance().font, "\u9ed1\u540d\u5355", x + 5, y + 6, 0xFFFFFF);
        guiGraphics.m_280509_(x, y + 20, x + 112, y + 188, bgColor.getRGB());
        guiGraphics.m_280637_(x, y, 112, 188, borderColor.getRGB());
        String hint1 = "\u62d6\u52a8\u7269\u54c1\u957f\u6309\u6dfb\u52a0\u9ed1\u540d\u5355";
        String hint2 = "\u53f3\u952e\u7269\u54c1\u53d6\u6d88\u9ed1\u540d\u5355";
        guiGraphics.drawString(Minecraft.getInstance().font, hint1, x + 5, y + 20 + 4, 0x888888);
        guiGraphics.drawString(Minecraft.getInstance().font, hint2, x + 5, y + 20 + 14, 0x888888);
    }

    private static void drawAddSlot(GuiGraphics guiGraphics, int x, int y) {
        Color slotColor = new Color(60, 120, 60, 180);
        Color borderColor = new Color(100, 200, 100, 255);
        guiGraphics.m_280509_(x, y, x + 18, y + 18, slotColor.getRGB());
        guiGraphics.m_280637_(x, y, 18, 18, borderColor.getRGB());
        String plus = "+";
        int textX = x + 9 - 4;
        int textY = y + 9 - 5;
        guiGraphics.drawString(Minecraft.getInstance().font, plus, textX, textY, 0xFFFFFF);
    }

    private static void drawItems(GuiGraphics guiGraphics, int x, int y) {
        int maxOffset;
        AutoDropConfig config = AutoDropConfig.getInstance();
        List<String> blacklist = config.blacklist;
        int visibleHeight = 120;
        int maxRows = visibleHeight / 18;
        int maxVisibleItems = maxRows * 6;
        if (scrollOffset < 0) {
            scrollOffset = 0;
        }
        if (scrollOffset > (maxOffset = Math.max(0, blacklist.size() - maxVisibleItems))) {
            scrollOffset = maxOffset;
        }
        int itemsWidth = 106;
        int startX = x + (104 - itemsWidth) / 2;
        int drawnCount = 0;
        for (int i = scrollOffset; i < blacklist.size() && drawnCount < maxVisibleItems; ++drawnCount, ++i) {
            String itemId = blacklist.get(i);
            int row = drawnCount / 6;
            int col = drawnCount % 6;
            int itemX = startX + col * 18;
            int itemY = y + row * 18;
            Item item = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
            ItemStack stack = new ItemStack((ItemLike)(item != null ? item : Items.f_42516_));
            guiGraphics.m_280509_(itemX, itemY, itemX + 16, itemY + 16, 0x44444444);
            guiGraphics.m_280480_(stack, itemX, itemY);
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(ScreenEvent.MouseScrolled.Pre event) {
        AutoDropConfig config = AutoDropConfig.getInstance();
        if (!config.enabled) {
            return;
        }
        if (!AutoDropPanel.isInventoryScreen(event.getScreen())) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        int panelX = AutoDropPanel.getPanelX(mc);
        int panelY = AutoDropPanel.getPanelY(mc);
        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();
        if (mouseX >= panelX && mouseX <= (panelX + 112) && mouseY >= panelY && mouseY <= (panelY + 188)) {
            scrollOffset -= (event.getScrollDelta() * 8.0);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
        int itemsWidth;
        int startX;
        int relX;
        int col;
        int relY;
        int row;
        int index;
        AutoDropConfig config = AutoDropConfig.getInstance();
        if (!config.enabled) {
            return;
        }
        if (!AutoDropPanel.isInventoryScreen(event.getScreen())) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        int panelX = AutoDropPanel.getPanelX(mc);
        int panelY = AutoDropPanel.getPanelY(mc);
        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();
        if (mouseX >= panelX && mouseX <= (panelX + 112) && mouseY >= panelY && mouseY <= (panelY + 20) && event.getButton() == 0) {
            dragging = true;
            dragOffsetX = mouseX - panelX;
            dragOffsetY = mouseY - panelY;
            event.setCanceled(true);
            return;
        }
        if (mouseX >= panelX && mouseX <= (panelX + 112) && mouseY >= (panelY + 64) && mouseY <= (panelY + 188) && event.getButton() == 1 && (index = (row = (relY = (mouseY - panelY - 64.0)) / 18) * 6 + (col = (relX = (mouseX - (startX = panelX + 4 + (104 - (itemsWidth = 106)) / 2))) / 18) + scrollOffset) >= 0 && index < config.blacklist.size()) {
            String itemId = config.blacklist.get(index);
            config.removeFromBlacklist(itemId);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseDrag(ScreenEvent.MouseDragged.Pre event) {
        ItemStack carried;
        AutoDropConfig config = AutoDropConfig.getInstance();
        if (!config.enabled) {
            return;
        }
        if (!AutoDropPanel.isInventoryScreen(event.getScreen())) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        int panelX = AutoDropPanel.getPanelX(mc);
        int panelY = AutoDropPanel.getPanelY(mc);
        if (dragging) {
            int newX = event.getMouseX() - dragOffsetX;
            int newY = event.getMouseY() - dragOffsetY;
            int screenWidth = mc.getWindow().m_85445_();
            int screenHeight = mc.getWindow().m_85446_();
            if (newX < 0) {
                newX = 0;
            }
            if (newY < 0) {
                newY = 0;
            }
            if (newX + 112 > screenWidth) {
                newX = screenWidth - 112;
            }
            if (newY + 188 > screenHeight) {
                newY = screenHeight - 188;
            }
            AutoDropPanel.savePosition(newX, newY);
            event.setCanceled(true);
            return;
        }
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        int addSlotX = panelX + 4;
        int addSlotY = panelY + 20 + 24;
        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();
        if (mouseX >= addSlotX && mouseX <= (addSlotX + 18) && mouseY >= addSlotY && mouseY <= (addSlotY + 18) && !(carried = mc.player.f_36096_.m_142621_()).m_41619_()) {
            String itemId = AutoDropHandler.getItemId(carried);
            config.addToBlacklist(itemId);
            mc.player.f_36096_.m_142503_(ItemStack.f_41583_);
            event.setCanceled(true);
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
        if (!config.enabled) {
            return;
        }
        String itemId = AutoDropHandler.getItemId(stack);
        config.addToBlacklist(itemId);
    }

    public static void resetScroll() {
        scrollOffset = 0;
    }
}

