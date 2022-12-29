package com.camador.oauth2.springboot.model.lucid.oauth2

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Create Access Token body as described in https://developer.lucid.co/api/v1/#create-access-token
 */
data class OAuth2AuthorizationCodeGrantData(
    @JsonProperty("client_id") val clientId: String,
    @JsonProperty("client_secret") val clientSecret: String,
    @JsonProperty("code") val code: String,
    @JsonProperty("redirect_uri") val redirectUri: String,
) {
    @JsonProperty("grant_type") val grantType: OAuth2TokenGrantType = OAuth2TokenGrantType.AuthorizationCode
}