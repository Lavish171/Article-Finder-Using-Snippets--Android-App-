package com.example.articlefinder

//import androidx.appcompat.app.AppCompatActivity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.example.articlefinder.models.Pdfs
import com.example.articlefinder.models.User
import com.google.android.gms.tasks.Task
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.drawer_header.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File


//import java.lang.Exception
//import kotlin.Exception

abstract class BaseCompatActivity : AppCompatActivity() {
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    lateinit var storage: FirebaseStorage
    private val FILE_STORAGE_CODE=1234
    private val GALLERY_CODE=1000
    private val auth:FirebaseAuth=FirebaseAuth.getInstance()
    private val fstore:FirebaseFirestore= FirebaseFirestore.getInstance()
    private val TAG="BaseCompatActivity"
    private lateinit var pdfUri:Uri
    private var signedUser: User?=null


    private val storageReference:StorageReference=FirebaseStorage.getInstance().reference


    override fun setContentView(layoutResID: Int) {
        val drawerLayout = layoutInflater.inflate(R.layout.activity_base_compat, null) as DrawerLayout
        setUpNavigationView(drawerLayout)
        setUpDrawerLayout(drawerLayout)
        val activityContainer = drawerLayout.findViewById<FrameLayout>(R.id.activityContainer)
        layoutInflater.inflate(layoutResID, activityContainer, true)

        super.setContentView(drawerLayout)
    }

    private fun setUpNavigationView(drawerLayout: DrawerLayout) {
        val navigationView = drawerLayout.findViewById<NavigationView>(R.id.navigation_View)
        navigationView.itemIconTintList = null
       val headerView: View =navigationView.inflateHeaderView(R.layout.drawer_header)
       val emailTextViewUserProfile:TextView=headerView.findViewById(R.id.userprofile_email_id)
        val userImageUserProfile:ImageView=headerView.findViewById(R.id.userprofile_image)


        //setting up the user profile image and uploading it to the firestore

        val profileReferenceInitialOnAppStart:StorageReference
                = storageReference.child("users/"+auth.currentUser?.uid+ "/profile.jpg")
        profileReferenceInitialOnAppStart.downloadUrl.addOnSuccessListener {
            Glide.with(this).load(it).into(userImageUserProfile)
        }


        //setting up the user profile image and uploading it to the firestore
        userImageUserProfile.setOnClickListener {
           val  openGalleryIntent:Intent=Intent(Intent.ACTION_PICK,
               MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

            startActivityForResult(openGalleryIntent,GALLERY_CODE)
        }

        val userId=auth.currentUser?.uid
        var documentReference:DocumentReference=fstore.collection("users").
                document(userId.toString())
        documentReference.addSnapshotListener { value, error ->
           if(value!=null && value.exists())
           {
               Log.i(TAG,"Document Fethced Successfully")
               emailTextViewUserProfile.text=value.get("emailIdUser").toString()
           }
            else
           {
              Log.i(TAG,error.toString())
           }
        }



        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {

                R.id.add_files -> {
                    Toast.makeText(this, "Add Files", Toast.LENGTH_SHORT).show()
                    addFiles()
                    true
                }
                R.id.saved_files-> {
                    Toast.makeText(this, "Saved Files", Toast.LENGTH_SHORT).show()
                    val intent=Intent(this,SavedArticlesActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.android_Version-> {
                    Toast.makeText(this, "Android Version", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.logout-> {
                    //Toast.makeText(this, "LogOut", Toast.LENGTH_SHORT).show()
                    Toast.makeText(this,"User wants to logout", Toast.LENGTH_SHORT).show()
                    FirebaseAuth.getInstance().signOut()
                    //  val intent=
                    val intent= Intent(this,LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent)
                    true
                }

                R.id.about_app->
                {
                    Toast.makeText(this,"About App", Toast.LENGTH_SHORT).show()
                    val intent= Intent(this,AboutAppActivity::class.java)
                    startActivity(intent)
                    true
                }
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun setUpDrawerLayout(drawerLayout: DrawerLayout) {
        val appBar = drawerLayout.findViewById<MaterialToolbar>(R.id.appBar)
        setSupportActionBar(appBar)
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.app_name, R.string.app_name)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
    }

    private fun addFiles()
    {

        fstore.collection("users")
            .document(auth.currentUser?.uid as String)
            .get()
            .addOnSuccessListener {documentSnapshot ->
                signedUser=documentSnapshot.toObject(User::class.java)
                Log.i(TAG,"Signed In user Just  $signedUser")
            }
            .addOnFailureListener { exception ->
                Log.i(TAG,"Error In Fetching Signed In User ",exception)
            }

        val intent=Intent()
        intent.type = "application/pdf"
        intent.action=Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent,"Select Pdf File"),FILE_STORAGE_CODE)
    }



    //on activity result for add file to the firebase
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==FILE_STORAGE_CODE && resultCode== Activity.RESULT_OK && data!=null)
        {
           //if request code is equal to file storage code,upload the file now to firestore
            pdfUri= data.data!!
            Toast.makeText(this,"FIle is Being Selected",Toast.LENGTH_SHORT).show()
            if(pdfUri!=null)
            {
                uploadFiles(pdfUri)
            }
        }

        if(requestCode==GALLERY_CODE)
        {
            if(resultCode==Activity.RESULT_OK)
                run {
                    val userProfileImageUri: Uri? = data?.data
                    //userprofile_image.setImageURI(userProfileImageUri)

                    GlobalScope.launch (Dispatchers.IO){
                        uploadImageToFirebaseStorage(userProfileImageUri)
                    }
                }
        }
    }

    private fun uploadFiles(pdfUri: Uri) {

        var fileData:Uri=pdfUri
        val file = File(fileData.getPath())
        Log.i(TAG,"file name ${file.name.toString()}")
          var ref=storageReference.child("Uploads/"+auth.currentUser?.uid+"/${file.nameWithoutExtension}")

         val uploadTask=ref.putFile(pdfUri)


        val urlTask = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this,"Uploaded Successfully",Toast.LENGTH_SHORT).show()
                val downloadUri = task.result
                val firestoreDb=FirebaseFirestore.getInstance()

                Log.i(TAG,"Signed above In user $signedUser")
                    val pdf=Pdfs(file.nameWithoutExtension,downloadUri.toString(),System.currentTimeMillis(),signedUser)
                Log.i(TAG,"Signed Near In user $signedUser")
                Log.i(TAG,"pdf content ${pdf.toString()}")
                firestoreDb.collection("pdfs").add(pdf)

                Log.i(TAG,"Downloaded Uri ${downloadUri.toString()}")
            } else {
                // Handle failures
                // ...
            }
        }





    }

    private fun uploadImageToFirebaseStorage(userProfileImageUri: Uri?)
     {
        val  fileReference:StorageReference=storageReference.child("users/"+auth.currentUser?.uid+ "/profile.jpg")
         if (userProfileImageUri != null) {
             fileReference.putFile(userProfileImageUri).addOnSuccessListener {
                fileReference.downloadUrl.addOnSuccessListener {
                    Glide.with(this).load(userProfileImageUri).into(userprofile_image)
                    Log.i(TAG,"Downloaded Url ${it.toString()}")
                    val ref=FirebaseFirestore.getInstance().collection("users").document(auth.uid.toString())
                    ref.update("imageUrl",it.toString())
                }

             }.addOnFailureListener {
                 Toast.makeText(this,"Image Upload Failed",Toast.LENGTH_SHORT)
                     .show()
             }
         }
     }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {

            return true
        }
        return super.onOptionsItemSelected(item)
    }

}