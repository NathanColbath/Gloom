package org.llw.studio.build;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes Windows {@code .ico} files from raster images.
 */
public final class BuildIconWriter {
    private static final int MAX_ICON_SIZE = 256;

    private BuildIconWriter() {
    }

    /**
     * @param sourceImage readable PNG/JPEG/etc.
     * @param outputIco   destination {@code .ico} path
     * @throws IOException when the image cannot be read or written
     */
    public static void writeIco(Path sourceImage, Path outputIco) throws IOException {
        BufferedImage source = ImageIO.read(sourceImage.toFile());
        if (source == null) {
            throw new IOException("Unsupported or unreadable icon image: " + sourceImage);
        }
        BufferedImage square = scaleToSquare(source, MAX_ICON_SIZE);
        ByteArrayOutputStream pngBytes = new ByteArrayOutputStream();
        if (!ImageIO.write(square, "PNG", pngBytes)) {
            throw new IOException("Failed to encode icon PNG: " + sourceImage);
        }
        Files.createDirectories(outputIco.getParent());
        try (OutputStream out = Files.newOutputStream(outputIco)) {
            writePngEmbeddedIco(out, pngBytes.toByteArray(), square.getWidth(), square.getHeight());
        }
    }

    private static BufferedImage scaleToSquare(BufferedImage source, int maxSize) {
        int size = Math.min(maxSize, Math.max(source.getWidth(), source.getHeight()));
        size = Math.max(1, size);
        int width = source.getWidth();
        int height = source.getHeight();
        float scale = Math.min((float) size / width, (float) size / height);
        int scaledW = Math.max(1, Math.round(width * scale));
        int scaledH = Math.max(1, Math.round(height * scale));
        Image scaled = source.getScaledInstance(scaledW, scaledH, Image.SCALE_SMOOTH);
        BufferedImage canvas = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = canvas.createGraphics();
        graphics.drawImage(scaled, (size - scaledW) / 2, (size - scaledH) / 2, null);
        graphics.dispose();
        return canvas;
    }

    private static void writePngEmbeddedIco(OutputStream out, byte[] png, int width, int height) throws IOException {
        out.write(new byte[]{0, 0, 1, 0, 1, 0});
        out.write(iconDirEntry(width, height, png.length, 6 + 16));
        out.write(png);
    }

    private static byte[] iconDirEntry(int width, int height, int pngLength, int offset) {
        byte[] entry = new byte[16];
        entry[0] = width >= 256 ? 0 : (byte) width;
        entry[1] = height >= 256 ? 0 : (byte) height;
        entry[4] = 1;
        entry[6] = 32;
        entry[8] = (byte) (pngLength & 0xFF);
        entry[9] = (byte) ((pngLength >> 8) & 0xFF);
        entry[10] = (byte) ((pngLength >> 16) & 0xFF);
        entry[11] = (byte) ((pngLength >> 24) & 0xFF);
        entry[12] = (byte) (offset & 0xFF);
        entry[13] = (byte) ((offset >> 8) & 0xFF);
        entry[14] = (byte) ((offset >> 16) & 0xFF);
        entry[15] = (byte) ((offset >> 24) & 0xFF);
        return entry;
    }
}
