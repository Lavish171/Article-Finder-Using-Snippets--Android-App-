package com.example.articlefinder

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.articlefinder.models.Pdfs
import com.example.articlefinder.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.itextpdf.text.pdf.PdfFileSpecification.url
import kotlinx.android.synthetic.main.activity_main.*


private  const val TAG="MainActivity"


class MainActivity :BaseCompatActivity(),MainActivityAdapter.MainActivityAdapterEvents {

    private lateinit var pdfsforuser:MutableList<Pdfs>
    private lateinit var  firestoreDb: FirebaseFirestore
    private lateinit var adapter: MainActivityAdapter
    private var signedUser: User?=null

    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUpRecylerViewAndAdapter()
        setUpViews()

    }

    private fun setUpRecylerViewAndAdapter() {
        pdfsforuser= mutableListOf()
        Toast.makeText(this,"In SetUpRecylerView",Toast.LENGTH_SHORT).show()
        adapter= MainActivityAdapter(this,pdfsforuser)
        recyclerViewMainActivityPdf.layoutManager=LinearLayoutManager(this)
        recyclerViewMainActivityPdf.adapter=adapter

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
                signedUser?.let { fethingUsersPdf(it) }

            }
            .addOnFailureListener { exception ->
                Log.i(TAG,"Error In Fetching Signed In User ",exception)
            }

    }

    private fun setUpViews()
    {

            search_article_icon.setOnClickListener {
                if(search_article_text.text.isBlank())
                {
                    Toast.makeText(this,"Search Bar Cannot be Empty",Toast.LENGTH_SHORT).show()
                }
                else {
                    val articleNameToSearch=search_article_text.text.toString().trim()
                    //now to pass this name that the user has typed to back end api
                    Toast.makeText(this, "Going To Article Result Activity", Toast.LENGTH_SHORT).show()
                    searchArticleAfterTypingName()
                }
        }
    }

    private  fun searchArticleAfterTypingName()
    {
        val intent=Intent(this,ArticleResultActivity::class.java)
        startActivity(intent)
    }

    fun fethingUsersPdf(signedUser:User) {

        Log.i(TAG, "Check Signed In User ${signedUser}")
        var postsReference =
            firestoreDb.collection("pdfs").orderBy("creationTimeMs", Query.Direction.DESCENDING)
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
            pdfsforuser.clear()
            pdfsforuser.addAll(pdfList)
            adapter.notifyDataSetChanged()
            //we got some data to show
            for (pdf in pdfList) {
                Log.i(TAG, "Info $pdf")
            }
        }
    }

    override fun onOpenButtonClicked(pdfItem: Pdfs, s: String)
    {
        //override the open button click handle
       // Toast.makeText(this,"Items clicked",Toast.LENGTH_SHORT).show()
        if(s=="star")
        {
            val builder = AlertDialog.Builder(this)
            //set title for alert dialog
            builder.setTitle("Save PDF")
            //set message for alert dialog
            builder.setMessage("Are you sure you want to save the pdf ?..")
            builder.setIcon(android.R.drawable.ic_dialog_alert)

            //performing positive action
            builder.setPositiveButton("Yes"){dialogInterface, which ->
                Toast.makeText(applicationContext,"clicked yes",Toast.LENGTH_LONG).show()
                lateinit var  firestoreDbSavedPdf: FirebaseFirestore
                firestoreDbSavedPdf= FirebaseFirestore.getInstance()

                Log.i("pdf user", pdfItem.pdfname.toString())
                val savepdf=Pdfs(pdfItem.pdfname,pdfItem.pdfUrl,pdfItem.creationTimeMs,pdfItem.user)

                firestoreDbSavedPdf.collection("savedpdf").add(savepdf).addOnSuccessListener {
                    Toast.makeText(this,"Pdf Saved",Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this,"Error In Saving Pdf",Toast.LENGTH_SHORT).show()
                }
            }

            builder.setNeutralButton("No "){dialogInterface , which ->
                Toast.makeText(applicationContext,"clicked No",Toast.LENGTH_LONG).show()
            }

            //performing negative action
            // Create the AlertDialog
            val alertDialog: AlertDialog = builder.create()
            // Set other dialog properties
            alertDialog.setCancelable(false)
            alertDialog.show()

        }
        else if(s=="open")
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
        }//else block ends
    }



}