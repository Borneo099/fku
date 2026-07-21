package fku.org.example.fku.features.attackindicator;

import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.ColorWheelPicker;
import fku.org.example.fku.features.attackindicator.AttackIndicatorConfig;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class AttackIndicatorConfigScreen
extends Screen {
    private static final int W = 280;
    private static final int H = 340;
    private int bx;
    private int by;
    private int currentPage = 0;
    private static final String[] PAGE_TITLES = new String[]{"\u00a7l\u901a\u7528\u8bbe\u7f6e", "\u00a7l\u8fde\u63a5\u7279\u6548", "\u00a7l\u76ee\u6807\u6807\u8bb0", "\u00a7l\u5c4f\u5e55\u8986\u76d6", "\u00a7l\u6027\u80fd\u8bbe\u7f6e"};
    private final AttackIndicatorConfig cfg = AttackIndicatorConfig.getInstance();
    private ColorWheelPicker colorWheelPicker = new ColorWheelPicker("FF4444", hex -> {
        if (this.pendingColorField == null) {
            return;
        }
        switch (this.pendingColorField) {
            case "beamColor": {
                this.cfg.beamColor = hex;
                break;
            }
            case "lightningColor": {
                this.cfg.lightningColor = hex;
                break;
            }
            case "waveColor": {
                this.cfg.waveColor = hex;
                break;
            }
            case "tetherColor": {
                this.cfg.tetherColor = hex;
                break;
            }
            case "boxColor": {
                this.cfg.boxColor = hex;
                break;
            }
            case "glowColor": {
                this.cfg.glowColor = hex;
                break;
            }
            case "beamMarkerColor": {
                this.cfg.beamMarkerColor = hex;
                break;
            }
            case "haloColor": {
                this.cfg.haloColor = hex;
                break;
            }
            case "flashColor": {
                this.cfg.flashColor = hex;
                break;
            }
            case "arrowColor": {
                this.cfg.arrowColor = hex;
                break;
            }
            case "swordWaveColor": {
                this.cfg.swordWaveColor = hex;
                break;
            }
        }
        AttackIndicatorConfig.save();
        this.rebuildWidgets();
    });
    private String pendingColorField = null;

    public AttackIndicatorConfigScreen() {
        super(Component.literal((String)"\u653b\u51fb\u6307\u793a\u5668\u914d\u7f6e"));
    }

    protected void init() {
        super.init();
        this.bx = (this.width - 280) / 2;
        this.by = (this.height - 340) / 2;
        this.rebuildWidgets();
    }

    protected void rebuildWidgets() {
        this.clearWidgets();
        int x = this.bx;
        int y = this.by + 5;
        int btnW = 52;
        for (int i = 0; i < 5; ++i) {
            int page = i;
            this.addRenderableWidget(Button.builder(Component.literal((String)(this.currentPage == i ? "\u00a7a" + PAGE_TITLES[i].replace("\u00a7l", "") : "\u00a77" + PAGE_TITLES[i].replace("\u00a7l", ""))), btn -> {
                this.currentPage = page;
                this.rebuildWidgets();
            }).bounds(x + i * (btnW + 4), y, btnW, 16).build());
        }
        y += 22;
        switch (this.currentPage) {
            case 0: {
                this.renderGeneralPage(x, y);
                break;
            }
            case 1: {
                this.renderConnectionPage(x, y);
                break;
            }
            case 2: {
                this.renderTargetMarkPage(x, y);
                break;
            }
            case 3: {
                this.renderScreenOverlayPage(x, y);
                break;
            }
            case 4: {
                this.renderPerformancePage(x, y);
            }
        }
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u00a77\u2190 \u8fd4\u56de"), btn -> Minecraft.getInstance().setScreen(new ClickGuiScreen())).bounds(x + 140 - 40, this.by + 340 - 22, 80, 18).build());
    }

    private void renderGeneralPage(int x, int y) {
        int sp = 24;
        this.addRenderableWidget(Button.builder(Component.literal((String)(this.cfg.enabled ? "\u00a7a\u25a0 \u529f\u80fd\u5df2\u5f00\u542f" : "\u00a7c\u25a1 \u529f\u80fd\u5df2\u5173\u95ed")), btn -> {
            this.cfg.enabled = !this.cfg.enabled;
            AttackIndicatorConfig.save();
            this.rebuildWidgets();
        }).bounds(x + 10, y, 260, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal((String)("\u89e6\u53d1\u6a21\u5f0f: \u00a7b" + this.cfg.triggerMode)), btn -> {
            this.cfg.triggerMode = switch (this.cfg.triggerMode) {
                case "ON_ATTACK" -> "ON_TPAURA_LOCK";
                case "ON_TPAURA_LOCK" -> "BOTH";
                default -> "ON_ATTACK";
            };
            AttackIndicatorConfig.save();
            btn.setMessage(Component.literal((String)("\u89e6\u53d1\u6a21\u5f0f: \u00a7b" + this.cfg.triggerMode)));
        }).bounds(x + 10, y += sp + 4, 260, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal((String)(this.cfg.smoothTransition ? "\u5e73\u6ed1\u8fc7\u6e21: \u5f00" : "\u5e73\u6ed1\u8fc7\u6e21: \u5173")), btn -> {
            this.cfg.smoothTransition = !this.cfg.smoothTransition;
            AttackIndicatorConfig.save();
            btn.setMessage(Component.literal((String)(this.cfg.smoothTransition ? "\u5e73\u6ed1\u8fc7\u6e21: \u5f00" : "\u5e73\u6ed1\u8fc7\u6e21: \u5173")));
        }).bounds(x + 10, y += sp, 260, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u00a77\u00a7l\u89e6\u53d1\u6a21\u5f0f\u8bf4\u660e"), b -> {}).bounds(x + 10, y += sp + 8, 260, 14).build());
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u00a77ON_ATTACK \u2192 \u4ec5\u653b\u51fb\u65f6\u89e6\u53d1"), b -> {}).bounds(x + 10, y += 16, 260, 14).build());
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u00a77ON_TPAURA_LOCK \u2192 \u4ec5TpAura\u9501\u5b9a"), b -> {}).bounds(x + 10, y += 14, 260, 14).build());
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u00a77BOTH \u2192 \u4e24\u8005\u90fd\u89e6\u53d1"), b -> {}).bounds(x + 10, y += 14, 260, 14).build());
    }

    private void renderConnectionPage(int x, int y) {
        int sp = 24;
        this.addToggle(x, y, 280, "\u80fd\u91cf\u5149\u675f", this.cfg.enableBeam, v -> {
            this.cfg.enableBeam = v;
        });
        y += sp;
        if (this.cfg.enableBeam) {
            this.addColorInput(x + 15, y, "\u989c\u8272", this.cfg.beamColor, "beamColor");
            this.addSliderDesc(x + 15, y += 20, "\u5bbd\u5ea6: " + String.format("%.1f", this.cfg.beamWidth)), b -> {
                this.cfg.beamWidth = this.cfg.beamWidth >= 5.0f ? 1.0f : this.cfg.beamWidth + 0.5f;
                AttackIndicatorConfig.save();
                this.rebuildWidgets();
            });
            this.addSliderDesc(x + 15, y += 20, "\u6d41\u901f: " + String.format("%.1f", this.cfg.beamFlowSpeed)), b -> {
                this.cfg.beamFlowSpeed = this.cfg.beamFlowSpeed >= 2.0f ? 0.1f : this.cfg.beamFlowSpeed + 0.2f;
                AttackIndicatorConfig.save();
                this.rebuildWidgets();
            });
            y += 12;
        }
        this.addToggle(x, y, 280, "\u95ea\u7535\u94fe", this.cfg.enableLightning, v -> {
            this.cfg.enableLightning = v;
        });
        y += sp;
        if (this.cfg.enableLightning) {
            this.addColorInput(x + 15, y, "\u989c\u8272", this.cfg.lightningColor, "lightningColor");
            this.addSliderDesc(x + 15, y += 20, "\u6bb5\u6570: " + this.cfg.lightningSegments, b -> {
                this.cfg.lightningSegments = this.cfg.lightningSegments >= 20 ? 4 : this.cfg.lightningSegments + 2;
                AttackIndicatorConfig.save();
                this.rebuildWidgets();
            });
            y += 12;
        }
        this.addToggle(x, y, 280, "\u8109\u51b2\u6ce2", this.cfg.enablePulseWave, v -> {
            this.cfg.enablePulseWave = v;
        });
        y += sp;
        if (this.cfg.enablePulseWave) {
            this.addColorInput(x + 15, y, "\u989c\u8272", this.cfg.waveColor, "waveColor");
            this.addSliderDesc(x + 15, y += 20, "\u901f\u5ea6: " + String.format("%.1f", this.cfg.waveSpeed)), b -> {
                this.cfg.waveSpeed = this.cfg.waveSpeed >= 3.0f ? 0.5f : this.cfg.waveSpeed + 0.5f;
                AttackIndicatorConfig.save();
                this.rebuildWidgets();
            });
            y += 12;
        }
        this.addToggle(x, y, 280, "\u9501\u94fe", this.cfg.enableTether, v -> {
            this.cfg.enableTether = v;
        });
        y += sp;
        if (this.cfg.enableTether) {
            this.addColorInput(x + 15, y, "\u989c\u8272", this.cfg.tetherColor, "tetherColor");
            this.addSliderDesc(x + 15, y += 20, "\u6447\u6643: " + String.format("%.1f", this.cfg.tetherSway)), b -> {
                this.cfg.tetherSway = this.cfg.tetherSway >= 1.0f ? 0.1f : this.cfg.tetherSway + 0.1f;
                AttackIndicatorConfig.save();
                this.rebuildWidgets();
            });
            y += 12;
        }
        this.addToggle(x, y, 280, "\u5251\u6c14\u5200\u6ce2", this.cfg.enableSwordWave, v -> {
            this.cfg.enableSwordWave = v;
        });
        y += sp;
        if (this.cfg.enableSwordWave) {
            this.addColorInput(x + 15, y, "\u989c\u8272", this.cfg.swordWaveColor, "swordWaveColor");
            this.addSliderDesc(x + 15, y += 20, "\u5f3a\u5ea6: " + String.format("%.1f", this.cfg.swordWaveIntensity)), b -> {
                this.cfg.swordWaveIntensity = this.cfg.swordWaveIntensity >= 2.0f ? 0.5f : this.cfg.swordWaveIntensity + 0.5f;
                AttackIndicatorConfig.save();
                this.rebuildWidgets();
            });
            this.addSliderDesc(x + 15, y += 20, "\u901f\u5ea6: " + String.format("%.1f", this.cfg.swordWaveSpeed)), b -> {
                this.cfg.swordWaveSpeed = this.cfg.swordWaveSpeed >= 2.0f ? 0.5f : this.cfg.swordWaveSpeed + 0.5f;
                AttackIndicatorConfig.save();
                this.rebuildWidgets();
            });
            y += 12;
        }
    }

    private void renderTargetMarkPage(int x, int y) {
        int sp = 24;
        this.addToggle(x, y, 280, "\u9501\u5b9a\u6846", this.cfg.enableLockBox, v -> {
            this.cfg.enableLockBox = v;
        });
        y += sp;
        if (this.cfg.enableLockBox) {
            this.addColorInput(x + 15, y, "\u989c\u8272", this.cfg.boxColor, "boxColor");
            this.addSliderDesc(x + 15, y += 20, "\u65cb\u8f6c\u901f\u5ea6: " + String.format("%.1f", this.cfg.boxRotateSpeed)), b -> {
                this.cfg.boxRotateSpeed = this.cfg.boxRotateSpeed >= 5.0f ? 0.5f : this.cfg.boxRotateSpeed + 0.5f;
                AttackIndicatorConfig.save();
                this.rebuildWidgets();
            });
            this.addSliderDesc(x + 15, y += 20, "\u5927\u5c0f: " + String.format("%.1f", this.cfg.boxSize)), b -> {
                this.cfg.boxSize = this.cfg.boxSize >= 2.0f ? 0.5f : this.cfg.boxSize + 0.25f;
                AttackIndicatorConfig.save();
                this.rebuildWidgets();
            });
            y += 12;
        }
        this.addToggle(x, y, 280, "\u8f6e\u5ed3\u9ad8\u4eae", this.cfg.enableGlow, v -> {
            this.cfg.enableGlow = v;
        });
        y += sp;
        if (this.cfg.enableGlow) {
            this.addColorInput(x + 15, y, "\u989c\u8272", this.cfg.glowColor, "glowColor");
            this.addSliderDesc(x + 15, y += 20, "\u5f3a\u5ea6: " + String.format("%.1f", this.cfg.glowIntensity)), b -> {
                this.cfg.glowIntensity = this.cfg.glowIntensity >= 2.0f ? 0.1f : this.cfg.glowIntensity + 0.2f;
                AttackIndicatorConfig.save();
                this.rebuildWidgets();
            });
            y += 12;
        }
        this.addToggle(x, y, 280, "\u6807\u8bb0\u5149\u67f1", this.cfg.enableBeamMarker, v -> {
            this.cfg.enableBeamMarker = v;
        });
        y += sp;
        if (this.cfg.enableBeamMarker) {
            this.addColorInput(x + 15, y, "\u989c\u8272", this.cfg.beamMarkerColor, "beamMarkerColor");
            this.addSliderDesc(x + 15, y += 20, "\u9ad8\u5ea6: " + String.format("%.1f", this.cfg.beamMarkerHeight)), b -> {
                this.cfg.beamMarkerHeight = this.cfg.beamMarkerHeight >= 16.0f ? 4.0f : this.cfg.beamMarkerHeight + 2.0f;
                AttackIndicatorConfig.save();
                this.rebuildWidgets();
            });
            y += 12;
        }
        this.addToggle(x, y, 280, "\u5149\u73af", this.cfg.enableHalo, v -> {
            this.cfg.enableHalo = v;
        });
        y += sp;
        if (this.cfg.enableHalo) {
            this.addColorInput(x + 15, y, "\u989c\u8272", this.cfg.haloColor, "haloColor");
            this.addSliderDesc(x + 15, y += 20, "\u534a\u5f84: " + String.format("%.1f", this.cfg.haloRadius)), b -> {
                this.cfg.haloRadius = this.cfg.haloRadius >= 3.0f ? 0.5f : this.cfg.haloRadius + 0.25f;
                AttackIndicatorConfig.save();
                this.rebuildWidgets();
            });
            this.addSliderDesc(x + 15, y += 20, "\u65cb\u8f6c\u901f\u5ea6: " + String.format("%.1f", this.cfg.haloRotateSpeed)), b -> {
                this.cfg.haloRotateSpeed = this.cfg.haloRotateSpeed >= 3.0f ? 0.5f : this.cfg.haloRotateSpeed + 0.5f;
                AttackIndicatorConfig.save();
                this.rebuildWidgets();
            });
            y += 12;
        }
    }

    private void renderScreenOverlayPage(int x, int y) {
        int sp = 24;
        this.addToggle(x, y, 280, "\u8fb9\u7f18\u95ea\u70c1", this.cfg.enableEdgeFlash, v -> {
            this.cfg.enableEdgeFlash = v;
        });
        y += sp;
        if (this.cfg.enableEdgeFlash) {
            this.addColorInput(x + 15, y, "\u989c\u8272", this.cfg.flashColor, "flashColor");
            this.addSliderDesc(x + 15, y += 20, "\u5f3a\u5ea6: " + String.format("%.1f", this.cfg.flashIntensity)), b -> {
                this.cfg.flashIntensity = this.cfg.flashIntensity >= 1.0f ? 0.1f : this.cfg.flashIntensity + 0.1f;
                AttackIndicatorConfig.save();
                this.rebuildWidgets();
            });
            y += 12;
        }
        this.addToggle(x, y, 280, "\u65b9\u5411\u6307\u793a", this.cfg.enableDirectionArrow, v -> {
            this.cfg.enableDirectionArrow = v;
        });
        y += sp;
        if (this.cfg.enableDirectionArrow) {
            this.addColorInput(x + 15, y, "\u989c\u8272", this.cfg.arrowColor, "arrowColor");
            this.addSliderDesc(x + 15, y += 20, "\u5927\u5c0f: " + String.format("%.1f", this.cfg.arrowSize)), b -> {
                this.cfg.arrowSize = this.cfg.arrowSize >= 3.0f ? 0.5f : this.cfg.arrowSize + 0.5f;
                AttackIndicatorConfig.save();
                this.rebuildWidgets();
            });
            y += 12;
        }
    }

    private void renderPerformancePage(int x, int y) {
        int sp = 24;
        this.addToggle(x, y, 280, "\u6027\u80fd\u6a21\u5f0f", this.cfg.enablePerformanceMode, v -> {
            this.cfg.enablePerformanceMode = v;
        });
        y += sp + 4;
        if (this.cfg.enablePerformanceMode) {
            this.addRenderableWidget(Button.builder(Component.literal((String)"\u00a77\u6027\u80fd\u6a21\u5f0f\u5f00\u542f\u65f6\uff1a"), b -> {}).bounds(x + 10, y, 260, 14).build());
            this.addRenderableWidget(Button.builder(Component.literal((String)"\u00a77- \u9650\u5236\u7c92\u5b50\u6570\u91cf"), b -> {}).bounds(x + 10, y += 14, 260, 14).build());
            this.addRenderableWidget(Button.builder(Component.literal((String)"\u00a77- \u7981\u7528\u7740\u8272\u5668\u7279\u6548"), b -> {}).bounds(x + 10, y += 14, 260, 14).build());
            this.addRenderableWidget(Button.builder(Component.literal((String)"\u00a77- \u964d\u4f4e\u6e32\u67d3\u9891\u7387"), b -> {}).bounds(x + 10, y += 14, 260, 14).build());
            y += 14;
        }
        this.addRenderableWidget(Button.builder(Component.literal((String)("\u6700\u5927\u7c92\u5b50\u6570: \u00a7b" + this.cfg.maxParticles)), btn -> {
            int n = this.cfg.maxParticles = this.cfg.maxParticles >= 500 ? 10 : this.cfg.maxParticles * 2;
            if (this.cfg.maxParticles > 500) {
                this.cfg.maxParticles = 10;
            }
            AttackIndicatorConfig.save();
            btn.setMessage(Component.literal((String)("\u6700\u5927\u7c92\u5b50\u6570: \u00a7b" + this.cfg.maxParticles)));
        }).bounds(x + 10, y += 4, 260, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal((String)("LOD\u8ddd\u79bb: \u00a7b" + String.format("%.0f", this.cfg.particleLODDistance)))), btn -> {
            float[] levels = new float[]{8.0f, 16.0f, 24.0f, 32.0f, 48.0f, 64.0f, 96.0f, 128.0f};
            int idx = 0;
            for (int i = 0; i < levels.length; ++i) {
                if (!(Math.abs(this.cfg.particleLODDistance - levels[i]) < 0.1)) continue;
                idx = i;
                break;
            }
            idx = (idx + 1) % levels.length;
            this.cfg.particleLODDistance = levels[idx];
            AttackIndicatorConfig.save();
            btn.setMessage(Component.literal((String)("LOD\u8ddd\u79bb: \u00a7b" + String.format("%.0f", this.cfg.particleLODDistance)))));
        }).bounds(x + 10, y += sp, 260, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal((String)("\u6b8b\u7559\u7279\u6548: \u00a7b" + this.cfg.despawnDelay + " ticks")), btn -> {
            this.cfg.despawnDelay = (this.cfg.despawnDelay + 1) % 11;
            AttackIndicatorConfig.save();
            btn.setMessage(Component.literal((String)("\u6b8b\u7559\u7279\u6548: \u00a7b" + this.cfg.despawnDelay + " ticks")));
        }).bounds(x + 10, y += sp, 260, 18).build());
    }

    private void addToggle(int x, int y, int w, String label, boolean current, Consumer<Boolean> setter) {
        this.addRenderableWidget(Button.builder(Component.literal((String)(current ? "\u00a7a\u25a0 " + label : "\u00a77\u25a1 " + label)), btn -> {
            setter.accept(!current);
            AttackIndicatorConfig.save();
            this.rebuildWidgets();
        }).bounds(x + 10, y, w - 20, 18).build());
    }

    private void addColorInput(int x, int y, String label, String current, String fieldName) {
        String colorCode = AttackIndicatorConfigScreen.hexToColorCode(current);
        this.addRenderableWidget(Button.builder(Component.literal((String)(colorCode + "\u2588")), btn -> this.openColorPicker(fieldName, current)).bounds(x, y, 18, 16).build());
        this.addRenderableWidget(Button.builder(Component.literal((String)("\u00a77" + label + ": \u00a7f#" + current)), btn -> this.openColorPicker(fieldName, current)).bounds(x + 20, y, 225, 16).build());
    }

    private static String hexToColorCode(String hex) {
        if (hex == null || hex.length() < 6) {
            return "\u00a7f";
        }
        StringBuilder sb = new StringBuilder("\u00a7x");
        for (int i = 0; i < 6; ++i) {
            sb.append('\u00a7').append(hex.charAt(i));
        }
        return sb.toString();
    }

    private void openColorPicker(String fieldName, String currentHex) {
        this.pendingColorField = fieldName;
        this.colorWheelPicker.setColor(currentHex);
        this.colorWheelPicker.open(this.bx + 140, this.by + 170);
    }

    private void addSliderDesc(int x, int y, String text, Consumer<Button> action) {
        this.addRenderableWidget(Button.builder(Component.literal((String)("\u00a77  " + text + "  \u00a78[\u70b9\u51fb\u8c03\u8282]")), b -> action.accept(b)).bounds(x, y, 245, 16).build());
    }

    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        this.fillGradient(g);
        GuiRenderHelper.drawRoundedRect(g, this.bx - 10, this.by - 8, 300, 356, -1440603614, 8);
        g.drawString(this.font, "\u00a7l\u653b\u51fb\u6307\u793a\u5668\u914d\u7f6e", this.bx + 10, this.by - 4, 0xFFFFFF);
        super.render(g, mx, my, pt);
        this.colorWheelPicker.render(g, mx, my);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.colorWheelPicker.isOpen() && this.colorWheelPicker.mouseClicked(mouseX, mouseY, button)) {
            this.pendingColorField = null;
            this.rebuildWidgets();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void onClose() {
        AttackIndicatorConfig.save();
        this.minecraft.setScreen(new ClickGuiScreen());
    }
}

