package com.balti.project_ads

import android.animation.AnimatorInflater
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.fragment.app.FragmentActivity
import com.balti.project_ads.backend.server
import com.balti.project_ads.backend.shared
import com.balti.project_ads.databinding.ActivityMainBinding



class MainActivity : FragmentActivity() {
    private lateinit var bindHome: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindHome = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindHome.root)

        // Adjust rotation and dimensions
        val root: View = bindHome.root

//        val displayMetrics = resources.displayMetrics
//        val height = displayMetrics.heightPixels
//        val width = displayMetrics.widthPixels

        bindHome.containerHome.rotation = 90f
        bindHome.containerConnect.rotation = 90f
        bindHome.offlineContainer.rotation = 90f
        root.requestLayout()




        // Logic for device connection
        val server = server()
        val deviceId = shared.get_id(this)

        server.is_device_connected(deviceId) { connected ->
            bindHome.loading.visibility = View.GONE

            if (connected) {
                bindHome.containerConnect.visibility = View.GONE
                bindHome.offlineContainer.visibility = View.GONE
                bindHome.containerHome.visibility = View.VISIBLE
            } else {
                bindHome.loading.visibility = View.VISIBLE
                bindHome.containerConnect.visibility = View.GONE
                bindHome.offlineContainer.visibility = View.GONE
                bindHome.containerHome.visibility = View.GONE

                Handler(Looper.getMainLooper()).postDelayed({
                    bindHome.loading.visibility = View.GONE
                    bindHome.containerConnect.visibility = View.VISIBLE
                }, 3000)
            }
        }
    }

}


