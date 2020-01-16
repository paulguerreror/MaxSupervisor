package com.neobit.maxsupervisor.ui.Informes

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.util.Log
import android.os.Build
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.ValueCallback;

import android.webkit.WebView
import android.widget.Toast
import androidx.navigation.Navigation
import com.neobit.maxsupervisor.R
import kotlinx.android.synthetic.main.informe_create_fragment.*
import kotlinx.android.synthetic.main.informes_fragment.*

class InformeCreateFragment : Fragment() {
    private lateinit var prefs: SharedPreferences
    private var mUploadMessage: ValueCallback<Uri>? = null
    private var uploadMessage: ValueCallback<Array<Uri>>? = null
     val FILECHOOSER_RESULTCODE = 1
     val REQUEST_SELECT_FILE = 100

    companion object {
        fun newInstance() = InformeCreateFragment()
    }

    private lateinit var viewModel: InformeCreateViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true)
        }
        return inflater.inflate(R.layout.informe_create_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wvInforme.addJavascriptInterface(WebAppInterface(this@InformeCreateFragment.context!!), "Android")
        prefs = PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext)
        var URL = ""
        Log.d("id_informe","id= ${prefs.getString("id_informe","")!!}")
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
            override fun onShowFileChooser(webView:WebView, filePathCallback:ValueCallback<Array<Uri>>, fileChooserParams:FileChooserParams):Boolean {
                var mFilePathCallback = filePathCallback
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.setType("*/*")
                val PICKFILE_REQUEST_CODE = 100
                startActivityForResult(intent, PICKFILE_REQUEST_CODE)
                return true
            }

        }

    }




    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(InformeCreateViewModel::class.java)
        // TODO: Use the ViewModel
    }


     fun onBackPressed(){
         Navigation.findNavController(view!!).navigate(R.id.nav_informe)
    }

    override fun onDetach() {
        prefs.edit().remove("id_informe").apply()
        super.onDetach()
    }

    inner class WebAppInterface
    /** Instantiate the interface and set the context  */
    internal constructor(internal var mContext: Context) {

        @JavascriptInterface
        fun showToast(toast: String) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show()
        }
        @JavascriptInterface
        fun editText(Text: String){
            //txt_from_web.text = Text
        }

        @JavascriptInterface
        fun informeCreated(message: String){
            //txt_from_web.text = Text
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
        }
    }

}

