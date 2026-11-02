package com.spectrafilm.app
import org.junit.Assert.*
import org.junit.Test
class FilmReferenceHistoryTest {
 @Test fun recentSelectionMovesToFrontWithoutDuplicatesAndCapsAtTwelve() {
  val many = (1..15).map { "film$it" }
  assertEquals(listOf("film5") + (1..12).filter { it != 5 }.map { "film$it" }, rememberFilmSelection(many, "film5"))
 }
 @Test fun emptyIdsCannotEnterHistory() { assertEquals(listOf("a"), rememberFilmSelection(listOf("a"), "")) }
}
