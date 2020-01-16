package com.neobit.maxsupervisor.model


data class Producto(
    val id_producto: Int,
    val nombre: String,
    val marca:String,
    val checked: String,
    val position: Int
)