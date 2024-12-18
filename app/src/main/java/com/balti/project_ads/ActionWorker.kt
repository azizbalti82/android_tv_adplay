package com.balti.project_ads

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class ActionWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // Retrieve data passed to the Worker
        val adId = inputData.getString("id") ?: return Result.failure()
        val mediaType = inputData.getString("mediaType") ?: return Result.failure()

        Log.d("ActionWorker", "ad id executed: $adId with media type: $mediaType")

        // Start the ad or hide it depending on the media type
        val urlString = data.url + "/ads/media/" + adId
        val uri = Uri.parse(urlString)

        // Make sure to run the UI updates on the main thread
        Handler(Looper.getMainLooper()).post {
            try {
                // Assuming showMedia updates the UI, so it should be done on the main thread
                data.showMedia(applicationContext, mediaType, uri)
            } catch (e: Exception) {
                Log.e("ActionWorker", "Error showing media", e)
            }
        }

        return Result.success()
    }
}
