import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.extractors.*
import java.util.Base64

class UpmoviesProvider : MainAPI() {
    override var mainUrl              = "https://upmovies.net"
    override var name                 = "upmovies"
    override val hasMainPage          = true
    override var lang                 = "hi"
    override val hasDownloadSupport   = true
    override val supportedTypes       = setOf(TvType.Movie,TvType.TvSeries)

    override val mainPage = mainPageOf(
        "${mainUrl}/" to "Latest",
        "${mainUrl}/movies-countries/india.html" to "India",
        "${mainUrl}/tv-series.html" to "TV Series",
        "${mainUrl}/asian-drama.html" to "Asian Dramas",
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(request.data).document
        //Log.d("Mandik","$document")
        val home = document.select("div.list-cate-detail > div.shortItem.listItem,div.category > div.shortItem.listItem").mapNotNull { it.toSearchResult() }

        return newHomePageResponse(
            list    = HomePageList(
                name               = request.name,
                list               = home,
                isHorizontalImages = false
            ),
            hasNext = true
        )
    }

    private fun Element.toSearchResult(): SearchResponse {
        val title     = fixTitle(this.select("div.title > a").text()).trim()
        val href      = fixUrl(this.select("div.title > a").attr("href"))
        val posterUrl = fixUrlNull(this.selectFirst("img")?.attr("src"))
        return newMovieSearchResponse(title, href, TvType.Movie) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val searchResponse = mutableListOf<SearchResponse>()

        for (i in 1..5) {
            val document = app.get("${mainUrl}/search-movies/$query/page-$i.html").document

            val results = document.select("div.shortItem.listItem").mapNotNull { it.toSearchResult() }

            if (!searchResponse.containsAll(results)) {
                searchResponse.addAll(results)
            } else {
                break
            }

            if (results.isEmpty()) break
        }

        return searchResponse
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        val title       = fixTitle(document.selectFirst("div.film-detail > div.about > h1")!!.text().trim())
        val poster      = fixUrlNull(document.selectFirst("div.film-detail > div.poster > img")?.attr("src")?.trim())
        val description = document.selectFirst("div.film-detail > div.textSpoiler")!!.text().trim()
        val tvType =if (document.select("#details.section-box > a").isNullOrEmpty()) TvType.Movie else TvType.TvSeries
        //Log.d("TVtype","$tvType")
        return if (tvType == TvType.TvSeries) {
            val episodes = document.select("#cont_player > #details > a").mapNotNull {
                val href = it.selectFirst("a.episode.episode_series_link")!!.attr("href")
                Log.d("href episodes", href)
                //val description = document.selectFirst("div.film-detail > div.textSpoiler").text().trim()
                val episode = it.select("#details > a").text().toString()
                Episode(
                    href,
                    episode
                )
            }
            Log.d("Phisher Epe","$episodes")

            newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                this.posterUrl = poster
                this.plot      = description
            }
        } else {
            newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl = poster
                this.plot      = description
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        val sources = mutableListOf<String>()
        val document = app.get(data).document
        document.select("#total_version > div > p.server_servername > a").forEach { element ->
            sources.add(element.attr("href").trim())
        }
        println(sources)
        // Iterate over each URL in sources
        sources.forEach { url ->
            val document = app.get(url).document
            val extractbase64 =
                document.selectFirst("script:containsData(Base64.decode)")?.data().toString()
            // Extracting Base64 encoded string using regex
            val pattern = "Base64.decode\\(\"([^\"]*)\"\\)".toRegex()
            val matchResult = pattern.find(extractbase64)
            val encodedString = matchResult?.groups?.get(1)?.value ?: ""
            val decodedstrings = encodedString.decodeBase64()

            val urlPattern = """src\s*=\s*["'](\bhttps:\/\/\S+\b)["']""".toRegex()
            urlPattern.findAll(decodedstrings).forEach { matchResult ->
                val url = matchResult.groups[1]?.value
                if (url != null) {
                            loadExtractor(
                                url,
                                subtitleCallback,
                                callback
                            )
                    }
                }
            }
            // Calling loadExtractor function with extracted links
        return true
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun String.decodeBase64(): String {
        val decodedBytes = Base64.getDecoder().decode(this)
        return String(decodedBytes, Charsets.UTF_8)
    }

class DoodWatchExtractor : DoodLaExtractor() {
    override var mainUrl = "https://dood.watch"
}
}
