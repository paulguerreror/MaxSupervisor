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
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

import com.neobit.maxsupervisor.R
import com.neobit.maxsupervisor.Utils.NetworkUtils
import com.neobit.maxsupervisor.Utils.Utils
import com.neobit.maxsupervisor.adapters.PuntoProductoAdapter
import com.neobit.maxsupervisor.model.Producto
import kotlinx.android.synthetic.main.informes_fragment.*
import kotlinx.android.synthetic.main.punto_inventario_fragment.*
import kotlinx.android.synthetic.main.punto_inventario_fragment.contentView
import kotlinx.android.synthetic.main.punto_inventario_fragment.progressView
import org.json.JSONObject
import java.util.HashMap

class PuntoInventarioFragment : Fragment() {
    private lateinit var prefs: SharedPreferences

    companion object {
        fun newInstance() = PuntoInventarioFragment()
    }

    private lateinit var viewModel: PuntoInventarioViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.punto_inventario_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(this@PuntoInventarioFragment.context!!)

        progressView.visibility = View.VISIBLE
        contentView.visibility = View.GONE
        emptyView.visibility = View.GONE

        if(prefs.contains("id_punto_inventario")){
            getInvetarioByPunto()
        }

        btnStartInventario.setOnClickListener {
            progressView.visibility = View.VISIBLE
            contentView.visibility = View.GONE
            createInventario()
            //Navigation.findNavController(view!!).navigate(R.id.nav_inventario)
        }

    }

    fun createInventario(){
        if (!NetworkUtils.isConnected(this@PuntoInventarioFragment.context!!)) {
            Toast.makeText(this@PuntoInventarioFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val queue = Volley.newRequestQueue(this@PuntoInventarioFragment.context!!)
            val URL = "${Utils.URL_SERVER}inventario"
            val stringRequest = object : StringRequest(Method.POST, URL, Response.Listener<String> { response ->
                try {
                    var strResp = response.toString()
                    Log.d(URL,strResp)
                    val jsonObj: JSONObject = JSONObject(strResp)
                    val id_inventario = jsonObj.get("id").toString()
                    prefs.edit().putString("id_inventario",id_inventario).apply()
                    Log.d("id_inventario",id_inventario)
                    startActivity(Intent(this@PuntoInventarioFragment.context!!, InventarioActivity::class.java))
                } catch (e: Exception) {
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    e.printStackTrace()
                    Toast.makeText(this@PuntoInventarioFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }, Response.ErrorListener { error ->
                try {
                    error.printStackTrace()
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    //Toast.makeText(applicationContext, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this@PuntoInventarioFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers.put("token", prefs.getString("api_key", "")!!)
                    return headers
                }
                override fun getParams(): MutableMap<String, String> {
                    val parameters = HashMap<String, String>()
                    parameters["id_punto"] = prefs.getString("id_punto_inventario", "")
                    return parameters
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            queue.add(stringRequest)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(PuntoInventarioViewModel::class.java)
        // TODO: Use the ViewModel
    }

    private fun getInvetarioByPunto() {
        if (!NetworkUtils.isConnected(this@PuntoInventarioFragment.context!!)) {
            Toast.makeText(this@PuntoInventarioFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val productos = ArrayList<Producto>()
            val queue = Volley.newRequestQueue(this@PuntoInventarioFragment.context!!)

            val URL = "${Utils.URL_SERVER}puntos/${prefs.getString("id_punto_inventario","")}"
            val stringRequest = object : StringRequest(Method.GET, URL, Response.Listener<String> { response ->
                try {
                    var strResp = response.toString()
                    Log.d("$URL",strResp)
                    val jsonObj: JSONObject = JSONObject(strResp)
                    val punto = jsonObj.getJSONObject("puntos")
                    val jsonArray = punto.getJSONArray("productos")
                    if(jsonArray.length() == 0)
                        btnStartInventario.isEnabled = false
                    if(jsonArray.length() != 0){
                        for (i in 0 until jsonArray.length()) {
                            var jsonInner: JSONObject = jsonArray.getJSONObject(i)

                            val id_producto = jsonInner.get("id_producto").toString().toInt()
                            val nombre = jsonInner.get("nombre").toString()
                            val marca = jsonInner.get("marca").toString()

                            Log.d("add","$id_producto,$nombre,$marca")
                            productos.add(Producto(id_producto,nombre,marca,"0",i))
                        }
                        rvPuntoProductos.layoutManager = LinearLayoutManager(this@PuntoInventarioFragment.context!!,
                            LinearLayoutManager.VERTICAL,false)
                        val adapter = PuntoProductoAdapter(productos)
                        rvPuntoProductos.adapter = adapter
                        progressView.visibility = View.GONE
                        contentView.visibility = View.VISIBLE
                        emptyView.visibility = View.GONE

                    }else{
                        progressView.visibility = View.GONE
                        contentView.visibility = View.GONE
                        emptyView.visibility = View.VISIBLE
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error ->
                try {
                    error.printStackTrace()
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    Toast.makeText(this@PuntoInventarioFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    Toast.makeText(this@PuntoInventarioFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
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
