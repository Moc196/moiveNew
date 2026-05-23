package com.example

import com.example.data.api.ApiClient
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testLiveApiAndEpisodes() = runBlocking {
    println("--- START LIVE API TEST ---")
    try {
        val newlyUpdated = ApiClient.service.getNewlyUpdatedMovies(1)
        println("Status newly updated: ${newlyUpdated.status}")
        println("Found ${newlyUpdated.items.size} movies.")
        if (newlyUpdated.items.isNotEmpty()) {
            val firstMovie = newlyUpdated.items.first()
            println("First movie: name='${firstMovie.name}' slug='${firstMovie.slug}'")
            
            val detail = ApiClient.service.getMovieDetail(firstMovie.slug)
            println("Detail fetch status: ${detail.status}")
            println("Movie field exists: ${detail.movie != null}")
            println("Movie name: ${detail.movie?.name}")
            println("Episodes server list size: ${detail.episodes.size}")
            
            detail.episodes.forEachIndexed { sIdx, server ->
                println("  Server $sIdx: name='${server.serverName}' itemsCount=${server.items.size}")
                server.items.take(3).forEach { ep ->
                    println("    Episode: name='${ep.name}' slug='${ep.slug}' embed='${ep.embed}' m3u8='${ep.m3u8}'")
                }
            }
        } else {
            println("No newly updated movies found!")
        }
    } catch (e: Exception) {
        println("Error during live API test: ${e.message}")
        e.printStackTrace()
    }
    println("--- END LIVE API TEST ---")
  }
}

