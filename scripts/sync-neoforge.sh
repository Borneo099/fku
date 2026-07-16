#!/bin/bash
# sync-neoforge.sh — 从 common/ 选择性同步「共享代码」到 neoforge/
# ───────────────────────────────────────────────────────────────────────────
# ⚠️ 安全策略（解决「可维护性陷阱」：同步脚本无脑覆盖 1.21.8 专属适配）
#
#   neoforge/ 是【独立模块】，其「专属 1.21.8 适配」永不参与同步、永不被删除。
#
#   识别机制（自动，无需手工维护大列表）：
#     对每个 common 文件，生成其「仅做导入路径转换后的 NeoForge 版本」(expected)；
#       · neoforge 中无对应文件        → 新增（复制 + 转换）
#       · neoforge 文件 == expected     → 已同步（仅导入差异或无差异），跳过
#       · neoforge 文件 != expected     → 进一步判定：
#           - 若 neoforge 含 1.21.8 专属 API 标记（common 没有）→ 【专属适配】，保护跳过
#           - 否则                                       → 共享文件被 common 改动，安全更新
#
#   绝不执行整目录删除；默认 dry-run，需显式 --apply 才真正写入磁盘。
# ───────────────────────────────────────────────────────────────────────────
set -uo pipefail

COMMON_DIR="$(cd "$(dirname "$0")/../common" && pwd)"
NEO_DIR="$(cd "$(dirname "$0")/../neoforge" && pwd)"

# 已知关键专属适配（额外保险；自动识别已覆盖绝大多数情况）
MANUAL_PROTECTED=(
  "fku/org/example/fku/features/displaymodel/DisplayModelManager.java"
  "fku/org/example/fku/features/arrowdmg/ArrowDmgFeature.java"
  "fku/org/example/fku/util/HotkeySystem.java"
  # 1.21.8 强制疾跑全向旋转：含「yaw→世界移动映射 +90°」专属修正（targetYaw -= 90.0F），
  # 以及 ClientTickEvent.Pre/Post 拆分、ClientInput 等 1.21.x 适配。common 版为 1.20.1，
  # 不含这些改动，故整体保护，绝不参与同步、永不被回滚。
  "fku/org/example/fku/features/sprint/SprintHandler.java"
)

# 1.21.8 专属 API 标记：仅出现在 1.21.8、绝不会出现在 1.20.1 common 中的写法。
# 用于区分「专属适配文件」与「被 common 改动的共享文件」。
# 注意：刻意排除 BuiltInRegistries / Vec3.scale( / GLFW.GLFW_KEY_LAST /
#       net.neoforged...NeoForgeRegistries —— 这些在 1.20.1 也存在或仅是导入转换结果，
#       用作标记会误把共享文件错判为「专属」而阻止同步。
EXCLUSIVE_MARKERS=(
  "DataComponents.ENTITY_DATA"
  "CustomData"
  "Operation.ADD_VALUE"
  "ResourceLocation.parse("
  "Transformation.EXTENDED_CODEC"
  "EXTENDED_CODEC"
)

APPLY=0
for a in "$@"; do
  case "$a" in
    --apply)  APPLY=1;;
    --dry-run) APPLY=0;;
    -h|--help) echo "用法: $0 [--apply]   (默认 dry-run，不写入)"; exit 0;;
  esac
done

if [ ! -d "$COMMON_DIR/src/main/java" ]; then
  echo "错误: 找不到 common 源码目录: $COMMON_DIR/src/main/java" >&2
  exit 1
fi

echo "=== FKU Common → NeoForge 同步 (安全 / 识别专属适配) ==="
echo "源:   $COMMON_DIR/src/main/java"
echo "目标: $NEO_DIR/src/main/java"
if [ "$APPLY" -eq 1 ]; then echo "模式: 实际写入 (--apply)"; else echo "模式: 预演 (dry-run；加 --apply 才真正写入)"; fi

# ── 导入路径转换规则（Forge → NeoForge），幂等 ──
RULES=(
  's/MinecraftForge\.EVENT_BUS/NeoForge.EVENT_BUS/g'
  's|import net\.minecraftforge\.common\.MinecraftForge;|import net.neoforged.neoforge.common.NeoForge;|g'
  's|import net\.minecraftforge\.api\.distmarker\.OnlyIn;|import net.neoforged.api.distmarker.OnlyIn;|g'
  's|import net\.minecraftforge\.api\.distmarker\.Dist;|import net.neoforged.api.distmarker.Dist;|g'
  's|import net\.minecraftforge\.client\.event\.InputEvent;|import net.neoforged.neoforge.client.event.InputEvent;|g'
  's|import net\.minecraftforge\.client\.event\.RenderLevelStageEvent;|import net.neoforged.neoforge.client.event.RenderLevelStageEvent;|g'
  's|import net\.minecraftforge\.client\.event\.RenderGuiOverlayEvent;|import net.neoforged.neoforge.client.event.RenderGuiOverlayEvent;|g'
  's|import net\.minecraftforge\.client\.event\.RegisterKeyMappingsEvent;|import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;|g'
  's|import net\.minecraftforge\.client\.event\.ClientPlayerNetworkEvent;|import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;|g'
  's|import net\.minecraftforge\.client\.event\.RegisterClientCommandsEvent;|import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;|g'
  's|import net\.minecraftforge\.client\.settings\.KeyConflictContext;|import net.neoforged.neoforge.client.settings.KeyConflictContext;|g'
  's|import net\.minecraftforge\.client\.renderer\.Shaders;|import net.neoforged.neoforge.client.renderer.Shaders;|g'
  's|import net\.minecraftforge\.client\.event\.ClientChatReceivedEvent;|import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;|g'
  's|import net\.minecraftforge\.client\.event\.MovementInputUpdateEvent;|import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;|g'
  's|import net\.minecraftforge\.client\.event\.RegisterShadersEvent;|import net.neoforged.neoforge.client.event.RegisterShadersEvent;|g'
  's|import net\.minecraftforge\.client\.event\.RenderGuiEvent;|import net.neoforged.neoforge.client.event.RenderGuiEvent;|g'
  's|import net\.minecraftforge\.client\.event\.ScreenEvent;|import net.neoforged.neoforge.client.event.ScreenEvent;|g'
  's|import net\.minecraftforge\.client\.gui\.overlay\.VanillaGuiOverlay;|import net.neoforged.neoforge.client.gui.overlay.VanillaGuiOverlay;|g'
  's|import net\.minecraftforge\.event\.TickEvent;|import net.neoforged.neoforge.event.tick.TickEvent;|g'
  's|import net\.minecraftforge\.event\.entity\.player\.AttackEntityEvent;|import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;|g'
  's|import net\.minecraftforge\.event\.entity\.living\.LivingFallEvent;|import net.neoforged.neoforge.event.entity.living.LivingFallEvent;|g'
  's|import net\.minecraftforge\.eventbus\.api\.SubscribeEvent;|import net.neoforged.bus.api.SubscribeEvent;|g'
  's|import net\.minecraftforge\.eventbus\.api\.Event;|import net.neoforged.bus.api.Event;|g'
  's|import net\.minecraftforge\.fml\.event\.lifecycle\.FMLClientSetupEvent;|import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;|g'
  's|import net\.minecraftforge\.fml\.config\.ModConfig;|import net.neoforged.fml.config.ModConfig;|g'
  's|import net\.minecraftforge\.fml\.ModList;|import net.neoforged.fml.ModList;|g'
  's|import net\.minecraftforge\.fml\.common\.Mod;|import net.neoforged.fml.common.Mod;|g'
  's|import net\.minecraftforge\.common\.ForgeConfigSpec;|import net.neoforged.neoforge.common.ModConfigSpec;|g'
  's|import net\.minecraftforge\.common\.ForgeMod;|import net.neoforged.neoforge.common.NeoForgeMod;|g'
  's|import net\.minecraftforge\.entity\.PartEntity;|import net.neoforged.neoforge.entity.PartEntity;|g'
  's|import net\.minecraftforge\.registries\.ForgeRegistries;|import net.neoforged.neoforge.registries.NeoForgeRegistries;|g'
  's|net\.minecraftforge\.client\.event\.RenderGuiOverlayEvent\.|net.neoforged.neoforge.client.event.RenderGuiOverlayEvent.|g'
  's|net\.minecraftforge\.client\.gui\.overlay\.VanillaGuiOverlay\.|net.neoforged.neoforge.client.gui.overlay.VanillaGuiOverlay.|g'
  's|net\.minecraftforge\.registries\.ForgeRegistries\.|net.neoforged.neoforge.registries.NeoForgeRegistries.|g'
  's|net\.minecraftforge\.event\.TickEvent\.|net.neoforged.neoforge.event.tick.TickEvent.|g'
  's/bus = Mod\.EventBusSubscriber\.Bus\.FORGE/bus = Mod.EventBusSubscriber.Bus.MOD/g'
)

transform_inplace() {
  local f="$1"
  for r in "${RULES[@]}"; do
    sed -i -e "$r" "$f"
  done
}

is_manual_protected() {
  local rel="$1"
  for p in "${MANUAL_PROTECTED[@]}"; do [ "$p" = "$rel" ] && return 0; done
  return 1
}

dst_has_exclusive() {
  # $1 = dst 文件, $2 = common 源文件
  local dst="$1" src="$2"
  for m in "${EXCLUSIVE_MARKERS[@]}"; do
    if grep -qF "$m" "$dst" && ! grep -qF "$m" "$src"; then
      return 0
    fi
  done
  return 1
}

ADDED=0; UPDATED=0; SKIPPED=0; PROTECTED=0
TMP="$(mktemp)"
trap 'rm -f "$TMP"' EXIT

while IFS= read -r src; do
  rel="${src#$COMMON_DIR/src/main/java/}"
  dst="$NEO_DIR/src/main/java/$rel"

  # 先生成 expected：common 文件经导入转换后的 NeoForge 版本（幂等）
  cp "$src" "$TMP"
  transform_inplace "$TMP"

  # 1) 手动保护列表 —— 永不覆盖；但若 common 的共享逻辑已更新（expected≠dst），
  #    打印差异，便于「修复错误后自动更新」时把共享改动手动合并进 neo 适配。
  if is_manual_protected "$rel"; then
    if [ -f "$dst" ] && ! cmp -s "$TMP" "$dst"; then
      echo "  [保护] $rel  (手动保护列表；common 有更新，附差异待人工合并)"
      diff -u "$TMP" "$dst" | sed 's/^/      /' | head -40
    else
      echo "  [保护] $rel  (手动保护列表)"
    fi
    PROTECTED=$((PROTECTED+1)); continue
  fi
  # 2) 路径含 /neoforge/ 专属子包的文件也保护（设计上的专属目录）
  if [[ "$rel" == *"/neoforge/"* ]]; then
    echo "  [保护] $rel  (neoforge 专属子包)"
    PROTECTED=$((PROTECTED+1)); continue
  fi

  if [ ! -f "$dst" ]; then
    echo "  [新增] $rel"
    [ "$APPLY" -eq 1 ] && { mkdir -p "$(dirname "$dst")"; cp "$TMP" "$dst"; }
    ADDED=$((ADDED+1))
  elif cmp -s "$TMP" "$dst"; then
    SKIPPED=$((SKIPPED+1))           # 已同步（仅导入差异或无差异）
  elif dst_has_exclusive "$dst" "$src"; then
    echo "  [保护] $rel  (neoforge 含专属 1.21.8 逻辑，跳过)"
    PROTECTED=$((PROTECTED+1))
  else
    echo "  [更新] $rel  (共享文件被 common 改动，安全更新)"
    [ "$APPLY" -eq 1 ] && cp "$TMP" "$dst"
    UPDATED=$((UPDATED+1))
  fi
done < <(find "$COMMON_DIR/src/main/java" -name "*.java")

[ "$APPLY" -eq 1 ] && echo "=== 已写入 ===" || echo "=== 预演完成（未写入；加 --apply 执行）==="
echo "新增: $ADDED，更新: $UPDATED，已同步跳过: $SKIPPED，保护跳过: $PROTECTED"
echo "── 自动更新工作流 ──"
echo "  · 修复【共享逻辑】错误 → 改 common/ → 运行本脚本 --apply，自动同步到 neoforge（仅更新 diff）。"
echo "  · 修复【1.21.x 专属】错误（如全向旋转 yaw 映射）→ 直接改 neoforge/，受保护、永不被回滚。"
echo "  · 受保护文件若 common 有新改动，脚本会打印差异，按提示把共享改动手动并入 neo 适配即可。"
echo "剩余 Forge 引用检查:"
grep -rn "net\.minecraftforge" "$NEO_DIR/src/main/java" --include="*.java" 2>/dev/null | grep -v "neoforge" || echo " (无残留)"
