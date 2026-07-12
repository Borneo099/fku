package lexis.Hack.Utils.Render;

import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

public final class OutlineShaderRenderer {
   private static OutlineShaderRenderer INSTANCE;
   private int programId = -1;
   private int vao = -1;
   private int vbo = -1;
   private boolean initialized = false;

   public static OutlineShaderRenderer get() {
      if (INSTANCE == null) {
         INSTANCE = new OutlineShaderRenderer();
      }

      return INSTANCE;
   }

   private OutlineShaderRenderer() {
   }

   public void render(int srcColorTexId, int w, int h, int width, int mode, boolean espColor, int fillColor, int outlineColor) {
      if (w > 0 && h > 0) {
         if (!this.initialized) {
            this.loadShaders();
            this.initVao();
            this.initialized = true;
         }

         if (this.programId != -1 && this.vao != -1) {
            GL20.glUseProgram(this.programId);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            GL20.glActiveTexture(33984);
            GL20.glBindTexture(3553, srcColorTexId);
            GL20.glUniform1i(this.loc("u_Texture"), 0);
            GL20.glUniform2f(this.loc("u_Size"), (float)w, (float)h);
            GL20.glUniform1i(this.loc("u_Width"), width);
            GL20.glUniform1i(this.loc("u_Mode"), mode);
            GL20.glUniform1i(this.loc("u_UseEspColor"), espColor ? 1 : 0);
            GL20.glUniform4f(this.loc("u_FillColor"), (float)(fillColor >> 16 & 255) / 255.0F, (float)(fillColor >> 8 & 255) / 255.0F, (float)(fillColor & 255) / 255.0F, (float)(fillColor >> 24 & 255) / 255.0F);
            GL20.glUniform4f(this.loc("u_OutlineColor"), (float)(outlineColor >> 16 & 255) / 255.0F, (float)(outlineColor >> 8 & 255) / 255.0F, (float)(outlineColor & 255) / 255.0F, (float)(outlineColor >> 24 & 255) / 255.0F);
            GL30.glBindVertexArray(this.vao);
            GL20.glDrawArrays(4, 0, 6);
            GL30.glBindVertexArray(0);
            GL20.glActiveTexture(33984);
            GL20.glBindTexture(3553, 0);
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            GL20.glUseProgram(0);
         }
      }
   }

   private int loc(String name) {
      return GL20.glGetUniformLocation(this.programId, name);
   }

   public void destroy() {
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
         int v = this.createShader("outline.vsh", 35633);
         int f = this.createShader("outline.fsh", 35632);
         if (v == -1 || f == -1) {
            this.programId = -1;
            return;
         }

         this.programId = GL20.glCreateProgram();
         GL20.glAttachShader(this.programId, v);
         GL20.glAttachShader(this.programId, f);
         GL20.glLinkProgram(this.programId);
         int linked = GL20.glGetProgrami(this.programId, 35714);
         if (linked == 0) {
            String log = GL20.glGetProgramInfoLog(this.programId, 1024);
            System.err.println("Outline shader program linking failed: " + log);
            this.programId = -1;
         } else {
            System.out.println("Outline shader loaded successfully.");
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
               System.err.println("Outline shader compilation failed (" + filename + "): " + log);
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
}
