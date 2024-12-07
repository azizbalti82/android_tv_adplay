package com.balti.project_ads.backend

import ApiInterface
import com.balti.project_ads.backend.models.CreateDeviceResponse
import com.balti.project_ads.backend.models.Device
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiCalls {
    private val baseUrl = "http://192.168.1.18:3000/"

    // Initialize Retrofit
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val Api = retrofit.create(ApiInterface::class.java)

    // CRUD operations for devices
    fun createDevice(callback: (String, Device?) -> Unit) {
        // Call the server's endpoint that creates the device
        val call = Api.createDevice()
        // Enqueue the request
        call.enqueue(object : Callback<CreateDeviceResponse> {
            override fun onResponse(call: Call<CreateDeviceResponse>, response: Response<CreateDeviceResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()
                    // Return the success message and device object
                    callback(responseBody!!.message, responseBody.device)
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
    fun getDevice(deviceId: String, callback: (String, Device?) -> Unit) {
        val call = Api.getDevice(deviceId)
        call.enqueue(object : Callback<Device> {
            override fun onResponse(call: Call<Device>, response: Response<Device>) {
                if (response.isSuccessful) {
                    callback("Device fetched successfully", response.body())
                } else {
                    callback("Failed to fetch device: ${response.message()}", null)
                }
            }
            override fun onFailure(call: Call<Device>, t: Throwable) {
                callback("Network error: ${t.message}", null)
            }
        })
    }


    // Method to check if device is connected
    fun isDeviceConnected(deviceId: String, callback: (Boolean) -> Unit) {
        // This function checks if the device is connected to the server
        // Assuming you'd have an API for checking device connectivity
        callback(false)
    }
}
