package com.example.articlefinder.models

data class Pdfs( var pdfname:String="",
                 var pdfUrl:String="",
                 var creationTimeMs:Long=0,
                 var user:User?=null)