package com.newapp.GuidoApp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_auth.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.emailEditText
import kotlinx.android.synthetic.main.activity_register.passwordEditText

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //Setup
        setup()
    }

    private fun setup() {
        // Registro de cuenta
        registerBtn.setOnClickListener {
            if (emailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty() && passwordEditText.text.toString() == passwordEditText2.text.toString()) {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                    emailEditText.text.toString(),
                    passwordEditText.text.toString()
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(
                            emailEditText.text.toString(),
                            passwordEditText.text.toString()
                        ).addOnCompleteListener {
                            if (it.isSuccessful) {
                                val id = FirebaseAuth.getInstance().getCurrentUser()?.getUid()
                                val database = FirebaseDatabase.getInstance("https://guidoapp-bdb08-default-rtdb.firebaseio.com/")
                                val myRef = database.getReference(id.toString())
                                myRef.setValue("1")
                                FirebaseAuth.getInstance().signOut()
                            } else {

                            }
                        }
                        showGoodAlert()
                        val intent = Intent(this, AuthActivity::class.java)
                        startActivity(intent)
                    } else {
                        showAlert()
                    }
                }
            } else {
                showPassAlert()
            }
        }
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Hubo un error en la autenticación del usuario")
        builder.setPositiveButton("Ok", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showPassAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Comprueba los campos introducidos")
        builder.setPositiveButton("Ok", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showGoodAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("¡Bien hecho!")
        builder.setMessage("Su cuenta ha sido registrada")
        builder.setPositiveButton("Ok", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}