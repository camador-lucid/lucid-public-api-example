package com.camador.oauth2.springboot.repository;

import com.camador.oauth2.springboot.model.app.User
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Serves as in-memory storage for the mock application users
 */
@Repository
class UserRepository {
    companion object {
        private val defaultUser = User(UUID.randomUUID(), "Default user")
    }

    private val storage: MutableMap<UUID, User> = mutableMapOf(
        defaultUser.id to defaultUser
    )

    fun setUser(user: User) {
        storage[user.id] = user
    }

    fun getUser(id: UUID): User? {
        return storage[id]
    }

    fun getAllUsers(): List<User> {
        return storage.values.toList()
    }
}
