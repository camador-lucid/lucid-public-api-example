package com.camador.oauth2.springboot.service

import org.springframework.http.MediaType
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient

@Service
class WebClientWrapper(objectMapper: ObjectMapperWrapper) {
    private val strategies = ExchangeStrategies.builder().codecs { clientDefaultCodecsConfigurer ->
        clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(objectMapper.mapper, MediaType.APPLICATION_JSON));
        clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(objectMapper.mapper, MediaType.APPLICATION_JSON));
    }.build()

    val client: WebClient = WebClient.builder().exchangeStrategies(strategies).build()
}