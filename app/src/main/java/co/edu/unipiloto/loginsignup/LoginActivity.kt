package co.edu.unipiloto.loginsignup

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var etUsuarioLogin: TextInputEditText
    private lateinit var etPasswordLogin: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var btnIrRegistro: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializar la base de datos
        databaseHelper = DatabaseHelper(this)

        // Vincular elementos de la UI
        etUsuarioLogin = findViewById(R.id.etUsuarioLogin)
        etPasswordLogin = findViewById(R.id.etPasswordLogin)
        btnLogin = findViewById(R.id.btnLogin)
        btnIrRegistro = findViewById(R.id.btnIrRegistro)

        // Evento del botón Login
        btnLogin.setOnClickListener { iniciarSesion() }

        // Evento del botón Registrarse
        btnIrRegistro.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun iniciarSesion() {
        val usuario = etUsuarioLogin.text.toString().trim()
        val password = etPasswordLogin.text.toString().trim()

        if (usuario.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Ingrese usuario y contraseña", Toast.LENGTH_SHORT).show()
            return
        }

        // Validar usuario en SQLite
        if (databaseHelper.validarUsuario(usuario, password)) {
            Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
        }
    }
}
