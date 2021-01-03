package com.newapp.GuidoApp

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.CaptureActivity
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {
    var valorEscaneado = "" // Variable donde se guarda la informacion del codigo QR
    lateinit var estado: TextView
    lateinit var database: FirebaseDatabase
    lateinit var myRef: DatabaseReference
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

        //Boton Estado
        estado = findViewById(R.id.estadoText)
        val button = findViewById<Button>(R.id.estadoBtn)
        button.setOnClickListener { v: View ->
            showMenu(v, R.menu.submenu)
        }

        val id = FirebaseAuth.getInstance().getCurrentUser()?.getUid()
        database = FirebaseDatabase.getInstance("https://guidoapp-bdb08-default-rtdb.firebaseio.com/")
        myRef = database.getReference(id.toString())

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val value =
                    dataSnapshot.getValue(String::class.java)!!
                ActualizarEstado(value.toInt())
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                // Log.w(FragmentActivity.TAG, "Failed to read value.", error.toException())
            }
        })
    }

    fun ActualizarEstado(tipo: Int){

        //SIN CONTAGIO
        if(tipo == 1){
            estado.text = "Sin Contagio"
            estado.setTextColor(Color.parseColor("#52AF56"))
        }
        //INTERACCION CON POSITIVO
        if(tipo == 2){
            estado.text = "Posible Contagio"
            estado.setTextColor(Color.parseColor("#FFEB3B"))
        }
        //POSITIVO
        if(tipo == 3){
            estado.text = "Positivo"
            estado.setTextColor(Color.parseColor("#F41B1B"))
        }
    }

    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(this!!, v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {
                R.id.option_1 -> {
                    myRef.setValue("1")
                    ActualizarEstado(1)
                }
                R.id.option_2 -> {
                    myRef.setValue("2")
                    ActualizarEstado(2)
                }
                R.id.option_3 -> {
                    myRef.setValue("3")
                    ActualizarEstado(3)
                }
            }

            true
        })

        popup.setOnDismissListener {
            // Respond to popup being dismissed.
        }
        // Show the popup menu.
        popup.show()
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

        qrButton.setOnClickListener {
            scanQrCode()
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

    private fun scanQrCode(){
        val integrator = IntentIntegrator(this).apply {
            captureActivity = CaptureActivity::class.java
            setOrientationLocked(false)
            setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
            setPrompt("Escanenado Codigo")
        }
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if(result != null){
            if(result.contents == null) Toast.makeText(this, "Cancelado", Toast.LENGTH_LONG).show()
            else{
                Toast.makeText(this, "Scaneado: " + result.contents, Toast.LENGTH_LONG).show()
                valorEscaneado = result.contents
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}