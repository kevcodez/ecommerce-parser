package de.kevcodez.ecommerce.parser.exception

/**
 * Generic unchecked parser exception that wraps every exception that may occur.
 */
class ParserException : RuntimeException {

    constructor(message: String) : super(message)

    constructor(message: String, cause: Throwable) : super(message, cause)

}
