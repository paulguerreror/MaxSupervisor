package com.neobit.maxsupervisor.ui.Avisos

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

import com.neobit.maxsupervisor.R
import com.neobit.maxsupervisor.Utils.NetworkUtils
import com.neobit.maxsupervisor.Utils.Utils
import com.neobit.maxsupervisor.adapters.AvisoAdapter
import com.neobit.maxsupervisor.data.model.Aviso
import kotlinx.android.synthetic.main.avisos_fragment.*
import org.json.JSONObject
import java.util.HashMap

class AvisosFragment : Fragment() {

    private lateinit var prefs: SharedPreferences


    companion object {
        fun newInstance() = AvisosFragment()
    }

    private lateinit var viewModel: AvisosViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.avisos_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext)
        getAvisos()
    }

    private fun getAvisos() {
        if (!NetworkUtils.isConnected(this@AvisosFragment.context!!)) {
            Toast.makeText(this@AvisosFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val avisos = ArrayList<Aviso>()
            val queue = Volley.newRequestQueue(this@AvisosFragment.context!!)

            val URL = "${Utils.URL_SERVER}avisos"
            val stringRequest = object : StringRequest(Method.GET, URL, Response.Listener<String> { response ->
                try {
                    var strResp = response.toString()
                    val jsonObj: JSONObject = JSONObject(strResp)
                    Log.d("$URL",strResp)

                    val jsonArray = jsonObj.getJSONArray("registros")
                    if(jsonArray.length() != 0){
                        txtPlain.visibility = View.GONE
                        rvAvisos.visibility = View.VISIBLE
                        for (i in 0 until jsonArray.length()) {
                            var jsonInner: JSONObject = jsonArray.getJSONObject(i)
                            val id_aviso_historial = jsonInner.get("id_aviso_historial").toString().toInt()
                            val validado = jsonInner.getInt("validado")
                            val id_aviso = jsonInner.getInt("id_aviso")
                            val aviso = jsonInner.getJSONObject("aviso")
                            val hora = aviso.get("hora").toString()
                            val descripcion = aviso.get("descripcion").toString()
                            Log.d("add","$id_aviso_historial,$validado,$id_aviso,$hora,$descripcion")
                            avisos.add(Aviso(id_aviso_historial,validado,id_aviso,hora,descripcion))
                        }
                        rvAvisos.layoutManager = LinearLayoutManager(this@AvisosFragment.context!!,
                            LinearLayoutManager.VERTICAL,false)
                        val adapter = AvisoAdapter(avisos)
                        rvAvisos.adapter = adapter
                    }
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
                    Toast.makeText(this@AvisosFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    Toast.makeText(this@AvisosFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
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


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(AvisosViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
