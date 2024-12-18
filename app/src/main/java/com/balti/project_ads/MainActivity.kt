package com.balti.project_ads


import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.balti.project_ads.backend.ApiCalls
import com.balti.project_ads.backend.shared
import com.balti.project_ads.databinding.ActivityMainBinding
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

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

        //save bindhome
        data.bindHome = bindHome

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
    fun get_schdules(deviceId: String) {
        apiCalls.getSchedulesByDeviceId(deviceId) { schedules ->
            if (schedules != null) {
                // we got schedules
                if(schedules.isEmpty()){
                    //see if there are new ads
                    data.showMedia(this, "",null)
                }else{
                    //there are some schedules:
                    //1) schedule them
                    for(s in schedules){
                        //get the media type from the ad
                        if(s!=null){
                            //get ad info (because we need the mediaType of that ad)

                            scheduleAd(s.ad_id!!,"image",s.start!!,s.end!!)
                        }
                    }

                    //2) save ads in the storage for offline consulting (if the ads not already saved)
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

    // functionality ---------------------------------------------
    private fun scheduleAd(adId: String,mediaType:String, startTime: Date, endTime: Date) {
        // Calculate delay in milliseconds
        val currentTime = System.currentTimeMillis()
        Log.d("ActionWorker", "Scheduled for: ${startTime.time}")
        Log.d("ActionWorker", "Current time: ${currentTime}")

        val still_valid = (endTime.time - currentTime) > 0
        val delay_to_start: Long = startTime.time - currentTime
        val delay_to_end: Long = endTime.time - currentTime

        // Check if the delay is positive (i.e., scheduled time is in the future)
        if (still_valid) {
            //save when the ad will start
            add_to_worked(adId,mediaType,delay_to_start)
            //save when the ad will end
            add_to_worked(adId,"",delay_to_end)
        } else {
            // If the scheduled time has already passed, do nothing
            Log.d("ActionWorker", "Scheduled time has already passed. Action not scheduled.")
        }
    }
    fun add_to_worked(adId: String,mediaType_:String,delay:Long){
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
        WorkManager.getInstance(this).enqueue(workRequest)
        Log.d("ActionWorker", "Work scheduled successfully.")
    }

}


