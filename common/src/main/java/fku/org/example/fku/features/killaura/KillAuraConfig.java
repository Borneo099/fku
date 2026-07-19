package fku.org.example.fku.features.killaura; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class KillAuraConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static KillAuraConfig instance;
    public boolean enabled = false;
    public double range = 4.5;
    public int delay = 2;          // ticks between attacks
    public boolean autoSwitch = true;
    /** 是否自动调整视角朝向目标 */
    public boolean autoRotate = true;
    /** 是否只攻击玩家 */
    public boolean playersOnly = false;
    /** 攻击模式: 0=最近, 1=最低血量 */
    public int targetMode = 0;
    /** 是否等待攻击冷却满 */
    public boolean attackCooldown = false;
    /** 是否同时攻击所有目标 */
    public boolean multiTarget = false;
    /** 实体白名单（空=攻击所有, 填 "minecraft:zombie" 等只攻击这些） */
    public List<String> whitelist = new ArrayList<>();

    private static File getConfigFile() {
        try { var mc = net.minecraft.client.Minecraft.getInstance(); if (mc != null && mc.gameDirectory != null) return new File(new File(mc.gameDirectory, "fku"), "kill_aura.json"); } catch (Exception ignored) {}
        return new File(Paths.get("config").toAbsolutePath().normalize().getParent().toFile(), "fku/kill_aura.json");
    }
    public static KillAuraConfig getInstance() { if (instance == null) load(); return instance; }
    public static void load() { File f = getConfigFile(); if (f.exists()) { try (FileReader r = new FileReader(f)) { instance = GSON.fromJson(r, KillAuraConfig.class); } catch (Exception e) { instance = new KillAuraConfig(); } } else { instance = new KillAuraConfig(); save(); } if (instance.whitelist == null) instance.whitelist = new ArrayList<>(); }
    public static void save() { if (instance == null) return; getConfigFile().getParentFile().mkdirs(); try (FileWriter w = new FileWriter(getConfigFile())) { GSON.toJson(instance, w); } catch (IOException e) { e.printStackTrace(); } }
    public void setEnabled(boolean v) { this.enabled = v; save(); }
    public void setRange(double v) { this.range = Math.max(1, Math.min(10, v)); save(); }
    public void setDelay(int v) { this.delay = Math.max(0, Math.min(10, v)); save(); }
    public void setAutoSwitch(boolean v) { this.autoSwitch = v; save(); }
    public void setAutoRotate(boolean v) { this.autoRotate = v; save(); }
    public void setPlayersOnly(boolean v) { this.playersOnly = v; save(); }
    public void setTargetMode(int v) { this.targetMode = Math.max(0, Math.min(1, v)); save(); }
    public void setAttackCooldown(boolean v) { this.attackCooldown = v; save(); }
    public void setMultiTarget(boolean v) { this.multiTarget = v; save(); }
}
