package com.neobit.maxsupervisor

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import com.beust.klaxon.Klaxon
import com.neobit.maxsupervisor.data.model.User

class SplashActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_splash)
        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        Handler().postDelayed({
            val i = if (!prefs.contains("guardias")) {
                Intent(this@SplashActivity, LoginActivity::class.java)
            } else{
                Intent(this@SplashActivity, MainActivity::class.java)
            }
            startActivity(i)
            finish()
        }, 3000)
    }
}
