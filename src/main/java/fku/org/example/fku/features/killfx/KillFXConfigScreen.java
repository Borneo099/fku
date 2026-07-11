package fku.org.example.fku.features.killfx; /* water */

import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.ColorWheelPicker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * 击杀特效配置界面
 *
 * ★ 职责：
 *   右键「击杀特效」组件后打开，包含所有特效设置。
 *   所有改动即时保存到 KillFXConfig，切换分类不丢失输入框的数值。
 *
 * ★ 设计思想：
 *   1. 切换分类前先保存所有输入框数值（floatingInputValues 缓存）
 *   2. 开关按钮即时保存（set + save）
 *   3. 全界面中文显示，粒子/音效名称也使用中文别名
 */
public class KillFXConfigScreen extends Screen {
    private static final int WIDTH = 300;
    private static final int VISIBLE_HEIGHT = 255;
    private static final int CONTENT_HEIGHT = 800;
    private static final int BTN_ON = 0x006600;
    private static final int BTN_OFF = 0x660000;

    private final KillFXConfig cfg;
    private String activeCategory = "通用";

    /** 滚轮偏移量 */
    private int scrollOffset = 0;
    /** 内容总高度 */
    private int contentMaxRow = 0;

    // ★ 彩色圆盘选择器
    private ColorWheelPicker colorWheelPicker;

    // ★ 输入框浮动值缓存
    private final java.util.Map<String, java.util.Map<String, String>> floatingValues = new java.util.HashMap<>();

    public KillFXConfigScreen() {
        super(Component.literal("击杀特效配置"));
        this.cfg = KillFXConfig.getInstance();
        for (String cat : new String[]{"通用", "闪电", "粒子", "音效", "额外", "着色器"}) {
            floatingValues.put(cat, new java.util.HashMap<>());
        }
        // 初始化彩色圆盘
        colorWheelPicker = new ColorWheelPicker(cfg.crystalTintColor, hex -> {
            cfg.crystalTintColor = hex;
            KillFXConfig.save();
        });
    }

    @Override
    protected void init() {
        super.init();
        rebuildWidgets();
    }

    /**
     * ★ 保存当前分类的输入框数值到浮动缓存
     *   在 rebuildWidgets 前调用，避免切换分类导致输入值丢失
     */
    private void saveFloatingInputs() {
        java.util.Map<String, String> catCache = floatingValues.get(activeCategory);
        if (catCache == null) return;

        // 通用
        if (timeoutInput != null) catCache.put("timeout", timeoutInput.getValue());
        // 闪电
        if (lightningAmountInput != null) catCache.put("lightningAmount", lightningAmountInput.getValue());
        // 粒子
        if (particleCountInput != null) catCache.put("particleCount", particleCountInput.getValue());
        if (particleSpeedInput != null) catCache.put("particleSpeed", particleSpeedInput.getValue());
        // 音效
        if (volumeInput != null) catCache.put("volume", volumeInput.getValue());
        if (pitchInput != null) catCache.put("pitch", pitchInput.getValue());
    }

    /**
     * ★ 从浮动缓存恢复输入框数值
     */
    private String loadFloatingInput(String key, String defaultValue) {
        java.util.Map<String, String> catCache = floatingValues.get(activeCategory);
        if (catCache == null) return defaultValue;
        return catCache.getOrDefault(key, defaultValue);
    }

    // ===== 输入框控件引用（rebuildWidgets 时重新创建） =====
    private EditBox timeoutInput;
    private EditBox lightningAmountInput;
    private EditBox particleCountInput;
    private EditBox particleSpeedInput;
    private EditBox volumeInput;
    private EditBox pitchInput;

    protected void rebuildWidgets() {
        clearWidgets();

        int cx = (width - WIDTH) / 2;
        int cy = (height - VISIBLE_HEIGHT) / 2;
        int col1 = cx + 135;
        int row = cy + 35 - scrollOffset;
        contentMaxRow = row;

        // ── 分类标签行 ──
        String[] categories = {"通用", "闪电", "粒子", "音效", "额外", "着色器"};
        int tabX = cx + 5;
        for (String cat : categories) {
            final String fcat = cat;
            int tw = Minecraft.getInstance().font.width(cat) + 10;
            boolean isActive = cat.equals(activeCategory);
            addRenderableWidget(Button.builder(
                Component.literal((isActive ? "§l[" : " ") + cat + (isActive ? "]§r" : " ")),
                btn -> {
                    saveFloatingInputs();           // ★ 保存当前分类输入
                    activeCategory = fcat;
                    rebuildWidgets();
                }
            ).bounds(tabX, cy + 5, Math.max(tw, 40), 16).build());
            tabX += Math.max(tw, 40) + 2;
        }

        // 清空引用，准备重建
        timeoutInput = null;
        lightningAmountInput = null;
        particleCountInput = null;
        particleSpeedInput = null;
        volumeInput = null;
        pitchInput = null;

        switch (activeCategory) {
            case "通用" -> buildGeneralSettings(cx, cy, row);
            case "闪电" -> buildLightningSettings(cx, cy, row);
            case "粒子" -> buildParticleSettings(cx, cy, row);
            case "音效" -> buildSoundSettings(cx, cy, row);
            case "额外" -> buildExtraSettings(cx, cy, row);
            case "着色器" -> buildShaderSettings(cx, cy, row);
        }

        // ── 底部按钮 ──
        addRenderableWidget(Button.builder(
            Component.literal("返回主菜单"),
            btn -> {
                saveAllNow();
                Minecraft.getInstance().setScreen(new ClickGuiScreen());
            }
        ).bounds(cx + 50, cy + VISIBLE_HEIGHT - 28, 100, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("重置为默认"),
            btn -> {
                // 重置配置为默认值
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
                cfg.shaderType = "无";
                cfg.shaderIntensity = 1.0;
                cfg.shaderDuration = 20;
                cfg.blackholeScale = 1.0;
                cfg.crystalStyle = "基础晶体";
                cfg.crystalTintColor = "88CCFF";
                cfg.crystalRadius = 1.0;
                cfg.crystalGlowIntensity = 0.8;
                cfg.crystalRotationSpeed = 1.5;
                cfg.crystalPulse = true;
                cfg.onlyTargeted = true;
                cfg.targetTimeout = 3.5;
                KillFXConfig.save();
                // 清空浮动缓存
                for (java.util.Map<String, String> cache : floatingValues.values()) {
                    cache.clear();
                }
                rebuildWidgets();
            }
        ).bounds(cx + 160, cy + VISIBLE_HEIGHT - 28, 90, 20).build());
    }

    // ════════════════════════════════════════════════════════════
    // ★ 通用设置
    // ════════════════════════════════════════════════════════════

    private void buildGeneralSettings(int cx, int cy, int row) {
        addToggle(row, "功能开关", cfg.enabled, v -> cfg.enabled = v);
        row += 24;

        addToggle(row, "仅限攻击目标", cfg.onlyTargeted, v -> cfg.onlyTargeted = v);
        row += 24;

        addLabeledInput(row, "记忆时间(秒)", loadFloatingInput("timeout", String.valueOf(cfg.targetTimeout)),
            box -> { timeoutInput = box; box.setMaxLength(5); });
    }

    // ════════════════════════════════════════════════════════════
    // ★ 闪电设置
    // ════════════════════════════════════════════════════════════

    private void buildLightningSettings(int cx, int cy, int row) {
        addToggle(row, "启用闪电", cfg.useLightning, v -> cfg.useLightning = v);
        row += 24;

        addLabeledInput(row, "闪电数量", loadFloatingInput("lightningAmount", String.valueOf(cfg.lightningAmount)),
            box -> { lightningAmountInput = box; box.setMaxLength(2); });
        row += 24;

        addToggle(row, "闪电音效", cfg.useLightningSound, v -> cfg.useLightningSound = v);
    }

    // ════════════════════════════════════════════════════════════
    // ★ 粒子设置
    // ════════════════════════════════════════════════════════════

    /** 粒子分类中文名称 → 英文键 */
    private static final String[][] PARTICLE_CATEGORIES = {
        {"战斗", "Combat"}, {"魔法", "Magic"}, {"火焰", "Fire"},
        {"自然", "Nature"}, {"特殊", "Update121"}, {"其他", "Misc"}
    };

    /** 粒子别名（显示用） */
    private static final java.util.Map<String, String> PARTICLE_ALIAS = new java.util.HashMap<>();
    static {
        // 战斗
        PARTICLE_ALIAS.put("DAMAGE_INDICATOR", "伤害指示");
        PARTICLE_ALIAS.put("CRIT", "暴击");
        PARTICLE_ALIAS.put("ENCHANTED_HIT", "附魔攻击");
        PARTICLE_ALIAS.put("SWEEP_ATTACK", "横扫");
        PARTICLE_ALIAS.put("EXPLOSION", "爆炸");
        PARTICLE_ALIAS.put("EXPLOSION_EMITTER", "爆炸发射");
        PARTICLE_ALIAS.put("SONIC_BOOM", "音爆");
        PARTICLE_ALIAS.put("TOTEM_OF_UNDYING", "不死图腾");
        PARTICLE_ALIAS.put("FIREWORK", "烟花");
        PARTICLE_ALIAS.put("EGG_CRACK", "蛋裂");
        // 魔法
        PARTICLE_ALIAS.put("WITCH", "女巫");
        PARTICLE_ALIAS.put("END_ROD", "末地烛");
        PARTICLE_ALIAS.put("PORTAL", "传送门");
        PARTICLE_ALIAS.put("ENCHANT", "附魔");
        PARTICLE_ALIAS.put("NAUTILUS", "鹦鹉螺");
        PARTICLE_ALIAS.put("ELDER_GUARDIAN", "远古守卫");
        PARTICLE_ALIAS.put("SCULK_CHARGE_POP", "幽匿爆裂");
        PARTICLE_ALIAS.put("SOUL", "灵魂");
        PARTICLE_ALIAS.put("GLOW_SQUID_INK", "发光墨汁");
        // 火焰
        PARTICLE_ALIAS.put("FLAME", "火焰");
        PARTICLE_ALIAS.put("SOUL_FIRE_FLAME", "灵魂火");
        PARTICLE_ALIAS.put("SMALL_FLAME", "小火苗");
        PARTICLE_ALIAS.put("LAVA", "熔岩");
        PARTICLE_ALIAS.put("LARGE_SMOKE", "浓烟");
        PARTICLE_ALIAS.put("SMOKE", "烟雾");
        PARTICLE_ALIAS.put("CAMPFIRE_COSY_SMOKE", "营火烟");
        PARTICLE_ALIAS.put("CAMPFIRE_SIGNAL_SMOKE", "信号烟");
        PARTICLE_ALIAS.put("GLOW", "荧光");
        PARTICLE_ALIAS.put("WAX_ON", "上蜡");
        PARTICLE_ALIAS.put("WAX_OFF", "脱蜡");
        PARTICLE_ALIAS.put("SCRAPE", "刮削");
        PARTICLE_ALIAS.put("ELECTRIC_SPARK", "电火花");
        // 自然
        PARTICLE_ALIAS.put("HEART", "爱心");
        PARTICLE_ALIAS.put("CLOUD", "云");
        PARTICLE_ALIAS.put("RAIN", "雨");
        PARTICLE_ALIAS.put("SNOWFLAKE", "雪花");
        PARTICLE_ALIAS.put("ITEM_SLIME", "史莱姆");
        PARTICLE_ALIAS.put("BUBBLE", "气泡");
        PARTICLE_ALIAS.put("BUBBLE_COLUMN_UP", "气泡柱上");
        PARTICLE_ALIAS.put("CURRENT_DOWN", "水流下");
        PARTICLE_ALIAS.put("BUBBLE_POP", "气泡破");
        PARTICLE_ALIAS.put("SPLASH", "溅水");
        PARTICLE_ALIAS.put("FISHING", "钓鱼");
        PARTICLE_ALIAS.put("DOLPHIN", "海豚");
        PARTICLE_ALIAS.put("UNDERWATER", "水下");
        PARTICLE_ALIAS.put("NOTE", "音符");
        PARTICLE_ALIAS.put("CHERRY_LEAVES", "樱花");
        PARTICLE_ALIAS.put("SPORE_BLOSSOM_AIR", "孢子花");
        PARTICLE_ALIAS.put("WHITE_ASH", "白灰");
        PARTICLE_ALIAS.put("WARPED_SPORE", "诡异孢子");
        PARTICLE_ALIAS.put("CRIMSON_SPORE", "绯红孢子");
        // 特殊（仅含 1.20.1 有效粒子）
        PARTICLE_ALIAS.put("DRAGON_BREATH", "龙息");
        PARTICLE_ALIAS.put("FLASH", "闪光");
        PARTICLE_ALIAS.put("POOF", "噗");
        PARTICLE_ALIAS.put("SPIT", "口水");
        // 其他
        PARTICLE_ALIAS.put("ASH", "灰烬");
        PARTICLE_ALIAS.put("MYCELIUM", "菌丝");
        PARTICLE_ALIAS.put("SCULK_SOUL", "幽匿灵魂");
        PARTICLE_ALIAS.put("HAPPY_VILLAGER", "村民开心");
        PARTICLE_ALIAS.put("ANGRY_VILLAGER", "村民生气");
        PARTICLE_ALIAS.put("SNEEZE", "喷嚏");
        PARTICLE_ALIAS.put("SQUID_INK", "墨汁");
    }

    /** 粒子形状中文别名 */
    private static final java.util.Map<String, String> SHAPE_ALIAS = new java.util.HashMap<>();
    static {
        SHAPE_ALIAS.put("Burst", "爆散");
        SHAPE_ALIAS.put("Sphere", "球体");
        SHAPE_ALIAS.put("Spiral", "螺旋");
        SHAPE_ALIAS.put("Column", "光柱");
        SHAPE_ALIAS.put("Halo", "光环");
        SHAPE_ALIAS.put("Heart", "爱心");
        SHAPE_ALIAS.put("Helix", "双螺旋");
        SHAPE_ALIAS.put("Star", "星形");
        SHAPE_ALIAS.put("Ring", "圆环");
    }

    private void buildParticleSettings(int cx, int cy, int row) {
        addToggle(row, "启用粒子", cfg.useParticles, v -> cfg.useParticles = v);
        row += 24;

        // ★ 粒子分类 - 每行3个按钮，避免超出面板宽度（WIDTH=300px）
        drawLabel("粒子分类:", cx, row);
        int catStartX = cx + 100;
        int catBtnW = 38, catBtnGap = 40;
        int catRowY = row;
        for (int i = 0; i < PARTICLE_CATEGORIES.length; i++) {
            String[] cat = PARTICLE_CATEGORIES[i];
            String cnName = cat[0];
            String enKey = cat[1];
            boolean active = cfg.particleCategory.equals(enKey);
            int col = i % 3; // 每行3列
            int rowOffset = (i / 3) * 18; // 换行偏移
            addRenderableWidget(Button.builder(
                Component.literal(active ? "§l[" + cnName + "]§r" : cnName),
                btn -> {
                    cfg.particleCategory = enKey;
                    KillFXConfig.save();
                    rebuildWidgets();
                }
            ).bounds(catStartX + col * catBtnGap, catRowY + rowOffset, catBtnW, 16).build());
        }
        int catRows = (PARTICLE_CATEGORIES.length + 2) / 3; // 向上取整
        row += 20 + (catRows - 1) * 18;

        // 具体粒子按钮
        drawLabel("具体粒子:", cx, row);
        String currentParticle = getCurrentParticleField();
        String displayName = PARTICLE_ALIAS.getOrDefault(currentParticle, currentParticle);
        addRenderableWidget(Button.builder(
            Component.literal(displayName),
            btn -> {
                cycleParticle();
                KillFXConfig.save();
                rebuildWidgets();
            }
        ).bounds(cx + 100, row, 80, 20).build());
        row += 24;

        // ★ 粒子形状 - 每行4个按钮，避免超出面板宽度（WIDTH=300px）
        drawLabel("粒子形状:", cx, row);
        java.util.Map.Entry<String, String>[] shapeEntries = SHAPE_ALIAS.entrySet().toArray(new java.util.Map.Entry[0]);
        int shapeStartX = cx + 100;
        int shapeBtnW = 36, shapeBtnGap = 38;
        int shapeRowY = row;
        for (int i = 0; i < shapeEntries.length; i++) {
            java.util.Map.Entry<String, String> entry = shapeEntries[i];
            String enKey = entry.getKey();
            String cnName = entry.getValue();
            boolean active = cfg.particleShape.equals(enKey);
            int col = i % 4; // 每行4列
            int rowOffset = (i / 4) * 18;
            addRenderableWidget(Button.builder(
                Component.literal(active ? "§l[" + cnName + "]§r" : cnName),
                btn -> {
                    cfg.particleShape = enKey;
                    KillFXConfig.save();
                    rebuildWidgets();
                }
            ).bounds(shapeStartX + col * shapeBtnGap, shapeRowY + rowOffset, shapeBtnW, 16).build());
        }
        int shapeRows = (shapeEntries.length + 3) / 4; // 向上取整
        row += 20 + (shapeRows - 1) * 18;

        addLabeledInput(row, "粒子数量", loadFloatingInput("particleCount", String.valueOf(cfg.particleCount)),
            box -> { particleCountInput = box; box.setMaxLength(4); });
        row += 24;

        addLabeledInput(row, "粒子速度", loadFloatingInput("particleSpeed", String.valueOf(cfg.particleSpeed)),
            box -> { particleSpeedInput = box; box.setMaxLength(5); });
    }

    // ════════════════════════════════════════════════════════════
    // ★ 音效设置
    // ════════════════════════════════════════════════════════════

    /** 音效分类中文别名 */
    private static final java.util.Map<String, String> SOUND_GROUP_ALIAS = new java.util.HashMap<>();
    static {
        SOUND_GROUP_ALIAS.put("Combat", "战斗");
        SOUND_GROUP_ALIAS.put("Magic", "魔法");
        SOUND_GROUP_ALIAS.put("Creature", "生物");
        SOUND_GROUP_ALIAS.put("Fun", "趣味");
    }

    /** 音效中文别名 */
    private static final java.util.Map<String, String> SOUND_ALIAS = new java.util.HashMap<>();
    static {
        // 战斗
        SOUND_ALIAS.put("THUNDER", "雷鸣");
        SOUND_ALIAS.put("EXPLODE", "爆炸");
        SOUND_ALIAS.put("ANVIL", "铁砧");
        SOUND_ALIAS.put("TRIDENT_THUNDER", "三叉戟雷");
        SOUND_ALIAS.put("WITHER_SPAWN", "凋灵生成");
        SOUND_ALIAS.put("WITHER_SHOOT", "凋灵射击");
        SOUND_ALIAS.put("ANCHOR", "锚消耗");
        SOUND_ALIAS.put("CRYSTAL", "水晶爆炸");
        SOUND_ALIAS.put("BREAK", "盾牌破碎");
        SOUND_ALIAS.put("CRIT", "暴击");
        SOUND_ALIAS.put("CROSSBOW_HIT", "弩命中");
        SOUND_ALIAS.put("TRIDENT_HIT", "三叉戟命中");
        SOUND_ALIAS.put("FIREWORK_BLAST", "烟花爆炸");
        SOUND_ALIAS.put("ATK_STRONG", "强攻击");
        SOUND_ALIAS.put("ATK_SWEEP", "横扫攻击");
        // 魔法
        SOUND_ALIAS.put("ANCHOR_CHARGE", "锚充能");
        SOUND_ALIAS.put("ANCHOR_SET", "锚设重生点");
        SOUND_ALIAS.put("TOTEM", "不死图腾");
        SOUND_ALIAS.put("BEACON", "信标激活");
        SOUND_ALIAS.put("CONDUIT", "潮涌激活");
        SOUND_ALIAS.put("PORTAL", "传送门");
        SOUND_ALIAS.put("LEVEL_UP", "升级");
        SOUND_ALIAS.put("ENCHANT", "附魔");
        SOUND_ALIAS.put("TELEPORT", "传送");
        SOUND_ALIAS.put("BELL", "钟");
        SOUND_ALIAS.put("CHIME", "紫水晶");
        SOUND_ALIAS.put("RESONATE", "紫晶共鸣");
        SOUND_ALIAS.put("ENDER_EYE", "末影之眼");
        SOUND_ALIAS.put("EXP_ORB", "经验球");
        SOUND_ALIAS.put("EVOKER_CAST", "唤魔者施法");
        SOUND_ALIAS.put("CONDUIT_ATK", "潮涌攻击");
        SOUND_ALIAS.put("DRAGON_FIREBALL", "龙息弹");
        // 生物
        SOUND_ALIAS.put("WARDEN", "循声守卫吼");
        SOUND_ALIAS.put("WARDEN_HEART", "循声心跳");
        SOUND_ALIAS.put("DRAGON", "末影龙死");
        SOUND_ALIAS.put("DRAGON_GROWL", "末影龙嚎");
        SOUND_ALIAS.put("BLAZE", "烈焰人");
        SOUND_ALIAS.put("GHAST", "恶魂");
        SOUND_ALIAS.put("ENDERMAN", "末影人");
        SOUND_ALIAS.put("PHANTOM", "幻翼");
        SOUND_ALIAS.put("WOLF", "狼嚎");
        SOUND_ALIAS.put("CAT", "猫嘶");
        SOUND_ALIAS.put("ALLAY_ITEM", "悦灵");
        SOUND_ALIAS.put("BEE_STING", "蜜蜂蜇");
        SOUND_ALIAS.put("RAVAGER_ROAR", "掠夺者吼");
        // 趣味
        SOUND_ALIAS.put("BURP", "打嗝");
        SOUND_ALIAS.put("PLING", "叮");
        SOUND_ALIAS.put("GOAT", "山羊奶");
        SOUND_ALIAS.put("NO", "村民否");
        SOUND_ALIAS.put("YES", "村民是");
        SOUND_ALIAS.put("EAT", "吃");
        SOUND_ALIAS.put("TOAST", "成就");
        SOUND_ALIAS.put("GLASS", "玻璃碎");
        SOUND_ALIAS.put("VILLAGER_CELEBRATE", "村民庆祝");
        SOUND_ALIAS.put("VILLAGER_TRADE", "村民交易");
        SOUND_ALIAS.put("BELL_RESONATE", "钟共鸣");
        SOUND_ALIAS.put("NOTE_BIT", "音符-低音");
        SOUND_ALIAS.put("NOTE_BANJO", "音符-班卓");
    }

    private void buildSoundSettings(int cx, int cy, int row) {
        addToggle(row, "启用音效", cfg.useSound, v -> cfg.useSound = v);
        row += 24;

        drawLabel("音效分类:", cx, row);
        int btnX = cx + 100;
        for (java.util.Map.Entry<String, String> entry : SOUND_GROUP_ALIAS.entrySet()) {
            String enKey = entry.getKey();
            String cnName = entry.getValue();
            boolean active = cfg.soundGroup.equals(enKey);
            addRenderableWidget(Button.builder(
                Component.literal(active ? "§l[" + cnName + "]§r" : cnName),
                btn -> {
                    cfg.soundGroup = enKey;
                    KillFXConfig.save();
                    rebuildWidgets();
                }
            ).bounds(btnX, row, 36, 16).build());
            btnX += 38;
        }
        row += 20;

        drawLabel("具体音效:", cx, row);
        String currentSound = getCurrentSoundField();
        String soundDisplay = SOUND_ALIAS.getOrDefault(currentSound, currentSound);
        addRenderableWidget(Button.builder(
            Component.literal(soundDisplay),
            btn -> {
                cycleSound();
                KillFXConfig.save();
                rebuildWidgets();
            }
        ).bounds(cx + 100, row, 90, 20).build());
        row += 24;

        addLabeledInput(row, "音量", loadFloatingInput("volume", String.valueOf(cfg.volume)),
            box -> { volumeInput = box; box.setMaxLength(5); });
        row += 24;

        addLabeledInput(row, "音调", loadFloatingInput("pitch", String.valueOf(cfg.pitch)),
            box -> { pitchInput = box; box.setMaxLength(5); });
    }

    // ════════════════════════════════════════════════════════════
    // ★ 额外视觉
    // ════════════════════════════════════════════════════════════

    private void buildExtraSettings(int cx, int cy, int row) {
        addToggle(row, "生成烟花", cfg.useFirework, v -> cfg.useFirework = v);
        row += 24;

        addToggle(row, "爆炸烟雾", cfg.useExplosion, v -> cfg.useExplosion = v);
    }

    private void buildShaderSettings(int cx, int cy, int row) {
        addToggle(row, "启用着色器", cfg.useShader, v -> cfg.useShader = v);
        row += 22;

        // ★ 特效类型：每行2个按钮
        drawLabel("特效类型:", cx, row);
        String[] types = {"黑洞", "水晶", "天光光束", "天光环", "超新星", "光线爆发"};
        int btW = 56;
        int typeRows = (types.length + 1) / 2; // 3行
        for (int i = 0; i < types.length; i++) {
            final String fType = types[i];
            boolean isActive = fType.equals(cfg.shaderType);
            int bx = cx + 130 + (i % 2) * (btW + 4);
            int by = row + (i / 2) * 18;
            addRenderableWidget(Button.builder(
                Component.literal(isActive ? "▶" + fType : fType),
                btn -> { cfg.shaderType = fType; KillFXConfig.save(); rebuildWidgets(); }
            ).bounds(bx, by, btW, 16).build());
        }
        row += typeRows * 18 + 4;

        if ("黑洞".equals(cfg.shaderType)) buildBlackholeSettings(cx, row);
        else if ("水晶".equals(cfg.shaderType)) buildCrystalSettings(cx, row);
        else if ("天光光束".equals(cfg.shaderType)) buildBeamRingSettings(cx, row);
        else if ("天光环".equals(cfg.shaderType)) buildBeamRingSettings(cx, row);
        else if ("超新星".equals(cfg.shaderType) || "光线爆发".equals(cfg.shaderType)) buildBeamRingSettings(cx, row);
    }

    /** 黑洞配置：各控件独立一行防重叠 */
    private void buildBlackholeSettings(int cx, int row) {
        int[] dur = {cfg.shaderDuration};
        drawLabel("持续Tick:", cx, row);
        addRenderableWidget(Button.builder(
            Component.literal(dur[0] + "t"),
            btn -> { dur[0] = dur[0] >= 80 ? 5 : dur[0] + 5;
                cfg.shaderDuration = dur[0]; KillFXConfig.save(); rebuildWidgets(); }
        ).bounds(cx + 135, row, 45, 18).build());
        row += 22;
        double[] sc = {cfg.blackholeScale};
        drawLabel("黑洞大小:", cx, row);
        addRenderableWidget(Button.builder(Component.literal(String.format("%.1f",sc[0])),
            btn -> { sc[0] += 0.25; if (sc[0] > 3.0) sc[0] = 0.5;
                cfg.blackholeScale = sc[0]; KillFXConfig.save(); rebuildWidgets(); }
        ).bounds(cx + 135, row, 45, 18).build());
    }

    /** 水晶配置：严格分行不重叠 */
    private void buildCrystalSettings(int cx, int row) {
        // 行1：持续Tick
        drawLabel("持续Tick:", cx, row);
        addRenderableWidget(Button.builder(Component.literal(cfg.shaderDuration + "t"),
            btn -> { cfg.shaderDuration = cfg.shaderDuration >= 80 ? 5 : cfg.shaderDuration + 5;
                KillFXConfig.save(); rebuildWidgets(); }
        ).bounds(cx + 135, row, 45, 18).build());
        row += 22;

        // 行2：风格（4个按钮横向排列，从cx+135开始）
        drawLabel("风格:", cx, row);
        String[] styles = {"基础晶体", "发光", "玻璃折射", "极光"};
        int sx = cx + 130;
        for (String s : styles) {
            boolean act = s.equals(cfg.crystalStyle);
            addRenderableWidget(Button.builder(Component.literal(act ? "▶" + s : s),
                btn -> { cfg.crystalStyle = s; KillFXConfig.save(); rebuildWidgets(); }
            ).bounds(sx, row, 40, 16).build());
            sx += 42;
        }
        row += 22;

        // 行3：色调 — 显示当前HEX颜色方块 + 选色按钮
        drawLabel("色调:", cx, row);
        int colorInt;
        try { colorInt = Integer.parseInt(cfg.crystalTintColor, 16); } catch (Exception e) { colorInt = 0x88CCFF; }
        int finalColorInt = colorInt;
        addRenderableWidget(Button.builder(
            Component.literal("§" + (cfg.crystalTintColor.length() == 6 ? "a●" : "7?")),
            btn -> {
                int cx2 = (width - WIDTH) / 2;
                int cy2 = (height - VISIBLE_HEIGHT) / 2;
                colorWheelPicker.open(cx2 + WIDTH / 2, cy2 + VISIBLE_HEIGHT / 2);
            }
        ).bounds(cx + 85, row, 18, 16).build());
        addRenderableWidget(Button.builder(
            Component.literal("#" + cfg.crystalTintColor),
            btn -> {
                int cx2 = (width - WIDTH) / 2;
                int cy2 = (height - VISIBLE_HEIGHT) / 2;
                colorWheelPicker.open(cx2 + WIDTH / 2, cy2 + VISIBLE_HEIGHT / 2);
            }
        ).bounds(cx + 105, row, 65, 16).build());
        row += 22;

        // 行4：半径 + 发光（同行，左右分开）
        drawLabel("半径:", cx, row);
        double[] radius = {cfg.crystalRadius};
        addRenderableWidget(Button.builder(Component.literal(String.format("%.1f",radius[0])),
            btn -> { radius[0] += 0.25; if (radius[0] > 3.0) radius[0] = 0.5;
                cfg.crystalRadius = radius[0]; KillFXConfig.save(); rebuildWidgets(); }
        ).bounds(cx + 55, row, 40, 16).build());
        drawLabel("发光:", cx + 130, row);
        double[] glow = {cfg.crystalGlowIntensity};
        addRenderableWidget(Button.builder(Component.literal(String.format("%.1f",glow[0])),
            btn -> { glow[0] += 0.2; if (glow[0] > 2.0) glow[0] = 0.0;
                cfg.crystalGlowIntensity = glow[0]; KillFXConfig.save(); rebuildWidgets(); }
        ).bounds(cx + 175, row, 40, 16).build());
        row += 22;

        // 行5：转速 + 脉冲（同行）
        drawLabel("转速:", cx, row);
        double[] speed = {cfg.crystalRotationSpeed};
        addRenderableWidget(Button.builder(Component.literal(String.format("%.1f",speed[0])),
            btn -> { speed[0] += 0.5; if (speed[0] > 5.0) speed[0] = 0.0;
                cfg.crystalRotationSpeed = speed[0]; KillFXConfig.save(); rebuildWidgets(); }
        ).bounds(cx + 55, row, 40, 16).build());
        if ("基础晶体".equals(cfg.crystalStyle) || "发光".equals(cfg.crystalStyle)) {
            addToggleSimple(cx + 130, row, "脉冲", cfg.crystalPulse, v -> cfg.crystalPulse = v);
        }
    }

    /** 天光/光环配置：严格分行 */
    private void buildBeamRingSettings(int cx, int row) {
        drawLabel("持续Tick:", cx, row);
        addRenderableWidget(Button.builder(Component.literal(cfg.shaderDuration + "t"),
            btn -> { cfg.shaderDuration = cfg.shaderDuration >= 80 ? 5 : cfg.shaderDuration + 5;
                KillFXConfig.save(); rebuildWidgets(); }
        ).bounds(cx + 135, row, 45, 18).build());
        row += 22;
        double[] sz = {cfg.shaderIntensity};
        drawLabel("大小:", cx, row);
        addRenderableWidget(Button.builder(Component.literal(String.format("%.1f",sz[0])),
            btn -> { sz[0] += 0.25; if (sz[0] > 3.0) sz[0] = 0.5;
                cfg.shaderIntensity = sz[0]; KillFXConfig.save(); rebuildWidgets(); }
        ).bounds(cx + 135, row, 45, 18).build());
    }

    /** 同行内小开关（不触发 rebuildWidgets 只刷新按钮文字） */
    private void addToggleSimple(int x, int y, String label, boolean current, java.util.function.Consumer<Boolean> setter) {
        drawLabel(label, x, y);
        addRenderableWidget(Button.builder(Component.literal(current ? "开" : "关"),
            btn -> { boolean nv = !current; setter.accept(nv);
                KillFXConfig.save(); btn.setMessage(Component.literal(nv ? "开" : "关")); }
        ).bounds(x + 35, y, 30, 16).build());
    }

    // ════════════════════════════════════════════════════════════
    // ★ 通用控件构建方法
    // ════════════════════════════════════════════════════════════

    private int currentRowGlobal;

    /** 添加开关按钮（即时保存） */
    private void addToggle(int row, String label, boolean currentValue, Consumer<Boolean> setter) {
        int cx = (width - WIDTH) / 2;
        drawLabel(label, cx, row);
        addRenderableWidget(Button.builder(
            Component.literal(currentValue ? "开" : "关"),
            btn -> {
                boolean newVal = !currentValue;
                setter.accept(newVal);
                KillFXConfig.save(); // ★ 即时保存
                btn.setMessage(Component.literal(newVal ? "开" : "关"));
                // 更新按钮背景色（开=绿，关=红）
                rebuildWidgets();
            }
        ).bounds(cx + 135, row, 40, 20).build());
    }

    /** 添加带标签的输入框（从浮动缓存恢复数值） */
    private void addLabeledInput(int row, String label, String cachedValue, Consumer<EditBox> setter) {
        int cx = (width - WIDTH) / 2;
        drawLabel(label, cx, row);
        EditBox box = new EditBox(font, cx + 135, row, 60, 18, Component.literal(""));
        box.setValue(cachedValue);
        box.setMaxLength(10);
        addRenderableWidget(box);
        setter.accept(box);
    }

    /** 绘制标签文本 */
    private void drawLabel(String text, int cx, int row) {
        // 标签在 render() 方法中统一绘制
    }

    // ════════════════════════════════════════════════════════════
    // ★ 粒子/音效 辅助方法
    // ════════════════════════════════════════════════════════════

    private String getCurrentParticleField() {
        return switch (cfg.particleCategory) {
            case "Combat" -> cfg.combatParticle;
            case "Magic" -> cfg.magicParticle;
            case "Fire" -> cfg.fireParticle;
            case "Nature" -> cfg.natureParticle;
            case "Update121" -> cfg.updateParticle;
            case "Misc" -> cfg.miscParticle;
            default -> cfg.magicParticle;
        };
    }

    /**
     * ★ 只含 Forge 1.20.1 真实存在的粒子类型
     *   移除 1.21+ 新增粒子（GUST, TRIAL_SPAWNER 等）
     */
    private static final String[][] ALL_PARTICLES = {
        {"DAMAGE_INDICATOR","CRIT","ENCHANTED_HIT","SWEEP_ATTACK","EXPLOSION","EXPLOSION_EMITTER","SONIC_BOOM","TOTEM_OF_UNDYING","FIREWORK","EGG_CRACK"},
        {"WITCH","END_ROD","PORTAL","ENCHANT","NAUTILUS","ELDER_GUARDIAN","SCULK_CHARGE_POP","SOUL","GLOW_SQUID_INK"},
        {"FLAME","SOUL_FIRE_FLAME","LAVA","LARGE_SMOKE","SMOKE","CAMPFIRE_COSY_SMOKE","CAMPFIRE_SIGNAL_SMOKE","GLOW","WAX_ON","WAX_OFF","SCRAPE","ELECTRIC_SPARK"},
        {"HEART","CLOUD","RAIN","SNOWFLAKE","ITEM_SLIME","BUBBLE","BUBBLE_COLUMN_UP","CURRENT_DOWN","BUBBLE_POP","SPLASH","FISHING","DOLPHIN","UNDERWATER","NOTE","CHERRY_LEAVES","SPORE_BLOSSOM_AIR","WHITE_ASH","WARPED_SPORE","CRIMSON_SPORE"},
        {"DRAGON_BREATH","FLASH","POOF","SNOWFLAKE","SPIT"},
        {"ASH","MYCELIUM","SCULK_SOUL","HAPPY_VILLAGER","ANGRY_VILLAGER","SNEEZE","SQUID_INK"}
    };
    private static final String[] PARTICLE_CAT_KEYS = {"Combat", "Magic", "Fire", "Nature", "Update121", "Misc"};

    private void cycleParticle() {
        for (int ci = 0; ci < PARTICLE_CAT_KEYS.length; ci++) {
            if (PARTICLE_CAT_KEYS[ci].equals(cfg.particleCategory)) {
                String[] arr = ALL_PARTICLES[ci];
                String currentField = getCurrentParticleField();
                int idx = indexOf(arr, currentField);

                // ★ 只循环有效粒子，不过期到不存在的粒子
                String nextParticle = arr[(idx + 1) % arr.length];
                switch (ci) {
                    case 0 -> cfg.combatParticle = nextParticle;
                    case 1 -> cfg.magicParticle = nextParticle;
                    case 2 -> cfg.fireParticle = nextParticle;
                    case 3 -> cfg.natureParticle = nextParticle;
                    case 4 -> cfg.updateParticle = nextParticle;
                    case 5 -> cfg.miscParticle = nextParticle;
                }
                break;
            }
        }
    }

    private String getCurrentSoundField() {
        return switch (cfg.soundGroup) {
            case "Combat" -> cfg.combatSound;
            case "Magic" -> cfg.magicSound;
            case "Creature" -> cfg.creatureSound;
            case "Fun" -> cfg.funSound;
            default -> cfg.combatSound;
        };
    }

    private static final String[][] ALL_SOUNDS = {
        {"THUNDER","EXPLODE","ANVIL","TRIDENT_THUNDER","WITHER_SPAWN","WITHER_SHOOT","ANCHOR","CRYSTAL","BREAK","CRIT","CROSSBOW_HIT","TRIDENT_HIT","FIREWORK_BLAST","ATK_STRONG","ATK_SWEEP"},
        {"ANCHOR_CHARGE","ANCHOR_SET","TOTEM","BEACON","CONDUIT","PORTAL","LEVEL_UP","ENCHANT","TELEPORT","BELL","CHIME","RESONATE","ENDER_EYE","EXP_ORB","EVOKER_CAST","CONDUIT_ATK","DRAGON_FIREBALL"},
        {"WARDEN","WARDEN_HEART","DRAGON","DRAGON_GROWL","BLAZE","GHAST","ENDERMAN","PHANTOM","WOLF","CAT","ALLAY_ITEM","BEE_STING","RAVAGER_ROAR"},
        {"BURP","PLING","GOAT","NO","YES","EAT","TOAST","GLASS","VILLAGER_CELEBRATE","VILLAGER_TRADE","BELL_RESONATE","NOTE_BIT","NOTE_BANJO"}
    };
    private static final String[] SOUND_GROUP_KEYS = {"Combat", "Magic", "Creature", "Fun"};

    private void cycleSound() {
        for (int ci = 0; ci < SOUND_GROUP_KEYS.length; ci++) {
            if (SOUND_GROUP_KEYS[ci].equals(cfg.soundGroup)) {
                String[] arr = ALL_SOUNDS[ci];
                String currentField = getCurrentSoundField();
                int idx = indexOf(arr, currentField);
                String nextSound = arr[(idx + 1) % arr.length];
                switch (ci) {
                    case 0 -> cfg.combatSound = nextSound;
                    case 1 -> cfg.magicSound = nextSound;
                    case 2 -> cfg.creatureSound = nextSound;
                    case 3 -> cfg.funSound = nextSound;
                }
                break;
            }
        }
    }

    private static int indexOf(String[] arr, String val) {
        for (int i = 0; i < arr.length; i++) if (arr[i].equals(val)) return i;
        return 0;
    }

    /** 保存所有输入框数值到配置并从浮动缓存持久化 */
    private void saveAllNow() {
        // 保存当前分类浮动缓存
        saveFloatingInputs();
        // 将浮动缓存同步到 cfg 对象
        syncFloatingToCfg();
        KillFXConfig.save();
    }

    /** 将所有分类的浮动缓存写入 cfg */
    private void syncFloatingToCfg() {
        try {
            // 通用
            java.util.Map<String, String> genCache = floatingValues.get("通用");
            if (genCache != null) {
                String v = genCache.get("timeout");
                if (v != null) cfg.targetTimeout = Double.parseDouble(v);
            }
            // 闪电
            java.util.Map<String, String> lightCache = floatingValues.get("闪电");
            if (lightCache != null) {
                String v = lightCache.get("lightningAmount");
                if (v != null) cfg.lightningAmount = Integer.parseInt(v);
            }
            // 粒子
            java.util.Map<String, String> particleCache = floatingValues.get("粒子");
            if (particleCache != null) {
                String v = particleCache.get("particleCount");
                if (v != null) cfg.particleCount = Integer.parseInt(v);
                v = particleCache.get("particleSpeed");
                if (v != null) cfg.particleSpeed = Double.parseDouble(v);
            }
            // 音效
            java.util.Map<String, String> soundCache = floatingValues.get("音效");
            if (soundCache != null) {
                String v = soundCache.get("volume");
                if (v != null) cfg.volume = Double.parseDouble(v);
                v = soundCache.get("pitch");
                if (v != null) cfg.pitch = Double.parseDouble(v);
            }
        } catch (NumberFormatException ignored) {
            // 忽略非法输入
        }
    }

    // ════════════════════════════════════════════════════════════
    // ★ 渲染
    // ════════════════════════════════════════════════════════════

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int cx = (width - WIDTH) / 2, cy = (height - VISIBLE_HEIGHT) / 2;
        // 只在面板区域内响应滚轮
        if (mouseX >= cx && mouseX <= cx + WIDTH && mouseY >= cy && mouseY <= cy + VISIBLE_HEIGHT) {
            int newScroll = scrollOffset - (int)(delta * 16);
            // 限制滚动范围：最多滚动到内容底部
            int maxScroll = Math.max(0, contentMaxRow - (cy + VISIBLE_HEIGHT - 60));
            scrollOffset = Math.max(0, Math.min(newScroll, maxScroll));
            rebuildWidgets();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        int cx = (width - WIDTH) / 2;
        int cy = (height - VISIBLE_HEIGHT) / 2;

        // 绘制主面板背景
        GuiRenderHelper.drawPanelBackground(guiGraphics, cx, cy, WIDTH, VISIBLE_HEIGHT, false);

        // 绘制标题
        guiGraphics.drawString(font, "击杀特效配置 - " + activeCategory, cx + 10, cy + 25, 0xFFFFFF);

        // 启用裁剪，防止内容超出面板
        guiGraphics.enableScissor(cx + 2, cy + 35, cx + WIDTH - 2, cy + VISIBLE_HEIGHT - 32);

        // 绘制当前分类的标签（行号已包含 scrollOffset）
        int row = cy + 35 - scrollOffset;
        String[][] labelDefs = switch (activeCategory) {
            case "通用" -> new String[][]{{"功能开关:", "35"}, {"仅限攻击目标:", "59"}, {"记忆时间(秒):", "83"}};
            case "闪电" -> new String[][]{{"启用闪电:", "35"}, {"闪电数量:", "59"}, {"闪电音效:", "83"}};
            case "粒子" -> new String[][]{{"启用粒子:", "35"}, {"粒子分类:", "59"}, {"具体粒子:", "95"}, {"粒子形状:", "119"}, {"粒子数量:", "171"}, {"粒子速度:", "195"}};
            case "音效" -> new String[][]{{"启用音效:", "35"}, {"音效分类:", "55"}, {"具体音效:", "75"}, {"音量:", "99"}, {"音调:", "123"}};
            case "额外" -> new String[][]{{"生成烟花:", "35"}, {"爆炸烟雾:", "59"}};
            case "着色器" -> {
                // ★ Y坐标：启用(35)→+22→类型(57)→+40→配置(97)→每行+22
                String type = cfg.shaderType;
                if ("黑洞".equals(type)) {
                    yield new String[][]{{"启用着色器:", "35"}, {"特效类型:", "57"}, {"持续Tick:", "97"}, {"黑洞大小:", "119"}};
                } else if ("水晶".equals(type)) {
                    yield new String[][]{{"启用着色器:", "35"}, {"特效类型:", "57"}, {"持续Tick:", "97"}, {"风格:", "119"}, {"色调:", "141"}, {"半径/发光:", "163"}, {"转速/脉冲:", "185"}};
                } else if ("天光光束".equals(type) || "天光环".equals(type)) {
                    yield new String[][]{{"启用着色器:", "35"}, {"特效类型:", "57"}, {"持续Tick:", "97"}, {"大小:", "119"}};
                } else if ("超新星".equals(type) || "光线爆发".equals(type)) {
                    yield new String[][]{{"启用着色器:", "35"}, {"特效类型:", "57"}, {"持续Tick:", "97"}, {"大小:", "119"}};
                } else {
                    yield new String[][]{{"启用着色器:", "35"}};
                }
            }
            default -> new String[][]{};
        };

        for (String[] pair : labelDefs) {
            int yRow = cy + Integer.parseInt(pair[1]) - scrollOffset;
            if (yRow + 4 >= cy + 35 && yRow < cy + VISIBLE_HEIGHT - 35) {
                guiGraphics.drawString(font, pair[0], cx + 10, yRow + 4, 0xAAAAAA);
            }
        }

        guiGraphics.disableScissor();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        // 彩色圆盘渲染在最上层
        colorWheelPicker.render(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // ★ 彩色圆盘优先处理点击
        if (colorWheelPicker.isOpen()) {
            if (colorWheelPicker.mouseClicked(mouseX, mouseY, button)) return true;
            // 如果圆盘关闭了（点击外部），刷新界面
            rebuildWidgets();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        saveAllNow();
        this.minecraft.setScreen(new ClickGuiScreen());
    }
}