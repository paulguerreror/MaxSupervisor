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
import com.neobit.maxsupervisor.data.model.Punto
import com.squareup.picasso.Picasso

class PuntoAdapter(val puntos:ArrayList<Punto>): RecyclerView.Adapter<PuntoAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val v  = LayoutInflater.from(p0?.context).inflate(R.layout.puntos,p0,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
       return puntos.size
    }

    override fun onBindViewHolder(holder: ViewHolder,position: Int) {
        holder.bindItems(puntos[position])
    }

    override fun getItemId(position: Int): Long {
        try {
            return puntos[position].id_punto.toLong()
        } catch (e:Exception) {
            e.printStackTrace()
        }
        return 0
    }

    class ViewHolder(itemView: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){
        fun bindItems(punto: Punto) {
            val cardPunto = itemView.findViewById<CardView>(R.id.cardPunto)
            val btnPunto = itemView.findViewById<ImageButton>(R.id.btnPunto)
            val txtPunto = itemView.findViewById<TextView>(R.id.txtPunto)
            txtPunto.text = punto.codigo
            //btnPunto.setTag(R.id.id,punto.id_punto.toString())
            cardPunto.setTag(R.id.id,punto.id_punto.toString())
        }


    }
}