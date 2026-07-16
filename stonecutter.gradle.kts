plugins {
    id("dev.kikugie.stonecutter")
}

// 默认激活版本：Forge 1.20.1（与 common 当前源码一致，IDE 默认看到 Forge 视图）。
stonecutter active "1.20.1-forge"

// 见 https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    val (version, loader) = current.project.split("-", limit = 2)

    // 让版本/加载器专属属性（来自 stonecutter.properties.toml，可选）生效
    properties {
        tags(version, loader)
    }

    // 条件常量：loader 名即布尔常量，可在 //? if neoforge { ... } 中使用
    constants {
        match(loader, "forge", "neoforge")
    }

    // ── Forge 1.20.1 → NeoForge 1.21.8 的纯文本映射（由 replacements 完成，无需 //? 包裹每个 import）──
    // 说明：.java 文件在 Stonecutter 0.9.6 中使用 //?（Slash）风格；#// 仅用于配置类文件。
    // 这里用 replacements 做机械的文本替换，覆盖 28 个导入映射 + 简名映射 + 少数 API 重命名。
    replacements {
        // 仅当构建 neoforge 版本时应用这些替换（loader 由 parameters 顶部解析得到）
        string(loader == "neoforge") {
            // —— 1) 完整限定名导入映射（同时覆盖行内限定名用法）——
            // 纯前缀替换（net.minecraftforge. -> net.neoforged.）
            replace("net.minecraftforge.api.distmarker.Dist", "net.neoforged.api.distmarker.Dist")
            replace("net.minecraftforge.fml.common.Mod", "net.neoforged.fml.common.Mod")
            replace("net.minecraftforge.api.distmarker.OnlyIn", "net.neoforged.api.distmarker.OnlyIn")
            replace("net.minecraftforge.fml.ModList", "net.neoforged.fml.ModList")
            replace("net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent", "net.neoforged.fml.event.lifecycle.FMLClientSetupEvent")
            replace("net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent", "net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent")
            replace("net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext", "net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext")
            replace("net.minecraftforge.eventbus.api.IEventBus", "net.neoforged.bus.api.IEventBus")
            replace("net.minecraftforge.fml.config.ModConfig", "net.neoforged.fml.config.ModConfig")
            // 路径变化（插入 neoforge / eventbus->bus）
            replace("net.minecraftforge.eventbus.api.SubscribeEvent", "net.neoforged.bus.api.SubscribeEvent")
            replace("net.minecraftforge.event.TickEvent", "net.neoforged.neoforge.event.tick.TickEvent")
            replace("net.minecraftforge.client.event.InputEvent", "net.neoforged.neoforge.client.event.InputEvent")
            // —— 注册表来源（NeoForge 1.21 硬移除 ForgeRegistries，原版注册表改用 BuiltInRegistries）——
            replace("net.minecraftforge.registries.ForgeRegistries", "net.minecraft.core.registries.BuiltInRegistries")
            replace("ForgeRegistries.BLOCKS", "BuiltInRegistries.BLOCKS")
            replace("ForgeRegistries.ITEMS", "BuiltInRegistries.ITEMS")
            replace("ForgeRegistries.ENTITY_TYPES", "BuiltInRegistries.ENTITY_TYPES")
            replace("ForgeRegistries.ENCHANTMENTS", "BuiltInRegistries.ENCHANTMENTS")
            replace("net.minecraftforge.common.MinecraftForge", "net.neoforged.neoforge.common.NeoForge")
            replace("net.minecraftforge.client.event.RenderLevelStageEvent", "net.neoforged.neoforge.client.event.RenderLevelStageEvent")
            replace("net.minecraftforge.event.entity.player.AttackEntityEvent", "net.neoforged.neoforge.event.entity.player.AttackEntityEvent")
            replace("net.minecraftforge.client.event.RegisterClientCommandsEvent", "net.neoforged.neoforge.client.event.RegisterClientCommandsEvent")
            replace("net.minecraftforge.client.gui.overlay.VanillaGuiOverlay", "net.neoforged.neoforge.client.gui.overlay.VanillaGuiOverlay")
            replace("net.minecraftforge.client.event.ScreenEvent", "net.neoforged.neoforge.client.event.ScreenEvent")
            replace("net.minecraftforge.client.event.RenderGuiOverlayEvent", "net.neoforged.neoforge.client.event.RenderGuiOverlayEvent")
            replace("net.minecraftforge.client.event.RegisterKeyMappingsEvent", "net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent")
            replace("net.minecraftforge.client.event.ClientPlayerNetworkEvent", "net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent")
            replace("net.minecraftforge.event.entity.living.LivingFallEvent", "net.neoforged.neoforge.event.entity.living.LivingFallEvent")
            replace("net.minecraftforge.entity.PartEntity", "net.neoforged.neoforge.entity.PartEntity")
            replace("net.minecraftforge.common.ForgeMod", "net.neoforged.neoforge.common.NeoForgeMod")
            // ForgeMod.BLOCK_REACH（1.20.1）→ 原版 Attributes.BLOCK_INTERACTION_RANGE（1.21）
            replace("ForgeMod.BLOCK_REACH", "Attributes.BLOCK_INTERACTION_RANGE")
            replace("net.minecraftforge.common.ForgeConfigSpec", "net.neoforged.neoforge.common.ModConfigSpec")
            replace("net.minecraftforge.client.settings.KeyConflictContext", "net.neoforged.neoforge.client.settings.KeyConflictContext")
            replace("net.minecraftforge.client.event.RenderGuiEvent", "net.neoforged.neoforge.client.event.RenderGuiEvent")
            replace("net.minecraftforge.client.event.RegisterShadersEvent", "net.neoforged.neoforge.client.event.RegisterShadersEvent")
            replace("net.minecraftforge.client.event.MovementInputUpdateEvent", "net.neoforged.neoforge.client.event.MovementInputUpdateEvent")
            replace("net.minecraftforge.client.event.ClientChatReceivedEvent", "net.neoforged.neoforge.client.event.ClientChatReceivedEvent")

            // —— 2) 行内简名映射（必须在完整限定名之后，避免再次命中已替换的导入）——
            replace("MinecraftForge", "NeoForge")
            replace("ForgeConfigSpec", "ModConfigSpec")

            // —— 3) 少数 API 重命名 ——
            // 注意：string {} 块走 TrieSearcher，from 是【字面量子串】，不是正则，不要加反斜杠转义。
            // Operation.ADD_VALUE (Forge 1.20.1) -> Operation.ADDITION (NeoForge 1.21.8)
            replace("Operation.ADD_VALUE", "Operation.ADDITION")
            // VanillaGuiOverlay.X.type() (Forge) -> .id() (NeoForge)
            // 简写形式（TpAuraFeature / YPosOverlay 用 import 简名）
            replace("VanillaGuiOverlay.CROSSHAIR.type()", "VanillaGuiOverlay.CROSSHAIR.id()")
            replace("VanillaGuiOverlay.HOTBAR.type()", "VanillaGuiOverlay.HOTBAR.id()")
            // 全限定形式（ArrowDmgFeature:367 行内写了完整包名），自包含一条避免与 FQCN 规则的顺序冲突
            replace("net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.CROSSHAIR.type()", "net.neoforged.neoforge.client.gui.overlay.VanillaGuiOverlay.CROSSHAIR.id()")

            // —— 4) 数据包构造签名（NeoForge 1.21 的 ServerboundMovePlayerPacket 多一个布尔参数）——
            // 这些是纯机械的「插入 true」，用字面量替换（string{} 走 TrieSearcher，from 即字面量）。
            replace("new ServerboundMovePlayerPacket.Pos(p.getX(), safeY, p.getZ(), p.onGround())", "new ServerboundMovePlayerPacket.Pos(p.getX(), safeY, p.getZ(), true, p.onGround())")
            replace("new ServerboundMovePlayerPacket.Rot(yaw, pitch, p.onGround())", "new ServerboundMovePlayerPacket.Rot(yaw, pitch, true, p.onGround())")
            replace("new ServerboundMovePlayerPacket.Pos(p.getX(), p.getY(), p.getZ(), p.onGround())", "new ServerboundMovePlayerPacket.Pos(p.getX(), p.getY(), p.getZ(), true, p.onGround())")
            replace("new ServerboundMovePlayerPacket.PosRot(shootPos.x, shootPos.y, shootPos.z, yaw, pitch, false)", "new ServerboundMovePlayerPacket.PosRot(shootPos.x, shootPos.y, shootPos.z, yaw, pitch, true, false)")
            replace("new ServerboundMovePlayerPacket.PosRot(orig.x, orig.y + 0.01, orig.z, origYaw, origPitch, false)", "new ServerboundMovePlayerPacket.PosRot(orig.x, orig.y + 0.01, orig.z, origYaw, origPitch, true, false)")
            replace("new ServerboundMovePlayerPacket.Pos(orig.x, orig.y, orig.z, true)", "new ServerboundMovePlayerPacket.Pos(orig.x, orig.y, orig.z, true, true)")
            replace("new ServerboundMovePlayerPacket.Pos(x, y, z, onGround)", "new ServerboundMovePlayerPacket.Pos(x, y, z, true, onGround)")
        }
    }
}
