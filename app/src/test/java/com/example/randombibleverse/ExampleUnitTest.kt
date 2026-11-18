package com.example.randombibleverse

import org.junit.Test
import org.junit.Assert.*
import java.util.Calendar

class HistoryUnitTest {
    @Test
    fun testIsWithinLast3Months() {
        val now = System.currentTimeMillis()
        assertTrue("Current timestamp should be within last 3 months", isWithinLast3Months(now))

        val oneMonthAgo = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }.timeInMillis
        assertTrue("1 month ago should be within last 3 months", isWithinLast3Months(oneMonthAgo))

        val twoMonthsAgo = Calendar.getInstance().apply { add(Calendar.MONTH, -2) }.timeInMillis
        assertTrue("2 months ago should be within last 3 months", isWithinLast3Months(twoMonthsAgo))

        val fourMonthsAgo = Calendar.getInstance().apply { add(Calendar.MONTH, -4) }.timeInMillis
        assertFalse("4 months ago should NOT be within last 3 months", isWithinLast3Months(fourMonthsAgo))
    }
}