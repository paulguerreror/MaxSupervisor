package com.neobit.maxsupervisor

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.neobit.maxsupervisor.Utils.Utils
import com.neobit.maxsupervisor.data.model.Horario
import com.neobit.maxsupervisor.data.model.Punto
import com.neobit.maxsupervisor.data.model.User
import com.google.android.gms.tasks.OnCompleteListener
import com.neobit.maxsupervisor.Utils.NetworkUtils
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONException
import com.google.firebase.iid.FirebaseInstanceId

import org.json.JSONObject
import org.xml.sax.Parser
import java.io.ByteArrayOutputStream


class LoginActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences
    private val REQUEST_IMAGE_CAPTURE = 1356
    var fusedLocationClient: FusedLocationProviderClient? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        fusedLocationClient = LocationServices.
            getFusedLocationProviderClient(this)
        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        getLocation()

        getToken()
    }

    fun getToken(){

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("tk", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }
                val token = task.result?.token
                Log.d("firebase", token)
                prefs.edit().putString("token",token).apply()
            })
    }

    fun getLocation(){
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            fusedLocationClient?.lastLocation?.
                addOnSuccessListener(this,
                    {location : Location? ->
                        if(location == null) {
                            Log.e("error","didnt get Loc")
                        } else location.apply {
                            Log.d("coordenadas",location.toString())
                            Log.d("lat",location.latitude.toString())
                            Log.d("lon",location.longitude.toString())
                            prefs.edit().putString("lat", location.latitude.toString()).apply()
                            prefs.edit().putString("lon", location.longitude.toString()).apply()
                        }
                    })
        }
    }


    fun alertEditText(imagen: String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Iniciar Turno").setMessage(R.string.alert_login)
        val view = layoutInflater.inflate(R.layout.dialog_edit_text, null)
        val txtAlert = view.findViewById(R.id.etprofile) as EditText
        builder.setView(view)
        builder.setPositiveButton(android.R.string.yes) { dialog, p1 ->
            val cedula = txtAlert.text.toString()
            authUser(imagen,cedula)
            dialog.dismiss()
        }
        builder.setNegativeButton(android.R.string.no) { dialog, p1 ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun authUser(imagen: String,cedula:String) {
        getLocation()
        if (!NetworkUtils.isConnected(this@LoginActivity)) {
            Toast.makeText(this@LoginActivity, R.string.error_internet, Toast.LENGTH_LONG).show()
        } else {
            progressView.visibility = View.VISIBLE
            contentView.visibility = View.GONE
            val queue = Volley.newRequestQueue(this)
            val stringRequest = object : StringRequest(Method.POST, "${Utils.URL_SERVER}login", Response.Listener<String> { response ->
                try {
                    val json: JsonObject = com.beust.klaxon.Parser.default().parse(StringBuilder(response)) as JsonObject
                    val result = Klaxon().parseFromJsonObject<User>(json.obj("guardias")!!)
                    val supervisor = json.obj("guardias")!!.int("supervisor")
                    if(supervisor == 0){
                        Toast.makeText(this, resources.getString(R.string.error_supervisor), Toast.LENGTH_LONG).show()
                        progressView.visibility = View.GONE
                        contentView.visibility = View.VISIBLE
                    }else{
                        prefs.edit().putString("guardias", Klaxon().toJsonString(result)).apply()
                        prefs.edit().putString("api_key", json.obj("guardias")!!.string("api_key")).apply()

                        startActivity(Intent(this@LoginActivity, SplashActivity::class.java))
                        finish()
                    }
                } catch (e: JSONException) {
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    e.printStackTrace()
                    Toast.makeText(this@LoginActivity, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }, Response.ErrorListener { error ->
                try {
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    error.printStackTrace()
                    val errorMessage = JSONObject(String(error.networkResponse.data)).getString("message")
                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }) {
                override fun getParams(): MutableMap<String, String> {

                    val parameters = HashMap<String, String>()
                    parameters["lon"] = prefs.getString("lon", "")!!
                    parameters["lat"] = prefs.getString("lat", "")!!
                    parameters["token"] = prefs.getString("token", "")!!
                    parameters["codigo"] = cedula
                    parameters["imagen"] = imagen
                    return parameters
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            queue.add(stringRequest)
        }
    }

    val PERMISSION_ID = 42
    private fun checkPermission(vararg perm:String) : Boolean {
        val havePermissions = perm.toList().all {
            ContextCompat.checkSelfPermission(this,it) ==
                    PackageManager.PERMISSION_GRANTED
        }
        if (!havePermissions) {
            if(perm.toList().any {
                    ActivityCompat.
                        shouldShowRequestPermissionRationale(this, it)}
            ) {

            } else {
                ActivityCompat.requestPermissions(this, perm, PERMISSION_ID)
            }
            return false
        }
        return true
    }

    fun dispatchTakePictureIntent(view: View) {
        getLocation()
        Dexter.withActivity(this@LoginActivity)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object: PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                        takePictureIntent.resolveActivity(applicationContext.packageManager)?.also {
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                        }
                    }
                }
                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    Toast.makeText(this@LoginActivity, R.string.error_permissions, Toast.LENGTH_SHORT).show()
                }
                override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
                    token!!.continuePermissionRequest()
                }
            }).
                withErrorListener{ Toast.makeText(this@LoginActivity, R.string.error_permissions, Toast.LENGTH_SHORT).show()}
            .onSameThread()
            .check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                if (data != null) {
                    val imageBitmap = data.extras.get("data") as Bitmap
                    val baos = ByteArrayOutputStream()
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val b = baos.toByteArray()
                    val imgString = Base64.encodeToString(b, Base64.DEFAULT)
                    alertEditText(imgString)
                }
            }
        } catch (e: Exception) {
            Log.e("FileSelectorActivity", "File select error", e)
        }
    }
}
