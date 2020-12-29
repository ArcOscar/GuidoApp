package com.newapp.GuidoApp

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Setup
        val bundle = intent.extras
        val email = bundle?.getString("email")
        setup(email ?: "")

        // Guardando datos
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.apply()
    }

    private fun setup(email: String) {
        title = "Inicio"
        usedEmailText.text = email

        // Borrando los datos para cierre de sesión
        signOutButton.setOnClickListener {
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }

        // Borrando los datos de la bd
        deleteButton.setOnClickListener {
            showAlert()
        }
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Eliminar cuenta")
        builder.setMessage("Al eliminar la cuenta se perderá toda la información relevante al usuario" +
                            " y se regresará a la pantalla de inicio de sesión.")
        builder.setPositiveButton("Eliminar", object: DialogInterface.OnClickListener {
            override fun onClick(dialog:DialogInterface, id:Int) {
                val user = Firebase.auth.currentUser!!

                user.delete()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@HomeActivity, "Cuenta Eliminada", Toast.LENGTH_LONG)
                                .show()

                            val intent = Intent(this@HomeActivity, AuthActivity::class.java)
                            startActivity(intent)
                        }
                        else
                            Toast.makeText(this@HomeActivity, task.exception?.message, Toast.LENGTH_LONG).show()
                    }
            }
        })
        builder.setNegativeButton("Cancelar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}