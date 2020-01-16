package com.neobit.maxsupervisor.ui.Novedades

import android.content.SharedPreferences
import android.graphics.Color
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.novedad_detalles_fragment.*

import com.neobit.maxsupervisor.R
import com.neobit.maxsupervisor.Utils.NetworkUtils
import com.neobit.maxsupervisor.Utils.Utils
import com.neobit.maxsupervisor.adapters.NovedadDetalleAdapter
import com.neobit.maxsupervisor.data.model.NovedadDetalle
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.novedad_detalles_fragment.contentView
import kotlinx.android.synthetic.main.novedad_detalles_fragment.progressView
import kotlinx.android.synthetic.main.punto_novedades_fragment.*
import org.json.JSONObject
import java.util.HashMap

class NovedadDetallesFragment : Fragment() {

    companion object {
        fun newInstance() = NovedadDetallesFragment()
    }

    private lateinit var viewModel: NovedadDetallesViewModel
    private lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.novedad_detalles_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext)
        progressView.visibility = View.VISIBLE
        contentView.visibility = View.GONE
        getDetallesByNovedad()
        btnNovedadDetalle.setOnClickListener {
            Navigation.findNavController(view!!).navigate(R.id.nav_crear_novedad_detalle)
        }
        cardprivacidad.setOnClickListener {
            val cliente =cardprivacidad.getTag().toString()
            val builder = AlertDialog.Builder(this@NovedadDetallesFragment.context!!)
            builder.setTitle(getString(R.string.app_name)).setMessage(R.string.alert_change_privacy)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.si)) { dialog, _ ->
                    dialog.cancel()
                    changeNovedadPrivacy(cliente.toInt())
                }
                .setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.cancel() }
            val alert = builder.create()
            alert.show()
            true
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(NovedadDetallesViewModel::class.java)
        // TODO: Use the ViewModel
    }

     fun getDetallesByNovedad() {
        if (!NetworkUtils.isConnected(this@NovedadDetallesFragment.context!!)) {
            Toast.makeText(this@NovedadDetallesFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val novedaddetalles = ArrayList<NovedadDetalle>()
            val queue = Volley.newRequestQueue(this@NovedadDetallesFragment.context!!)
            val URL = "${Utils.URL_SERVER}novedades/${prefs.getString("id_novedad","")}"
            val stringRequest = object : StringRequest(Method.GET, URL, Response.Listener<String> { response ->
                try {
                    var strResp = response.toString()
                    Log.d("$URL",strResp)
                    val jsonObj: JSONObject = JSONObject(strResp)
                    val novedad = jsonObj.getJSONObject("novedades")
                    val detalle = novedad.getJSONArray("detalle")
                    cardprivacidad.setTag(novedad.getInt("cliente"))
                    txtDetalleFecha.text = novedad.getString("fecha_creacion")
                    if(novedad.getString("imagen") == ""){
                        imgDetalleDescripcion.visibility = View.GONE
                    }else{
                        Picasso.get().load(Utils.URL_MEDIA + novedad.getString("imagen")).error(R.drawable.placeholder).placeholder(R.drawable.placeholder).noFade().into(imgDetalleDescripcion)
                    }
                    txtDetalleDescripcion.text = novedad.getString("descripcion")
                    txtDetalleNombre.text = novedad.getJSONObject("creador").getString("nombres")
                    if(novedad.getInt("cliente") == 1){
                        cardprivacidad.setCardBackgroundColor(Color.parseColor("#26dad2"))
                        txtprivacidad.text = "Público"
                    }else{
                        cardprivacidad.setCardBackgroundColor(Color.parseColor("#ef5350"))
                        txtprivacidad.text = "Privado"
                    }
                    for (i in 0 until detalle.length()) {
                        var jsonInner: JSONObject = detalle.getJSONObject(i)
                        val id_novedad_detalle = jsonInner.get("id_novedad_detalle").toString().toInt()
                        val nombres = jsonInner.getJSONObject("id_creador").getString("nombres")
                        val tipo = jsonInner.get("tipo").toString()
                        val descripcion = jsonInner.get("descripcion").toString()
                        val imagen = jsonInner.get("imagen").toString()
                        val fecha = jsonInner.get("fecha_creacion").toString()
                        val cliente = jsonInner.getInt("cliente")

                        Log.d("add","$id_novedad_detalle|$cliente|$nombres|$tipo|$descripcion|$imagen|$fecha|")
                        novedaddetalles.add(NovedadDetalle(id_novedad_detalle,cliente,nombres,tipo,descripcion,imagen,fecha))
                    }
                    rvNovedadDetalles.layoutManager = LinearLayoutManager(this@NovedadDetallesFragment.context!!,
                        LinearLayoutManager.VERTICAL,false)
                    val adapter = NovedadDetalleAdapter(novedaddetalles)
                    rvNovedadDetalles.adapter = adapter
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    e.printStackTrace()
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                }
            }, Response.ErrorListener { error ->
                try {
                    error.printStackTrace()
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    Toast.makeText(this@NovedadDetallesFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    Toast.makeText(this@NovedadDetallesFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
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


    private fun changeNovedadPrivacy(cliente: Int) {
        var client = 0
        if(cliente == 0){
            client = 1
        }
        if(prefs.contains("id_novedad")){
            if (!NetworkUtils.isConnected(this@NovedadDetallesFragment.context!!)) {
                Toast.makeText(this@NovedadDetallesFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
            } else{
                progressView.visibility = View.VISIBLE
                contentView.visibility = View.GONE
                val queue = Volley.newRequestQueue(this@NovedadDetallesFragment.context!!)
                val URL = "${Utils.URL_SERVER}novedades/${prefs.getString("id_novedad","")}"
                val stringRequest = object : StringRequest(Method.PUT, URL, Response.Listener<String> { response ->
                    try {
                        var strResp = response.toString()
                        Log.d("$URL",strResp)
                        val jsonObj: JSONObject = JSONObject(strResp)
                        val message = jsonObj.get("message").toString()
                        progressView.visibility = View.GONE
                        contentView.visibility = View.VISIBLE
                        Toast.makeText(this@NovedadDetallesFragment.context!!, message, Toast.LENGTH_LONG).show()
                        getDetallesByNovedad()

                    } catch (e: Exception) {
                        progressView.visibility = View.GONE
                        contentView.visibility = View.VISIBLE
                        e.printStackTrace()
                        Toast.makeText(this@NovedadDetallesFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    }
                }, Response.ErrorListener { error ->
                    try {
                        progressView.visibility = View.GONE
                        contentView.visibility = View.VISIBLE
                        error.printStackTrace()
                        Toast.makeText(this@NovedadDetallesFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        progressView.visibility = View.GONE
                        contentView.visibility = View.VISIBLE
                        Toast.makeText(this@NovedadDetallesFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    }
                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers.put("token", prefs.getString("api_key", "")!!)
                        return headers
                    }
                    override fun getParams(): MutableMap<String, String> {
                        val parameters = HashMap<String, String>()
                        parameters["cliente"] = client.toString()
                        return parameters
                    }
                }
                stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                queue.add(stringRequest)
            }
        }else{
            Log.e("error","no novedad")
        }

    }


}
