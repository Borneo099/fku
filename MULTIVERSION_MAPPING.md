# FKU 多版本差异映射表

> 1.20.1 Forge ↔ 1.21.8 NeoForge  
> 自动维护：编辑 `common/` → 运行 `scripts/sync-neoforge.sh` 同步至 neoforge

---

## 1️⃣ 包路径映射

| 描述 | 1.20.1 Forge | 1.21.8 NeoForge |
|------|-------------|-----------------|
| 主事件总线 | `net.minecraftforge.common.MinecraftForge` | `net.neoforged.neoforge.common.NeoForge` |
| 模组事件总线 | `net.minecraftforge.fml.common.Mod.EventBusSubscriber` | `net.neoforged.fml.common.EventBusSubscriber` |
| 客户端分布 | `net.minecraftforge.api.distmarker.OnlyIn`<br>`net.minecraftforge.api.distmarker.Dist` | `net.neoforged.api.distmarker.OnlyIn`<br>`net.neoforged.api.distmarker.Dist` |
| Tick 事件 | `net.minecraftforge.event.TickEvent.ClientTickEvent` | `net.neoforged.neoforge.event.tick.TickEvent.ClientTickEvent` |
| 渲染事件 | `net.minecraftforge.client.event.RenderLevelStageEvent` | `net.neoforged.neoforge.client.event.RenderLevelStageEvent` |
| GUI 覆盖 | `net.minecraftforge.client.event.RenderGuiOverlayEvent` | `net.neoforged.neoforge.client.event.RenderGuiOverlayEvent` |
| 按键注册 | `net.minecraftforge.client.event.RegisterKeyMappingsEvent` | `net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent` |
| 实体攻击事件 | `net.minecraftforge.event.entity.player.AttackEntityEvent` | `net.neoforged.neoforge.event.entity.player.AttackEntityEvent` |
| 玩家网络事件 | `net.minecraftforge.client.event.ClientPlayerNetworkEvent` | `net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent` |
| FML 客户端设置 | `net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent` | `net.neoforged.fml.event.lifecycle.FMLClientSetupEvent` |
| 模组配置 | `net.minecraftforge.fml.config.ModConfig` | `net.neoforged.fml.config.ModConfig` |

## 2️⃣ 常量 / 静态字段映射

| 描述 | 1.20.1 | 1.21.8 |
|------|--------|--------|
| Forge/Neo 事件总线实例 | `MinecraftForge.EVENT_BUS` | `NeoForge.EVENT_BUS` |
| Forge/Neo 模组总线 | `Mod.EventBusSubscriber.Bus.FORGE` | `Mod.EventBusSubscriber.Bus.MOD` |
| 命名空间 | `Mod.EventBusSubscriber(modid = "fku")` | `EventBusSubscriber(modid = "fku")` |
| @Mod 注解 | `net.minecraftforge.fml.common.Mod` | `net.neoforged.fml.common.Mod` |

## 3️⃣ 注册系统映射

| 操作 | 1.20.1 | 1.21.8 |
|------|--------|--------|
| 创建注册表 | `DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID)` | `DeferredRegister.create(NeoForgeRegistries.Keys.ITEMS, MOD_ID)` |
| 注册方块 | `DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID)` | `DeferredRegister.create(NeoForgeRegistries.Keys.BLOCKS, MOD_ID)` |
| 注册实体 | `DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MOD_ID)` | `DeferredRegister.create(NeoForgeRegistries.Keys.ENTITY_TYPES, MOD_ID)` |

## 4️⃣ Mixin 配置差异

| 属性 | 1.20.1 | 1.21.8 |
|------|--------|--------|
| refmap 命名 | `fku.refmap.json` | `fku-common.refmap.json` |
| 插件 | `mixingradle:0.7-SNAPSHOT` | 内置 (neogradle) |
| mixin 环境 | JVM 参数指定 | 插件自动注入 |

## 5️⃣ 构建系统差异

| 属性 | 1.20.1 Forge | 1.21.8 NeoForge |
|------|-------------|-----------------|
| 插件 | `net.minecraftforge.gradle` `[6.0.16,6.2)` | `net.neoforged.gradle.userdev` `7.0.170` |
| Gradle | 8.5 | 8.10+ |
| Java | 17 | 21 |
| 映射 | `official` | `neoform` (默认) |
| 启动类 | `@Mod` | `@Mod` (NeoForge包) |

## 6️⃣ 自动同步命令

```bash
# 从 common/ 同步到 neoforge/ (自动转换导入路径)
cd forge && ./gradlew build    # 构建 1.20.1
cd neoforge && ./gradlew build # 构建 1.21.8
```
