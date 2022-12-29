package com.camador.oauth2.springboot.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Holds Lucid's information:
 * - lucidUrl: base url use to load web components e.g. the OAuth2 authorization page, the Embedded Document Picker, ...
 * - lucidApiUrl: base url used for API calls e.g. Get Profile, Create Document, ...
 * - clientId: the id of your OAuth2 app as described in https://developer.lucid.co/api/v1/#app-registration
 * - clientId: the secret of your OAuth2 app as described in https://developer.lucid.co/api/v1/#app-registration
 */
@Component
class LucidConfig(
    @Value("\${lucid.webUrl}") val lucidUrl: String,
    @Value("\${lucid.apiUrl}") val lucidApiUrl: String,
    @Value("\${lucid.oauth2.clientId}") val clientId: String,
    @Value("\${lucid.oauth2.clientSecret}") val clientSecret: String,
) {
    /**
     * Lucid API Version headers as described in https://developer.lucid.co/api/v1/#headers
     */
    val lucidVersionHeader = "Lucid-Api-Version"
    val lucidVersionValue = "1"
}