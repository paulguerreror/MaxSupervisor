package com.neobit.maxsupervisor.ui.Novedades

import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer

import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

import com.neobit.maxsupervisor.R
import com.neobit.maxsupervisor.Utils.NetworkUtils
import com.neobit.maxsupervisor.Utils.Utils
import com.neobit.maxsupervisor.adapters.PuntoAdapter
import com.neobit.maxsupervisor.data.model.Punto
import com.neobit.maxsupervisor.ui.gallery.GalleryViewModel
import kotlinx.android.synthetic.main.fragment_novedades.*
import kotlinx.android.synthetic.main.fragment_novedades.contentView
import kotlinx.android.synthetic.main.fragment_novedades.progressView
import kotlinx.android.synthetic.main.fragment_novedades.txtPlain
import kotlinx.android.synthetic.main.incumplimientos_fragment.*
import kotlinx.android.synthetic.main.informes_fragment.*
import org.json.JSONObject
import java.util.HashMap

class NovedadesFragment : Fragment() {
    private lateinit var prefs: SharedPreferences


    companion object {
        fun newInstance() = NovedadesFragment()
    }
    private lateinit var novedadesViewModel: NovedadesViewModel

    private lateinit var viewModel: NovedadesViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        novedadesViewModel =
            ViewModelProviders.of(this).get(NovedadesViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_novedades, container, false)
       // val textView: TextView = root.findViewById(R.id.txtProfile)
        prefs = PreferenceManager.getDefaultSharedPreferences(this@NovedadesFragment.context!!)

        novedadesViewModel.text.observe(this, Observer {
           // textView.text = it
        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnSearchPunto.setOnClickListener {
            progressView.visibility = View.VISIBLE
            contentView.visibility = View.GONE
            val text = txtSearchPunto.text.toString()

            if(text !=""){
                //Toast.makeText(this@NovedadesFragment.context!!,"buscando: $text", Toast.LENGTH_LONG).show()
                getPuntos(text)
            }else{
                Toast.makeText(this@NovedadesFragment.context!!,R.string.error_empty, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(NovedadesViewModel::class.java)
        // TODO: Use the ViewModel
    }


    private fun getPuntos(texto:String) {
        if (!NetworkUtils.isConnected(this@NovedadesFragment.context!!)) {
            Toast.makeText(this@NovedadesFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else{
            val puntos = ArrayList<Punto>()
            val queue = Volley.newRequestQueue(this@NovedadesFragment.context!!)
            val URL = "${Utils.URL_SERVER}puntos/search"
            val stringRequest = object : StringRequest(Method.POST, URL, Response.Listener<String> { response ->
                try {
                    var strResp = response.toString()
                    val jsonObj: JSONObject = JSONObject(strResp)
                    Log.d("$URL",strResp)

                    val jsonArray = jsonObj.getJSONArray("puntos")
                    if(jsonArray.length() == 0){
                        txtPlain.visibility = View.VISIBLE
                        rvPuntos.visibility = View.GONE
                    }else{
                        txtPlain.visibility = View.GONE
                        rvPuntos.visibility = View.VISIBLE
                        for (i in 0 until jsonArray.length()) {
                            var jsonInner: JSONObject = jsonArray.getJSONObject(i)
                            val id_punto = jsonInner.get("id_punto").toString().toInt()
                            val codigo = jsonInner.get("codigo").toString()
                            val latitud = jsonInner.get("latitud").toString()
                            val longitud = jsonInner.get("longitud").toString()
                            Log.d("add","$id_punto,$codigo,$latitud,$longitud")
                            puntos.add(Punto(id_punto,codigo,latitud,longitud))
                        }
                        rvPuntos.layoutManager = LinearLayoutManager(this@NovedadesFragment.context!!,
                            LinearLayoutManager.VERTICAL,false)
                        val adapter = PuntoAdapter(puntos)
                        rvPuntos.adapter = adapter
                    }

                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@NovedadesFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }, Response.ErrorListener { error ->
                try {
                    error.printStackTrace()
                    Toast.makeText(this@NovedadesFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this@NovedadesFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers.put("token", prefs.getString("api_key", "")!!)
                    return headers
                }
                override fun getParams(): MutableMap<String, String> {
                    val parameters = HashMap<String, String>()
                    parameters["text"] = texto
                    return parameters
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            queue.add(stringRequest)
        }
    }



}
