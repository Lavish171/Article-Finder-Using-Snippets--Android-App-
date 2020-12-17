package com.example.articlefinder

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.articlefinder.models.Pdfs
import com.example.articlefinder.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_saved_articles.*

private  const val TAG="SavedArticlesActivity"


class SavedArticlesActivity : BaseCompatActivity(),SavedArticlesActivityAdapter.SavedArticlesActivityAdapterEvents {

    private lateinit var savedpdfsforuser:MutableList<Pdfs>
    private lateinit var  firestoreDb: FirebaseFirestore
    private lateinit var adapter: SavedArticlesActivityAdapter
    private var signedUser: User?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_articles)

        setUpRecylerViewAndAdapter()
    }

    private fun setUpRecylerViewAndAdapter() {
        savedpdfsforuser= mutableListOf()
        Toast.makeText(this,"In SetUpRecylerView", Toast.LENGTH_SHORT).show()
        adapter= SavedArticlesActivityAdapter(this,savedpdfsforuser)
        saveArticleRecyclerView.layoutManager= LinearLayoutManager(this)
        saveArticleRecyclerView.adapter=adapter

        firestoreDb= FirebaseFirestore.getInstance()
        //edit

        firestoreDb.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get()
            .addOnSuccessListener {documentSnapshot ->
                signedUser=documentSnapshot.toObject(User::class.java)
                Log.i(TAG,"Signed In user $signedUser")
                if(signedUser!=null)
                    Log.i(TAG,"Let's see current email id ${signedUser!!.emailIdUser.toString()}")
                signedUser?.let { fethingUsersSavedPdf(it) }

            }
            .addOnFailureListener { exception ->
                Log.i(TAG,"Error In Fetching Signed In User ",exception)
            }
    }//setUpRecylerViewAndAdapter ends


    fun fethingUsersSavedPdf(signedUser:User) {

        Log.i(TAG, "Check Signed In User ${signedUser}")
        var postsReference =
            firestoreDb.collection("savedpdf").orderBy("creationTimeMs", Query.Direction.DESCENDING)
        Log.i(TAG, "reference ${postsReference.toString()}")

        Log.i(TAG, "extra tag $signedUser")
        //val signedInUserMailId:String=signedUser?.emailIdUser.toString()
        //  Log.i(TAG,"mail id is $signedInUserMailId")

        val signedInUserMailId: String = signedUser?.emailIdUser.toString()
        Log.i(TAG, "mail id is $signedInUserMailId")
        val signedInUserUId: String = signedUser?.uid.toString()
        Log.i(TAG, "uid is $signedInUserUId")
        Log.i(TAG, "signed in inner user is $signedUser")

        postsReference =
            postsReference.whereEqualTo("user.emailIdUser", signedUser.emailIdUser.toString())


        // Log.i(TAG,"Emai id ${emailId.toString()}")
        postsReference.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            //if the query is not null,then querySnapshot will contain the data
            //else if query is null,exception would not be null
            if (querySnapshot == null || firebaseFirestoreException != null) {
                Log.i(TAG, "Exception when querying posts", firebaseFirestoreException)
                return@addSnapshotListener
            }

            Log.i(TAG, "query snapshot ${querySnapshot.toString()}")
            val pdfList = querySnapshot.toObjects(Pdfs::class.java)
            Log.i("PDf List", pdfList.toString())
            //as we got some updated data from firestore
            //we will tell the adapter that we have
            //received some updated data
            for (pdf in pdfList) {
                Log.i(TAG, "Info $pdf")
            }
            savedpdfsforuser.clear()
            savedpdfsforuser.addAll(pdfList)
            adapter.notifyDataSetChanged()
            //we got some data to show
            for (pdf in pdfList) {
                Log.i(TAG, "Info $pdf")
            }
        }
    }

    override fun onOpenButtonClicked(pdfItem: Pdfs, s: String)
    {
        Toast.makeText(this,"Open Button Clicked",Toast.LENGTH_SHORT).show()
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse(pdfItem.pdfUrl.toString()), "application/pdf")
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        val newIntent = Intent.createChooser(intent, "Open File")
        try {
            startActivity(newIntent)
        } catch (e: ActivityNotFoundException) {
            // Instruct the user to install a PDF reader here, or something
        }
    }
}