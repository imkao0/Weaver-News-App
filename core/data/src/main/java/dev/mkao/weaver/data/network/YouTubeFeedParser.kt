package dev.mkao.weaver.data.network

import dev.mkao.weaver.domain.model.VideoItem
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

/**
 * Minimal XmlPullParser-based parser for YouTube channel RSS (Atom + MediaRSS)
 * feeds. Avoids adding a Retrofit converter dependency — feeds are fetched as
 * raw XML via a plain GET and parsed here.
 *
 * Expected shape:
 * ```
 * <feed>
 *   <entry>
 *     <yt:videoId>…</yt:videoId>
 *     <published>…</published>
 *     <media:group>
 *       <media:title>…</media:title>
 *       <media:thumbnail url="…"/>
 *     </media:group>
 *   </entry>
 * </feed>
 * ```
 */
object YouTubeFeedParser {

    fun parse(xml: String, channelName: String): List<VideoItem> {
        val items = mutableListOf<VideoItem>()
        runCatching {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(xml.reader())

            var videoId = ""
            var title = ""
            var thumbnail = ""
            var published = ""
            var currentTag = ""

            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                when (event) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name
                        when (currentTag) {
                            "entry" -> {
                                videoId = ""
                                title = ""
                                thumbnail = ""
                                published = ""
                            }
                            "thumbnail" -> {
                                for (i in 0 until parser.attributeCount) {
                                    if (parser.getAttributeName(i) == "url") {
                                        thumbnail = parser.getAttributeValue(i)
                                    }
                                }
                            }
                        }
                    }
                    XmlPullParser.TEXT -> {
                        val text = parser.text?.trim().orEmpty()
                        when (currentTag) {
                            "videoId" -> videoId = text
                            "title" -> if (title.isEmpty()) title = text
                            "published" -> if (published.isEmpty()) published = text
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "entry" && videoId.isNotEmpty()) {
                            items.add(
                                VideoItem(
                                    videoId = videoId,
                                    title = title,
                                    channelName = channelName,
                                    thumbnailUrl = thumbnail.ifEmpty {
                                        "https://i4.ytimg.com/vi/$videoId/hqdefault.jpg"
                                    },
                                    watchUrl = "https://www.youtube.com/watch?v=$videoId",
                                    publishedAt = published,
                                ),
                            )
                        }
                        currentTag = ""
                    }
                }
                event = parser.next()
            }
        }
        return items
    }
}
