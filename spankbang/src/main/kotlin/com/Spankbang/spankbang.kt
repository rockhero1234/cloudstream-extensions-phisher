package com.coxju

import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class ixiporn : MainAPI() {
    override var mainUrl              = "https://spankbang.com"
    override var name                 = "Spankbang"
    override val hasMainPage          = true
    override var lang                 = "en"
    override val hasQuickSearch       = false
    override val hasDownloadSupport   = true
    override val hasChromecastSupport = true
    override val supportedTypes       = setOf(TvType.NSFW)

    override val mainPage = mainPageOf(
            "${mainUrl}/j2/channel/familyxxx/" to "Family XXX",
            "${mainUrl}/k5/channel/japan+hdv/" to "Japan HDV",
            "${mainUrl}/j3/channel/hot+wife+xxx/" to "Hot Wife XXX",
            "${mainUrl}/d6/channel/my+family+pies/" to "My Family Pies",
            "${mainUrl}/ho/channel/brazzers/" to "Brazzers",
            "${mainUrl}/6l/channel/mylf/" to "MYLF",
            "${mainUrl}/9n/channel/evil+angel/" to "Evil Angel",
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(request.data + page).document
        val home     = document.select("div.video-item").mapNotNull { it.toSearchResult() }

        return newHomePageResponse(
            list    = HomePageList(
                name               = request.name,
                list               = home,
                isHorizontalImages = true
            ),
            hasNext = true
        )
    }

    private fun Element.toSearchResult(): SearchResponse {
        val title     = fixTitle(this.select("a.thumb > img").attr("alt")).trim().toString()
        val href      = fixUrl(this.select("a.thumb").attr("href"))
        val posterUrl = fixUrlNull(this.select("a.thumb > img").attr("data-src"))

        return newMovieSearchResponse(title, href, TvType.Movie) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val searchResponse = mutableListOf<SearchResponse>()

        for (i in 1..10) {
            val document = app.get("${mainUrl}/page/$i?s=$query").document

            val results = document.select("div.video-item").mapNotNull { it.toSearchResult() }

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

        val title       = document.selectFirst("div.left > h1")?.attr("title")?.trim().toString()
        val poster      = fixUrlNull(document.selectFirst("div.play_cover")?.attr("data-src"))
        //val description = document.selectFirst("meta[property=og:description]")?.attr("content")?.trim()
    

        return newMovieLoadResponse(title, url, TvType.NSFW, url) {
            this.posterUrl = poster
            //this.plot      = description
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        val document = app.get(data).document

        document.select("div.video-player").map { res ->
            callback.invoke(
                    ExtractorLink(
                        source  = this.name,
                        name    = this.name,
                        url     = fixUrl(res.selectFirst("meta[itemprop=contentURL]")?.attr("content")?.trim().toString()),
                        referer = data,
                        quality = Qualities.Unknown.value
                    )
            )
        }

        return true
    }
}