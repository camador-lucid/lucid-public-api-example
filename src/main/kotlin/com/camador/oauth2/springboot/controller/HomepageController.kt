package com.camador.oauth2.springboot.controller

import com.camador.oauth2.springboot.model.app.CreateUserData
import com.camador.oauth2.springboot.model.lucid.api.LucidProfile
import com.camador.oauth2.springboot.model.app.User
import com.camador.oauth2.springboot.repository.UserRepository
import com.camador.oauth2.springboot.service.LucidWebService
import com.camador.oauth2.springboot.service.OAuth2TokenRefresher
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.result.view.Rendering
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Controller
class HomepageController(
    private val oAuth2TokenRefresher: OAuth2TokenRefresher,
    private val userRepository: UserRepository,
    private val lucidWebService: LucidWebService,
) {
    companion object {
        private const val defaultUserName = "User"
    }

    /**
     * Our main app page.
     * It will get every registered user and fetch the Lucid profile information
     * for those that have granted an access token.
     */
    @GetMapping("/")
    fun index(): Mono<Rendering> {
        val users = userRepository.getAllUsers()

        val usersWithProfileMono: Mono<List<Pair<User, LucidProfile?>>> = Flux.fromIterable(users).flatMap { user ->
            /**
             * First we need to get a valid access token. Access token expire every hour
             * so this handles getting a non-expired access token.
             */
            oAuth2TokenRefresher.getValidOAuth2Token(user.id)?.let {
                it.flatMap { accessToken ->
                    /**
                     * Once we have a valid access token, we use it to get the users profile
                     */
                    lucidWebService.getUserProfile(accessToken).map { lucidProfile ->
                        user to lucidProfile
                    }
                }
            }?: Mono.just(user to null)
        }.collectList()

        return usersWithProfileMono.map { usersWithProfile ->
            Rendering.view("homepage").model(
                mapOf("users" to usersWithProfile.sortedBy { it.first.name }, "newUser" to CreateUserData(""))
            ).build()
        }
    }

    @PostMapping("/createUser")
    fun createUser(@ModelAttribute data: CreateUserData): Rendering {
        val correctedName = data.name.ifEmpty { defaultUserName }
        val newUser = User(UUID.randomUUID(), correctedName)

        userRepository.setUser(newUser)

        return Rendering.redirectTo("/").build()
    }
}