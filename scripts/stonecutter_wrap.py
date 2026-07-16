#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
scripts/stonecutter_wrap.py
=====================================================================
FKU → Stonecutter 单源迁移：把 common/ 里所有「Forge 1.20.1」写法
（导入 + 行内限定名 + 行内简名）用 `#//if MC_1_21_8 ... #//else ... #//endif`
包裹，使同一份 common 源码能同时编译 Forge 1.20.1 与 NeoForge 1.21.8。

- 导入行：`import net.minecraftforge.X;` → #//if 包成 neoforged / minecraftforge 两分支
- 行内限定名：`net.minecraftforge.X.Y` → 同上
- 行内简名（包名不同而简名也不同）：MinecraftForge / ForgeRegistries / ForgeConfigSpec
  → 用负向后顾避免误伤已包裹的限定名分支

注意：
- ForgeMod.BLOCK_REACH.get() → Attributes.BLOCK_INTERACTION_RANGE 是「不同 API」，
  且需额外导入 Attributes，不在本脚本自动处理范围，交由人工在对应文件处理。
- 默认 --dry-run（只统计，不写盘）；--apply 才真正改写，并保留 .bak。
=====================================================================
"""
import argparse
import os
import re

# forge → neoforge 完整映射（覆盖 common 中出现的 28 个导入 + 行内限定名）
MAPPING = {
    "net.minecraftforge.api.distmarker.Dist": "net.neoforged.api.distmarker.Dist",
    "net.minecraftforge.eventbus.api.SubscribeEvent": "net.neoforged.bus.api.SubscribeEvent",
    "net.minecraftforge.fml.common.Mod": "net.neoforged.fml.common.Mod",
    "net.minecraftforge.event.TickEvent": "net.neoforged.neoforge.event.tick.TickEvent",
    "net.minecraftforge.api.distmarker.OnlyIn": "net.neoforged.api.distmarker.OnlyIn",
    "net.minecraftforge.client.event.InputEvent": "net.neoforged.neoforge.client.event.InputEvent",
    "net.minecraftforge.registries.ForgeRegistries": "net.neoforged.neoforge.registries.NeoForgeRegistries",
    "net.minecraftforge.common.MinecraftForge": "net.neoforged.neoforge.common.NeoForge",
    "net.minecraftforge.client.event.RenderLevelStageEvent": "net.neoforged.neoforge.client.event.RenderLevelStageEvent",
    "net.minecraftforge.event.entity.player.AttackEntityEvent": "net.neoforged.neoforge.event.entity.player.AttackEntityEvent",
    "net.minecraftforge.client.event.RegisterClientCommandsEvent": "net.neoforged.neoforge.client.event.RegisterClientCommandsEvent",
    "net.minecraftforge.client.gui.overlay.VanillaGuiOverlay": "net.neoforged.neoforge.client.gui.overlay.VanillaGuiOverlay",
    "net.minecraftforge.client.event.ScreenEvent": "net.neoforged.neoforge.client.event.ScreenEvent",
    "net.minecraftforge.client.event.RenderGuiOverlayEvent": "net.neoforged.neoforge.client.event.RenderGuiOverlayEvent",
    "net.minecraftforge.client.event.RegisterKeyMappingsEvent": "net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent",
    "net.minecraftforge.client.event.ClientPlayerNetworkEvent": "net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent",
    "net.minecraftforge.fml.ModList": "net.neoforged.fml.ModList",
    "net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent": "net.neoforged.fml.event.lifecycle.FMLClientSetupEvent",
    "net.minecraftforge.fml.config.ModConfig": "net.neoforged.fml.config.ModConfig",
    "net.minecraftforge.event.entity.living.LivingFallEvent": "net.neoforged.neoforge.event.entity.living.LivingFallEvent",
    "net.minecraftforge.entity.PartEntity": "net.neoforged.neoforge.entity.PartEntity",
    "net.minecraftforge.common.ForgeMod": "net.neoforged.neoforge.common.NeoForgeMod",
    "net.minecraftforge.common.ForgeConfigSpec": "net.neoforged.neoforge.common.ModConfigSpec",
    "net.minecraftforge.client.settings.KeyConflictContext": "net.neoforged.neoforge.client.settings.KeyConflictContext",
    "net.minecraftforge.client.event.RenderGuiEvent": "net.neoforged.neoforge.client.event.RenderGuiEvent",
    "net.minecraftforge.client.event.RegisterShadersEvent": "net.neoforged.neoforge.client.event.RegisterShadersEvent",
    "net.minecraftforge.client.event.MovementInputUpdateEvent": "net.neoforged.neoforge.client.event.MovementInputUpdateEvent",
    "net.minecraftforge.client.event.ClientChatReceivedEvent": "net.neoforged.neoforge.client.event.ClientChatReceivedEvent",
}

# 行内简名（包名不同、简名也不同）；用负向后顾避免误伤已包裹的限定名分支
SIMPLE = {
    "MinecraftForge": ("NeoForge", r"(?<!net\.minecraftforge\.common\.)"),
    "ForgeRegistries": ("NeoForgeRegistries", r"(?<!net\.minecraftforge\.registries\.)"),
    "ForgeConfigSpec": ("ModConfigSpec", r"(?<!net\.minecraftforge\.common\.)"),
}

IF_HEAD = "#//if MC_1_21_8"
ELSE = "#//else"
END = "#//endif"


def wrap_token(f, n):
    """生成 #//if 包裹块：neoforge 分支用 n，forge 分支用 f。"""
    return f"{IF_HEAD}\n{n}\n{ELSE}\n{f}\n{END}"


def process_line(line):
    """处理单行：导入 + 行内限定名 + 行内简名。返回 (新行, 是否改动)。"""
    changed = False

    # 1) 导入行
    m = re.match(r"^(\s*)import (net\.minecraftforge(?:\.[A-Za-z0-9_]+)+);\s*$", line)
    if m:
        indent, fqcn = m.group(1), m.group(2)
        neo = MAPPING.get(fqcn)
        if neo:
            block = f"{indent}{IF_HEAD}\n{indent}import {neo};\n{indent}{ELSE}\n{indent}import {fqcn};\n{indent}{END}\n"
            return block, True
        return line, False  # 映射里没有（理论上不应发生）

    # 2) 行内限定名（最长优先，避免子串重复匹配）
    new_line = line
    for fqcn in sorted(MAPPING.keys(), key=len, reverse=True):
        neo = MAPPING[fqcn]
        if fqcn in new_line:
            # 只包裹「作为独立限定名」出现的（后面跟 . 或 ; 或 行尾或空白）
            pat = re.compile(re.escape(fqcn) + r"(?=[\s.;)])")
            if pat.search(new_line):
                new_line = pat.sub(lambda _: wrap_token(fqcn, neo), new_line)
                changed = True

    # 3) 行内简名（带负向后顾，避免误伤第 2 步生成的 #//else 分支里的限定名）
    for simp, (neo, look) in SIMPLE.items():
        # 简名后跟 . 或 ( 或 ; 等，且不被对应限定前缀包住
        pat = re.compile(look + r"\b" + re.escape(simp) + r"\b(?=[\s.(;])")
        if pat.search(new_line):
            new_line = pat.sub(lambda _: wrap_token(simp, neo), new_line)
            changed = True

    return new_line, changed


def main():
    ap = argparse.ArgumentParser(description="FKU common → Stonecutter #//if 包裹")
    ap.add_argument("--root", default="common/src", help="源码根（默认 common/src）")
    ap.add_argument("--apply", action="store_true", help="真正改写（默认 dry-run）")
    args = ap.parse_args()

    if not os.path.isdir(args.root):
        print(f"[错误] 目录不存在: {args.root}", file=__import__("sys").stderr)
        return 1

    files = []
    for dp, _, fs in os.walk(args.root):
        for f in fs:
            if f.endswith(".java"):
                files.append(os.path.join(dp, f))
    files.sort()

    total_files = 0
    total_lines = 0
    for path in files:
        with open(path, "r", encoding="utf-8") as fh:
            lines = fh.readlines()
        new_lines = []
        file_changed = False
        for ln in lines:
            nl, ch = process_line(ln)
            if ch:
                file_changed = True
                total_lines += 1
            new_lines.append(nl)
        if file_changed:
            total_files += 1
            if args.apply:
                bak = path + ".bak"
                if not os.path.exists(bak):
                    with open(bak, "w", encoding="utf-8") as fh:
                        fh.writelines(lines)
                with open(path, "w", encoding="utf-8") as fh:
                    fh.writelines(new_lines)
            else:
                # dry-run：打印前几个改动样例
                if total_files <= 3:
                    print(f"--- {os.path.relpath(path)} (示例前2处改动) ---")
                    shown = 0
                    for i, (o, n) in enumerate(zip(lines, new_lines)):
                        if o != n:
                            print("  原:", o.rstrip())
                            print("  新:", n.rstrip()[:200])
                            shown += 1
                            if shown >= 2:
                                break

    print(f"\n统计: {total_files} 个文件、{total_lines} 行被包裹")
    if args.apply:
        print("已应用（已保留 .bak）。")
    else:
        print("DRY-RUN：未写盘。加 --apply 执行。")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
