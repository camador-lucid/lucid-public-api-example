package com.camador.oauth2.springboot.service

import com.camador.oauth2.springboot.config.LucidConfig
import com.camador.oauth2.springboot.model.lucid.api.LucidProfile
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class LucidWebService(
    private val lucidConfig: LucidConfig,
    private val webClient: WebClientWrapper,
) {
    /**
     * Executes the Get Profile API call as described in https://developer.lucid.co/api/v1/#get-profile84
     * Note how the Lucid API Version and access token are provided via headers
     */
    fun getUserProfile(accessToken: String): Mono<LucidProfile> {
        val request = webClient.client
            .get()
            .uri(lucidConfig.lucidApiUrl + "/users/me/profile")
            .header(lucidConfig.lucidVersionHeader, lucidConfig.lucidVersionValue)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")

        return request.retrieve().bodyToMono(LucidProfile::class.java)
    }
}