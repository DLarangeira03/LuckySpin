package pt.ipt.dam.luckyspin

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Classe que permite a apresentação de informações sobre os desenvolvedores
 */
class AboutActivity : AppCompatActivity() {

    //declaração de componentes
    private lateinit var btVoltar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //definição do layout da página
        setContentView(R.layout.about)

        btVoltar = findViewById(R.id.btVoltar)

        //ação do botão voltar
        btVoltar.setOnClickListener {
            //criação do intent para a página inicial
            val intent = Intent(this, MainActivity::class.java)
            //início da página inicial
            startActivity(intent)
        }

    }
}