package com.newapp.GuidoApp

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class infoActivity : AppCompatActivity() {
    lateinit var riesgoLu: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        session()
    }

    private fun session() {

        lateinit var database: FirebaseDatabase
        lateinit var myRef: DatabaseReference

        val id = FirebaseAuth.getInstance().getCurrentUser()?.getUid()
        database = FirebaseDatabase.getInstance("https://guidoapp-bdb08-default-rtdb.firebaseio.com/")
        myRef = database.getReference(id.toString())

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again

                // whenever data at this location is updated.
                val value =
                    dataSnapshot.getValue(String::class.java)!!
                riesgos(value.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                // Log.w(FragmentActivity.TAG, "Failed to read value.", error.toException())
            }
        })


    }

    fun riesgos(tipo: String){
        riesgoLu = findViewById(R.id.informacionRiesgo)

        riesgoLu.text = tipo.riesgo
        riesgoLu.setTextColor(Color.parseColor("#52AF56"))

    }
}