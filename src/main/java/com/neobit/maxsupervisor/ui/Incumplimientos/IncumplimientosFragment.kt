package com.neobit.maxsupervisor.ui.Incumplimientos

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
import com.neobit.maxsupervisor.adapters.GuardiaAdapter
import com.neobit.maxsupervisor.adapters.InformeAdapter
import com.neobit.maxsupervisor.adapters.PuntoAdapter
import com.neobit.maxsupervisor.data.model.Guardia
import com.neobit.maxsupervisor.data.model.Informe
import com.neobit.maxsupervisor.data.model.Punto
import kotlinx.android.synthetic.main.incumplimientos_fragment.*
import kotlinx.android.synthetic.main.informes_fragment.*
import kotlinx.android.synthetic.main.guardias.cardGuardia
import kotlinx.android.synthetic.main.informes_fragment.contentView
import kotlinx.android.synthetic.main.informes_fragment.progressView
import org.json.JSONObject
import java.util.HashMap

class IncumplimientosFragment : Fragment() {
    private lateinit var prefs: SharedPreferences

    companion object {
        fun newInstance() = IncumplimientosFragment()
    }

    private lateinit var viewModel: IncumplimientosViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        prefs = PreferenceManager.getDefaultSharedPreferences(this@IncumplimientosFragment.context!!)

        return inflater.inflate(R.layout.incumplimientos_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(this@IncumplimientosFragment.context!!)
        btnSearchGuardia.setOnClickListener{
            progressView.visibility = View.VISIBLE
            contentView.visibility = View.GONE
            val text = txtSearchGuardia.text.toString()
            if(text !=""){
                //Toast.makeText(this@IncumplimientosFragment.context!!,"buscando: $text", Toast.LENGTH_LONG).show()
                getGuardias(text)
            }else{
                Toast.makeText(this@IncumplimientosFragment.context!!,R.string.error_empty, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(IncumplimientosViewModel::class.java)
        // TODO: Use the ViewModel
    }

    private fun getGuardias(texto:String) {
        if (!NetworkUtils.isConnected(this@IncumplimientosFragment.context!!)) {
            Toast.makeText(this@IncumplimientosFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else{
            val guardias = ArrayList<Guardia>()
            val queue = Volley.newRequestQueue(this@IncumplimientosFragment.context!!)
            val URL = "${Utils.URL_SERVER}guardia/search"
            val stringRequest = object : StringRequest(Method.POST, URL, Response.Listener<String> { response ->
                try {
                    var strResp = response.toString()
                    val jsonObj: JSONObject = JSONObject(strResp)
                    Log.d("$URL",strResp)

                    val jsonArray = jsonObj.getJSONArray("guardias")
                    if(jsonArray.length() == 0){
                        txtPlain.visibility = View.VISIBLE
                        rvGuardias.visibility = View.GONE
                    }else{
                        txtPlain.visibility = View.GONE
                        rvGuardias.visibility = View.VISIBLE

                        for (i in 0 until jsonArray.length()) {
                            var jsonInner: JSONObject = jsonArray.getJSONObject(i)
                            val id_guardia = jsonInner.get("id_guardia").toString().toInt()
                            val codigo = jsonInner.get("codigo").toString()
                            val nombres = jsonInner.get("nombres").toString()
                            val imagen = jsonInner.get("imagen").toString()
                            val supervisor = jsonInner.get("supervisor").toString().toInt()

                            Log.d("add","$id_guardia,$codigo,$nombres,$imagen")
                            guardias.add(Guardia(id_guardia,codigo,nombres,imagen,supervisor,""))
                        }
                        rvGuardias.layoutManager = LinearLayoutManager(this@IncumplimientosFragment.context!!,
                            LinearLayoutManager.VERTICAL,false)
                        val adapter = GuardiaAdapter(guardias)
                        rvGuardias.adapter = adapter
                    }

                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@IncumplimientosFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }, Response.ErrorListener { error ->
                try {
                    error.printStackTrace()
                    Toast.makeText(this@IncumplimientosFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this@IncumplimientosFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers.put("token", prefs.getString("api_key", "")!!)
                    return headers
                }
                override fun getParams(): MutableMap<String, String> {
                    val parameters = HashMap<String, String>()
                    parameters["texto"] = texto
                    return parameters
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            queue.add(stringRequest)
        }
    }



}
