package com.portfolio.kotlinrag.web

import com.portfolio.kotlinrag.service.CorpusService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class RagController(private val corpus: CorpusService) {
    @PostMapping("/ingest")
    fun ingest(@RequestBody body: Map<String, String>): ResponseEntity<Map<String, Any>> {
        val title = body["title"].orEmpty()
        val text = body["text"].orEmpty()
        return ResponseEntity.ok(corpus.ingest(title, text))
    }

    @PostMapping("/query")
    fun query(@RequestBody body: Map<String, String>): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(corpus.query(body["q"].orEmpty()))
    }
}
