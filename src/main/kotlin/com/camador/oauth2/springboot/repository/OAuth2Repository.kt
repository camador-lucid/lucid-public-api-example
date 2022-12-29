package com.camador.oauth2.springboot.repository

import com.camador.oauth2.springboot.model.app.OAuth2AuthenticationData
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Serves as in-memory storage for OAuth2 authentication data
 */
@Repository
class OAuth2Repository {
    private val storage: MutableMap<UUID, OAuth2AuthenticationData> = mutableMapOf()

    fun setOAuth2Data(userId: UUID, data: OAuth2AuthenticationData) {
        storage[userId] = data
    }

    fun getOAuth2Data(userId: UUID): OAuth2AuthenticationData? {
        return storage[userId]
    }

    fun deleteOAuth2Data(userId: UUID) {
        storage.remove(userId)
    }
}