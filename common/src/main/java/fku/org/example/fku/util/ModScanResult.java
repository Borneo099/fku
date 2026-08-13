package fku.org.example.fku.util; /* water */

import java.util.HashMap;
import java.util.Map;

/**
 * ModScanResult — 扫描目录后分离主 mod 与 jarjar 内嵌 mod 的计数结果
 *
 * ★ 为什么放在 util 包而非 mixin 包：
 *   Mixin 强制禁止外部引用 mixin 包内的类，内部类在 mixin 包中会导致 OpMod 加载时
 *   IllegalClassLoadError。
 *
 * ★ 该方法是赛博教员实现
 */
public class ModScanResult {
    /** 主 mod（顶层 mods.toml 提取）→ 出现次数 */
    public final Map<String, Integer> mainMods = new HashMap<>();
    /** jarjar 内嵌 mod → 出现次数 */
    public final Map<String, Integer> jarjarMods = new HashMap<>();
    /** 该目录是否检测到携带 MixinExtras（任意形式：jar-in-jar / relocate / META-INF services） */
    public boolean hasMixinExtras = false;
}