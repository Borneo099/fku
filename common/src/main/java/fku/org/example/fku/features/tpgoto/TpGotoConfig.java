package fku.org.example.fku.features.tpgoto; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * 传送前往 — 合并配置类
 * 默认开启，无需开关
 */
public class TpGotoConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static TpGotoConfig instance;

    /** TP时自动启用飞行 */
    public boolean tpFlightEnabled = true;
    /** 等待区块加载后再发送下一包 */
    public boolean waitForChunk = true;
    /** 区块加载等待超时（毫秒），默认3秒后强制继续 */
    public int chunkWaitTimeout = 3000;
    /** 发包间隔（毫秒） */
    public int packetInterval = 30;
    /** 停止距离（格），进入此距离视为到达 */
    public double stopDistance = 1.5;
    /** 路径简化最大步长 */
    public double maxStep = 3.0;
    /** 最大搜索范围 */
    public double maxRange = 100.0;
    /** 允许空中路径 */
    public boolean airPath = true;
    /** 路径渲染 */
    public boolean renderPath = true;
    /** 位置校验 — 每次TP后检查玩家是否实际到达目标位置（防止被卡住） */
    public boolean positionCheck = true;
    /** 位置校验间隔（毫秒），减少资源占用 */
    public int positionCheckInterval = 1000;

    private static File getConfigFile() {
        try { var mc = net.minecraft.client.Minecraft.getInstance(); if (mc != null && mc.gameDirectory != null) return new File(new File(mc.gameDirectory, "fku"), "tpgoto.json"); } catch (Exception ignored) {}
        return new File(Paths.get("config").toAbsolutePath().normalize().getParent().toFile(), "fku/tpgoto.json");
    }
    public static TpGotoConfig getInstance() { if (instance == null) load(); return instance; }
    public static void load() { File f = getConfigFile(); if (f.exists()) { try (FileReader r = new FileReader(f)) { instance = GSON.fromJson(r, TpGotoConfig.class); } catch (Exception e) { instance = new TpGotoConfig(); } } else { instance = new TpGotoConfig(); save(); } }
    public static void save() { if (instance == null) return; getConfigFile().getParentFile().mkdirs(); try (FileWriter w = new FileWriter(getConfigFile())) { GSON.toJson(instance, w); } catch (IOException e) { e.printStackTrace(); } }
    public void setTpFlightEnabled(boolean v) { this.tpFlightEnabled = v; save(); }
    public void setWaitForChunk(boolean v) { this.waitForChunk = v; save(); }
    public void setChunkWaitTimeout(int v) { this.chunkWaitTimeout = Math.max(500, Math.min(30000, v)); save(); }
    public void setPacketInterval(int v) { this.packetInterval = Math.max(5, Math.min(200, v)); save(); }
    public void setStopDistance(double v) { this.stopDistance = Math.max(0.5, Math.min(10, v)); save(); }
    public void setMaxStep(double v) { this.maxStep = Math.max(0.5, Math.min(10, v)); save(); }
    public void setMaxRange(double v) { this.maxRange = Math.max(10, Math.min(500, v)); save(); }
    public void setAirPath(boolean v) { this.airPath = v; save(); }
    public void setRenderPath(boolean v) { this.renderPath = v; save(); }
    public void setPositionCheck(boolean v) { this.positionCheck = v; save(); }
    public void setPositionCheckInterval(int v) { this.positionCheckInterval = Math.max(200, Math.min(5000, v)); save(); }
}