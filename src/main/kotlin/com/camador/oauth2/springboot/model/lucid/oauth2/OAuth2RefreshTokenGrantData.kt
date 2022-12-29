package com.camador.oauth2.springboot.model.lucid.oauth2

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Refresh Access Token as described in https://developer.lucid.co/api/v1/#oauth2-refresh-access-token
 */
data class OAuth2RefreshTokenGrantData(
    @JsonProperty("client_id") val clientId: String,
    @JsonProperty("client_secret") val clientSecret: String,
    @JsonProperty("refresh_token") val refreshToken: String,
) {
    @JsonProperty("grant_type") val grantType: OAuth2TokenGrantType = OAuth2TokenGrantType.RefreshToken
}
