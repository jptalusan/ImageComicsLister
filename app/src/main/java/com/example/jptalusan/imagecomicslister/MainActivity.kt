package com.example.jptalusan.imagecomicslister

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.db.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.jsoup.Jsoup

//TODO: basically this: http://www.androidhive.info/2016/05/android-working-with-card-view-and-recycler-view/
class MainActivity : AppCompatActivity() {
    var titlesResult: List<ComicTitle>? = null
    val titlesOnlyList: List<String> by lazy { getStringTitles(titlesResult!!) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        list.layoutManager = GridLayoutManager(applicationContext, 2)
        list.hasFixedSize()
//        startGetComics("saga", -1)

        doAsync {
//            getComicTitles(400)
            if (getComicBookTitleList().isEmpty()) {
                val pages = getPagination("https://imagecomics.com/comics/series")
                pages.forEach {
                    getComicTitles(it)
                }
            }
            titlesResult = getComicBookTitleList()

            uiThread {
                left_drawer.adapter = ArrayAdapter<String>(applicationContext, R.layout.drawer_list_item, titlesOnlyList)
            }
        }

        left_drawer.setOnItemClickListener { _, _, i, _ ->
            val link = titlesResult.let { it?.get(i)?.link }
            link.let {
                startGetComics(it!!, i)
            }
//            println(titlesResult.let { it?.get(i)?.link })
            drawer_layout.closeDrawer(left_drawer)
        }

//        val test = getComicBookTitleList()
//        test.forEach { title ->
//            println(title)
//        }
    }

    fun startGetComics(link: String, index: Int) {
        doAsync {
            val result = getComics(link, index)
            uiThread {
                //TODO: if result == 0, delete from title list? or add some other info
                if (result.isEmpty()) {
                    var temp = mutableListOf<Comic>()
                    val nullComic = Comic("null", "null", "null", "null")
                    temp.add(nullComic)
                    list.adapter = ListAdapter(applicationContext, temp)
                } else {
                    list.adapter = ListAdapter(applicationContext, result)
                }
                list.adapter.notifyDataSetChanged()
//                result.forEach { println(it) }
            }
        }
    }

    //TODO: if comic is in database, use that instead
    fun getComics(link: String, comicTitleIndex: Int): List<Comic> {
        if (getComicBooksWithTitle(titlesResult!![comicTitleIndex].title).isNotEmpty()) {
            println("$link is not empty in DB")
            return getComicBooksWithTitle(titlesResult!![comicTitleIndex].title)
        } else {
            println("$link is empty in DB, downloading first")
            val comics = mutableListOf<Comic>()
            Jsoup.connect("https://imagecomics.com/comics/release-archive/series/" + link).get().run {
                val releaseDates = select("div.book__content > p.book__text")
                val namesAndUrls = select("div.book__img > a > img")
                for ((index, value) in namesAndUrls.withIndex()) {
                    var tempName: String
                    if (comicTitleIndex == -1) {
                        tempName = "saga"
                    } else {
                        tempName = titlesResult!![comicTitleIndex].title
                    }
                    val c = Comic(
                            comicName = tempName,
                            issue = value.attr("alt").toString(),
                            url = value.attr("src").toString(),
                            release = releaseDates[index].text()
                    )
                    database.use {
                        insert(
                                comicBooks,
                                "comicBook" to c.comicName,
                                "issue" to c.issue,
                                "imageUrl" to c.url,
                                "releaseDate" to c.release
                        )
                    }
                    comics.add(c)
                }
            }
            return comics
        }
    }

    fun getStringTitles(comicTitles: List<ComicTitle>): List<String> {
        val titlesOnly = mutableListOf<String>()
        comicTitles.forEach { (title) ->
            titlesOnly.add(title)
        }
        return titlesOnly
    }

    fun getComicTitles(page: Int): List<ComicTitle> {
        println("Getting titles on page $page")
        val titles = mutableListOf<ComicTitle>()
        Jsoup.connect("https://imagecomics.com/comics/series/P$page").get().run {
            select("h2.u-mb0 > a[href]").forEach { element ->
                val url = element.attr("href").toString().split("/")
                val title = ComicTitle(
                        title = element.text(),
                        link = url[url.size - 1]
                )
                //println(title)

                database.use {
                    insert(
                            comicBookTitles,
                            "title" to title.title,
                            "link" to title.link
                    )
                }
                titles.add(title)
            }
        }
        return titles
    }

    //If less than 3? get second, as that is the last page, if more than, or if Last text() is present, get that value
    fun getPagination(url: String): List<Int> {
        val pages = mutableListOf<Int>()
        val temp = mutableListOf<Int>()
        Jsoup.connect(url).get().run {
            select("div.paginate > a[href]").forEach { element ->
                println(element.attr("href") + ", " + element.text())
                val href = element.attr("href").split("/")
                temp.add(Integer.parseInt(href[href.size - 1].substring(1, href[href.size - 1].length)))
            }
        }

        for (i in 0..(temp[temp.size - 1] + 25) step 25) {
            pages.add(i)
        }
        return pages
    }

    fun doesDatabaseExist(context: Context, dbName: String): Boolean {
        val dbFile = context.getDatabasePath(dbName)
        return dbFile.exists()
    }

    val comicBookRowParser = classParser<Comic>()
    val comicBookTitleRowParser = classParser<ComicTitle>()

    private fun getComicBooksWithTitle(comicBook: String) =
            database.use {
                select(comicBooks).whereSimple("comicBook=?", comicBook).exec {
                    parseList(comicBookRowParser)
                }
            }

    private fun getComicBookTitleList() =
            database.use {
                select(comicBookTitles).exec {
                    parseList(comicBookTitleRowParser)
                }
            }
}
