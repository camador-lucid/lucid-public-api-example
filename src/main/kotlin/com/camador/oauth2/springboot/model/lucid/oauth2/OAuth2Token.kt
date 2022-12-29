package com.camador.oauth2.springboot.model.lucid.oauth2

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * OAuth2 Token as described in https://developer.lucid.co/api/v1/#oauth2-resources
 */
@JsonIgnoreProperties(ignoreUnknown=true)
data class OAuth2Token(
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("client_id") val clientId: String,
    @JsonProperty("expires_in") val expiresIn: Long,
    @JsonProperty("refresh_token") val refreshToken: String,
    @JsonProperty("scopes") val scopes: List<String>,
    @JsonProperty("user_id") val userId: Long,
)