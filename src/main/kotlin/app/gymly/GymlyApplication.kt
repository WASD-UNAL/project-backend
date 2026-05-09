package app.gymly

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(
	exclude = [
		DataSourceAutoConfiguration::class
	]
)

class GymlyApplication

fun main(args: Array<String>) {
	runApplication<GymlyApplication>(*args)
}
