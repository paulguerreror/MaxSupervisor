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
import com.neobit.maxsupervisor.model.Producto
import com.squareup.picasso.Picasso

class PuntoProductoAdapter(val puntoproductos:ArrayList<Producto>): RecyclerView.Adapter<PuntoProductoAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val v  = LayoutInflater.from(p0?.context).inflate(R.layout.punto_productos,p0,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
       return puntoproductos.size
    }

    override fun onBindViewHolder(holder: ViewHolder,position: Int) {
        holder.bindItems(puntoproductos[position])
    }

    override fun getItemId(position: Int): Long {
        try {
            return puntoproductos[position].id_producto.toLong()
        } catch (e:Exception) {
            e.printStackTrace()
        }
        return 0
    }

    class ViewHolder(itemView: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){
        fun bindItems(producto: Producto) {
            val txtPuntoProductoCodigo = itemView.findViewById<TextView>(R.id.txtPuntoProductoCodigo)
            val txtPuntoProductoMarca = itemView.findViewById<TextView>(R.id.txtPuntoProductoMarca)
            txtPuntoProductoCodigo.text = producto.nombre
            txtPuntoProductoMarca.text = producto.marca
        }


    }
}