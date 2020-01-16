package com.neobit.maxsupervisor.ui.Informes

import android.content.Context
import android.content.Intent
import android.Manifest
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.webkit.*
import android.widget.Toast
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.informe_create_fragment.*
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.util.Base64
import com.neobit.maxsupervisor.Utils.NetworkUtils
import com.neobit.maxsupervisor.Utils.Utils
import android.view.ViewGroup
import android.webkit.WebViewClient
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.ValueCallback
import android.content.res.AssetFileDescriptor
import android.provider.MediaStore
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import android.webkit.WebView
import com.neobit.maxsupervisor.R
import kotlinx.android.synthetic.main.activity_informe.*
import kotlinx.android.synthetic.main.informe_create_fragment.wvInforme
import org.json.JSONObject
import java.util.HashMap

class InformeActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences
    private val REQUEST_GET_SINGLE_FILE = 2735


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_informe)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        val actionbar = supportActionBar
        actionbar!!.title= "Informes"
        wvInforme.addJavascriptInterface(WebAppInterface(this), "Android")
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        var URL = ""
        Log.d("id_informe","id= ${prefs.getString("id_informe","")!!}")
        //URL = "http://wysihtml.com/"

        if(prefs.contains("id_informe")){
            URL = "https://admin.maxlink.ec/informe.php?id_informe=${prefs.getString("id_informe","")}&token=${prefs.getString("api_key","")!!}"
        }else{
            URL = "https://admin.maxlink.ec/informe.php?token=${prefs.getString("api_key","")!!}"
        }

        wvInforme.loadUrl(URL)
        wvInforme.setWebViewClient(WebViewClient())
        wvInforme.restoreState(savedInstanceState)
        wvInforme.settings.javaScriptEnabled = true
        wvInforme.settings.allowFileAccess = true
        wvInforme.settings.allowFileAccessFromFileURLs = true
        wvInforme.settings.allowFileAccess = true
        wvInforme.settings.allowContentAccess = true

        wvInforme.webChromeClient = object: WebChromeClient(){
            override fun onProgressChanged(view: WebView, newProgress: Int) {
            }
            override fun onShowFileChooser(webView: WebView, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: FileChooserParams):Boolean {
                var mFilePathCallback = filePathCallback
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.setType("*/*")
                val PICKFILE_REQUEST_CODE = 100
                startActivityForResult(intent, PICKFILE_REQUEST_CODE)
                return true
            }
        }
/*
        btnAdjunto.setOnClickListener{
            Dexter.withActivity(this)
                .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object: MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            openGallery()
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied) {
                            Toast.makeText(applicationContext, R.string.error_permissions, Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {
                        token.continuePermissionRequest()
                    }
                }).withErrorListener{ Toast.makeText(this, R.string.error_permissions, Toast.LENGTH_SHORT).show()}
                .onSameThread()
                .check()
        }

 */
    }

    private fun openGallery() {
        Intent(Intent.ACTION_GET_CONTENT).also {galleryIntent ->
            galleryIntent.type = "image/*"
            galleryIntent.addCategory(Intent.CATEGORY_OPENABLE).also {
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"), REQUEST_GET_SINGLE_FILE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == REQUEST_GET_SINGLE_FILE) {
                if (data != null) {
                    val contentURI = data.data
                    val afd: AssetFileDescriptor = this!!.contentResolver.openAssetFileDescriptor(contentURI,"r")!!
                    val fileSize: Long = afd.length
                    afd.close()
                    if (fileSize <= 5000000) {
                        val bitmap = MediaStore.Images.Media.getBitmap(this!!.contentResolver, contentURI)
                        //imageView.setImageBitmap(bitmap)
                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                        val b = baos.toByteArray()
                        val imgString = Base64.encodeToString(b, Base64.DEFAULT)
                        sendImage(imgString)
                    } else Toast.makeText(applicationContext, R.string.error_heavy, Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Log.e("FileSelectorActivity", "File select error", e)
        }
    }

    private fun sendImage(imagen: String) {
        if (!NetworkUtils.isConnected(applicationContext)) {
            Toast.makeText(applicationContext, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else{
            val queue = Volley.newRequestQueue(applicationContext)
            val URL = "${Utils.URL_SERVER}upload_image"
            val stringRequest = object : StringRequest(Method.POST, URL, Response.Listener<String> { response ->
                try {
                    var strResp = response.toString()
                    Log.d("$URL",strResp)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(applicationContext, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }, Response.ErrorListener { error ->
                try {
                    error.printStackTrace()
                    Toast.makeText(applicationContext, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(applicationContext, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers.put("token", prefs.getString("api_key", "")!!)
                    return headers
                }
                override fun getParams(): MutableMap<String, String> {
                    val parameters = HashMap<String, String>()
                    parameters["imagen"] = imagen
                    return parameters
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            queue.add(stringRequest)
        }
    }


    inner class WebAppInterface
    /** Instantiate the interface and set the context  */
    internal constructor(internal var mContext: Context) {

        @JavascriptInterface
        fun showToast(toast: String) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show()
        }

        @JavascriptInterface
        fun endActivity(){
            finish()
        }
    }


}
