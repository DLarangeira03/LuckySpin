package pt.ipt.dam.luckyspin

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.dam.luckyspin.data.Amount
import pt.ipt.dam.luckyspin.data.MbwayPaymentResponse
import pt.ipt.dam.luckyspin.data.MbwayRequest
import pt.ipt.dam.luckyspin.data.Payment
import pt.ipt.dam.luckyspin.data.Repository
import pt.ipt.dam.luckyspin.data.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStreamReader

/**
 * Classe que aloja a página de créditos do utilizador
 */
class CreditosActivity : AppCompatActivity(){

    //Botão para retornar à roleta
    private lateinit var btVoltar : Button
    //Botão para depositar créditos
    private lateinit var btDepositar : Button
    //Valor a depositar no mbway
    private lateinit var inputValor : EditText
    //Nome de utilizador do utilizador
    var username: String = ""
    //Créditos do utilizador
    var creditos: Int = 0

    /**
     * Função que cria a página de créditos
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //definição do layout da página
        setContentView(R.layout.creditos)

        //inicialização dos elementos da página
        btVoltar = findViewById(R.id.btVoltar)
        btDepositar = findViewById(R.id.btDepositar)
        inputValor = findViewById(R.id.inputValor)

        //leitura do nome de utilizador e dos créditos do utilizador
        username = readFromFile("user.txt")
        creditos = readFromFile("creditos.txt").toInt()

        //ação do botão voltar
        btVoltar.setOnClickListener {
            //criação do intent para a página inicial
            val intent = Intent(this, RoletaActivity::class.java)
            //início da página inicial
            startActivity(intent)
        }

        btDepositar.setOnClickListener {
            //abre a um pop-up com ajuda
            initiateTransaction()
        }
    }

    /**
     * Função que abre um pop-up para colocar o número de telemóvel
     */
    private fun initiateTransaction() {
        //criação do pop-up
        val inflater = LayoutInflater.from(this)
        //definição do layout do pop-up
        val view = inflater.inflate(R.layout.mbway, null)
        //campo de texto para inserir o número de telemóvel
        val inputPhone : EditText = view.findViewById(R.id.inputMbwayPhone)
        //definição do pop-up
        val dialog = AlertDialog.Builder(this)
            .setTitle("MB WAY Depósito")
            .setMessage("Introduza o seu número MB WAY:")
            .setView(view)
            .setPositiveButton("Confirmar") { _, _ ->
                //leitura do número de telemóvel
                val phoneNumber = inputPhone.text.toString()
                //verificação do número de telemóvel
                if (phoneNumber.isNotEmpty()) {
                    //criar transação mbway
                    createMbwayTransaction(phoneNumber, inputValor.getText().toString().toInt()*100)
                } else {
                    Toast.makeText(this, "Número inválido!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()
    }

    /**
     * Função que permite criar uma transação mbway
     * @param phoneNumber número de telemóvel do utilizador
     * @param amount valor a depositar
     */
    private fun createMbwayTransaction(phoneNumber: String, amount: Int) {
        //criação da transação
        val request = MbwayRequest(
            payment = Payment(
                amount = Amount(currency = "EUR", value = amount),
                identifier = "test_${System.currentTimeMillis()}",
                customerPhone = phoneNumber,
                countryCode = "+351"
            )
        )

        //envio da transação
        RetrofitClient.instance.createMbwayPayment(request)
            .enqueue(object : Callback<MbwayPaymentResponse> {
                override fun onResponse(
                    call: Call<MbwayPaymentResponse>,
                    response: Response<MbwayPaymentResponse>
                ) {
                    if (response.isSuccessful) {
                        val result = response.body()
                        //verificação do resultado da transação
                        if (result?.transactionStatus == "Success") {
                            Toast.makeText(this@CreditosActivity, "Pagamento MB WAY concluído!", Toast.LENGTH_LONG).show()
                            Log.d("teste_creditos","${creditos} antes da alteracao")
                            creditos+=inputValor.getText().toString().toInt()*100
                            Log.d("teste_creditos","${creditos} depois da alteracao")
                            updateUserCredits(username,creditos){ sucesso ->
                                if (sucesso) {
                                    Toast.makeText(this@CreditosActivity, "Créditos atualizados!", Toast.LENGTH_SHORT).show()
                                    writeToFile("creditos.txt",creditos.toString())
                                } else {
                                    Toast.makeText(this@CreditosActivity, "Erro ao atualizar créditos", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(this@CreditosActivity, "Erro: ${result?.transactionStatus}", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this@CreditosActivity, "Erro na comunicação com Eupago!", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<MbwayPaymentResponse>, t: Throwable) {
                    Toast.makeText(this@CreditosActivity, "Falha na transação: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    /**
     * Função que permite atualizar os créditos do utilizador
     * @param username nome de utilizador do utilizador
     * @param novosCreditos novos créditos do utilizador
     * @param onResult retorno da atualização dos créditos
     */
    fun updateUserCredits(username: String, novosCreditos: Int, onResult: (Boolean) -> Unit) {
        //instância da classe Repository
        val rep = Repository()
        //leitura da lista de utilizadores
        rep.getUsers { users ->
            //procura do nome de utilizador na API
            val user = users?.find { it.username == username }
            if (user != null && user.id != null) {
                //atualização dos créditos
                val userAtualizado = user.copy(creditos = novosCreditos)
                //atualização dos dados no servidor
                rep.updateUser(user.id, userAtualizado) { updatedUser ->
                    if (updatedUser != null) {
                        onResult(true)
                    } else {
                        onResult(false)
                    }
                }
            } else {
                onResult(false)
            }
        }
    }
    /**
     * Função que permite ler um ficheiro de texto
     * @param fileName nome do ficheiro
     */
    fun readFromFile(fileName: String): String {
        return try {
            val fileInputStream = openFileInput(fileName)
            val inputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader = BufferedReader(inputStreamReader)

            val stringBuilder: StringBuilder = StringBuilder()
            bufferedReader.useLines { lines ->
                lines.forEach { stringBuilder.append(it).append("\n") }
            }

            stringBuilder.toString().trim()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * Função que permite escrever num ficheiro de texto
     * @param fileName nome do ficheiro
     * @param dataToSave dados a escrever
     */
    private fun writeToFile(fileName: String, dataToSave: String) {
        //abertura do ficheiro
        try {
            //escrita dos dados no ficheiro
            val fileOutputStream : FileOutputStream
            try {
                fileOutputStream = openFileOutput(fileName, MODE_PRIVATE)
                fileOutputStream.write(dataToSave.toByteArray())
                fileOutputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}