package lexis.Hack.Hacks.Render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.RenderListener;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.IOUtils;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

public class MatrixOverlayHack extends Hack implements RenderListener {
   private HackConfig config;
   private static final String CONFIG_KEY = "矩阵覆盖";
   private Style style;
   private double scanSpeed;
   private boolean loopScan;
   private double scanDuration;
   private double gridSpeed;
   private double moodIntensity;
   private double wetness;
   private double petalDensity;
   private double pinkIntensity;
   private double fogDensity;
   private double grainIntensity;
   private double styleMode;
   private double sunSpeed;
   private double pulseSpeed;
   private int programId;
   private int vao;
   private int vbo;
   private long startTime;
   private boolean initialized;

   public MatrixOverlayHack() {
      super("矩阵覆盖", new String[]{"全屏风格化效果好看", "和 OptiFine(高清修复) 不兼容"}, Hack.Category.RENDER, true);
      this.style = MatrixOverlayHack.Style.MATRIX;
      this.scanSpeed = 1.0;
      this.loopScan = true;
      this.scanDuration = 4.0;
      this.gridSpeed = 3.0;
      this.moodIntensity = 0.8;
      this.wetness = 0.5;
      this.petalDensity = 1.0;
      this.pinkIntensity = 0.7;
      this.fogDensity = 0.02;
      this.grainIntensity = 0.3;
      this.styleMode = 0.0;
      this.sunSpeed = 1.0;
      this.pulseSpeed = 2.0;
      this.programId = -1;
      this.vao = -1;
      this.vbo = -1;
      this.initialized = false;
      this.addSetting(new Hack.Setting("风格", "选择视觉风格", "黑客帝国", new String[]{"黑客帝国", "寂静岭（情绪）", "樱花", "寂静岭（表里）", "赛博网格"}));
      this.addSetting(new Hack.Setting("扫描速度", "扫描线速度", this.scanSpeed, 0.1, 5.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("扫描持续时间", "单次扫描时长", this.scanDuration, 1.0, 10.0, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("网格速度", "黑客帝国网格流动速度", this.gridSpeed, 0.0, 10.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("情绪强度", "寂静岭情绪强度", this.moodIntensity, 0.0, 1.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("湿润度", "地面反光程度", this.wetness, 0.0, 1.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("花瓣密度", "樱花花瓣密度", this.petalDensity, 0.0, 2.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("粉色强度", "粉色滤镜强度", this.pinkIntensity, 0.0, 1.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("雾密度", "雾气浓度", this.fogDensity, 0.0, 0.1, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("噪点强度", "颗粒噪点强度", this.grainIntensity, 0.0, 1.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("太阳速度", "赛博网格太阳速度", this.sunSpeed, 0.0, 5.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("脉冲速度", "网格脉冲速度", this.pulseSpeed, 0.0, 5.0, Hack.ValueDisplay.DECIMAL));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      String styleStr = this.config.getStringSetting("矩阵覆盖", "风格", "黑客帝国");
      Style[] var2 = MatrixOverlayHack.Style.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Style s = var2[var4];
         if (s.toString().equals(styleStr)) {
            this.style = s;
            break;
         }
      }

      this.scanSpeed = this.config.getDoubleSetting("矩阵覆盖", "扫描速度", 1.0);
      this.loopScan = this.config.getBooleanSetting("矩阵覆盖", "循环扫描", true);
      this.scanDuration = this.config.getDoubleSetting("矩阵覆盖", "扫描持续时间", 4.0);
      this.gridSpeed = this.config.getDoubleSetting("矩阵覆盖", "网格速度", 3.0);
      this.moodIntensity = this.config.getDoubleSetting("矩阵覆盖", "情绪强度", 0.8);
      this.wetness = this.config.getDoubleSetting("矩阵覆盖", "湿润度", 0.5);
      this.petalDensity = this.config.getDoubleSetting("矩阵覆盖", "花瓣密度", 1.0);
      this.pinkIntensity = this.config.getDoubleSetting("矩阵覆盖", "粉色强度", 0.7);
      this.fogDensity = this.config.getDoubleSetting("矩阵覆盖", "雾密度", 0.02);
      this.grainIntensity = this.config.getDoubleSetting("矩阵覆盖", "噪点强度", 0.3);
      this.styleMode = this.config.getDoubleSetting("矩阵覆盖", "表里模式", 0.0);
      this.sunSpeed = this.config.getDoubleSetting("矩阵覆盖", "太阳速度", 1.0);
      this.pulseSpeed = this.config.getDoubleSetting("矩阵覆盖", "脉冲速度", 2.0);
      Iterator var6 = this.getSettings().iterator();

      while(var6.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var6.next();
         switch (setting.getName()) {
            case "风格":
               setting.setValue(this.style.toString());
               break;
            case "扫描速度":
               setting.setValue(this.scanSpeed);
               break;
            case "循环扫描":
               setting.setValue(this.loopScan);
               break;
            case "扫描持续时间":
               setting.setValue(this.scanDuration);
               break;
            case "网格速度":
               setting.setValue(this.gridSpeed);
               break;
            case "情绪强度":
               setting.setValue(this.moodIntensity);
               break;
            case "湿润度":
               setting.setValue(this.wetness);
               break;
            case "花瓣密度":
               setting.setValue(this.petalDensity);
               break;
            case "粉色强度":
               setting.setValue(this.pinkIntensity);
               break;
            case "雾密度":
               setting.setValue(this.fogDensity);
               break;
            case "噪点强度":
               setting.setValue(this.grainIntensity);
               break;
            case "表里模式":
               setting.setValue(this.styleMode);
               break;
            case "太阳速度":
               setting.setValue(this.sunSpeed);
               break;
            case "脉冲速度":
               setting.setValue(this.pulseSpeed);
         }
      }

   }

   public void onEnable() {
      EventManager.add(RenderListener.class, this);
      this.startTime = System.currentTimeMillis();
      this.initialized = false;
   }

   public void onDisable() {
      EventManager.remove(RenderListener.class, this);
      if (this.initialized) {
         if (this.programId != -1) {
            GL20.glDeleteProgram(this.programId);
         }

         if (this.vao != -1) {
            GL30.glDeleteVertexArrays(this.vao);
         }

         if (this.vbo != -1) {
            GL20.glDeleteBuffers(this.vbo);
         }

         this.programId = -1;
         this.vao = -1;
         this.vbo = -1;
         this.initialized = false;
      }

   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(true) {
         label121:
         while(var2.hasNext()) {
            Hack.Setting setting = (Hack.Setting)var2.next();
            switch (setting.getName()) {
               case "风格":
                  String s = setting.getString();
                  Style[] var7 = MatrixOverlayHack.Style.values();
                  int var8 = var7.length;
                  int var9 = 0;

                  while(true) {
                     if (var9 >= var8) {
                        continue label121;
                     }

                     Style st = var7[var9];
                     if (st.toString().equals(s) && this.style != st) {
                        this.style = st;
                        needSave = true;
                        continue label121;
                     }

                     ++var9;
                  }
               case "扫描速度":
                  if (setting.getDouble() != this.scanSpeed) {
                     this.scanSpeed = setting.getDouble();
                     needSave = true;
                  }
                  break;
               case "循环扫描":
                  if (setting.getBoolean() != this.loopScan) {
                     this.loopScan = setting.getBoolean();
                     needSave = true;
                  }
                  break;
               case "扫描持续时间":
                  if (setting.getDouble() != this.scanDuration) {
                     this.scanDuration = setting.getDouble();
                     needSave = true;
                  }
                  break;
               case "网格速度":
                  if (setting.getDouble() != this.gridSpeed) {
                     this.gridSpeed = setting.getDouble();
                     needSave = true;
                  }
                  break;
               case "情绪强度":
                  if (setting.getDouble() != this.moodIntensity) {
                     this.moodIntensity = setting.getDouble();
                     needSave = true;
                  }
                  break;
               case "湿润度":
                  if (setting.getDouble() != this.wetness) {
                     this.wetness = setting.getDouble();
                     needSave = true;
                  }
                  break;
               case "花瓣密度":
                  if (setting.getDouble() != this.petalDensity) {
                     this.petalDensity = setting.getDouble();
                     needSave = true;
                  }
                  break;
               case "粉色强度":
                  if (setting.getDouble() != this.pinkIntensity) {
                     this.pinkIntensity = setting.getDouble();
                     needSave = true;
                  }
                  break;
               case "雾密度":
                  if (setting.getDouble() != this.fogDensity) {
                     this.fogDensity = setting.getDouble();
                     needSave = true;
                  }
                  break;
               case "噪点强度":
                  if (setting.getDouble() != this.grainIntensity) {
                     this.grainIntensity = setting.getDouble();
                     needSave = true;
                  }
                  break;
               case "表里模式":
                  if (setting.getDouble() != this.styleMode) {
                     this.styleMode = setting.getDouble();
                     needSave = true;
                  }
                  break;
               case "太阳速度":
                  if (setting.getDouble() != this.sunSpeed) {
                     this.sunSpeed = setting.getDouble();
                     needSave = true;
                  }
                  break;
               case "脉冲速度":
                  if (setting.getDouble() != this.pulseSpeed) {
                     this.pulseSpeed = setting.getDouble();
                     needSave = true;
                  }
            }
         }

         if (needSave) {
            this.config.saveHackSettings("矩阵覆盖", this.getSettings());
         }

         return;
      }
   }

   public void onRender(PoseStack poseStack, float partialTicks) {
      if (!this.initialized) {
         this.loadShaders();
         this.initVao();
         this.initialized = true;
      }

      if (this.programId != -1 && this.vao != -1) {
         Minecraft mc = Minecraft.m_91087_();
         if (mc.m_91385_() != null) {
            GL20.glUseProgram(this.programId);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            GL20.glActiveTexture(33984);
            GL20.glBindTexture(3553, mc.m_91385_().m_83980_());
            GL20.glUniform1i(GL20.glGetUniformLocation(this.programId, "MainDepthSampler"), 0);
            GL20.glActiveTexture(33985);
            GL20.glBindTexture(3553, mc.m_91385_().m_83975_());
            GL20.glUniform1i(GL20.glGetUniformLocation(this.programId, "MainColorSampler"), 1);
            GL20.glUniform1f(GL20.glGetUniformLocation(this.programId, "U_GameTime"), (float)(System.currentTimeMillis() - this.startTime) / 1000.0F);
            GL20.glUniform2f(GL20.glGetUniformLocation(this.programId, "ScreenSize"), (float)mc.m_91268_().m_85443_(), (float)mc.m_91268_().m_85444_());
            GL20.glUniform2f(GL20.glGetUniformLocation(this.programId, "FboSize"), (float)mc.m_91385_().f_83915_, (float)mc.m_91385_().f_83916_);
            if (mc.f_91063_.m_109153_() != null) {
               GL20.glUniform3f(GL20.glGetUniformLocation(this.programId, "U_CameraPosition"), (float)mc.f_91063_.m_109153_().m_90583_().f_82479_, (float)mc.f_91063_.m_109153_().m_90583_().f_82480_, (float)mc.f_91063_.m_109153_().m_90583_().f_82481_);
            }

            this.uploadMatrix(this.programId, "U_InverseProjectionMatrix", (new Matrix4f(RenderSystem.getProjectionMatrix())).invert());
            this.uploadMatrix(this.programId, "U_InverseViewMatrix", (new Matrix4f(RenderSystem.getModelViewMatrix())).invert());
            GL20.glUniform1i(GL20.glGetUniformLocation(this.programId, "U_Mode"), this.style.ordinal());
            GL20.glUniform1f(GL20.glGetUniformLocation(this.programId, "U_ScanSpeed"), (float)this.scanSpeed);
            GL20.glUniform1f(GL20.glGetUniformLocation(this.programId, "U_LoopEnabled"), this.loopScan ? 1.0F : 0.0F);
            GL20.glUniform1f(GL20.glGetUniformLocation(this.programId, "U_ScanDuration"), (float)this.scanDuration);
            GL20.glUniform1f(GL20.glGetUniformLocation(this.programId, "U_GridSpeed"), (float)this.gridSpeed);
            GL20.glUniform3f(GL20.glGetUniformLocation(this.programId, "U_SkyTop"), 0.4F, 0.6F, 0.8F);
            GL20.glUniform3f(GL20.glGetUniformLocation(this.programId, "U_SkyBottom"), 0.2F, 0.2F, 0.3F);
            GL20.glUniform3f(GL20.glGetUniformLocation(this.programId, "U_FogColor"), 0.55F, 0.55F, 0.58F);
            GL20.glUniform3f(GL20.glGetUniformLocation(this.programId, "U_MoodColor"), 0.3F, 0.4F, 0.8F);
            GL20.glUniform1f(GL20.glGetUniformLocation(this.programId, "U_MoodIntensity"), (float)this.moodIntensity);
            GL20.glUniform1f(GL20.glGetUniformLocation(this.programId, "U_Wetness"), (float)this.wetness);
            GL20.glUniform1f(GL20.glGetUniformLocation(this.programId, "U_PetalDensity"), (float)this.petalDensity);
            GL20.glUniform1f(GL20.glGetUniformLocation(this.programId, "U_PinkIntensity"), (float)this.pinkIntensity);
            GL20.glUniform1f(GL20.glGetUniformLocation(this.programId, "U_FogDensity"), (float)this.fogDensity);
            GL20.glUniform1f(GL20.glGetUniformLocation(this.programId, "U_GrainIntensity"), (float)this.grainIntensity);
            GL20.glUniform1f(GL20.glGetUniformLocation(this.programId, "U_StyleMode"), (float)this.styleMode);
            GL20.glUniform1f(GL20.glGetUniformLocation(this.programId, "U_SunSpeed"), (float)this.sunSpeed);
            GL20.glUniform1f(GL20.glGetUniformLocation(this.programId, "U_PulseSpeed"), (float)this.pulseSpeed);
            GL30.glBindVertexArray(this.vao);
            GL20.glDrawArrays(4, 0, 6);
            GL30.glBindVertexArray(0);
            GL20.glActiveTexture(33985);
            GL20.glBindTexture(3553, 0);
            GL20.glActiveTexture(33984);
            GL20.glBindTexture(3553, 0);
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            GL20.glUseProgram(0);
         }
      }
   }

   private void uploadMatrix(int program, String name, Matrix4f mat) {
      int loc = GL20.glGetUniformLocation(program, name);
      if (loc != -1) {
         FloatBuffer buffer = MemoryUtil.memAllocFloat(16);
         mat.get(buffer);
         GL20.glUniformMatrix4fv(loc, false, buffer);
         MemoryUtil.memFree(buffer);
      }

   }

   private void initVao() {
      float[] vertices = new float[]{-1.0F, -1.0F, 0.0F, 1.0F, -1.0F, 0.0F, -1.0F, 1.0F, 0.0F, -1.0F, 1.0F, 0.0F, 1.0F, -1.0F, 0.0F, 1.0F, 1.0F, 0.0F};
      this.vao = GL30.glGenVertexArrays();
      this.vbo = GL20.glGenBuffers();
      GL30.glBindVertexArray(this.vao);
      GL20.glBindBuffer(34962, this.vbo);
      FloatBuffer buffer = MemoryUtil.memAllocFloat(vertices.length);
      buffer.put(vertices).flip();
      GL20.glBufferData(34962, buffer, 35044);
      MemoryUtil.memFree(buffer);
      GL20.glVertexAttribPointer(0, 3, 5126, false, 0, 0L);
      GL20.glEnableVertexAttribArray(0);
      GL30.glBindVertexArray(0);
   }

   private void loadShaders() {
      try {
         int v = this.createShader("matrix.vsh", 35633);
         int f = this.createShader("matrix.fsh", 35632);
         this.programId = GL20.glCreateProgram();
         GL20.glAttachShader(this.programId, v);
         GL20.glAttachShader(this.programId, f);
         GL20.glLinkProgram(this.programId);
         int linked = GL20.glGetProgrami(this.programId, 35714);
         if (linked == 0) {
            String log = GL20.glGetProgramInfoLog(this.programId, 1024);
            System.err.println("Shader program linking failed: " + log);
            this.programId = -1;
         } else {
            System.out.println("Matrix shader loaded successfully.");
         }
      } catch (Exception var5) {
         var5.printStackTrace();
         this.programId = -1;
      }

   }

   private int createShader(String filename, int type) throws IOException {
      String path = "assets/lexis/shaders/" + filename;
      InputStream is = this.getClass().getClassLoader().getResourceAsStream(path);

      byte var9;
      label47: {
         int var8;
         try {
            if (is == null) {
               throw new IOException("Shader not found in classpath: " + path);
            }

            String source = IOUtils.toString(is, StandardCharsets.UTF_8);
            int shader = GL20.glCreateShader(type);
            GL20.glShaderSource(shader, source);
            GL20.glCompileShader(shader);
            int compiled = GL20.glGetShaderi(shader, 35713);
            if (compiled == 0) {
               String log = GL20.glGetShaderInfoLog(shader, 1024);
               System.err.println("Shader compilation failed (" + filename + "): " + log);
               var9 = -1;
               break label47;
            }

            var8 = shader;
         } catch (Throwable var11) {
            if (is != null) {
               try {
                  is.close();
               } catch (Throwable var10) {
                  var11.addSuppressed(var10);
               }
            }

            throw var11;
         }

         if (is != null) {
            is.close();
         }

         return var8;
      }

      if (is != null) {
         is.close();
      }

      return var9;
   }

   public void onClick() {
      this.toggle();
   }

   public static enum Style {
      MATRIX("黑客帝国"),
      SILENT_HILL_MOODY("寂静岭（情绪）"),
      SAKURA("樱花"),
      SILENT_HILL_DUAL("寂静岭（表里）"),
      CYBER_GRID("赛博网格");

      private final String name;

      private Style(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }

      // $FF: synthetic method
      private static Style[] $values() {
         return new Style[]{MATRIX, SILENT_HILL_MOODY, SAKURA, SILENT_HILL_DUAL, CYBER_GRID};
      }
   }
}
