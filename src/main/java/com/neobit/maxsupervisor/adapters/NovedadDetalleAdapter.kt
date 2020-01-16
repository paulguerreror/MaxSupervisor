package com.neobit.maxsupervisor.adapters

import android.graphics.Color
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.neobit.maxsupervisor.R
import com.neobit.maxsupervisor.Utils.Utils
import com.neobit.maxsupervisor.data.model.NovedadDetalle
import com.squareup.picasso.Picasso

class NovedadDetalleAdapter(val novedaddetalles:ArrayList<NovedadDetalle>): RecyclerView.Adapter<NovedadDetalleAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val v  = LayoutInflater.from(p0?.context).inflate(R.layout.novedaddetalles,p0,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
       return novedaddetalles.size
    }

    override fun onBindViewHolder(holder: ViewHolder,position: Int) {
        holder.bindItems(novedaddetalles[position])
    }

    override fun getItemId(position: Int): Long {
        try {
            return novedaddetalles[position].id_novedad_detalle.toLong()
        } catch (e:Exception) {
            e.printStackTrace()
        }
        return 0
    }

    class ViewHolder(itemView: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){
        fun bindItems(novedaddetalle: NovedadDetalle) {
            val cardNovedadDetalle = itemView.findViewById<CardView>(R.id.cardNovedadDetalle)

            val txtNovedadDetallefecha = itemView.findViewById<TextView>(R.id.txtNovedadDetallefecha)
            val txtNovedadDetalle = itemView.findViewById<TextView>(R.id.txtNovedadDetalle)
            val imgnovedadDetalle = itemView.findViewById<ImageView>(R.id.imgnovedadDetalle)
            val txtCreadorDetalle = itemView.findViewById<TextView>(R.id.txtCreadorDetalle)
            val btn_privacidad = itemView.findViewById<Button>(R.id.btn_privacidad)

            //cardNovedadDetalle.setTag(R.id.cliente,novedaddetalle.cliente.toString())
            btn_privacidad.setTag(R.id.id,novedaddetalle.id_novedad_detalle.toString())
            btn_privacidad.setTag(R.id.cliente,novedaddetalle.cliente.toString())

            //cardNovedadDetalle.setTag(novedaddetalle.id_novedad_detalle.toString())
            //cardNovedadDetalle.setTag(novedaddetalle.id_novedad_detalle.toString())
            txtNovedadDetallefecha.text = novedaddetalle.fecha_creacion
            txtNovedadDetalle.text = novedaddetalle.descripcion
            txtCreadorDetalle.text = novedaddetalle.creador
            if(novedaddetalle.cliente == 1){
                btn_privacidad.setBackgroundResource(R.drawable.button_publico)
                btn_privacidad.text = "Público"
            }else{
                btn_privacidad.setBackgroundResource(R.drawable.button_privado)
                btn_privacidad.text = "Privado"
            }
            if(novedaddetalle.imagen == "" || novedaddetalle.imagen == null){
                imgnovedadDetalle.visibility = View.GONE
            }else{
                Picasso.get().load(Utils.URL_MEDIA + novedaddetalle.imagen).error(R.drawable.placeholder).placeholder(R.drawable.placeholder).noFade().into(imgnovedadDetalle)
            }
        }
    }
}