package com.balti.project_ads

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.balti.project_ads.backend.ApiCalls
import com.balti.project_ads.backend.models.Status
import com.balti.project_ads.backend.shared
import com.balti.project_ads.databinding.ActivityMainBinding
import com.balti.project_ads.workers.FetchSchedules
import java.util.concurrent.TimeUnit


class MainActivity : FragmentActivity() {
    private lateinit var bindHome: ActivityMainBinding
    private val TAG = "MainActivity"

    override fun onDestroy() {
        super.onDestroy()
        if(data.connected){
            set_connectivity(false)
        }
    }
    override fun onBackPressed() {
        // Prevent the app from closing
        Log.d("MainActivity", "Back button pressed. Action prevented.")
        // Optionally, you can show a toast or a confirmation dialog here
    }

    override fun onPause() {
        super.onPause()
        //update connectivity status
        if(data.connected){
            set_connectivity(false)
        }
    }

    override fun onResume() {
        super.onResume()
        if(data.connected){
            set_connectivity(true)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindHome = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindHome.root)


        //save bindhome
        data.bindHome = bindHome

        //initialise the media player
        data.initializeExoPlayer(this)


        // rotate the screen to portrait ----------------------
        val root: View = bindHome.root
        // Get screen dimensions
        val displayMetrics = resources.displayMetrics
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        // Swap width and height
        root.layoutParams = root.layoutParams.apply {
            this.height = width
            this.width = height
        }
        // Set the pivot to the center of the screen
        root.pivotX = (height/1.125).toFloat()
        root.pivotY = (height/1.125).toFloat()
        // Apply rotation
        root.rotation = 90f
        // Ensure the layout is centered
        root.requestLayout()
        //-----------------------------------------------------


        // Logic for device connection
        data.apiCalls = ApiCalls()
        data.deviceId = shared.get_id(this)


        //start the app
        setupApp()

    }


    private fun setupApp(){
        //start loading animation
        setSection("loading")
        data.apiCalls.isDeviceConnected(data.deviceId) { connected ->
            data.connected = connected
            if (connected) {
                setupHome()
            }
            else {
                //device not added to server
                //check if there is a temp device in android
                if(data.deviceId!=""){
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
        setSection("home")

        //setup a worker to fetch schedules for every 12h
        FetchSchedulesWorker(this)

        //show empty ads until the a new schedule fetching accurs
        //data.bindHome.noMedia.visibility = View.VISIBLE
        data.get_schdules(this,data.deviceId)
    }

    //api functions ---------------------------------------------
    fun create_device() {
        data.apiCalls.createTempDevice { message, device ->
            if (device != null) {
                // Device céreation successful
                shared.save_id(this, device.id)
                data.deviceId = device.id
                setSection("connect", id = data.deviceId)
            } else {
                // Handle error message
                Log.e("error_server", message)
                setSection("offline")
            }
        }
    }
    fun get_device_temp() {
        data.apiCalls.getTempDevice(data.deviceId) { message, device ->
            Log.d("error_server",data.deviceId )
            if (device != null) {
                // Device exist in the server
                setSection("connect", id = data.deviceId)
                Log.d("error_server",data.deviceId )
            } else {
                // Device does not exist in the server
                create_device()
                // Handle error message
                Log.e("API_ERROR", message)
            }
        }
    }
    fun set_connectivity(connected: Boolean) {
        data.apiCalls.getDevice(data.deviceId) { device ->
            //Toast.makeText(this, device.toString(), Toast.LENGTH_SHORT).show()
            if (device != null) {
                device.Device.name.let { Log.d("error_", it) }
                if(connected){
                    device.Device.status = "online"

                }else{
                    device.Device.status = "offline"
                }
                Log.d("error_server", device.Device.toString())
                //update device in server
                device.Device.id.let { id->
                    val new_device = Status(status = device.Device.status)
                    data.apiCalls.updateDevice(id,new_device) { is_updated ->
                        if (is_updated) {
                            // Device update successful
                            Log.d("error","error while updating status")
                        }
                    }
                }
            }
        }
    }

    //ui functions ----------------------------------------------
    fun setSection(sectionName: String, id: String? = null) {
        // Reset visibility for all sections
        bindHome.loading.visibility = View.GONE
        bindHome.containerConnect.visibility = View.GONE
        bindHome.offlineContainer.visibility = View.GONE
        bindHome.containerHome.visibility = View.GONE

        when (sectionName) {
            "offline" -> {
                bindHome.offlineContainer.visibility = View.VISIBLE
                bindHome.tryAgain.setOnClickListener {
                    setSection("loading")
                    data.apiCalls.isDeviceConnected(data.deviceId) { connected ->
                        data.connected = connected
                        if (connected) {
                            setupHome()
                        } else {
                            create_device()
                        }
                    }
                }
            }
            "connect" -> {
                bindHome.containerConnect.visibility = View.VISIBLE
                if (!id.isNullOrEmpty()) {
                    bindHome.codeText.text = id
                }
                bindHome.continueBtn.setOnClickListener {
                    bindHome.containerConnect.visibility = View.GONE
                    setupApp()
                }
            }
            "home" -> {
                bindHome.containerHome.visibility = View.VISIBLE
            }
            "loading" -> {
                bindHome.loading.visibility = View.VISIBLE
            }
            else -> {
                throw IllegalArgumentException("Invalid section name: $sectionName")
            }
        }
    }

    // functionality ---------------------------------------------
    fun FetchSchedulesWorker(context: Context) {
        //fetch schedules every 12h
        val workRequest = PeriodicWorkRequestBuilder<FetchSchedules>(
            15, TimeUnit.MINUTES // Repeat interval
        ).build()

        // Enqueue the periodic work
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "My12HourTask",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP, // Prevents creating duplicates
            workRequest
        )
    }
}


