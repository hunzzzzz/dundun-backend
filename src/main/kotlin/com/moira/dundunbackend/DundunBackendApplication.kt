package com.moira.dundunbackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableScheduling

@EnableJpaAuditing
@EnableScheduling
@SpringBootApplication
class DundunBackendApplication

fun main(args: Array<String>) {
    runApplication<DundunBackendApplication>(*args)
}
