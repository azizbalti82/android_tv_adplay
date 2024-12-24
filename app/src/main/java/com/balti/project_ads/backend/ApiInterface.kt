import com.balti.project_ads.backend.Ad
import com.balti.project_ads.backend.Device
import com.balti.project_ads.backend.DeviceTemp
import com.balti.project_ads.backend.DeviceTemp_content
import com.balti.project_ads.backend.Schedule
import com.balti.project_ads.backend.Status
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path


interface ApiInterface {
    // Endpoint to create a device
    @POST("/temp_devices")
    fun createTempDevice(): Call<DeviceTemp>

    // Endpoint to get a device temp by ID
    @GET("/temp_devices/{deviceId}")
    fun getTempDevice(@Path("deviceId") deviceId: String): Call<DeviceTemp_content>

    // Endpoint to get a device by ID
    @GET("/devices/{deviceId}")
    fun getDevice(@Path("deviceId") deviceId: String): Call<Device>

    // Endpoint to update a device by ID
    @PUT("/devices/{deviceId}")
    fun updateDeviceStatus(
        @Path("deviceId") deviceId: String,
        @Body status: Status
    ): Call<Boolean>

    //get all schedules for a specific device
    @GET("schedules/search/device/{deviceId}")
    fun getSchedulesByDeviceId(@Path("deviceId") deviceId: String?): Call<List<Schedule?>?>?

    // get one ad by its id
    @GET("/ads/{adId}")
    fun getAd(@Path("adId") adId: String): Call<Ad>
}
