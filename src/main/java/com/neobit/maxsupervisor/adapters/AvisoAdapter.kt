package com.neobit.maxsupervisor.adapters

import android.content.res.Resources
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.neobit.maxsupervisor.R
import com.neobit.maxsupervisor.Utils.Utils
import com.neobit.maxsupervisor.data.model.Aviso
import com.neobit.maxsupervisor.data.model.Punto
import com.squareup.picasso.Picasso

class AvisoAdapter(val avisos:ArrayList<Aviso>): RecyclerView.Adapter<AvisoAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val v  = LayoutInflater.from(p0?.context).inflate(R.layout.avisos,p0,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
       return avisos.size
    }

    override fun onBindViewHolder(holder: ViewHolder,position: Int) {
        holder.bindItems(avisos[position])
    }

    override fun getItemId(position: Int): Long {
        try {
            return avisos[position].id_aviso.toLong()
        } catch (e:Exception) {
            e.printStackTrace()
        }
        return 0
    }

    class ViewHolder(itemView: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){
        fun bindItems(aviso: Aviso) {
            val btnCheckAviso = itemView.findViewById<ImageButton>(R.id.btnCheckAviso)
            val txtAvisoDescripcion = itemView.findViewById<TextView>(R.id.txtAvisoDescripcion)
            val txtAvisoFecha = itemView.findViewById<TextView>(R.id.txtAvisoFecha)

            txtAvisoDescripcion.text = aviso.descripcion
            txtAvisoFecha.text = aviso.hora
            btnCheckAviso.setTag(R.id.id,aviso.id_aviso_historial)
            if(aviso.validado == 1){
                btnCheckAviso.isClickable = false
                Picasso.get().load(R.drawable.ic_checked).error(R.drawable.ic_check).placeholder(R.drawable.ic_check).noFade().into(btnCheckAviso)
            }
        }


    }
}