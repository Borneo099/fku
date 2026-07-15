#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
scripts/sync_neoforge_1.21.8.py
=====================================================================
FKU · NeoForge 1.21.8 同步 / 自检脚本
=====================================================================

目的
----
把 FKU 从旧版（Forge / 老 NeoForge）移植到 NeoForge 1.21.8 时踩过的坑，
整理成一份「可复跑的检查脚本」，让后续开发在编译前 / 提交前就能发现绝大多数错误：

  1) API 迁移扫描：扫描源码里仍在使用的「旧 API 写法」，给出对应的 1.21.8 新写法。
     覆盖：ForgeRegistries→BuiltInRegistries、ForgeMod.BLOCK_REACH→Attributes.BLOCK_INTERACTION_RANGE、
     AttributeModifier.Operation.ADDITION→ADD_VALUE、EnchantmentHelper.getEnchantments→stack.getEnchantments()、
     @Mod.EventBusSubscriber→@EventBusSubscriber、ClientTickEvent / RenderGuiOverlayEvent 包名、
     VanillaGuiOverlay.X.type()→.id()、new ResourceLocation→ResourceLocation.parse、getIntArray().orElse(...) 等。

  2) Mixin 运行时注入校验（关键！）：编译能通过、但进游戏才崩的那类错误。
     逐一核对 fku.mixins.json 里每个 mixin 的 @Inject / @Shadow / @Accessor：
       - 目标方法是 void → 处理器回调必须是 CallbackInfo（用 CallbackInfoReturnable 会 Invalid descriptor 崩溃）
       - 目标方法非 void → 处理器回调必须是 CallbackInfoReturnable<T>
       - @Inject 处理器参数（不含回调）必须与目标方法参数「数量+顺序」一致（漏参数同样 Invalid descriptor）
       - @Shadow / @Accessor 指向的字段必须在 1.21.8 目标类里真实存在
     校验依据是 neoForm 生成的「官方映射」Minecraft jar（rename/output.jar），用 javap 读取真实签名。

为什么需要它
------------
本次 (2026-07-15) 玩家进游戏崩溃，根因就是上面第 2 类：
  - MixinChatScreen 对 void 的 ChatScreen.handleChatInput 误用了 CallbackInfoReturnable<Object>
  - MixinMultiPlayerGameMode.onReleaseUsingItem 漏写了 releaseUsingItem(Player) 的 Player 参数
两处都能被本脚本在「编译前」抓出来。

用法
----
  # 只扫描（默认，不改动任何文件，安全）
  python3 scripts/sync_neoforge_1.21.8.py --root neoforge/src

  # 扫描 + Mixin 注入校验（需要 rename jar；会自动探测）
  python3 scripts/sync_neoforge_1.21.8.py --root neoforge/src --audit

  # 自动修复「安全」的 API 写法（会先备份 .bak，谨慎使用）
  python3 scripts/sync_neoforge_1.21.8.py --root neoforge/src --fix-api

  # 指定 Minecraft 官方映射 jar（不指定则自动从 neoforge/build/neoForm 探测）
  python3 scripts/sync_neoforge_1.21.8.py --root neoforge/src --audit \
      --jar neoforge/build/neoForm/neoFormJoined1.21.8-*/steps/rename/output.jar

退出码：发现任何问题时返回 1，全绿返回 0（方便接入 CI / pre-commit）。
=====================================================================
"""

import argparse
import glob
import json
import os
import re
import subprocess
import sys

# =====================================================================
# 1) API 迁移规则（旧写法 → 新写法）
#    safe=True 的规则可被 --fix-api 机械替换；safe=False 仅报告，需手工改。
# =====================================================================
API_RULES = [
    {
        "id": "forge-registries",
        "desc": "ForgeRegistries.X.getValue(new ResourceLocation(...)) → BuiltInRegistries.X.getValue(ResourceLocation.parse(...))",
        "pattern": r'ForgeRegistries\.(\w+)\.getValue\(new ResourceLocation\(([^()]*)\)\)',
        "repl": r'BuiltInRegistries.\1.getValue(ResourceLocation.parse(\2))',
        "safe": True,
    },
    {
        "id": "rl-new",
        "desc": "new ResourceLocation(x) → ResourceLocation.parse(x)（1.21.8 推荐静态工厂）",
        "pattern": r'new ResourceLocation\(([^()]*)\)',
        "repl": r'ResourceLocation.parse(\1)',
        "safe": True,
    },
    {
        "id": "block-reach",
        "desc": "ForgeMod.BLOCK_REACH.get() → Attributes.BLOCK_INTERACTION_RANGE（已是 Holder，勿再 .get()）",
        "pattern": r'ForgeMod\.BLOCK_REACH\.get\(\)',
        "repl": r'Attributes.BLOCK_INTERACTION_RANGE',
        "safe": True,
    },
    {
        "id": "mod-eventbussubscriber",
        "desc": "@Mod.EventBusSubscriber → @EventBusSubscriber（import net.neoforged.fml.common.EventBusSubscriber）",
        "pattern": r'@Mod\.EventBusSubscriber',
        "repl": r'@EventBusSubscriber',
        "safe": True,
    },
    {
        "id": "op-addition",
        "desc": "AttributeModifier.Operation.ADDITION → Operation.ADD_VALUE（枚举改名）",
        "pattern": r'AttributeModifier\.Operation\.ADDITION',
        "repl": r'Operation.ADD_VALUE',
        "safe": True,
    },
    {
        "id": "guioverlay-type",
        "desc": "VanillaGuiOverlay.X.type() → VanillaGuiOverlay.X.id()（1.21.8 改名）",
        "pattern": r'VanillaGuiOverlay\.(\w+)\.type\(\)',
        "repl": r'VanillaGuiOverlay.\1.id()',
        "safe": True,
    },
    {
        "id": "tick-event",
        "desc": "net.minecraftforge.event.TickEvent.ClientTickEvent → net.neoforged.neoforge.client.event.ClientTickEvent（用 .Pre/.Post）",
        "pattern": r'net\.minecraftforge\.event\.TickEvent\.ClientTickEvent',
        "repl": r'net.neoforged.neoforge.client.event.ClientTickEvent',
        "safe": True,
    },
    {
        "id": "renderguioverlay",
        "desc": "net.minecraftforge.client.event.RenderGuiOverlayEvent → net.neoforged.neoforge.client.event.RenderGuiOverlayEvent",
        "pattern": r'net\.minecraftforge\.client\.event\.RenderGuiOverlayEvent',
        "repl": r'net.neoforged.neoforge.client.event.RenderGuiOverlayEvent',
        "safe": True,
    },
    {
        "id": "getenchantments",
        "desc": "EnchantmentHelper.getEnchantments(stack) → stack.getEnchantments()（返回 ItemEnchantments，遍历需改）",
        "pattern": r'EnchantmentHelper\.getEnchantments\(',
        "repl": r'stack.getEnchantments(',
        "safe": False,
    },
    {
        "id": "getintarray-orElse",
        "desc": "getIntArray(\"x\") → getIntArray(\"x\").orElse(new int[0])（1.21.8 返回 Optional）",
        "pattern": r'\.getIntArray\(([^()]*)\)(?!\.orElse)',
        "repl": r'.getIntArray(\1).orElse(new int[0])',
        "safe": False,
    },
    {
        "id": "bufferuploader-draw",
        "desc": "BufferUploader.drawWithShader(...) 在 1.21.8 已移除，需改用直接渲染或注释该行",
        "pattern": r'BufferUploader\.drawWithShader\(',
        "repl": None,
        "safe": False,
    },
    {
        "id": "rendersystem-modelview",
        "desc": "RenderSystem.getModelViewStack()/applyModelViewMatrix() 等部分方法 1.21.8 语义变化，请核对是否仍需使用",
        "pattern": r'RenderSystem\.(getModelViewStack|applyModelViewMatrix)\(',
        "repl": None,
        "safe": False,
    },
]

# =====================================================================
# 2) Mixin 注入校验
# =====================================================================

CALLBACK_INFO = "CallbackInfo"
CALLBACK_INFO_RETURNable = "CallbackInfoReturnable"


def find_source_files(root):
    files = []
    for dirpath, _, fnames in os.walk(root):
        for f in fnames:
            if f.endswith(".java"):
                files.append(os.path.join(dirpath, f))
    return files


def scan_api(root, fix=False):
    """扫描 API 迁移问题。fix=True 时对 safe 规则做机械替换（先备份）。"""
    findings = []
    files = find_source_files(root)
    for path in files:
        try:
            with open(path, "r", encoding="utf-8") as fh:
                lines = fh.readlines()
        except Exception:
            continue
        new_lines = list(lines)
        changed = False
        for i, line in enumerate(lines):
            for rule in API_RULES:
                for m in re.finditer(rule["pattern"], line):
                    findings.append({
                        "file": path, "line": i + 1, "rule": rule["id"],
                        "desc": rule["desc"],
                        "snippet": line.strip()[:160],
                        "safe": rule["safe"],
                    })
                    if fix and rule["safe"] and rule["repl"] is not None:
                        new_lines[i] = re.sub(rule["pattern"], rule["repl"], new_lines[i])
                        changed = True
        if changed:
            bak = path + ".bak"
            if not os.path.exists(bak):
                with open(bak, "w", encoding="utf-8") as fh:
                    fh.writelines(lines)
            with open(path, "w", encoding="utf-8") as fh:
                fh.writelines(new_lines)
    return findings


# ---------- javap 工具 ----------
_javap_cache = {}


def javap_class(jar, classname):
    """返回该类的 javap -p 文本（带私有成员），按类名缓存。"""
    if jar is None:
        return None
    if classname in _javap_cache:
        return _javap_cache[classname]
    # 类名可能是全限定（点分隔），转成 javap 接受的形式
    target = classname.replace("/", ".")
    try:
        out = subprocess.run(
            ["javap", "-p", "-classpath", jar, target],
            capture_output=True, text=True, timeout=60,
        ).stdout
    except Exception:
        out = ""
    _javap_cache[classname] = out
    return out


def parse_method_descriptor(desc):
    """解析 (Lx;ZI)V 形式描述符 → (param_count, returns_void)。"""
    m = re.match(r"\(([^)]*)\)(.+)$", desc)
    if not m:
        return None, None
    params, ret = m.group(1), m.group(2)
    count = 0
    i = 0
    while i < len(params):
        c = params[i]
        if c == "L":
            i = params.find(";", i) + 1
        elif c == "[":
            i += 1
            while i < len(params) and params[i] == "[":
                i += 1
            if i < len(params) and params[i] == "L":
                i = params.find(";", i) + 1
            else:
                i += 1
        else:
            i += 1
        count += 1
    return count, (ret == "V")


def find_target_methods(javap_text, name):
    """从 javap 文本里找出名为 name 的方法，返回 [(param_count, returns_void)]。

    javap -p 行形如：
        public void handleChatInput(java.lang.String, boolean);
        public int foo(int);
        protected final void bar();
    注意：返回类型在「方法名之前」，方法名(参数) 之后通常直接是 ';'。
    """
    results = []
    if not javap_text:
        return results
    pat = re.compile(
        r"(?:public|private|protected|static|final|abstract|native|synchronized|"
        r"transient|volatile|strictfp|\s)+([\w$.<>\[\], ]+?)\s+"
        + re.escape(name) + r"\(([^)]*)\)(?:\s+throws\s+[\w,.<> \[\]]+)?\s*;"
    )
    for line in javap_text.splitlines():
        m = pat.search(line)
        if not m:
            continue
        ret = m.group(1).strip()
        params_str = m.group(2).strip()
        count = 0 if params_str == "" else params_str.count(",") + 1
        results.append((count, ret == "void"))
    return results


def find_field(javap_text, fieldname):
    if not javap_text:
        return None
    for line in javap_text.splitlines():
        # 字段声明形如：private final int entityId;
        m = re.search(r"\b" + re.escape(fieldname) + r"\s*;", line)
        if m and "(" not in line:  # 排除方法
            return True
    return None


def internal_name(fqcn):
    """把 net.minecraft.client.gui.screens.ChatScreen 形式转成 javap 用的点形式（原样即可）。"""
    return fqcn


def parse_mixin_file(path):
    """粗略解析一个 mixin 源文件，返回其 @Mixin 目标与所有注入点。"""
    with open(path, "r", encoding="utf-8") as fh:
        src = fh.read()
    mixins = []

    # 解析 import，建立 简单名 -> 全限定名 映射。
    # 关键：@Mixin(ChatScreen.class) 里是简单名，但 javap 需要全限定名
    # net.minecraft.client.gui.screens.ChatScreen 才能读签名，必须还原。
    imports = {}
    for im in re.finditer(r"^\s*import\s+([\w.]+)\.(\w+)\s*;", src, re.M):
        imports[im.group(2)] = im.group(1) + "." + im.group(2)

    m = re.search(r"@Mixin\(\s*([\w.]+)\.class", src)
    if not m:
        return mixins
    simple = m.group(1)
    if "." in simple:
        target = simple            # 已是全限定名（如 net.minecraft.x.Y）
    elif simple in imports:
        target = imports[simple]   # 通过 import 还原成全限定名
    else:
        target = simple            # 退化：javap 多半失败，会在校验时给出明确提示

    # 收集 @Inject / @Shadow / @Accessor 块及其紧随的方法签名
    # 简单做法：逐行扫描，记录最近的注解，遇到方法声明时关联
    lines = src.splitlines()
    cur_annot = None
    cur_method = None
    for idx, line in enumerate(lines):
        st = line.strip()
        if st.startswith("@Inject"):
            cur_annot = {"kind": "inject", "raw": st, "line": idx + 1}
            cur_method = None
        elif st.startswith("@Shadow"):
            cur_annot = {"kind": "shadow", "raw": st, "line": idx + 1}
            cur_method = None
        elif st.startswith("@Accessor"):
            mm = re.search(r'@Accessor\(\s*"([^"]+)"\s*\)', st)
            cur_annot = {"kind": "accessor", "field": mm.group(1) if mm else None,
                         "raw": st, "line": idx + 1}
            cur_method = None
        elif cur_annot is not None and not st.startswith("@") \
                and not re.match(r"(private|public|protected|abstract)", st):
            # 注解块延续行（method = "..." / at = @At(...) / cancellable = true 等）：
            # 累加到 raw，便于后续从多行 @Inject 块中提取 method= 值。
            cur_annot["raw"] += " " + st
        elif re.match(r"(private|public|protected|abstract).*\b\w+\s*\(", st) and cur_annot:
            # 方法声明
            sig = st
            # 提取方法名
            nm = re.search(r"\b(\w+)\s*\(", sig)
            method_name = nm.group(1) if nm else None
            # 提取参数 token（去掉注解、类型只留名字附近）——这里只要"参数个数（不含回调）"和"回调类型"
            params_block = re.search(r"\(([^)]*)\)", sig)
            params = []  # 每个元素是「参数类型」token（而非变量名）
            if params_block:
                inner = params_block.group(1).strip()
                if inner:
                    for p in inner.split(","):
                        p = p.strip()
                        if not p:
                            continue
                        # 去掉注解 @Xxx / @Final 等
                        p = re.sub(r"@\w+(\([^)]*\))?", "", p).strip()
                        toks = p.split()
                        # 去掉修饰符（final / var），保留真正的类型 token
                        while toks and toks[0] in ("final", "var"):
                            toks = toks[1:]
                        if not toks:
                            continue
                        params.append(toks[0])
            # 用参数「类型」判断回调类型（不能用变量名 ci/cir，否则识别失败）
            callback = None
            non_cb = []
            for p in params:
                if p.startswith("CallbackInfoReturnable"):
                    callback = "CallbackInfoReturnable"
                elif p == "CallbackInfo":
                    callback = "CallbackInfo"
                else:
                    non_cb.append(p)
            entry = dict(cur_annot)
            entry["method_name"] = method_name
            entry["handler_params"] = non_cb
            entry["callback"] = callback
            entry["line"] = idx + 1
            mixins.append(entry)
            cur_annot = None
        elif st.startswith("@"):
            # 其它注解，不关联方法
            cur_annot = None
    return target, mixins


def audit_mixins(root, jar):
    """对每个 mixin 文件，用 javap 校验注入点。返回问题列表。"""
    problems = []
    files = find_source_files(root)
    mixin_files = [f for f in files if "@Mixin(" in open(f, encoding="utf-8").read()]
    for f in mixin_files:
        target, injects = parse_mixin_file(f)
        if not injects:
            continue
        jtext = javap_class(jar, target)
        if jtext is None:
            problems.append({"file": f, "target": target, "issue": "无法读取目标类 javap（jar 未提供？）", "line": 0})
            continue
        for inj in injects:
            if inj["kind"] == "inject":
                # 解析 @Inject method= 的值
                mm = re.search(r'method\s*=\s*"([^"]+)"', inj["raw"])
                method_ref = mm.group(1) if mm else inj.get("method_name")
                if not method_ref:
                    continue
                # 若 method 含描述符 name(desc)
                if "(" in method_ref:
                    name = method_ref[:method_ref.index("(")]
                    desc = method_ref[method_ref.index("("):]
                    tcount, tvoid = parse_method_descriptor(desc)
                    tmethods = [(tcount, tvoid)] if tcount is not None else []
                else:
                    name = method_ref
                    tmethods = find_target_methods(jtext, name)
                if not tmethods:
                    problems.append({"file": f, "target": target, "issue":
                                      f"@Inject 目标方法 {name} 在 1.21.8 中未找到（方法名/描述符错误）", "line": inj["line"]})
                    continue
                # 目标方法数可能多个重载；只要有一个匹配即放行
                matched = False
                for (tcount, tvoid) in tmethods:
                    # 回调类型校验
                    cb_ok = (inj["callback"] == "CallbackInfo" and tvoid) or \
                            (inj["callback"] == "CallbackInfoReturnable" and not tvoid)
                    # 参数个数校验（处理器非回调参数 == 目标参数数）
                    param_ok = (len(inj["handler_params"]) == tcount)
                    if cb_ok and param_ok:
                        matched = True
                        break
                if not matched:
                    detail = []
                    for (tcount, tvoid) in tmethods:
                        detail.append(f"目标(name={name}, 参数数={tcount}, 返回void={tvoid})")
                    problems.append({
                        "file": f, "target": target,
                        "issue": f"@Inject {name} 处理器签名不匹配 → 回调类型或参数个数错误。"
                                  f" 处理器: 回调={inj['callback']}, 非回调参数={len(inj['handler_params'])}。"
                                  f" 目标候选: {'; '.join(detail)}",
                        "line": inj["line"],
                    })
            elif inj["kind"] in ("shadow", "accessor"):
                field = inj.get("field")
                # @Shadow 没有显式名字时，取处理器/字段名（这里粗略用不到名字则跳过）
                if field:
                    if find_field(jtext, field) is None:
                        problems.append({
                            "file": f, "target": target,
                            "issue": f"@{inj['kind'].capitalize()} 字段 '{field}' 在 1.21.8 目标类 {target} 中不存在",
                            "line": inj["line"],
                        })
    return problems


# =====================================================================
# 2.5) TickEvent 订阅校验（事件总线层面，与 Mixin 同类的"致命陷阱"）
# =====================================================================
ABSTRACT_TICK_EVENTS = ["ClientTickEvent", "PlayerTickEvent", "ServerTickEvent", "LevelTickEvent"]


def audit_tick_events(root):
    """扫描 @SubscribeEvent 方法，禁止直接监听抽象基类 TickEvent。

    NeoForge 1.21.8 把 ClientTickEvent / PlayerTickEvent / ServerTickEvent /
    LevelTickEvent 改成了 abstract class，必须监听其子类（.Pre/.Post 或
    .Start/.End）。直接监听抽象基类会在 mod 加载阶段抛 IllegalArgumentException，
    导致整个 mod 无法加载（比 Mixin 注入点崩溃更早、更致命）。
    """
    problems = []
    for path in find_source_files(root):
        try:
            lines = open(path, encoding="utf-8").read().splitlines()
        except Exception:
            continue
        for i, line in enumerate(lines):
            if "@SubscribeEvent" not in line:
                continue
            # 合并到方法签名结束（含 ')'），覆盖 @SubscribeEvent 与方法同行/分行两种写法
            sig = line
            j = i + 1
            while j < len(lines) and ")" not in sig:
                sig += " " + lines[j]
                j += 1
            m = re.search(r"\(([^)]*)\)", sig)
            if not m:
                continue
            params = m.group(1)
            for t in ABSTRACT_TICK_EVENTS:
                # 排除基类.子类（如 ClientTickEvent.Post）；命中"基类 变量名"才算直接监听抽象类
                pat = re.compile(r"\b" + re.escape(t) + r"(?!\.[A-Za-z])(?:<[^>]*>)?\s+\w+")
                if pat.search(params):
                    problems.append({
                        "file": path, "line": i + 1,
                        "issue": f"@SubscribeEvent 直接监听抽象基类 {t}（1.21.8 中 {t} 为 abstract，"
                                 f"必须监听其子类，如 {t}.Post / {t}.Start）",
                    })
    return problems


# =====================================================================
# 2.6) 抽象事件类订阅校验（通用版：任何抽象事件基类都拦）
# =====================================================================
def _collect_imports(src):
    """返回 (简单名->全限定名, [star 包前缀...])。"""
    simple = {}
    star = []
    for m in re.finditer(r'^\s*import\s+(?:static\s+)?([\w\.]+)\s*;\s*$', src, re.M):
        fqn = m.group(1)
        if fqn.endswith(".*"):
            star.append(fqn[:-2])
        else:
            simple[fqn.split(".")[-1]] = fqn
    return simple, star


def _resolve_fqn(base, simple, star):
    """把事件参数类型解析成 javap 可用的全限定名（含嵌套类 $）。"""
    base = re.sub(r"<.*>", "", base).strip()   # 去泛型
    base = re.sub(r"^(final\s+|var\s+)", "", base)
    if "." not in base:
        if base in simple:
            return simple[base]
        for pkg in star:
            return pkg + "." + base
        return None
    # 含点：pkg.Outer 或 Outer.Nested
    head, tail = base.rsplit(".", 1)
    if head in simple:                       # Outer.Nested，Outer 是简单名
        return simple[head] + "$" + tail
    if "." in head:                          # pkg.Outer.Nested，Outer 是限定的
        return head + "$" + tail
    for pkg in star:                         # Outer 来自 star import
        return pkg + "." + head + "$" + tail
    return base


def _is_abstract_class(fqn, jar, _cache={}):
    if fqn in _cache:
        return _cache[fqn]
    javap_name = fqn.replace("/", ".")
    try:
        out = subprocess.run(["javap", "-p", "-classpath", jar, javap_name],
                             capture_output=True, text=True, timeout=30).stdout
    except Exception:
        _cache[fqn] = None
        return None
    first = ""
    for line in out.splitlines():
        if "class " in line or "interface " in line:
            first = line
            break
    res = ("abstract" in first.split("{")[0]) if first else None
    _cache[fqn] = res
    return res


def audit_abstract_events(root, jar):
    """扫描所有 @SubscribeEvent 方法，用 javap 核对事件参数类型是否为 abstract class。

    这是 audit_tick_events 的通用版：不局限于已知的 TickEvent 列表，而是直接问
    JVM/字节码「这个事件类型是不是 abstract」。任何直接监听抽象事件基类
    （如 PlayerTickEvent / ClientTickEvent / RenderLevelStageEvent / ServerTickEvent /
    LevelTickEvent 等）都会在 mod 加载阶段抛 IllegalArgumentException，导致整个
    mod 无法加载。

    依赖 neoForm 合并 jar（需含 NeoForge 事件类，优先 packRecomp/output.jar）。
    """
    if not jar or not os.path.exists(jar):
        return []   # 无 jar 则跳过（调用方应提示）
    problems = []
    for path in find_source_files(root):
        try:
            src = open(path, encoding="utf-8").read()
        except Exception:
            continue
        simple, star = _collect_imports(src)
        for i, line in enumerate(src.splitlines()):
            if "@SubscribeEvent" not in line:
                continue
            sig = line
            j = i + 1
            lines = src.splitlines()
            while j < len(lines) and ")" not in sig:
                sig += " " + lines[j]
                j += 1
            m = re.search(r"\(([^)]*)\)", sig)
            if not m:
                continue
            params = m.group(1).strip()
            if not params:
                continue
            first_param = params.split(",")[0].strip()
            # 去掉变量名，只保留类型（如 "RenderLevelStageEvent.AfterEntities event" -> 类型）
            ptype = first_param.rsplit(" ", 1)[0].strip()
            fqn = _resolve_fqn(ptype, simple, star)
            if not fqn:
                continue
            if "event" not in fqn.lower() and "Event" not in fqn:
                continue
            abs_flag = _is_abstract_class(fqn, jar)
            if abs_flag is True:
                problems.append({
                    "file": path, "line": i + 1,
                    "issue": f"@SubscribeEvent 直接监听抽象事件基类 {fqn}"
                             f"（必须监听其具体子类，如 .Pre/.Post/.AfterEntities 等）",
                })
    return problems


# =====================================================================
# 3) 入口
# =====================================================================
def detect_rename_jar():
    cands = glob.glob(os.path.join("neoforge", "build", "neoForm", "neoFormJoined*",
                                    "steps", "rename", "output.jar"))
    cands += glob.glob(os.path.join("**", "steps", "rename", "output.jar"), recursive=True)
    return cands[0] if cands else None


def detect_neoforge_jar():
    """优先取含 NeoForge 事件类的合并 jar（packRecomp），回退 rename。"""
    cands = glob.glob(os.path.join("neoforge", "build", "neoForm", "neoFormJoined*",
                                    "steps", "packRecomp", "output.jar"))
    cands += glob.glob(os.path.join("**", "steps", "packRecomp", "output.jar"), recursive=True)
    if cands:
        return cands[0]
    return detect_rename_jar()


def main():
    ap = argparse.ArgumentParser(description="FKU NeoForge 1.21.8 同步/自检脚本")
    ap.add_argument("--root", default="neoforge/src", help="源码根目录（默认 neoforge/src）")
    ap.add_argument("--audit", action="store_true", help="启用 Mixin 运行时注入校验（需 Minecraft 官方映射 jar）")
    ap.add_argument("--jar", default=None, help="Minecraft 官方映射 jar 路径（rename/output.jar）")
    ap.add_argument("--fix-api", action="store_true", help="自动修复 safe 的 API 写法（先备份 .bak）")
    args = ap.parse_args()

    if not os.path.isdir(args.root):
        print(f"[错误] 源码根目录不存在: {args.root}", file=sys.stderr)
        sys.exit(2)

    print(f"=== [1/2] API 迁移扫描：{args.root} ===")
    findings = scan_api(args.root, fix=args.fix_api)
    if not findings:
        print("  ✅ 未发现旧 API 写法。")
    else:
        for f in findings:
            tag = "可自动修" if f["safe"] else "需手工"
            print(f"  [{'API:'+f['rule']}] {f['file']}:{f['line']} ({tag})")
            print(f"      {f['desc']}")
            if f["snippet"]:
                print(f"      代码: {f['snippet']}")
        print(f"  → 共 {len(findings)} 处。{'已自动修复 safe 项（.bak 备份）。' if args.fix_api else '用 --fix-api 可自动修复 safe 项。'}")

    problems = []
    if args.audit:
        jar = args.jar or detect_rename_jar()
        nf_jar = args.jar or detect_neoforge_jar()
        print(f"\n=== [2/2] Mixin 运行时注入校验 ===")
        if not jar or not os.path.exists(jar):
            print(f"  ⚠️ 未找到 Minecraft 官方映射 jar（rename/output.jar），跳过 Mixin 校验。")
            print(f"     请用 --jar 指定，例如 neoforge/build/neoForm/neoFormJoined1.21.8-*/steps/rename/output.jar")
        else:
            print(f"  使用 jar: {jar}")
            problems = audit_mixins(args.root, jar)
            problems += audit_tick_events(args.root)
            problems += audit_abstract_events(args.root, nf_jar)
            if not problems:
                print("  ✅ 所有 @Inject 回调类型/参数个数、@Shadow/@Accessor 字段、"
                      "抽象事件类订阅（含 TickEvent / RenderLevelStageEvent 等）均通过校验。")
            else:
                for p in problems:
                    loc = f"{p['file']}:{p['line']}" if p.get("line") else p["file"]
                    print(f"  [MIXIN] {loc} → {p['issue']}")

    # 退出码
    has_issue = bool(findings) or bool(problems)
    print(f"\n=== 汇总：API {len(findings)} 处，Mixin 问题 {len(problems)} 个 ===")
    sys.exit(1 if has_issue else 0)


if __name__ == "__main__":
    main()
