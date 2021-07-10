package com.sergey_kurochkin.core

import java.util.*
import kotlin.concurrent.timer

object Ticker {
    private var mTimer: Timer? = null

    fun start(action: () -> Unit) {
        mTimer?.cancel()
        mTimer = timer(name = "PomodoroTicker", initialDelay = 1000L, daemon = false, period = 1000L) {
            action()
        }
    }

    fun stop() {
        mTimer?.cancel()
    }
}