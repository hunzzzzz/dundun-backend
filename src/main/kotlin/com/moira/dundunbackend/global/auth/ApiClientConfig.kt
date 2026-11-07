package com.moira.dundunbackend.global.auth

import com.moira.dundunbackend.domain.auth.component.GoogleLoginApiClient
import com.moira.dundunbackend.domain.auth.component.KakaoLoginApiClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import org.springframework.web.service.invoker.createClient
import java.net.http.HttpClient
import java.time.Duration

@Configuration
class ApiClientConfig {
    /**
     * KakaoLoginApiClient
     */
    @Bean
    fun kakaoApiClient(): KakaoLoginApiClient {
        // 1. 연결 타임아웃 (5초)
        val httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build()

        // 2. 읽기 타임아웃 (5초)
        val requestFactory = JdkClientHttpRequestFactory(httpClient).apply {
            setReadTimeout(Duration.ofSeconds(5))
        }

        // 3. RestClient 빌드
        val restClient = RestClient.builder()
            .requestFactory(requestFactory)
            .build()
        val adapter = RestClientAdapter.create(restClient)
        val factory = HttpServiceProxyFactory.builderFor(adapter).build()

        return factory.createClient<KakaoLoginApiClient>()
    }

    /**
     * GoogleLoginApiClient
     */
    @Bean
    fun googleApiClient(): GoogleLoginApiClient {
        // 1. 연결 타임아웃 (5초)
        val httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build()

        // 2. 읽기 타임아웃 (5초)
        val requestFactory = JdkClientHttpRequestFactory(httpClient).apply {
            setReadTimeout(Duration.ofSeconds(5))
        }

        // 3. RestClient 빌드
        val restClient = RestClient.builder()
            .requestFactory(requestFactory)
            .build()
        val adapter = RestClientAdapter.create(restClient)
        val factory = HttpServiceProxyFactory.builderFor(adapter).build()

        return factory.createClient<GoogleLoginApiClient>()
    }
}