package com.balti.project_ads

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.balti.project_ads.backend.ApiCalls
import com.balti.project_ads.databinding.ActivityMainBinding
import com.balti.project_ads.workers.ActionWorker
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Date
import java.util.concurrent.TimeUnit

class data {
    @SuppressLint("StaticFieldLeak")
    companion object{
        val url = "https://adplayforandroidtv-production-13eb.up.railway.app/"
        lateinit var bindHome: ActivityMainBinding
        lateinit var deviceId: String
        lateinit var apiCalls: ApiCalls
        var connected = false
        //for exo player
        private lateinit var player: ExoPlayer

        fun initializeExoPlayer(c:Context) {
            // Initialize the player
            player = ExoPlayer.Builder(c).build()
        }

        //deal with media
        suspend fun downloadMedia(context: Context, mediaUrl: String, mediaName: String): Boolean {
            return withContext(Dispatchers.IO) {  // Perform the network and file I/O in the IO thread
                try {
                    Log.d("storage", "Downloading started for name: $mediaName, url: $mediaUrl")

                    //1) check if that file already exist or not
                    val file = getFileByName(context,mediaName)
                    if(file == null){
                        // Get internal storage directory
                        val internalStorage = context.filesDir
                        val mediaFile = File(internalStorage, mediaName)
                        Log.d("storage", "Internal storage directory: ${internalStorage.absolutePath}")

                        // Open connection to the URL
                        val url = URL(mediaUrl)
                        val connection = url.openConnection() as HttpURLConnection
                        connection.connect()

                        // Check response code to ensure successful connection
                        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                            Log.d("storage", "Failed to connect: ${connection.responseCode}")
                            return@withContext false
                        }

                        // Create input stream from the URL
                        val inputStream: InputStream = connection.inputStream

                        // Create output stream to write to internal storage
                        val outputStream: OutputStream = mediaFile.outputStream()

                        // Buffer to read and write data
                        val buffer = ByteArray(1024)
                        var bytesRead: Int

                        // Read data and write to output stream
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                        }

                        // Close streams
                        inputStream.close()
                        outputStream.close()

                        Log.d("storage", "Downloading ended, path: ${mediaFile.absolutePath}")
                    }else{
                        Log.d("storage", "File already downloaded")
                    }
                    return@withContext true
                } catch (e: Exception) {
                    Log.d("storage", "Error while downloading: ", e)
                    return@withContext false
                }
            }
        }
        fun showMedia(context: Context, mediaType: String, mediaFile: File?) {
            // Stop and release the player if it already exists
            if (::player.isInitialized) {
                player.stop()
                player.release()
            }

            // Reinitialize the Player
            player = ExoPlayer.Builder(context).build()

            // show the home
            showHome()
            // Hide all media views initially
            bindHome.mediaAudio.cancelAnimation()
            bindHome.mediaImage.visibility = View.GONE
            bindHome.mediaAudio.visibility = View.GONE
            bindHome.mediaVideo.visibility = View.GONE
            bindHome.noMedia.visibility = View.GONE

            if (mediaFile != null) {
                when (mediaType.lowercase()) {
                    "image" -> {
                        bindHome.mediaImage.visibility = View.VISIBLE
                        // Load the image into Glide from external storage
                        Glide.with(context)
                            .load(Uri.fromFile(mediaFile))
                            .into(bindHome.mediaImage)
                    }
                    "video" -> {
                        bindHome.mediaVideo.visibility = View.VISIBLE
                        // Initialize ExoPlayer to play video from external storage
                        val player: ExoPlayer = ExoPlayer.Builder(context).build()
                        bindHome.mediaVideo.player = player
                        bindHome.mediaVideo.useController = false

                        val mediaItem = MediaItem.fromUri(Uri.fromFile(mediaFile))
                        val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, "exo"))
                        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(mediaItem)

                        player.repeatMode = Player.REPEAT_MODE_ONE
                        player.setMediaSource(mediaSource)
                        player.prepare()
                        player.play()
                    }
                    "music" -> {
                        // Show the audio view
                        bindHome.mediaAudio.visibility = View.VISIBLE
                        bindHome.mediaAudio.playAnimation()

                        // Play audio using ExoPlayer
                        val mediaItem = MediaItem.fromUri(Uri.fromFile(mediaFile))
                        val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, "exo"))
                        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(mediaItem)

                        player.setMediaSource(mediaSource)
                        player.prepare()
                        player.repeatMode = Player.REPEAT_MODE_ONE
                        player.play()
                    }
                    else -> {
                        // Handle unsupported media type
                        bindHome.noMedia.visibility = View.VISIBLE
                    }
                }
            }else{
                // Handle unsupported media type
                bindHome.noMedia.visibility = View.VISIBLE
            }
        }
        fun getFileByName(context: Context, fileName: String): File? {
            // Get the internal storage directory
            val internalStorage = context.filesDir
            val file = File(internalStorage, fileName)

            // Check if the file exists
            return if (file.exists()) {
                file
            } else {
                null
            }
        }

        //use this to schedule an ad (save it to worker)
        fun get_schdules(c:Context,deviceId: String) {
            //first thing : show there is no ads for this moment
            showMedia(c, "",null)
            //now schedule ads (if they exist)
            apiCalls.getSchedulesByDeviceId(deviceId) { schedules ->
                if (schedules != null) {
                    // we got schedules
                    if(schedules.isEmpty()){
                        //see if there are new ads
                        showMedia(c, "",null)
                    }else{
                        //there are some schedules:
                        //1) schedule them
                        for(s in schedules){
                            if(s!=null){
                                //1) get the media type from the ad
                                apiCalls.getMediaTypeFromAd(s.ad_id!!) { type ->
                                    if (type != null) {
                                        //2) save media in the storage for offline consulting (if that ads media not already saved)
                                        //each media saved in the storage with the name equals to ad_id
                                        GlobalScope.launch {
                                            val result = downloadMedia(c,data.url + "media/" + s.ad_id,s.ad_id.toString())
                                            if(result){
                                                //now when we got the ad : start scheduling it
                                                scheduleAd(c,s.ad_id!!,type,s.start!!,s.end!!)
                                                Log.d("storage", "downloaded successfully")
                                            }else{
                                                Log.d("storage", "download Error")
                                            }
                                        }
                                    } else {
                                        // unable to extract that media type
                                    }
                                }
                            }
                        }
                    }
                }
                else {
                    show_offline_if_unable_to_load_schedules(c)
                }
            }
        }
        fun scheduleAd(c:Context,adId: String, mediaType:String, startTime: Date, endTime: Date) {
            // Calculate delay in milliseconds
            val currentTime = System.currentTimeMillis()
            Log.d("ActionWorker", "ad type: ${mediaType}")

            val one_houre = 3600000L

            val delay_to_start: Long = (startTime.time/*+one_houre*/) - currentTime
            val delay_to_end: Long = (endTime.time/*+one_houre*/) - currentTime

            val still_valid = delay_to_end > 0

            // Check if the delay is positive (i.e., scheduled time is in the future)
            if (still_valid) {
                //save when the ad will start
                add_to_worked(c,adId,mediaType,delay_to_start)
                //save when the ad will end
                add_to_worked(c,adId,"",delay_to_end)
            } else {
                // If the scheduled time has already passed, do nothing
                Log.d("ActionWorker", "Scheduled time has already passed. Action not scheduled.")
            }
        }
        fun add_to_worked(c:Context,adId: String,mediaType_:String,delay:Long){
            // Prepare input data for the Worker
            val inputData: Data = Data.Builder()
                .putString("id", adId)
                .putString("mediaType", mediaType_)
                .build()

            // Create a OneTimeWorkRequest with a delay
            val workRequest: OneTimeWorkRequest = OneTimeWorkRequest.Builder(ActionWorker::class.java)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build()

            // Schedule the work
            WorkManager.getInstance(c).enqueue(workRequest)
            Log.d("ActionWorker", "Work scheduled successfully.")
        }

        //ui functions ----------------------------------------------
        fun showHome() {
            // Reset visibility for all sections
            bindHome.loading.visibility = View.GONE
            bindHome.containerConnect.visibility = View.GONE
            bindHome.offlineContainer.visibility = View.GONE
            bindHome.containerHome.visibility = View.VISIBLE
        }
        fun show_offline_if_unable_to_load_schedules(c:Context){
            bindHome.loading.visibility = View.GONE
            bindHome.offlineContainer.visibility = View.VISIBLE
            bindHome.containerHome.visibility = View.GONE
            bindHome.containerConnect.visibility = View.GONE

            bindHome.tryAgain.setOnClickListener {
                get_schdules(c, deviceId)
            }
        }
    }
}