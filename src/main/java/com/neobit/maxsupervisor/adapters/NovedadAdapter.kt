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
import com.neobit.maxsupervisor.data.model.Novedad
import com.squareup.picasso.Picasso

class NovedadAdapter(val novedades:ArrayList<Novedad>): RecyclerView.Adapter<NovedadAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val v  = LayoutInflater.from(p0?.context).inflate(R.layout.novedades,p0,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
       return novedades.size
    }

    override fun onBindViewHolder(holder: ViewHolder,position: Int) {
        holder.bindItems(novedades[position])
    }

    override fun getItemId(position: Int): Long {
        try {
            return novedades[position].id_novedad.toLong()
        } catch (e:Exception) {
            e.printStackTrace()
        }
        return 0
    }

    class ViewHolder(itemView: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){
        fun bindItems(novedad: Novedad) {
            val cardNovedad = itemView.findViewById<CardView>(R.id.cardNovedad)

            val txtnovedadfecha = itemView.findViewById<TextView>(R.id.txtnovedadfecha)
            val txtnovedad = itemView.findViewById<TextView>(R.id.txtnovedad)
            val imgnovedad = itemView.findViewById<ImageView>(R.id.imgnovedad)
            val txtCreador = itemView.findViewById<TextView>(R.id.txtCreador)
            val cardPrivacidad = itemView.findViewById<CardView>(R.id.cardprivacidad)
            val txtprivacidad = itemView.findViewById<TextView>(R.id.txtprivacidad)
            cardPrivacidad.setTag(R.id.cliente,novedad.cliente.toString())
            cardNovedad.setTag(R.id.id,novedad.id_novedad.toString())
            txtnovedadfecha.text = novedad.fecha_creacion
            txtnovedad.text = novedad.descripcion
            txtCreador.text = novedad.creador
            if(novedad.cliente == 1){
                cardPrivacidad.setCardBackgroundColor(Color.parseColor("#26dad2"))
                txtprivacidad.text = "Público"
            }else{
                cardPrivacidad.setCardBackgroundColor(Color.parseColor("#ef5350"))
                txtprivacidad.text = "Privado"
            }
            if(novedad.imagen == "" || novedad.imagen == null){
                imgnovedad.visibility = View.GONE
            }else{
                Picasso.get().load(Utils.URL_MEDIA + novedad.imagen).error(R.drawable.placeholder).placeholder(R.drawable.placeholder).noFade().into(imgnovedad)
            }
        }
    }
}