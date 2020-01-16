package com.neobit.maxsupervisor.Utils

import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.neobit.maxsupervisor.R
import com.neobit.maxsupervisor.Utils.*
import com.squareup.picasso.Picasso
import org.json.JSONArray
import java.lang.Exception

class InfoWindowPunto: GoogleMap.InfoWindowAdapter {

    private var context: Context
    private var data: JSONArray

    constructor(ctx: Context, array: JSONArray) {
        context = ctx
        data = array
    }
    override fun getInfoContents(p0: Marker?): View {
        val view = (context as Activity).layoutInflater .inflate(R.layout.item_info_punto, null)
        val textNombre = view.findViewById<TextView>(R.id.textNombre)
        val textDireccion = view.findViewById<TextView>(R.id.textDireccion)
        val imgIcon = view.findViewById<ImageView>(R.id.imgIcon)

        val cliente = data.getJSONObject(p0!!.tag as Int).getJSONObject("cliente")
        textNombre.text = cliente.getString("nombres")
        textDireccion.text = cliente.getString("direccion")
        return view
    }

    override fun getInfoWindow(p0: Marker?): View {
        val view = (context as Activity).layoutInflater .inflate(R.layout.item_info_punto, null)
        val textNombre = view.findViewById<TextView>(R.id.textNombre)
        val textDireccion = view.findViewById<TextView>(R.id.textDireccion)
        val textTelefono = view.findViewById<TextView>(R.id.textTelefono)

        val imgIcon = view.findViewById<ImageView>(R.id.imgIcon)

        val cliente = data.getJSONObject(p0!!.tag as Int).getJSONObject("cliente")
        textNombre.text = "${cliente.getString("nombres")}"
        textDireccion.text = cliente.getString("direccion")
        textTelefono.text = cliente.getString("telefono")

        if (!cliente.getString("imagen").isNullOrBlank())
            Picasso.get().load(Utils.URL_MEDIA +cliente.getString("imagen")).error(R.drawable.ic_maximseg).placeholder(R.drawable.placeholder).noFade().into(imgIcon, object: com.squareup.picasso.Callback {
                override fun onSuccess() {
                    if (!cliente.has("loaded")) {
                        cliente.put("loaded", "1")
                        p0.hideInfoWindow()
                        p0.showInfoWindow()
                    }
                }
                override fun onError(e: Exception?) {
                    // Nothing to do here
                }
            })
        return view
    }
}