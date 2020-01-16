package com.neobit.maxsupervisor.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import com.neobit.maxsupervisor.R
import com.neobit.maxsupervisor.model.Producto

class ProductoAdapter(val inventario:ArrayList<Producto>):
    androidx.recyclerview.widget.RecyclerView.Adapter<ProductoAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val v  = LayoutInflater.from(p0?.context).inflate(R.layout.productos,p0,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
       return inventario.size
    }

    override fun onBindViewHolder(holder: ViewHolder,position: Int) {
        holder.bindItems(inventario[position])
    }

    override fun getItemId(position: Int): Long {
        try {
            return inventario[position].id_producto.toLong()
        } catch (e:Exception) {
            e.printStackTrace()
        }
        return 0
    }

    class ViewHolder(itemView: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){
        fun bindItems(producto: Producto) {
            val tvnombre = itemView.findViewById<TextView>(R.id.tvnombre)
            val tvmarca = itemView.findViewById<TextView>(R.id.tvmarca)
            val btncard = itemView.findViewById<ImageButton>(R.id.btnReport)
            val ckProducto = itemView.findViewById<CheckBox>(R.id.ckProducto)
            if(producto.checked == "1"){
                ckProducto.isChecked = true
            }else if(producto.checked == "0"){
                ckProducto.isChecked = false
               // Picasso.get().load(R.drawable.reporteapro).error(R.drawable.reporte).placeholder(R.drawable.reporte).noFade().into(btncard)
            }
            //ckProducto.setTag(producto.position.toString())
           // btncard.setTag(producto.position.toString())
            btncard.setTag(R.id.position,producto.position.toString())
            ckProducto.setTag(R.id.position,producto.position.toString())

            btncard.setTag(R.id.id,producto.id_producto.toString())
            ckProducto.setTag(R.id.id,producto.id_producto.toString())

            tvmarca.text = producto.marca
            tvnombre.text = producto.nombre
        }


    }
}