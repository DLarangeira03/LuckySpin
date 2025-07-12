package pt.ipt.dam.luckyspin.data

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Classes de resposta que retorna um pagamento
 * @param payment um pagamento
 */
data class MbwayRequest(
    val payment: Payment
)

/**
 * Classes de resposta que define um pagamento
 * @param amount valor do pagamento
 * @param identifier identificador do pagamento
 * @param customerPhone número do cliente
 * @param countryCode código do país
 */
data class Payment(
    val amount: Amount,
    val identifier: String,
    val customerPhone: String,
    val countryCode: String
)

/**
 * Classes de resposta que define o valor do pagamento
 * @param currency moeda do pagamento
 * @param value valor do pagamento
 */
data class Amount(
    val currency: String,
    val value: Int
)

/**
 * Classes de resposta que retorna um pagamento
 * @param transactionStatus status do pagamento
 * @param transactionID id do pagamento
 * @param reference referência do pagamento
 */
data class MbwayPaymentResponse(
    val transactionStatus: String,
    val transactionID: String?,
    val reference: String?
)

/**
 * Interface que permite a comunicação com a API MbWay
 */
interface MbWAY {
    //Cabeçalho da requisição
    @Headers(
        "accept: application/json",
        "content-type: application/json",
        "Authorization: ApiKey demo-ed56-83d1-1326-cb5"
    )

    //Função que permite a criação de um pagamento
    @POST("mbway/create")
    fun createMbwayPayment(
        @Body request: MbwayRequest
    ): Call<MbwayPaymentResponse>
}
