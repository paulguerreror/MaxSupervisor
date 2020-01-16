package com.neobit.maxsupervisor

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.neobit.maxsupervisor.Utils.*
import com.neobit.maxsupervisor.adapters.ClientePuntoAdapter
import com.neobit.maxsupervisor.adapters.NovedadDetalleAdapter
import com.neobit.maxsupervisor.data.model.NovedadDetalle
import com.neobit.maxsupervisor.data.model.Punto
import com.neobit.maxsupervisor.ui.Informes.InformeActivity
import com.neobit.maxsupervisor.ui.Puntos.ClientePuntoFragment
import com.neobit.maxsupervisor.ui.Puntos.InventarioFragment
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.cliente_punto_fragment.*
import kotlinx.android.synthetic.main.fragment_novedades.contentView
import kotlinx.android.synthetic.main.fragment_novedades.progressView
import kotlinx.android.synthetic.main.novedad_detalles_fragment.*

import org.json.JSONObject
import java.util.HashMap

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var prefs: SharedPreferences
    var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        fusedLocationClient = LocationServices.
            getFusedLocationProviderClient(this)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
       // getLocation()
        getLocationUpdates()

        if(!prefs.contains("id_ronda_supervisor")){
            getRonda()
        }

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_cliente_puntos,R.id.nav_informe,R.id.nav_incumplimiento,R.id.nav_novedad,R.id.nav_map,R.id.nav_avisos
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if(result != null){
            if(result.contents != null){
                Log.d("qr",result.contents)
                scanCliente(result.contents)
            } else {
                Log.d("qr","scan failed")
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
        /*
        val anonymousFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        if (anonymousFragment is ClientePuntoFragment) {
            Log.d("procces","entro")
            anonymousFragment.onActivityResult(requestCode, resultCode, data)
        }
        super.onActivityResult(requestCode, resultCode, data)
*/
    }

    fun scanCliente2(codigo:String){
        prefs.edit().putString("id_cliente","4").apply()
        prefs.edit().putString("id_ronda_historial_detalle_sup","2").apply()
        val progressView = findViewById<ProgressBar>(R.id.progressView)
        val contenView = findViewById<LinearLayout>(R.id.contentView)
        progressView.visibility = View.GONE
        contenView.visibility = View.VISIBLE

    }

    fun scanCliente(codigo:String){
        if (!NetworkUtils.isConnected(this)) {
            Toast.makeText(this, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val queue = Volley.newRequestQueue(this)
            val id_ronda_historial_sup = prefs.getString("id_ronda_historial_sup","")

            val URL = "${Utils.URL_SERVER}supervisor/rondas/$id_ronda_historial_sup/paradas"
            val stringRequest = object : StringRequest(Method.POST, URL, Response.Listener<String> { response ->
                try {
                    var strResp = response.toString()
                    Log.d("URL",strResp)
                    val jsonObj: JSONObject = JSONObject(strResp)

                    val parada = jsonObj.getJSONObject("parada")
                    val cliente = parada.getJSONObject("cliente")
                    val id_cliente= cliente.get("id_cliente").toString()
                    val id = jsonObj.get("rondas_historial_detalle_sup").toString()
                    prefs.edit().putString("id_cliente",id_cliente).apply()
                    prefs.edit().putString("id_ronda_historial_detalle_sup",id).apply()
                   // val fragment = supportFragmentManager.findFragmentById(R.id.nav_cliente_puntos) as ClientePuntoFragment
                   // fragment.displayView()
                    Toast.makeText(this, jsonObj.get("message").toString(), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }, Response.ErrorListener { error ->
                try {
                    error.printStackTrace()
                    Toast.makeText(this, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers.put("token", prefs.getString("api_key", "")!!)
                    return headers
                }
                override fun getParams(): MutableMap<String, String> {
                    val parameters = HashMap<String, String>()
                    parameters["codigo"] = codigo
                    parameters["latitud"] = prefs.getString("lat", "")!!
                    parameters["longitud"] = prefs.getString("lon", "")!!
                    return parameters
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            queue.add(stringRequest)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun navigateCrearIncumplimiento(view: View){
        val id= view.getTag(R.id.id).toString()
        //Log.d("Id",id)
        prefs.edit().putString("id_guardia",id).apply()
        Navigation.findNavController(view!!).navigate(R.id.nav_crear_incumplimiento)
    }

    fun navigatePuntoInventario(view: View){
        val id= view.getTag(R.id.id).toString()
        //Log.d("Id",id)
        prefs.edit().putString("id_punto_inventario",id).apply()
        Navigation.findNavController(view!!).navigate(R.id.nav_punto_inventario)
    }

    fun navigatePuntoNovedad(view: View){
        val id= view.getTag(R.id.id).toString()
        //Log.d("Id",id)
        prefs.edit().putString("id_punto",id).apply()
        Navigation.findNavController(view!!).navigate(R.id.nav_punto_novedades)
    }

    fun navigatePuntoTarea(view: View){
        val id= view.getTag(R.id.id).toString()
        //Log.d("Id",id)
        prefs.edit().putString("id_punto",id).apply()
        Navigation.findNavController(view!!).navigate(R.id.nav_tareas)
    }

    fun markTarea(view: View){
        Log.d("tarea",view.getTag(R.id.id).toString())
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.app_name)).setMessage(R.string.alert_end_tarea)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.si)) { dialog, _ ->
                    dialog.cancel()
                    endTarea(view.getTag(R.id.id).toString())
                }
                .setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.cancel() }
            val alert = builder.create()
            alert.show()
            true
    }


    fun endTarea(id: String){
        if (!NetworkUtils.isConnected(applicationContext)) {
            Toast.makeText(applicationContext, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else{
            val queue = Volley.newRequestQueue(applicationContext)
            val URL = "${Utils.URL_SERVER}tareas/$id"
            val stringRequest = object : StringRequest(Method.POST, URL, Response.Listener<String> { response ->
                try {

                    var strResp = response.toString()
                    val jsonObj: JSONObject = JSONObject(strResp)
                    Log.d(URL,strResp)
                    val message = jsonObj.get("message").toString()
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(applicationContext, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }, Response.ErrorListener { error ->
                try {
                    error.printStackTrace()
                    Toast.makeText(applicationContext, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(applicationContext, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers.put("token", prefs.getString("api_key", "")!!)
                    return headers
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            queue.add(stringRequest)
        }
    }



    fun navigateNovedadDetalles(view: View){
        val id= view.getTag(R.id.id).toString()
        //Log.d("Id",id)
        prefs.edit().putString("id_novedad",id).apply()
        Navigation.findNavController(view!!).navigate(R.id.nav_novedad_detalles)
    }

    fun navigateEditInforme(view: View){
        val id= view.getTag(R.id.id).toString()
        //Log.d("Id",id)
        prefs.edit().putString("id_informe",id).apply()
        startActivity(Intent(this, InformeActivity::class.java))
        //finish()
        //Navigation.findNavController(view!!).navigate(R.id.nav_crear_informe)
    }

    fun checkAviso(view: View){
        val id= view.getTag(R.id.id).toString()
        Log.d("aviso",id)
    }

     fun getRonda() {
        if (!NetworkUtils.isConnected(this)) {
            Toast.makeText(this, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val queue = Volley.newRequestQueue(this)
            val URL = "${Utils.URL_SERVER}rondas"
            val stringRequest = object : StringRequest(Method.GET, URL, Response.Listener<String> { response ->
                try {
                    var strResp = response.toString()
                    val jsonObj: JSONObject = JSONObject(strResp)
                    Log.d("$URL",strResp)
                    val id_ronda_supervisor = jsonObj.getJSONObject("rondas").get("id_ronda_supervisor").toString()
                    prefs.edit().putString("id_ronda_supervisor",id_ronda_supervisor).apply()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error ->
                try {
                    error.printStackTrace()
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    Toast.makeText(this, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    Toast.makeText(this, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers.put("token", prefs.getString("api_key", "")!!)
                    return headers
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            queue.add(stringRequest)
        }
    }


    fun getLocationUpdates()
    {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext!!)
            locationRequest = LocationRequest()
            locationRequest.interval = 50000
            locationRequest.fastestInterval = 50000
            locationRequest.smallestDisplacement = 170f // 170 m = 0.1 mile
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY //set according to your app function
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    locationResult ?: return

                    if (locationResult.locations.isNotEmpty()) {
                        // get latest location
                        val location =
                            locationResult.lastLocation
                        // use your location object
                        // get latitude , longitude and other info from this
                        prefs.edit().putString("lat", location.latitude.toString()).apply()
                        prefs.edit().putString("lon", location.longitude.toString()).apply()
                        Log.d("Coordenadas:", "${prefs.getString("lat","")}, ${prefs.getString("lon","")}")
                        saveLocation()
                        //Toast.makeText(applicationContext,"Nuevas Coordenadas: ${prefs.getString("lat","")}, ${prefs.getString("lon","")}" , Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun saveLocation() {
            if (!NetworkUtils.isConnected(applicationContext)) {
                Toast.makeText(applicationContext, R.string.error_internet2, Toast.LENGTH_LONG).show()
            } else{
                val queue = Volley.newRequestQueue(applicationContext)
                val URL = "${Utils.URL_SERVER}ubicacion"
                val stringRequest = object : StringRequest(Method.POST, URL, Response.Listener<String> { response ->
                    try {
                        var strResp = response.toString()
                        Log.d("$URL",strResp)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(applicationContext, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    }
                }, Response.ErrorListener { error ->
                    try {
                        error.printStackTrace()
                        Toast.makeText(applicationContext, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(applicationContext, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    }
                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers.put("token", prefs.getString("api_key", "")!!)
                        return headers
                    }
                    override fun getParams(): MutableMap<String, String> {
                        val parameters = HashMap<String, String>()
                        parameters["longitud"] = prefs.getString("lon","")
                        parameters["latitud"] = prefs.getString("lat","")
                        return parameters
                    }
                }
                stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                queue.add(stringRequest)
            }
    }

    fun alertNovedadDetallePrivacidad(view: View){
        //val data  = view.getTag(R.id.id).toString()
        var cliente =view.getTag(R.id.cliente).toString().toInt()
        if(cliente==1)
            cliente = 0
        else
            cliente = 1
        val id = view.getTag(R.id.id).toString().toInt()
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.app_name)).setMessage(R.string.alert_change_privacy)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.si)) { dialog, _ ->
                dialog.cancel()
                changeNovedadDetallePrivacy(cliente,id)
            }
            .setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
        true
    }

    fun getDetallesByNovedad2() {
        val rvNovedadDetalles = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvNovedadDetalles)
        rvNovedadDetalles.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        if (!NetworkUtils.isConnected(this)) {
            Toast.makeText(this, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val novedaddetalles = ArrayList<NovedadDetalle>()
            val queue = Volley.newRequestQueue(this)
            val URL = "${Utils.URL_SERVER}novedades/${prefs.getString("id_novedad","")}"
            val stringRequest = object : StringRequest(Method.GET, URL, Response.Listener<String> { response ->
                try {
                    var strResp = response.toString()
                    Log.d("$URL",strResp)
                    val jsonObj: JSONObject = JSONObject(strResp)
                    val novedad = jsonObj.getJSONObject("novedades")
                    val detalle = novedad.getJSONArray("detalle")
                    cardprivacidad.setTag(novedad.getInt("cliente"))
                    txtDetalleFecha.text = novedad.getString("fecha_creacion")
                    if(novedad.getString("imagen") == ""){
                        imgDetalleDescripcion.visibility = View.GONE
                    }else{
                        Picasso.get().load(Utils.URL_MEDIA + novedad.getString("imagen")).error(R.drawable.placeholder).placeholder(R.drawable.placeholder).noFade().into(imgDetalleDescripcion)
                    }
                    txtDetalleDescripcion.text = novedad.getString("descripcion")
                    txtDetalleNombre.text = novedad.getJSONObject("creador").getString("nombres")
                    if(novedad.getInt("cliente") == 1){
                        cardprivacidad.setCardBackgroundColor(Color.parseColor("#26dad2"))
                        txtprivacidad.text = "Público"
                    }else{
                        cardprivacidad.setCardBackgroundColor(Color.parseColor("#ef5350"))
                        txtprivacidad.text = "Privado"
                    }
                    for (i in 0 until detalle.length()) {
                        var jsonInner: JSONObject = detalle.getJSONObject(i)
                        val id_novedad_detalle = jsonInner.get("id_novedad_detalle").toString().toInt()
                        val nombres = jsonInner.getJSONObject("id_creador").getString("nombres")
                        val tipo = jsonInner.get("tipo").toString()
                        val descripcion = jsonInner.get("descripcion").toString()
                        val imagen = jsonInner.get("imagen").toString()
                        val fecha = jsonInner.get("fecha_creacion").toString()
                        val cliente = jsonInner.getInt("cliente")

                        Log.d("add","$id_novedad_detalle|$cliente|$nombres|$tipo|$descripcion|$imagen|$fecha|")
                        novedaddetalles.add(NovedadDetalle(id_novedad_detalle,cliente,nombres,tipo,descripcion,imagen,fecha))
                    }

                    val adapter = NovedadDetalleAdapter(novedaddetalles)
                    rvNovedadDetalles.adapter = adapter
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    e.printStackTrace()
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                }
            }, Response.ErrorListener { error ->
                try {
                    error.printStackTrace()
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    Toast.makeText(this, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    Toast.makeText(this, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers.put("token", prefs.getString("api_key", "")!!)
                    return headers
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            queue.add(stringRequest)
        }
    }

    private fun changeNovedadDetallePrivacy(cliente: Int,id:Int) {
        if(prefs.contains("id_novedad")){
            if (!NetworkUtils.isConnected(applicationContext)) {
                Toast.makeText(applicationContext, R.string.error_internet2, Toast.LENGTH_LONG).show()
            } else{
                progressView.visibility = View.VISIBLE
                contentView.visibility = View.GONE
                val queue = Volley.newRequestQueue(applicationContext)
                val URL = "${Utils.URL_SERVER}novedades/detalle/$id"
                val stringRequest = object : StringRequest(Method.PUT, URL, Response.Listener<String> { response ->
                    try {
                        var strResp = response.toString()
                        Log.d("$URL",strResp)
                        val jsonObj: JSONObject = JSONObject(strResp)
                        val message = jsonObj.get("message").toString()
                        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                        getDetallesByNovedad2()
                    } catch (e: Exception) {
                        progressView.visibility = View.GONE
                        contentView.visibility = View.VISIBLE
                        e.printStackTrace()
                        Toast.makeText(applicationContext, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    }
                }, Response.ErrorListener { error ->
                    try {
                        progressView.visibility = View.GONE
                        contentView.visibility = View.VISIBLE
                        error.printStackTrace()
                        Toast.makeText(applicationContext, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        progressView.visibility = View.GONE
                        contentView.visibility = View.VISIBLE
                        Toast.makeText(applicationContext, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    }
                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers.put("token", prefs.getString("api_key", "")!!)
                        return headers
                    }
                    override fun getParams(): MutableMap<String, String> {
                        val parameters = HashMap<String, String>()
                        parameters["cliente"] = cliente.toString()
                        return parameters
                    }
                }
                stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                queue.add(stringRequest)
            }
        }else{
            Log.e("error","no novedad")
        }

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

    fun endTurno() {
        if (!NetworkUtils.isConnected(applicationContext)) {
            Toast.makeText(applicationContext, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val queue = Volley.newRequestQueue(applicationContext)
            val URL = "${Utils.URL_SERVER}logout"
            val stringRequest = object : StringRequest(Method.POST, URL, Response.Listener<String> { response ->
                try {
                    prefs.edit().clear().commit()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(applicationContext, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }, Response.ErrorListener { error ->
                try {
                    error.printStackTrace()
                    Toast.makeText(applicationContext, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(applicationContext, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers.put("token", prefs.getString("api_key", "")!!)
                    return headers
                }

                override fun getParams(): MutableMap<String, String> {
                    val parameters = HashMap<String, String>()
                    parameters["lat"] = prefs.getString("lat", "")!!
                    parameters["lon"] = prefs.getString("lon", "")!!
                    return parameters
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            queue.add(stringRequest)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle(getString(R.string.app_name)).setMessage(R.string.alert_end_turno)
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.btn_si)) { dialog, _ ->
                        dialog.cancel()
                        endTurno()
                    }
                    .setNegativeButton(getString(R.string.btn_no)) { dialog, _ -> dialog.cancel() }
                val alert = builder.create()
                alert.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
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
                /*
                Dexter.withActivity(activity)
                    .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(object: PermissionListener {
                        override fun onPermissionGranted(response: PermissionGrantedResponse?) {

                        }
                        override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                            Toast.makeText(activity, R.string.error_permissions, Toast.LENGTH_SHORT).show()
                        }
                        override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
                            token!!.continuePermissionRequest()
                        }
                    }).
                        withErrorListener{ Toast.makeText(activity, R.string.error_permissions, Toast.LENGTH_SHORT).show()}
                    .onSameThread()
                    .check()

                 */
            } else {
                ActivityCompat.requestPermissions(this, perm, PERMISSION_ID)
            }
            return false
        }
        return true
    }

    private fun startLocationUpdates() {
        fusedLocationClient?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        )
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            val anonymousFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            if (anonymousFragment is InventarioFragment) {
                Toast.makeText(this, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
            } else {
                super.onBackPressed()
            }
        }
    }


    // stop location updates
    private fun stopLocationUpdates() {
        fusedLocationClient?.removeLocationUpdates(locationCallback)
    }

    // stop receiving location update when activity not visible/foreground
    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    // start receiving location update when activity  visible/foreground
    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }
}
