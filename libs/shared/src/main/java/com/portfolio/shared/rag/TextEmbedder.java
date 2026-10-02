package com.portfolio.shared.rag;

/** Text to a float vector. Callers must use one implementation for both ingest and query. */
public interface TextEmbedder {

    float[] embed(String text);
}
