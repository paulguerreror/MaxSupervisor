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
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.inventario_fragment.*
import com.neobit.maxsupervisor.Utils.NetworkUtils
import com.neobit.maxsupervisor.Utils.Utils
import com.neobit.maxsupervisor.adapters.ProductoAdapter
import com.neobit.maxsupervisor.R
import com.neobit.maxsupervisor.model.Producto
import kotlinx.android.synthetic.main.inventario_fragment.contentView
import kotlinx.android.synthetic.main.inventario_fragment.progressView
import kotlinx.android.synthetic.main.inventario_reporte_fragment.*
import kotlinx.android.synthetic.main.punto_inventario_fragment.*
import org.json.JSONObject
import java.util.HashMap

class InventarioFragment : Fragment() {

    private lateinit var prefs: SharedPreferences
    private lateinit var  id_productos: String
    private lateinit var checks: String
    private lateinit var imagenes: String
    private lateinit var danos: String
    private lateinit var descripciones: String

    companion object {
        fun newInstance() = InventarioFragment()
    }

    private lateinit var viewModel: InventarioViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.inventario_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressView.visibility = View.VISIBLE
        contentView.visibility = View.GONE
        prefs = PreferenceManager.getDefaultSharedPreferences(this@InventarioFragment.context!!)
        if(prefs.contains("id_productos")){
            Log.d("existe",prefs.getString("id_productos",""))
            getInventario2()
        }else{
            getInventario()
            Log.d("existe","no existe")
        }
        /*
        if(prefs.contains("id_inventario")){

            if(prefs.contains("id_productos")){
                Log.d("existe",prefs.getString("id_productos",""))
                getInventario2()
            }else{
                getInventario()
                Log.d("existe","no existe")
            }
        }else{
            Toast.makeText(this@InventarioFragment.context!!, "No ha creado inventario", Toast.LENGTH_LONG).show()
        }*/

        btnInventario.setOnClickListener {
            saveInventario()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(InventarioViewModel::class.java)
        // TODO: Use the ViewModel
    }

    private fun getInventario() {
        val productos = ArrayList<Producto>()
        if (!NetworkUtils.isConnected(this@InventarioFragment.context!!)) {
            Toast.makeText(this@InventarioFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val queue = Volley.newRequestQueue(this@InventarioFragment.context!!)
            val URL = "${Utils.URL_SERVER}puntos/${prefs.getString("id_punto", "")!!}"
            val stringRequest = object : StringRequest(Method.GET, URL, Response.Listener<String> { response ->
                try {
                    id_productos = ""
                    checks= ""
                    imagenes= ""
                    descripciones= ""
                    danos= ""
                    var strResp = response.toString()
                    Log.d("$URL",strResp)
                    val jsonObj: JSONObject = JSONObject(strResp)
                    val puntos = jsonObj.getJSONObject("puntos")
                    val jsonArray = puntos.getJSONArray("productos")

                    for (i in 0 until jsonArray.length()) {
                        var jsonInner: JSONObject = jsonArray.getJSONObject(i)
                        val id_producto = jsonInner.get("id_producto").toString().toInt()
                        val nombre = jsonInner.get("nombre").toString()
                        val marca = jsonInner.get("marca").toString()
                        Log.d("add","$id_producto,$nombre,$marca")
                        productos.add(
                            Producto(
                                id_producto,
                                nombre,
                                marca,
                                "%",
                                i
                            )
                        )
                        if(id_productos == ""){
                            id_productos +=  id_producto.toString()
                            checks +=  "%"
                        }else{
                            id_productos += "," + jsonInner.get("id_producto").toString()
                            checks +=  ",%"
                            imagenes += ","
                            danos += ","
                            descripciones += ","

                        }
                        Log.d("id_productos",id_productos)
                    }
                    rvproductos.layoutManager = LinearLayoutManager(this@InventarioFragment.context!!,
                        LinearLayoutManager.VERTICAL,false)
                    val adapter = ProductoAdapter(productos)
                    rvproductos.adapter = adapter
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    prefs.edit().putString("id_productos",id_productos).apply()
                    prefs.edit().putString("checks",checks).apply()
                    prefs.edit().putString("imagenes",imagenes).apply()
                    prefs.edit().putString("descripciones",descripciones).apply()
                    prefs.edit().putString("danos",danos).apply()

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@InventarioFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                }
            }, Response.ErrorListener { error ->
                try {
                    error.printStackTrace()
                    Toast.makeText(this@InventarioFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    Toast.makeText(this@InventarioFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
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

    private fun getInventario2(){
        val productos = ArrayList<Producto>()
        if (!NetworkUtils.isConnected(this@InventarioFragment.context!!)) {
            Toast.makeText(this@InventarioFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val queue = Volley.newRequestQueue(this@InventarioFragment.context!!)
            val URL = "${Utils.URL_SERVER}puntos/${prefs.getString("id_punto", "")!!}"
            val stringRequest = object : StringRequest(Method.GET, URL, Response.Listener<String> { response ->
                try {
                    id_productos = ""
                    var strResp = response.toString()
                    Log.d("$URL",strResp)
                    val jsonObj: JSONObject = JSONObject(strResp)
                    val puntos = jsonObj.getJSONObject("puntos")
                    val jsonArray = puntos.getJSONArray("productos")

                    for (i in 0 until jsonArray.length()) {
                        var jsonInner: JSONObject = jsonArray.getJSONObject(i)
                        val id_producto = jsonInner.get("id_producto").toString().toInt()
                        val nombre = jsonInner.get("nombre").toString()
                        val marca = jsonInner.get("marca").toString()
                        val checked = getCheck(i)
                        Log.d("add2","$id_producto,$nombre,$marca")
                        productos.add(
                            Producto(
                                id_producto,
                                nombre,
                                marca,
                                checked,
                                i
                            )
                        )
                    }
                    rvproductos.layoutManager = LinearLayoutManager(this@InventarioFragment.context!!,
                        LinearLayoutManager.VERTICAL,false)
                    val adapter = ProductoAdapter(productos)
                    rvproductos.adapter = adapter
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@InventarioFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }, Response.ErrorListener { error ->
                try {
                    error.printStackTrace()
                    Toast.makeText(this@InventarioFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    Toast.makeText(this@InventarioFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
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

    private fun saveInventario() {
        progressView.visibility = View.VISIBLE
        contentView.visibility = View.GONE
        if (!NetworkUtils.isConnected(this@InventarioFragment.context!!)) {
            Toast.makeText(this@InventarioFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else if(validate()){
            Toast.makeText(this@InventarioFragment.context!!, R.string.error_empty, Toast.LENGTH_LONG).show()
        }else{
            val id_inventario = prefs.getString("id_inventario","")
            val queue = Volley.newRequestQueue(this@InventarioFragment.context!!)
            val URL = "${Utils.URL_SERVER}inventario/$id_inventario/batch"
            val stringRequest = object : StringRequest(Method.POST, URL, Response.Listener<String> { response ->
                try {
                    Log.d("estado","enviado")
                    prefs.edit().remove("id_inventario").commit()
                    prefs.edit().remove("id_productos").commit()
                    prefs.edit().remove("checks").commit()
                    prefs.edit().remove("imagenes").commit()
                    prefs.edit().remove("descripciones").commit()
                    prefs.edit().remove("danos").commit()
                    Navigation.findNavController(view!!).navigate(R.id.nav_punto_inventario)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@InventarioFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }, Response.ErrorListener { error ->
                try {
                    error.printStackTrace()
                    Toast.makeText(this@InventarioFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    Toast.makeText(this@InventarioFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers.put("token", prefs.getString("api_key", "")!!)
                    return headers
                }
                override fun getParams(): MutableMap<String, String> {
                    val parameters = HashMap<String, String>()
                    parameters["id_productos"] = prefs.getString("id_productos", "")
                    parameters["checks"] = prefs.getString("checks", "")
                    parameters["imagenes"] = prefs.getString("imagenes", "")
                    parameters["descripciones"] = prefs.getString("descripciones", "")

                    parameters["danos"] = prefs.getString("danos", "")
                    return parameters
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            queue.add(stringRequest)
        }
    }

    fun onCheck(view: View){
        val id = view.getTag(R.id.position).toString()
        var arrayId = prefs.getString("id_productos", "").split(",")
        val Listchecks = prefs.getString("checks", "").split(",")
        val Arraychecks = arrayListOf<String>()
        val Listimagenes = prefs.getString("imagenes", "").split(",")
        val Arrayimagenes = arrayListOf<String>()
        val Listdescripciones = prefs.getString("descripciones", "").split(",")
        val Arraydescripciones = arrayListOf<String>()
        val Listdanos = prefs.getString("danos", "").split(",")
        val Arraydanos = arrayListOf<String>()
        val position = id.toInt()
        var i= 0
        Listchecks.forEach{
            var ck = it
            if(i == position){
                if(it.contains("1"))
                    ck = "%"
                else
                    ck = "1"
            }
            Arraychecks.add(ck)
            i++
        }
        Log.d("id_productos",prefs.getString("id_productos", ""))

        checks = Arraychecks.toString().replace("[","").replace("]","").replace("  "," ").replace(", ",",")
        prefs.edit().putString("checks",checks).apply()
        Log.d("checks",prefs.getString("checks", ""))
        //imagenes
        i=0
        Listimagenes.forEach{
            var ck = it
            if(i == position){
                ck = ""
            }
            Arrayimagenes.add(ck)
            i++
        }
        imagenes = Arrayimagenes.toString().replace("[","").replace("]","").replace(" ","")
        prefs.edit().putString("imagenes",imagenes).apply()
        Log.d("imagenes",prefs.getString("imagenes", ""))
        //descripciones
        i=0
        Listdescripciones.forEach{
            var ck = it
            if(i == position){
                ck = ""
            }
            Arraydescripciones.add(ck)
            i++
        }
        descripciones = Arraydescripciones.toString().replace("[","").replace("]","").replace(" ","")
        prefs.edit().putString("descripciones",descripciones).apply()
        Log.d("descripciones",prefs.getString("descripciones", ""))
        //danos
        i=0
        Listdanos.forEach{
            var ck = it
            if(i == position){
                ck = ""
            }
            Arraydanos.add(ck)
            i++
        }
        danos = Arraydanos.toString().replace("[","").replace("]","").replace(" ","")
        prefs.edit().putString("danos",danos).apply()
        Log.d("danos",prefs.getString("danos", ""))
        Log.d("id_invetario",prefs.getString("id_inventario",""))
        //getInventario2()
        //finish()
        //startActivity(Intent(this@InventarioActivity, InventarioActivity::class.java))
    }

    fun validate(): Boolean {
        Log.d("validar", "empieza")
        if(prefs.getString("checks","").contains("%")){
            Log.d("validate","faltan datos")
            return true
        }else{
            return false
            Log.d("validate","completo")
        }
    }

    fun getCheck(position: Int):String{
        val Listchecks = prefs.getString("checks", "").split(",")
        var i= 0
        Listchecks.forEach{
            var ck = it
            if(i == position){
                return it
            }
            i++
        }
        return "%"
    }

}
