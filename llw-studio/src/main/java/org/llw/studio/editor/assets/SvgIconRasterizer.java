package org.llw.studio.editor.assets;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.LoaderContext;
import com.github.weisj.jsvg.parser.SVGLoader;
import com.github.weisj.jsvg.parser.resources.ResourcePolicy;
import com.github.weisj.jsvg.view.ViewBox;
import org.llw.render.graphics.Texture2d;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Rasterizes SVG icon bytes to GPU textures for ImGui.
 */
public final class SvgIconRasterizer {
    private static final SVGLoader LOADER = new SVGLoader();
    private static final LoaderContext CONTEXT = LoaderContext.builder()
            .externalResourcePolicy(ResourcePolicy.DENY_EXTERNAL)
            .build();

    private SvgIconRasterizer() {
    }

    /**
     * @param svgBytes encoded SVG
     * @param size     square output size in pixels
     * @return RGBA texture uploaded to OpenGL
     */
    public static Texture2d rasterizeToTexture(byte[] svgBytes, int size) throws IOException {
        BufferedImage image = rasterizeToImage(svgBytes, size);
        return uploadRgba(image);
    }

    /**
     * @param svgBytes encoded SVG
     * @param size     square output size in pixels
     * @return ARGB image with icon centered and scaled to fit
     */
    public static BufferedImage rasterizeToImage(byte[] svgBytes, int size) throws IOException {
        if (svgBytes == null || svgBytes.length == 0) {
            throw new IOException("SVG bytes are empty");
        }
        try (ByteArrayInputStream in = new ByteArrayInputStream(svgBytes)) {
            SVGDocument document = LOADER.load(in, null, CONTEXT);
            if (document == null) {
                throw new IOException("Failed to parse SVG icon");
            }
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            try {
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                document.render(null, graphics, new ViewBox(size, size));
            } finally {
                graphics.dispose();
            }
            return image;
        }
    }

    static Texture2d uploadRgba(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        ByteBuffer rgba = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder());
        int[] row = new int[width];
        for (int y = height - 1; y >= 0; y--) {
            image.getRGB(0, y, width, 1, row, 0, width);
            for (int pixel : row) {
                rgba.put((byte) ((pixel >> 16) & 0xFF));
                rgba.put((byte) ((pixel >> 8) & 0xFF));
                rgba.put((byte) (pixel & 0xFF));
                rgba.put((byte) ((pixel >> 24) & 0xFF));
            }
        }
        rgba.flip();
        return Texture2d.fromRgbaPixels(width, height, rgba);
    }

    /** Simple flat fallback when remote icons are unavailable. */
    public static Texture2d flatFallback(int size, int rgb) {
        ByteBuffer rgba = ByteBuffer.allocateDirect(size * size * 4).order(ByteOrder.nativeOrder());
        byte r = (byte) ((rgb >> 16) & 0xFF);
        byte g = (byte) ((rgb >> 8) & 0xFF);
        byte b = (byte) (rgb & 0xFF);
        for (int i = 0; i < size * size; i++) {
            rgba.put(r).put(g).put(b).put((byte) 255);
        }
        rgba.flip();
        return Texture2d.fromRgbaPixels(size, size, rgba);
    }
}
