package com.neobit.maxsupervisor.adapters

import android.content.res.Resources
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.neobit.maxsupervisor.R
import com.neobit.maxsupervisor.Utils.Utils
import com.neobit.maxsupervisor.data.model.Informe
import com.squareup.picasso.Picasso

class InformeAdapter(val informes:ArrayList<Informe>): RecyclerView.Adapter<InformeAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val v  = LayoutInflater.from(p0?.context).inflate(R.layout.informes,p0,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
       return informes.size
    }

    override fun onBindViewHolder(holder: ViewHolder,position: Int) {
        holder.bindItems(informes[position])
    }

    override fun getItemId(position: Int): Long {
        try {
            return informes[position].id_informe.toLong()
        } catch (e:Exception) {
            e.printStackTrace()
        }
        return 0
    }

    class ViewHolder(itemView: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){
        fun bindItems(informe: Informe) {
            val cardInforme = itemView.findViewById<CardView>(R.id.cardInforme)
            val txtInformeFecha = itemView.findViewById<TextView>(R.id.txtInformeFecha)
            val txtInformeDescripcion = itemView.findViewById<TextView>(R.id.txtInformeDescripcion)
            cardInforme.setTag(R.id.id,informe.id_informe.toString())
            txtInformeFecha.text = informe.fecha_creacion
            if(informe.descripcion.length >= 100){
                txtInformeDescripcion.text = informe.descripcion.take(100) + "..."
            }else{
                txtInformeDescripcion.text = informe.descripcion
            }
        }
    }
}