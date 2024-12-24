package com.balti.project_ads.backend
import java.io.File
import java.util.Date


//for device temp
data class DeviceTemp(
    val message: String,
    val device: DeviceTemp_content
)
data class DeviceTemp_content(
    val id: String,
    val _id: String,
    val status: String,
    val createdAt: String,
    val __v: Int
)


//for device
data class Device(
    val Device: device_content
)
data class device_content(
    val _id:String,
    val id: String,
    val name: String,
    var status: String = "offline",
    val createdAt: Date = Date(),
    val lastSeen: Date = Date(),
    val __v:String
)

//status (use it to update status of device)
data class Status(
    val status: String
)

//for schedule
class Schedule {
    var id: String? = null
    var ad_id: String? = null
    var device_id: String? = null
    var start: Date? = null
    var end: Date? = null
    var orientation: String? = null
    var status: String? = null
}


//for ad
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


data class AdGroupItem(
    var type: String,
    var mediaFile: File?,
)

