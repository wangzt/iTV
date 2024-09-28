import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://resource-1313403875.cos.ap-guangzhou.myqcloud.com/"

    // 初始化OkHttpClient，可以在这里添加拦截器或配置超时
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // 初始化Retrofit实例
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)  // 可选：配置OkHttp客户端
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // 创建并返回API接口服务
    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
}
