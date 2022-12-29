package com.camador.oauth2.springboot.model.app

import com.camador.oauth2.springboot.model.lucid.oauth2.OAuth2Token
import java.time.Instant

data class OAuth2AuthenticationData(
    val accessToken: String,
    val expiration: Instant,
    val lucidUserId: Long,
    val refreshToken: String,
    val scopes: List<String>,
) {
    constructor(response: OAuth2Token): this(
        accessToken = response.accessToken,
        expiration = Instant.now().plusSeconds(response.expiresIn),
        lucidUserId = response.userId,
        refreshToken = response.refreshToken,
        scopes = response.scopes
    )
}
