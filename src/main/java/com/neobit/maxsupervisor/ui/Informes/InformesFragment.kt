package com.neobit.maxsupervisor.ui.Informes

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
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.neobit.maxsupervisor.adapters.InformeAdapter
import com.neobit.maxsupervisor.data.model.Informe


import com.neobit.maxsupervisor.R
import com.neobit.maxsupervisor.Utils.NetworkUtils
import com.neobit.maxsupervisor.Utils.Utils
import kotlinx.android.synthetic.main.informes_fragment.*
import kotlinx.android.synthetic.main.informes_fragment.contentView
import kotlinx.android.synthetic.main.informes_fragment.progressView
import kotlinx.android.synthetic.main.punto_inventario_fragment.*
import org.json.JSONObject
import java.util.HashMap

class InformesFragment : Fragment() {

    private lateinit var prefs: SharedPreferences


    companion object {
        fun newInstance() = InformesFragment()
    }

    private lateinit var viewModel: InformesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.informes_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext)
        progressView.visibility = View.VISIBLE
        contentView.visibility = View.GONE

        getInformes()
        prefs.edit().remove("id_informe").commit()
        prefs.edit().remove("id_informe").apply()

        btnCreateInforme.setOnClickListener {
            startActivity(Intent(this@InformesFragment!!.context, InformeActivity::class.java))
            //Navigation.findNavController(view!!).navigate(R.id.nav_crear_informe)
        }
    }

    override fun onResume() {
        super.onResume()
        getInformes()
    }


    private fun getInformes() {
        if (!NetworkUtils.isConnected(this@InformesFragment.context!!)) {
            Toast.makeText(this@InformesFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val informes = ArrayList<Informe>()
            val queue = Volley.newRequestQueue(this@InformesFragment.context!!)

            val URL = "${Utils.URL_SERVER}informes?limite=10"
            val stringRequest = object : StringRequest(Method.GET, URL, Response.Listener<String> { response ->
                try {
                    var strResp = response.toString()
                    val jsonObj: JSONObject = JSONObject(strResp)
                    Log.d("$URL",strResp)

                    val jsonArray = jsonObj.getJSONArray("informes")

                        for (i in 0 until jsonArray.length()) {
                            var jsonInner: JSONObject = jsonArray.getJSONObject(i)
                            val id_informe = jsonInner.get("id_informe").toString().toInt()
                            val descripcion = jsonInner.get("descripcion").toString()
                            val id_creador = jsonInner.getJSONObject("creador").get("id_guardia").toString().toInt()
                            val nombre_creador = jsonInner.getJSONObject("creador").get("nombres").toString()
                            val id_editor = jsonInner.getJSONObject("editor").get("id_guardia").toString().toInt()
                            val nombre_editor = jsonInner.getJSONObject("editor").get("nombres").toString()
                            val fecha_creacion = jsonInner.get("fecha_creacion").toString()
                            Log.d("add","$id_informe,$descripcion,$id_creador,$nombre_creador,$id_editor,$id_editor,$nombre_editor,$fecha_creacion")
                            informes.add(Informe(id_informe,descripcion,id_creador,nombre_creador,id_editor,nombre_editor,fecha_creacion))
                        }
                        rvInformes.layoutManager = LinearLayoutManager(this@InformesFragment.context!!,
                            LinearLayoutManager.VERTICAL,false)
                        val adapter = InformeAdapter(informes)
                        rvInformes.adapter = adapter
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
                    Toast.makeText(this@InformesFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    Toast.makeText(this@InformesFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
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
        viewModel = ViewModelProviders.of(this).get(InformesViewModel::class.java)
    }

}
