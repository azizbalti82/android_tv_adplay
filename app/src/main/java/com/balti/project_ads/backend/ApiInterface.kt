import com.balti.project_ads.backend.models.Ad
import com.balti.project_ads.backend.models.CreateDeviceResponse
import com.balti.project_ads.backend.models.Device
import com.balti.project_ads.backend.models.DeviceTemp
import com.balti.project_ads.backend.models.Schedule
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path


interface ApiInterface {
    // Endpoint to create a device
    @POST("/temp_devices")
    fun createTempDevice(): Call<CreateDeviceResponse>

    // Endpoint to get a device temp by ID
    @GET("/temp_devices/{deviceId}")
    fun getDeviceTemp(@Path("deviceId") deviceId: String): Call<DeviceTemp>

    // Endpoint to get a device by ID
    @GET("/devices/{deviceId}")
    fun getDevice(@Path("deviceId") deviceId: String): Call<Device>

    // Endpoint to update a device by ID
    @PUT("/devices/{deviceId}")
    fun updateDevice(
        @Path("deviceId") deviceId: String,
        @Body device: Device
    ): Call<Device>

    //get all schedules for a specific device
    @GET("schedules/search/device/{deviceId}")
    fun getSchedulesByDeviceId(@Path("deviceId") deviceId: String?): Call<List<Schedule?>?>?

    // get one ad by its id
    @GET("/ads/{adId}")
    fun getAd(@Path("adId") adId: String): Call<Ad>
}
