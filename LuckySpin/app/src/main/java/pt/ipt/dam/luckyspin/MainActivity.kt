package pt.ipt.dam.luckyspin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

/**
 * Classe que aloja a página inicial da aplicação
 */
class MainActivity : AppCompatActivity() {

    //botões da página inicial
    //startBt é o botão para seguir para a página de login
    private lateinit var startBt: Button
    //aboutBt é o botão para seguir para a página de sobre a aplicação e os criadores
    private lateinit var aboutBt: Button

    /**
     * Função que cria a página inicial
     * @param savedInstanceState estado da instância
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //definição do layout da página
        setContentView(R.layout.activity_main)

        //inicialização dos botões
        startBt = findViewById(R.id.startButton)
        aboutBt = findViewById(R.id.about)

        //ação dos botões
        //startBt abre a página de login quando pressionado
        startBt.setOnClickListener {
            //criação do intent para a página de login
            val intent = Intent(this, LoginActivity::class.java)
            //início da página de login
            startActivity(intent)
        }

        //aboutBt abre a página que explica a aplicação e os criadores quando pressionado
        aboutBt.setOnClickListener {
            //criação do intent para a página de sobre a aplicação e os criadores
            val intent = Intent(this, AboutActivity::class.java)
            //início da página de sobre a aplicação e os criadores
            startActivity(intent)
        }

    }
}
