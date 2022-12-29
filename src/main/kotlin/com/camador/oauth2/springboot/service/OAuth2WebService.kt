package com.camador.oauth2.springboot.service

import com.camador.oauth2.springboot.config.LucidConfig
import com.camador.oauth2.springboot.model.lucid.oauth2.OAuth2AuthorizationCodeGrantData
import com.camador.oauth2.springboot.model.lucid.oauth2.OAuth2RefreshTokenGrantData
import com.camador.oauth2.springboot.model.lucid.oauth2.OAuth2RevokeTokenData
import com.camador.oauth2.springboot.model.lucid.oauth2.OAuth2Token
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Mono

@Service
class OAuth2WebService(
    private val lucidConfig: LucidConfig,
    private val webClient: WebClientWrapper,
) {
    /**
     * OAuth2 Token endpoints as described in https://developer.lucid.co/api/v1/#authentication
     */
    private val oAuth2TokenEndpointUrl = "${lucidConfig.lucidApiUrl}/oauth2/token"
    private val oAuth2RevokeTokenEndpointUrl = "${lucidConfig.lucidApiUrl}/oauth2/token/revoke"

    /**
     * Executes the Create Access Token call as described in https://developer.lucid.co/api/v1/#create-access-token
     */
    fun exchangeAuthorizationCodeForToken(code: String, redirectUri: String): Mono<OAuth2Token> {
        val body = OAuth2AuthorizationCodeGrantData(
            clientId = lucidConfig.clientId,
            clientSecret = lucidConfig.clientSecret,
            code = code,
            redirectUri = redirectUri,
        )

        val request = webClient.client
            .post()
            .uri(oAuth2TokenEndpointUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(body))

        return request.retrieve().bodyToMono(OAuth2Token::class.java)
    }

    /**
     * Executes the Refresh Access Token call as described in https://developer.lucid.co/api/v1/#oauth2-refresh-access-token
     */
    fun refreshOAuth2Token(refreshToken: String): Mono<OAuth2Token> {
        val body = OAuth2RefreshTokenGrantData(
            clientId = lucidConfig.clientId,
            clientSecret = lucidConfig.clientSecret,
            refreshToken = refreshToken,
        )

        val request = webClient.client
            .post()
            .uri(oAuth2TokenEndpointUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(body))

        return request.retrieve().bodyToMono(OAuth2Token::class.java)
    }

    /**
     * Executes the Revoke Access Token call as described in https://developer.lucid.co/api/v1/#oauth2-revoke-access-token
     */
    fun revokeOAuth2Token(token: String): Mono<Unit> {
        val body = OAuth2RevokeTokenData(
            clientId = lucidConfig.clientId,
            clientSecret = lucidConfig.clientSecret,
            token = token,
        )

        val request = webClient.client
            .post()
            .uri(oAuth2RevokeTokenEndpointUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(body))

        return request.exchangeToMono { response ->
            if(response.statusCode().is2xxSuccessful) {
                Mono.just(Unit)
            } else {
                response.createError()
            }
        }
    }
}