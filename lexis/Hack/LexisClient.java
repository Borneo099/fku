package lexis.Hack;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import lexis.Hack.Hacks.Blocks.AirPlaceHack;
import lexis.Hack.Hacks.Lexis.BindsDisplayHack;
import lexis.Hack.Hacks.Lexis.GuiKeyBindHack;
import lexis.Hack.Hacks.Lexis.TabGuiHack;
import lexis.Hack.Hacks.Render.CameraDistanceHack;
import lexis.Hack.Hackutil.HackManager;
import lexis.Hack.Hackutil.NotificationManager;
import lexis.Hack.Hackutil.HUD.BindsDisplayWidget;
import lexis.Hack.Hackutil.HUD.HUDRenderer;
import lexis.Hack.Hackutil.HUD.MoveBindsDisplayScreen;
import lexis.Hack.Hackutil.HUD.TabGui;
import lexis.Hack.Hackutil.config.KeyBindConfig;
import lexis.Hack.Utils.Colors.ColorSettingScreen;
import lexis.Hack.Utils.HeadOnlyLookUtils.HeadOnlyLook;
import lexis.Hack.Utils.ThemeColors.ThemeManager;
import lexis.Hack.Utils.window.TitleBarAnimator;
import lexis.Hack.Utils.window.WindowColorUtil;
import lexis.Hack.Utils.window.WindowHandle;
import lexis.Hack.events.IGuiRenderable;
import lexis.Hack.events.PacketCancelListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber({Dist.CLIENT})
public class LexisClient {
   public static final String VERSION = "1.6.2";
   private static final Minecraft mc = Minecraft.m_91087_();
   private static HackGui hackGui;
   private static boolean initialized = false;
   private static KeyBindConfig keyBindConfig;
   private static Map keyBinds;
   private static TabGui tabGui;
   private static boolean enabled = true;
   private static Timer announcementTimer;
   private static TimerTask announcementTask;
   private static boolean hasShownOpModWarning;
   private static boolean started;
   private static boolean debugTickFired;
   private static boolean loadFinished;
   private static final Set pressedKeys;

   private static void checkOpModAndCheatUtils() {
      if (!hasShownOpModWarning) {
         ModList modList = ModList.get();
         boolean hasOpMod = modList.isLoaded("opmod");
         boolean hasCheatUtils = modList.isLoaded("cheatutils");
         if (hasOpMod && hasCheatUtils) {
            hasShownOpModWarning = true;
            NotificationManager.warning("绕过反作弊崩溃", "服务器有OpMod，你有安装ChestUtils了 已绕过反作弊崩溃你的游戏", 19);
         }

      }
   }

   @SubscribeEvent
   public static void onClientConnect(ClientPlayerNetworkEvent.LoggingIn event) {
      checkOpModAndCheatUtils();
      HUDRenderer.onJoinServer();
      ThemeManager.load();
      if (announcementTask != null) {
         announcementTask.cancel();
      }

      announcementTask = new TimerTask() {
         public void run() {
            Minecraft mc = Minecraft.m_91087_();
            if (mc.f_91074_ != null) {
               mc.execute(() -> {
                  LexisClient.sendAnnouncement();
               });
            }

         }
      };
      if (announcementTimer == null) {
         announcementTimer = new Timer(true);
      }

      announcementTimer.schedule(announcementTask, 12000L);
   }

   @SubscribeEvent
   public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
      if (announcementTask != null) {
         announcementTask.cancel();
         announcementTask = null;
      }

   }

   private static void sendAnnouncement() {
      Component message = Component.m_237113_("§d[§6Lexis§d] §f当前版本是1.6.2\n").m_7220_(Component.m_237113_("§7===================\n")).m_7220_(Component.m_237113_("§d注意：如果这房间有网易OpMod可以绕过你的所有客户端modid改成伪造modid\n")).m_7220_(Component.m_237113_("§b如何打开作弊端GUI？ 默认按键是M，有指令中 lexis client/Server/help_Lexis <指令>\n"));
      if (ModList.get().isLoaded("baritoe")) {
         message = message.m_6881_().m_7220_(Component.m_237113_("§a你已经安装Baritone！在Lexis Gui 新出解锁窗口！更多其地功能的设置功能可解锁！\n"));
      } else {
         message = message.m_6881_().m_7220_(Component.m_237113_("§c看起来你没安装 §d[§5Baritone§d] §c在Lexis Gui 解锁不了新窗口 和 其地功能的设置功能 解锁 去这手动下载 ")).m_7220_(Component.m_237113_("§a§o[点我下载]").m_130938_((style) -> {
            return style.m_131142_(new ClickEvent(Action.OPEN_URL, "https://github.com/cabaletta/baritone/releases/download/v1.10.3/baritone-api-forge-1.10.3.jar"));
         })).m_7220_(Component.m_237113_("\n"));
      }

      if (ModList.get().isLoaded("tacz")) {
         message = message.m_6881_().m_7220_(Component.m_237113_("§d当前服务器有安装模组 §b§l§oTaCZ-永恒枪械工坊 §f枪战模组了 在Lexis Gui 已经新解锁窗口！\n"));
      } else {
         message = message.m_6881_().m_7220_(Component.m_237113_("§c这当前服务器没有 §b§l§oTaCZ-永恒枪械工坊 §f枪战模组 已经在Gui隐藏窗口了！\n"));
      }

      message = message.m_6881_().m_7220_(Component.m_237113_("§7==================="));
      Minecraft.m_91087_().f_91065_.m_93076_().m_93785_(message);
   }

   public static boolean isEnabled() {
      return enabled;
   }

   public static void setEnabled(boolean e) {
      enabled = e;
   }

   public static void init() {
      if (!initialized) {
         hackGui = new HackGui();
         tabGui = new TabGui();
         keyBindConfig = KeyBindConfig.getInstance();
         keyBinds = keyBindConfig.getKeyBinds();
         initialized = true;
         PacketCancelListener.register();
         loadKeyBinds();
         ThemeManager.load();
         ColorSettingScreen.initRainbow();
      }

   }

   private static void loadKeyBinds() {
      Iterator var0 = HackManager.getInstance().getHacks().iterator();

      while(var0.hasNext()) {
         Hack hack = (Hack)var0.next();
         if (keyBinds.containsKey(hack.getName())) {
            hack.setKeyBind((Integer)keyBinds.get(hack.getName()));
         }
      }

   }

   @SubscribeEvent
   public static void onClientTick(TickEvent.ClientTickEvent event) {
      if (!debugTickFired) {
         debugTickFired = true;
         if (mc.f_91074_ != null) {
            mc.f_91074_.m_5661_(Component.m_237113_("[Lexis] §aTick事件正常! 按M打开GUI"), false);
         }
      }

      if (event.phase == Phase.END) {
         if (hackGui == null) {
            init();
         }

         if (!loadFinished && mc.f_91074_ != null) {
            loadFinished = true;
            HackManager.getInstance().finishLoading();
         }

         HeadOnlyLook.onClientTick();
         if (!hackGui.isGuiVisible() && mc.f_91080_ == null) {
            Iterator var1 = HackManager.getInstance().getHacks().iterator();

            while(var1.hasNext()) {
               Hack hack = (Hack)var1.next();
               int keyBind = hack.getKeyBind();
               if (keyBind != -1) {
                  boolean isDown = GLFW.glfwGetKey(mc.m_91268_().m_85439_(), keyBind) == 1;
                  if (isDown && !pressedKeys.contains(keyBind)) {
                     hack.handleKeyPress();
                  }

                  if (isDown) {
                     pressedKeys.add(keyBind);
                  } else {
                     pressedKeys.remove(keyBind);
                  }
               }
            }
         }

         HackManager.getInstance().onUpdate();
      }

      if (!started && Minecraft.m_91087_().m_91268_() != null && WindowHandle.getMinecraftHwnd() != null) {
         WindowColorUtil.enableDarkMode(true);
         TitleBarAnimator.start();
         started = true;
      }

   }

   @SubscribeEvent
   public static void onKeyInput(InputEvent.Key event) {
      if (hackGui == null) {
         init();
      }

      if (event.getAction() == 1) {
         TabGui tabGui = TabGuiHack.getTabGui();
         if (tabGui != null) {
            tabGui.handleKeyPress(event.getKey(), event.getAction());
         }

         GuiKeyBindHack keyBindHack = null;
         Iterator var3 = HackManager.getInstance().getHacks().iterator();

         while(var3.hasNext()) {
            Hack hack = (Hack)var3.next();
            if (hack instanceof GuiKeyBindHack) {
               keyBindHack = (GuiKeyBindHack)hack;
               break;
            }
         }

         if (keyBindHack != null && keyBindHack.isBinding()) {
            keyBindHack.handleKeyPress(event.getKey());
            return;
         }

         int guiKey = keyBindHack != null ? keyBindHack.getGuiKey() : 77;
         if (event.getKey() == guiKey) {
            if (mc.f_91080_ == hackGui) {
               hackGui.setGuiVisible(false);
               mc.m_91152_((Screen)null);
            } else if (mc.f_91080_ == null) {
               hackGui.setGuiVisible(true);
               mc.m_91152_(hackGui);
            }
         }
      }

   }

   @SubscribeEvent
   public static void onMouseScrolled(InputEvent.MouseScrollingEvent event) {
      Iterator var1 = HackManager.getInstance().getHacks().iterator();

      CameraDistanceHack cameraHack;
      boolean altPressed;
      do {
         do {
            Hack hack;
            do {
               do {
                  if (!var1.hasNext()) {
                     return;
                  }

                  hack = (Hack)var1.next();
               } while(!(hack instanceof CameraDistanceHack));
            } while(!hack.isEnabled());

            cameraHack = (CameraDistanceHack)hack;
         } while(!cameraHack.isScrollControlEnabled());

         long window = Minecraft.m_91087_().m_91268_().m_85439_();
         altPressed = GLFW.glfwGetKey(window, 342) == 1 || GLFW.glfwGetKey(window, 346) == 1;
      } while(!altPressed);

      double delta = -event.getScrollDelta() * 2.0;
      cameraHack.addDistance((float)delta);
      event.setCanceled(true);
   }

   @SubscribeEvent
   public static void onRenderGui(RenderGuiEvent.Post event) {
      if (hackGui != null) {
         Iterator var1 = HackManager.getInstance().getHacks().iterator();

         while(var1.hasNext()) {
            Hack hack = (Hack)var1.next();
            if (hack instanceof IGuiRenderable && hack.isEnabled()) {
               ((IGuiRenderable)hack).onRenderGui(event.getGuiGraphics(), event.getPartialTick());
            }
         }

         HUDRenderer.render(event.getGuiGraphics(), event.getPartialTick());
         TabGui tabGui = TabGuiHack.getTabGui();
         if (tabGui != null) {
            tabGui.render(event.getGuiGraphics(), 0, 0, event.getPartialTick());
         }

         HUDRenderer.render(event.getGuiGraphics(), event.getPartialTick());
         Iterator var10 = HackManager.getInstance().getHacks().iterator();

         while(var10.hasNext()) {
            Hack hack = (Hack)var10.next();
            if (hack instanceof BindsDisplayHack && hack.isEnabled()) {
               BindsDisplayWidget widget = ((BindsDisplayHack)hack).getWidget();
               if (widget != null) {
                  double mouseX = mc.f_91067_.m_91589_() * (double)mc.m_91268_().m_85445_() / (double)mc.m_91268_().m_85443_();
                  double mouseY = mc.f_91067_.m_91594_() * (double)mc.m_91268_().m_85446_() / (double)mc.m_91268_().m_85444_();
                  widget.render(event.getGuiGraphics(), (int)mouseX, (int)mouseY, event.getPartialTick());
               }
               break;
            }
         }

      }
   }

   @SubscribeEvent
   public static void onMouseInput(InputEvent.MouseButton.Pre event) {
      Iterator var1;
      Hack hack;
      if (event.getButton() == 1 && event.getAction() == 1) {
         var1 = HackManager.getInstance().getHacks().iterator();

         while(var1.hasNext()) {
            hack = (Hack)var1.next();
            if (hack instanceof AirPlaceHack && hack.isEnabled()) {
               ((AirPlaceHack)hack).onRightClick();
            }
         }
      }

      if (mc.f_91080_ instanceof MoveBindsDisplayScreen && event.getAction() == 1) {
         var1 = HackManager.getInstance().getHacks().iterator();

         while(var1.hasNext()) {
            hack = (Hack)var1.next();
            if (hack instanceof BindsDisplayHack && hack.isEnabled()) {
               BindsDisplayWidget widget = ((BindsDisplayHack)hack).getWidget();
               if (widget != null) {
                  double mouseX = mc.f_91067_.m_91589_() * (double)mc.m_91268_().m_85445_() / (double)mc.m_91268_().m_85443_();
                  double mouseY = mc.f_91067_.m_91594_() * (double)mc.m_91268_().m_85446_() / (double)mc.m_91268_().m_85444_();
                  if (widget.mouseClicked(mouseX, mouseY, event.getButton())) {
                     event.setCanceled(true);
                     break;
                  }
               }
            }
         }
      }

   }

   @SubscribeEvent
   public static void onMouseReleased(InputEvent.MouseButton.Post event) {
      if (mc.f_91080_ instanceof MoveBindsDisplayScreen) {
         Iterator var1 = HackManager.getInstance().getHacks().iterator();

         while(var1.hasNext()) {
            Hack hack = (Hack)var1.next();
            if (hack instanceof BindsDisplayHack && hack.isEnabled()) {
               BindsDisplayWidget widget = ((BindsDisplayHack)hack).getWidget();
               if (widget != null) {
                  widget.mouseReleased();
               }
            }
         }
      }

   }

   static {
      init();
      hasShownOpModWarning = false;
      started = false;
      debugTickFired = false;
      loadFinished = false;
      pressedKeys = new HashSet();
   }
}
