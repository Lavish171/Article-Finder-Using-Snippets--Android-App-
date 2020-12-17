package com.example.articlefinder

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.articlefinder.models.Pdfs
import kotlinx.android.synthetic.main.pdf_card_view.view.*

class SavedArticlesActivityAdapter(val savedArticlesActivityAdapterEvents: SavedArticlesActivityAdapterEvents, val savedpdfsforuser:List<Pdfs>):
    RecyclerView.Adapter<SavedArticlesActivityAdapter.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedArticlesActivityAdapter.ViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.pdf_card_view_saved_articles,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return savedpdfsforuser.size
    }

    override fun onBindViewHolder(holder: SavedArticlesActivityAdapter.ViewHolder, position: Int) {
        holder.bind( savedpdfsforuser[position])
        // holder.urlOfPdf=pdfsforuser[position].pdfUrl

    }

    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView), View.OnClickListener
    {

        // lateinit var urlOfPdf:String

        val pdfName: TextView =itemView.findViewById(R.id.item_title)
        val creationTime: TextView =itemView.findViewById(R.id.item_detail)
        val openpdf: TextView =itemView.findViewById(R.id.item_open)


        fun bind(pdf:Pdfs)
        {
            var name:String=pdf.pdfname.trim().toString()
            if(name.length>17) name=name.substring(0,15)+"..."
            itemView.item_title.text=name
            itemView.item_detail.text= DateUtils.getRelativeTimeSpanString(pdf.creationTimeMs)
            itemView.setOnClickListener(this)

        }

        override fun onClick(v: View?) {
            //implement the concept here
            val position=adapterPosition

                savedArticlesActivityAdapterEvents.onOpenButtonClicked(savedpdfsforuser.get(position),"open")

        }


    }


    interface SavedArticlesActivityAdapterEvents
    {
        fun onOpenButtonClicked(pdfItem: Pdfs, s: String)
    }
}