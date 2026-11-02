/* Spektrafilm Android — local reference history. GPLv3. */
package com.spectrafilm.app

internal fun rememberFilmSelection(previous: List<String>, id: String): List<String> =
    if (id.isBlank()) previous else (listOf(id) + previous.filter { it != id }).take(12)

