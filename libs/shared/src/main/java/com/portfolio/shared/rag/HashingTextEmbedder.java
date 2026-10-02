package com.portfolio.shared.rag;

/**
 * Deterministic bag-of-hashed-tokens embedder. No network; used for offline
 * retrieval tests and local cosine ranking. Vectors are L2-normalized.
 */
public final class HashingTextEmbedder implements TextEmbedder {

    private final int dimensions;

    public HashingTextEmbedder(int dimensions) {
        if (dimensions < 8) {
            throw new IllegalArgumentException("dimensions must be >= 8");
        }
        this.dimensions = dimensions;
    }

    public float[] embed(String text) {
        float[] v = new float[dimensions];
        if (text == null || text.isBlank()) {
            return v;
        }
        for (String token : text.toLowerCase().split("[^a-z0-9.#]+")) {
            if (token.isEmpty()) {
                continue;
            }
            int h = token.hashCode();
            int idx = Math.floorMod(h, dimensions);
            v[idx] += 1.0f;
            int idx2 = Math.floorMod(h >>> 16, dimensions);
            v[idx2] += 0.5f;
        }
        return l2Normalize(v);
    }

    /** Dot product. Cosine of two L2-normalized vectors is this value. */
    public static double dotProduct(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("vector length mismatch");
        }
        double dot = 0;
        for (int i = 0; i < a.length; i++) {
            dot += (double) a[i] * b[i];
        }
        return dot;
    }

    private static float[] l2Normalize(float[] v) {
        double sum = 0;
        for (float x : v) {
            sum += (double) x * x;
        }
        if (sum == 0) {
            return v;
        }
        float inv = (float) (1.0 / Math.sqrt(sum));
        for (int i = 0; i < v.length; i++) {
            v[i] *= inv;
        }
        return v;
    }
}
