package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.HostAccess;

import java.util.function.Supplier;

/**
 * Play-mode {@link org.llw.studio.ecs.World} API: RGBA color accessor for component bindings.
 */
public final class ColorBinding {
    private final Supplier<float[]> reader;
    private final ColorWriter writer;

    ColorBinding(Supplier<float[]> reader, ColorWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    /**
     * @return red channel in {@code [0, 1]}
     */
    @HostAccess.Export
    public double getR() {
        return reader.get()[0];
    }

    /**
     * @param value red channel in {@code [0, 1]}
     */
    @HostAccess.Export
    public void setR(double value) {
        float[] current = reader.get();
        writer.write((float) value, current[1], current[2], current[3]);
    }

    /**
     * @return green channel in {@code [0, 1]}
     */
    @HostAccess.Export
    public double getG() {
        return reader.get()[1];
    }

    /**
     * @param value green channel in {@code [0, 1]}
     */
    @HostAccess.Export
    public void setG(double value) {
        float[] current = reader.get();
        writer.write(current[0], (float) value, current[2], current[3]);
    }

    /**
     * @return blue channel in {@code [0, 1]}
     */
    @HostAccess.Export
    public double getB() {
        return reader.get()[2];
    }

    /**
     * @param value blue channel in {@code [0, 1]}
     */
    @HostAccess.Export
    public void setB(double value) {
        float[] current = reader.get();
        writer.write(current[0], current[1], (float) value, current[3]);
    }

    /**
     * @return alpha channel in {@code [0, 1]}
     */
    @HostAccess.Export
    public double getA() {
        return reader.get()[3];
    }

    /**
     * @param value alpha channel in {@code [0, 1]}
     */
    @HostAccess.Export
    public void setA(double value) {
        float[] current = reader.get();
        writer.write(current[0], current[1], current[2], (float) value);
    }

    @FunctionalInterface
    interface ColorWriter {
        void write(float r, float g, float b, float a);
    }
}
