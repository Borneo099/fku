package fku.org.example.fku.features.dynamicisland;

import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class DynamicIslandConfigScreen extends Screen {
   private static final int WIDTH = 360;
   private static final int VISIBLE_HEIGHT = 380;
   private static final int BTN_ON = 26112;
   private static final int BTN_OFF = 6684672;
   private final DynamicIslandConfig cfg = DynamicIslandConfig.getInstance();
   private String activeCategory = "通用";
   private int scrollOffset = 0;
   private int contentMaxRow = 0;
   private int cy = 0;
   private final List recordedLabels = new ArrayList();

   public DynamicIslandConfigScreen() {
      super(Component.literal("灵动岛配置"));
   }

   protected void init() {
      super.init();
      this.rebuildWidgets();
   }

   protected void rebuildWidgets() {
      this.clearWidgets();
      this.recordedLabels.clear();
      int cx = (this.width - 360) / 2;
      int cy = (this.height - 380) / 2;
      this.cy = cy;
      int row = cy + 35 - this.scrollOffset;
      this.contentMaxRow = row;
      String[] cats = new String[]{"通用", "动画", "通知", "外观", "进度条", "过滤"};
      int tabX = cx + 5;
      String[] var6 = cats;
      int var7 = cats.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         String cat = var6[var8];
         int tw = Minecraft.getInstance().font.width(cat) + 12;
         boolean active = cat.equals(this.activeCategory);
         this.addRenderableWidget(Button.builder(Component.literal((active ? "§l[" : " ") + cat + (active ? "]§r" : " ")), (btn) -> {
            this.activeCategory = cat;
            this.scrollOffset = 0;
            this.rebuildWidgets();
         }).bounds(tabX, cy + 5, Math.max(tw, 44), 16).build());
         tabX += Math.max(tw, 44) + 2;
      }

      int var10000;
      switch (this.activeCategory) {
         case "通用":
            var10000 = this.buildGeneral(cx, cy, row);
            break;
         case "动画":
            var10000 = this.buildAnimation(cx, cy, row);
            break;
         case "通知":
            var10000 = this.buildNotify(cx, cy, row);
            break;
         case "外观":
            var10000 = this.buildAppearance(cx, cy, row);
            break;
         case "进度条":
            var10000 = this.buildProgress(cx, cy, row);
            break;
         case "过滤":
            var10000 = this.buildFilter(cx, cy, row);
            break;
         default:
            var10000 = row;
      }

      int endRow = var10000;
      this.contentMaxRow = endRow + this.scrollOffset - cy;
      this.addRenderableWidget(Button.builder(Component.literal("返回主菜单"), (btn) -> {
         Minecraft.getInstance().setScreen(new ClickGuiScreen());
      }).bounds(cx + 50, cy + 380 - 28, 100, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("重置为默认"), (btn) -> {
         this.resetDefaults();
         this.rebuildWidgets();
      }).bounds(cx + 210, cy + 380 - 28, 100, 20).build());
   }

   private boolean rowVisible(int row) {
      return row >= this.cy + 30 && row <= this.cy + 380 - 34;
   }

   private int buildGeneral(int cx, int cy, int row) {
      this.addToggle(row, "启用灵动岛", this.cfg.enabled, () -> {
         this.cfg.setEnabled(!this.cfg.enabled);
      });
      row += 26;
      this.addToggle(row, "空闲常驻信息", this.cfg.idleShowInfo, () -> {
         this.cfg.idleShowInfo = !this.cfg.idleShowInfo;
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      this.addToggle(row, "Tab键替换玩家列表", this.cfg.tabShowEnabled, () -> {
         this.cfg.tabShowEnabled = !this.cfg.tabShowEnabled;
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      this.addToggle(row, "允许拖动锚点", this.cfg.dragEnabled, () -> {
         this.cfg.dragEnabled = !this.cfg.dragEnabled;
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      int[] px;
      String[][] var5;
      int var6;
      int var7;
      boolean a;
      if (this.rowVisible(row)) {
         this.drawLabel("信息显示", cx, row);
         px = new int[]{cx + 110};
         var5 = new String[][]{{"轮换", "ROTATE"}, {"同显", "ALL"}};
         var6 = var5.length;

         for(var7 = 0; var7 < var6; ++var7) {
            final String[] p = var5[var7];
            a = this.cfg.idleDisplayMode.equals(p[1]);
            this.addRenderableWidget(Button.builder(Component.literal(a ? "▶" + p[0] : p[0]), (btn) -> {
               this.cfg.idleDisplayMode = p[1];
               DynamicIslandConfig var10000 = this.cfg;
               DynamicIslandConfig.save();
               this.rebuildWidgets();
            }).bounds(px[0], row, 56, 18).build());
            px[0] += 60;
         }
      }

      row += 26;
      this.addAdjust(row, "轮换间隔(秒)", String.valueOf(this.cfg.idleInfoRotateSeconds), () -> {
         this.cfg.idleInfoRotateSeconds = Math.max(1, this.cfg.idleInfoRotateSeconds - 1);
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      }, () -> {
         this.cfg.idleInfoRotateSeconds = Math.min(30, this.cfg.idleInfoRotateSeconds + 1);
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      this.addToggle(row, "· 玩家ID", this.cfg.idleShowPlayer, () -> {
         this.cfg.idleShowPlayer = !this.cfg.idleShowPlayer;
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      this.addToggle(row, "· 游戏时间", this.cfg.idleShowTime, () -> {
         this.cfg.idleShowTime = !this.cfg.idleShowTime;
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      this.addToggle(row, "· 天气", this.cfg.idleShowWeather, () -> {
         this.cfg.idleShowWeather = !this.cfg.idleShowWeather;
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      this.addToggle(row, "· 坐标", this.cfg.idleShowCoords, () -> {
         this.cfg.idleShowCoords = !this.cfg.idleShowCoords;
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      this.addToggle(row, "· FPS", this.cfg.idleShowFps, () -> {
         this.cfg.idleShowFps = !this.cfg.idleShowFps;
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      this.addToggle(row, "· 维度", this.cfg.idleShowDimension, () -> {
         this.cfg.idleShowDimension = !this.cfg.idleShowDimension;
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      this.addToggle(row, "· 生物群系", this.cfg.idleShowBiome, () -> {
         this.cfg.idleShowBiome = !this.cfg.idleShowBiome;
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      if (this.rowVisible(row)) {
         this.drawLabel("锚点位置", cx, row);
         px = new int[]{cx + 110};
         var5 = new String[][]{{"居中", "TOP_CENTER"}, {"左上", "TOP_LEFT"}, {"右上", "TOP_RIGHT"}};
         var6 = var5.length;

         for(var7 = 0; var7 < var6; ++var7) {
            final String[] p = var5[var7];
            a = this.cfg.islandPosition.equals(p[1]);
            this.addRenderableWidget(Button.builder(Component.literal(a ? "▶" + p[0] : p[0]), (btn) -> {
               this.cfg.setIslandPosition(p[1]);
               this.rebuildWidgets();
            }).bounds(px[0], row, 56, 18).build());
            px[0] += 60;
         }
      }

      row += 26;
      this.addAdjust(row, "水平偏移", String.valueOf(this.cfg.positionOffsetX), () -> {
         this.cfg.positionOffsetX = Math.max(-400, this.cfg.positionOffsetX - 5);
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      }, () -> {
         this.cfg.positionOffsetX = Math.min(400, this.cfg.positionOffsetX + 5);
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      this.addAdjust(row, "垂直偏移", String.valueOf(this.cfg.positionOffsetY), () -> {
         this.cfg.positionOffsetY = Math.max(0, this.cfg.positionOffsetY - 2);
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      }, () -> {
         this.cfg.positionOffsetY = Math.min(200, this.cfg.positionOffsetY + 2);
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      return row;
   }

   private int buildAnimation(int cx, int cy, int row) {
      this.addToggle(row, "启用动画", this.cfg.animationEnabled, () -> {
         this.cfg.setAnimationEnabled(!this.cfg.animationEnabled);
      });
      row += 26;
      this.addAdjust(row, "展开时长(ms)", String.valueOf(this.cfg.expandDuration), () -> {
         this.cfg.expandDuration = Math.max(50, this.cfg.expandDuration - 50);
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      }, () -> {
         this.cfg.expandDuration = Math.min(1000, this.cfg.expandDuration + 50);
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      this.addAdjust(row, "弹簧刚度", String.format("%.0f", this.cfg.stiffness), () -> {
         this.cfg.setStiffness(this.cfg.stiffness - 25.0F);
         this.rebuildWidgets();
      }, () -> {
         this.cfg.setStiffness(this.cfg.stiffness + 25.0F);
         this.rebuildWidgets();
      });
      row += 26;
      this.addAdjust(row, "阻尼系数", String.format("%.0f", this.cfg.damping), () -> {
         this.cfg.setDamping(this.cfg.damping - 2.0F);
         this.rebuildWidgets();
      }, () -> {
         this.cfg.setDamping(this.cfg.damping + 2.0F);
         this.rebuildWidgets();
      });
      row += 26;
      this.addToggle(row, "唱片机节奏摇摆", this.cfg.musicSwayEnabled, () -> {
         this.cfg.musicSwayEnabled = !this.cfg.musicSwayEnabled;
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      this.addToggle(row, "伤害震动", this.cfg.damageShakeEnabled, () -> {
         this.cfg.damageShakeEnabled = !this.cfg.damageShakeEnabled;
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      return row;
   }

   private int buildNotify(int cx, int cy, int row) {
      this.addToggle(row, "攻击伤害显示", this.cfg.showDamage, () -> {
         this.cfg.showDamage = !this.cfg.showDamage;
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      this.addAdjust(row, "展示时长(ms)", String.valueOf(this.cfg.notificationDuration), () -> {
         this.cfg.notificationDuration = Math.max(500, this.cfg.notificationDuration - 250);
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      }, () -> {
         this.cfg.notificationDuration = Math.min(10000, this.cfg.notificationDuration + 250);
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      this.addAdjust(row, "最大队列", String.valueOf(this.cfg.maxQueueSize), () -> {
         this.cfg.setMaxQueueSize(this.cfg.maxQueueSize - 1);
         this.rebuildWidgets();
      }, () -> {
         this.cfg.setMaxQueueSize(this.cfg.maxQueueSize + 1);
         this.rebuildWidgets();
      });
      row += 26;
      this.addToggle(row, "同模块合并", this.cfg.mergeSameFeature, () -> {
         this.cfg.mergeSameFeature = !this.cfg.mergeSameFeature;
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      this.addToggle(row, "显示时间戳", this.cfg.showTimestamp, () -> {
         this.cfg.showTimestamp = !this.cfg.showTimestamp;
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      this.addToggle(row, "进/退游戏提示", this.cfg.showSessionNotify, () -> {
         this.cfg.showSessionNotify = !this.cfg.showSessionNotify;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      this.addToggle(row, "多人玩家进/退游戏", this.cfg.showPlayerJoinLeave, () -> {
         this.cfg.showPlayerJoinLeave = !this.cfg.showPlayerJoinLeave;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      return row;
   }

   private int buildAppearance(int cx, int cy, int row) {
      this.addAdjust(row, "收缩宽", String.valueOf(this.cfg.capsuleWidth), () -> {
         this.cfg.capsuleWidth = Math.max(40, this.cfg.capsuleWidth - 10);
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      }, () -> {
         this.cfg.capsuleWidth = Math.min(200, this.cfg.capsuleWidth + 10);
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      this.addAdjust(row, "收缩高", String.valueOf(this.cfg.capsuleHeight), () -> {
         this.cfg.capsuleHeight = Math.max(16, this.cfg.capsuleHeight - 2);
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      }, () -> {
         this.cfg.capsuleHeight = Math.min(60, this.cfg.capsuleHeight + 2);
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      this.addAdjust(row, "展开宽", String.valueOf(this.cfg.expandedWidth), () -> {
         this.cfg.expandedWidth = Math.max(120, this.cfg.expandedWidth - 20);
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      }, () -> {
         this.cfg.expandedWidth = Math.min(500, this.cfg.expandedWidth + 20);
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      this.addAdjust(row, "展开高", String.valueOf(this.cfg.expandedHeight), () -> {
         this.cfg.expandedHeight = Math.max(24, this.cfg.expandedHeight - 4);
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      }, () -> {
         this.cfg.expandedHeight = Math.min(80, this.cfg.expandedHeight + 4);
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      this.addAdjust(row, "圆角半径", String.valueOf(this.cfg.cornerRadius), () -> {
         this.cfg.cornerRadius = Math.max(0, this.cfg.cornerRadius - 2);
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      }, () -> {
         this.cfg.cornerRadius = Math.min(30, this.cfg.cornerRadius + 2);
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      this.addHexInput(row, "背景色", this.cfg.backgroundColor, (hex) -> {
         this.cfg.backgroundColor = hex;
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
      });
      row += 26;
      this.addHexInput(row, "文字色", this.cfg.textColor, (hex) -> {
         this.cfg.textColor = hex;
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
      });
      row += 26;
      this.addRenderableWidget(Button.builder(Component.literal("重置锚点位置"), (btn) -> {
         this.cfg.freeX = -1;
         this.cfg.freeY = -1;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      }).bounds(cx + 50, row, 130, 20).build());
      row += 26;
      return row;
   }

   private int buildProgress(int cx, int cy, int row) {
      this.addToggle(row, "启用进度条", this.cfg.showProgressBars, () -> {
         this.cfg.showProgressBars = !this.cfg.showProgressBars;
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      this.addToggle(row, "挖掘方块", this.cfg.showProgMining, () -> {
         this.cfg.showProgMining = !this.cfg.showProgMining;
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      this.addToggle(row, "进食/饮用", this.cfg.showProgEating, () -> {
         this.cfg.showProgEating = !this.cfg.showProgEating;
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      this.addToggle(row, "弓弩蓄力", this.cfg.showProgBow, () -> {
         this.cfg.showProgBow = !this.cfg.showProgBow;
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      this.addToggle(row, "盾牌耐久", this.cfg.showProgShield, () -> {
         this.cfg.showProgShield = !this.cfg.showProgShield;
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      this.addToggle(row, "钓鱼状态", this.cfg.showProgFishing, () -> {
         this.cfg.showProgFishing = !this.cfg.showProgFishing;
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      this.addToggle(row, "唱片机播放", this.cfg.showProgMusic, () -> {
         this.cfg.showProgMusic = !this.cfg.showProgMusic;
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      this.addToggle(row, "经验升级", this.cfg.showProgXp, () -> {
         this.cfg.showProgXp = !this.cfg.showProgXp;
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      this.addToggle(row, "实体模型放置", this.cfg.showProgModelPlace, () -> {
         this.cfg.showProgModelPlace = !this.cfg.showProgModelPlace;
         DynamicIslandConfig.save();
         this.rebuildWidgets();
      });
      row += 26;
      return row;
   }

   private int buildFilter(int cx, int cy, int row) {
      this.addHexInput(row, "允许模块(逗号分隔, 空=全部)", String.join(",", this.cfg.enabledFeatures), (s) -> {
         this.cfg.enabledFeatures = splitList(s);
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
      });
      row += 26;
      this.addHexInput(row, "禁用模块(逗号分隔)", String.join(",", this.cfg.disabledFeatures), (s) -> {
         this.cfg.disabledFeatures = splitList(s);
         DynamicIslandConfig var10000 = this.cfg;
         DynamicIslandConfig.save();
      });
      row += 26;
      this.drawLabel("提示: 模块id见各功能中文名(如 如来神掌/32k弓)", cx, row);
      row += 26;
      return row;
   }

   private static List<String> splitList(String s) {
      return s != null && !s.isBlank() ? new ArrayList(Arrays.asList(s.split(","))) : new ArrayList();
   }

   private void addToggle(int row, String label, boolean current, Runnable setter) {
      if (this.rowVisible(row)) {
         int cx = (this.width - 360) / 2;
         this.drawLabel(label, cx, row);
         this.addRenderableWidget(Button.builder(Component.literal(current ? "开" : "关"), (btn) -> {
            setter.run();
            btn.setMessage(Component.literal(current ? "关" : "开"));
            this.rebuildWidgets();
         }).bounds(cx + 200, row, 50, 20).build());
      }
   }

   private void addAdjust(int row, String label, String value, Runnable minus, Runnable plus) {
      if (this.rowVisible(row)) {
         int cx = (this.width - 360) / 2;
         this.drawLabel(label, cx, row);
         this.addRenderableWidget(Button.builder(Component.literal("-"), (btn) -> {
            minus.run();
         }).bounds(cx + 200, row, 24, 20).build());
         this.addRenderableWidget(Button.builder(Component.literal(value), (btn) -> {
         }).bounds(cx + 226, row, 60, 20).build());
         this.addRenderableWidget(Button.builder(Component.literal("+"), (btn) -> {
            plus.run();
         }).bounds(cx + 288, row, 24, 20).build());
      }
   }

   private void addHexInput(int row, String label, String value, Consumer<String> onChanged) {
      if (this.rowVisible(row)) {
         int cx = (this.width - 360) / 2;
         this.drawLabel(label, cx, row);
         EditBox box = new EditBox(Minecraft.getInstance().font, cx + 200, row, 140, 18, Component.literal(""));
         box.setValue(value);
         box.setMaxLength(64);
         box.setResponder((s) -> {
            if (!s.isBlank()) {
               onChanged.accept(s.trim());
            }

         });
         this.addRenderableWidget(box);
      }
   }

   private void drawLabel(String text, int cx, int row) {
      this.recordedLabels.add(new String[]{text, String.valueOf(row + this.scrollOffset - this.cy)});
   }

   private void resetDefaults() {
      DynamicIslandConfig d = new DynamicIslandConfig();
      d.enabledFeatures = new ArrayList();
      d.disabledFeatures = new ArrayList();
      this.cfg.enabled = d.enabled;
      this.cfg.islandPosition = d.islandPosition;
      this.cfg.positionOffsetX = d.positionOffsetX;
      this.cfg.positionOffsetY = d.positionOffsetY;
      this.cfg.idleShowInfo = d.idleShowInfo;
      this.cfg.idleDisplayMode = d.idleDisplayMode;
      this.cfg.idleInfoRotateSeconds = d.idleInfoRotateSeconds;
      this.cfg.idleShowPlayer = d.idleShowPlayer;
      this.cfg.idleShowTime = d.idleShowTime;
      this.cfg.idleShowWeather = d.idleShowWeather;
      this.cfg.idleShowCoords = d.idleShowCoords;
      this.cfg.idleShowFps = d.idleShowFps;
      this.cfg.idleShowDimension = d.idleShowDimension;
      this.cfg.idleShowBiome = d.idleShowBiome;
      this.cfg.showProgressBars = d.showProgressBars;
      this.cfg.showProgMining = d.showProgMining;
      this.cfg.showProgEating = d.showProgEating;
      this.cfg.showProgBow = d.showProgBow;
      this.cfg.showProgShield = d.showProgShield;
      this.cfg.showProgFishing = d.showProgFishing;
      this.cfg.showProgXp = d.showProgXp;
      this.cfg.showProgMusic = d.showProgMusic;
      this.cfg.showProgModelPlace = d.showProgModelPlace;
      this.cfg.musicSwayEnabled = d.musicSwayEnabled;
      this.cfg.damageShakeEnabled = d.damageShakeEnabled;
      this.cfg.animationEnabled = d.animationEnabled;
      this.cfg.expandDuration = d.expandDuration;
      this.cfg.stiffness = d.stiffness;
      this.cfg.damping = d.damping;
      this.cfg.notificationDuration = d.notificationDuration;
      this.cfg.maxQueueSize = d.maxQueueSize;
      this.cfg.mergeSameFeature = d.mergeSameFeature;
      this.cfg.showTimestamp = d.showTimestamp;
      this.cfg.showDamage = d.showDamage;
      this.cfg.showSessionNotify = d.showSessionNotify;
      this.cfg.showPlayerJoinLeave = d.showPlayerJoinLeave;
      this.cfg.capsuleWidth = d.capsuleWidth;
      this.cfg.capsuleHeight = d.capsuleHeight;
      this.cfg.expandedWidth = d.expandedWidth;
      this.cfg.expandedHeight = d.expandedHeight;
      this.cfg.backgroundColor = d.backgroundColor;
      this.cfg.textColor = d.textColor;
      this.cfg.cornerRadius = d.cornerRadius;
      this.cfg.enabledFeatures.clear();
      this.cfg.disabledFeatures.clear();
      DynamicIslandConfig.save();
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
      int cx = (this.width - 360) / 2;
      int cy = (this.height - 380) / 2;
      if (mouseX >= (double)cx && mouseX <= (double)(cx + 360) && mouseY >= (double)cy && mouseY <= (double)(cy + 380)) {
         int newScroll = this.scrollOffset - (int)(delta * 16.0);
         int maxScroll = Math.max(0, this.contentMaxRow - 335);
         this.scrollOffset = Math.max(0, Math.min(newScroll, maxScroll));
         this.rebuildWidgets();
         return true;
      } else {
         return super.mouseScrolled(mouseX, mouseY, delta);
      }
   }

   public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(g);
      int cx = (this.width - 360) / 2;
      int cy = (this.height - 380) / 2;
      GuiRenderHelper.drawPanelBackground(g, cx, cy, 360, 380, false);
      g.drawString(this.font, "灵动岛配置 - " + this.activeCategory, cx + 10, cy + 25, 16777215);
      g.enableScissor(cx + 2, cy + 35, cx + 360 - 2, cy + 380 - 32);
      Iterator var7 = this.recordedLabels.iterator();

      while(var7.hasNext()) {
         String[] label = (String[])var7.next();
         int yRow = cy + Integer.parseInt(label[1]) - this.scrollOffset;
         if (yRow + 4 >= cy + 35 && yRow < cy + 380 - 35) {
            g.drawString(this.font, label[0], cx + 10, yRow + 4, 11184810);
         }
      }

      g.disableScissor();
      super.render(g, mouseX, mouseY, partialTick);
   }

   public boolean isPauseScreen() {
      return false;
   }

   public void onClose() {
      Minecraft.getInstance().setScreen(new ClickGuiScreen());
   }
}
