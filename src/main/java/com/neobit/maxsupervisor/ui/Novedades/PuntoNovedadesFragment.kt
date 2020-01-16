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
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.neobit.maxsupervisor.Utils.NetworkUtils
import com.neobit.maxsupervisor.Utils.Utils
import com.android.volley.toolbox.Volley
import com.neobit.maxsupervisor.data.model.Novedad
import com.neobit.maxsupervisor.R
import com.neobit.maxsupervisor.adapters.NovedadAdapter
import kotlinx.android.synthetic.main.punto_novedades_fragment.*
import org.json.JSONObject
import java.util.HashMap

class PuntoNovedadesFragment : Fragment() {

    private lateinit var prefs: SharedPreferences

    companion object {
        fun newInstance() = PuntoNovedadesFragment()
    }

    private lateinit var viewModel: PuntoNovedadesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.punto_novedades_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext)
        progressView.visibility = View.VISIBLE
        contentView.visibility = View.GONE
        getNovedades()
        btnNovedad.setOnClickListener {
            Navigation.findNavController(view!!).navigate(R.id.nav_crear_novedad)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(PuntoNovedadesViewModel::class.java)
        // TODO: Use the ViewModel
    }


    private fun getNovedades() {
        if (!NetworkUtils.isConnected(this@PuntoNovedadesFragment.context!!)) {
            Toast.makeText(this@PuntoNovedadesFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val novedades = ArrayList<Novedad>()
            val queue = Volley.newRequestQueue(this@PuntoNovedadesFragment.context!!)
            val URL = "${Utils.URL_SERVER}punto/${prefs.getString("id_punto","")}/novedades"
            val stringRequest = object : StringRequest(Method.GET, URL, Response.Listener<String> { response ->
                try {
                    var strResp = response.toString()
                    val jsonObj: JSONObject = JSONObject(strResp)
                    Log.d("$URL",strResp)

                    val jsonArray = jsonObj.getJSONArray("novedades")

                    for (i in 0 until jsonArray.length()) {
                        var jsonInner: JSONObject = jsonArray.getJSONObject(i)
                        val id_novedad = jsonInner.get("id_novedad").toString().toInt()
                        val tipo = jsonInner.get("tipo").toString()
                        val descripcion = jsonInner.get("descripcion").toString()
                        val imagen = jsonInner.get("imagen").toString()
                        val fecha_creacion = jsonInner.get("fecha_creacion").toString()
                        val cliente = jsonInner.getInt("cliente")
                        val creador =jsonInner.getJSONObject("creador").get("nombres").toString()
                        Log.d("add","$id_novedad,$descripcion,$creador,$tipo,$imagen,$cliente,$fecha_creacion")
                        novedades.add(Novedad(id_novedad,creador,tipo,descripcion,imagen,cliente,fecha_creacion))
                    }
                    rvNovedades.layoutManager = LinearLayoutManager(this@PuntoNovedadesFragment.context!!,
                        LinearLayoutManager.VERTICAL,false)
                    val adapter = NovedadAdapter(novedades)
                    rvNovedades.adapter = adapter
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
                    Toast.makeText(this@PuntoNovedadesFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    Toast.makeText(this@PuntoNovedadesFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
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
