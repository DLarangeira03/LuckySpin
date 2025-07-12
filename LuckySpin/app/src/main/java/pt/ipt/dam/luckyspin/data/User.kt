package pt.ipt.dam.luckyspin.data

/**
 * Classe que representa um utilizador
 * @param id id do utilizador (tratado pela API)
 * @param email email do utilizador
 * @param hashPass hash da password do utilizador
 * @param username username do utilizador
 * @param creditos creditos do utilizador
 */
data class User(
    val id: Int? = null,
    val email: String?,
    val hashPass: String?,
    val username: String?,
    val creditos: Int?
)