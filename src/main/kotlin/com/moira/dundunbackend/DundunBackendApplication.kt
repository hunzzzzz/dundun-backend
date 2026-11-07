package com.moira.dundunbackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableJpaAuditing
@SpringBootApplication
class DundunBackendApplication

fun main(args: Array<String>) {
    runApplication<DundunBackendApplication>(*args)
}
