package com.balti.project_ads.backend.models
import java.util.Date

data class CreateDeviceResponse(
    val message: String,
    val device: DeviceTemp
)

data class DeviceTemp(
    val id: String,
    val _id: String,
    val status: String,
    val createdAt: String,
    val __v: Int
)



data class Device(
    val id: String,  // Unique identifier
    val name: String,  // Name of the device
    var status: String = "offline",  // Default value 'offline'
    val createdAt: Date = Date(),  // Default is the current date and time
    val lastSeen: Date = Date()  // Default is the current date and time
)


class Schedule {
    // Getters and setters
    var id: String? = null
    var ad_id: String? = null
    var device_id: String? = null
    var start: Date? = null
    var end: Date? = null
    var orientation: String? = null
    var status: String? = null
}

data class Ad(
    val ad: ad_content
)
data class ad_content(
    val _id: String? = null,  // Corresponds to "ad._id"
    val id: String? = null,   // Corresponds to "ad.id"
    val title: String? = null,  // Corresponds to "ad.title"
    val description: String? = null,  // Corresponds to "ad.description"
    val type: String? = null,  // Corresponds to "ad.type"
    val mediaUrl: String? = null,  // Corresponds to "ad.mediaUrl"
    val mediaExtension: String? = null,  // Corresponds to "ad.mediaExtension"
    val createdAt: String? = null,  // Corresponds to "ad.createdAt"
    val updatedAt: String? = null,  // Corresponds to "ad.updatedAt"
    val v: Int? = null  // Corresponds to "ad.__v"
)

