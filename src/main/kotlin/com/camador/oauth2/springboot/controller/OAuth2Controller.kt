package com.camador.oauth2.springboot.controller

import com.camador.oauth2.springboot.config.LucidConfig
import com.camador.oauth2.springboot.config.ServiceConfig
import com.camador.oauth2.springboot.model.app.OAuth2AuthenticationData
import com.camador.oauth2.springboot.model.app.User
import com.camador.oauth2.springboot.repository.OAuth2Repository
import com.camador.oauth2.springboot.repository.UserRepository
import com.camador.oauth2.springboot.service.OAuth2WebService
import com.camador.oauth2.springboot.service.ObjectMapperWrapper
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.result.view.Rendering
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponents
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.net.URLDecoder
import java.net.URLEncoder
import java.time.Duration
import java.util.UUID

@Controller
class OAuth2Controller(
    private val serviceConfig: ServiceConfig,
    private val lucidConfig: LucidConfig,
    private val oAuth2WebService: OAuth2WebService,
    private val oAuth2Repository: OAuth2Repository,
    private val objectMapper: ObjectMapperWrapper,
    private val userRepository: UserRepository,
) {
    companion object {
        /**
         * OAuth2 authentication flow query parameters names
         */
        private const val ClientIdParamName = "client_id"
        private const val RedirectUriParamName = "redirect_uri"
        private const val ScopesParamName = "scopes"
        private const val StateParamName = "state"
        private const val CodeParamName = "code"

        /**
         * The requested scopes
         * Must be valid scopes as described in https://developer.lucid.co/api/v1/#access-token-scopes
         */
        private const val Scopes = "user.profile offline_access"

        /**
         * Cookies data used in the authorization flow
         */
        private const val UserCookieName = "x-user"
        private const val StateCookieName = "x-state"
        private val RedirectCookieDuration = Duration.ofMinutes(10)

        // Miscellaneous
        private const val RedirectPath = "/oauth2/redirect"
        private val redirectToHomepage = Rendering.redirectTo("/").build()
    }

    /**
     * OAuth2 User Authorization endpoint as described in https://developer.lucid.co/api/v1/#access-token-endpoints
     */
    private val oAuth2AuthorizationEndpointUrl = "${lucidConfig.lucidUrl}/oauth2/authorizeUser"

    private val redirectUrl = serviceConfig.serviceUrl + RedirectPath

    /**
     * The purpose of this method is to set security information in the request
     * and redirect to Lucid's authorization URL
     */
    @GetMapping("/oauth2/authorize/{userId}")
    fun authorize(
        response: ServerHttpResponse,
        @PathVariable userId: UUID,
    ): Rendering {
        /**
         * This depends on how each app is implement. You want to set a value that allows your app
         * to tie the obtained OAuth2 access token to the correct user in your system.
         */
        val user = userRepository.getUser(userId)
        val userJsonString = URLEncoder.encode(objectMapper.mapper.writeValueAsString(user), Charsets.UTF_8.name())
        response.addCookie(buildRedirectCookie(UserCookieName, userJsonString))


        /**
         * The state field is used to implement the Double Submit Cookie technique for CSRF protection
         * For more information look at:
         * https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html#double-submit-cookie
         */
        val state = UUID.randomUUID().toString()
        response.addCookie(buildRedirectCookie(StateCookieName, state))

        /**
         * Once we have set the necessary cookies, we redirect to the Lucid authorization URL
         */
        val lucidAuthorizationUri = buildLucidAuthorizationUrl(state)
        return Rendering.redirectTo(lucidAuthorizationUri.encode().toUriString()).build()
    }

    /**
     * This endpoint will be reached by Lucid's authorization server once the user has
     * granted access to your app, cancelled, or some error occurs.
     * TODO: include OAuth2 error and cancellation handling
     */
    @GetMapping(RedirectPath)
    fun handleRedirect(
        @CookieValue(UserCookieName) userEncodedJson: String,
        @CookieValue(StateCookieName) state: String,
        @RequestParam(StateParamName) stateParam: String,
        @RequestParam(CodeParamName) code: String,
    ): Mono<Rendering> {
        /**
         * To complete the Double Submit Cookie check, we compare both state values
         */
        if(state != stateParam) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }

        /**
         * The user sent via cookies will tell us to which app user the access token belongs to
         */
        val decodedJsonString = URLDecoder.decode(userEncodedJson, Charsets.UTF_8.name())
        val user = objectMapper.mapper.readValue(decodedJsonString, User::class.java)

        /**
         * Now we exchange the obtained code for an actual OAuth2 access token
         */
        return oAuth2WebService.exchangeAuthorizationCodeForToken(code, redirectUrl).map { tokenResponse ->
            /**
             * Finally, we persist the OAuth2 information to use in future requests
             */
            oAuth2Repository.setOAuth2Data(user.id, OAuth2AuthenticationData(tokenResponse))

            Rendering.view("handleConnectionResult").model(
                mapOf("serviceUrl" to serviceConfig.serviceUrl)
            ).build()
        }
    }

    /**
     * This endpoint revokes the users current OAuth2 tokens
     * The revoke token call will invalidate both access and refresh token regardless of which one you provide
     */
    @PostMapping("/oauth2/revoke/{userId}")
    fun revokeOAuth2Token(@PathVariable userId: UUID): Mono<Rendering> {
        val oAuth2Data = oAuth2Repository.getOAuth2Data(userId)?:
            return Mono.just(redirectToHomepage)

        /**
         * It is a good practise to revoke the OAuth2 tokens when the user disconnects their account
         */
        return oAuth2WebService.revokeOAuth2Token(oAuth2Data.accessToken).map {
            oAuth2Repository.deleteOAuth2Data(userId)
            redirectToHomepage
        }
    }

    private fun buildRedirectCookie(name: String, value: String) =  ResponseCookie
        .from(name, value)
        .path(RedirectPath)
        .maxAge(RedirectCookieDuration)
        .httpOnly(true)
        .secure(true)
        .build()

    /**
     * This method constructs the OAuth2
     * We attach add the following query parameters using the 'application/x-www-form-urlencoded' format.
     * - client_id: your OAuth2 client id
     * - redirect_uri: the URI you want the authorization code to be sent to
     * - scopes: the OAuth2 scopes your application requires
     * - state: a random string used for CSRF protection
     */
    private fun buildLucidAuthorizationUrl(state: String): UriComponents = UriComponentsBuilder
        .fromHttpUrl(oAuth2AuthorizationEndpointUrl)
        .queryParam(ClientIdParamName, lucidConfig.clientId)
        .queryParam(RedirectUriParamName, redirectUrl)
        .queryParam(ScopesParamName, Scopes)
        .queryParam(StateParamName, state)
        .build()
}