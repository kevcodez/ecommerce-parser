package de.kevcodez.ecommerce.parser

import de.kevcodez.ecommerce.parser.domain.product.Product
import de.kevcodez.ecommerce.parser.downloader.WebsiteSourceDownloader
import de.kevcodez.ecommerce.parser.exception.ParserException
import de.kevcodez.ecommerce.parser.impl.*
import java.net.URI
import java.net.URISyntaxException

class ECommerceParser(websiteSourceDownloader: WebsiteSourceDownloader) {

    private val parsers: MutableList<ProductParser> = ArrayList()

    init {
        parsers.add(AlternateParser(websiteSourceDownloader))
        parsers.add(AmazonParser(websiteSourceDownloader))
        parsers.add(BonPrixParser(websiteSourceDownloader))
        parsers.add(ConradParser(websiteSourceDownloader))
        parsers.add(CyberportParser(websiteSourceDownloader))
    }

    fun parseLink(url: String): Product {
        try {
            val domainName = getDomainName(url)
            val linkDataParser = parsers.stream()
                    .filter({ parser -> parser.matches(domainName) })
                    .findFirst()

            if (linkDataParser.isPresent) {
                return linkDataParser.get().parse(url)
            }
        } catch (exc: URISyntaxException) {
            throw ParserException("Uri could not be parsed", exc)
        }

        throw ParserException("No parser found for url " + url)
    }

    @Throws(URISyntaxException::class)
    private fun getDomainName(url: String): String {
        val uri = URI(url)
        val domain = uri.host

        return if (domain.startsWith("www.")) domain.substring(4) else domain
    }

}
