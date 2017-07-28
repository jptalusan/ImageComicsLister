package com.example.jptalusan.imagecomicslister

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.list_item.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.jsoup.Jsoup

//TODO: basically this: http://www.androidhive.info/2016/05/android-working-with-card-view-and-recycler-view/
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        list.layoutManager = GridLayoutManager(applicationContext, 2)
        list.hasFixedSize()
        doAsync {
            val result = getComics()
            uiThread {
                list.adapter = ListAdapter(applicationContext, result)
                result.forEach { println(it) }
            }
        }

    }

    fun getComics(): List<Comic> {
        val comics = mutableListOf<Comic>()
        Jsoup.connect("https://imagecomics.com/comics/release-archive/series/paper-girls").get().run {
//            getElementsByClass("book__img").forEach { element ->
//                println(element.getElementsByTag("img"))
//            }
            val releaseDates = select("div.book__content > p.book__text")
            val namesAndUrls = select("div.book__img > a > img")
            for ((index, value) in namesAndUrls.withIndex()) {
                val c = Comic(
                        title = value.attr("alt").toString(),
                        url = value.attr("src").toString(),
                        release = releaseDates[index].text()
                )
                comics.add(c)
            }
        }
        return comics
    }

    data class Comic(val title: String = "", val url: String = "", val release: String = "")

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
                itemView.titleTV.text = _comic.title
                itemView.releaseDateTV.text = _comic.release
                Glide.with(c)
                        .load(_comic.url)
                        .into(itemView.coverIV)
                        .onLoadStarted(c.resources.getDrawable(R.mipmap.ic_launcher_round))
//                itemView.titleTV.text = _comic.title
            }
        }
    }
}
