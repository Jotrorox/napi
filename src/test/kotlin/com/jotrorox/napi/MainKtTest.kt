package com.jotrorox.napi

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime
import java.time.Month

class MainKtTest {

    @BeforeEach
    fun setUp() {}

    @AfterEach
    fun tearDown() {}

    @Test
    fun `getCurrentTime should return current time in correct format`() {
        val result = getCurrentTime()

        // Check if the result matches the expected format
        assertTrue(result.matches(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")))
    }

    @Test
    fun `getCurrentTime should return different times for subsequent calls`() {
        val result1 = getCurrentTime()
        Thread.sleep(1000)  // Wait for a second
        val result2 = getCurrentTime()

        assertNotEquals(result1, result2)
    }

    @Test
    fun `getDateFromString should return correct LocalDateTime for valid date string`() {
        val dateString = "2022-03-01T12:30:00Z"
        val expectedDateTime = LocalDateTime.of(2022, Month.MARCH, 1, 12, 30)

        val result = getDateFromString(dateString)

        assertEquals(expectedDateTime, result)
    }

    @Test
    fun `getDateFromString should handle leap year correctly`() {
        val dateString = "2024-02-29T12:30:00Z"
        val expectedDateTime = LocalDateTime.of(2024, Month.FEBRUARY, 29, 12, 30)

        val result = getDateFromString(dateString)

        assertEquals(expectedDateTime, result)
    }

    @Test
    fun `getDateFromString should handle end of year correctly`() {
        val dateString = "2022-12-31T23:59:59Z"
        val expectedDateTime = LocalDateTime.of(2022, Month.DECEMBER, 31, 23, 59, 59)

        val result = getDateFromString(dateString)

        assertEquals(expectedDateTime, result)
    }
}