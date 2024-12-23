package com.balti.project_ads

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.net.Uri
import android.os.CountDownTimer
import android.util.Log
import android.view.TextureView
import android.view.View
import android.widget.Toast
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.balti.project_ads.backend.ApiCalls
import com.balti.project_ads.backend.models.AdGroupItem
import com.balti.project_ads.databinding.ActivityMainBinding
import com.balti.project_ads.workers.ActionWorker
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
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
        private var playbackJob: Job? = null

        //schedule group is a group of ads will be shown in order in a loop
        var ads_group:ArrayList<AdGroupItem> = ArrayList()

        //deal with media
        fun initializeExoPlayer(c:Context) {
            // Initialize the player
            player = ExoPlayer.Builder(c).build()
        }
        /*
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
         */

        suspend fun downloadFileFromUrl(mediaUrl: String): ByteArray? {
            return withContext(Dispatchers.IO) { // Perform the network I/O in the IO thread
                try {
                    Log.d("storage", "Downloading started for url: $mediaUrl")

                    // Open connection to the URL
                    val url = URL(mediaUrl)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connect()

                    // Check response code to ensure successful connection
                    if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                        Log.d("storage", "Failed to connect: ${connection.responseCode}")
                        return@withContext null
                    }

                    // Create input stream from the URL
                    val inputStream: InputStream = connection.inputStream

                    // Read all data from the input stream
                    val result = inputStream.readBytes()
                    inputStream.close()

                    Log.d("storage", "Downloading ended for url: $mediaUrl")
                    return@withContext result
                } catch (e: Exception) {
                    Log.d("storage", "Error while downloading: ", e)
                    return@withContext null
                }
            }
        }
        suspend fun saveFileToStorage(context: Context, mediaName: String, fileData: ByteArray): Boolean {
            return withContext(Dispatchers.IO) { // Perform the file I/O in the IO thread
                try {
                    Log.d("storage", "Saving file started for name: $mediaName")

                    // Check if the file already exists
                    val file = getFileByName(context, mediaName)
                    if (file != null) {
                        Log.d("storage", "File already exists: ${file.absolutePath}")
                        return@withContext true
                    }

                    // Get internal storage directory
                    val internalStorage = context.filesDir
                    val mediaFile = File(internalStorage, mediaName)

                    // Write the data to the file
                    mediaFile.outputStream().use { outputStream ->
                        outputStream.write(fileData)
                    }

                    Log.d("storage", "Saving file ended, path: ${mediaFile.absolutePath}")
                    return@withContext true
                } catch (e: Exception) {
                    Log.d("storage", "Error while saving file: ", e)
                    return@withContext false
                }
            }
        }



        fun showMedia(context: Context, mediaType: String, mediaFile: File?,in_group:Boolean) {
            if(in_group){
                //add this ad to the ads_group
                ads_group.add(AdGroupItem(mediaType,mediaFile))
                Log.d("media_player", "ads group: "+ ads_group)

                playAdsSequentially(context, ads_group)
            }
            else{
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
                            // Initialize ExoPlayer
                            val player: ExoPlayer = ExoPlayer.Builder(context).build()
                            // Apply 90-degree rotation using a Matrix
                            val textureView = bindHome.mediaVideo.videoSurfaceView as? TextureView
                            textureView?.post {
                                val matrix = Matrix()
                                val viewWidth = textureView.width.toFloat()
                                val viewHeight = textureView.height.toFloat()

                                // Rotate around the center
                                matrix.postRotate(90f, viewWidth / 2, viewHeight / 2)

                                // Scale to fill the PlayerView
                                matrix.postScale(viewHeight / viewWidth, viewWidth / viewHeight, viewWidth / 2, viewHeight / 2)

                                textureView.setTransform(matrix)
                            }
                            bindHome.mediaVideo.player = player
                            bindHome.mediaVideo.useController = false

                            val mediaItem = MediaItem.fromUri(Uri.fromFile(mediaFile))
                            val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, "exo"))
                            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                                .createMediaSource(mediaItem)

                            // Apply rotation
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
                }
                else{
                    // Handle unsupported media type
                    bindHome.noMedia.visibility = View.VISIBLE
                }
            }
        }
        fun playAdsSequentially(context:Context,adsGroup: List<AdGroupItem>) {
            try{
                    // Cancel any ongoing playback
                    playbackJob?.cancel()
                    // Launch a new coroutine for playback
                    playbackJob =  CoroutineScope(Dispatchers.Main).launch {
                        for (ad in adsGroup) {
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

                            when (ad.type.lowercase()) {
                                "image" -> {
                                    Log.d("media_player", "image will play")
                                    bindHome.mediaImage.visibility = View.VISIBLE
                                    Glide.with(context)
                                        .load(Uri.fromFile(ad.mediaFile))
                                        .into(bindHome.mediaImage)

                                    // Wait for 10 seconds before hiding the image
                                    delay(10000)
                                    bindHome.mediaImage.visibility = View.GONE
                                }
                                "video" -> {
                                    Log.d("media_player", "video will play")
                                    bindHome.mediaVideo.visibility = View.VISIBLE
                                    player = ExoPlayer.Builder(context).build()
                                    bindHome.mediaVideo.player = player
                                    bindHome.mediaVideo.useController = false

                                    val mediaItem = MediaItem.fromUri(Uri.fromFile(ad.mediaFile))
                                    val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, "exo"))
                                    val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)

                                    player.setMediaSource(mediaSource)
                                    player.prepare()
                                    player.play()

                                    // Suspend until video playback completes
                                    suspendCancellableCoroutine { continuation ->
                                        player.addListener(object : Player.Listener {
                                            override fun onPlaybackStateChanged(state: Int) {
                                                if (state == Player.STATE_ENDED) {
                                                    continuation.resume(Unit) {}
                                                }
                                            }
                                        })
                                    }

                                    bindHome.mediaVideo.visibility = View.GONE
                                    player.release()
                                }
                                "music" -> {
                                    Log.d("media_player", "audio will play")
                                    bindHome.mediaAudio.visibility = View.VISIBLE
                                    bindHome.mediaAudio.playAnimation()

                                    player = ExoPlayer.Builder(context).build()
                                    val mediaItem = MediaItem.fromUri(Uri.fromFile(ad.mediaFile))
                                    val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, "exo"))
                                    val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)

                                    player.setMediaSource(mediaSource)
                                    player.prepare()
                                    player.play()

                                    // Suspend until music playback completes
                                    suspendCancellableCoroutine { continuation ->
                                        player.addListener(object : Player.Listener {
                                            override fun onPlaybackStateChanged(state: Int) {
                                                if (state == Player.STATE_ENDED) {
                                                    continuation.resume(Unit) {}
                                                }
                                            }
                                        })
                                    }

                                    bindHome.mediaAudio.visibility = View.GONE
                                    bindHome.mediaAudio.cancelAnimation()
                                    player.release()
                                }
                                else -> {
                                    Log.d("media_player", "unsupported media type")
                                    bindHome.noMedia.visibility = View.VISIBLE
                                    delay(2000) // Wait for 2 seconds for unsupported media
                                    bindHome.noMedia.visibility = View.GONE
                                }
                            }
                        }

                        Log.d("media_player", "done playing all ads")
                        if(ads_group.isNotEmpty()){
                            playAdsSequentially(context,adsGroup)
                        }
                    }
                }
            catch (e:Exception){ Log.e("media_player", "playAdsSequentially: "+e.message.toString()) }
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
        @OptIn(DelicateCoroutinesApi::class)
        fun get_schdules(c:Context, deviceId: String) {
            //first thing : show there is no ads for this moment
            ads_group.clear() //clear the ads group
            showMedia(c, "",null,false)
            //now schedule ads (if they exist)
            apiCalls.getSchedulesByDeviceId(deviceId) { schedules ->
                if (schedules != null) {
                    // we got schedules
                    if (schedules.isEmpty()) {
                        //see if there are new ads
                        showMedia(c, "", null,false)
                    } else {
                        //there are some schedules:
                        //1) schedule them
                        for (s in schedules) {
                            if (s != null) {
                                val ids = s.ad_id!!.split(";")
                                val inGroup = s.ad_id!!.contains(";")
                                for (id in ids) {
                                    Log.d("storage", "id: $id")
                                    //1) get the media type from the ad
                                    apiCalls.getMediaTypeFromAd(id) { type ->
                                        if(type!=null) {
                                            //2) save media in the storage for offline consulting (if that ads media not already saved)
                                            //each media saved in the storage with the name equals to ad_id
                                            GlobalScope.launch {
                                                //1) check if that file already exist or not
                                                val file = getFileByName(c,id)
                                                if(file == null) {
                                                    val result = downloadFileFromUrl(url + "media/" + id)
                                                    if (result == null) {
                                                        Log.d("storage", "download failed")
                                                        //start again the download
                                                        withContext(Dispatchers.Main) {
                                                            show_offline_if_unable_to_load_schedules(c)
                                                        }
                                                    } else {
                                                        Log.d("storage", "downloaded successfully")
                                                        saveFileToStorage(c, id, result)
                                                        //now when we got the ad : start scheduling it
                                                        scheduleAd(c, id, type, s.start!!, s.end!!, inGroup)
                                                    }
                                                }else{
                                                    //file already downloaded
                                                    scheduleAd(c, id, type, s.start!!, s.end!!, inGroup)
                                                }
                                            }
                                        }else{
                                            //type is null
                                        }
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
        fun scheduleAd(c:Context,adId: String, mediaType:String, startTime: Date, endTime: Date, inGroup:Boolean) {
            // Calculate delay in milliseconds
            val currentTime = System.currentTimeMillis()
            Log.d("ActionWorker", "ad type: ${mediaType}")

            val one_houre = 3600000L

            val delay_to_start: Long = (startTime.time/*+one_houre*/) - currentTime
            val delay_to_end: Long = (endTime.time/*+one_houre*/) - currentTime

            Log.d("ActionWorker", "current time : $currentTime")
            val still_valid = delay_to_end > 0

            // Check if the delay is positive (i.e., scheduled time is in the future)
            if (still_valid) {
                //save when the ad will start
                add_to_worked(c,adId,mediaType,delay_to_start,inGroup)
                //save when the ad will end
                add_to_worked(c,adId,"",delay_to_end,inGroup)
            } else {
                // If the scheduled time has already passed, do nothing
                Log.d("ActionWorker", "Scheduled time has already passed. Action not scheduled.")
            }
        }
        fun add_to_worked(c:Context,adId: String,mediaType_:String,delay:Long,inGroup:Boolean){
            // Prepare input data for the Worker
            val inputData: Data = Data.Builder()
                .putString("id", adId)
                .putString("mediaType", mediaType_)
                .putBoolean("inGroup", inGroup)
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

            bindHome.offlineContainer.visibility = View.VISIBLE
            bindHome.timer.text = "10"
            // Create a 10-second timer
            val timer = object : CountDownTimer(10000, 1000) { // 10 seconds, ticks every 1 second
                override fun onTick(millisUntilFinished: Long) {
                    bindHome.timer.text = (millisUntilFinished/1000).toString()
                }

                override fun onFinish() {
                    get_schdules(c, deviceId)
                }
            }
            timer.start()
        }
    }
}