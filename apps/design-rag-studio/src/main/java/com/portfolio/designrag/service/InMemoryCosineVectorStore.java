package com.portfolio.designrag.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory vector index with real cosine similarity (dot product of L2-normalized vectors).
 * Used under the {@code pgvector} profile so unit tests need no Postgres/Docker image.
 * Compose {@code --profile pgvector} (or {@code postgres}/{@code prod}) still points JDBC at Postgres.
 */
public final class InMemoryCosineVectorStore {

    private final HashingTextEmbedder embedder;
    private final CopyOnWriteArrayList<Entry> entries = new CopyOnWriteArrayList<>();

    public InMemoryCosineVectorStore(HashingTextEmbedder embedder) {
        this.embedder = embedder;
    }

    public void clear() {
        entries.clear();
    }

    public void add(TokenDoc doc) {
        String text = doc.name() + " " + doc.value() + " " + doc.notes();
        entries.add(new Entry(doc, embedder.embed(text)));
    }

    public int size() {
        return entries.size();
    }

    /**
     * Rank by descending cosine similarity. Returns empty when the store is empty.
     * Cosine is the normalized dot product; scores are not invented.
     */
    public List<ScoredToken> search(String query, int limit) {
        if (entries.isEmpty() || limit <= 0) {
            return List.of();
        }
        float[] q = embedder.embed(query == null ? "" : query);
        List<ScoredToken> scored = new ArrayList<>(entries.size());
        for (Entry e : entries) {
            scored.add(new ScoredToken(e.doc(), cosine(q, e.vector())));
        }
        scored.sort(Comparator.comparingDouble(ScoredToken::score).reversed());
        if (scored.size() <= limit) {
            return List.copyOf(scored);
        }
        return List.copyOf(scored.subList(0, limit));
    }

    static double cosine(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("vector length mismatch");
        }
        double dot = 0;
        for (int i = 0; i < a.length; i++) {
            dot += (double) a[i] * b[i];
        }
        return dot;
    }

    public record ScoredToken(TokenDoc doc, double score) {
    }

    private record Entry(TokenDoc doc, float[] vector) {
    }
}
