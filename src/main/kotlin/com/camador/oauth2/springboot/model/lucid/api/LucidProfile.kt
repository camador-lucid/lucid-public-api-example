package com.camador.oauth2.springboot.model.lucid.api

/**
 * Lucid's Profile resource as described in https://developer.lucid.co/api/v1/#users-resources-profile
 */
data class LucidProfile(
    val accountId: Long,
    val email: String,
    val fullName: String,
    val id: Long,
    val username: String,
)