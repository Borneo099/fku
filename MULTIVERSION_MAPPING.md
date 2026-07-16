# FKU 多版本差异映射表

> 1.20.1 Forge ↔ 1.21.8 NeoForge  
> 自动维护：编辑 `common/` → 运行 `scripts/sync-neoforge.sh` 同步至 neoforge
>
> ⚠️ **neoforge/ 是独立模块**：其「专属 1.21.8 适配」永不参与同步、永不被覆盖。
> `sync-neoforge.sh` 默认 dry-run，用「导入路径归一化 + 1.21.8 专属标记」自动识别并保护专属文件，
> 绝不会执行整目录删除。详见第 7 节与脚本内 `MANUAL_PROTECTED` / `EXCLUSIVE_MARKERS`。

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

## 6️⃣ 自动同步命令（安全模式）

```bash
# 1) 预演：看 sync 会改变哪些文件（默认，不写盘）
bash scripts/sync-neoforge.sh

# 2) 确认无误后，实际写入
bash scripts/sync-neoforge.sh --apply

# 3) 分别构建两个版本
cd forge    && ./gradlew build   # 构建 1.20.1
cd neoforge && ./gradlew build   # 构建 1.21.8
```

脚本行为：
- **永不删除** neoforge 任何文件；默认 dry-run，需 `--apply` 才写入。
- 对每个 common 文件生成「仅做导入转换后的 NeoForge 版本」(expected)：
  - neoforge 无对应文件 → 新增；neoforge == expected → 已同步跳过；
  - neoforge ≠ expected 且含 1.21.8 专属标记 → **保护跳过**（见第 7 节）；
  - neoforge ≠ expected 且无专属标记 → 视为共享文件被 common 改动，**安全更新**。
- 被保护的文件若需同步 common 的改动，请**人工移植**到 neoforge 对应文件。

> 真正的「单源多版本」长期方案是 **Stonecutter 预处理**：同一份 `common` 代码用
> `#//if MC_1_21_8 … #//else … #//endif` 在构建时自动适配，不再需要复制式同步。
> 见文末「路线图」。

## 7️⃣ 专属修改 / 不可自动同步的逻辑差异

> 以下都是**逻辑差异**，不是导入路径差异。`sync-neoforge.sh` 的 sed 规则**无法**处理它们，
> 必须人工移植。这些文件已被脚本识别为「专属适配」并保护（见 `MANUAL_PROTECTED` / `EXCLUSIVE_MARKERS`）。
> 判断原则：common 的对应改动若涉及下表任一模式，应在 neoforge 手动重写，而非依赖同步。

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
1. 在 neoforge 写好 1.21.8 专属逻辑；
2. 把它加入 `scripts/sync-neoforge.sh` 的 `MANUAL_PROTECTED`（保险）；
3. 把对应的 1.20.1↔1.21.8 差异补进本表第 7 节；
4. 绝不要把专属逻辑写回 `common/`，否则会被同步脚本当作共享代码复制。

## 🗺️ 路线图：从「复制式同步」到「Stonecutter 单源」

当前 `neoforge/` 是独立模块，`sync-neoforge.sh` 用「导入归一化 + 专属标记」避免覆盖。
下一步彻底消除维护陷阱的做法：

1. 引入 **Stonecutter** Gradle 插件到 `common/`；
2. 把第 7 节的「专属修改」改写为同一文件内的 `#//if MC_1_21_8 … #//else … #//endif` 预处理块；
3. `forge/` 与 `neoforge/` 仅保留各自的 `gradle.build` / 资源 / 真正平台独有的新特性；
4. 构建 1.20.1 与 1.21.8 时，Stonecutter 自动裁剪出对应版本代码 —— **不再有任何复制脚本**，
   专属逻辑也绝不会被覆盖。

> 此迁移涉及构建系统改动，建议在确认 `sync-neoforge.sh` 安全策略稳定后再实施。
