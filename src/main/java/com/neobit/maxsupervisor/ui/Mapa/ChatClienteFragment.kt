package com.neobit.maxsupervisor.ui.Mapa

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
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

import com.neobit.maxsupervisor.R
import com.neobit.maxsupervisor.Utils.NetworkUtils
import com.neobit.maxsupervisor.Utils.Utils
import com.neobit.maxsupervisor.adapters.MensajeAdapter
import com.neobit.maxsupervisor.data.model.Mensaje
import kotlinx.android.synthetic.main.chat_cliente_fragment.*
import kotlinx.android.synthetic.main.chat_cliente_fragment.contentView
import kotlinx.android.synthetic.main.chat_cliente_fragment.progressView

import org.json.JSONObject
import java.util.HashMap

class ChatClienteFragment : Fragment() {

    companion object {
        fun newInstance() =
            ChatClienteFragment()
    }

    private lateinit var viewModel: ChatClienteViewModel
    private lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.chat_cliente_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ChatClienteViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext)
        progressView.visibility = View.VISIBLE
        contentView.visibility = View.GONE
        loadChat()
        btnReload.setOnClickListener {
            loadChat()
        }
        btnMensaje.setOnClickListener {
            sendMensaje()
        }
    }

    private fun loadChat() {
        if (!NetworkUtils.isConnected(this@ChatClienteFragment.context!!)) {
            Toast.makeText(this@ChatClienteFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            progressView.visibility = View.VISIBLE
            contentView.visibility = View.GONE
            val mensajes = ArrayList<Mensaje>()
            val queue = Volley.newRequestQueue(this@ChatClienteFragment.context!!)
            val URL = "${Utils.URL_SERVER}cliente/${prefs.getString("id_cliente","")}/chat"
            val stringRequest = object : StringRequest(Method.GET, URL, Response.Listener<String> { response ->
                try {
                    var strResp = response.toString()
                    val jsonObj: JSONObject = JSONObject(strResp)
                    Log.d("$URL",strResp)

                    val jsonArray = jsonObj.getJSONArray("mensajes")
                    for (i in 0 until jsonArray.length()) {
                        var jsonInner: JSONObject = jsonArray.getJSONObject(i)

                        val id_chat = jsonInner.get("id_chat").toString().toInt()
                        val mensaje = jsonInner.get("mensaje").toString()
                        val nombre_creador = jsonInner.getJSONObject("creador").get("nombres").toString()
                        val imagen_creador = jsonInner.getJSONObject("creador").get("imagen").toString()

                        val fecha_creacion = jsonInner.get("fecha_creacion").toString()
                        var mio = 0
                        //val  token = jsonInner.getJSONObject("creador").get("token").toString()
                        if(jsonInner.getJSONObject("creador").has("token")){
                            mio =1
                        }

                        Log.d("add","$id_chat,$mensaje,$nombre_creador,$fecha_creacion,$mio")

                        mensajes.add(Mensaje(id_chat,mensaje,imagen_creador,nombre_creador,fecha_creacion,mio))
                    }
                    rvChats.layoutManager = LinearLayoutManager(this@ChatClienteFragment.context!!,
                        LinearLayoutManager.VERTICAL,false)
                    val adapter = MensajeAdapter(mensajes)
                    rvChats.adapter = adapter
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
                    Toast.makeText(this@ChatClienteFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    Toast.makeText(this@ChatClienteFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
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

    fun sendMensaje(){
        if(txtMensaje.text.toString() != "" ){
            if (!NetworkUtils.isConnected(this@ChatClienteFragment.context!!)) {
                Toast.makeText(this@ChatClienteFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
            } else{
                progressView.visibility = View.VISIBLE
                contentView.visibility = View.GONE
                val queue = Volley.newRequestQueue(this@ChatClienteFragment.context!!)
                val URL = "${Utils.URL_SERVER}cliente/${prefs.getString("id_cliente","")}/chat"
                val stringRequest = object : StringRequest(Method.POST, URL, Response.Listener<String> { response ->
                    try {
                        var strResp = response.toString()
                        Log.d("$URL",strResp)
                        txtMensaje.setText("")
                        loadChat()
                    } catch (e: Exception) {
                        progressView.visibility = View.GONE
                        contentView.visibility = View.VISIBLE
                        e.printStackTrace()
                        Toast.makeText(this@ChatClienteFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    }
                }, Response.ErrorListener { error ->
                    try {
                        progressView.visibility = View.GONE
                        contentView.visibility = View.VISIBLE
                        error.printStackTrace()
                        Toast.makeText(this@ChatClienteFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        progressView.visibility = View.GONE
                        contentView.visibility = View.VISIBLE
                        Toast.makeText(this@ChatClienteFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    }
                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers.put("token", prefs.getString("api_key", "")!!)
                        return headers
                    }
                    override fun getParams(): MutableMap<String, String> {
                        val parameters = HashMap<String, String>()
                        parameters["mensaje"] = txtMensaje.text.toString()
                        return parameters
                    }
                }
                stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                queue.add(stringRequest)
            }
        }else{
            Toast.makeText(this@ChatClienteFragment.context!!, "Todos los campos son obligatorios", Toast.LENGTH_LONG).show()
        }
    }



}
