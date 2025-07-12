package pt.ipt.dam.luckyspin

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.dam.luckyspin.data.Repository
import pt.ipt.dam.luckyspin.data.User
import java.security.MessageDigest

/**
 * Classe que aloja a página de registo
 */
class RegisterActivity : AppCompatActivity() {

    /**
     * Função que cria a página de registo
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //definição do layout da página
        setContentView(R.layout.register)

        //inicialização dos elementos da página
        //campo de texto para inserir o email
        val emailInput: EditText = findViewById(R.id.emailInput)
        //campo de texto para inserir o nome de utilizador
        val usernameInput : EditText = findViewById(R.id.usernameInput)
        //campo de texto para inserir a palavra-passe
        val passwordInput: EditText = findViewById(R.id.passwordInput)
        //botão para efetuar o registo
        val signUpButton: Button = findViewById(R.id.signUpButton)

        //instância da classe Repository
        val rep = Repository()

        //ação do botão de registo
        //se o email, o nome de utilizador e a palavra-passe forem válidos, o utilizador é criado
        signUpButton.setOnClickListener {
            val email = emailInput.text.toString().trim().lowercase()
            val username = usernameInput.text.toString().trim().lowercase()
            val password = passwordInput.text.toString().trim()
            val passhash = hashPass(password)

            //verificação dos campos
            if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //criação do utilizador
            rep.createUser(User(email = email, username = username, hashPass = passhash, creditos = 500)) { user ->
                if (user != null) {
                    Toast.makeText(this, "Registado com sucesso!", Toast.LENGTH_SHORT).show()
                    //redirecionamento para a página de login
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Erro no registo!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Função que permite transformar uma string num hash da mesma
     * @param input string a transformar
     * @param algorithm algoritmo de hash a usar (neste caso é sempre o mesmo)
     */
    private fun hashPass(input: String, algorithm: String = "SHA-256"): String {
        //transformação da string num hash
        val bytes = MessageDigest.getInstance(algorithm).digest(input.toByteArray())
        //transformação do hash num string em base64
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }


}
