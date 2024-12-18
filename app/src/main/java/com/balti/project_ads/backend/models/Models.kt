package com.balti.project_ads.backend.models
import java.util.Date

data class CreateDeviceResponse(
    val message: String,
    val device: DeviceTemp
)

data class DeviceTemp(
    val id: String,
    val status: String,
    val _id: String,
    val createdAt: String,
    val __v: Int
)


data class Device(
    val id: String ="",           // Unique device ID
    val name: String="",         // Device name
    var status: String = "offline",  // Device status, default "offline"
    val createdAt: Date = Date(),  // Created date, defaults to current date/time
    val lastSeen: Date? = null // Last seen date, defaults to null
)
