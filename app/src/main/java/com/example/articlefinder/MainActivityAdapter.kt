package com.example.articlefinder

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.articlefinder.models.Pdfs
import kotlinx.android.synthetic.main.pdf_card_view.view.*

class MainActivityAdapter(val mainAdapterEvents: MainActivityAdapterEvents,val pdfsforuser:List<Pdfs>): RecyclerView.Adapter<MainActivityAdapter.ViewHolder>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.pdf_card_view,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return pdfsforuser.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind( pdfsforuser[position])
       // holder.urlOfPdf=pdfsforuser[position].pdfUrl

    }

    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView),View.OnClickListener
    {

       // lateinit var urlOfPdf:String

        val pdfName:TextView=itemView.findViewById(R.id.item_title)
        val creationTime:TextView=itemView.findViewById(R.id.item_detail)
        val star_icon:ImageView=itemView.findViewById(R.id.star_icon)
        val openpdf:TextView=itemView.findViewById(R.id.item_open)


         fun bind(pdf:Pdfs)
         {
              var name:String=pdf.pdfname.trim().toString()
               if(name.length>17) name=name.substring(0,15)+"..."
                itemView.item_title.text=name
                itemView.item_detail.text=DateUtils.getRelativeTimeSpanString(pdf.creationTimeMs)
                 itemView.setOnClickListener(this)

             star_icon.setOnClickListener(this)

         }

        override fun onClick(v: View?) {
            //implement the concept here
            val position=adapterPosition
            if(v!!.id==R.id.star_icon)
            {
                mainAdapterEvents.onOpenButtonClicked(pdfsforuser.get(position),"star")
            }
            else
            {
                mainAdapterEvents.onOpenButtonClicked(pdfsforuser.get(position),"open")
            }


           // if (v != null) {
             //   Toast.makeText(v.context,"Position is ${position}",Toast.LENGTH_SHORT).show()
            //}


        }


    }

    interface MainActivityAdapterEvents
    {
        fun onOpenButtonClicked(pdfItem: Pdfs, s: String)
    }
}
