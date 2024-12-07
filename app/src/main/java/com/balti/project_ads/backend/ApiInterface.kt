import com.balti.project_ads.backend.models.CreateDeviceResponse
import com.balti.project_ads.backend.models.Device
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiInterface {
    // Endpoint to create a device
    @POST("/temp_devices")
    fun createDevice(): Call<CreateDeviceResponse>

    // Endpoint to get a device by ID
    @GET("/temp_devices/{deviceId}")
    fun getDevice(@Path("deviceId") deviceId: String): Call<Device>
}
