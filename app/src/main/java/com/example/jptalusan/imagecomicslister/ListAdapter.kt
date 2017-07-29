package com.example.jptalusan.imagecomicslister

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.list_item.view.*

class ListAdapter(var c: Context, var lists: List<Comic>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        val v = LayoutInflater.from(c).inflate(R.layout.list_item, parent, false)
        return Item(v)
    }

    override fun getItemCount(): Int {
        return lists.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        (holder as Item).bindData(c, lists[position])
    }

    class Item(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindData(c: Context, _comic: Comic) {
            itemView.titleTV.text = _comic.issue
            itemView.releaseDateTV.text = _comic.release
            Glide.with(c)
                    .load(_comic.url)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(itemView.coverIV)
        }
    }
}