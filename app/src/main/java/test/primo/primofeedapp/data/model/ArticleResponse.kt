package test.primo.primofeedapp.data.model

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "rss", strict = false)
class ArticleResponse {
    @field:Element(name = "channel", required = false)
    var channel: ChannelResponse? = null

    @Root(name = "channel", strict = false)
    class ChannelResponse {
        @field:ElementList(name = "item", inline = true, required = false)
        var articles: List<ItemResponse> = ArrayList()
    }

    @Root(name = "item", strict = false)
    class ItemResponse {
        @field:Element(name = "title", required = false)
        var title: String? = null

        @field:Element(name = "updated", required = false)
        var dateTime: String? = null

        @field:Element(name = "encoded", data = true, required = false)
        var content: String? = null
    }
}