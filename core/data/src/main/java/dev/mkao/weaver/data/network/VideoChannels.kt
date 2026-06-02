package dev.mkao.weaver.data.network

/**
 * YouTube news-channel RSS feeds (Atom XML, no API key required). Channel IDs
 * verified live on 2026-09-15.
 */
data class VideoChannel(
    val name: String,
    val channelId: String,
) {
    val feedUrl: String
        get() = "https://www.youtube.com/feeds/videos.xml?channel_id=$channelId"
}

object VideoChannels {
    val feeds: List<VideoChannel> = listOf(
        VideoChannel("BBC News", "UC16niRr50-MSBwiO3YDb3RA"),
        VideoChannel("CNN", "UCupvZG-5ko_eiXAupbDfxWw"),
        VideoChannel("Al Jazeera English", "UCNye-wNBqNL5ZzHSJj3l8Bg"),
        VideoChannel("Sky News", "UCoMdktPbSTixAyNGwb-UYkQ"),
        VideoChannel("DW News", "UCknLrEdhRCp1aegoMqRaCZg"),
        VideoChannel("Reuters", "UChqUTb7kYRX8-EiaN3XFrSQ"),
        VideoChannel("Associated Press", "UC52X5wxOL_s5yw0dQk7NtgA"),
        VideoChannel("euronews", "UCSrZ3UV4jOidv8ppoVuvW9Q"),
        VideoChannel("ABC News", "UCBi2mrWuNuyYy4gbM6fU18Q"),
        VideoChannel("FRANCE 24 English", "UCQfwfsi5VrQ8yKZ-UWmAEFg"),
        VideoChannel("CBC News", "UCuFFtHWoLl5fauMMD5Ww2jA"),
    )
}
