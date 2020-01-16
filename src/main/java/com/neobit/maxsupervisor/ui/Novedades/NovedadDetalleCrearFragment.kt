package com.neobit.maxsupervisor.ui.Novedades

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
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.Navigation
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
import com.neobit.maxsupervisor.Utils.*

import com.neobit.maxsupervisor.R
import kotlinx.android.synthetic.main.novedad_detalle_crear_fragment.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.HashMap

class NovedadDetalleCrearFragment : Fragment() {
    private val REQUEST_IMAGE_CAPTURE = 1356
    private var imgString  = ""
    private lateinit var prefs: SharedPreferences
    companion object {
        fun newInstance() = NovedadDetalleCrearFragment()
    }

    private lateinit var viewModel: NovedadDetalleCrearViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        prefs = PreferenceManager.getDefaultSharedPreferences(this@NovedadDetalleCrearFragment.context!!)
        return inflater.inflate(R.layout.novedad_detalle_crear_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnCrearNovedadDetalle.setOnClickListener {
            createNovedadDetalle()
        }
        btnAdjunto.setOnClickListener {
            dispatchTakePictureIntent()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(NovedadDetalleCrearViewModel::class.java)
        // TODO: Use the ViewModel
    }


    fun dispatchTakePictureIntent() {
        Dexter.withActivity(this@NovedadDetalleCrearFragment.activity)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object: PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                        takePictureIntent.resolveActivity(this@NovedadDetalleCrearFragment.context!!.packageManager)?.also {
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                        }
                    }
                }
                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    Toast.makeText(this@NovedadDetalleCrearFragment.context!!, R.string.error_permissions, Toast.LENGTH_SHORT).show()
                }
                override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
                    token!!.continuePermissionRequest()
                }
            }).
                withErrorListener{ Toast.makeText(this@NovedadDetalleCrearFragment.context!!, R.string.error_permissions, Toast.LENGTH_SHORT).show()}
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

    private fun createNovedadDetalle() {
        if(txtNovedadDetalleCrear.text.toString() != "" ){
            if (!NetworkUtils.isConnected(this@NovedadDetalleCrearFragment.context!!)) {
                Toast.makeText(this@NovedadDetalleCrearFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
            } else{
                progressView.visibility = View.VISIBLE
                contentView.visibility = View.GONE
                val queue = Volley.newRequestQueue(this@NovedadDetalleCrearFragment.context!!)
                val URL = "${Utils.URL_SERVER}novedades/${prefs.getString("id_novedad","")}"
                val stringRequest = object : StringRequest(Method.POST, URL, Response.Listener<String> { response ->
                    try {
                        var strResp = response.toString()
                        Log.d("$URL",strResp)
                        val jsonObj: JSONObject = JSONObject(strResp)
                        val message = jsonObj.get("message").toString()
                        progressView.visibility = View.GONE
                        contentView.visibility = View.VISIBLE
                        Toast.makeText(this@NovedadDetalleCrearFragment.context!!, message, Toast.LENGTH_LONG).show()
                        Navigation.findNavController(view!!).popBackStack(R.id.nav_novedad_detalles,false)


                    } catch (e: Exception) {
                        progressView.visibility = View.GONE
                        contentView.visibility = View.VISIBLE
                        e.printStackTrace()
                        Toast.makeText(this@NovedadDetalleCrearFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    }
                }, Response.ErrorListener { error ->
                    try {
                        progressView.visibility = View.GONE
                        contentView.visibility = View.VISIBLE
                        error.printStackTrace()
                        Toast.makeText(this@NovedadDetalleCrearFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        progressView.visibility = View.GONE
                        contentView.visibility = View.VISIBLE
                        Toast.makeText(this@NovedadDetalleCrearFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    }
                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers.put("token", prefs.getString("api_key", "")!!)
                        return headers
                    }
                    override fun getParams(): MutableMap<String, String> {
                        val parameters = HashMap<String, String>()
                        parameters["descripcion"] = txtNovedadDetalleCrear.text.toString()
                        parameters["imagen"] = imgString
                        return parameters
                    }
                }
                stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                queue.add(stringRequest)
            }
        }else{
            Log.e("error","no id_tarea_historial")
        }
    }


}
