package pt.ipt.dam.luckyspin

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.dam.luckyspin.data.Repository
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.security.MessageDigest

/**
 * Classe que aloja a página de perfil do utilizador
 */
class ProfileActivity: AppCompatActivity() {

    //elementos da página
    //campo de texto para inserir o username
    private lateinit var usrInput : EditText
    //campo de texto para inserir o email
    private lateinit var emailInput : EditText
    //campo de texto para inserir a password
    private lateinit var passInput : EditText
    //campo de texto para inserir a password
    private lateinit var passConfirm : EditText
    //imageVie para voltar à roleta
    private lateinit var btVoltar : ImageView
    //botão para alterar os dados
    private lateinit var alterarDadosButton: Button
    //botão para fazer logout
    private lateinit var btLogout: Button
    //botão para eliminar a conta
    private lateinit var btEliminar: Button

    //variável que guarda o email do utilizador
    var lastEmail: String? = ""

    /**
     * Função que cria a página de perfil do utilizador
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //definição do layout da página
        setContentView(R.layout.profile)

        //inicialização dos elementos da página
        usrInput           = findViewById(R.id.usernameEditText)
        emailInput         = findViewById(R.id.emailEditText)
        passInput          = findViewById(R.id.passwordEditText)
        passConfirm        = findViewById(R.id.passwordNewEditText)
        alterarDadosButton = findViewById(R.id.alterarDadosButton)
        btLogout           = findViewById(R.id.btLogout)
        btEliminar         = findViewById(R.id.eliminarUser)
        btVoltar           = findViewById(R.id.btVoltar)

        //instância da classe Repository
        val rep = Repository()

        //leitura do nome de utilizador do ficheiro
        var lastUsername = readFromFile("user.txt")
        //definição do nome de utilizador no campo de texto
        usrInput.setText(lastUsername)

        //leitura do email da API
        rep.getUsers { users ->
            if (users != null) {
                //procura do nome de utilizador na API
                val user = users.find { it.username == lastUsername }
                if (user != null) {
                    //definição do email no campo de texto
                    lastEmail = user.email
                    emailInput.setText(lastEmail)
                }
            } else {
                Toast.makeText(this, "Erro na procura do utilizador", Toast.LENGTH_SHORT).show()
            }
        }

        //ação do botão de alterar dados
        //se o username, o email e a password forem válidos, os dados são alterados
        alterarDadosButton.setOnClickListener {
            //leitura do nome de utilizador do ficheiro e atribuir à variável
            lastUsername = readFromFile("user.txt")
            //leitura dos créditos do utilizador do ficheiro e atribuir à variável
            val creditStr = readFromFile("creditos.txt").toInt()

            //verificação do campo da nova palavra-passe, se estiver vazio não é necessário alterar a palavra-passe
            if (passConfirm.getText().toString().isEmpty()){
                //alteração dos dados
                updateUser(lastUsername, usrInput.getText().toString(), emailInput.getText().toString(), passInput.getText().toString(), creditStr){ sucesso ->
                    if(sucesso){
                        //mensagem de sucesso
                        Toast.makeText(this, "Dados atualizados!", Toast.LENGTH_SHORT).show()
                        //atualização do nome de utilizador no ficheiro
                        writeToFile("user.txt", usrInput.getText().toString())
                    }
                }
            } else {
                //alteração dos dados
                updateUser(lastUsername, usrInput.getText().toString(), emailInput.getText().toString(), passInput.getText().toString(), creditStr){ sucesso ->
                    if(sucesso){
                        //mensagem de sucesso
                        Toast.makeText(this, "Dados atualizados!", Toast.LENGTH_SHORT).show()
                        //atualização do nome de utilizador no ficheiro
                        writeToFile("user.txt", usrInput.getText().toString())
                    }
                }
                //atribuição do nome de utilizador do ficheiro à variável
                lastUsername = readFromFile("user.txt")
                //alteração da palavra-passe
                updateUserPass(lastUsername, passConfirm.getText().toString(), passInput.getText().toString()) { sucesso ->
                    if (sucesso) {
                        //mensagem de sucesso
                        Toast.makeText(this, "Palavra-Passe atualizada!", Toast.LENGTH_SHORT).show()
                        //limpar campos de texto
                        passConfirm.setText("")
                        passInput.setText("")
                    }
                }
            }

        }

        //ação do botão de logout
        //o utilizador é redirecionado para a página de login
        btLogout.setOnClickListener {
            //leitura do nome de utilizador do ficheiro e atribuir à variável
            val username = readFromFile("user.txt")
            //leitura dos créditos do utilizador do ficheiro e atribuir à variável
            val creditStr = readFromFile("creditos.txt").toInt()
            //atualização dos créditos
            updateUserCredits(username, creditStr) { sucesso ->
                if (sucesso) {
                    //mensagem de sucesso
                    Toast.makeText(this, "Créditos atualizados!", Toast.LENGTH_SHORT).show()
                } else {
                    //mensagem de erro
                    Toast.makeText(this, "Erro ao atualizar créditos", Toast.LENGTH_SHORT).show()
                }
            }

            //redirecionamento para a página de login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()

        }

        //ação do botão de eliminar a conta
        //o utilizador é redirecionado para a página de login
        btEliminar.setOnClickListener {

            if (passInput.getText().toString().isEmpty()){
                Toast.makeText(this, "Insira a sua Palavra-Passe para eliminar a sua conta!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else {
                //leitura da lista de utilizadores
                rep.getUsers { users ->
                    if (users != null) {
                        //procura do nome de utilizador na API
                        val user = users.find { it.username == lastUsername }
                        if (user != null && user.id != null) {
                            //verificação da palavra-passe
                            if (hashPass(passInput.getText().toString()) == user.hashPass) {
                                //eliminação do utilizador
                                rep.deleteUser(user.id) { success ->
                                    //mensagem de sucesso ou erro
                                    if (success) {
                                        Toast.makeText(
                                            this,
                                            "Utilizador eliminado com sucesso!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        //redirecionamento para a página de login
                                        val intent = Intent(this, LoginActivity::class.java)
                                        startActivity(intent)
                                        finish()

                                    } else {
                                        Toast.makeText(
                                            this,
                                            "Erro ao eliminar utilizador",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(this, "Erro na procura do utilizador", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        //ação do botão de voltar
        //o utilizador é redirecionado para a página de roleta
        btVoltar.setOnClickListener {
            //redirecionamento para a página de roleta
            val intent = Intent(this, RoletaActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /**
     * Função que permite ler um ficheiro de texto
     * @param fileName nome do ficheiro
     */
    private fun readFromFile(fileName: String): String {
        //leitura do ficheiro
        return try {
            //abertura do ficheiro
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

    /**
     * Função que permite atualizar os créditos do utilizador
     * @param username nome de utilizador do utilizador
     * @param novosCreditos novos créditos do utilizador
     * @param onResult retorno da atualização dos créditos
     */
    private fun updateUserCredits(username: String, novosCreditos: Int, onResult: (Boolean) -> Unit) {
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
     * Função que permite atualizar a palavra-passe do utilizador
     * @param username nome de utilizador do utilizador
     * @param passNova nova palavra-passe do utilizador
     * @param pass palavra-passe antiga do utilizador
     * @param onResult retorno da atualização da palavra-passe
     */
    private fun updateUserPass(username: String, passNova: String, pass: String, onResult: (Boolean) -> Unit) {
        //instância da classe Repository
        val rep = Repository()
        //leitura da lista de utilizadores
        rep.getUsers { users ->
            //procura do nome de utilizador na API
            val user = users?.find { it.username == username }
            if (user != null && user.id != null) {
                //verificação da palavra-passe
                if(hashPass(pass) == user.hashPass){
                    //atualização da palavra-passe
                    val userAtualizado = user.copy(hashPass = hashPass(passNova))
                    //atualização dos dados no servidor
                    rep.updateUser(user.id, userAtualizado) { updatedUser ->
                        if (updatedUser != null) {
                            onResult(true)
                        } else {
                            onResult(false)
                        }
                    }
                }
            } else {
                onResult(false)
            }
        }
    }

    /**
     * Função que permite atualizar os dados do utilizador
     * @param lastUsername nome de utilizador do utilizador
     * @param usernameNew novo nome de utilizador do utilizador
     * @param emailnovo novo email do utilizador
     * @param pass palavra-passe do utilizador
     * @param novosCreditos novos créditos do utilizador
     */
    private fun updateUser(lastUsername: String, usernameNew: String, emailnovo: String?, pass: String, novosCreditos: Int, onResult: (Boolean) -> Unit) {
        //instância da classe Repository
        val rep = Repository()
        //leitura da lista de utilizadores
        rep.getUsers { users ->
            //procura do nome de utilizador na API
            val user = users?.find { it.username == lastUsername }
            if (user != null && user.id != null) {
                //verificação do campo da palavra-passe
                if(pass.isNotEmpty()){
                    //verificação da palavra-passe
                    if(hashPass(pass) == user.hashPass){
                        //atualização dos dados
                        val userAtualizado = user.copy(username = usernameNew, email = emailnovo, creditos = novosCreditos)
                        //atualização dos dados no servidor
                        rep.updateUser(user.id, userAtualizado) { updatedUser ->
                            if (updatedUser != null) {
                                onResult(true)
                            } else {
                                onResult(false)
                            }
                        }
                    } else {
                        Toast.makeText(this, "Palavra-Passe Incorreta!", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Toast.makeText(this, "Insira a sua Palavra-Passe para alterar os seus dados!", Toast.LENGTH_SHORT).show()
                }


            } else {
                onResult(false)
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

