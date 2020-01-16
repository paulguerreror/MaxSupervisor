package com.neobit.maxsupervisor.ui.Puntos

import android.content.Intent
import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.os.Handler

import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import kotlinx.android.synthetic.main.cliente_punto_fragment.*

import com.neobit.maxsupervisor.R
import com.neobit.maxsupervisor.Utils.NetworkUtils
import com.neobit.maxsupervisor.Utils.Utils
import com.neobit.maxsupervisor.adapters.ClientePuntoAdapter
import com.neobit.maxsupervisor.adapters.PuntoAdapter
import com.neobit.maxsupervisor.data.model.Informe
import com.neobit.maxsupervisor.data.model.Punto
import kotlinx.android.synthetic.main.cliente_punto_fragment.contentView
import kotlinx.android.synthetic.main.cliente_punto_fragment.progressView
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_novedades.*
import kotlinx.android.synthetic.main.informes_fragment.*
import kotlinx.android.synthetic.main.inventario_fragment.*
import org.json.JSONObject
import java.util.HashMap

class ClientePuntoFragment : Fragment() {

    private lateinit var prefs: SharedPreferences


    companion object {
        fun newInstance() = ClientePuntoFragment()
    }

    private lateinit var viewModel: ClientePuntoViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.cliente_punto_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(this@ClientePuntoFragment.context!!)
       // prefs.edit().putString("id_cliente","4").apply()
        progressView.visibility = View.VISIBLE
        contentView.visibility = View.GONE

        displayView()

        btnStartRonda.setOnClickListener {
            startRonda()
        }
        btnScanCliente.setOnClickListener {
            progressView.visibility = View.VISIBLE
            contentView.visibility = View.GONE
            run {
                IntentIntegrator(this@ClientePuntoFragment.activity).initiateScan()
            }
            val handler = Handler()
            handler.postDelayed({
                displayView()
            }, 15000)

        }
        btnEndCliente.setOnClickListener {
            endParada()
        }
        btnEndRonda.setOnClickListener {
            val builder = AlertDialog.Builder(this@ClientePuntoFragment.context!!)
            builder.setTitle("Finalizar Ronda").setMessage(R.string.alert_end_ronda)
            val view = layoutInflater.inflate(R.layout.dialog_input, null)
            val txtEndRonda = view.findViewById(R.id.txtInput) as EditText
            builder.setView(view)
            builder.setPositiveButton(android.R.string.yes) { dialog, p1 ->
                val descripcion = txtEndRonda.text.toString()
                endRonda(descripcion)
                dialog.dismiss()
            }
            builder.setNegativeButton(android.R.string.no) { dialog, p1 ->
                dialog.cancel()
            }
            builder.show()

        }
    }

    fun displayView(){
        if(!prefs.contains("id_ronda_supervisor")){

            Log.d("display","4")
            content1.visibility = View.GONE
            content2.visibility = View.GONE
            content3.visibility = View.GONE
            content4.visibility = View.VISIBLE
        }else if(!prefs.contains("id_ronda_historial_sup")){
            Log.d("display","1")

            content1.visibility = View.VISIBLE
            content2.visibility = View.GONE
            content3.visibility = View.GONE
            content4.visibility = View.GONE
        }else if(!prefs.contains("id_ronda_historial_detalle_sup")){
            Log.d("display","2")

            content1.visibility = View.GONE
            content2.visibility = View.VISIBLE
            content3.visibility = View.GONE
            content4.visibility = View.GONE
        }else{
            Log.d("display","3")
            getPuntosByCliente(prefs.getString("id_cliente",""))
            content1.visibility = View.GONE
            content2.visibility = View.GONE
            content3.visibility = View.VISIBLE
            content4.visibility = View.GONE
        }

        progressView.visibility = View.GONE
        contentView.visibility = View.VISIBLE
    }

    private fun getPuntosByCliente(id_cliente:String) {
        if (!NetworkUtils.isConnected(this@ClientePuntoFragment.context!!)) {
            Toast.makeText(this@ClientePuntoFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val puntos = ArrayList<Punto>()
            val queue = Volley.newRequestQueue(this@ClientePuntoFragment.context!!)

            val URL = "${Utils.URL_SERVER}clientes/$id_cliente/puntos"
            val stringRequest = object : StringRequest(Method.GET, URL, Response.Listener<String> { response ->
                try {
                    var strResp = response.toString()
                    val jsonObj: JSONObject = JSONObject(strResp)
                    Log.d("$URL",strResp)

                    val jsonArray = jsonObj.getJSONArray("puntos")
                    for (i in 0 until jsonArray.length()) {
                        var jsonInner: JSONObject = jsonArray.getJSONObject(i)

                        val id_punto = jsonInner.get("id_punto").toString().toInt()
                        val codigo = jsonInner.get("codigo").toString()
                        val latitud = jsonInner.get("latitud").toString()
                        val longitud = jsonInner.get("longitud").toString()

                        Log.d("add","$id_punto,$codigo,$latitud,$longitud")
                        puntos.add(Punto(id_punto,codigo,latitud,longitud))
                    }
                    rvClientePunto.layoutManager = LinearLayoutManager(this@ClientePuntoFragment.context!!,
                        LinearLayoutManager.VERTICAL,false)
                    val adapter = ClientePuntoAdapter(puntos)
                    rvClientePunto.adapter = adapter
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error ->
                try {
                    error.printStackTrace()
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    Toast.makeText(this@ClientePuntoFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    Toast.makeText(this@ClientePuntoFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
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

    fun scanCliente(codigo:String){
        if (!NetworkUtils.isConnected(this@ClientePuntoFragment.context!!)) {
            Toast.makeText(this@ClientePuntoFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val queue = Volley.newRequestQueue(this@ClientePuntoFragment.context!!)
            val ronda_historial = JSONObject(prefs.getString("ronda_historial",""))
            val id_ronda_historial = ronda_historial.get("id_ronda_historial").toString()
            val URL = "${Utils.URL_SERVER}rondas/$id_ronda_historial/paradas"
            val stringRequest = object : StringRequest(Method.POST, URL, Response.Listener<String> { response ->
                try {
                    var strResp = response.toString()
                    Log.d(URL,strResp)
                    val jsonObj: JSONObject = JSONObject(strResp)
                    val ronda = jsonObj.get("detalle").toString()
                    prefs.edit().putString("ronda",ronda).apply()
                    Toast.makeText(this@ClientePuntoFragment.context!!, jsonObj.get("message").toString(), Toast.LENGTH_LONG).show()
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    e.printStackTrace()
                    Toast.makeText(this@ClientePuntoFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }, Response.ErrorListener { error ->
                try {
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    error.printStackTrace()
                    Toast.makeText(this@ClientePuntoFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this@ClientePuntoFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ClientePuntoViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if(result != null){
            if(result.contents != null){
                Log.d("qr",result.contents)

            } else {
                Log.d("qr","scan failed")
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


    fun startRonda(){
            progressView.visibility = View.VISIBLE
            contentView.visibility = View.GONE
            if (!NetworkUtils.isConnected(this@ClientePuntoFragment.context!!)) {
                Toast.makeText(this@ClientePuntoFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
            }else{
                val queue = Volley.newRequestQueue(this@ClientePuntoFragment.context!!)
                val URL = "${Utils.URL_SERVER}supervisor/rondas"
                val stringRequest = object : StringRequest(Method.POST, URL, Response.Listener<String> { response ->
                    try {
                        var strResp = response.toString()
                        val jsonObj: JSONObject = JSONObject(strResp)
                        Log.d("$URL",strResp)
                        val id_ronda_historial_sup = jsonObj.get("id").toString()
                        prefs.edit().putString("id_ronda_historial_sup",id_ronda_historial_sup).apply()
                        Toast.makeText(this@ClientePuntoFragment.context!!, jsonObj.get("message").toString(), Toast.LENGTH_LONG).show()

                        displayView()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@ClientePuntoFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    }
                }, Response.ErrorListener { error ->
                    try {
                        error.printStackTrace()
                        Toast.makeText(this@ClientePuntoFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                        displayView()
                    } catch (e: Exception) {
                        Toast.makeText(this@ClientePuntoFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    }
                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers.put("token", prefs.getString("api_key", "")!!)
                        return headers
                    }
                    override fun getParams(): MutableMap<String, String> {
                        val parameters = HashMap<String, String>()
                        parameters["id_ronda_supervisor"] = prefs.getString("id_ronda_supervisor", "")
                        return parameters
                    }
                }
                stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                queue.add(stringRequest)
            }

    }

    fun endRonda(descripcion: String){
        progressView.visibility = View.VISIBLE
        contentView.visibility = View.GONE
        if (!NetworkUtils.isConnected(this@ClientePuntoFragment.context!!)) {
            Toast.makeText(this@ClientePuntoFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
        }else{
            val queue = Volley.newRequestQueue(this@ClientePuntoFragment.context!!)
            val URL = "${Utils.URL_SERVER}rondas/${prefs.getString("id_ronda_historial_sup","")}/finish"
            val stringRequest = object : StringRequest(Method.POST, URL, Response.Listener<String> { response ->
                try {
                    var strResp = response.toString()
                    val jsonObj: JSONObject = JSONObject(strResp)
                    Log.d("$URL",strResp)
                    prefs.edit().remove("id_ronda_historial_sup").commit()
                    Toast.makeText(this@ClientePuntoFragment.context!!, jsonObj.get("message").toString(), Toast.LENGTH_LONG).show()

                    displayView()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@ClientePuntoFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }, Response.ErrorListener { error ->
                try {
                    error.printStackTrace()
                    Toast.makeText(this@ClientePuntoFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                    displayView()

                } catch (e: Exception) {
                    Toast.makeText(this@ClientePuntoFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers.put("token", prefs.getString("api_key", "")!!)
                    return headers
                }
                override fun getParams(): MutableMap<String, String> {
                    val parameters = HashMap<String, String>()
                    parameters["descripcion"] = descripcion
                    return parameters
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            queue.add(stringRequest)
        }

    }

    fun endParada(){
        progressView.visibility = View.VISIBLE
        contentView.visibility = View.GONE
        if (!NetworkUtils.isConnected(this@ClientePuntoFragment.context!!)) {
            Toast.makeText(this@ClientePuntoFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
        }else{
            val queue = Volley.newRequestQueue(this@ClientePuntoFragment.context!!)
            val URL = "${Utils.URL_SERVER}supervisor/rondas/paradas/${prefs.getString("id_ronda_historial_detalle_sup","")}"
            val stringRequest = object : StringRequest(Method.PUT, URL, Response.Listener<String> { response ->
                try {
                    var strResp = response.toString()
                    val jsonObj: JSONObject = JSONObject(strResp)
                    //Log.d("$URL",strResp)
                    prefs.edit().remove("id_ronda_historial_detalle_sup").commit()
                    Toast.makeText(this@ClientePuntoFragment.context!!, jsonObj.get("message").toString(), Toast.LENGTH_LONG).show()

                    displayView()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@ClientePuntoFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }, Response.ErrorListener { error ->
                try {
                    error.printStackTrace()
                    Toast.makeText(this@ClientePuntoFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                    displayView()
                } catch (e: Exception) {
                    Toast.makeText(this@ClientePuntoFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
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

}
