#!/bin/bash
# sync-neoforge.sh — 从 common/ 同步代码到 neoforge/，自动转换导入路径
set -e

COMMON_DIR="$(cd "$(dirname "$0")/../common" && pwd)"
NEO_DIR="$(cd "$(dirname "$0")/../neoforge" && pwd)"

echo "=== FKU Common → NeoForge 同步 ==="
echo "源: $COMMON_DIR/src/main/java"
echo "目标: $NEO_DIR/src/main/java"

# 清理旧 NeoForge 源（保留平台特定文件）
find "$NEO_DIR/src/main/java" -name "*.java" -not -path "*/neoforge/*" -delete 2>/dev/null || true

# 复制所有通用源文件
mkdir -p "$NEO_DIR/src/main/java"
cp -r "$COMMON_DIR/src/main/java/"* "$NEO_DIR/src/main/java/"
echo "已复制 $(find "$COMMON_DIR/src/main/java" -name '*.java' | wc -l) 个文件"

# ── 执行导入路径转换 ──
cd "$NEO_DIR"
echo "执行导入路径转换..."

# 逐条执行替换
apply() {
  local pattern="$1"
  local replacement="$2"
  find src/main/java -name "*.java" -exec sed -i "$pattern" {} \;
}

# === 替换规则 ===

# MinecraftForge → NeoForge (代码中引用)
apply 's/MinecraftForge\.EVENT_BUS/NeoForge.EVENT_BUS/g'
apply 's/import net\.minecraftforge\.common\.MinecraftForge;/import net.neoforged.neoforge.common.NeoForge;/g'

# api.distmarker
apply 's/import net\.minecraftforge\.api\.distmarker\.OnlyIn;/import net.neoforged.api.distmarker.OnlyIn;/g'
apply 's/import net\.minecraftforge\.api\.distmarker\.Dist;/import net.neoforged.api.distmarker.Dist;/g'

# 客户端事件
apply 's/import net\.minecraftforge\.client\.event\.InputEvent;/import net.neoforged.neoforge.client.event.InputEvent;/g'
apply 's|import net\.minecraftforge\.client\.event\.RenderLevelStageEvent;|import net.neoforged.neoforge.client.event.RenderLevelStageEvent;|g'
apply 's|import net\.minecraftforge\.client\.event\.RenderGuiOverlayEvent;|import net.neoforged.neoforge.client.event.RenderGuiOverlayEvent;|g'
apply 's/import net\.minecraftforge\.client\.event\.RegisterKeyMappingsEvent;/import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;/g'
apply 's/import net\.minecraftforge\.client\.event\.ClientPlayerNetworkEvent;/import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;/g'
apply 's/import net\.minecraftforge\.client\.event\.RegisterClientCommandsEvent;/import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;/g'
apply 's/import net\.minecraftforge\.client\.settings\.KeyConflictContext;/import net.neoforged.neoforge.client.settings.KeyConflictContext;/g'
apply 's/import net\.minecraftforge\.client\.renderer\.Shaders;/import net.neoforged.neoforge.client.renderer.Shaders;/g' 2>/dev/null || true

# 通用事件
apply 's/import net\.minecraftforge\.event\.TickEvent;/import net.neoforged.neoforge.event.tick.TickEvent;/g'
apply 's/import net\.minecraftforge\.event\.entity\.player\.AttackEntityEvent;/import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;/g'

# fml
apply 's/import net\.minecraftforge\.fml\.event\.lifecycle\.FMLClientSetupEvent;/import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;/g'
apply 's/import net\.minecraftforge\.fml\.config\.ModConfig;/import net.neoforged.fml.config.ModConfig;/g'

# @Mod.EventBusSubscriber 属性 (FORGE→MOD)
apply 's/@Mod\.EventBusSubscriber(modid = "fku", bus = Mod\.EventBusSubscriber\.Bus\.FORGE, value = Dist\.CLIENT)/@Mod.EventBusSubscriber(modid = "fku", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)/g'

# Registries
apply 's/import net\.minecraftforge\.registries\.\(ForgeRegistries\|IForgeRegistry\|RegisterEvent\);/import net.neoforged.neoforge.registries.\1;/g' 2>/dev/null || true

echo "=== 同步完成 ==="
echo "剩余 Forge 引用检查:"
grep -rn "net\.minecraftforge" "$NEO_DIR/src/main/java" --include="*.java" | grep -v "neoforge" || echo " (无残留)"
