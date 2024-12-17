package com.balti.project_ads

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class check_connectivity {

    private val scheduler = Executors.newSingleThreadScheduledExecutor()

    fun startTask() {
        // Schedule a task to run every 2 minutes (120,000 milliseconds)
        scheduler.scheduleWithFixedDelay({
            // Task to perform every 2 minutes
            check()
        }, 0, 2, TimeUnit.MINUTES)
    }

    private fun check() {
        // Code to execute every 2 minutes
        println("Doing something!")
    }

    fun stopTask() {
        scheduler.shutdown()
    }
}
