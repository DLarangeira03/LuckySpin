package pt.ipt.dam.luckyspin.data

import retrofit2.Call
import retrofit2.http.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
* Interface que permite a interação com a API
*/
interface ApiSheety {

    /**
    *Classes de resposta que retorna uma lista de utilizadores
     * @param users lista de utilizadores
    */
    data class UserResponse(
        val users : List<User>
    )

    /**
    *Classe de resposta que retorna um utilizador
     * @param user utilizador
    */
    data class UserRequest(
        val user : User
    )

    /**
    *Função que permite a obtenção de todos os utilizadores
    */
    @GET("users/")
    fun getUsers(): Call<UserResponse>

    /**
     *Função que permite a criação de um utilizador
     * @param userReq user a criar
     */
    @POST("users/")
     fun createUser(@Body userReq: UserRequest): Call<UserRequest>

    /**
     * Função que permite a atualização de um utilizador
     * @param id id do utilizador a atualizar
     * @param userReq user com os dados a atualizar
     */
    @PUT("users/{id}")
    fun updateUser(@Path("id") id: Int, @Body userReq: UserRequest): Call<UserRequest>

     /**
      *Função que permite a eliminação de um utilizador
      * @param id id do utilizador a eliminar
      */
    @DELETE("users/{id}")
     fun deleteUser(@Path("id") id: Int): Call<Void>

}

/**
* Objeto que permite a instância da API
*/
object Api {
    // URL base da API
    private const val BASE_URL = "https://api.sheety.co/f0e4e74efdf030397e3491e453ff91f5/luckySpin/"
    // Instância da API
    val instance: ApiSheety by lazy {
        //utilização da biblioteca Retrofit 
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiSheety::class.java)
    }
}