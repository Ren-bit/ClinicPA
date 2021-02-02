package com.example.clinicpa.loginregister

data class User(
    var email :String? = "",
    var username :String? ="",
    var password :String? ="",
    var uid :String? = "",
    var userType : Int
)
