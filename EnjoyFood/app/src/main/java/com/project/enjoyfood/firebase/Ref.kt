package com.project.enjoyfood.firebase

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class Ref {
    companion object {

        private val database = Firebase.database

        val boardRef = database.getReference("board")
        val restaurantRef = database.getReference("restaurant")
    }
}