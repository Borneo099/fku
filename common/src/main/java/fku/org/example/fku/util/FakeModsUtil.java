package fku.org.example.fku.util;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * OpMod 绕过用伪装 modId 列表管理。
 *
 * 伪装用的候选 modId 从 fku/fake_mods.txt 读取（每行一个，以 # 开头为注释），轮询使用。
 * 文件在 mod 启动阶段（commonSetup）即生成默认内容，不依赖 OpMod 是否加载；
 * 玩家可随时编辑该文件增删改 modId。
 *
 * 该类是普通工具类（非 Mixin），可被任意模块安全调用，避免直接引用 @Mixin 类导致
 * 类加载器将 Mixin 类标记为 invalid 而崩溃。
 */
public final class FakeModsUtil {

    private static volatile String[] fakeModIds = null;

    private FakeModsUtil() {}

    /** 启动阶段调用：确保 fku/fake_mods.txt 存在（不存在则用默认内容创建） */
    public static void ensureFakeModsFile() {
        try {
            File dir = getFkuDir();
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "fake_mods.txt");
            if (!file.exists()) {
                try (FileWriter w = new FileWriter(file)) {
                    w.write("# 每行一个伪装用的 modId，轮询使用，可随意增删改\r\n");
                    w.write("netease_official\r\n");
                    w.write("opmod\r\n");
                }
            }
        } catch (Exception ignored) {}
    }

    /** 读取伪装 modId 列表（带缓存），不存在文件时回退到默认内容 */
    public static String[] loadFakeModIds() {
        if (fakeModIds != null) return fakeModIds;
        ensureFakeModsFile();
        File file = new File(getFkuDir(), "fake_mods.txt");
        List<String> lines = new ArrayList<>();
        if (file.exists()) {
            try (BufferedReader r = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = r.readLine()) != null) {
                    String t = line.trim();
                    if (!t.isEmpty() && !t.startsWith("#")) lines.add(t);
                }
            } catch (Exception ignored) {}
        }
        if (lines.isEmpty()) {
            lines.add("netease_official");
            lines.add("forge");
            lines.add("mixinextras");
        }
        fakeModIds = lines.toArray(new String[0]);
        return fakeModIds;
    }

    private static File getFkuDir() {
        try {
            return new File(FMLPaths.GAMEDIR.get().toFile(), "fku");
        } catch (Exception ignored) {
            return new File("fku");
        }
    }
}
