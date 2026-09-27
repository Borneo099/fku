package fku.org.example.fku.features.skija;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.humbleui.skija.Bitmap;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.ColorAlphaType;
import io.github.humbleui.skija.Image;
import io.github.humbleui.skija.ImageInfo;
import io.github.humbleui.skija.Surface;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;

/**
 * Skija 渲染核心（离屏纹理路线）
 *
 * ★ 集成方式：Skija 离屏 Surface 渲染 → 导出 Image → 读取像素 → 上传为 Minecraft 纹理 → GuiGraphics.blit 贴回。
 *   完全不碰 Minecraft 的 GL 状态（RenderSystem），兼容 OptiFine / Iris。
 * ★ 原生库：由 skija-windows-x64 依赖在运行时经 Library.load 提取，无需手动处理。
 * ★ 回退：任意一步失败都置 available=false，调用方回退到原版渲染。
 *
 * 注：当前 Maven Central 仅有 skija 0.143.x，该版本缺失 types 模块（RRect/Rect/PaintStyle/Path 构造），
 * 故阶段1仅用 drawCircle/Shader（色轮），圆角/文字/灵动岛（需 RRect/Path/FontCollection）待补齐依赖后实现。
 */
public class SkijaRenderer {
    private static final Logger LOGGER = LogManager.getLogger("FKU-Skija");
    private static boolean available = false;

    /** 离屏绘制回调 */
    public interface SkijaDraw {
        void draw(Canvas canvas);
    }

    public static boolean isAvailable() {
        return available;
    }

    /** 运行时若发现 Skija 类不可用（如启动配置未包含 Skija 依赖），立即禁用并回退原版渲染 */
    public static void markUnavailable() {
        if (available) {
            available = false;
            LOGGER.warn("[FKU-Skija] 运行时检测到 Skija 类不可用，已禁用并回退原版渲染");
        }
    }

    /** 客户端启动时调用一次；失败则安静回退。
     *  ★ 已回退：用户不再使用 Skija 渲染，这里直接置为不可用，所有调用方回退原版
     *    （GUI 圆角/文字/灵动岛 走原版，色轮走 ColorWheelPicker.drawFullColorWheel 原版逐像素）。
     *    保留下方原生加载代码以备将来重新启用。 */
    public static void init() {
        available = false;
    }

    /**
     * 将 draw 绘制到一张离屏纹理并返回。失败返回 null。
     * 纹理所有权归调用方，不再使用时请 close()。
     */
    public static DynamicTexture createDynamicTexture(int w, int h, SkijaDraw draw) {
        if (!available) return null;
        try {
            // UNPREMUL：导出为「直alpha」字节，匹配 Minecraft GUI 的 SRC_ALPHA 混合；
            // 不透明像素(色轮)与 PREMUL 等价，半透明面板不会被压暗。
            ImageInfo info = ImageInfo.makeS32(w, h, ColorAlphaType.UNPREMUL);
            Surface surface = Surface.makeRaster(info);
            Canvas canvas = surface.getCanvas();
            canvas.clear(0);
            draw.draw(canvas);
            Image image = surface.makeImageSnapshot();
            Bitmap bitmap = new Bitmap();
            bitmap.allocPixels(info);
            if (!image.readPixels(bitmap, 0, 0)) {
                surface.close();
                return null;
            }
            // Skija 0.143.17：Bitmap.readPixels() 直接返回 R,G,B,A 字节数组
            byte[] pixels = bitmap.readPixels();
            ByteBuffer buf = ByteBuffer.wrap(pixels);
            NativeImage ni = new NativeImage(w, h, false);
            // Skija 缓冲为 R,G,B,A 字节序；NativeImage.setPixelRGBA 以小端写入，
            // 故内存字节为 [r,g,b,a]，与 GL_RGBA 一致：int = (a<<24)|(b<<16)|(g<<8)|r
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int r = buf.get() & 0xFF;
                    int g = buf.get() & 0xFF;
                    int b = buf.get() & 0xFF;
                    int a = buf.get() & 0xFF;
                    ni.setPixelRGBA(x, y, (a << 24) | (b << 16) | (g << 8) | r);
                }
            }
            DynamicTexture tex = new DynamicTexture(ni);
            tex.upload();
            // 线性过滤：配合超采样(4x)渲染，blit 缩小时边缘/文字平滑而非像素锯齿
            tex.setFilter(true, false);
            surface.close();
            return tex;
        } catch (Throwable t) {
            LOGGER.warn("[FKU-Skija] 纹理创建失败: " + t.getMessage());
            return null;
        }
    }
}
