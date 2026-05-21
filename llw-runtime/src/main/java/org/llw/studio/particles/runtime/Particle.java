package org.llw.studio.particles.runtime;

/**
 * Single simulated particle (mutable, pooled).
 */
public final class Particle {
    static final int MAX_TRAIL_POINTS = 16;

    public float x;
    public float y;
    public float vx;
    public float vy;
    public float life;
    public float maxLife;
    public float size;
    public float r = 1f;
    public float g = 1f;
    public float b = 1f;
    public float a = 1f;
    public float rotation;
    public float frameTime;
    public int frameIndex;
    public int randomSeed;
    public boolean alive;
    public final float[] trailX = new float[MAX_TRAIL_POINTS];
    public final float[] trailY = new float[MAX_TRAIL_POINTS];
    public int trailCount;
    public float trailDistance;

    public void reset() {
        alive = false;
        life = 0f;
        maxLife = 0f;
        trailCount = 0;
        trailDistance = 0f;
        frameTime = 0f;
        frameIndex = 0;
    }
}
