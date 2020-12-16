package com.example.articlefinder.daos

import com.example.articlefinder.models.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UserDao {
    val db=FirebaseFirestore.getInstance()
    val usersCollection=db.collection("users")

    fun addUser(user:User)
    {
        user?.let {
            GlobalScope.launch {
                usersCollection.document(user.uid).set(it)
            }

        }
    }

}