# FKU 模组新功能开发计划

## 1. 概述

**目标**: 在现有 fku 模组基础上添加新功能模块，遵循项目规范：
- ClickGUI 菜单开关复用已有组件（`ToggleComponent`、`GuiComponent` 等）
- 功能逻辑放在新包路径 `fku.org.example.fku.features.<功能名>` 下
- 保持与项目现有风格一致

**Forge 版本**: 1.20.1 (Forge 47.3.0)

---

## 2. 当前代码库分析

### 2.1 包结构
```
fku.org.example.fku/
├── Fku.java                    # Mod 入口
├── client/
│   ├── gui/
│   │   ├── ClickGuiScreen.java # GUI 主屏幕
│   │   └── components/
│   │       ├── GuiPanel.java        # 面板基类（可拖拽、折叠）
│   │       ├── GuiComponent.java    # 组件基类
│   │       ├── ToggleComponent.java # 开关组件基类
│   │       ├── MovementPanel.java   # 移动面板
│   │       ├── OtherPanel.java      # 其它面板
│   │       ├── ToolPanel.java       # 工具面板
│   │       └── VisualPanel.java     # 视觉面板
│   └── KeyBindings.java        # 按键绑定
├── config/
│   ├── FkuConfig.java          # TOML 配置（GUI 位置）
│   └── MovementConfig.java      # JSON 配置（功能开关）
└── features/
    └── autodrop/                # 功能模块示例
        ├── AutoDropConfig.java
        ├── AutoDropHandler.java
        └── AutoDropScreen.java
```

### 2.2 现有组件模式

**ToggleComponent** (抽象类):
```java
public abstract class ToggleComponent extends GuiComponent {
    protected abstract boolean isEnabled();   // 读取状态
    protected abstract void toggle();         // 切换状态
    protected abstract void saveConfig();    // 保存配置
}
```

**GuiPanel** (抽象面板):
```java
public abstract class GuiPanel {
    protected abstract void init();        // 添加组件
    protected abstract void savePosition(); // 保存面板位置
}
```

**配置模式**:
- 位置/按键: `FkuConfig` (TOML)
- 功能开关: 独立 JSON 配置文件（如 `MovementConfig`）

---

## 3. 待开发功能 (TBD)

**功能名称**: ____________

**功能描述**: ____________

**需要的功能组件**:
- [ ] ClickGUI 开关按钮 (ToggleComponent)
- [ ] 独立配置面板 (GuiPanel)
- [ ] 功能处理器 (Handler)
- [ ] 配置类 (Config)

**是否需要网络包处理**: 是 / 否

**主要机制**: ____________

---

## 4. 实现步骤

### 步骤 1: 创建功能包和配置类

**新包路径**: `src/main/java/fku/org/example/fku/features/<功能名>/`

**新建文件**:
1. `<FeatureName>Config.java` - 功能配置（参考 `AutoDropConfig.java`）
2. `<FeatureName>Handler.java` - 功能逻辑处理器（使用 Forge EventBus）
3. （可选）`<FeatureName>Screen.java` - 独立配置界面

### 步骤 2: 创建 GUI 组件

**在 `client.gui.components` 包创建**:
1. `<FeatureName>Component.java` - 继承 `ToggleComponent` 或 `GuiComponent`
2. （可选）`<FeatureName>Panel.java` - 如果需要独立面板，继承 `GuiPanel`

### 步骤 3: 注册组件到 ClickGUI

**修改 `ClickGuiScreen.java`**:
```java
panels.add(new FeaturePanel()); // 添加新面板
```

**或修改现有面板（如 `ToolPanel.java`）**:
```java
addComponent(new FeatureComponent(0, 0, 110, 25));
```

### 步骤 4: 添加 Forge 配置项（如果需要面板位置）

**修改 `FkuConfig.java`**:
```java
public static ForgeConfigSpec.IntValue featureXPos;
public static ForgeConfigSpec.IntValue featureYPos;
```

---

## 5. 关键文件参考

| 目的 | 参考文件 |
|------|----------|
| ToggleComponent 实现 | `NoJumpDelayComponent.java` |
| 自定义组件实现 | `AutoDropComponent.java` |
| Panel 实现 | `MovementPanel.java`, `ToolPanel.java` |
| JSON 配置 | `MovementConfig.java`, `AutoDropConfig.java` |
| TOML 配置 | `FkuConfig.java` |
| Event Bus 注册 | `NoJumpDelayHandler.java` |

---

## 6. 验证步骤

1. **编译测试**: `./gradlew build`
2. **运行游戏**: `./gradlew runClient`
3. **功能测试**:
   - 打开 ClickGUI (右Shift)
   - 验证开关状态切换
   - 验证配置保存/加载
   - （如有网络包）验证数据包发送/接收

---

## 7. 待确认事项

- [ ] 具体功能名称是什么？
- [ ] 功能的主要机制是什么？
- [ ] 需要哪些 GUI 组件？
- [ ] 是否需要网络包处理？如果是，预期处理什么数据包？
- [ ] 功能应该放在哪个现有面板（ToolPanel/MovementPanel/OtherPanel）还是需要新建面板？
