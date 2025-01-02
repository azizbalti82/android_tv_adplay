package com.balti.project_ads

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.balti.project_ads.backend.ApiCalls
import com.balti.project_ads.backend.Status
import com.balti.project_ads.data.Companion.playbackJob
import com.balti.project_ads.data.Companion.player
import com.balti.project_ads.databinding.ActivityMainBinding
import com.balti.project_ads.storage.shared


class MainActivity : FragmentActivity() {
    val TAG = "main_activity"
    private lateinit var bindHome: ActivityMainBinding

    override fun onDestroy() {
        super.onDestroy()
        if(data.connected){
            setConnectivity(false)
        }
    }
    override fun onBackPressed() {
        // Prevent the app from closing
    }
    override fun onPause() {
        super.onPause()
        //update connectivity status
        if(data.connected){
            setConnectivity(false)
        }
    }
    override fun onResume() {
        super.onResume()
        if(data.connected){
            setConnectivity(true)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindHome = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindHome.root)

        //save binding home object to easily access all views
        data.bindHome = bindHome

        // rotate the screen to be in portrait mode
        rotateScreen()

        // Logic for device connection
        data.apiCalls = ApiCalls()
        data.deviceId = shared.get_id(this)

        //check if there is deviceId in shared preferences
        if(data.deviceId==""){
            getTempDevice()
        }else{
            setupApp()
        }
    }


    //ui functions ----------------------------------------------
    private fun setupApp(){
        Log.d(TAG, "setup App started")
        //start loading animation
        setSection("loading")

        data.apiCalls.isDeviceConnected(data.deviceId) {msg ->
            data.connected = msg=="connect"
            if(msg=="connect"){
                setupHome()
            }else if(msg=="not_connect"){
                //device not added to server
                //check if there is a temp device in android
                if(data.deviceId!=""){
                    //there is a temp device, check if its still valid in server
                    getTempDevice()
                }
                else{
                    //generate a new temp device
                    createTempDevice()
                }
            }else if(msg=="error"){
                setSection("offline")
            }
        }
    }
    private fun setupHome() {
        Log.d(TAG, "setup Home started")
        //update connectivity status to online
        setConnectivity(true)

        //show empty ads until the a new schedule fetching occurs
        data.getSchdules(this,data.deviceId)

        //device is connected so open the 'home' section
        setSection("home")

        if(!data.refresher_started){
            data.scheduleRefresh(this)
        }
    }
    private fun setSection(sectionName: String) {
        // Reset visibility for all sections
        bindHome.loading.visibility = View.GONE
        bindHome.containerConnect.visibility = View.GONE
        bindHome.containerOffline.visibility = View.GONE
        bindHome.containerHome.visibility = View.GONE

        Log.d(TAG, "setSection: you are trying to show section '$sectionName'")
        when (sectionName) {
            "offline" -> {
                // Stop and release the player if it already exists
                try{
                    // Cancel any ongoing playback
                    playbackJob?.cancel()
                    player.stop()
                    player.release()
                }catch (e:Exception){
                }


                bindHome.containerOffline.visibility = View.VISIBLE
                bindHome.timer.text = "10"
                // Create a 10-second timer
                val timer = object : CountDownTimer(10000, 1000) { // 10 seconds, ticks every 1 second
                    override fun onTick(millisUntilFinished: Long) {
                        bindHome.timer.text = (millisUntilFinished/1000).toString()
                    }
                    override fun onFinish() {
                        setupApp()
                    }
                }
                timer.start()
            }
            "connect" -> {
                bindHome.containerConnect.visibility = View.VISIBLE
                if (data.deviceId.isNotEmpty()) {
                    bindHome.codeText.text = data.deviceId
                }
                bindHome.continueBtn.setOnClickListener {
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
                Log.e(TAG, "setSection: your section '$sectionName' in unavailable", )
            }
        }
    }

    //api functions ---------------------------------------------
    private fun createTempDevice() {
        data.apiCalls.createTempDevice { device ->
            if (device != null) {
                // Device Created successfully
                shared.save_id(this, device.id)
                data.deviceId = device.id
                setSection("connect")
            } else {
                //error while creating temp device
                setSection("offline")
            }
        }
    }
    private fun getTempDevice() {
        Log.d(TAG,"getting temporary device")
        data.apiCalls.getTempDevice(data.deviceId) {device ->
            if (device != null) {
                Log.d(TAG,"temporary device exist , go to connect")
                // Device exist in the server
                setSection("connect")
            } else {
                Log.d(TAG,"temporary device does not exist , create new one")
                // Device does not exist in the server
                createTempDevice()
            }
        }
    }
    private fun setConnectivity(connected: Boolean){
        //update device in server
        if(data.deviceId.isNotEmpty()){
            data.deviceId.let { id->
                data.current_date { date->
                    val device = Status(status = if(connected) "online" else "offline",date)
                    data.apiCalls.updateDevice(id,device) { isUpdated ->
                        if (isUpdated) {
                            // Device update successful
                            Log.d("server_messages","device status updated")
                        }else {
                            Log.d("server_messages","error while updating status")
                        }
                    }
                }
            }
        }
    }

    // functionality ---------------------------------------------
    private fun rotateScreen() {
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
    }
}


