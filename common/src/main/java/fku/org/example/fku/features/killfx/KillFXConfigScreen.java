package fku.org.example.fku.features.killfx;

import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.ColorWheelPicker;
import fku.org.example.fku.features.killfx.KillFXConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class KillFXConfigScreen
extends Screen {
    private static final int WIDTH = 300;
    private static final int VISIBLE_HEIGHT = 255;
    private static final int CONTENT_HEIGHT = 800;
    private static final int BTN_ON = 26112;
    private static final int BTN_OFF = 0x660000;
    private final KillFXConfig cfg;
    private String activeCategory = "\u901a\u7528";
    private int scrollOffset = 0;
    private int contentMaxRow = 0;
    private ColorWheelPicker colorWheelPicker;
    private final Map<String, Map<String, String>> floatingValues = new HashMap<String, Map<String, String>>();
    private EditBox timeoutInput;
    private EditBox lightningAmountInput;
    private EditBox particleCountInput;
    private EditBox particleSpeedInput;
    private EditBox volumeInput;
    private EditBox pitchInput;
    private static final String[][] PARTICLE_CATEGORIES = new String[][]{{"\u6218\u6597", "Combat"}, {"\u9b54\u6cd5", "Magic"}, {"\u706b\u7130", "Fire"}, {"\u81ea\u7136", "Nature"}, {"\u7279\u6b8a", "Update121"}, {"\u5176\u4ed6", "Misc"}};
    private static final Map<String, String> PARTICLE_ALIAS = new HashMap<String, String>();
    private static final Map<String, String> SHAPE_ALIAS;
    private static final Map<String, String> SOUND_GROUP_ALIAS;
    private static final Map<String, String> SOUND_ALIAS;
    private int currentRowGlobal;
    private static final String[][] ALL_PARTICLES;
    private static final String[] PARTICLE_CAT_KEYS;
    private static final String[][] ALL_SOUNDS;
    private static final String[] SOUND_GROUP_KEYS;

    public KillFXConfigScreen() {
        super(Component.literal((String)"\u51fb\u6740\u7279\u6548\u914d\u7f6e"));
        this.cfg = KillFXConfig.getInstance();
        for (String cat : new String[]{"\u901a\u7528", "\u95ea\u7535", "\u7c92\u5b50", "\u97f3\u6548", "\u989d\u5916", "\u7740\u8272\u5668"}) {
            this.floatingValues.put(cat, new HashMap());
        }
        this.colorWheelPicker = new ColorWheelPicker(this.cfg.crystalTintColor, hex -> {
            this.cfg.crystalTintColor = hex;
            KillFXConfig.save();
        });
    }

    protected void init() {
        super.init();
        this.rebuildWidgets();
    }

    private void saveFloatingInputs() {
        Map<String, String> catCache = this.floatingValues.get(this.activeCategory);
        if (catCache == null) {
            return;
        }
        if (this.timeoutInput != null) {
            catCache.put("timeout", this.timeoutInput.getValue());
        }
        if (this.lightningAmountInput != null) {
            catCache.put("lightningAmount", this.lightningAmountInput.getValue());
        }
        if (this.particleCountInput != null) {
            catCache.put("particleCount", this.particleCountInput.getValue());
        }
        if (this.particleSpeedInput != null) {
            catCache.put("particleSpeed", this.particleSpeedInput.getValue());
        }
        if (this.volumeInput != null) {
            catCache.put("volume", this.volumeInput.getValue());
        }
        if (this.pitchInput != null) {
            catCache.put("pitch", this.pitchInput.getValue());
        }
    }

    private String loadFloatingInput(String key, String defaultValue) {
        Map<String, String> catCache = this.floatingValues.get(this.activeCategory);
        if (catCache == null) {
            return defaultValue;
        }
        return catCache.getOrDefault(key, defaultValue);
    }

    protected void rebuildWidgets() {
        int row;
        this.clearWidgets();
        int cx = (this.width - 300) / 2;
        int cy = (this.height - 255) / 2;
        int col1 = cx + 135;
        this.contentMaxRow = row = cy + 35 - this.scrollOffset;
        String[] categories = new String[]{"\u901a\u7528", "\u95ea\u7535", "\u7c92\u5b50", "\u97f3\u6548", "\u989d\u5916", "\u7740\u8272\u5668"};
        int tabX = cx + 5;
        String[] object = categories;
        int n = ((String[])object).length;
        for (int i = 0; i < n; ++i) {
            String cat;
            String fcat = cat = object[i];
            int tw = Minecraft.getInstance().font.width(cat) + 10;
            boolean isActive = cat.equals(this.activeCategory);
            this.addRenderableWidget(Button.builder(Component.literal((String)((isActive ? "\u00a7l[" : " ") + cat + (isActive ? "]\u00a7r" : " "))), btn -> {
                this.saveFloatingInputs();
                this.activeCategory = fcat;
                this.rebuildWidgets();
            }).bounds(tabX, cy + 5, Math.max(tw, 40), 16).build());
            tabX += Math.max(tw, 40) + 2;
        }
        this.timeoutInput = null;
        this.lightningAmountInput = null;
        this.particleCountInput = null;
        this.particleSpeedInput = null;
        this.volumeInput = null;
        this.pitchInput = null;
        switch (this.activeCategory) {
            case "\u901a\u7528": {
                this.buildGeneralSettings(cx, cy, row);
                break;
            }
            case "\u95ea\u7535": {
                this.buildLightningSettings(cx, cy, row);
                break;
            }
            case "\u7c92\u5b50": {
                this.buildParticleSettings(cx, cy, row);
                break;
            }
            case "\u97f3\u6548": {
                this.buildSoundSettings(cx, cy, row);
                break;
            }
            case "\u989d\u5916": {
                this.buildExtraSettings(cx, cy, row);
                break;
            }
            case "\u7740\u8272\u5668": {
                this.buildShaderSettings(cx, cy, row);
            }
        }
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u8fd4\u56de\u4e3b\u83dc\u5355"), btn -> {
            this.saveAllNow();
            Minecraft.getInstance().setScreen(new ClickGuiScreen());
        }).bounds(cx + 50, cy + 255 - 28, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u91cd\u7f6e\u4e3a\u9ed8\u8ba4"), btn -> {
            KillFXConfig cfg = KillFXConfig.getInstance();
            cfg.useLightning = true;
            cfg.lightningAmount = 1;
            cfg.useLightningSound = true;
            cfg.useParticles = true;
            cfg.particleCategory = "Magic";
            cfg.magicParticle = "END_ROD";
            cfg.particleShape = "Burst";
            cfg.particleCount = 40;
            cfg.particleSpeed = 0.2;
            cfg.useSound = true;
            cfg.soundGroup = "Combat";
            cfg.combatSound = "THUNDER";
            cfg.volume = 1.0;
            cfg.pitch = 1.0;
            cfg.useFirework = false;
            cfg.useExplosion = false;
            cfg.useShader = false;
            cfg.shaderType = "\u65e0";
            cfg.shaderIntensity = 1.0;
            cfg.shaderDuration = 20;
            cfg.blackholeScale = 1.0;
            cfg.crystalStyle = "\u57fa\u7840\u6676\u4f53";
            cfg.crystalTintColor = "88CCFF";
            cfg.crystalRadius = 1.0;
            cfg.crystalGlowIntensity = 0.8;
            cfg.crystalRotationSpeed = 1.5;
            cfg.crystalPulse = true;
            cfg.onlyTargeted = true;
            cfg.targetTimeout = 3.5;
            KillFXConfig.save();
            for (Map<String, String> cache : this.floatingValues.values()) {
                cache.clear();
            }
            this.rebuildWidgets();
        }).bounds(cx + 160, cy + 255 - 28, 90, 20).build());
    }

    private void buildGeneralSettings(int cx, int cy, int row) {
        this.addToggle(row, "\u529f\u80fd\u5f00\u5173", this.cfg.enabled, v -> {
            this.cfg.enabled = v;
        });
        this.addToggle(row += 24, "\u4ec5\u9650\u653b\u51fb\u76ee\u6807", this.cfg.onlyTargeted, v -> {
            this.cfg.onlyTargeted = v;
        });
        this.addLabeledInput(row += 24, "\u8bb0\u5fc6\u65f6\u95f4(\u79d2)", this.loadFloatingInput("timeout", String.valueOf(this.cfg.targetTimeout)), box -> {
            this.timeoutInput = box;
            box.setMaxLength(5);
        });
    }

    private void buildLightningSettings(int cx, int cy, int row) {
        this.addToggle(row, "\u542f\u7528\u95ea\u7535", this.cfg.useLightning, v -> {
            this.cfg.useLightning = v;
        });
        this.addLabeledInput(row += 24, "\u95ea\u7535\u6570\u91cf", this.loadFloatingInput("lightningAmount", String.valueOf(this.cfg.lightningAmount)), box -> {
            this.lightningAmountInput = box;
            box.setMaxLength(2);
        });
        this.addToggle(row += 24, "\u95ea\u7535\u97f3\u6548", this.cfg.useLightningSound, v -> {
            this.cfg.useLightningSound = v;
        });
    }

    private void buildParticleSettings(int cx, int cy, int row) {
        this.addToggle(row, "\u542f\u7528\u7c92\u5b50", this.cfg.useParticles, v -> {
            this.cfg.useParticles = v;
        });
        this.drawLabel("\u7c92\u5b50\u5206\u7c7b:", cx, row += 24);
        int catStartX = cx + 100;
        int catBtnW = 38;
        int catBtnGap = 40;
        int catRowY = row;
        for (int i = 0; i < PARTICLE_CATEGORIES.length; ++i) {
            String[] cat = PARTICLE_CATEGORIES[i];
            String cnName = cat[0];
            String enKey = cat[1];
            boolean active = this.cfg.particleCategory.equals(enKey);
            int col = i % 3;
            int rowOffset = i / 3 * 18;
            this.addRenderableWidget(Button.builder(Component.literal((String)(active ? "\u00a7l[" + cnName + "]\u00a7r" : cnName)), btn -> {
                this.cfg.particleCategory = enKey;
                KillFXConfig.save();
                this.rebuildWidgets();
            }).bounds(catStartX + col * catBtnGap, catRowY + rowOffset, catBtnW, 16).build());
        }
        int catRows = (PARTICLE_CATEGORIES.length + 2) / 3;
        this.drawLabel("\u5177\u4f53\u7c92\u5b50:", cx, row += 20 + (catRows - 1) * 18);
        String currentParticle = this.getCurrentParticleField();
        String displayName = PARTICLE_ALIAS.getOrDefault(currentParticle, currentParticle);
        this.addRenderableWidget(Button.builder(Component.literal((String)displayName), btn -> {
            this.cycleParticle();
            KillFXConfig.save();
            this.rebuildWidgets();
        }).bounds(cx + 100, row, 80, 20).build());
        this.drawLabel("\u7c92\u5b50\u5f62\u72b6:", cx, row += 24);
        Map.Entry[] shapeEntries = SHAPE_ALIAS.entrySet().toArray(new Map.Entry[0]);
        int shapeStartX = cx + 100;
        int shapeBtnW = 36;
        int shapeBtnGap = 38;
        int shapeRowY = row;
        for (int i = 0; i < shapeEntries.length; ++i) {
            Map.Entry entry = shapeEntries[i];
            String enKey = (String)entry.getKey();
            String cnName = (String)entry.getValue();
            boolean active = this.cfg.particleShape.equals(enKey);
            int col = i % 4;
            int rowOffset = i / 4 * 18;
            this.addRenderableWidget(Button.builder(Component.literal((String)(active ? "\u00a7l[" + cnName + "]\u00a7r" : cnName)), btn -> {
                this.cfg.particleShape = enKey;
                KillFXConfig.save();
                this.rebuildWidgets();
            }).bounds(shapeStartX + col * shapeBtnGap, shapeRowY + rowOffset, shapeBtnW, 16).build());
        }
        int shapeRows = (shapeEntries.length + 3) / 4;
        this.addLabeledInput(row += 20 + (shapeRows - 1) * 18, "\u7c92\u5b50\u6570\u91cf", this.loadFloatingInput("particleCount", String.valueOf(this.cfg.particleCount)), box -> {
            this.particleCountInput = box;
            box.setMaxLength(4);
        });
        this.addLabeledInput(row += 24, "\u7c92\u5b50\u901f\u5ea6", this.loadFloatingInput("particleSpeed", String.valueOf(this.cfg.particleSpeed)), box -> {
            this.particleSpeedInput = box;
            box.setMaxLength(5);
        });
    }

    private void buildSoundSettings(int cx, int cy, int row) {
        this.addToggle(row, "\u542f\u7528\u97f3\u6548", this.cfg.useSound, v -> {
            this.cfg.useSound = v;
        });
        this.drawLabel("\u97f3\u6548\u5206\u7c7b:", cx, row += 24);
        int btnX = cx + 100;
        for (Map.Entry<String, String> entry : SOUND_GROUP_ALIAS.entrySet()) {
            String enKey = entry.getKey();
            String cnName = entry.getValue();
            boolean active = this.cfg.soundGroup.equals(enKey);
            this.addRenderableWidget(Button.builder(Component.literal((String)(active ? "\u00a7l[" + cnName + "]\u00a7r" : cnName)), btn -> {
                this.cfg.soundGroup = enKey;
                KillFXConfig.save();
                this.rebuildWidgets();
            }).bounds(btnX, row, 36, 16).build());
            btnX += 38;
        }
        this.drawLabel("\u5177\u4f53\u97f3\u6548:", cx, row += 20);
        String currentSound = this.getCurrentSoundField();
        String soundDisplay = SOUND_ALIAS.getOrDefault(currentSound, currentSound);
        this.addRenderableWidget(Button.builder(Component.literal((String)soundDisplay), btn -> {
            this.cycleSound();
            KillFXConfig.save();
            this.rebuildWidgets();
        }).bounds(cx + 100, row, 90, 20).build());
        this.addLabeledInput(row += 24, "\u97f3\u91cf", this.loadFloatingInput("volume", String.valueOf(this.cfg.volume)), box -> {
            this.volumeInput = box;
            box.setMaxLength(5);
        });
        this.addLabeledInput(row += 24, "\u97f3\u8c03", this.loadFloatingInput("pitch", String.valueOf(this.cfg.pitch)), box -> {
            this.pitchInput = box;
            box.setMaxLength(5);
        });
    }

    private void buildExtraSettings(int cx, int cy, int row) {
        this.addToggle(row, "\u751f\u6210\u70df\u82b1", this.cfg.useFirework, v -> {
            this.cfg.useFirework = v;
        });
        this.addToggle(row += 24, "\u7206\u70b8\u70df\u96fe", this.cfg.useExplosion, v -> {
            this.cfg.useExplosion = v;
        });
    }

    private void buildShaderSettings(int cx, int cy, int row) {
        this.addToggle(row, "\u542f\u7528\u7740\u8272\u5668", this.cfg.useShader, v -> {
            this.cfg.useShader = v;
        });
        this.drawLabel("\u7279\u6548\u7c7b\u578b:", cx, row += 22);
        String[] types = new String[]{"\u9ed1\u6d1e", "\u6c34\u6676", "\u5929\u5149\u5149\u675f", "\u5929\u5149\u73af", "\u8d85\u65b0\u661f", "\u5149\u7ebf\u7206\u53d1"};
        int btW = 56;
        int typeRows = (types.length + 1) / 2;
        for (int i = 0; i < types.length; ++i) {
            String fType = types[i];
            boolean isActive = fType.equals(this.cfg.shaderType);
            int bx = cx + 130 + i % 2 * (btW + 4);
            int by = row + i / 2 * 18;
            this.addRenderableWidget(Button.builder(Component.literal((String)(isActive ? "\u25b6" + fType : fType)), btn -> {
                this.cfg.shaderType = fType;
                KillFXConfig.save();
                this.rebuildWidgets();
            }).bounds(bx, by, btW, 16).build());
        }
        row += typeRows * 18 + 4;
        if ("\u9ed1\u6d1e".equals(this.cfg.shaderType)) {
            this.buildBlackholeSettings(cx, row);
        } else if ("\u6c34\u6676".equals(this.cfg.shaderType)) {
            this.buildCrystalSettings(cx, row);
        } else if ("\u5929\u5149\u5149\u675f".equals(this.cfg.shaderType)) {
            this.buildBeamRingSettings(cx, row);
        } else if ("\u5929\u5149\u73af".equals(this.cfg.shaderType)) {
            this.buildBeamRingSettings(cx, row);
        } else if ("\u8d85\u65b0\u661f".equals(this.cfg.shaderType) || "\u5149\u7ebf\u7206\u53d1".equals(this.cfg.shaderType)) {
            this.buildBeamRingSettings(cx, row);
        }
    }

    private void buildBlackholeSettings(int cx, int row) {
        int[] dur = new int[]{this.cfg.shaderDuration};
        this.drawLabel("\u6301\u7eedTick:", cx, row);
        this.addRenderableWidget(Button.builder(Component.literal((String)(dur[0] + "t")), btn -> {
            dur[0] = dur[0] >= 80 ? 5 : dur[0] + 5;
            this.cfg.shaderDuration = dur[0];
            KillFXConfig.save();
            this.rebuildWidgets();
        }).bounds(cx + 135, row, 45, 18).build());
        double[] sc = new double[]{this.cfg.blackholeScale};
        this.drawLabel("\u9ed1\u6d1e\u5927\u5c0f:", cx, row += 22);
        this.addRenderableWidget(Button.builder(Component.literal((String)String.format("%.1f", sc[0])), btn -> {
            sc[0] = sc[0] + 0.25;
            if (sc[0] > 3.0) {
                sc[0] = 0.5;
            }
            this.cfg.blackholeScale = sc[0];
            KillFXConfig.save();
            this.rebuildWidgets();
        }).bounds(cx + 135, row, 45, 18).build());
    }

    private void buildCrystalSettings(int cx, int row) {
        int colorInt;
        this.drawLabel("\u6301\u7eedTick:", cx, row);
        this.addRenderableWidget(Button.builder(Component.literal((String)(this.cfg.shaderDuration + "t")), btn -> {
            this.cfg.shaderDuration = this.cfg.shaderDuration >= 80 ? 5 : this.cfg.shaderDuration + 5;
            KillFXConfig.save();
            this.rebuildWidgets();
        }).bounds(cx + 135, row, 45, 18).build());
        this.drawLabel("\u98ce\u683c:", cx, row += 22);
        String[] styles = new String[]{"\u57fa\u7840\u6676\u4f53", "\u53d1\u5149", "\u73bb\u7483\u6298\u5c04", "\u6781\u5149"};
        int sx = cx + 130;
        for (String s : styles) {
            boolean act = s.equals(this.cfg.crystalStyle);
            this.addRenderableWidget(Button.builder(Component.literal((String)(act ? "\u25b6" + s : s)), btn -> {
                this.cfg.crystalStyle = s;
                KillFXConfig.save();
                this.rebuildWidgets();
            }).bounds(sx, row, 40, 16).build());
            sx += 42;
        }
        this.drawLabel("\u8272\u8c03:", cx, row += 22);
        try {
            colorInt = Integer.parseInt(this.cfg.crystalTintColor, 16);
        }
        catch (Exception e) {
            colorInt = 0x88CCFF;
        }
        int finalColorInt = colorInt;
        this.addRenderableWidget(Button.builder(Component.literal((String)("\u00a7" + (this.cfg.crystalTintColor.length() == 6 ? "a\u25cf" : "7?"))), btn -> {
            int cx2 = (this.width - 300) / 2;
            int cy2 = (this.height - 255) / 2;
            this.colorWheelPicker.open(cx2 + 150, cy2 + 127);
        }).bounds(cx + 85, row, 18, 16).build());
        this.addRenderableWidget(Button.builder(Component.literal((String)("#" + this.cfg.crystalTintColor)), btn -> {
            int cx2 = (this.width - 300) / 2;
            int cy2 = (this.height - 255) / 2;
            this.colorWheelPicker.open(cx2 + 150, cy2 + 127);
        }).bounds(cx + 105, row, 65, 16).build());
        this.drawLabel("\u534a\u5f84:", cx, row += 22);
        double[] radius = new double[]{this.cfg.crystalRadius};
        this.addRenderableWidget(Button.builder(Component.literal((String)String.format("%.1f", radius[0])), btn -> {
            radius[0] = radius[0] + 0.25;
            if (radius[0] > 3.0) {
                radius[0] = 0.5;
            }
            this.cfg.crystalRadius = radius[0];
            KillFXConfig.save();
            this.rebuildWidgets();
        }).bounds(cx + 55, row, 40, 16).build());
        this.drawLabel("\u53d1\u5149:", cx + 130, row);
        double[] glow = new double[]{this.cfg.crystalGlowIntensity};
        this.addRenderableWidget(Button.builder(Component.literal((String)String.format("%.1f", glow[0])), btn -> {
            glow[0] = glow[0] + 0.2;
            if (glow[0] > 2.0) {
                glow[0] = 0.0;
            }
            this.cfg.crystalGlowIntensity = glow[0];
            KillFXConfig.save();
            this.rebuildWidgets();
        }).bounds(cx + 175, row, 40, 16).build());
        this.drawLabel("\u8f6c\u901f:", cx, row += 22);
        double[] speed = new double[]{this.cfg.crystalRotationSpeed};
        this.addRenderableWidget(Button.builder(Component.literal((String)String.format("%.1f", speed[0])), btn -> {
            speed[0] = speed[0] + 0.5;
            if (speed[0] > 5.0) {
                speed[0] = 0.0;
            }
            this.cfg.crystalRotationSpeed = speed[0];
            KillFXConfig.save();
            this.rebuildWidgets();
        }).bounds(cx + 55, row, 40, 16).build());
        if ("\u57fa\u7840\u6676\u4f53".equals(this.cfg.crystalStyle) || "\u53d1\u5149".equals(this.cfg.crystalStyle)) {
            this.addToggleSimple(cx + 130, row, "\u8109\u51b2", this.cfg.crystalPulse, v -> {
                this.cfg.crystalPulse = v;
            });
        }
    }

    private void buildBeamRingSettings(int cx, int row) {
        this.drawLabel("\u6301\u7eedTick:", cx, row);
        this.addRenderableWidget(Button.builder(Component.literal((String)(this.cfg.shaderDuration + "t")), btn -> {
            this.cfg.shaderDuration = this.cfg.shaderDuration >= 80 ? 5 : this.cfg.shaderDuration + 5;
            KillFXConfig.save();
            this.rebuildWidgets();
        }).bounds(cx + 135, row, 45, 18).build());
        double[] sz = new double[]{this.cfg.shaderIntensity};
        this.drawLabel("\u5927\u5c0f:", cx, row += 22);
        this.addRenderableWidget(Button.builder(Component.literal((String)String.format("%.1f", sz[0])), btn -> {
            sz[0] = sz[0] + 0.25;
            if (sz[0] > 3.0) {
                sz[0] = 0.5;
            }
            this.cfg.shaderIntensity = sz[0];
            KillFXConfig.save();
            this.rebuildWidgets();
        }).bounds(cx + 135, row, 45, 18).build());
    }

    private void addToggleSimple(int x, int y, String label, boolean current, Consumer<Boolean> setter) {
        this.drawLabel(label, x, y);
        this.addRenderableWidget(Button.builder(Component.literal((String)(current ? "\u5f00" : "\u5173")), btn -> {
            boolean nv = !current;
            setter.accept(nv);
            KillFXConfig.save();
            btn.setMessage(Component.literal((String)(nv ? "\u5f00" : "\u5173")));
        }).bounds(x + 35, y, 30, 16).build());
    }

    private void addToggle(int row, String label, boolean currentValue, Consumer<Boolean> setter) {
        int cx = (this.width - 300) / 2;
        this.drawLabel(label, cx, row);
        this.addRenderableWidget(Button.builder(Component.literal((String)(currentValue ? "\u5f00" : "\u5173")), btn -> {
            boolean newVal = !currentValue;
            setter.accept(newVal);
            KillFXConfig.save();
            btn.setMessage(Component.literal((String)(newVal ? "\u5f00" : "\u5173")));
            this.rebuildWidgets();
        }).bounds(cx + 135, row, 40, 20).build());
    }

    private void addLabeledInput(int row, String label, String cachedValue, Consumer<EditBox> setter) {
        int cx = (this.width - 300) / 2;
        this.drawLabel(label, cx, row);
        EditBox box = new EditBox(this.font, cx + 135, row, 60, 18, Component.literal((String)""));
        box.setValue(cachedValue);
        box.setMaxLength(10);
        this.addRenderableWidget(box);
        setter.accept(box);
    }

    private void drawLabel(String text, int cx, int row) {
    }

    private String getCurrentParticleField() {
        return switch (this.cfg.particleCategory) {
            case "Combat" -> this.cfg.combatParticle;
            case "Magic" -> this.cfg.magicParticle;
            case "Fire" -> this.cfg.fireParticle;
            case "Nature" -> this.cfg.natureParticle;
            case "Update121" -> this.cfg.updateParticle;
            case "Misc" -> this.cfg.miscParticle;
            default -> this.cfg.magicParticle;
        };
    }

    private void cycleParticle() {
        for (int ci = 0; ci < PARTICLE_CAT_KEYS.length; ++ci) {
            if (!PARTICLE_CAT_KEYS[ci].equals(this.cfg.particleCategory)) continue;
            String[] arr = ALL_PARTICLES[ci];
            String currentField = this.getCurrentParticleField();
            int idx = KillFXConfigScreen.indexOf(arr, currentField);
            String nextParticle = arr[(idx + 1) % arr.length];
            switch (ci) {
                case 0: {
                    this.cfg.combatParticle = nextParticle;
                    break;
                }
                case 1: {
                    this.cfg.magicParticle = nextParticle;
                    break;
                }
                case 2: {
                    this.cfg.fireParticle = nextParticle;
                    break;
                }
                case 3: {
                    this.cfg.natureParticle = nextParticle;
                    break;
                }
                case 4: {
                    this.cfg.updateParticle = nextParticle;
                    break;
                }
                case 5: {
                    this.cfg.miscParticle = nextParticle;
                }
            }
            break;
        }
    }

    private String getCurrentSoundField() {
        return switch (this.cfg.soundGroup) {
            case "Combat" -> this.cfg.combatSound;
            case "Magic" -> this.cfg.magicSound;
            case "Creature" -> this.cfg.creatureSound;
            case "Fun" -> this.cfg.funSound;
            default -> this.cfg.combatSound;
        };
    }

    private void cycleSound() {
        for (int ci = 0; ci < SOUND_GROUP_KEYS.length; ++ci) {
            if (!SOUND_GROUP_KEYS[ci].equals(this.cfg.soundGroup)) continue;
            String[] arr = ALL_SOUNDS[ci];
            String currentField = this.getCurrentSoundField();
            int idx = KillFXConfigScreen.indexOf(arr, currentField);
            String nextSound = arr[(idx + 1) % arr.length];
            switch (ci) {
                case 0: {
                    this.cfg.combatSound = nextSound;
                    break;
                }
                case 1: {
                    this.cfg.magicSound = nextSound;
                    break;
                }
                case 2: {
                    this.cfg.creatureSound = nextSound;
                    break;
                }
                case 3: {
                    this.cfg.funSound = nextSound;
                }
            }
            break;
        }
    }

    private static int indexOf(String[] arr, String val) {
        for (int i = 0; i < arr.length; ++i) {
            if (!arr[i].equals(val)) continue;
            return i;
        }
        return 0;
    }

    private void saveAllNow() {
        this.saveFloatingInputs();
        this.syncFloatingToCfg();
        KillFXConfig.save();
    }

    private void syncFloatingToCfg() {
        try {
            Map<String, String> soundCache;
            Map<String, String> particleCache;
            String v;
            Map<String, String> lightCache;
            String v2;
            Map<String, String> genCache = this.floatingValues.get("\u901a\u7528");
            if (genCache != null && (v2 = genCache.get("timeout")) != null) {
                this.cfg.targetTimeout = Double.parseDouble(v2);
            }
            if ((lightCache = this.floatingValues.get("\u95ea\u7535")) != null && (v = lightCache.get("lightningAmount")) != null) {
                this.cfg.lightningAmount = Integer.parseInt(v);
            }
            if ((particleCache = this.floatingValues.get("\u7c92\u5b50")) != null) {
                String v3 = particleCache.get("particleCount");
                if (v3 != null) {
                    this.cfg.particleCount = Integer.parseInt(v3);
                }
                if ((v3 = particleCache.get("particleSpeed")) != null) {
                    this.cfg.particleSpeed = Double.parseDouble(v3);
                }
            }
            if ((soundCache = this.floatingValues.get("\u97f3\u6548")) != null) {
                String v4 = soundCache.get("volume");
                if (v4 != null) {
                    this.cfg.volume = Double.parseDouble(v4);
                }
                if ((v4 = soundCache.get("pitch")) != null) {
                    this.cfg.pitch = Double.parseDouble(v4);
                }
            }
        }
        catch (NumberFormatException numberFormatException) {
            // ignored
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int cx = (this.width - 300) / 2;
        int cy = (this.height - 255) / 2;
        if (mouseX >= cx && mouseX <= (cx + 300) && mouseY >= cy && mouseY <= (cy + 255)) {
            int newScroll = this.scrollOffset - (int)(delta * 16.0);
            int maxScroll = Math.max(0, this.contentMaxRow - (cy + 255 - 60));
            this.scrollOffset = Math.max(0, Math.min(newScroll, maxScroll));
            this.rebuildWidgets();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        String[][] labelDefs;
        String[][] stringArrayArray;
        this.renderBackground(guiGraphics);
        int cx = (this.width - 300) / 2;
        int cy = (this.height - 255) / 2;
        GuiRenderHelper.drawPanelBackground(guiGraphics, cx, cy, 300, 255, false);
        guiGraphics.drawString(this.font, "\u51fb\u6740\u7279\u6548\u914d\u7f6e - " + this.activeCategory, cx + 10, cy + 25, 0xFFFFFF);
        guiGraphics.enableScissor(cx + 2, cy + 35, cx + 300 - 2, cy + 255 - 32);
        int row = cy + 35 - this.scrollOffset;
        switch (this.activeCategory) {
            case "\u901a\u7528": {
                String[][] stringArrayArray2 = new String[3][];
                stringArrayArray2[0] = new String[]{"\u529f\u80fd\u5f00\u5173:", "35"};
                stringArrayArray2[1] = new String[]{"\u4ec5\u9650\u653b\u51fb\u76ee\u6807:", "59"};
                stringArrayArray = stringArrayArray2;
                stringArrayArray2[2] = new String[]{"\u8bb0\u5fc6\u65f6\u95f4(\u79d2):", "83"};
                break;
            }
            case "\u95ea\u7535": {
                String[][] stringArrayArray3 = new String[3][];
                stringArrayArray3[0] = new String[]{"\u542f\u7528\u95ea\u7535:", "35"};
                stringArrayArray3[1] = new String[]{"\u95ea\u7535\u6570\u91cf:", "59"};
                stringArrayArray = stringArrayArray3;
                stringArrayArray3[2] = new String[]{"\u95ea\u7535\u97f3\u6548:", "83"};
                break;
            }
            case "\u7c92\u5b50": {
                String[][] stringArrayArray4 = new String[6][];
                stringArrayArray4[0] = new String[]{"\u542f\u7528\u7c92\u5b50:", "35"};
                stringArrayArray4[1] = new String[]{"\u7c92\u5b50\u5206\u7c7b:", "59"};
                stringArrayArray4[2] = new String[]{"\u5177\u4f53\u7c92\u5b50:", "95"};
                stringArrayArray4[3] = new String[]{"\u7c92\u5b50\u5f62\u72b6:", "119"};
                stringArrayArray4[4] = new String[]{"\u7c92\u5b50\u6570\u91cf:", "171"};
                stringArrayArray = stringArrayArray4;
                stringArrayArray4[5] = new String[]{"\u7c92\u5b50\u901f\u5ea6:", "195"};
                break;
            }
            case "\u97f3\u6548": {
                String[][] stringArrayArray5 = new String[5][];
                stringArrayArray5[0] = new String[]{"\u542f\u7528\u97f3\u6548:", "35"};
                stringArrayArray5[1] = new String[]{"\u97f3\u6548\u5206\u7c7b:", "55"};
                stringArrayArray5[2] = new String[]{"\u5177\u4f53\u97f3\u6548:", "75"};
                stringArrayArray5[3] = new String[]{"\u97f3\u91cf:", "99"};
                stringArrayArray = stringArrayArray5;
                stringArrayArray5[4] = new String[]{"\u97f3\u8c03:", "123"};
                break;
            }
            case "\u989d\u5916": {
                String[][] stringArrayArray6 = new String[2][];
                stringArrayArray6[0] = new String[]{"\u751f\u6210\u70df\u82b1:", "35"};
                stringArrayArray = stringArrayArray6;
                stringArrayArray6[1] = new String[]{"\u7206\u70b8\u70df\u96fe:", "59"};
                break;
            }
            case "\u7740\u8272\u5668": {
                String type = this.cfg.shaderType;
                if ("\u9ed1\u6d1e".equals(type)) {
                    String[][] stringArrayArray7 = new String[4][];
                    stringArrayArray7[0] = new String[]{"\u542f\u7528\u7740\u8272\u5668:", "35"};
                    stringArrayArray7[1] = new String[]{"\u7279\u6548\u7c7b\u578b:", "57"};
                    stringArrayArray7[2] = new String[]{"\u6301\u7eedTick:", "97"};
                    stringArrayArray = stringArrayArray7;
                    stringArrayArray7[3] = new String[]{"\u9ed1\u6d1e\u5927\u5c0f:", "119"};
                    break;
                }
                if ("\u6c34\u6676".equals(type)) {
                    String[][] stringArrayArray8 = new String[7][];
                    stringArrayArray8[0] = new String[]{"\u542f\u7528\u7740\u8272\u5668:", "35"};
                    stringArrayArray8[1] = new String[]{"\u7279\u6548\u7c7b\u578b:", "57"};
                    stringArrayArray8[2] = new String[]{"\u6301\u7eedTick:", "97"};
                    stringArrayArray8[3] = new String[]{"\u98ce\u683c:", "119"};
                    stringArrayArray8[4] = new String[]{"\u8272\u8c03:", "141"};
                    stringArrayArray8[5] = new String[]{"\u534a\u5f84/\u53d1\u5149:", "163"};
                    stringArrayArray = stringArrayArray8;
                    stringArrayArray8[6] = new String[]{"\u8f6c\u901f/\u8109\u51b2:", "185"};
                    break;
                }
                if ("\u5929\u5149\u5149\u675f".equals(type) || "\u5929\u5149\u73af".equals(type)) {
                    String[][] stringArrayArray9 = new String[4][];
                    stringArrayArray9[0] = new String[]{"\u542f\u7528\u7740\u8272\u5668:", "35"};
                    stringArrayArray9[1] = new String[]{"\u7279\u6548\u7c7b\u578b:", "57"};
                    stringArrayArray9[2] = new String[]{"\u6301\u7eedTick:", "97"};
                    stringArrayArray = stringArrayArray9;
                    stringArrayArray9[3] = new String[]{"\u5927\u5c0f:", "119"};
                    break;
                }
                if ("\u8d85\u65b0\u661f".equals(type) || "\u5149\u7ebf\u7206\u53d1".equals(type)) {
                    String[][] stringArrayArray10 = new String[4][];
                    stringArrayArray10[0] = new String[]{"\u542f\u7528\u7740\u8272\u5668:", "35"};
                    stringArrayArray10[1] = new String[]{"\u7279\u6548\u7c7b\u578b:", "57"};
                    stringArrayArray10[2] = new String[]{"\u6301\u7eedTick:", "97"};
                    stringArrayArray = stringArrayArray10;
                    stringArrayArray10[3] = new String[]{"\u5927\u5c0f:", "119"};
                    break;
                }
                String[][] stringArrayArray11 = new String[1][];
                stringArrayArray = stringArrayArray11;
                stringArrayArray11[0] = new String[]{"\u542f\u7528\u7740\u8272\u5668:", "35"};
                break;
            }
            default: {
                stringArrayArray = new String[][]{};
            }
        }
        for (String[] pair : labelDefs = stringArrayArray) {
            int yRow = cy + Integer.parseInt(pair[1]) - this.scrollOffset;
            if (yRow + 4 < cy + 35 || yRow >= cy + 255 - 35) continue;
            guiGraphics.drawString(this.font, pair[0], cx + 10, yRow + 4, 0xAAAAAA);
        }
        guiGraphics.disableScissor();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.colorWheelPicker.render(guiGraphics, mouseX, mouseY);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.colorWheelPicker.isOpen()) {
            if (this.colorWheelPicker.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            this.rebuildWidgets();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void onClose() {
        this.saveAllNow();
        this.minecraft.setScreen(new ClickGuiScreen());
    }

    static {
        PARTICLE_ALIAS.put("DAMAGE_INDICATOR", "\u4f24\u5bb3\u6307\u793a");
        PARTICLE_ALIAS.put("CRIT", "\u66b4\u51fb");
        PARTICLE_ALIAS.put("ENCHANTED_HIT", "\u9644\u9b54\u653b\u51fb");
        PARTICLE_ALIAS.put("SWEEP_ATTACK", "\u6a2a\u626b");
        PARTICLE_ALIAS.put("EXPLOSION", "\u7206\u70b8");
        PARTICLE_ALIAS.put("EXPLOSION_EMITTER", "\u7206\u70b8\u53d1\u5c04");
        PARTICLE_ALIAS.put("SONIC_BOOM", "\u97f3\u7206");
        PARTICLE_ALIAS.put("TOTEM_OF_UNDYING", "\u4e0d\u6b7b\u56fe\u817e");
        PARTICLE_ALIAS.put("FIREWORK", "\u70df\u82b1");
        PARTICLE_ALIAS.put("EGG_CRACK", "\u86cb\u88c2");
        PARTICLE_ALIAS.put("WITCH", "\u5973\u5deb");
        PARTICLE_ALIAS.put("END_ROD", "\u672b\u5730\u70db");
        PARTICLE_ALIAS.put("PORTAL", "\u4f20\u9001\u95e8");
        PARTICLE_ALIAS.put("ENCHANT", "\u9644\u9b54");
        PARTICLE_ALIAS.put("NAUTILUS", "\u9e66\u9e49\u87ba");
        PARTICLE_ALIAS.put("ELDER_GUARDIAN", "\u8fdc\u53e4\u5b88\u536b");
        PARTICLE_ALIAS.put("SCULK_CHARGE_POP", "\u5e7d\u533f\u7206\u88c2");
        PARTICLE_ALIAS.put("SOUL", "\u7075\u9b42");
        PARTICLE_ALIAS.put("GLOW_SQUID_INK", "\u53d1\u5149\u58a8\u6c41");
        PARTICLE_ALIAS.put("FLAME", "\u706b\u7130");
        PARTICLE_ALIAS.put("SOUL_FIRE_FLAME", "\u7075\u9b42\u706b");
        PARTICLE_ALIAS.put("SMALL_FLAME", "\u5c0f\u706b\u82d7");
        PARTICLE_ALIAS.put("LAVA", "\u7194\u5ca9");
        PARTICLE_ALIAS.put("LARGE_SMOKE", "\u6d53\u70df");
        PARTICLE_ALIAS.put("SMOKE", "\u70df\u96fe");
        PARTICLE_ALIAS.put("CAMPFIRE_COSY_SMOKE", "\u8425\u706b\u70df");
        PARTICLE_ALIAS.put("CAMPFIRE_SIGNAL_SMOKE", "\u4fe1\u53f7\u70df");
        PARTICLE_ALIAS.put("GLOW", "\u8367\u5149");
        PARTICLE_ALIAS.put("WAX_ON", "\u4e0a\u8721");
        PARTICLE_ALIAS.put("WAX_OFF", "\u8131\u8721");
        PARTICLE_ALIAS.put("SCRAPE", "\u522e\u524a");
        PARTICLE_ALIAS.put("ELECTRIC_SPARK", "\u7535\u706b\u82b1");
        PARTICLE_ALIAS.put("HEART", "\u7231\u5fc3");
        PARTICLE_ALIAS.put("CLOUD", "\u4e91");
        PARTICLE_ALIAS.put("RAIN", "\u96e8");
        PARTICLE_ALIAS.put("SNOWFLAKE", "\u96ea\u82b1");
        PARTICLE_ALIAS.put("ITEM_SLIME", "\u53f2\u83b1\u59c6");
        PARTICLE_ALIAS.put("BUBBLE", "\u6c14\u6ce1");
        PARTICLE_ALIAS.put("BUBBLE_COLUMN_UP", "\u6c14\u6ce1\u67f1\u4e0a");
        PARTICLE_ALIAS.put("CURRENT_DOWN", "\u6c34\u6d41\u4e0b");
        PARTICLE_ALIAS.put("BUBBLE_POP", "\u6c14\u6ce1\u7834");
        PARTICLE_ALIAS.put("SPLASH", "\u6e85\u6c34");
        PARTICLE_ALIAS.put("FISHING", "\u9493\u9c7c");
        PARTICLE_ALIAS.put("DOLPHIN", "\u6d77\u8c5a");
        PARTICLE_ALIAS.put("UNDERWATER", "\u6c34\u4e0b");
        PARTICLE_ALIAS.put("NOTE", "\u97f3\u7b26");
        PARTICLE_ALIAS.put("CHERRY_LEAVES", "\u6a31\u82b1");
        PARTICLE_ALIAS.put("SPORE_BLOSSOM_AIR", "\u5b62\u5b50\u82b1");
        PARTICLE_ALIAS.put("WHITE_ASH", "\u767d\u7070");
        PARTICLE_ALIAS.put("WARPED_SPORE", "\u8be1\u5f02\u5b62\u5b50");
        PARTICLE_ALIAS.put("CRIMSON_SPORE", "\u7eef\u7ea2\u5b62\u5b50");
        PARTICLE_ALIAS.put("DRAGON_BREATH", "\u9f99\u606f");
        PARTICLE_ALIAS.put("FLASH", "\u95ea\u5149");
        PARTICLE_ALIAS.put("POOF", "\u5657");
        PARTICLE_ALIAS.put("SPIT", "\u53e3\u6c34");
        PARTICLE_ALIAS.put("ASH", "\u7070\u70ec");
        PARTICLE_ALIAS.put("MYCELIUM", "\u83cc\u4e1d");
        PARTICLE_ALIAS.put("SCULK_SOUL", "\u5e7d\u533f\u7075\u9b42");
        PARTICLE_ALIAS.put("HAPPY_VILLAGER", "\u6751\u6c11\u5f00\u5fc3");
        PARTICLE_ALIAS.put("ANGRY_VILLAGER", "\u6751\u6c11\u751f\u6c14");
        PARTICLE_ALIAS.put("SNEEZE", "\u55b7\u568f");
        PARTICLE_ALIAS.put("SQUID_INK", "\u58a8\u6c41");
        SHAPE_ALIAS = new HashMap<String, String>();
        SHAPE_ALIAS.put("Burst", "\u7206\u6563");
        SHAPE_ALIAS.put("Sphere", "\u7403\u4f53");
        SHAPE_ALIAS.put("Spiral", "\u87ba\u65cb");
        SHAPE_ALIAS.put("Column", "\u5149\u67f1");
        SHAPE_ALIAS.put("Halo", "\u5149\u73af");
        SHAPE_ALIAS.put("Heart", "\u7231\u5fc3");
        SHAPE_ALIAS.put("Helix", "\u53cc\u87ba\u65cb");
        SHAPE_ALIAS.put("Star", "\u661f\u5f62");
        SHAPE_ALIAS.put("Ring", "\u5706\u73af");
        SOUND_GROUP_ALIAS = new HashMap<String, String>();
        SOUND_GROUP_ALIAS.put("Combat", "\u6218\u6597");
        SOUND_GROUP_ALIAS.put("Magic", "\u9b54\u6cd5");
        SOUND_GROUP_ALIAS.put("Creature", "\u751f\u7269");
        SOUND_GROUP_ALIAS.put("Fun", "\u8da3\u5473");
        SOUND_ALIAS = new HashMap<String, String>();
        SOUND_ALIAS.put("THUNDER", "\u96f7\u9e23");
        SOUND_ALIAS.put("EXPLODE", "\u7206\u70b8");
        SOUND_ALIAS.put("ANVIL", "\u94c1\u7827");
        SOUND_ALIAS.put("TRIDENT_THUNDER", "\u4e09\u53c9\u621f\u96f7");
        SOUND_ALIAS.put("WITHER_SPAWN", "\u51cb\u7075\u751f\u6210");
        SOUND_ALIAS.put("WITHER_SHOOT", "\u51cb\u7075\u5c04\u51fb");
        SOUND_ALIAS.put("ANCHOR", "\u951a\u6d88\u8017");
        SOUND_ALIAS.put("CRYSTAL", "\u6c34\u6676\u7206\u70b8");
        SOUND_ALIAS.put("BREAK", "\u76fe\u724c\u7834\u788e");
        SOUND_ALIAS.put("CRIT", "\u66b4\u51fb");
        SOUND_ALIAS.put("CROSSBOW_HIT", "\u5f29\u547d\u4e2d");
        SOUND_ALIAS.put("TRIDENT_HIT", "\u4e09\u53c9\u621f\u547d\u4e2d");
        SOUND_ALIAS.put("FIREWORK_BLAST", "\u70df\u82b1\u7206\u70b8");
        SOUND_ALIAS.put("ATK_STRONG", "\u5f3a\u653b\u51fb");
        SOUND_ALIAS.put("ATK_SWEEP", "\u6a2a\u626b\u653b\u51fb");
        SOUND_ALIAS.put("ANCHOR_CHARGE", "\u951a\u5145\u80fd");
        SOUND_ALIAS.put("ANCHOR_SET", "\u951a\u8bbe\u91cd\u751f\u70b9");
        SOUND_ALIAS.put("TOTEM", "\u4e0d\u6b7b\u56fe\u817e");
        SOUND_ALIAS.put("BEACON", "\u4fe1\u6807\u6fc0\u6d3b");
        SOUND_ALIAS.put("CONDUIT", "\u6f6e\u6d8c\u6fc0\u6d3b");
        SOUND_ALIAS.put("PORTAL", "\u4f20\u9001\u95e8");
        SOUND_ALIAS.put("LEVEL_UP", "\u5347\u7ea7");
        SOUND_ALIAS.put("ENCHANT", "\u9644\u9b54");
        SOUND_ALIAS.put("TELEPORT", "\u4f20\u9001");
        SOUND_ALIAS.put("BELL", "\u949f");
        SOUND_ALIAS.put("CHIME", "\u7d2b\u6c34\u6676");
        SOUND_ALIAS.put("RESONATE", "\u7d2b\u6676\u5171\u9e23");
        SOUND_ALIAS.put("ENDER_EYE", "\u672b\u5f71\u4e4b\u773c");
        SOUND_ALIAS.put("EXP_ORB", "\u7ecf\u9a8c\u7403");
        SOUND_ALIAS.put("EVOKER_CAST", "\u5524\u9b54\u8005\u65bd\u6cd5");
        SOUND_ALIAS.put("CONDUIT_ATK", "\u6f6e\u6d8c\u653b\u51fb");
        SOUND_ALIAS.put("DRAGON_FIREBALL", "\u9f99\u606f\u5f39");
        SOUND_ALIAS.put("WARDEN", "\u5faa\u58f0\u5b88\u536b\u543c");
        SOUND_ALIAS.put("WARDEN_HEART", "\u5faa\u58f0\u5fc3\u8df3");
        SOUND_ALIAS.put("DRAGON", "\u672b\u5f71\u9f99\u6b7b");
        SOUND_ALIAS.put("DRAGON_GROWL", "\u672b\u5f71\u9f99\u568e");
        SOUND_ALIAS.put("BLAZE", "\u70c8\u7130\u4eba");
        SOUND_ALIAS.put("GHAST", "\u6076\u9b42");
        SOUND_ALIAS.put("ENDERMAN", "\u672b\u5f71\u4eba");
        SOUND_ALIAS.put("PHANTOM", "\u5e7b\u7ffc");
        SOUND_ALIAS.put("WOLF", "\u72fc\u568e");
        SOUND_ALIAS.put("CAT", "\u732b\u5636");
        SOUND_ALIAS.put("ALLAY_ITEM", "\u60a6\u7075");
        SOUND_ALIAS.put("BEE_STING", "\u871c\u8702\u8707");
        SOUND_ALIAS.put("RAVAGER_ROAR", "\u63a0\u593a\u8005\u543c");
        SOUND_ALIAS.put("BURP", "\u6253\u55dd");
        SOUND_ALIAS.put("PLING", "\u53ee");
        SOUND_ALIAS.put("GOAT", "\u5c71\u7f8a\u5976");
        SOUND_ALIAS.put("NO", "\u6751\u6c11\u5426");
        SOUND_ALIAS.put("YES", "\u6751\u6c11\u662f");
        SOUND_ALIAS.put("EAT", "\u5403");
        SOUND_ALIAS.put("TOAST", "\u6210\u5c31");
        SOUND_ALIAS.put("GLASS", "\u73bb\u7483\u788e");
        SOUND_ALIAS.put("VILLAGER_CELEBRATE", "\u6751\u6c11\u5e86\u795d");
        SOUND_ALIAS.put("VILLAGER_TRADE", "\u6751\u6c11\u4ea4\u6613");
        SOUND_ALIAS.put("BELL_RESONATE", "\u949f\u5171\u9e23");
        SOUND_ALIAS.put("NOTE_BIT", "\u97f3\u7b26-\u4f4e\u97f3");
        SOUND_ALIAS.put("NOTE_BANJO", "\u97f3\u7b26-\u73ed\u5353");
        ALL_PARTICLES = new String[][]{{"DAMAGE_INDICATOR", "CRIT", "ENCHANTED_HIT", "SWEEP_ATTACK", "EXPLOSION", "EXPLOSION_EMITTER", "SONIC_BOOM", "TOTEM_OF_UNDYING", "FIREWORK", "EGG_CRACK"}, {"WITCH", "END_ROD", "PORTAL", "ENCHANT", "NAUTILUS", "ELDER_GUARDIAN", "SCULK_CHARGE_POP", "SOUL", "GLOW_SQUID_INK"}, {"FLAME", "SOUL_FIRE_FLAME", "LAVA", "LARGE_SMOKE", "SMOKE", "CAMPFIRE_COSY_SMOKE", "CAMPFIRE_SIGNAL_SMOKE", "GLOW", "WAX_ON", "WAX_OFF", "SCRAPE", "ELECTRIC_SPARK"}, {"HEART", "CLOUD", "RAIN", "SNOWFLAKE", "ITEM_SLIME", "BUBBLE", "BUBBLE_COLUMN_UP", "CURRENT_DOWN", "BUBBLE_POP", "SPLASH", "FISHING", "DOLPHIN", "UNDERWATER", "NOTE", "CHERRY_LEAVES", "SPORE_BLOSSOM_AIR", "WHITE_ASH", "WARPED_SPORE", "CRIMSON_SPORE"}, {"DRAGON_BREATH", "FLASH", "POOF", "SNOWFLAKE", "SPIT"}, {"ASH", "MYCELIUM", "SCULK_SOUL", "HAPPY_VILLAGER", "ANGRY_VILLAGER", "SNEEZE", "SQUID_INK"}};
        PARTICLE_CAT_KEYS = new String[]{"Combat", "Magic", "Fire", "Nature", "Update121", "Misc"};
        ALL_SOUNDS = new String[][]{{"THUNDER", "EXPLODE", "ANVIL", "TRIDENT_THUNDER", "WITHER_SPAWN", "WITHER_SHOOT", "ANCHOR", "CRYSTAL", "BREAK", "CRIT", "CROSSBOW_HIT", "TRIDENT_HIT", "FIREWORK_BLAST", "ATK_STRONG", "ATK_SWEEP"}, {"ANCHOR_CHARGE", "ANCHOR_SET", "TOTEM", "BEACON", "CONDUIT", "PORTAL", "LEVEL_UP", "ENCHANT", "TELEPORT", "BELL", "CHIME", "RESONATE", "ENDER_EYE", "EXP_ORB", "EVOKER_CAST", "CONDUIT_ATK", "DRAGON_FIREBALL"}, {"WARDEN", "WARDEN_HEART", "DRAGON", "DRAGON_GROWL", "BLAZE", "GHAST", "ENDERMAN", "PHANTOM", "WOLF", "CAT", "ALLAY_ITEM", "BEE_STING", "RAVAGER_ROAR"}, {"BURP", "PLING", "GOAT", "NO", "YES", "EAT", "TOAST", "GLASS", "VILLAGER_CELEBRATE", "VILLAGER_TRADE", "BELL_RESONATE", "NOTE_BIT", "NOTE_BANJO"}};
        SOUND_GROUP_KEYS = new String[]{"Combat", "Magic", "Creature", "Fun"};
    }
}

