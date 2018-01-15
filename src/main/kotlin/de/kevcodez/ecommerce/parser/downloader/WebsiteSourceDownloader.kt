package de.kevcodez.ecommerce.parser.downloader

interface WebsiteSourceDownloader {

    fun download(url: String): String
}
