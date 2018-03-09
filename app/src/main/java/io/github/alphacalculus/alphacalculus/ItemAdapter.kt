package io.github.alphacalculus.alphacalculus

import android.content.Context
import android.content.Intent
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.bumptech.glide.Glide

class ItemAdapter(private val mHomeItemList: List<ChapterItem>) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    private var mContext: Context? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var cardView: CardView
        var itemImage: ImageView
        var itemName: TextView

        init {
            cardView = view as CardView
            itemImage = view.findViewById(R.id.item_image) as ImageView
            itemName = view.findViewById(R.id.item_name) as TextView
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (mContext == null) {
            mContext = parent.context
        }
        val view = LayoutInflater.from(mContext).inflate(R.layout.home_item, parent, false)
        val holder = ViewHolder(view)
        holder.cardView.setOnClickListener {
            val position = holder.adapterPosition
            val homeItem = mHomeItemList[position]
            val intent = Intent(mContext, ItemActivity::class.java)
            intent.putExtra(ItemActivity.CHAPTER_ITEM, homeItem)
            mContext!!.startActivity(intent)
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val homeItem = mHomeItemList[position]
        holder.itemName.text = homeItem.name
        Glide.with(mContext).load(homeItem.imageId).into(holder.itemImage)
    }

    override fun getItemCount(): Int {
        return mHomeItemList.size
    }

    companion object {

        private val TAG = "ItemAdapter"
    }

}
