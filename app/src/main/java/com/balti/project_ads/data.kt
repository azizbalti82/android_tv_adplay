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

class data {
    @SuppressLint("StaticFieldLeak")
    companion object{
        val url = "http://192.168.1.122:3000"
        lateinit var bindHome: ActivityMainBinding
        lateinit var player: ExoPlayer

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
    }
}