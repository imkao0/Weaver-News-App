package dev.mkao.weaver.domain.model

data class Edition(
    val code: String,
    val name: String,
    val nativeName: String,
    val abbreviations: List<String>,
)
