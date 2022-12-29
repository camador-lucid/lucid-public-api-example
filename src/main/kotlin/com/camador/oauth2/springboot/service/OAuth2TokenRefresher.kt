package com.camador.oauth2.springboot.service

import com.camador.oauth2.springboot.model.app.OAuth2AuthenticationData
import com.camador.oauth2.springboot.repository.OAuth2Repository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

@Service
class OAuth2TokenRefresher(
    private val oAuth2Repository: OAuth2Repository,
    private val oAuth2WebService: OAuth2WebService,
) {
    companion object {
        const val expirationThresholdInSeconds = 60L
    }

    /**
     * Access token have a lifetime of 60 minutes.
     * For this reason, your application need to refresh the access token continuously.
     * This method exemplifies a way to do it.
     */
    fun getValidOAuth2Token(userId: UUID): Mono<String>? {
        val oAuth2Data = oAuth2Repository.getOAuth2Data(userId)?: return null
        val requiresRefresh = shouldRefreshAccessToken(oAuth2Data)

        /**
         * Further validation checks can be done here e.g. if you added a new feature and require more scopes
         * you may want to send the user through the authorization flow again.
         */
        if(!requiresRefresh) {
            return Mono.just(oAuth2Data.accessToken)
        }

        /**
         * If the access token expired, we make a refresh token call to get a new one
         */
        return oAuth2WebService.refreshOAuth2Token(oAuth2Data.refreshToken).map { refreshedTokenResponse ->
            oAuth2Repository.setOAuth2Data(userId, OAuth2AuthenticationData(refreshedTokenResponse))

            refreshedTokenResponse.accessToken
        }
    }

    private fun shouldRefreshAccessToken(oAuth2Data: OAuth2AuthenticationData): Boolean {
        /**
         * Give one minute of threshold, to avoid failure due to tight lifespan
         */
        val expirationLimit = Instant.now().minusSeconds(expirationThresholdInSeconds)

        return oAuth2Data.expiration.isBefore(expirationLimit)
    }
}