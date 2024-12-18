package com.balti.project_ads

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.View
import com.balti.project_ads.databinding.ActivityMainBinding
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import java.io.File

class data {
    @SuppressLint("StaticFieldLeak")
    companion object{
        val url = "http://192.168.1.122:3000"
        lateinit var bindHome: ActivityMainBinding

        //for exo player
        private lateinit var player: ExoPlayer
        private lateinit var cache: SimpleCache
        private lateinit var cacheDataSourceFactory: CacheDataSource.Factory

        fun initializeExoPlayer(c:Context) {
            // Initialize the cache (Do this once, ideally in onCreate)
            val cacheDir = File(c.cacheDir, "exo_cache") // Directory to store the cache
            cache = SimpleCache(cacheDir, LeastRecentlyUsedCacheEvictor(500 * 1024 * 1024)) // 100MB cache size

            val dataSourceFactory = DefaultDataSourceFactory(c, Util.getUserAgent(c, "ads"))
            cacheDataSourceFactory = CacheDataSource.Factory()
                .setCache(cache)
                .setUpstreamDataSourceFactory(dataSourceFactory)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
            // Initialize the player
            player = ExoPlayer.Builder(c).build()
        }

        fun showMedia(context: Context, mediaType: String, uri: Uri?) {
            // Stop and release the player if it already exists
            if (::player.isInitialized) {
                player.stop()
                player.release()
            }

            // Reinitialize the Player
            player = ExoPlayer.Builder(context).build()

            // Hide all media views initially
            bindHome.mediaAudio.cancelAnimation()
            bindHome.mediaImage.visibility = View.GONE
            bindHome.mediaAudio.visibility = View.GONE
            bindHome.mediaVideo.visibility = View.GONE
            bindHome.noMedia.visibility = View.GONE

            when (mediaType.lowercase()) {
                "image" -> {
                    bindHome.mediaImage.visibility = View.VISIBLE
                    // Show image
                    Glide.with(context)
                        .load(uri) // URI of the image
                        .into(bindHome.mediaImage)
                }
                "video" -> {
                    // Show the ExoPlayer view
                    bindHome.mediaVideo.visibility = View.VISIBLE
                    bindHome.mediaVideo.player = player

                    // Disable controls (ensure that 'useController' is false)
                    bindHome.mediaVideo.useController = false

                    val mediaItem = uri?.let { MediaItem.fromUri(it) }

                    // Create media source using CacheDataSourceFactory
                    mediaItem?.let {
                        val mediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                            .createMediaSource(it)

                        // Set the media source to ExoPlayer
                        player.setMediaSource(mediaSource)
                    }

                    // Set repeat mode (repeat the media)
                    player.repeatMode = Player.REPEAT_MODE_ONE
                    player.prepare()
                    player.play()
                }
                "music" -> {
                    // Show the audio view
                    bindHome.mediaAudio.visibility = View.VISIBLE
                    bindHome.mediaAudio.playAnimation()

                    // Play audio using ExoPlayer
                    val mediaItem = uri?.let { MediaItem.fromUri(it) }

                    mediaItem?.let {
                        val mediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                            .createMediaSource(it)

                        // Set the media source to ExoPlayer
                        player.setMediaSource(mediaSource)
                        player.repeatMode = Player.REPEAT_MODE_ONE
                        bindHome.mediaVideo.useController = false
                        player.prepare()
                        player.play()
                    }
                }
                else -> {
                    // Show the "no media" page
                    bindHome.noMedia.visibility = View.VISIBLE
                }
            }
        }

        /*
        fun showMedia(context: Context, mediaType: String, uri: Uri?) {
            // Stop and release the player if it already exists
            if (::player.isInitialized) {
                player.stop()
                player.release()
            }
            //reinitialize the Player
            player = ExoPlayer.Builder(context).build()
            //first hide all media views
            bindHome.mediaAudio.cancelAnimation()
            bindHome.mediaImage.visibility = View.GONE
            bindHome.mediaAudio.visibility = View.GONE
            bindHome.mediaVideo.visibility = View.GONE
            bindHome.noMedia.visibility = View.GONE

            when (mediaType.lowercase()) {
                "image" -> {
                    bindHome.mediaImage.visibility = View.VISIBLE
                    // Show image
                    Glide.with(context)
                        .load(uri) // URI of the image
                        .into(bindHome.mediaImage)

                }
                "video" -> {
                    //show the exoplayer view
                    bindHome.mediaVideo.visibility = View.VISIBLE
                    // Play audio using ExoPlayer
                    bindHome.mediaVideo.player = player

                    // Disable controls (ensure that 'useController' is false)
                    bindHome.mediaVideo.useController = false

                    val mediaItem = uri?.let { MediaItem.fromUri(it) }
                    if (mediaItem != null) {
                        player.setMediaItem(mediaItem)
                    }

                    // Set repeat mode (repeat the media)
                    player.repeatMode = Player.REPEAT_MODE_ONE
                    player.prepare()
                    player.play()

                }
                "music" -> {
                    //show the exoplayer view
                    bindHome.mediaAudio.visibility = View.VISIBLE
                    bindHome.mediaAudio.playAnimation()
                    // Play audio using ExoPlayer
                    val mediaItem = uri?.let { MediaItem.fromUri(it) }

                    if (mediaItem != null) {
                        player.setMediaItem(mediaItem)
                        player.repeatMode = Player.REPEAT_MODE_ONE
                        bindHome.mediaVideo.useController = false
                        player.prepare()
                        player.play()
                    }
                }
                else -> {
                    //show empty page
                    bindHome.noMedia.visibility = View.VISIBLE
                }
            }
        }

         */
    }
}