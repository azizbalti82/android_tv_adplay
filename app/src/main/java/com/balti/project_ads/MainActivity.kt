package com.balti.project_ads

import android.os.Bundle
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //1) check if this device signed in (exist in server)

        //2) if not, sign in: add new device with id(code generated)


    }
}