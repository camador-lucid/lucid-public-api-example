package com.camador.oauth2.springboot.model.lucid.oauth2

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Revoke Access Token as described in https://developer.lucid.co/api/v1/#oauth2-revoke-access-token
 */
data class OAuth2RevokeTokenData(
    @JsonProperty("client_id") val clientId: String,
    @JsonProperty("client_secret") val clientSecret: String,
    @JsonProperty("token") val token: String,
)
