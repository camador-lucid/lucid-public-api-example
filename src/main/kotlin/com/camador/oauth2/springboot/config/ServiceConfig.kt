package com.camador.oauth2.springboot.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ServiceConfig(
    @Value("\${service.url}") val serviceUrl: String
)