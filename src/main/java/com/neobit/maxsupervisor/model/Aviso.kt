package com.neobit.maxsupervisor.data.model

data class Aviso(
    val id_aviso_historial: Int,
    val validado: Int,
    val id_aviso: Int,
    val hora: String,
    val descripcion: String
)