package com.neobit.maxsupervisor.ui.Puntos

import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.HashMap
import com.neobit.maxsupervisor.Utils.NetworkUtils
import com.neobit.maxsupervisor.Utils.Utils
import android.view.ViewGroup
import com.neobit.maxsupervisor.R
import com.neobit.maxsupervisor.model.PuntoTarea
import com.neobit.maxsupervisor.adapters.PuntoTareaAdapter
import kotlinx.android.synthetic.main.supervisor_tarea_fragment.*

class SupervisorTareaFragment : Fragment() {

    companion object {
        fun newInstance() = SupervisorTareaFragment()
    }

    private lateinit var viewModel: SupervisorTareaViewModel
    private lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.supervisor_tarea_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SupervisorTareaViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        progressView.visibility = View.VISIBLE
        contentView.visibility = View.GONE
        getTareasPunto()
    }
    private fun getTareasPunto() {
        if (!NetworkUtils.isConnected(this@SupervisorTareaFragment.context!!)) {
            Toast.makeText(this@SupervisorTareaFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val puntotareas = ArrayList<PuntoTarea>()
            val queue = Volley.newRequestQueue(this@SupervisorTareaFragment.context!!)
            val URL = "${Utils.URL_SERVER}puntos/${prefs.getString("id_punto","")}/guardias_tareas?supervisor=1"
            val stringRequest = object : StringRequest(Method.GET, URL, Response.Listener<String> { response ->
                try {
                    var strResp = response.toString()
                    val jsonObj: JSONObject = JSONObject(strResp)
                    val jsonArray = jsonObj.getJSONArray("registros")
                    for (i in 0 until jsonArray.length()) {
                        var jsonInner: JSONObject = jsonArray.getJSONObject(i)
                        val id_punto_tarea = jsonInner.get("id_punto_tarea").toString().toInt()
                        val nombre = jsonInner.get("nombre").toString()
                        val hora_inicio = jsonInner.get("hora_inicio").toString()
                        val info = jsonInner.getInt("info")
                        val supervisor = jsonInner.getInt("supervisor")
                        var done = 0
                        if(jsonInner.has("completado")){
                            done = 1
                        }
                        Log.d("add","$id_punto_tarea,$nombre,$info,$hora_inicio")
                        if(info == 0){
                            puntotareas.add(PuntoTarea(id_punto_tarea,nombre,info,hora_inicio,supervisor,done))
                        }
                    }
                    rvSupervisorTareas.layoutManager = LinearLayoutManager(this@SupervisorTareaFragment.context!!,
                        LinearLayoutManager.VERTICAL,false)

                    val adapter = PuntoTareaAdapter(puntotareas)
                    rvSupervisorTareas.adapter = adapter

                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error ->
                try {
                    error.printStackTrace()
                    Toast.makeText(this@SupervisorTareaFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    Toast.makeText(this@SupervisorTareaFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
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
