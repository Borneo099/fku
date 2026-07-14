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
apply 's/import net\.minecraftforge\.client\.event\.ClientChatReceivedEvent;/import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;/g'
apply 's/import net\.minecraftforge\.client\.event\.MovementInputUpdateEvent;/import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;/g'
apply 's/import net\.minecraftforge\.client\.event\.RegisterShadersEvent;/import net.neoforged.neoforge.client.event.RegisterShadersEvent;/g'
apply 's/import net\.minecraftforge\.client\.event\.RenderGuiEvent;/import net.neoforged.neoforge.client.event.RenderGuiEvent;/g'
apply 's/import net\.minecraftforge\.client\.event\.ScreenEvent;/import net.neoforged.neoforge.client.event.ScreenEvent;/g'
apply 's/import net\.minecraftforge\.client\.gui\.overlay\.VanillaGuiOverlay;/import net.neoforged.neoforge.client.gui.overlay.VanillaGuiOverlay;/g'

# 通用事件
apply 's/import net\.minecraftforge\.event\.TickEvent;/import net.neoforged.neoforge.event.tick.TickEvent;/g'
apply 's/import net\.minecraftforge\.event\.entity\.player\.AttackEntityEvent;/import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;/g'
apply 's/import net\.minecraftforge\.event\.entity\.living\.LivingFallEvent;/import net.neoforged.neoforge.event.entity.living.LivingFallEvent;/g'

# eventbus
apply 's/import net\.minecraftforge\.eventbus\.api\.SubscribeEvent;/import net.neoforged.bus.api.SubscribeEvent;/g'
apply 's/import net\.minecraftforge\.eventbus\.api\.Event;/import net.neoforged.bus.api.Event;/g'

# fml
apply 's/import net\.minecraftforge\.fml\.event\.lifecycle\.FMLClientSetupEvent;/import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;/g'
apply 's/import net\.minecraftforge\.fml\.config\.ModConfig;/import net.neoforged.fml.config.ModConfig;/g'
apply 's/import net\.minecraftforge\.fml\.ModList;/import net.neoforged.fml.ModList;/g'
apply 's/import net\.minecraftforge\.fml\.common\.Mod;/import net.neoforged.fml.common.Mod;/g'

# common
apply 's/import net\.minecraftforge\.common\.ForgeConfigSpec;/import net.neoforged.neoforge.common.ModConfigSpec;/g'
apply 's/import net\.minecraftforge\.common\.ForgeMod;/import net.neoforged.neoforge.common.NeoForgeMod;/g'
apply 's/import net\.minecraftforge\.entity\.PartEntity;/import net.neoforged.neoforge.entity.PartEntity;/g'

# registries
apply 's/import net\.minecraftforge\.registries\.ForgeRegistries;/import net.neoforged.neoforge.registries.NeoForgeRegistries;/g'

# 行内引用（非 import 语句中的完整限定名）
apply 's/net\.minecraftforge\.client\.event\.RenderGuiOverlayEvent\./net.neoforged.neoforge.client.event.RenderGuiOverlayEvent./g'
apply 's/net\.minecraftforge\.client\.gui\.overlay\.VanillaGuiOverlay\./net.neoforged.neoforge.client.gui.overlay.VanillaGuiOverlay./g'
apply 's/net\.minecraftforge\.registries\.ForgeRegistries\./net.neoforged.neoforge.registries.NeoForgeRegistries./g'
apply 's/net\.minecraftforge\.event\.TickEvent\./net.neoforged.neoforge.event.tick.TickEvent./g'

# 将 @Mod.EventBusSubscriber 的 bus=FORGE 改为 bus=MOD
apply 's/bus = Mod\.EventBusSubscriber\.Bus\.FORGE/bus = Mod.EventBusSubscriber.Bus.MOD/g'

echo "=== 同步完成 ==="
echo "剩余 Forge 引用检查:"
grep -rn "net\.minecraftforge" "$NEO_DIR/src/main/java" --include="*.java" | grep -v "neoforge" || echo " (无残留)"