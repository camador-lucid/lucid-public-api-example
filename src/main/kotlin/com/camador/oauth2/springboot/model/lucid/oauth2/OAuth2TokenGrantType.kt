package com.camador.oauth2.springboot.model.lucid.oauth2

import com.fasterxml.jackson.annotation.JsonValue

/**
 * OAuth2 Grant Type as shown in https://developer.lucid.co/api/v1/#oauth2-resources
 */
enum class OAuth2TokenGrantType(@JsonValue val value: String) {
    AuthorizationCode("authorization_code"),
    RefreshToken("refresh_token"),
}