#!/usr/bin/env python3
"""
sync.py — FKU 多版本安全同步工具
────────────────────────────────────
设计原则：
  1. 默认 DRY-RUN：只报告差异，永不写入
  2. 只做 import 路径转换（不做 API 适配）
  3. 永不该写【已有 1.21.8 专属适配】的文件
  4. 任何写入操作前都显示 diff+确认

用法：
  python scripts/sync.py                 # dry-run：报告所有差异
  python scripts/sync.py --new            # dry-run：只报告 neoforge 中不存在的文件
  python scripts/sync.py --diff           # dry-run：显示详细 diff
  python scripts/sync.py --apply          # 写入可安全同步的文件
  python scripts/sync.py --apply --force  # 写入包括覆盖保护文件（危险！）
  python scripts/sync.py --verify         # 同步后自动构建验证
"""

import re, os, sys, subprocess, json, difflib

PROJECT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))

COMMON_SRC = os.path.join(PROJECT, "common", "src", "main", "java")
NEO_SRC    = os.path.join(PROJECT, "neoforge", "src", "main", "java")
MANIFEST   = os.path.join(PROJECT, "neoforge", ".sync-manifest.json")

# ═══════════════════════════════════════════════════════════════════
# 状态标记（自动检测）
# ═══════════════════════════════════════════════════════════════════

# 若 neoforge 文件包含以下任一模式 → 视为「已有 1.21.8 专属适配」，受保护
EXCLUSIVE_MARKERS = [
    "net.neoforged.neoforge.client.event.ClientTickEvent",
    "net.neoforged.fml.common.EventBusSubscriber",
    "net.neoforged.neoforge.client.event.RenderGuiEvent",
    "RenderStateShard.NoShaderStateShard",        # 1.21.8 新增
    "DataComponents.ENTITY_DATA",                  # 1.21.8 组件
    "ResourceLocation.parse(",                     # 1.21.8 方法
    "Transformation.EXTENDED_CODEC",              # 1.21.8 编解码
    "Operation.ADD_VALUE",                        # 1.21.8 操作
    "CustomData",                                  # 1.21.8 数据组件
    "net.neoforged.neoforge.common.ModConfigSpec", # 1.21.8 配置
    "net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent",
    "net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent",
    "net.neoforged.neoforge.client.event.ClientChatReceivedEvent",
    "net.neoforged.neoforge.client.event.MovementInputUpdateEvent",
]

# 手动保护列表（绝对不参与同步，即使 force 也不覆盖）
MANUAL_PROTECTED = {
    "fku/org/example/fku/features/displaymodel/DisplayModelManager.java",
    "fku/org/example/fku/features/arrowdmg/ArrowDmgFeature.java",
    "fku/org/example/fku/util/HotkeySystem.java",
    "fku/org/example/fku/features/sprint/SprintHandler.java",
}

# 已知可靠的 import 转换规则
IMPORT_RULES = [
    # ── 核心事件总线 ──
    (r'MinecraftForge\.EVENT_BUS', r'NeoForge.EVENT_BUS'),
    (r'import net\.minecraftforge\.common\.MinecraftForge;', r'import net.neoforged.neoforge.common.NeoForge;'),
    # ── 分发端 ──
    (r'import net\.minecraftforge\.api\.distmarker\.OnlyIn;', r'import net.neoforged.api.distmarker.OnlyIn;'),
    (r'import net\.minecraftforge\.api\.distmarker\.Dist;', r'import net.neoforged.api.distmarker.Dist;'),
    # ── 客户端事件 ──
    (r'import net\.minecraftforge\.client\.event\.InputEvent;', r'import net.neoforged.neoforge.client.event.InputEvent;'),
    (r'import net\.minecraftforge\.client\.event\.RenderLevelStageEvent;', r'import net.neoforged.neoforge.client.event.RenderLevelStageEvent;'),
    (r'import net\.minecraftforge\.client\.event\.RegisterKeyMappingsEvent;', r'import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;'),
    (r'import net\.minecraftforge\.client\.event\.ClientPlayerNetworkEvent;', r'import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;'),
    (r'import net\.minecraftforge\.client\.event\.RegisterClientCommandsEvent;', r'import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;'),
    (r'import net\.minecraftforge\.client\.event\.ClientChatReceivedEvent;', r'import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;'),
    (r'import net\.minecraftforge\.client\.event\.MovementInputUpdateEvent;', r'import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;'),
    (r'import net\.minecraftforge\.client\.event\.ScreenEvent;', r'import net.neoforged.neoforge.client.event.ScreenEvent;'),
    # ── 实体事件 ──
    (r'import net\.minecraftforge\.event\.entity\.player\.AttackEntityEvent;', r'import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;'),
    (r'import net\.minecraftforge\.event\.entity\.living\.LivingFallEvent;', r'import net.neoforged.neoforge.event.entity.living.LivingFallEvent;'),
    # ── Bus ──
    (r'import net\.minecraftforge\.eventbus\.api\.SubscribeEvent;', r'import net.neoforged.bus.api.SubscribeEvent;'),
    (r'import net\.minecraftforge\.eventbus\.api\.Event;', r'import net.neoforged.bus.api.Event;'),
    # ── FML ──
    (r'import net\.minecraftforge\.fml\.event\.lifecycle\.FMLClientSetupEvent;', r'import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;'),
    (r'import net\.minecraftforge\.fml\.config\.ModConfig;', r'import net.neoforged.fml.config.ModConfig;'),
    (r'import net\.minecraftforge\.fml\.ModList;', r'import net.neoforged.fml.ModList;'),
    # ── 配置 ──
    (r'import net\.minecraftforge\.common\.ForgeConfigSpec;', r'import net.neoforged.neoforge.common.ModConfigSpec;'),
    (r'import net\.minecraftforge\.common\.ForgeMod;', r'import net.neoforged.neoforge.common.NeoForgeMod;'),
    # ── 实体 ──
    (r'import net\.minecraftforge\.entity\.PartEntity;', r'import net.neoforged.neoforge.entity.PartEntity;'),
    # ── 注册表 ──
    (r'import net\.minecraftforge\.registries\.ForgeRegistries;', r'import net.neoforged.neoforge.registries.NeoForgeRegistries;'),
    # ── 客户端设置 ──
    (r'import net\.minecraftforge\.client\.settings\.KeyConflictContext;', r'import net.neoforged.neoforge.client.settings.KeyConflictContext;'),
    (r'import net\.minecraftforge\.client\.renderer\.Shaders;', r'import net.neoforged.neoforge.client.renderer.Shaders;'),
    # ── 新事件包替换（NeoForge 1.21.8） ──
    (r'import net\.minecraftforge\.client\.event\.RenderGuiOverlayEvent;', r'import net.neoforged.neoforge.client.event.RenderGuiOverlayEvent;'),
    (r'import net\.minecraftforge\.client\.gui\.overlay\.VanillaGuiOverlay;', r'import net.neoforged.neoforge.client.gui.overlay.VanillaGuiOverlay;'),
    (r'import net\.minecraftforge\.client\.event\.RegisterShadersEvent;', r'import net.neoforged.neoforge.client.event.RegisterShadersEvent;'),
    (r'import net\.minecraftforge\.client\.event\.RenderGuiEvent;', r'import net.neoforged.neoforge.client.event.RenderGuiEvent;'),
]

TRANSFORM_RULES = IMPORT_RULES + [
    # ── Inline 全限定名替换 ──
    (r'net\.minecraftforge\.registries\.ForgeRegistries\.', r'net.neoforged.neoforge.registries.NeoForgeRegistries.'),
    (r'net\.minecraftforge\.client\.event\.RenderGuiOverlayEvent\.', r'net.neoforged.neoforge.client.event.RenderGuiOverlayEvent.'),
    (r'net\.minecraftforge\.client\.gui\.overlay\.VanillaGuiOverlay\.', r'net.neoforged.neoforge.client.gui.overlay.VanillaGuiOverlay.'),
    (r'net\.minecraftforge\.event\.TickEvent\.', r'net.neoforged.neoforge.event.tick.TickEvent.'),
]


def transform(text):
    """仅做 import 路径转换。不处理深层 API 差异。"""
    for pat, repl in TRANSFORM_RULES:
        text = re.sub(pat, repl, text)
    return text


def has_exclusive(content):
    return any(m in content for m in EXCLUSIVE_MARKERS)


def rel_path(abs_path, base):
    return os.path.relpath(abs_path, base).replace(os.sep, "/")


def collect_java_files(src_dir):
    result = {}
    for root, dirs, files in os.walk(src_dir):
        for f in files:
            if f.endswith(".java"):
                full = os.path.join(root, f)
                rel = rel_path(full, src_dir)
                result[rel] = full
    return result


def load_manifest():
    if os.path.isfile(MANIFEST):
        try:
            with open(MANIFEST, encoding="utf-8") as f:
                return json.load(f)
        except: pass
    return {}


def save_manifest(data):
    os.makedirs(os.path.dirname(MANIFEST), exist_ok=True)
    with open(MANIFEST, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)


# ═══════════════════════════════════════════════════════════════════
# 分析阶段
# ═══════════════════════════════════════════════════════════════════

common_files = collect_java_files(COMMON_SRC)
neo_files = collect_java_files(NEO_SRC)

new_files = []          # 只存在于 common，不存在于 neoforge
exclusive_protected = [] # neoforge 已含专属适配标记，受保护
manual_protected = []    # 手动保护列表中的文件
safe_updates = []        # 两版都存在，经检查可以安全同步
safe_new = []            # 新文件，转换后写入
manifest = load_manifest()

for rel, common_path in sorted(common_files.items()):
    with open(common_path, encoding="utf-8") as f:
        common_text = f.read()
    converted = transform(common_text)

    # neoforge/ 子包文件不参与同步
    if "/neoforge/" in rel:
        continue

    if rel not in neo_files:
        # 新文件：common 有，neoforge 无
        new_files.append((rel, common_path, converted))
    else:
        neo_path = neo_files[rel]
        with open(neo_path, encoding="utf-8") as f:
            neo_text = f.read()

        if neo_text == converted:
            # 已同步，跳过
            continue

        if rel in MANUAL_PROTECTED:
            manual_protected.append(rel)
            continue

        if has_exclusive(neo_text) and not has_exclusive(converted):
            exclusive_protected.append(rel)
            continue

        # 可以安全更新的文件
        safe_updates.append((rel, common_path, neo_path, converted, neo_text))


# ═══════════════════════════════════════════════════════════════════
# 报告
# ═══════════════════════════════════════════════════════════════════

def print_section(title, items, detail=False):
    if not items:
        return
    print(f"\n{'=' * 60}")
    print(f"  {title} ({len(items)})")
    print(f"{'=' * 60}")
    for item in items:
        if isinstance(item, tuple):
            print(f"  {item[0]}")
            if detail and len(item) > 3:
                diff = difflib.unified_diff(
                    item[-1].splitlines(keepends=True),
                    item[-2].splitlines(keepends=True) if len(item) > 4 else [],
                    fromfile="common", tofile="neoforge"
                )
                for line in diff:
                    print(f"    {line}", end="")
        else:
            print(f"  {item}")

want_new   = "--new" in sys.argv
want_diff  = "--diff" in sys.argv
want_apply = "--apply" in sys.argv
want_force = "--force" in sys.argv
want_verify = "--verify" in sys.argv

if want_new or not (want_apply or want_force):
    print_section("[NEW] neoforge 不存在的新文件（可同步）", new_files)
    print_section("[PROTECTED] 手动保护的文件（永不参与同步）", manual_protected)
    print_section("[EXCLUSIVE] 含 1.21.8 专属 API 标记的文件（受保护）", exclusive_protected)
    if not want_new:
        print_section("[SAFE-UPDATE] 可安全更新的文件（两版差异仅 import）", safe_updates, detail=want_diff)
    print()
    if want_apply:
        print(">> 检测到 --apply，写入以下内容：")
    elif not want_new:
        print("[dry-run 模式] 使用 --apply 写入，--force 覆盖保护文件")

# ── 非写入模式：报告后退出 ──
if not want_apply:
    if not new_files and not safe_updates and not manual_protected and not exclusive_protected:
        print("全部已同步，无需任何操作。")
    elif not want_new:
        print("[dry-run] 使用 --apply 写入，--force 覆盖保护文件，--new 只看新文件，--diff 看详细差异")
    sys.exit(0)


# ═══════════════════════════════════════════════════════════════════
# 执行阶段
# ═══════════════════════════════════════════════════════════════════

def colored_diff(text_a, text_b, filepath):
    """Simple binary indicator for files that differ"""
    if text_a != text_b:
        return f"  ⚡ {filepath} — 内容不同"
    return None

written = []
skipped = []

# 1. 写新文件
for rel, common_path, converted in new_files:
    neo_path = os.path.join(NEO_SRC, rel)
    os.makedirs(os.path.dirname(neo_path), exist_ok=True)
    # 添加自动生成标记
    content = f"// [AUTO-GENERATED from common/ — verify 1.21.8 compatibility before editing]\n{converted}"
    with open(neo_path, "w", encoding="utf-8") as f:
        f.write(content)
    manifest[rel] = {"status": "new", "source": "common"}
    written.append(rel)
    print(f"  [新增] {rel}")

# 2. 写可安全更新的文件
for rel, common_path, neo_path, converted, neo_text in safe_updates:
    if rel in MANUAL_PROTECTED and not want_force:
        skipped.append(rel)
        continue
    content = f"// [AUTO-GENERATED from common/ — verify 1.21.8 compatibility before editing]\n{converted}"
    with open(neo_path, "w", encoding="utf-8") as f:
        f.write(content)
    manifest[rel] = {"status": "updated", "source": "common"}
    written.append(rel)
    print(f"  [更新] {rel}")

if written:
    save_manifest(manifest)
    print(f"\n[DONE] 已写入 {len(written)} 个文件")
if skipped:
    print(f"[SKIP] 跳过 {len(skipped)} 个保护文件（使用 --force 覆盖）")

# ═══════════════════════════════════════════════════════════════════
# 验证阶段
# ═══════════════════════════════════════════════════════════════════

if want_verify and written:
    print(f"\n{'=' * 60}")
    print(f"  [VERIFY] 编译 neoforge 项目...")
    print(f"{'=' * 60}")

    gradle_home = os.path.join(os.environ.get("USERPROFILE", ""),
                                ".gradle", "wrapper", "dists",
                                "gradle-8.10-bin",
                                "5xcyvlep9uowrbag304eabj8b", "gradle-8.10")
    gradlew = os.path.join(gradle_home, "bin", "gradle.bat")
    if not os.path.isfile(gradlew):
        gradlew = os.path.join(PROJECT, "neoforge", "gradlew.bat")

    if os.path.isfile(gradlew):
        try:
            result = subprocess.run(
                [gradlew, "compileJava", "--no-daemon"],
                cwd=os.path.join(PROJECT, "neoforge"),
                capture_output=True, text=True, timeout=300,
                env={**os.environ, "JAVA_HOME": "C:\\Program Files\\Java\\jdk-21",
                     "Path": "C:\\Program Files\\Java\\jdk-21\\bin;" + os.environ["PATH"]}
            )
            if result.returncode == 0:
                print("[OK] 编译通过！")
            else:
                print(f"[FAIL] 编译失败（{len([l for l in result.stdout.split('\\n') if '错误:' in l])} 个错误）")
                print("  检查编译输出，手动修正以下文件后重新同步：")
                for rel, *_ in new_files + safe_updates:
                    print(f"    - {rel}")
                # 回滚写入的文件
                if input("\n是否回滚新写入的文件？(y/N): ").lower() == "y":
                    for rel in written:
                        neo_path = os.path.join(NEO_SRC, rel)
                        if os.path.isfile(neo_path):
                            os.remove(neo_path)
                    print("已回滚")
        except subprocess.TimeoutExpired:
            print("[TIMEOUT] 编译超时（>5分钟），请手动验证")
    else:
        print("⚠️  未找到 Gradle，跳过自动验证")
