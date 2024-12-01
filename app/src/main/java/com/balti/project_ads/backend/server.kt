package com.balti.project_ads.backend

class server {

    fun is_device_connected(device_id:String,callback: (Boolean) -> Unit){
        //check if the device is connected to the server
        callback(true)
    }
}