package pt.ipt.dam.luckyspin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import pt.ipt.dam.luckyspin.data.Repository
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStreamReader

/**
 * Classe que aloja a página da roleta
 */
class RoletaActivity : AppCompatActivity(), SensorEventListener  {

    //elementos da página
    //imagem do resultado
    private lateinit var image: ImageView
    //imagem da roleta
    private lateinit var roulette: ImageView
    //campos de texto
    //nome de utilizador
    private lateinit var userPlace: TextView
    //créditos do utilizador
    private lateinit var cPlace: TextView
    //botão de perfil
    private lateinit var profPic: ImageButton
    //campo de texto para inserir a aposta
    private lateinit var betValue: EditText

    //botões da roleta
    //apostar na cor preta
    private lateinit var btBlack : Button
    //apostar na cor vermelha
    private lateinit var btRed : Button
    //apostar na cor verde
    private lateinit var btGreen : Button
    //botão para girar a roleta
    private lateinit var btSpin : Button
    //botões de configurações
    //botão de ajuda
    private lateinit var btHelp : ImageButton
    //botão de definições
    private lateinit var btSettings : ImageButton
    //botão de mudar o sensor acelerometro
    private lateinit var btChangeSensor : ImageButton
    //botão de mudar a vibração
    private lateinit var btChangeVib : ImageButton

    //lista dos botões das apostas
    private lateinit var betButtons : List<Button>

    //variáveis que guardam as apostas
    //guarda aposta no preto
    private var betBlack: Boolean = false
    //guarda aposta no vermelho
    private var betRed: Boolean = false
    //guarda aposta no verde
    private var betGreen: Boolean = false
    //guarda o botão selecionado
    private var selectBt: Button? = null

    //variáveis de configurações
    //guarda se os botões das definições estão a ser mostrados
    private var defOn: Boolean = false
    //guarda se o sensor acelerometro está ligado
    private var acOn: Boolean = true
    //guarda se a vibração está ligada
    private var vibOn: Boolean = true

    //variáveis para o sensor acelerometro
    private lateinit var sensorManager: SensorManager
    //variáveis para a vibração
    private var vibrator: Vibrator? = null

    /**
     * Função que cria a página da roleta
     */
    @SuppressLint("NewApi", "WrongThread", "SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //definição do layout da página
        setContentView(R.layout.roleta)


        //desativar o modo escuro, no modo noturno o sensor pode não ser lido ou a sua leitura não ser tão precisa
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        //configurações do sensor acelerómetro
        setUpSensor()


        //Mostrar Gif para girar a roleta
        val source :ImageDecoder.Source = ImageDecoder.createSource(resources, R.drawable.spin_win)
        val drawable : Drawable = ImageDecoder.decodeDrawable(source)
        image = findViewById(R.id.imageViewResult)
        image.setImageDrawable(drawable)
        (drawable as? AnimatedImageDrawable)?.start()

        //Mostrar o Gif da roleta
        val sourceR :ImageDecoder.Source = ImageDecoder.createSource(resources, R.drawable.roleta_gif)
        val drawableR : Drawable = ImageDecoder.decodeDrawable(sourceR)
        roulette = findViewById(R.id.rouletteWheel)
        roulette.setImageDrawable(drawableR)
        (drawableR as? AnimatedImageDrawable)?.start()

        //identificação dos botões
         btBlack        = findViewById(R.id.btBlack)
         btRed          = findViewById(R.id.btRed)
         btGreen        = findViewById(R.id.btGreen)
         btSpin         = findViewById(R.id.btSpin)
         btHelp         = findViewById(R.id.btHelp)
         btSettings     = findViewById(R.id.btDef)
         btChangeSensor = findViewById(R.id.btAc)
         btChangeVib    = findViewById(R.id.btVib)
         userPlace      = findViewById(R.id.usernamePlace)
         cPlace         = findViewById(R.id.userCredits)
         betValue       = findViewById(R.id.betValue)
         profPic        = findViewById(R.id.profilePicture)

        //variáveis para mudar a cor da limites dos botões
        val bordaBl = GradientDrawable()
        val bordaRd = GradientDrawable()
        val bordaGn = GradientDrawable()

        //leitura do nome de utilizador do ficheiro e colocação no campo de texto
        userPlace.setText(readFromFile("user.txt"))
        //leitura dos créditos do utilizador do ficheiro e colocação no campo de texto
        cPlace.setText("$" + readFromFile("creditos.txt"))

        //lista dos botões de apostas
        betButtons = listOf(btBlack, btRed, btGreen)
        //função que permite bloquear os restantes botões quando um é acionado
        fun betButtonSelected(bt: Button) {
            if (bt == selectBt) {
                betButtons.forEach { it.isEnabled = true }
                selectBt = null
            } else {
                betButtons.forEach { it.isEnabled = it == bt }
                selectBt = bt
            }
        }

        //função que dá reset a todas as variáveis que sejam botões
         fun resetBt() {
            betButtons.forEach { it.isEnabled = true }
            selectBt = null
            betBlack = false
            betRed = false
            betGreen = false
            btBlack.setBackgroundColor(Color.BLACK)
            btRed.setBackgroundColor(Color.RED)
            btGreen.setBackgroundColor(Color.GREEN)
        }

        //Quando carregado, abre a página dos créditos
        cPlace.setOnClickListener {
            //abre a página dos créditos
            val intent = Intent(this, CreditosActivity::class.java)
            startActivity(intent);
        }

        //Quando carregado no botão, abre a página de perfil
        profPic.setOnClickListener{
            //abre a página de perfil
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        //Quando carregado no botão, implementa a função betButtonSelected(bt: Button) e muda a cor do limite do botão
        btBlack.setOnClickListener{
            //função que permite bloquear os restantes botões quando este é acionado
            betButtonSelected(btBlack)
            //muda a cor do limite do botão conforme este já tenha sido carregado ou não
            if (betBlack == false) {
                betBlack = true
                bordaBl.setColor(Color.BLACK)
                bordaBl.setStroke(10, Color.WHITE)
                bordaBl.cornerRadius = 10f
                btBlack.background = bordaBl
            } else {
                betBlack = false
                bordaBl.setColor(Color.BLACK)
                bordaBl.setStroke(0, Color.WHITE)
                bordaBl.cornerRadius = 10f
                btBlack.background = bordaBl
            }
        }

        //Quando carregado no botão, implementa a função betButtonSelected(bt: Button) e muda a cor do limite do botão
        btRed.setOnClickListener{
            //função que permite bloquear os restantes botões quando este é acionado
            betButtonSelected(btRed)
            //muda a cor do limite do botão conforme este já tenha sido carregado ou não
            if (betRed == false) {
                betRed = true
                bordaRd.setColor(Color.RED)
                bordaRd.setStroke(10, Color.WHITE)
                bordaRd.cornerRadius = 10f
                btRed.background = bordaRd
            } else {
                betRed = false
                bordaRd.setColor(Color.RED)
                bordaRd.setStroke(0, Color.WHITE)
                bordaRd.cornerRadius = 10f
                btRed.background = bordaRd
            }
        }

        //Quando carregado no botão, implementa a função betButtonSelected(bt: Button) e muda a cor do limite do botão
        btGreen.setOnClickListener{
            //função que permite bloquear os restantes botões quando este é acionado
            betButtonSelected(btGreen)
            //muda a cor do limite do botão conforme este já tenha sido carregado ou não
            if (betGreen == false) {
                betGreen = true
                bordaGn.setColor(Color.GREEN)
                bordaGn.setStroke(10, Color.WHITE)
                bordaGn.cornerRadius = 10f
                btGreen.background = bordaGn
            } else {
                betGreen = false
                bordaGn.setColor(Color.GREEN)
                bordaGn.setStroke(0, Color.WHITE)
                bordaGn.cornerRadius = 10f
                btGreen.background = bordaGn
            }
        }

        //Quando carregado no botão, abre a um pop-up com ajuda
        btHelp.setOnClickListener{
            //abre a um pop-up com ajuda
            showInstructionsDialog()
        }

        //Quando carregado no botão, mostra ou esconde os botões de configurações
        btSettings.setOnClickListener{
            //altera a variável que guarda se os botões das definições estão a ser mostrados
            defOn = !defOn
            //mostra ou esconde os botões de configurações
            if (defOn == true) {
                btChangeSensor.visibility = View.VISIBLE
                btChangeVib.visibility = View.VISIBLE
            } else {
                btChangeSensor.visibility = View.INVISIBLE
                btChangeVib.visibility = View.INVISIBLE
            }
        }

        //Quando carregado no botão, muda o sensor acelerometro
        btChangeSensor.setOnClickListener{
            //se o sensor estiver desligado um botão que substitui a sua função aparece
            if(acOn == true){
                acOn = false
                Toast.makeText(this, "Acelerómetro Desligado!", Toast.LENGTH_SHORT).show()
                btSpin.visibility = View.VISIBLE
            } else {
                acOn = true
                Toast.makeText(this, "Acelerómetro Ligado!", Toast.LENGTH_SHORT).show()
                btSpin.visibility = View.INVISIBLE
            }
        }

        //Quando carregado no botão, liga ou desliga a vibração
        btChangeVib.setOnClickListener{
            //se a vibração estiver desligada, o telemóvel não vibrará
            if(vibOn == true){
                vibOn = false
                Toast.makeText(this, "Vibração Desligada!", Toast.LENGTH_SHORT).show()
            } else {
                vibOn = true
                Toast.makeText(this, "Vibração Ligada!", Toast.LENGTH_SHORT).show()
            }
        }

        //Quando carregado no botão, gira a roleta e verifica se venceu
        btSpin.setOnClickListener{
            //gerar um número entre 1 e 10
            //1, 3, 6, 8 - > preto
            //2, 4, 7, 9 - > vermelho
            //5, 10      - > verde

            //leitura do valor da aposta
            val bet = betValue.text.toString().toInt()
            //leitura dos créditos do utilizador
            var credits  = readFromFile("creditos.txt").toInt()
            //verifica se existe uma aposta feita
            if (betBlack == false && betRed == false && betGreen == false){
                        Toast.makeText(this, "Por favor, faça a sua aposta!", Toast.LENGTH_SHORT).show()
            } else {
                //verifica se o utilizador tem créditos suficientes
                if (bet <= credits) {
                    //atualiza os créditos do utilizador
                    credits -= bet
                    //escreve os créditos atualizados no ficheiro
                    writeToFile("creditos.txt", credits.toString())
                    //coloca os créditos atualizados no campo de texto
                    cPlace.setText("$" + readFromFile("creditos.txt"))
                    //gera um número entre 1 e 10
                    var randNum = Random.nextInt(10) + 1
                    //gira a roleta
                    spinRoulette(randNum)
                    checkWin(checkColor(randNum), bet)
                }  else {
                    Toast.makeText(this, "Não tens Creditos Suficientes", Toast.LENGTH_SHORT).show()
                }

            }
            //reset dos botões
            resetBt()
        }
    }



    /*                ROLETA                               */

    /**
     * Função que atribui uma imagem da roleta ao resultado
     * @param n número que determina a imagem
     */
    private fun spinRoulette(n : Int) {
        //atribuição da imagem
        var imageToRoulette = when (n) {
            1 -> R.drawable.roleta1
            2 -> R.drawable.roleta2
            3 -> R.drawable.roleta3
            4 -> R.drawable.roleta4
            5 -> R.drawable.roleta5
            6 -> R.drawable.roleta6
            7 -> R.drawable.roleta7
            8 -> R.drawable.roleta8
            9 -> R.drawable.roleta9
            10 -> R.drawable.roleta10
            else -> R.drawable.roleta
        }
        //definição da imagem
        roulette.setImageResource(imageToRoulette)
    }

    /**
     * Função que verifica a cor do número
     * @param n número que determina a cor
     */
    private fun checkColor(n : Int) : String {
        //atribuição da cor pelo número
        if (n == 1 || n == 3 || n == 6 || n == 8) {
            return "preto"
        } else{
            if (n == 2 || n == 4 || n == 7 || n == 9) {
                return "vermelho"
            } else {
                return "verde"
            }
        }
    }

    /**
     * Função que verifica se o utilizador venceu
     * @param str cor do número
     * @param betV valor da aposta
     */
    @RequiresApi(Build.VERSION_CODES.P)
    private fun checkWin(str : String, betV: Int) {
        //verifica se a cor do número é a mesma que a apostada (preto e vermelho)
        if ((str == "preto" && betBlack == true) || (str == "vermelho" && betRed == true)) {
            //mostra a imagem do resultado
            val sourceRes :ImageDecoder.Source = ImageDecoder.createSource(
                resources, R.drawable.win
            )
            val drawableRes : Drawable = ImageDecoder.decodeDrawable(sourceRes)
            image.setImageDrawable(drawableRes)
            (drawableRes as? AnimatedImageDrawable)?.start()
            //se a vibração estiver ligada, vibrar
            if (vibOn == true) {
                shake(1500, 50)
            }
            //leitura dos creditos do utilizador do ficheiro
            var credits  = readFromFile("creditos.txt").toInt()
            //soma os créditos
            credits += betV * 2
            //escreve os créditos atualizados no ficheiro
            writeToFile("creditos.txt", credits.toString())
            //coloca os créditos atualizados no campo de texto
            cPlace.setText(readFromFile("creditos.txt"))
        } else {
                //verifica se a cor do número é a mesma que a apostada (verde)
                if (str == "verde" && betGreen == true){
                    //mostra a imagem do resultado
                    val sourceRes :ImageDecoder.Source = ImageDecoder.createSource(
                        resources, R.drawable.jackpot
                    )
                    val drawableRes : Drawable = ImageDecoder.decodeDrawable(sourceRes)
                    image.setImageDrawable(drawableRes)
                    (drawableRes as? AnimatedImageDrawable)?.start()
                    //se a vibração estiver ligada, vibrar
                    if (vibOn == true) {
                        shake(3000, 200)
                    }
                    //leitura dos creditos do utilizador do ficheiro
                    var credits  = readFromFile("creditos.txt").toInt()
                    //soma os créditos
                    credits += betV * 5
                    //escreve os créditos atualizados no ficheiro
                    writeToFile("creditos.txt", credits.toString())
                    //coloca os créditos atualizados no campo de texto
                    cPlace.setText("$" + readFromFile("creditos.txt"))
                } else {
                        //mostra a imagem do resultado
                        val sourceRes :ImageDecoder.Source = ImageDecoder.createSource(
                            resources, R.drawable.lose
                        )
                        val drawableRes : Drawable = ImageDecoder.decodeDrawable(sourceRes)
                        image.setImageDrawable(drawableRes)
                        (drawableRes as? AnimatedImageDrawable)?.start()
                }
            }
        }


     /*                 SENSOR ACELERÓMETRO                     */
    /**
     * Função que inicializa o sensor acelerometro
     */
    private fun setUpSensor() {
        //inicialização do sensor manager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        //inicialização do sensor acelerometro
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { sensor ->
            if (acOn) {
                sensorManager.registerListener(
                    this,
                    sensor,
                    SensorManager.SENSOR_DELAY_UI,
                    SensorManager.SENSOR_DELAY_UI
                )
            } else {
                //se o sensor estiver desligado, o sensor não é lido
                sensorManager.unregisterListener(this)
            }
        }
    }

    /**
     * Função que lê os dados do sensor acelerometro
     * @param event dados do sensor
     */
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onSensorChanged(event: SensorEvent?) {
        //verifica se o sensor acelerometro está ligado e se o evento é do dado pelo acelerómetro
        if (!acOn || event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return
        //leitura dos dados do sensor
        val sides = event.values[0]
        val upDown = event.values[1]

        //escolha da sensibilidade do movimento e do tempo entre leituras
        if ((sides > 15 || sides < -15 || upDown > 15 || upDown < -15) && System.currentTimeMillis() - lastSensorTrigger > 1000) {
            //atualização da última leitura do sensor
            lastSensorTrigger = System.currentTimeMillis()

            //leitura do valor da aposta
            val bet = betValue.text.toString().toInt()
            //leitura dos créditos do utilizador
            var credits  = readFromFile("creditos.txt").toInt()
            //verifica se existe uma aposta feita
            if (!betBlack && !betRed && !betGreen) {
                    Toast.makeText(this, "Por favor, faça a sua aposta!", Toast.LENGTH_SHORT).show()
                } else {
                    //verifica se o utilizador tem créditos suficientes
                    if (bet <= credits) {
                        //atualiza os créditos do utilizador
                        credits -= bet
                        //escreve os créditos atualizados no ficheiro
                        writeToFile("creditos.txt", credits.toString())
                        //coloca os créditos atualizados no campo de texto
                        cPlace.setText("$" + readFromFile("creditos.txt"))
                        //gera um número entre 1 e 10
                        val randNum = Random.nextInt(10) + 1
                        //gira a roleta
                        spinRoulette(randNum)
                        checkWin(checkColor(randNum), bet)
                    } else {
                        Toast.makeText(this, "Não tens Creditos Suficientes", Toast.LENGTH_SHORT).show()
                    }
                }



        }
    }

    // Variável global para evitar múltiplos acionamentos em sequência
    private var lastSensorTrigger: Long = 0

    /**
     * Função não utilizada do sensor
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {return}

    /**
     * Função executada quando a activity é destruida
     */
    override fun onDestroy() {
        super.onDestroy()
        //desligar o sensor acelerometro
        sensorManager.unregisterListener(this)

        //leitura do nome de utilizador do ficheiro e atribuir à variável
        val username = readFromFile("user.txt")
        //leitura dos créditos do utilizador do ficheiro e atribuir à variável
        val creditStr  = readFromFile("creditos.txt").toInt()
        //atualização dos créditos na API
        updateUserCredits(username, creditStr){ sucesso ->
            if (sucesso) {
                Toast.makeText(this, "Créditos atualizados!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Erro ao atualizar créditos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Função que aciona a vibração
     */
    private fun shake(millis: Long, intensity: Int) {
        //verifica a versão do android se é compativel com vibração
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //ligar a vibração
            vibrator?.vibrate(VibrationEffect.createOneShot(millis, intensity))
        }
    }

    /**
     * Função que mostra um pop-up com as instruções
     */
    private fun showInstructionsDialog() {
        //criação do pop-up
        val builder = AlertDialog.Builder(this)

        //definição do pop-up
        builder.setTitle("Como Jogar!")
            .setMessage("Como Jogar! \n\n" +
                    "1. Escolha quanto quer apostar.\n" +
                    "2. Faça uma aposta selecionando uma das três cores (preto, vermelho, verde).\n" +
                    "3. Agite o telemóvel.\n" +
                    "4. Veja o resultado!!\n\n" +
                    "NÃO SE ESQUEÇA DE TERMINAR SESSÃO PARA GUARDAR OS SEUS CRÉDITOS!\n " +
                    "Caso contrário o seus créditos não serão atualizados para a próxma vez que jogar\n\n" +
                    "Pode desativar a vibração e o acelerómetro nas definições")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }

        builder.create().show()

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


}




