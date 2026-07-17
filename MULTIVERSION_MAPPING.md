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

> ⚠️ **Stonecutter 单源方案已评估并放弃**（2026-07）：实测将 `common` 迁移为 Stonecutter 单源后，
> 1.21.8 出现 200+ 编译错误（事件类拆分、渲染 API 不存在等），证明机械化的包名替换并未真正
> 编译通过；且单源预处理会让「全向旋转的 moveVector 反射强制」这类逻辑差异混入 `//? if` 块，可读性下降。
> **当前选定方案 = `common/` 共享源 + `sync-neoforge.sh` 安全同步**，稳定可编译，见文末说明。

## 7️⃣ 专属修改 / 不可自动同步的逻辑差异

> 以下都是**逻辑差异**，不是导入路径差异。`sync-neoforge.sh` 的 sed 规则**无法**处理它们，
> 必须人工移植。这些文件已被脚本识别为「专属适配」并保护（见 `MANUAL_PROTECTED` / `EXCLUSIVE_MARKERS`）。
> 判断原则：common 的对应改动若涉及下表任一模式，应在 neoforge 手动重写，而非依赖同步。

| 文件 | 1.20.1（common） | 1.21.8（neoforge） | 差异类型 |
|------|------------------|--------------------|----------|
| `features/displaymodel/DisplayModelManager.java` | 刷怪蛋用 `CUSTOM_DATA` + `EntityTag` 包装乘客 NBT | `DataComponents.ENTITY_DATA` + `CustomData` 直接作为实体完整存档 NBT | 物品组件化 |
| `features/displaymodel/ModelParser.java` | `fixFloatListValues` 对所有 `[...]` 列表做浮点后缀修补 | 仅对已知浮点向量键（`transformation`/`translation`/`scale`/`left_rotation`/`right_rotation`）修补，保护 `text_display` 的 `text:[{json}]` 不被误改 | 1.21.8 指令结构（含 text_display JSON） |
| `features/displaymodel/DisplayModelManager.java` + `DisplayModelScreen.java` | 报错仅 `setStatus`，屏幕仅在 `isRunning()` 时显示状态 | 报错/完成均 `fireStatusUpdate()`；屏幕 `updateFromManager` 始终显示管理器最新状态（含 `§c` 报错/`§a` 完成） | UI 反馈（避免"点击召唤无反应"因失败静默无可感知） |
| `features/displaymodel/DisplayModelScreen.java` | 指令框默认 `maxLength=32`、默认 `filter` 拒非法字符 | `MAX_COMMAND_LENGTH = 1<<20`；`setFilter(s->true)` 关闭默认过滤；`startSummon` 收集时 `replaceAll("\\R"," ")` 合并换行 | EditBox 陷阱（1.21.8：默认 filter 拒绝含换行/控制字符；粘贴超长模型指令会"输到某处就输不进"。注意 `insertText` 内部 `StringUtil.filterText` 会先剥离换行，故真正的硬截断来自 `setValue`/`filter.test` 拒绝，需 `setFilter(s->true)` 兜底） |
| `features/arrowdmg/ArrowDmgFeature.java` | `Vec3.multiply(double)` | `Vec3.scale(double)`（1.21.8 无 `multiply(double)`） | API 改名 |
| `features/arrowdmg/ArrowDmgFeature.java` | 旧微抖动疾跑包（±1e-10） | 基于视线向量的位置欺骗序列（参考 `ArrowDmg.java`） | 逻辑重写 |
| `util/HotkeySystem.java` | `for(key=32; key<512; key++)` 读 GLFW 键盘缓冲 | 上界 `GLFW.GLFW_KEY_LAST` + `isKeyDown` 安全包装 | 崩溃修复 |
| `features/sprint/SprintHandler.java` | 全向旋转 `targetYaw = getMovementDirection(...)`（与 1.20.1 共享，无 ±90° 偏移）直接用于 `setYRot` | 1.21.x 专属：`ClientTickEvent.Pre/Post` 拆分、`ClientInput`/`Input` API 改名；并在 `MovementInputUpdateEvent` 用反射把 `ClientInput.moveVector` 设为 `(0,1)` 强制纯向前（1.21.8 的 `moveVector` 由 `KeyboardInput.tick()` 从真实按键缓存，只改 `keyPresses` 不生效） | 逻辑/事件重写 |
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

## 🗺️ 方案决策：放弃 Stonecutter，安全同步即终态

`neoforge/` 是独立模块，`sync-neoforge.sh` 用「导入归一化 + 专属标记 + 手动保护列表」避免覆盖，
已能稳定编译出 1.21.8 jar。**不采用 Stonecutter 单源预处理**，原因：

1. **实测失败**：将 `common` 迁为 Stonecutter 单源后，1.21.8 出现 200+ 编译错误（1.20.1 事件类
   `TickEvent` 拆分为 `ClientTickEvent/ServerTickEvent/PlayerTickEvent`；`RenderGuiOverlayEvent`、
   `RegisterShadersEvent`、`ShaderInstance` 等在 1.21.8 根本不存在等）。机械化的 `//? if` 包名替换
   并未真正编译通过，反而引入维护陷阱。
2. **逻辑差异难塞进预处理块**：「强制疾跑全向旋转的 `moveVector` 反射强制纯向前」（`MovementInputUpdateEvent` 中改 `keyPresses` + 反射设 `moveVector=(0,1)`，因 1.21.8 的 `moveVector` 由 `KeyboardInput.tick()` 缓存）、
   `ClientTickEvent.Pre/Post` 拆分、`ClientInput` 等，是整段逻辑重写而非包名差异，硬塞 `//?` 反而更乱。
3. **安全同步已满足需求**：共享 bug 在 `common` 修 → `bash scripts/sync-neoforge.sh --apply` 自动同步；
   专属适配受保护、永不被回滚。这正是「一次开发，多版本构建」的目标，且风险可控。

> 结论：保持 `common/`（共享源）+ `forge/`（直编 common）+ `neoforge/`（安全同步副本）+ `scripts/`
> 的结构，不引入 Stonecutter。如未来确有强需求，再单独评估，但当前不是优先项。
