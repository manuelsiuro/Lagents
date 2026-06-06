package com.msa.lagents.domain.knowledge

import java.io.InputStream

interface TextExtractor {
    fun extract(inputStream: InputStream): String
}

class PlainTextExtractor : TextExtractor {
    override fun extract(inputStream: InputStream): String {
        return inputStream.bufferedReader().use { it.readText() }
    }
}
