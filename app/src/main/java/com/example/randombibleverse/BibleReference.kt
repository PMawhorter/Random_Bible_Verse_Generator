package com.example.randombibleverse

data class BibleReference(val book: String, val chapter: Int, val verse: Int) {
    override fun toString(): String = "$book $chapter:$verse"
}
