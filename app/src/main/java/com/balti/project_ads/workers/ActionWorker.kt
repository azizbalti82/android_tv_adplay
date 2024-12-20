package com.balti.project_ads.workers

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.balti.project_ads.data

class ActionWorker(val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        // Retrieve data passed to the Worker
        val adId = inputData.getString("id") ?: return Result.failure()
        val mediaType = inputData.getString("mediaType") ?: return Result.failure()

        Log.d("ActionWorker", "ad id executed: $adId with media type: $mediaType")

        // Make sure to run the UI updates on the main thread
        Handler(Looper.getMainLooper()).post {
            try {
                // Assuming showMedia updates the UI, so it should be done on the main thread
                val file = data.getFileByName(context, adId)
                data.showMedia(applicationContext, mediaType, file)
            } catch (e: Exception) {
                Log.e("ActionWorker", "Error showing media", e)
            }
        }

        return Result.success()
    }
}
class FetchSchedules(val c: Context, workerParams: WorkerParameters) : Worker(c, workerParams) {
    override fun doWork(): Result {
        // Task to perform every 12 hours
        Log.d("MyPeriodicWorker", "Task executed at: ${System.currentTimeMillis()}")
        // Make sure to run the UI updates on the main thread
        Handler(Looper.getMainLooper()).post {
            try {
                Log.e("MyPeriodicWorker", "fetching schedules media")
                data.get_schdules(c,data.deviceId)
            } catch (e: Exception) {
                Log.e("MyPeriodicWorker", "Error fetching schedules media", e)
            }
        }

        // Return success
        return Result.success()
    }
}
