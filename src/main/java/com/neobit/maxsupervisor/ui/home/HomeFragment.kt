package com.neobit.maxsupervisor.ui.home

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import androidx.lifecycle.ViewModelProviders
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.beust.klaxon.Klaxon
import com.neobit.maxsupervisor.MainActivity
import com.squareup.picasso.Picasso
import com.neobit.maxsupervisor.Utils.Utils
import com.neobit.maxsupervisor.Utils.NetworkUtils

import com.neobit.maxsupervisor.R
import com.neobit.maxsupervisor.data.model.Guardia
import kotlinx.android.synthetic.main.fragment_home.*
import org.json.JSONObject
import java.util.HashMap

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var prefs: SharedPreferences
    private lateinit var guardia: Guardia

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        /*val textView: TextView = root.findViewById(R.id.txtProfile)
        homeViewModel.text.observe(this, Observer {
            textView.text = it
        })*/
        return root
    }

    fun startRonda(){
        progressView.visibility = View.VISIBLE
        contentView.visibility = View.GONE
        if(prefs.contains("ronda_historial")){
            Toast.makeText(this@HomeFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
        }else{
            if (!NetworkUtils.isConnected(this@HomeFragment.context!!)) {
                Toast.makeText(this@HomeFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
            } else {
                val queue = Volley.newRequestQueue(this@HomeFragment.context!!)
                val URL = "${Utils.URL_SERVER}supervisor/rondas\n"
                val stringRequest = object : StringRequest(Method.POST, URL, Response.Listener<String> { response ->
                    try {
                        var strResp = response.toString()
                        Log.d(URL,strResp)
                        val jsonObj: JSONObject = JSONObject(strResp)
                        val ronda = jsonObj.get("detalle").toString()
                        prefs.edit().putString("ronda_historial",ronda).apply()
                        Toast.makeText(this@HomeFragment.context!!, jsonObj.get("message").toString(), Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@HomeFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    }
                }, Response.ErrorListener { error ->
                    try {
                        error.printStackTrace()
                        Toast.makeText(this@HomeFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()

                    } catch (e: Exception) {
                        Toast.makeText(this@HomeFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    }
                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers.put("token", prefs.getString("api_key", "")!!)
                        return headers
                    }
                    override fun getParams(): MutableMap<String, String> {
                        val parameters = HashMap<String, String>()
                        parameters["id_ronda_supervisor"] = prefs.getString("id_ronda_supervisor", "")!!
                        parameters["latitud"] = prefs.getString("lat", "")!!
                        parameters["longitud"] = prefs.getString("lon", "")!!
                        return parameters
                    }
                }
                stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                queue.add(stringRequest)
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = PreferenceManager.getDefaultSharedPreferences(this@HomeFragment.context!!)
        prefs = PreferenceManager.getDefaultSharedPreferences(this@HomeFragment.context)
        guardia = Klaxon().parse<Guardia>(prefs.getString("guardias", ""))!!
        Log.d("guardia",guardia.toString())
        txtProfile.text = "${guardia.nombres} "
        txtPunto.text = "Supervisor"

        if (guardia.imagen.isNotEmpty())
            Picasso.get().load(Utils.URL_MEDIA + guardia.imagen).error(R.drawable.placeholder_profile).placeholder(R.drawable.placeholder_profile).noFade().into(profilePicture)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if(result != null){
            if(result.contents != null){
                Log.d("qr",result.contents)
                scanClient(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }


    fun scanClient(codigo:String){
        if (!NetworkUtils.isConnected(this@HomeFragment.context!!)) {
            Toast.makeText(this@HomeFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val queue = Volley.newRequestQueue(this@HomeFragment.context!!)
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
                    Toast.makeText(this@HomeFragment.context!!, jsonObj.get("message").toString(), Toast.LENGTH_LONG).show()
                    if(jsonObj.getBoolean("completada")){
                        //DialogEndRonda()
                        Toast.makeText(this@HomeFragment.context!!, jsonObj.get("Ronda completada!").toString(), Toast.LENGTH_LONG).show()
                    }
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    //validateRonda()
                } catch (e: Exception) {
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    e.printStackTrace()
                    Toast.makeText(this@HomeFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }, Response.ErrorListener { error ->
                try {
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    error.printStackTrace()
                    Toast.makeText(this@HomeFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this@HomeFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
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


}