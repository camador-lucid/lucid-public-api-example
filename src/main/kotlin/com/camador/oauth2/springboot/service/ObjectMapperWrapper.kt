package com.camador.oauth2.springboot.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.stereotype.Service

@Service
class ObjectMapperWrapper {
    val mapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())
}