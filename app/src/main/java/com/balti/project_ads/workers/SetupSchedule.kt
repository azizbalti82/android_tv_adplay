package com.balti.project_ads.workers

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.balti.project_ads.data

class FetchSchedules(val c: Context, workerParams: WorkerParameters) : Worker(c, workerParams) {
    override fun doWork(): Result {
        // Make sure to run the UI updates on the main thread
        Handler(Looper.getMainLooper()).post {
            try {
                Log.e("worker_fetch_schedules", "Started fetching schedules media (at:${System.currentTimeMillis()})")
                data.getSchdules(c,data.deviceId)
            } catch (e: Exception) {
                Log.e("worker_fetch_schedules", "Error fetching schedules media", e)
            }
        }
        return Result.success()
    }
}
