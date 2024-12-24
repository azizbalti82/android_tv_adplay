package com.balti.project_ads.backend

import ApiInterface
import android.util.Log
import com.balti.project_ads.data
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class ApiCalls {
    val TAG = "server_messages"
    private val baseUrl = data.url

    // Initialize Retrofit
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val api = retrofit.create(ApiInterface::class.java)

    // CRUD operations for devices temp ------------------------------------------------------------
    fun createTempDevice(callback: (DeviceTemp_content?) -> Unit) {
        val call = api.createTempDevice()
        call.enqueue(object : Callback<DeviceTemp> {
            override fun onResponse(call: Call<DeviceTemp>, response: Response<DeviceTemp>) {
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()
                    Log.d(TAG,"createTempDevice: The created code: "+responseBody?.device?.id)
                    // Return the success message and device object
                    callback(responseBody?.device)
                } else {
                    // Handle API error or empty response
                    Log.e(TAG, "createTempDevice: Failed to create device: ${response.message()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<DeviceTemp>, t: Throwable) {
                Log.e(TAG, "error while creating temporary device ", t)
                callback(null)
            }
        })
    }
    fun getTempDevice(deviceId: String, callback: (DeviceTemp_content?) -> Unit) {
        val call = api.getTempDevice(deviceId)
        call.enqueue(object : Callback<DeviceTemp_content> {
            override fun onResponse(call: Call<DeviceTemp_content>, response: Response<DeviceTemp_content>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "getTempDevice: Device fetched successfully")
                    callback(response.body())
                } else {
                    Log.e(TAG, "getTempDevice: Failed to fetch device: ${response.message()}")
                    callback(null)
                }
            }
            override fun onFailure(call: Call<DeviceTemp_content>, t: Throwable) {
                Log.e(TAG, "getTempDevice: Failed to fetch device",t)
                callback(null)
            }
        })
    }

    // crud for connected devices ------------------------------------------------------------------
    fun isDeviceConnected(deviceId: String, callback: (String) -> Unit) {
        val call = api.getDevice(deviceId) // Get device to check if connected
        call.enqueue(object : Callback<Device> {
            override fun onResponse(call: Call<Device>, response: Response<Device>) {
                // If the device exists, it's considered connected
                if(response.body()?.Device!=null && response.body()?.Device?.id == deviceId){
                    Log.d(TAG, "isDeviceConnected: device connected")
                    callback("connect")
                }else{
                    Log.e(TAG, "isDeviceConnected: device is not connected")
                    callback("not_connect")
                }
            }

            override fun onFailure(call: Call<Device>, t: Throwable) {
                Log.e(TAG, "isDeviceConnected: error checking device connectivity:",t)
                callback("error")
            }
        })
    }
    fun getDevice(deviceId: String, callback: (Device?) -> Unit) {
        val call = api.getDevice(deviceId) // Get device to check if connected
        call.enqueue(object : Callback<Device> {
            override fun onResponse(call: Call<Device>, response: Response<Device>) {
                if (response.isSuccessful) {
                    val device = response.body()
                    if (device != null) {
                        // Log the device details to confirm if Date fields are parsed correctly
                        Log.d(TAG, "Device: $device")
                        callback(device)
                    } else {
                        Log.e(TAG, "Device response is empty.")
                        callback(null)
                    }
                } else {
                    Log.e(TAG, "Failed to fetch device")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<Device>, t: Throwable) {
                Log.e(TAG, "Network error (get device): ${t.message}")
                callback(null)
            }
        })
    }
    fun updateDevice(deviceId: String, device: Status, callback: (Boolean) -> Unit) {
        val call = api.updateDeviceStatus(deviceId, device)
        call.enqueue(object : Callback<Boolean> {
            override fun onResponse(p0: Call<Boolean>, p1: Response<Boolean>) {
                if (p1.isSuccessful && p1.body() != null) {
                    Log.d(TAG, "updateDevice: updated device successfully: "+p1.message())
                    callback(true)
                } else {
                    Log.e(TAG, "updateDevice: error while updating device: "+p1.message())
                    callback(false)
                }
            }
            override fun onFailure(p0: Call<Boolean>, p1: Throwable) {
                Log.e(TAG, "updateDevice: error while updating device: ",p1)
                callback(false)
            }
        })
    }

    // crud for schedules --------------------------------------------------------------------------
    fun getSchedulesByDeviceId(deviceId: String, callback: (List<Schedule?>?) -> Any) {
        api.getSchedulesByDeviceId(deviceId)?.enqueue(object : Callback<List<Schedule?>?> {
            override fun onResponse(
                call: Call<List<Schedule?>?>,
                response: Response<List<Schedule?>?>
            ) {
                if (response.isSuccessful) {
                    Log.d(TAG, "getSchedules: successfully got schedules : " + response.body())
                    val schedules = response.body()
                    // Handle the schedules, update UI, etc.
                    callback(schedules)
                } else {
                    Log.e(TAG, "getSchedules: error in getting schedules" + response.message())
                    callback(null)
                }
            }
            override fun onFailure(call: Call<List<Schedule?>?>, t: Throwable) {
                Log.e(TAG, "getSchedules: error in getting schedules" ,t)
                callback(null)
            }
        })
    }

    // crud for ads --------------------------------------------------------------------------------
    fun getMediaTypeFromAd(adId: String, callback: (String?) -> Any) {
        api.getAd(adId).enqueue(object : Callback<Ad> {
            override fun onResponse(p0: Call<Ad>, p1: Response<Ad>) {
                if (p1.isSuccessful) {
                    Log.e(TAG, "getMediaType: successfully got ad type : " +p1.body()?.ad?.type)
                    callback(p1.body()?.ad?.type)
                } else {
                    Log.e(TAG, "getMediaType: error in getting ad type " + p1.message())
                    callback(null)
                }
            }
            override fun onFailure(p0: Call<Ad>, p1: Throwable) {
                Log.e(TAG, "getMediaType: error in getting ad type",p1)
                callback(null)
            }
        })
    }
}
