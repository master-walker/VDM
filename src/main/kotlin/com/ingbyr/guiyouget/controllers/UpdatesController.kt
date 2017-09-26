package com.ingbyr.guiyouget.controllers

import com.ingbyr.guiyouget.core.OkHttpController
import com.ingbyr.guiyouget.events.*
import com.ingbyr.guiyouget.utils.CoreUtils
import org.slf4j.LoggerFactory
import tornadofx.*
import java.nio.file.Paths

class UpdatesController : Controller() {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val okhttp = OkHttpController()

    fun subscribeEvents() {
        subscribe<RequestCheckUpdatesYouGet> {
            fire(UpdateYouGetStates("[you-get] Check for updates..."))
            val remoteJson = okhttp.requestJson(CoreUtils.REMOTE_CONF_URL)
            if (remoteJson != null) {
                val youget = remoteJson["youget"] as String
                logger.debug("[you-get] remote version url $youget")
                doUpdates(CoreUtils.YOU_GET, youget)
            } else {
                //todo update failed
            }
        }

        subscribe<RequestCheckUpdatesYoutubeDL> {
            fire(UpdateYoutubeDLStates("[youtube-dl] Check for updates..."))
            val remoteJson = okhttp.requestJson(CoreUtils.REMOTE_CONF_URL)
            if (remoteJson != null) {
                val youtubedl = remoteJson["youtubedl"] as String
                logger.debug("[youtube-dl] remote version url $youtubedl")
                doUpdates(CoreUtils.YOUTUBE_DL, youtubedl)
            } else {
                //todo update failed
            }
        }
    }

    private fun doUpdates(core: String, url: String) {
        val vStr = okhttp.requestString(url)
        val v = Regex("'\\d+.+'").findAll(vStr.toString()).toList().flatMap(MatchResult::groupValues)
        val remoteVersion = v.first().substring(1, v.first().length - 1)
        when (core) {
            CoreUtils.YOU_GET -> {
                val localVersion = app.config["you-get-version"] as String
                logger.debug("[you-get] remote version is $remoteVersion, local version is $localVersion")
                if (remoteVersion > localVersion) {
                    // do updates
                    fire(UpdateYouGetStates("[you-get] New version $remoteVersion downloading..."))
                    val url = CoreUtils.yougetUpdateURL(remoteVersion)
                    logger.debug("[you-get] update url $url")
                    okhttp.downloadFile(url,
                            Paths.get(System.getProperty("user.dir"), "core", "you-get.exe").toFile(),
                            CoreUtils.YOU_GET_VERSION,
                            remoteVersion)
                } else {
                    fire(UpdateYouGetStates("[you-get] No updates"))
                    logger.debug("[you-get] no updates")
                }
            }
            CoreUtils.YOUTUBE_DL -> {
                val localVersion = app.config["youtube-dl-version"] as String
                logger.debug("[youtube-dl] remote version is $remoteVersion, local version is $localVersion")
                if (remoteVersion > localVersion) {
                    // do updates
                    fire(UpdateYoutubeDLStates("[youtube-dl] New version $remoteVersion downloading..."))
                    val url = CoreUtils.youtubedlUpdateURL(remoteVersion)
                    logger.debug("[youtube-dl] update url $url")
                    okhttp.downloadFile(url,
                            Paths.get(System.getProperty("user.dir"), "core", "youtube-dl.exe").toFile(),
                            CoreUtils.YOUTUBE_DL_VERSION,
                            remoteVersion)
                } else {
                    fire(UpdateYoutubeDLStates("[youtube-dl]No updates"))
                    logger.debug("[youtube-dl] no updates")
                }
            }
        }
    }
}