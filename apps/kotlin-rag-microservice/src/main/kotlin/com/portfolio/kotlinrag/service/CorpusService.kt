package com.portfolio.kotlinrag.service

import org.springframework.stereotype.Service
import java.util.concurrent.CopyOnWriteArrayList

@Service
class CorpusService {
    private val docs = CopyOnWriteArrayList<Pair<String, String>>()

    fun ingest(title: String, text: String): Map<String, Any> {
        require(title.isNotBlank() && text.isNotBlank()) { "title and text required" }
        docs += title.trim() to text.trim()
        return mapOf("mode" to "offline", "size" to docs.size, "title" to title.trim())
    }

    /** Test helper: wipe in-memory corpus between cases. */
    fun clear() {
        docs.clear()
    }

    fun query(q: String): Map<String, Any> {
        require(q.isNotBlank()) { "q required" }
        if (docs.isEmpty()) {
            return mapOf(
                "mode" to "offline",
                "refused" to true,
                "reason" to "No corpus ingested; refuse empty retrieval.",
                "q" to q.trim(),
                "hits" to emptyList<Map<String, String>>()
            )
        }
        val needle = q.lowercase()
        val hits = docs.filter { (t, body) ->
            t.lowercase().contains(needle) || body.lowercase().contains(needle)
        }.take(5).map { (t, body) -> mapOf("title" to t, "snippet" to body.take(120)) }
        val resolved = hits.ifEmpty {
            docs.take(1).map { (t, body) -> mapOf("title" to t, "snippet" to body.take(120)) }
        }
        return mapOf("mode" to "offline", "q" to q.trim(), "hits" to resolved, "refused" to false)
    }
}
