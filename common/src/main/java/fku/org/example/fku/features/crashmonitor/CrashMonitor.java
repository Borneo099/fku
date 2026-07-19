package fku.org.example.fku.features.crashmonitor; /* water */

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 游戏崩溃监控器 — 加载阶段检测崩溃原因，生成报告到 fku/crash_reports/
 *
 * 原理：
 * 1. 启动时写入 .crash_lock 锁文件 + .crash_stage 阶段追踪文件
 * 2. 每个加载阶段更新阶段名（如 "注册配置"、"初始化功能"、"客户端设置"）
 * 3. 设置默认未捕获异常处理器 + JVM 关闭钩子
 * 4. 正常完成时删除锁文件
 * 5. 下次启动时检查锁文件是否存在 → 存在说明上次崩溃了
 * 6. 崩溃时生成详细报告（系统信息、最后阶段、异常栈）
 *
 * 用法：
 *   CrashMonitor.init(baseDir);       // 最早调用，baseDir = fku 配置目录
 *   CrashMonitor.startPhase("名称");  // 每个阶段开始
 *   CrashMonitor.endPhase("名称");    // 每个阶段结束
 *   CrashMonitor.markLaunchComplete();// 启动完成时调用
 */
public class CrashMonitor {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
    private static final SimpleDateFormat LOG_FMT = new SimpleDateFormat("HH:mm:ss");

    private static File reportDir;
    private static File lockFile;
    private static File stageFile;
    private static String currentStage = "未启动";
    private static boolean cleanShutdown = false;
    private static boolean initialized = false;

    /**
     * 初始化崩溃监控器（应在 mod 构造器中最早调用）
     * @param fkuDirectory fku 配置目录（如 new File(mc.gameDirectory, "fku")）
     */
    public static void init(File fkuDirectory) {
        if (initialized) return;
        initialized = true;

        try {
            if (!fkuDirectory.exists()) fkuDirectory.mkdirs();

            reportDir = new File(fkuDirectory, "crash_reports");
            if (!reportDir.exists()) reportDir.mkdirs();

            lockFile = new File(fkuDirectory, ".crash_lock");
            stageFile = new File(fkuDirectory, ".crash_stage");

            // ── 检测上次是否崩溃 ──
            checkPreviousCrash();

            // ── 写锁文件 ──
            writeLockFile();

            // ── 注册默认未捕获异常处理器 ──
            Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
                generateCrashReport("未捕获异常", "线程: " + thread.getName(), throwable);
                if (defaultHandler != null) {
                    defaultHandler.uncaughtException(thread, throwable);
                }
            });

            // ── 注册 JVM 关闭钩子（检测非正常退出） ──
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (!cleanShutdown && lockFile != null && lockFile.exists()) {
                    generateCrashReport("JVM关闭 - 疑似崩溃", "最后阶段: " + currentStage, null);
                }
            }, "FKU Crash Monitor"));

            log("初始化完成，报告目录: " + reportDir.getAbsolutePath());

        } catch (Exception e) {
            try {
                File fallback = new File("fku_crash_monitor_error.txt");
                try (PrintWriter pw = new PrintWriter(fallback)) { e.printStackTrace(pw); }
            } catch (Exception ignored) {}
        }
    }

    /** 设置当前加载阶段（每步更新 stageFile + 立即刷盘） */
    public static void setStage(String stage) {
        currentStage = stage;
        if (stageFile == null) return;
        try { Files.writeString(stageFile.toPath(), stage); } catch (IOException ignored) {}
    }

    /** 标记某阶段开始 */
    public static void startPhase(String phaseName) {
        setStage("进行中: " + phaseName);
    }

    /** 标记某阶段完成 */
    public static void endPhase(String phaseName) {
        log("[OK] " + phaseName + " 完成");
    }

    /** 正常启动完成 → 删除锁文件 */
    public static void markLaunchComplete() {
        cleanShutdown = true;
        deleteLockFile();
        setStage("启动完成");
    }

    /** 记录信息到监控日志 */
    public static void logInfo(String msg) { log("[信息] " + msg); }
    /** 记录警告到监控日志 */
    public static void logWarn(String msg) { log("[警告] " + msg); }

    // ═══════════ 内部方法 ═══════════

    private static void checkPreviousCrash() {
        if (lockFile != null && lockFile.exists()) {
            String lastStage = "未知";
            if (stageFile != null && stageFile.exists()) {
                try { lastStage = Files.readString(stageFile.toPath()); } catch (IOException ignored) {}
            }
            generateCrashReport("上次启动崩溃", "崩溃前最后阶段: " + lastStage, null);
        }
    }

    private static void writeLockFile() {
        if (lockFile == null) return;
        try {
            Files.writeString(lockFile.toPath(), "DO NOT DELETE - FKU crash monitor lock\nStarted: " + DATE_FMT.format(new Date()));
        } catch (IOException ignored) {}
    }

    private static void deleteLockFile() {
        try { if (lockFile != null && lockFile.exists()) Files.delete(lockFile.toPath()); } catch (IOException ignored) {}
        try { if (stageFile != null && stageFile.exists()) Files.delete(stageFile.toPath()); } catch (IOException ignored) {}
    }

    /** 生成崩溃报告 */
    private static synchronized void generateCrashReport(String reason, String detail, Throwable throwable) {
        try {
            if (reportDir == null) return;
            String timestamp = DATE_FMT.format(new Date());
            File reportFile = new File(reportDir, "crash-" + timestamp + ".txt");

            try (PrintWriter pw = new PrintWriter(reportFile)) {
                pw.println("==============================================");
                pw.println("  FKU \u5d29\u6e83\u76d1\u63a7\u62a5\u544a");
                pw.println("==============================================");
                pw.println("\u751f\u6210\u65f6\u95f4: " + LOG_FMT.format(new Date()));
                pw.println("\u5d29\u6e83\u539f\u56e0: " + reason);
                pw.println("\u8be6\u7ec6\u4fe1\u606f: " + detail);
                pw.println();
                pw.println("--- \u7cfb\u7edf\u4fe1\u606f ---");
                pw.println("Java\u7248\u672c: " + System.getProperty("java.version", "\u672a\u77e5"));
                pw.println("Java\u4f9b\u5e94\u5546: " + System.getProperty("java.vendor", "\u672a\u77e5"));
                pw.println("\u64cd\u4f5c\u7cfb\u7edf: " + System.getProperty("os.name", "\u672a\u77e5") + " " + System.getProperty("os.version", ""));
                pw.println("\u7cfb\u7edf\u67b6\u6784: " + System.getProperty("os.arch", "\u672a\u77e5"));
                pw.println("\u53ef\u7528\u5904\u7406\u5668: " + Runtime.getRuntime().availableProcessors());
                pw.println("\u6700\u5927\u5185\u5b58: " + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + " MB");
                pw.println("\u7528\u6237\u76ee\u5f55: " + System.getProperty("user.home", "\u672a\u77e5"));
                pw.println();
                pw.println("--- FKU \u72b6\u6001 ---");
                pw.println("\u6700\u540e\u9636\u6bb5: " + currentStage);
                pw.println("\u5e72\u51c0\u5173\u95ed: " + cleanShutdown);
                pw.println();

                if (throwable != null) {
                    pw.println("--- \u5f02\u5e38\u6808 ---");
                    StringWriter sw = new StringWriter();
                    throwable.printStackTrace(new PrintWriter(sw));
                    pw.println(sw);
                    pw.println();

                    // \u6839\u56e0\u94fe
                    Throwable cause = throwable;
                    int level = 0;
                    while (cause.getCause() != null && cause != cause.getCause() && level < 10) {
                        cause = cause.getCause();
                        level++;
                        pw.println("--- \u6839\u56e0 (level " + level + ") ---");
                        sw = new StringWriter();
                        cause.printStackTrace(new PrintWriter(sw));
                        pw.println(sw);
                    }
                }

                pw.println("--- \u542f\u52a8\u4fe1\u606f ---");
                pw.println("\u6700\u540e\u52a0\u8f7d\u9636\u6bb5: " + currentStage);
                pw.println();
                pw.println("==============================================");
                pw.println("  \u8bf7\u5c06\u6b64\u6587\u4ef6\u5185\u5bb9\u63d0\u4f9b\u7ed9 FKU \u5f00\u53d1\u8005");
                pw.println("  \u6587\u4ef6\u4f4d\u7f6e: " + reportFile.getAbsolutePath());
                pw.println("==============================================");
            }

            System.err.println("[FKU CrashMonitor] \u5d29\u6e83\u62a5\u544a\u5df2\u751f\u6210: " + reportFile.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("[FKU CrashMonitor] \u751f\u6210\u5d29\u6e83\u62a5\u544a\u5931\u8d25: " + e.getMessage());
        }
    }

    private static void log(String msg) {
        String line = "[" + LOG_FMT.format(new Date()) + "] " + msg;
        System.out.println("[FKU CrashMonitor] " + line);
        try {
            if (lockFile != null) {
                File logFile = new File(lockFile.getParentFile(), "crash_monitor.log");
                Files.writeString(logFile.toPath(), line + System.lineSeparator(),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
        } catch (IOException ignored) {}
    }
}
