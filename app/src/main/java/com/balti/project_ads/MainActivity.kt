package com.balti.project_ads

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.balti.project_ads.backend.server
import com.balti.project_ads.backend.shared
import com.balti.project_ads.databinding.ActivityMainBinding

class MainActivity : FragmentActivity() {
    private lateinit var bind_home: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind_home = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind_home.root)

        //initialise variables
        val server = server()
        val device_id = shared.get_id(this)


        //1) check if this device signed in (exist in server)
        //if the a device_id stored in shared preferences == device_id in server:
        server.is_device_connected(device_id){connected ->
            if(connected){
                //select the ad screen

            }else{
                //select the connecting device screen

            }
        }
    }
}