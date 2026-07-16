#!/bin/bash
# sync-neoforge.sh — [已废弃] 原 common/ → neoforge/ 选择性同步脚本
# ───────────────────────────────────────────────────────────────────────────
# ⚠️ 此脚本已废弃（Stonecutter 单源迁移完成）。
#
# 原作用：把「共享源」(当时是 common/) 选择性同步到 neoforge/ 独立模块，
#         并试图用「专属适配识别」避免覆盖 1.21.8 专属逻辑。
# 原「危险」：任何识别失误都会无声覆盖 neoforge/ 的 1.21.8 适配，难以回滚。
#
# 迁移后该危险已从根本上消除：
#   · 唯一事实来源是 src/（由旧 common/ 重命名而来），不再有第二个可覆盖的项目；
#   · 加载器差异用 //? if neoforge { ... } //? } 条件编译表达，与共享代码同处一文件；
#   · stonecutter 生成时按加载器展开，非活跃分支被包进 /* */ 注释，绝不互相污染。
#
# 新工作流（单一共享源 = src/）：
#   生成 NeoForge 1.21.8 工程：  ./gradlew :1.21.8-neoforge:stonecutterGenerate
#   生成 Forge   1.20.1 工程：  ./gradlew :1.20.1-forge:stonecutterGenerate
#   本地预览展开后源码：          python3 scripts/stonecutter_wrap.py --help
#   构建：见 build.neoforge.gradle.kts / build.forge.gradle.kts
#         （接入 ModDevGradle / ForgeGradle 后即可 ./gradlew :<ver>-<loader>:build）
# ───────────────────────────────────────────────────────────────────────────
set -uo pipefail

echo "⚠️  sync-neoforge.sh 已废弃：Stonecutter 单源迁移后无需再『同步 common → neoforge』。"
echo "    共享源现为 src/，加载器差异用 //? if neoforge { } 表达。"
echo "    新流程：./gradlew :1.21.8-neoforge:stonecutterGenerate"
exit 0
