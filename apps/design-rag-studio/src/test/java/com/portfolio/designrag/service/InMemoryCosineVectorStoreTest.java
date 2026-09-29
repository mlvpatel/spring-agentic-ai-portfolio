package com.portfolio.designrag.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryCosineVectorStoreTest {

    @Test
    void emptyStoreReturnsNoHits() {
        InMemoryCosineVectorStore store = new InMemoryCosineVectorStore(new HashingTextEmbedder(64));
        assertThat(store.search("primary brand color", 5)).isEmpty();
    }

    @Test
    void matchingQueryReturnsKnownSnippetWithRealCosine() {
        InMemoryCosineVectorStore store = new InMemoryCosineVectorStore(new HashingTextEmbedder(64));
        store.add(new TokenDoc("color.primary", "#0B1F33", "brand navy"));
        store.add(new TokenDoc("space.md", "16px", "rhythm"));
        store.add(new TokenDoc("font.body", "Inter", "type"));

        List<InMemoryCosineVectorStore.ScoredToken> hits = store.search("primary brand navy", 2);
        assertThat(hits).isNotEmpty();
        assertThat(hits.get(0).doc().name()).isEqualTo("color.primary");
        assertThat(hits.get(0).score()).isGreaterThan(hits.get(hits.size() - 1).score());
        // Self-similarity of identical text is 1.0 for L2-normalized vectors
        float[] a = new HashingTextEmbedder(64).embed("color.primary #0B1F33 brand navy");
        assertThat(InMemoryCosineVectorStore.cosine(a, a)).isCloseTo(1.0, org.assertj.core.data.Offset.offset(1e-5));
    }
}
