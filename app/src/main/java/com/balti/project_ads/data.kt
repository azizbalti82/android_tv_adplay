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
import com.balti.project_ads.backend.AdItem
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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Collections
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class data {
    companion object{
        val url = "https://adplayforandroidtv-production-13eb.up.railway.app/"
        val TAG = "data_management"
        lateinit var deviceId: String
        lateinit var apiCalls: ApiCalls
        var connected = false
        //for exo player
        lateinit var player: ExoPlayer
        var playbackJob: Job? = null
        private val getAdsScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        lateinit var bindHome: ActivityMainBinding
        var refresher_started = false
        // 'ads_group' is a group of ads will be shown in order in a loop
        var ads_group:ArrayList<AdItem> = ArrayList()
        val ads_handler = Handler(Looper.getMainLooper())


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
                            return@withContext ByteArray(0)
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
        fun showMedia(
            context: Context,
            adItem:AdItem,
        ) {
            if(adItem.inGroup){
                Log.i("media_player", "ad:${adItem.mediaFile?.name} is in group, index:${adItem.position}")

                //get only the ads of this current schedule
                val ads = ads_group.filter { it.scheduleId == adItem.scheduleId }
                Log.d("media_player", "scheduleID: ${adItem.scheduleId}, ads in GROUP: "+ ads.toString())
                playAdsSequentially(context, ads)
            }
            else{
                Log.d("media_player", "showMedia: this is a single ad: ${adItem.mediaFile?.absolutePath}")
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

                if (adItem.mediaFile != null && adItem.type.isNotEmpty()) {
                    when (adItem.type.lowercase()) {
                        "image" -> {
                            Log.d("media_player", "(not in group) image will play ad_ID: ${adItem.mediaFile?.name}")
                            bindHome.mediaImage.visibility = View.VISIBLE
                            // Load the image into Glide from external storage
                            Glide.with(context)
                                .load(Uri.fromFile(adItem.mediaFile))
                                .into(bindHome.mediaImage)
                        }
                        "video" -> {
                            Log.d("media_player", "(not in group) video will play ad_ID: ${adItem.mediaFile?.name}")
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

                            val mediaItem = MediaItem.fromUri(Uri.fromFile(adItem.mediaFile))
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
                            Log.d("media_player", "(not in group) audio will play ad_ID: ${adItem.mediaFile?.name}")
                            // Show the audio view
                            bindHome.mediaAudio.visibility = View.VISIBLE
                            bindHome.mediaAudio.playAnimation()

                            // Play audio using ExoPlayer
                            val mediaItem = MediaItem.fromUri(Uri.fromFile(adItem.mediaFile))
                            val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, "exo"))
                            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                                .createMediaSource(mediaItem)

                            player.setMediaSource(mediaSource)
                            player.prepare()
                            player.repeatMode = Player.REPEAT_MODE_ONE
                            player.play()
                        }
                        "invalid" ->{
                            Log.d("media_player", "(not in group) invalid will play ad_ID: ${adItem.mediaFile?.name}")
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
        fun playAdsSequentially(context:Context,adsGroup: List<AdItem>) {
            try{
                    // Cancel any ongoing playback
                    playbackJob?.cancel()
                    // Launch a new coroutine for playback
                    playbackJob =  CoroutineScope(Dispatchers.Main).launch {
                        for (ad in adsGroup) {
                            // Stop and release the player if it already exists
                            withContext(Dispatchers.Main) {
                                if (::player.isInitialized) {
                                    player.stop()
                                    player.release()
                                }
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
            showMedia(c.applicationContext, AdItem("","","",null,0,0,0,false))
            try {
                //show loading screen
                show_loading()
                // Stop and release the player if it already exists
                if (::player.isInitialized) {
                    player.stop()
                    player.release()
                }
                // Cancel any ongoing playback
                playbackJob?.cancel()


                Toast.makeText(c, "Refreshing...", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "getSchdules: started getting schedules for deviceID: $deviceId")
                ads_group.clear() //clear the ads group

                apiCalls.getSchedulesByDeviceId(deviceId) { schedules ->
                    if (schedules != null) {
                        // we got schedules
                        if (schedules.isEmpty()) {
                            Log.d(TAG, "getSchdules: there is no schedules")
                            showMedia(c, adItem = AdItem("", "","", null, 0, 0, 0, false))
                        } else {
                            Log.d(TAG, "getSchdules: there is schedules, lets save them")
                            //there are some schedules:
                            //1) schedule them
                            for (s in schedules) {
                                if (s != null && s.id != null) {
                                    val ids = s.ad_id!!.split(";").toMutableList()
                                    val inGroup = s.ad_id!!.contains(";")
                                    Log.d(
                                        TAG,
                                        "getSchdules: total of ads in this schedule: ${ids.size}:ids: $ids"
                                    )

                                    //2) save media in the storage (if that ads media not already saved)
                                    GlobalScope.launch {
                                        // Define a scope for download jobs
                                        val adsList = Collections.synchronizedList(mutableListOf<AdItem>())
                                        val getAdds = ids.map { id ->
                                            getAdsScope.async(Dispatchers.IO) {
                                                // Wrap the callback with a suspending function
                                                val type = suspendCoroutine { continuation ->
                                                    apiCalls.getMediaTypeFromAd(id) { result ->
                                                        continuation.resume(result)
                                                    }
                                                }

                                                if (type != null) {
                                                    val file = getFile(c, id)
                                                    if (file == null) {
                                                        val result =
                                                            downloadFile(url + "media/" + id)
                                                        if (result == null) {
                                                            Log.d(
                                                                TAG,
                                                                "getSchedules: download failed for id: $id"
                                                            )
                                                            withContext(Dispatchers.Main) {
                                                                show_offline_if_unable_to_load_schedules(
                                                                    c
                                                                )
                                                            }
                                                            return@async
                                                        } else {
                                                            if (result.isEmpty()) {
                                                                Log.d(
                                                                    TAG,
                                                                    "getSchedules: invalid media ID: $id"
                                                                )
                                                                adsList.add(
                                                                    AdItem(
                                                                        id,
                                                                        s.id!!,
                                                                        "invalid",
                                                                        null,
                                                                        s.start!!,
                                                                        s.end!!,
                                                                        ids.indexOf(id),
                                                                        inGroup
                                                                    )
                                                                )
                                                            } else {
                                                                Log.d(
                                                                    TAG,
                                                                    "getSchedules: downloaded successfully for id: $id"
                                                                )
                                                                saveFileToStorage(c, id, result)
                                                                adsList.add(
                                                                    AdItem(
                                                                        id,
                                                                        s.id!!,
                                                                        type,
                                                                        file,
                                                                        s.start!!,
                                                                        s.end!!,
                                                                        ids.indexOf(id),
                                                                        inGroup
                                                                    )
                                                                )
                                                            }
                                                        }
                                                    } else {
                                                        Log.d(TAG, "getSchedules: File already exists for id: $id")
                                                        adsList.add(AdItem(id, s.id!!,type, file, s.start!!, s.end!!, ids.indexOf(id), inGroup))
                                                    }
                                                } else {
                                                    Log.d(TAG, "getSchedules: Media type is null for id: $id")
                                                }
                                            }
                                        }

                                        // ad this schedule ads:
                                        // Wait for all downloads to finish
                                        try {
                                            getAdds.awaitAll()
                                            if (ids.size == adsList.size) {
                                                //this is the final ads list for the current schedule ordered by position
                                                val sortedAdsList = adsList.sortedBy { it.position }
                                                //lets save it
                                                ads_group.addAll(sortedAdsList)
                                                for (i in sortedAdsList) {
                                                    Log.d("got_nigga_ads", i.id)
                                                    scheduleAd(c, i)
                                                }
                                            } else {
                                                withContext(Dispatchers.Main) {
                                                    show_offline_if_unable_to_load_schedules(c)
                                                }
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                show_offline_if_unable_to_load_schedules(c)
                                            }
                                            return@launch
                                        }
                                    }
                                } else {
                                    Log.d(TAG, "getSchdules: schedule is null")
                                }
                            }
                        }
                    } else {
                        Log.e(TAG, "getSchdules: Error while fetching schedules")
                        show_offline_if_unable_to_load_schedules(c)
                    }
                }
            }catch (e:Exception){
                show_offline_if_unable_to_load_schedules(c)
                Log.e(TAG, "error in getSchdules:",e )
            }
        }
        fun scheduleAd(c:Context,adItem: AdItem) {
            Log.d("worker_setup_schedule", "scheduleAd: started scheduling ad: ${adItem.id}, type: ${adItem.type}, inGroup: ${adItem.inGroup}")
            current_date{currentTime ->
                val delay_to_start: Long = adItem.start - currentTime
                val delay_to_end: Long = adItem.end - currentTime
                if(delay_to_end>0){
                    //when the media will start playing
                    ads_handler.postDelayed({
                        Log.d("worker_setup_schedule", "${adItem.id} started playing, media type: ${adItem.type} ,ingroup:${adItem.inGroup} file:${adItem.mediaFile?.absolutePath}")
                        adItem.mediaFile = getFile(c, adItem.id) //re-check the file from storage
                        showMedia(c.applicationContext, adItem)
                    }, delay_to_start)


                    //when the media is done playing
                    ads_handler.postDelayed({
                        showMedia(c.applicationContext, AdItem("","","",null,0,0,0,false))
                    }, delay_to_end)
                }
                else {
                    // If the scheduled time has already passed, do nothing
                    Log.d("worker_setup_schedule", "scheduleAd: Scheduled time has already passed. Action not scheduled.")
                }
            }

        }
        fun scheduleRefresh(c:Context){
            val handler = Handler(Looper.getMainLooper())
            //rescan the schedules after 5mn again
            handler.postDelayed({
                Log.d("worker_refresh", "started fetching schedules (10mn passed)")
                scheduleRefresh(c)
                getSchdules(c, deviceId)
            }, 10 * 60 * 1000)

            refresher_started = true

            //cancel all scheduled ads (uncomment this after testing
            //ads_handler.removeCallbacksAndMessages(null)
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
            // Stop and release the player if it already exists
            if (::player.isInitialized) {
                player.stop()
                player.release()
            }
            // Cancel any ongoing playback
            playbackJob?.cancel()

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
        fun show_loading(){
            bindHome.loading.visibility = View.VISIBLE
            bindHome.containerHome.visibility = View.GONE
            bindHome.containerConnect.visibility = View.GONE
            bindHome.containerOffline.visibility = View.GONE
        }
        fun current_date(callback: (Long) -> Unit){
            apiCalls.getDate {date ->
                if (date != null) {
                    callback(date)
                } else {
                    callback(System.currentTimeMillis())
                }
            }
        }
    }
}