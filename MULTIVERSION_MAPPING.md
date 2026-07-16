# FKU 多版本差异映射表

> 1.20.1 Forge ↔ 1.21.8 NeoForge  
> 单源模型（Stonecutter 迁移已完成 ✅）：唯一事实来源是 `src/`（由旧 `common/` 重命名而来），
> 加载器差异用 `//? if neoforge { … } //? }` 条件编译表达，Stonecutter 在生成时按加载器展开。
>
> ⚠️ `scripts/sync-neoforge.sh` 已**废弃**（原 `common/` → `neoforge/` 复制式同步脚本）：
> 单源模型下不再有第二个可覆盖的项目，原「无脑覆盖 1.21.8 专属适配」的危险已从根本上根除。
> 新工作流见第 6 节。

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

## 6️⃣ 单源工作流（Stonecutter）

```bash
# 1) 生成 NeoForge 1.21.8 工程（在 versions/1.21.8-neoforge/build/generated/stonecutter/ 展开 src/）
./gradlew :1.21.8-neoforge:stonecutterGenerate

# 2) 生成 Forge 1.20.1 工程
./gradlew :1.20.1-forge:stonecutterGenerate

# 3) 构建（需先在 build.forge.gradle.kts / build.neoforge.gradle.kts 接入
#    ForgeGradle / ModDevGradle，目前为最小 plugins { java } 占位）
./gradlew :1.20.1-forge:build
./gradlew :1.21.8-neoforge:build
```

工作模型：
- `src/main/java` 是**唯一**共享源码；`//? if neoforge { … } //? }` 块按加载器展开，
  非活跃分支被包进 `/* … */` 注释，绝不互相污染。
- `src/main/resources` 是共享资源（lang / 贴图 / 着色器 / pack.mcmeta）。
- `versions/1.20.1-forge/src/main/resources` 与 `versions/1.21.8-neoforge/src/main/resources`
  仅放各加载器**独有**配置：`mods.toml` / `neoforge.mods.toml` 与 `fku.mixins.json`。
- forge↔neo 的导入 / 简名 / API 文本替换由 `stonecutter.gradle.kts` 的 `replacements { }` 统一完成
  （如 `MinecraftForge`→`NeoForge`、`net.minecraftforge.*`→`net.neoforged.*`），无需用 `//?` 包每个 import。

> 旧 `scripts/sync-neoforge.sh`（common → neoforge 复制式同步）已废弃：单源下无第二个项目可覆盖，
> 原「专属适配被无脑覆盖」的风险已不存在。详见脚本内说明。

## 7️⃣ 专属修改 / 不可自动同步的逻辑差异

> 以下都是**逻辑差异**，不是导入路径差异。`stonecutter.gradle.kts` 的 `replacements` 文本替换**无法**
> 处理它们，必须在 `src/` 中用 `//? if neoforge { … } //? }` 条件块表达。
> 判断原则：`src/` 的对应代码若涉及下表任一模式，应把 1.21.8 分支包进 `//? if neoforge { }`，
> 必要时用 `//? } else { }` 提供 1.20.1 分支。

| 文件 | 1.20.1（common） | 1.21.8（neoforge） | 差异类型 |
|------|------------------|--------------------|----------|
| `features/displaymodel/DisplayModelManager.java` | 刷怪蛋用 `CUSTOM_DATA` + `EntityTag` 包装乘客 NBT | `DataComponents.ENTITY_DATA` + `CustomData` 直接作为实体完整存档 NBT | 物品组件化 |
| `features/arrowdmg/ArrowDmgFeature.java` | `Vec3.multiply(double)` | `Vec3.scale(double)`（1.21.8 无 `multiply(double)`） | API 改名 |
| `features/arrowdmg/ArrowDmgFeature.java` | 旧微抖动疾跑包（±1e-10） | 基于视线向量的位置欺骗序列（参考 `ArrowDmg.java`） | 逻辑重写 |
| `util/HotkeySystem.java` | `for(key=32; key<512; key++)` 读 GLFW 键盘缓冲 | 上界 `GLFW.GLFW_KEY_LAST` + `isKeyDown` 安全包装 | 崩溃修复 |
| 通用 | `AttributeModifier.Operation.ADDITION` | `Operation.ADD_VALUE` | 枚举改名 |
| 通用 | `new ResourceLocation(x)` | `ResourceLocation.parse(x)` | API 惯用法 |
| 通用 | `ForgeRegistries.X.getValue(new ResourceLocation(...))` | 原版注册表改用 `BuiltInRegistries.X.getValue(ResourceLocation.parse(...))` | 注册表来源 |
| 通用 | `VanillaGuiOverlay.X.type()` | `VanillaGuiOverlay.X.id()` | 方法改名 |
| 通用 | `BlockState` / `Transformation` 旧 codec | `Transformation.EXTENDED_CODEC` 接受扁平 16-float 形式 | codec 变更 |

**新增专属适配时的操作清单**：
1. 在 `src/` 对应文件内，把 1.21.8 专属逻辑包进 `//? if neoforge { … } //? }`；
2. 若 1.20.1 分支需不同实现，用 `//? } else { … //? }` 提供 forge 分支；
3. 纯导入 / 简名差异交给 `stonecutter.gradle.kts` 的 `replacements`，不用 `//?`；
4. 把新的 1.20.1↔1.21.8 差异补进本表第 7 节。

## 🗺️ 路线图：Stonecutter 单源迁移（已完成 ✅）

`common/` 已重命名为 `src/` 成为唯一共享源；`forge/`、`neoforge/` 旧独立项目已移除；
第 7 节的「专属修改」已全部改写为 `src/` 内 `//? if neoforge { }` 条件块；
`stonecutterGenerate` 对两个加载器均验证通过（neo 无 `net.minecraftforge` 残留，双分支花括号平衡）。

剩余收尾（Task #35，需联网拉取插件）：
- 在 `build.forge.gradle.kts` / `build.neoforge.gradle.kts` 接入 `ForgeGradle` / `ModDevGradle`，
  使 `./gradlew :<ver>-<loader>:build` 能真正产出 jar（当前为最小 `plugins { java }` 占位）。
- 完成后 `versions/<ver>-<loader>/build/` 下的生成产物随构建产生，已被 `.gitignore` 忽略。

> 历史：`sync-neoforge.sh` 的「复制式同步」方案已废弃，单源模型下专属逻辑绝不会被覆盖。
