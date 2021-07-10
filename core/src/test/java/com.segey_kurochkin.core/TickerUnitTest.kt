package com.segey_kurochkin.core


import com.sergey_kurochkin.core.Ticker
import kotlinx.coroutines.*
import kotlinx.coroutines.withTimeout
import org.junit.Test

import org.junit.Assert.*


class TickerUnitTest {

    @Test
    fun `call action 4 times`() {

        runBlocking {
            var count = 0
            val action : () -> Unit = {
                count++
            }
            Ticker.stop()

            Ticker.start(action)

            launch {
                delay(3000L)
                Ticker.stop()
                assertEquals(4, count)
            }
        }

    }
}