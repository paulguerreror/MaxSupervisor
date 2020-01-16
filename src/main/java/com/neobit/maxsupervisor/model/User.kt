package com.neobit.maxsupervisor.data.model

data class User(
    val id_guardia: Int,
    val codigo: String,
    val nombres: String,
    val imagen: String,
    val supervisor: Int,
    val api_key:String
)