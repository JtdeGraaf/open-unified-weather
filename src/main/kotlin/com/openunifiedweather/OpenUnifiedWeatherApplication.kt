package com.openunifiedweather

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OpenUnifiedWeatherApplication

fun main(args: Array<String>) {
	runApplication<OpenUnifiedWeatherApplication>(*args)
}
