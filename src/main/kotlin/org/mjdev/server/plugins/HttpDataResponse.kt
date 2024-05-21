package org.mjdev.server.plugins

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode

@Suppress("CustomizableKDocMissingDocumentation", "ArrayInDataClass", "unused")
data class HttpDataResponse(
    val code: HttpStatusCode,
    val data: ByteArray = ByteArray(0),
    val contentType: ContentType = ContentType.Text.Plain,
    val dataProvider: () -> ByteArray = { data }
) {
    constructor(t: Throwable) : this(
        code = HttpStatusCode.InternalServerError,
        data = (t.message ?: "Unrecognized error").toByteArray()
    )

    constructor(
        code: HttpStatusCode,
        data: String,
        contentType: ContentType = ContentType.Text.Plain
    ) : this(
        code = code,
        data = data.toByteArray(),
        contentType = contentType
    )
}