package com.submithub.app.util

object CodeGenerator {
    private const val ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"

    fun generate(length: Int = 6): String =
        (1..length).map { ALPHABET.random() }.joinToString("")
}
