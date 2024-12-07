package com.balti.project_ads.backend.models

data class CreateDeviceResponse(
    val message: String,
    val device: Device
)

data class Device(
    val id: String,
    val status: String,
    val _id: String,
    val createdAt: String,
    val __v: Int
)
