package com.balti.project_ads.backend

import ApiInterface
import android.util.Log
import com.balti.project_ads.backend.models.CreateDeviceResponse
import com.balti.project_ads.backend.models.Device
import com.balti.project_ads.backend.models.DeviceTemp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiCalls {
    private val baseUrl = "http://192.168.1.255:3000/"

    // Initialize Retrofit
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val Api = retrofit.create(ApiInterface::class.java)

    // CRUD operations for devices temp
    fun createDevice(callback: (String, DeviceTemp?) -> Unit) {
        // Call the server's endpoint that creates the device
        val call = Api.createDevice()
        // Enqueue the request
        call.enqueue(object : Callback<CreateDeviceResponse> {
            override fun onResponse(call: Call<CreateDeviceResponse>, response: Response<CreateDeviceResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()
                    // Return the success message and device object
                    callback(responseBody!!.message, responseBody.deviceTemp)
                } else {
                    // Handle API error or empty response
                    callback("Failed to create device: ${response.message()}", null)
                }
            }

            override fun onFailure(call: Call<CreateDeviceResponse>, t: Throwable) {
                // Handle network or other failures
                callback("Network error: ${t.message}", null)
            }
        })
    }
    fun getDeviceTemp(deviceId: String, callback: (String, DeviceTemp?) -> Unit) {
        val call = Api.getDeviceTemp(deviceId)
        call.enqueue(object : Callback<DeviceTemp> {
            override fun onResponse(call: Call<DeviceTemp>, response: Response<DeviceTemp>) {
                if (response.isSuccessful) {
                    callback("Device fetched successfully", response.body())
                } else {
                    callback("Failed to fetch device: ${response.message()}", null)
                }
            }
            override fun onFailure(call: Call<DeviceTemp>, t: Throwable) {
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
                callback(response.isSuccessful)
            }

            override fun onFailure(call: Call<Device>, t: Throwable) {
                callback(false)
                Log.e("error", t.message.toString())
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
                        // If the device exists, return the device object
                        callback(device)
                    } else {
                        // If the device is found but empty, return null
                        Log.e("error_", "Device response is empty.")
                        callback(null)
                    }
                } else {
                    // If the device is not found or some error occurs, log it
                    Log.e("error_", "Failed to fetch device: ${response.message()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<Device>, t: Throwable) {
                // If there was a network error, return null and log the error
                Log.e("error_", "Network error: ${t.message}")
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
                    callback("Failed to update device: ${response.message()}", null)
                }
            }

            override fun onFailure(call: Call<Device>, t: Throwable) {
                callback("Network error: ${t.message}", null)
                Log.e("error", "error in update"+t.message.toString() )
            }
        })
    }
}
