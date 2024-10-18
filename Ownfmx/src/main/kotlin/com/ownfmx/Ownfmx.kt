package com.Phisher98

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.extractors.StreamSB
import com.lagradost.cloudstream3.extractors.StreamTape
import com.lagradost.cloudstream3.extractors.XStreamCdn
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor
import org.jsoup.nodes.Element
import android.util.Log

class Ownfmx : MainAPI() { // all providers must be an instance of MainAPI
    override var mainUrl = "https://ownfmx.com"
    override var name = "ownfmx"
    override val hasMainPage = true
    override var lang = "hi"
    override val hasDownloadSupport = true
    override val supportedTypes = setOf(
        TvType.Movie,
    )
    val headerss = mapOf(
    "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:92.0) Gecko/20100101 Firefox/92.0"
   )
    private fun toResult(post: Element): SearchResponse {
        val url = post.select("a").attr("href")
        val title = post.select("img").attr("alt").toString()
        var imageUrl = post.select("img").attr("src")
        Log.d("post",post.toString())
        //val quality = post.select(".video-label").text()
        return newMovieSearchResponse(title, url, TvType.Movie) {
            this.posterUrl = imageUrl
        }
    }
    override val mainPage = mainPageOf(
        "movies" to "Latest",
        //"$mainUrl/category/bollywood-featured" to "Bollywood",
        //"$mainUrl/language/hindi-dubbed" to "Hindi Dubbed",
        //"$mainUrl/category/hollywood-featured" to "Hollywood"
    )

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest
    ): HomePageResponse {
        
        val url = "$mainUrl/movies?page=1/"
        val document = app.get(url,headers=headerss).document
        Log.d("data",document.toString())
        Log.d("movies",document.select(".movie-card").toString())
        val home = document.select(".movie-card").mapNotNull {
            toResult(it)
        }
        return newHomePageResponse(request.name, home)
    }

  /*  private fun Element.toSearchResult(): SearchResponse? {
        val title =
            this.selectFirst("a")?.attr("title")?.trim()?.substringBefore("(") ?: return null
        val href = fixUrl(this.selectFirst("a")?.attr("href").toString())
        val posterUrl = fixUrlNull(this.selectFirst("img")?.attr("src"))

        return newMovieSearchResponse(title, href, TvType.Movie) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/?s=$query").document

        return document.select("#main .cont_display").mapNotNull {
            it.toSearchResult()
        }
    }
*/
    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        val title = document.selectFirst("div.text-muted > span")?.text().toString()
        val poster = fixUrlNull(document.selectFirst("meta[property=og:image]")?.attr("content"))
        //val yearRegex = Regex("""\d{4}""")
        //val year = yearRegex.find(
            //document.select("h2.entry-title").text()
        //)?.groupValues?.getOrNull(0)?.toIntOrNull()
       // val description = document.select("div.entry-content > p:nth-child(6)").text().trim()
       /* val actors =
            document.select("div.entry-content > p:nth-child(5)").text()
                .substringAfter("Starring by:")
                .substringBefore("Genres:").split(",").map { it }
*/
        return newMovieLoadResponse(title, url, TvType.Movie, url) {
            this.posterUrl = poster
 
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val fl = app.get(data).document.select("a[href*='https://filelions.to']").first().attr("href").toString()
        Log.d("vidhidelink",fl)
        loadExtractor(
                fl,
                "$mainUrl/",
                subtitleCallback,
                callback
            )
        return true
    }


}
