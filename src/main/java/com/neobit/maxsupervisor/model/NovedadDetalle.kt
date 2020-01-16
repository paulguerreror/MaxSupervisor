package com.neobit.maxsupervisor.data.model

data class NovedadDetalle(
    val id_novedad_detalle: Int,
    val cliente: Int,
    val creador: String,
    val tipo: String,
    val descripcion: String,
    val imagen: String,
    val fecha_creacion: String
)