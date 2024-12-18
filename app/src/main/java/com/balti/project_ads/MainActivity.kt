package com.balti.project_ads

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.balti.project_ads.backend.ApiCalls
import com.balti.project_ads.backend.shared
import com.balti.project_ads.databinding.ActivityMainBinding
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player

class MainActivity : FragmentActivity() {
    private lateinit var bindHome: ActivityMainBinding
    private lateinit var deviceId: String
    private lateinit var apiCalls:ApiCalls
    private val TAG = "MainActivity"

    override fun onBackPressed() {
        // Prevent the app from closing
        Log.d("MainActivity", "Back button pressed. Action prevented.")
        // Optionally, you can show a toast or a confirmation dialog here
    }

    override fun onPause() {
        super.onPause()
        //update connectivity status
        set_connectivity(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindHome = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindHome.root)


        //listeners
        bindHome.refrech.setOnClickListener {
            get_schdules(deviceId)
        }

//        val root: View = bindHome.root
//        // Get screen dimensions
//        val displayMetrics = resources.displayMetrics
//        val height = displayMetrics.heightPixels
//        val width = displayMetrics.widthPixels
//        // Swap width and height
//        root.layoutParams = root.layoutParams.apply {
//            this.height = width
//            this.width = height
//        }
//        // Set the pivot to the center of the screen
//        root.pivotX = (height/1.125).toFloat()
//        root.pivotY = (height/1.125).toFloat()
//        // Apply rotation
//        root.rotation = 90f
//        // Ensure the layout is centered
//        root.requestLayout()


        // Logic for device connection
        apiCalls = ApiCalls()
        deviceId = shared.get_id(this)

        //start the app
        setupApp()
    }


    private fun setupApp(){
        //start loading animation
        show_loading()
        apiCalls.isDeviceConnected(deviceId) { connected ->
            if (connected /*replace true with 'connected'*/) {
                setupHome()
            }
            else {
                //device not added to server
                //check if there is a temp device in android
                if(deviceId!=""){
                    //there is a temp device, check if its still valid in server
                    get_device_temp()
                }
                else{
                    //if its not valid (expired or deleted): generate a new temp device
                    create_device()
                }
            }
        }
    }
    private fun setupHome() {
        //update connectivity status to online
        set_connectivity(true)

        //if device added to server
        show_home_section()


        //get schedule of this device
        get_schdules(deviceId)



        //if there are new ads, download their media


        //start the scheduling of the ads with WorkManager




//        val urlImageString = "https://pics.craiyon.com/2023-09-28/84df15a1a9ca4520b32c3631d097ecd2.webp"
//        val urlAudioString = "https://www.sousound.com/music/healing/healing_01.mp3"
//        val urlVideoString = "https://tekeye.uk/html/images/Joren_Falls_Izu_Jap.mp4"
//        val uri = Uri.parse(urlAudioString)
//
          //showMedia(this, "music", uri)
    }

    //api functions ---------------------------------------------
    fun create_device() {
        apiCalls.createTempDevice { message, device ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

            if (device != null) {
                // Device céreation successful
                shared.save_id(this, device.id)
                deviceId = device.id
                show_connect_section(device.id)
            } else {
                // Handle error message
                Log.e("error_server", message)
                show_offline_section("create_temp_device")
            }
        }
    }
    fun get_device_temp() {
        apiCalls.getTempDevice(deviceId) { message, device ->
            if (device != null) {
                // Device exist in the server
                show_connect_section(deviceId)
            } else {
                // Device does not exist in the server
                create_device()
                // Handle error message
                Log.e("API_ERROR", message)
            }
        }
    }
    fun set_connectivity(connected: Boolean) {
        apiCalls.getDevice(deviceId) { device ->
            //Toast.makeText(this, device.toString(), Toast.LENGTH_SHORT).show()
            if (device != null) {
                device.name?.let { Log.d("error_", it) }
                if(connected){
                    device.status = "online"

                }else{
                    device.status = "offline"
                }

                //update device in server
                device.id?.let {
                    apiCalls.updateDevice(it,device) { message, device ->
                        if (device == null) {
                            // Device update successful
                            Log.d("error", message)
                        }
                    }
                }
            }
        }
    }
    fun showMedia(context: Context, mediaType: String, uri: Uri?) {
        //first hide all media views
        bindHome.mediaAudio.cancelAnimation()
        bindHome.mediaImage.visibility = View.GONE
        bindHome.mediaAudio.visibility = View.GONE
        bindHome.mediaVideo.visibility = View.GONE

        when (mediaType.lowercase()) {
            "image" -> {
                bindHome.mediaImage.visibility = View.VISIBLE
                // Show image
                Glide.with(context)
                    .load(uri) // URI of the image
                    .into(bindHome.mediaImage)

            }
            "video" -> {
                //show the exoplayer view
                bindHome.mediaVideo.visibility = View.VISIBLE
                // Play audio using ExoPlayer

                val player = ExoPlayer.Builder(context).build()
                bindHome.mediaVideo.player = player

                // Disable controls (ensure that 'useController' is false)
                bindHome.mediaVideo.useController = false

                val mediaItem = uri?.let { MediaItem.fromUri(it) }
                if (mediaItem != null) {
                    player.setMediaItem(mediaItem)
                }

                // Set repeat mode (repeat the media)
                player.repeatMode = Player.REPEAT_MODE_ONE
                player.prepare()
                player.play()

            }
            "music" -> {
                //show the exoplayer view
                bindHome.mediaAudio.visibility = View.VISIBLE
                bindHome.mediaAudio.playAnimation()
                // Play audio using ExoPlayer
                val player = ExoPlayer.Builder(context).build()
                val mediaItem = uri?.let { MediaItem.fromUri(it) }

                if (mediaItem != null) {
                    player.setMediaItem(mediaItem)
                    player.repeatMode = Player.REPEAT_MODE_ONE
                    bindHome.mediaVideo.useController = false
                    player.prepare()
                    player.play()
                }

            }
            else -> {
                //show empty page
                bindHome.noMedia.visibility = View.VISIBLE
            }
        }
    }
    fun get_schdules(deviceId: String) {
        apiCalls.getSchedulesByDeviceId(deviceId) { schedules ->
            if (schedules != null) {
                // we got schedules
                if(schedules.isEmpty()){
                    //see if there are new ads
                    showMedia(this, "",null)
                }else{
                    //there are some schedules:
                    //1) get their ads

                    //2) save ads in the storage for offline consulting (if the ads not already saved)


                    //testttt
                    val urlstring = "http://192.168.1.122:3000/ads/media/904205"
                    val uri = Uri.parse(urlstring)
                    showMedia(this, "image", uri)
                }
            } else {
                // there is no schedules
                Toast.makeText(this, "Error while loading schedules", Toast.LENGTH_SHORT).show()

            }
        }
    }


    //ui functions ----------------------------------------------
    fun show_offline_section(message: String){
        bindHome.loading.visibility = View.GONE
        bindHome.containerConnect.visibility = View.GONE
        bindHome.offlineContainer.visibility = View.VISIBLE
        bindHome.containerHome.visibility = View.GONE

        bindHome.tryAgain.setOnClickListener {
            show_loading()
            apiCalls.isDeviceConnected(deviceId) { connected ->
                if (connected) {
                    setupHome()
                } else {
                    if(message=="create_temp_device"){
                        create_device()
                    }
                }
            }
        }
    }
    fun show_connect_section(id: String) {
        bindHome.codeText.text = id
        bindHome.loading.visibility = View.GONE
        bindHome.containerConnect.visibility = View.VISIBLE
        bindHome.offlineContainer.visibility = View.GONE
        bindHome.containerHome.visibility = View.GONE

        bindHome.continueBtn.setOnClickListener {
            bindHome.containerConnect.visibility = View.GONE
            setupApp()
        }
    }
    fun show_home_section(){
        bindHome.loading.visibility = View.GONE
        bindHome.containerConnect.visibility = View.GONE
        bindHome.offlineContainer.visibility = View.GONE
        bindHome.containerHome.visibility = View.VISIBLE
    }
    fun show_loading(){
        bindHome.loading.visibility = View.VISIBLE
        bindHome.containerConnect.visibility = View.GONE
        bindHome.offlineContainer.visibility = View.GONE
        bindHome.containerHome.visibility = View.GONE
    }
}


