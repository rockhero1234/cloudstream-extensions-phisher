package com.HindiProviders

import com.HindiProviders.StreamPlayExtractor.invokeDotmovies
import com.HindiProviders.StreamPlayExtractor.invokeDramacool
import com.HindiProviders.StreamPlayExtractor.invokeDramaday
import com.HindiProviders.StreamPlayExtractor.invokeMoviesmod
import com.HindiProviders.StreamPlayExtractor.invokeTopMovies
import com.HindiProviders.StreamPlayExtractor.invokeUhdmovies
import com.HindiProviders.StreamPlayExtractor.invokemovies4u
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.argamap
import com.lagradost.cloudstream3.utils.AppUtils
import com.lagradost.cloudstream3.utils.ExtractorLink

class StreamPlayTest : StreamPlay() {
    override var name = "StreamPlay-Test"
    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {

        val res = AppUtils.parseJson<LinkData>(data)

        argamap(
            { if (!res.isAnime && !res.isBollywood) invokeMoviesmod(
                res.title,
                res.year,
                res.season,
                res.lastSeason,
                res.episode,
                subtitleCallback,
                callback
            )
            }
        )
        return true
    }

}