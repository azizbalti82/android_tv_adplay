package com.balti.project_ads

import android.content.Context
import android.graphics.Matrix
import android.net.Uri
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.TextureView
import android.view.View
import android.widget.Toast
import com.balti.project_ads.backend.AdGroupItem
import com.balti.project_ads.backend.ApiCalls
import com.balti.project_ads.databinding.ActivityMainBinding
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
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class data {
    companion object{
        val url = "https://adplayforandroidtv-production-13eb.up.railway.app/"
        val TAG = "data_management"
        lateinit var deviceId: String
        lateinit var apiCalls: ApiCalls
        var connected = false
        //for exo player
        private lateinit var player: ExoPlayer
        private var playbackJob: Job? = null
        lateinit var bindHome: ActivityMainBinding

        // 'ads_group' is a group of ads will be shown in order in a loop
        var ads_group:ArrayList<AdGroupItem> = ArrayList()


        // download functions ----------------------------------------------------------------------
        suspend fun downloadFile(mediaUrl: String): ByteArray? {
            return withContext(Dispatchers.IO) {
                try {
                    Log.d(TAG, "downloadFile: Downloading started for url: $mediaUrl")

                    // Open connection to the URL
                    val url = URL(mediaUrl)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connect()

                    // Check response code
                    when (connection.responseCode) {
                        HttpURLConnection.HTTP_OK -> {
                            // If response code is 200, proceed to download
                            val inputStream: InputStream = connection.inputStream
                            val result = inputStream.readBytes()
                            inputStream.close()
                            Log.d(TAG, "downloadFile: Downloading ended for url: $mediaUrl")
                            return@withContext result
                        }
                        HttpURLConnection.HTTP_NOT_FOUND -> {
                            // If response code is 404, return empty byte array
                            Log.d(TAG, "downloadFile: There is no media file, check the server")
                            return@withContext ByteArray(0)
                        }
                        else -> {
                            // Handle other response codes
                            Log.d(TAG, "downloadFile: Server returned code: ${connection.responseCode}")
                        }
                    }
                    return@withContext null
                } catch (e: Exception) {
                    Log.d(TAG, "downloadFile: Error while downloading: ", e)
                    return@withContext null
                }
            }
        }
        suspend fun saveFileToStorage(context: Context, mediaName: String, fileData: ByteArray): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    Log.d(TAG, "saveFile: Saving file started for name: $mediaName")

                    // Check if the file already exists
                    val file = getFile(context, mediaName)
                    if (file != null) {
                        Log.d(TAG, "saveFile: File already exists: ${file.absolutePath}")
                        return@withContext true
                    }

                    // Get internal storage directory
                    val internalStorage = context.filesDir
                    val mediaFile = File(internalStorage, mediaName)

                    // Write the data to the file
                    mediaFile.outputStream().use { outputStream ->
                        outputStream.write(fileData)
                    }

                    Log.d(TAG, "saveFile: Saving file ended, path: ${mediaFile.absolutePath}")
                    return@withContext true
                } catch (e: Exception) {
                    Log.d(TAG, "saveFile: Error while saving file: ", e)
                    return@withContext false
                }
            }
        }
        fun getFile(context: Context, fileName: String): File? {
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

        // show ads functions ----------------------------------------------------------------------
        fun showMedia(context: Context, mediaType: String, mediaFile: File?,in_group:Boolean) {
            if(in_group){
                //add this ad to the ads_group
                ads_group.add(AdGroupItem(mediaType,mediaFile))

                Log.d("media_player", "GROUP: "+ ads_group.toString())
                playAdsSequentially(context, ads_group)
            }
            else{
                Log.d("media_player", "showMedia: this is a single ad: ${mediaFile?.absolutePath}")
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
                bindHome.mediaInvalid.visibility = View.GONE
                bindHome.noMedia.visibility = View.GONE

                if (mediaFile != null && mediaType.isNotEmpty()) {
                    when (mediaType.lowercase()) {
                        "image" -> {
                            Log.d("media_player", "(not in group) image will play ad_ID: ${mediaFile.name}")
                            bindHome.mediaImage.visibility = View.VISIBLE
                            // Load the image into Glide from external storage
                            Glide.with(context)
                                .load(Uri.fromFile(mediaFile))
                                .into(bindHome.mediaImage)
                        }
                        "video" -> {
                            Log.d("media_player", "(not in group) video will play ad_ID: ${mediaFile.name}")
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
                            Log.d("media_player", "(not in group) audio will play ad_ID: ${mediaFile.name}")
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
                        "invalid" ->{
                            Log.d("media_player", "(not in group) invalid will play ad_ID: ${mediaFile.name}")
                            bindHome.mediaInvalid.visibility = View.VISIBLE
                        }
                        else -> {
                            // Handle unsupported media type
                            bindHome.noMedia.visibility = View.VISIBLE
                        }
                    }
                }
                else{
                    Log.d("media_player", "(not in group) empty section will be displayed")
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
                            bindHome.mediaInvalid.visibility = View.GONE
                            bindHome.noMedia.visibility = View.GONE

                            when (ad.type.lowercase()) {
                                "image" -> {
                                    Log.d("media_player", "(in group) image will play ad_ID: ${ad.mediaFile?.name}")
                                    bindHome.mediaImage.visibility = View.VISIBLE
                                    Glide.with(context)
                                        .load(Uri.fromFile(ad.mediaFile))
                                        .into(bindHome.mediaImage)

                                    // Wait for 10 seconds before hiding the image
                                    delay(10000)
                                    bindHome.mediaImage.visibility = View.GONE
                                }
                                "video" -> {
                                    Log.d("media_player", "(in group) video will play ad_ID: ${ad.mediaFile?.name}")
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
                                    Log.d("media_player", "(in group) audio will play ad_ID: ${ad.mediaFile?.name}")
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
                                "invalid" ->{
                                    Log.d("media_player", "(in group) invalid will play ad_ID: ${ad.mediaFile?.name}")
                                    bindHome.mediaInvalid.visibility = View.VISIBLE
                                    delay(10000)
                                    bindHome.mediaInvalid.visibility = View.GONE
                                }
                                else -> {
                                    bindHome.noMedia.visibility = View.VISIBLE
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

        // schedules management functions ----------------------------------------------------------
        @OptIn(DelicateCoroutinesApi::class)
        fun getSchdules(c:Context, deviceId: String) {
            Log.d(TAG, "getSchdules: started getting schedules for deviceID: $deviceId")
            ads_group.clear() //clear the ads group

            apiCalls.getSchedulesByDeviceId(deviceId) { schedules ->
                if (schedules != null) {
                    // we got schedules
                    if (schedules.isEmpty()) {
                        Log.d(TAG, "getSchdules: there is no schedules")
                        showMedia(c, "", null,false)
                    } else {
                        Log.d(TAG, "getSchdules: there is schedules, lets save them")
                        //show users the first schedule
                        try {
                            val earliestSchedule = schedules.minByOrNull { it?.start ?: Date(Long.MAX_VALUE) }
                            Toast.makeText(c, "Current time: ${formatDate(null,isCurrent = true)}", Toast.LENGTH_LONG).show()
                            Toast.makeText(c, "date of first schedule: ${formatDate(earliestSchedule?.start.toString())}", Toast.LENGTH_LONG).show()
                            Log.d(TAG, "getSchdules: Your first schedule will start in: ${formatDate(earliestSchedule?.start.toString())}")
                        }catch (e:Exception){
                            Log.d(TAG, "getSchdules: error while getting the first schedule")
                        }


                        //there are some schedules:
                        //1) schedule them
                        for (s in schedules) {
                            if (s != null) {
                                val ids = s.ad_id!!.split(";")
                                val inGroup = s.ad_id!!.contains(";")
                                Log.d(TAG, "getSchdules: total of ads in this schedule: ${ids.size}")
                                for (id in ids) {
                                    //1) get the media type from the ad
                                    apiCalls.getMediaTypeFromAd(id) { type ->
                                        Log.d(TAG, "getSchdules: Extracted ad is 'id': $id, 'type': $type")
                                        if(type!=null) {
                                            //2) save media in the storage (if that ads media not already saved)
                                            GlobalScope.launch {
                                                //1) check if that file already exist or not
                                                val file = getFile(c,id)
                                                if(file == null) {
                                                    val result = downloadFile(url + "media/" + id)
                                                    if (result == null) {
                                                        Log.d(TAG, "getSchdules: download failed")
                                                        //start again the download
                                                        withContext(Dispatchers.Main) {
                                                            show_offline_if_unable_to_load_schedules(c)
                                                        }
                                                    }
                                                    else {
                                                        //we got response from server
                                                        if(result.isEmpty()){
                                                            Log.d(TAG, "getSchdules: invalid media ID:$id")
                                                            scheduleAd(c, id, "invalid", s.start!!, s.end!!, inGroup)
                                                        }else{
                                                            Log.d(TAG, "getSchdules: downloaded successfully")
                                                            saveFileToStorage(c, id, result)
                                                            //now when we got the ad : start scheduling it
                                                            scheduleAd(c, id, type, s.start!!, s.end!!, inGroup)
                                                        }
                                                    }
                                                }
                                                else{
                                                    //file already downloaded
                                                    Log.d(TAG, "getSchdules: File already downloaded id:$id")
                                                    scheduleAd(c, id, type, s.start!!, s.end!!, inGroup)
                                                }
                                            }
                                        }
                                    }
                                }
                            } else{
                                Log.d(TAG, "getSchdules: schedule is null")
                            }
                        }
                        showMedia(c, "", null,false)
                    }
                }
                else {
                    Log.e(TAG, "getSchdules: Error while fetching schedules")
                    show_offline_if_unable_to_load_schedules(c)
                }
            }
        }
        fun scheduleAd(c:Context,adId: String, mediaType:String, startTime: Date, endTime: Date, inGroup:Boolean) {
            Log.d("worker_setup_schedule", "scheduleAd: started scheduling ad: $adId, type: $mediaType, inGroup: $inGroup")

            val handler = Handler(Looper.getMainLooper())
            val hour = 3600000
            val currentTime = System.currentTimeMillis()
            val delay_to_start: Long = (startTime.time-hour) - currentTime
            val delay_to_end: Long = (endTime.time-hour) - currentTime

            if(delay_to_end>0){
                //when the media will start playing
                handler.postDelayed({
                    val file = getFile(c, adId)
                    Log.d("worker_setup_schedule", "${adId} started playing: $adId with media type: $mediaType ,ingroup:$inGroup file:${file?.absolutePath}")
                    showMedia(c.applicationContext, mediaType, file,inGroup)
                }, delay_to_start)

                //when the media is done playing
                handler.postDelayed({
                    showMedia(c.applicationContext, "", null,false)
                }, delay_to_end)
            } else {
                // If the scheduled time has already passed, do nothing
                Log.d("worker_setup_schedule", "scheduleAd: Scheduled time has already passed. Action not scheduled.")
            }
        }



        // ui functions ----------------------------------------------------------------------------
        fun showHome() {
            // Reset visibility for all sections
            bindHome.loading.visibility = View.GONE
            bindHome.containerConnect.visibility = View.GONE
            bindHome.containerOffline.visibility = View.GONE
            bindHome.containerHome.visibility = View.VISIBLE
        }
        fun show_offline_if_unable_to_load_schedules(c:Context){
            bindHome.loading.visibility = View.GONE
            bindHome.containerHome.visibility = View.GONE
            bindHome.containerConnect.visibility = View.GONE
            bindHome.containerOffline.visibility = View.VISIBLE

            bindHome.timer.text = "10"
            // Create a 10-second timer
            val timer = object : CountDownTimer(10000, 1000) { // 10 seconds, ticks every 1 second
                override fun onTick(millisUntilFinished: Long) {
                    bindHome.timer.text = (millisUntilFinished/1000).toString()
                }
                override fun onFinish() {
                    getSchdules(c, deviceId)
                }
            }
            timer.start()
        }
        fun formatDate(dateString: String?, isCurrent: Boolean=false): String? {
            return if (isCurrent) {
                // Return the current date and time if isCurrent is true
                val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH) // Without seconds
                outputFormat.format(Date())
            } else {
                try {
                    val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
                    val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH) // Without seconds

                    val date = inputFormat.parse(dateString)
                    if (date == null) return null // Explicit null check if parsing fails

                    // Subtract one hour from the parsed date
                    val calendar = Calendar.getInstance()
                    calendar.time = date
                    calendar.add(Calendar.HOUR_OF_DAY, -1) // Subtract 1 hour because of timezone

                    // Format the modified date
                    outputFormat.format(calendar.time)
                } catch (e: Exception) {
                    null // Return null if parsing fails
                }
            }
        }
    }
}