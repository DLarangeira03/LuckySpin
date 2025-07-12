package pt.ipt.dam.luckyspin.data


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//classe que permite a instância da API
object RetrofitClient {
    private const val BASE_URL = "https://sandbox.eupago.pt/api/v1.02/"
    //instância da API
    val instance: MbWAY by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(MbWAY::class.java)
    }
}
