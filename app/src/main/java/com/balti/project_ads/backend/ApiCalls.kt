package com.balti.project_ads.backend

import ApiInterface
import android.util.Log
import com.balti.project_ads.backend.models.Ad
import com.balti.project_ads.backend.models.Device
import com.balti.project_ads.backend.models.DeviceTemp
import com.balti.project_ads.backend.models.DeviceTemp_content
import com.balti.project_ads.backend.models.Schedule
import com.balti.project_ads.backend.models.Status
import com.balti.project_ads.data
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class ApiCalls {
    val TAG = "error_server"
    private val baseUrl = data.url

    // Initialize Retrofit
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create(provideGson()))
        .build()

    private val Api = retrofit.create(ApiInterface::class.java)

    // CRUD operations for devices temp
    fun createTempDevice(callback: (String, DeviceTemp_content?) -> Unit) {
        // Call the server's endpoint that creates the device
        val call = Api.createTempDevice()
        // Enqueue the request
        call.enqueue(object : Callback<DeviceTemp> {
            override fun onResponse(call: Call<DeviceTemp>, response: Response<DeviceTemp>) {
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()
                    Log.d(TAG, "the created code: "+responseBody?.device.toString())
                    // Return the success message and device object
                    callback(responseBody!!.message, responseBody.device)
                } else {
                    // Handle API error or empty response
                    Log.e(TAG, response.message())
                    callback("Failed to create device: ${response.message()}", null)
                }
            }

            override fun onFailure(call: Call<DeviceTemp>, t: Throwable) {
                // Handle network or other failures
                callback("Network error (create temp device): ${t.message}", null)
            }
        })
    }
    fun getTempDevice(deviceId: String, callback: (String, DeviceTemp_content?) -> Unit) {
        val call = Api.getTempDevice(deviceId)
        call.enqueue(object : Callback<DeviceTemp_content> {
            override fun onResponse(call: Call<DeviceTemp_content>, response: Response<DeviceTemp_content>) {
                if (response.isSuccessful) {
                    callback("Device fetched successfully", response.body())
                } else {
                    Log.e(TAG, response.message())
                    callback("Failed to fetch device: ${response.message()}", null)
                }
            }
            override fun onFailure(call: Call<DeviceTemp_content>, t: Throwable) {
                Log.e(TAG, t.message.toString())
                callback("Network error (get temp device) : ${t.message}", null)
            }
        })
    }

    // crud for connected devices
    fun isDeviceConnected(deviceId: String, callback: (String) -> Unit) {
        val call = Api.getDevice(deviceId) // Get device to check if connected
        call.enqueue(object : Callback<Device> {
            override fun onResponse(call: Call<Device>, response: Response<Device>) {
                // If the device exists, it's considered connected
                Log.e(TAG, "device connected")
                if(response.body()?.Device!=null && response.body()?.Device?.id == deviceId){
                    callback("connect")
                }else{
                    callback("not_connect")
                }
            }

            override fun onFailure(call: Call<Device>, t: Throwable) {
                Log.e(TAG, "error checking device connectivity")
                callback("error")
            }
        })
    }
    fun getDevice(deviceId: String, callback: (Device?) -> Unit) {
        val call = Api.getDevice(deviceId) // Get device to check if connected
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
        val call = Api.updateDeviceStatus(deviceId, device)
        call.enqueue(object : Callback<Boolean> {
            override fun onResponse(p0: Call<Boolean>, p1: Response<Boolean>) {
                if (p1.isSuccessful && p1.body() != null) {
                    callback(true)
                } else {
                    Log.e(TAG, "updated device successfully: "+p1.message())
                    callback(false)
                }
            }

            override fun onFailure(p0: Call<Boolean>, p1: Throwable) {
                Log.e(TAG, "error in update"+p1.message.toString() )
                callback(false)
            }
        })
    }
    // crud for schedules
    fun getSchedulesByDeviceId(deviceId: String, callback: (List<Schedule?>?) -> Any) {
        Api.getSchedulesByDeviceId(deviceId)?.enqueue(object : Callback<List<Schedule?>?> {
            override fun onResponse(
                call: Call<List<Schedule?>?>,
                response: Response<List<Schedule?>?>
            ) {
                if (response.isSuccessful) {
                    val schedules = response.body()
                    // Handle the schedules, update UI, etc.
                    callback(schedules)
                } else {
                    Log.e(TAG, "error in getting schedules" + response.message())
                    callback(null)
                }
            }

            override fun onFailure(call: Call<List<Schedule?>?>, t: Throwable) {
                Log.e(TAG, "error in getting schedules" + t.message.toString())
                callback(null)
            }
        })
    }

    // crud for ads
    fun getMediaTypeFromAd(adId: String, callback: (String?) -> Any) {
        Api.getAd(adId).enqueue(object : Callback<Ad> {
            override fun onResponse(p0: Call<Ad>, p1: Response<Ad>) {
                if (p1.isSuccessful) {
                    val ad = p1.body()
                    // Handle the schedules, update UI, etc.
                    callback(ad?.ad?.type)
                } else {
                    Log.e(TAG, "error in getting ad type" + p1.message())
                    callback(null)
                }
            }

            override fun onFailure(p0: Call<Ad>, p1: Throwable) {
                Log.e(TAG, "error in getting ad type" + p1.message)
                callback(null)
            }
        })
    }
}
