package pt.ipt.dam.luckyspin.data

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Classe que permite a interação com a API
 */
class Repository() {

    // Instância da API
    private val api = Api.instance

    /**
     * Função que permite a obtenção de todos os utilizadores
     * @param onResult retorno da lista de utilizadores
     */
    fun getUsers(onResult: (List<User>?) -> Unit) {
        // chamada à API para obter a lista de utilizadores
        api.getUsers().enqueue(object : Callback<ApiSheety.UserResponse> {
            // tratamento da resposta da API
            override fun onResponse(
                call: Call<ApiSheety.UserResponse>,
                response: Response<ApiSheety.UserResponse>
            ) {
                // se a resposta for bem-sucedida, a lista de utilizadores é retornada
                if (response.isSuccessful) {
                    //Log.d("teste_response", response.body().toString())
                    val users = response.body()?.users ?: emptyList()
                    onResult(users)
                } else {
                    onResult(null)
                }
            }
            // tratamento de erros
            override fun onFailure(call: Call<ApiSheety.UserResponse>, t: Throwable) {
                onResult(null)
            }
        })
    }

    /**
     * Função que permite a criação de um utilizador
     * @param user utilizador a criar
     * @param onResult retorno do utilizador criado
     */
    fun createUser(user: User, onResult: (User?) -> Unit) {
        // criação de um utilziador com os dados do utilizador passado por parametro
        val newUser = User(
            email = user.email,
            hashPass = user.hashPass,
            username = user.username,
            creditos = user.creditos
        )
        // chamada à API para criar o utilizador
        val request = ApiSheety.UserRequest(newUser)
        api.createUser(request).enqueue(object : Callback<ApiSheety.UserRequest> {
            // tratamento da resposta da API
            override fun onResponse(
                call: Call<ApiSheety.UserRequest>,
                response: Response<ApiSheety.UserRequest>
            ) {
                // se a resposta for bem-sucedida, o utilizador criado é retornado
                if (response.isSuccessful) {
                    Log.d("teste_response", response.body().toString())
                    val createdUser = response.body()?.user
                    onResult(createdUser)
                } else {
                    onResult(null)
                }
            }
            // tratamento de erros
            override fun onFailure(call: Call<ApiSheety.UserRequest>, t: Throwable) {
                onResult(null)
            }
        })
    }

    /**
     * Função que permite a editar os dados de um utilizador
     * @param id id do utilizador a editar
     * @param user utilizador com os dados a editar
     * @param onResult retorno do utilizador editado
     */
    fun updateUser(id: Int, user: User, onResult: (User?) -> Unit) {
        val request = ApiSheety.UserRequest(user)
        api.updateUser(id, request).enqueue(object : Callback<ApiSheety.UserRequest> {
            override fun onResponse(
                call: Call<ApiSheety.UserRequest>,
                response: Response<ApiSheety.UserRequest>
            ) {
                // se a resposta for bem-sucedida, o utilizador é editado
                if (response.isSuccessful) {
                    onResult(response.body()?.user)
                } else {
                    onResult(null)
                }
            }
            // tratamento de erros
            override fun onFailure(call: Call<ApiSheety.UserRequest>, t: Throwable) {
                onResult(null)
            }
        })
    }

    /**
     * Função que permite a eliminação de um utilizador
     * @param id id do utilizador a eliminar
     * @param onResult retorno do resultado da eliminação
     */
    fun deleteUser(id: Int, onResult: (Boolean) -> Unit) {
        // chamada à API para eliminar o utilizador
        api.deleteUser(id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                onResult(response.isSuccessful)
            }
            // tratamento de erros
            override fun onFailure(call: Call<Void>, t: Throwable) {
                onResult(false)
            }
        })
    }

}
