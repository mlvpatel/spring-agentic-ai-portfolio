package com.portfolio.kotlinrag.service

import com.portfolio.shared.ai.PortfolioAiClient
import com.portfolio.shared.ai.PortfolioEmbeddingClient
import com.portfolio.shared.rag.HashingTextEmbedder
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.util.concurrent.CopyOnWriteArrayList

@Service
class CorpusService(
    private val portfolioAiClient: PortfolioAiClient,
    private val environment: Environment,
    private val embeddings: PortfolioEmbeddingClient,
) {
    private fun mode(): String = when {
        portfolioAiClient.isLive -> "live"
        vectorRetrieval() -> "vector-offline"
        else -> "offline"
    }

    private fun vectorRetrieval(): Boolean =
        environment.activeProfiles.contains("pgvector")
            || environment.getProperty("kotlin.rag.vector-retrieval", "false").toBoolean()

    private data class Doc(val title: String, val text: String, val vector: FloatArray)

    private val docs = CopyOnWriteArrayList<Doc>()

    fun ingest(title: String, text: String): Map<String, Any> {
        require(title.isNotBlank() && text.isNotBlank()) { "title and text required" }
        val t = title.trim()
        val body = text.trim()
        docs += Doc(t, body, embeddings.embed("$t $body"))
        return mapOf("mode" to mode(), "size" to docs.size, "title" to t)
    }

    /** Test helper: wipe in-memory corpus between cases. */
    fun clear() {
        docs.clear()
    }

    fun query(q: String): Map<String, Any> {
        require(q.isNotBlank()) { "q required" }
        if (docs.isEmpty()) {
            return mapOf(
                "mode" to mode(),
                "refused" to true,
                "reason" to "No corpus ingested; refuse empty retrieval.",
                "q" to q.trim(),
                "hits" to emptyList<Map<String, String>>()
            )
        }
        val hits = if (vectorRetrieval()) {
            cosineHits(q.trim())
        } else {
            substringHits(q.trim())
        }
        return mapOf(
            "mode" to mode(),
            "q" to q.trim(),
            "hits" to hits,
            "refused" to false,
            "aiNote" to portfolioAiClient.assist("query-note", q.trim()),
        )
    }

    private fun substringHits(q: String): List<Map<String, String>> {
        val needle = q.lowercase()
        val hits = docs.filter { d ->
            d.title.lowercase().contains(needle) || d.text.lowercase().contains(needle)
        }.take(5).map { d -> mapOf("title" to d.title, "snippet" to d.text.take(120)) }
        return hits.ifEmpty {
            docs.take(1).map { d -> mapOf("title" to d.title, "snippet" to d.text.take(120)) }
        }
    }

    private fun cosineHits(q: String): List<Map<String, Any>> {
        val qv = embeddings.embed(q)
        return docs
            .map { d -> Triple(d, HashingTextEmbedder.dotProduct(qv, d.vector), d.text.take(120)) }
            .sortedByDescending { it.second }
            .take(5)
            .map { (d, score, snippet) ->
                mapOf("title" to d.title, "snippet" to snippet, "score" to score)
            }
    }
}
