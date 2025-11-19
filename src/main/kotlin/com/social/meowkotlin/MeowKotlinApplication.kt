package com.social.meowkotlin

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cache.annotation.EnableCaching

@EnableCaching
@SpringBootApplication
object MeowKotlinApplication {
    @JvmStatic
    fun main(args: Array<String>) {
        SpringApplication.run(MeowKotlinApplication::class.java, *args)
    }
}
