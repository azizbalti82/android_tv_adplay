package com.balti.project_ads

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.balti.project_ads.backend.ApiCalls
import com.balti.project_ads.backend.shared
import com.balti.project_ads.databinding.ActivityMainBinding


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

        //start loading animation
        show_loading()
        apiCalls.isDeviceConnected(deviceId) { connected ->
            if (connected) {
                //update connectivity status
                set_connectivity(true)


                //if device added to server
                show_home_section()

                //tell server that i'm online


                //get schedule of this device


                //see if there are new ads


                //if there are new ads, download their media


                //start the scheduling of the ads with WorkManager


            }
            else {
                //device not added to server
                //check if there is a temp device in android
                if(deviceId!=""){
                    //there is a temp device, check if its still valid in server
                    get_device_temp()
                }else{
                    //if its not valid (expired or deleted): generate a new temp device
                    create_device()
                }
            }
        }
    }

    //api functions ---------------------------------------------
    fun create_device() {
        apiCalls.createDevice { message, device ->
            if (device != null) {
                // Device céreation successful
                shared.save_id(this, device.id)
                show_connect_section(device.id)
            } else {
                // Handle error message
                Log.e("API_ERROR", message)
                show_offline_section("create_temp_device")
            }
        }
    }
    fun get_device_temp() {
        apiCalls.getDeviceTemp(deviceId) { message, device ->
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

    private fun set_connectivity(connected: Boolean) {
        apiCalls.getDevice(deviceId) { device ->
            if (device != null) {
                Log.d("error_", device.name)
                if(connected){
                    device.status = "online"

                }else{
                    device.status = "offline"
                }

                //update device in server
                apiCalls.updateDevice(device.id,device) { message, device ->
                    if (device == null) {
                        // Device update successful
                        Log.d("error", message)
                    }
                }
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
            if(message=="create_temp_device"){
                create_device()
            }
        }
    }
    fun show_connect_section(id: String) {
        bindHome.codeText.text = id
        bindHome.loading.visibility = View.GONE
        bindHome.containerConnect.visibility = View.VISIBLE
        bindHome.offlineContainer.visibility = View.GONE
        bindHome.containerHome.visibility = View.GONE
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


