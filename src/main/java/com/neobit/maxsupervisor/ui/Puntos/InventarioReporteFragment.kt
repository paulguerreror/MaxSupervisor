package com.neobit.maxsupervisor.ui.Puntos

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.inventario_reporte_fragment.*
import com.neobit.maxsupervisor.R
import kotlinx.android.synthetic.main.inventario_reporte_fragment.contentView
import kotlinx.android.synthetic.main.inventario_reporte_fragment.progressView
import kotlinx.android.synthetic.main.punto_inventario_fragment.*
import com.neobit.maxsupervisor.Utils.NetworkUtils
import com.neobit.maxsupervisor.Utils.Utils
import com.neobit.maxsupervisor.model.Dano
import com.neobit.maxsupervisor.adapters.DanoAdapter
import kotlinx.android.synthetic.main.inventario_reporte_fragment.btnAdjunto
import kotlinx.android.synthetic.main.inventario_reporte_fragment.txtadjunto
import kotlinx.android.synthetic.main.novedad_crear_fragment.*

import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.HashMap

class InventarioReporteFragment : Fragment() {
    private val REQUEST_IMAGE_CAPTURE = 1356
    private var imgString  = ""
    private var reporte  = ""
    private lateinit var prefs: SharedPreferences
    private lateinit var checks: String
    private lateinit var imagenes: String
    private lateinit var descripciones: String
    private lateinit var danos: String

    companion object {
        fun newInstance() = InventarioReporteFragment()
    }

    private lateinit var viewModel: InventarioReporteViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.inventario_reporte_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(InventarioReporteViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(this@InventarioReporteFragment.context!!)
        progressView.visibility = View.VISIBLE
        contentView.visibility = View.GONE
        loadDanos()
        prefs.edit().putString("idanos","").apply()
        btnReporte.setOnClickListener {
            sendReport()
        }
    }

    fun dispatchTakePictureIntent(view: View) {
        Dexter.withActivity(this@InventarioReporteFragment.activity!!)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object: PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                        takePictureIntent.resolveActivity(this@InventarioReporteFragment.context!!.packageManager)?.also {
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                        }
                    }
                }
                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    Toast.makeText(this@InventarioReporteFragment.context!!, R.string.error_permissions, Toast.LENGTH_SHORT).show()
                }
                override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
                    token!!.continuePermissionRequest()
                }
            }).
                withErrorListener{ Toast.makeText(this@InventarioReporteFragment.context!!, R.string.error_permissions, Toast.LENGTH_SHORT).show()}
            .onSameThread()
            .check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                if (data != null) {
                    val imageBitmap = data.extras.get("data") as Bitmap
                    val baos = ByteArrayOutputStream()
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val b = baos.toByteArray()
                    imgString = Base64.encodeToString(b, Base64.DEFAULT)
                    btnAdjunto.setBackgroundResource(R.drawable.btn_attached)
                    txtadjunto.setText(R.string.hint_attached_image)
                }
            }
        } catch (e: Exception) {
            Log.e("FileSelectorActivity", "File select error", e)
        }
    }

    fun sendReport(){
        val idanos = prefs.getString("idanos","")
        reporte = txtReporte.text.toString()
        reporte.replace(","," ")
        Log.d("campos","danos:$idanos | descripcion:$reporte | imagen: $imgString")

        if((idanos == "" && reporte == "") || imgString == ""){
            Toast.makeText(this@InventarioReporteFragment.context, "Todos los campos son obligatorios", Toast.LENGTH_LONG).show()
        }else{
            progressView.visibility = View.VISIBLE
            contentView.visibility = View.GONE
            editArray(idanos,reporte,imgString)
            Navigation.findNavController(view!!).navigate(R.id.nav_inventario)
        }
    }

    fun editArray(inputdanos: String,inputreporte: String,inputimagen: String){
        val id = prefs.getString("position", "")
        var arrayId = prefs.getString("id_productos", "").split(",")
        val Listchecks = prefs.getString("checks", "").split(",")
        val Arraychecks = arrayListOf<String>()
        val Listimagenes = prefs.getString("imagenes", "").split(",")
        val Arrayimagenes = arrayListOf<String>()
        val Listdanos = prefs.getString("danos", "").split(",")
        val Arraydanos = arrayListOf<String>()
        val Listdespripciones = prefs.getString("descripciones", "").split(",")
        val Arraydespripciones = arrayListOf<String>()
        val position = id.toInt()
        Log.d("position",position.toString())
        //checks
        var i= 0
        Listchecks.forEach{
            var ck = it
            if(i == position){
                ck = "0"
            }
            Arraychecks.add(ck)
            i++
        }
        checks = Arraychecks.toString().replace("[","").replace("]","").replace(" ","")
        prefs.edit().putString("checks",checks).apply()
        Log.d("checks",prefs.getString("checks", ""))
        //imagenes
        i=0
        Listimagenes.forEach{
            var ck = it
            if(i == position){
                ck = inputimagen
            }
            Arrayimagenes.add(ck)
            i++
        }
        imagenes = Arrayimagenes.toString().replace("[","").replace("]","").replace(" ","")
        prefs.edit().putString("imagenes",imagenes).apply()
        Log.d("imagenes",prefs.getString("imagenes", ""))
        //descripciones
        i=0
        Listdespripciones.forEach{
            var ck = it
            if(i == position){
                ck = inputreporte
            }
            Arraydespripciones.add(ck)
            i++
        }
        descripciones = Arraydespripciones.toString().replace("[","").replace("]","").replace(" ","")
        prefs.edit().putString("descripciones",descripciones).apply()
        Log.d("descripciones",prefs.getString("descripciones", ""))
        //danos
        i=0
        Listdanos.forEach{
            var ck = it
            if(i == position){
                ck = inputdanos
            }
            Arraydanos.add(ck)
            i++
        }
        danos = Arraydanos.toString().replace("[","").replace("]","").replace("  "," ")
        prefs.edit().putString("danos",danos).apply()
        Log.d("danos",prefs.getString("danos", ""))
    }

    fun loadDanos(){
        val danos = ArrayList<Dano>()
        if (!NetworkUtils.isConnected(this@InventarioReporteFragment.context!!)) {
            Toast.makeText(this@InventarioReporteFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val queue = Volley.newRequestQueue(this@InventarioReporteFragment.context!!)
            val URL = "${Utils.URL_SERVER}productos/${prefs.getString("id_producto", "")!!}"
            val stringRequest = object : StringRequest(Method.GET, URL, Response.Listener<String> { response ->
                try {
                    var strResp = response.toString()
                    Log.d(URL,strResp)
                    val jsonObj: JSONObject = JSONObject(strResp)
                    val producto = jsonObj.getJSONObject("productos")
                    val dano = producto.getJSONArray("danos")
                    for(j in 0 until  dano.length()){
                        var dInner = dano.getJSONObject(j)
                        val id_dano = dInner.get("id_dano").toString()
                        val codigo = dInner.get("codigo").toString()
                        danos.add(
                            Dano(
                                id_dano.toInt(),
                                codigo
                            )
                        )
                        Log.d("add","$id_dano,$codigo")
                    }
                    rvPuntoProductos.layoutManager = LinearLayoutManager(this@InventarioReporteFragment.context!!,
                        LinearLayoutManager.VERTICAL,false)
                    val adapter = DanoAdapter(danos)
                    rvPuntoProductos.adapter = adapter
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@InventarioReporteFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                }
            }, Response.ErrorListener { error ->
                try {
                    error.printStackTrace()
                    Toast.makeText(this@InventarioReporteFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    Toast.makeText(this@InventarioReporteFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
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
