package pt.ipt.dam.luckyspin

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.dam.luckyspin.data.Repository
import java.io.FileOutputStream
import java.security.MessageDigest

/**
 * Classe que aloja a página de login
 */
class LoginActivity : AppCompatActivity() {

    /**
     * Função que cria a página de login
     * @param savedInstanceState estado da instância
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //definição do layout da página
        setContentView(R.layout.login)

        //inicialização dos elementos da página
        //campo de texto para inserir o username
        val usernameInput : EditText = findViewById(R.id.usernameInput)
        //campo de texto para inserir a password
        val passwordInput: EditText = findViewById(R.id.passwordInput)
        //botão para efetuar o login
        val loginButton: Button = findViewById(R.id.loginButton)
        //ir para a página de registo
        val signUpTextView: TextView = findViewById(R.id.newUserSignUp)

        //instância da classe Repository
        val rep = Repository()

        //ação do botão de login
        //se o username e a password forem válidos, o utilizador é redirecionado para a página da roleta
        loginButton.setOnClickListener {
            val username = usernameInput.text.toString().trim().lowercase()
            val password = passwordInput.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //verificação do nome de utilizador e da palavra-passe
            rep.getUsers { users ->
                if (users != null) {
                    //procura do nome de utilizador na API
                    val user = users.find { it.username == username }
                    //verificação da palavra-passe
                    if (user != null && user.hashPass == hashPass(password)) {
                        //login bem-sucedido
                        Toast.makeText(this, "Autenticação bem-sucedida!", Toast.LENGTH_SHORT).show()

                        //gravação do nome de utilizador no ficheiro de texto
                        writeToFile("user.txt", user.username ?: "")
                        //gravação dos créditos do utilizador no ficheiro de texto
                        writeToFile("creditos.txt", user.creditos.toString())

                        //redirecionamento para a página da roleta
                        val intent = Intent(this, RoletaActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Nome de utilizador ou senha incorretos", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Erro na procura do utilizador", Toast.LENGTH_SHORT).show()
                }
            }
        }

        //ação do texto "ir para a página de registo"
        //o utilizador é redirecionado para a página de registo
        signUpTextView.setOnClickListener {
            //criação do intent para a página de registo
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
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

    /**
     * Função que permite escrever num ficheiro de texto
     * @param fileName nome do ficheiro
     * @param dataToSave dados a escrever
     */
    private fun writeToFile(fileName: String, dataToSave: String) {
        try {
            //abertura do ficheiro
            val fileOutputStream : FileOutputStream
            try {
                //escrita dos dados no ficheiro
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
