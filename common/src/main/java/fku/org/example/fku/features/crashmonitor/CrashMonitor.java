package fku.org.example.fku.features.crashmonitor;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CrashMonitor {
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
    private static final SimpleDateFormat LOG_FMT = new SimpleDateFormat("HH:mm:ss");
    private static File reportDir;
    private static File lockFile;
    private static File stageFile;
    private static String currentStage;
    private static boolean cleanShutdown;
    private static boolean initialized;

    public static void init(File fkuDirectory) {
        if (initialized) {
            return;
        }
        initialized = true;
        try {
            if (!fkuDirectory.exists()) {
                fkuDirectory.mkdirs();
            }
            if (!(reportDir = new File(fkuDirectory, "crash_reports")).exists()) {
                reportDir.mkdirs();
            }
            lockFile = new File(fkuDirectory, ".crash_lock");
            stageFile = new File(fkuDirectory, ".crash_stage");
            CrashMonitor.checkPreviousCrash();
            CrashMonitor.writeLockFile();
            Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
                CrashMonitor.generateCrashReport("\u672a\u6355\u83b7\u5f02\u5e38", "\u7ebf\u7a0b: " + thread.getName(), throwable);
                if (defaultHandler != null) {
                    defaultHandler.uncaughtException(thread, throwable);
                }
            });
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (!cleanShutdown && lockFile != null && lockFile.exists()) {
                    CrashMonitor.generateCrashReport("JVM\u5173\u95ed - \u7591\u4f3c\u5d29\u6e83", "\u6700\u540e\u9636\u6bb5: " + currentStage, null);
                }
            }, "FKU Crash Monitor"));
            CrashMonitor.log("\u521d\u59cb\u5316\u5b8c\u6210\uff0c\u62a5\u544a\u76ee\u5f55: " + reportDir.getAbsolutePath());
        }
        catch (Exception e) {
            try {
                File fallback = new File("fku_crash_monitor_error.txt");
                try (PrintWriter pw = new PrintWriter(fallback);){
                    e.printStackTrace(pw);
                }
            }
            catch (Exception exception) {
                // ignored
            }
        }
    }

    public static void setStage(String stage) {
        currentStage = stage;
        if (stageFile == null) {
            return;
        }
        try {
            Files.writeString(stageFile.toPath(), (CharSequence)stage, new OpenOption[0]);
        }
        catch (IOException iOException) {
            // ignored
        }
    }

    public static void startPhase(String phaseName) {
        CrashMonitor.setStage("\u8fdb\u884c\u4e2d: " + phaseName);
    }

    public static void endPhase(String phaseName) {
        CrashMonitor.log("[OK] " + phaseName + " \u5b8c\u6210");
    }

    public static void markLaunchComplete() {
        cleanShutdown = true;
        CrashMonitor.deleteLockFile();
        CrashMonitor.setStage("\u542f\u52a8\u5b8c\u6210");
    }

    public static void logInfo(String msg) {
        CrashMonitor.log("[\u4fe1\u606f] " + msg);
    }

    public static void logWarn(String msg) {
        CrashMonitor.log("[\u8b66\u544a] " + msg);
    }

    private static void checkPreviousCrash() {
        if (lockFile != null && lockFile.exists()) {
            String lastStage = "\u672a\u77e5";
            if (stageFile != null && stageFile.exists()) {
                try {
                    lastStage = Files.readString(stageFile.toPath());
                }
                catch (IOException iOException) {
                    // ignored
                }
            }
            CrashMonitor.generateCrashReport("\u4e0a\u6b21\u542f\u52a8\u5d29\u6e83", "\u5d29\u6e83\u524d\u6700\u540e\u9636\u6bb5: " + lastStage, null);
        }
    }

    private static void writeLockFile() {
        if (lockFile == null) {
            return;
        }
        try {
            Files.writeString(lockFile.toPath(), (CharSequence)("DO NOT DELETE - FKU crash monitor lock\nStarted: " + DATE_FMT.format(new Date())), new OpenOption[0]);
        }
        catch (IOException iOException) {
            // ignored
        }
    }

    private static void deleteLockFile() {
        try {
            if (lockFile != null && lockFile.exists()) {
                Files.delete(lockFile.toPath());
            }
        }
        catch (IOException iOException) {
            // ignored
        }
        try {
            if (stageFile != null && stageFile.exists()) {
                Files.delete(stageFile.toPath());
            }
        }
        catch (IOException iOException) {
            // ignored
        }
    }

    private static synchronized void generateCrashReport(String reason, String detail, Throwable throwable) {
        try {
            if (reportDir == null) {
                return;
            }
            String timestamp = DATE_FMT.format(new Date());
            File reportFile = new File(reportDir, "crash-" + timestamp + ".txt");
            try (PrintWriter pw = new PrintWriter(reportFile);){
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
                pw.println("\u6700\u5927\u5185\u5b58: " + Runtime.getRuntime().maxMemory() / 1024L / 1024L + " MB");
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
                    int level = 0;
                    for (Throwable cause = throwable; cause.getCause() != null && cause != cause.getCause() && level < 10; cause = cause.getCause()) {
                        pw.println("--- \u6839\u56e0 (level " + ++level + ") ---");
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
        }
        catch (Exception e) {
            System.err.println("[FKU CrashMonitor] \u751f\u6210\u5d29\u6e83\u62a5\u544a\u5931\u8d25: " + e.getMessage());
        }
    }

    private static void log(String msg) {
        String line = "[" + LOG_FMT.format(new Date()) + "] " + msg;
        System.out.println("[FKU CrashMonitor] " + line);
        try {
            if (lockFile != null) {
                File logFile = new File(lockFile.getParentFile(), "crash_monitor.log");
                Files.writeString(logFile.toPath(), (CharSequence)(line + System.lineSeparator()), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
        }
        catch (IOException iOException) {
            // ignored
        }
    }

    static {
        currentStage = "\u672a\u542f\u52a8";
        cleanShutdown = false;
        initialized = false;
    }
}

