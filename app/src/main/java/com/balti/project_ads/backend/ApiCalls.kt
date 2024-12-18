package com.balti.project_ads.backend

import ApiInterface
import android.util.Log
import com.balti.project_ads.backend.models.CreateDeviceResponse
import com.balti.project_ads.backend.models.Device
import com.balti.project_ads.backend.models.DeviceTemp
import com.balti.project_ads.backend.models.Schedule
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class ApiCalls {
    val TAG = "error_server"
    private val baseUrl = "http://192.168.1.122:3000/"

    // Initialize Retrofit
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create(provideGson()))
        .build()

    private val Api = retrofit.create(ApiInterface::class.java)

    // CRUD operations for devices temp
    fun createTempDevice(callback: (String, DeviceTemp?) -> Unit) {
        // Call the server's endpoint that creates the device
        val call = Api.createTempDevice()
        // Enqueue the request
        call.enqueue(object : Callback<CreateDeviceResponse> {
            override fun onResponse(call: Call<CreateDeviceResponse>, response: Response<CreateDeviceResponse>) {
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

            override fun onFailure(call: Call<CreateDeviceResponse>, t: Throwable) {
                // Handle network or other failures
                Log.e(TAG, t.message.toString())
                callback("Network error: ${t.message}", null)
            }
        })
    }
    fun getTempDevice(deviceId: String, callback: (String, DeviceTemp?) -> Unit) {
        val call = Api.getDeviceTemp(deviceId)
        call.enqueue(object : Callback<DeviceTemp> {
            override fun onResponse(call: Call<DeviceTemp>, response: Response<DeviceTemp>) {
                if (response.isSuccessful) {
                    callback("Device fetched successfully", response.body())
                } else {
                    Log.e(TAG, response.message())
                    callback("Failed to fetch device: ${response.message()}", null)
                }
            }
            override fun onFailure(call: Call<DeviceTemp>, t: Throwable) {
                Log.e(TAG, t.message.toString())
                callback("Network error: ${t.message}", null)
            }
        })
    }

    // crud for connected devices
    fun isDeviceConnected(deviceId: String, callback: (Boolean) -> Unit) {
        val call = Api.getDevice(deviceId) // Get device to check if connected
        call.enqueue(object : Callback<Device> {
            override fun onResponse(call: Call<Device>, response: Response<Device>) {
                // If the device exists, it's considered connected
                Log.e(TAG, response.message())
                callback(response.isSuccessful)
            }

            override fun onFailure(call: Call<Device>, t: Throwable) {
                Log.e(TAG, t.message.toString())
                callback(false)
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
                    Log.e(TAG, "Failed to fetch device: ${response.message()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<Device>, t: Throwable) {
                Log.e(TAG, "Network error: ${t.message}")
                callback(null)
            }
        })
    }
    fun updateDevice(deviceId: String, device: Device, callback: (String, Device?) -> Unit) {
        val call = Api.updateDevice(deviceId, device)

        call.enqueue(object : retrofit2.Callback<Device> {
            override fun onResponse(call: Call<Device>, response: retrofit2.Response<Device>) {
                if (response.isSuccessful && response.body() != null) {
                    callback("Device updated successfully", response.body())
                } else {
                    Log.e(TAG, response.message())
                    callback("Failed to update device: ${response.message()}", null)
                }
            }

            override fun onFailure(call: Call<Device>, t: Throwable) {
                Log.e(TAG, "error in update"+t.message.toString() )
                callback("Network error: ${t.message}", null)
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
}
