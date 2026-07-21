package fku.org.example.fku.features.fastjoin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import net.minecraft.client.Minecraft;

public class FastJoinConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static FastJoinConfig instance;
    public boolean enabled = false;
    public String mode = "SMOOTH";
    public int targetRenderDistance = 12;
    public int recoverSpeed = 1;
    public boolean showLoadingProgress = true;
    public boolean onTimeoutFallback = true;

    private static File getConfigFile() {
        File configDir = new File(FastJoinConfig.getGameDirectory(), "fku");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return new File(configDir, "fastjoin.json");
    }

    private static File getGameDirectory() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
                return mc.gameDirectory;
            }
        }
        catch (Exception exception) {
            // ignored
        }
        return Paths.get("config", new String[0]).toAbsolutePath().normalize().getParent().toFile();
    }

    public static FastJoinConfig getInstance() {
        if (instance == null) {
            FastJoinConfig.load();
        }
        return instance;
    }

    public static void load() {
        File f = FastJoinConfig.getConfigFile();
        if (f.exists()) {
            try (FileReader r = new FileReader(f);){
                instance = (FastJoinConfig)GSON.fromJson(r, FastJoinConfig.class);
            }
            catch (IOException e) {
                instance = new FastJoinConfig();
            }
        } else {
            instance = new FastJoinConfig();
            FastJoinConfig.save();
        }
    }

    public static void save() {
        if (instance == null) {
            return;
        }
        try (FileWriter w = new FileWriter(FastJoinConfig.getConfigFile());){
            GSON.toJson(instance, w);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setEnabled(boolean v) {
        this.enabled = v;
        FastJoinConfig.save();
    }

    public void setMode(String v) {
        if ("EXTREME".equals(v) || "SMOOTH".equals(v) || "COMPAT".equals(v)) {
            this.mode = v;
        }
        FastJoinConfig.save();
    }

    public void setTargetRenderDistance(int v) {
        this.targetRenderDistance = Math.max(2, Math.min(32, v));
        FastJoinConfig.save();
    }

    public void setRecoverSpeed(int v) {
        this.recoverSpeed = Math.max(1, Math.min(4, v));
        FastJoinConfig.save();
    }

    public void setShowLoadingProgress(boolean v) {
        this.showLoadingProgress = v;
        FastJoinConfig.save();
    }

    public void setOnTimeoutFallback(boolean v) {
        this.onTimeoutFallback = v;
        FastJoinConfig.save();
    }

    public static String getModeTooltip(String mode) {
        return switch (mode) {
            case "EXTREME" -> "\u00a77\u521d\u59cb\u4ec5\u52a0\u8f7d1\u4e2a\u533a\u5757\uff0c\u8fdb\u5165\u540e\u9010\u6b65\u6062\u590d\u89c6\u8ddd\u3002\u52a0\u8f7d\u6700\u5feb\uff0c\u4f46\u8fdb\u5165\u540e\u53ef\u80fd\u77ed\u6682\u770b\u5230\u5730\u5f62\u52a0\u8f7d\u3002";
            case "SMOOTH" -> "\u00a77\u521d\u59cb\u52a0\u8f7d\u4e00\u534a\u89c6\u8ddd\uff0c\u8fdb\u5165\u540e\u5e73\u7f13\u6062\u590d\u3002\u901f\u5ea6\u4e0e\u7a33\u5b9a\u6027\u5747\u8861\u3002";
            case "COMPAT" -> "\u00a77\u4e0d\u6539\u52a8\u52a0\u8f7d\u903b\u8f91\uff0c\u4ec5\u542f\u7528\u8d85\u65f6\u4fdd\u62a4\u3002\u9047\u5230\u517c\u5bb9\u6027\u95ee\u9898\u65f6\u4f7f\u7528\u3002";
            default -> "";
        };
    }
}

